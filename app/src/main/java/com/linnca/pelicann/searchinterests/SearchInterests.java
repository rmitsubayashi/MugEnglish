package com.linnca.pelicann.searchinterests;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.R;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataAPISearchConnector;
import com.linnca.pelicann.db.FirebaseAnalyticsHeaders;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;
import com.linnca.pelicann.userinterestcontrols.EntitySearcher;
import com.linnca.pelicann.userinterestcontrols.UserInterestAdder;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SearchInterests extends Fragment {
    private FirebaseAnalytics firebaseLog;
    private String userID;
    private final String TAG = "SearchInterests";
    private EntitySearcher searcher;
    private SearchView searchView;
    private RecyclerView list;
    private SearchResultsAdapter adapter = null;
    //initial row count of search results
    private final int defaultRowCt = 10;
    private final int defaultRecommendationCt = 1;
    private final int incrementRecommendationCt = 1;
    private int recommendationCt = defaultRecommendationCt;
    //so we don't show search results queried before the currently shown result
    private int queryOrderSent = 0;
    private final Lock lock = new ReentrantLock();
    private int queryOrderReceived = 0;
    //we can do continue=# to get the results from that number of results
    //increment when we want more rows
    private Integer currentRowCt = defaultRowCt;
    //so  we can filter out user interests we don't need
    private final List<WikiDataEntryData> userInterests = new ArrayList<>();
    //we get more than we need to guarantee populating the recommendations
    //so save it and when the user loads more,
    // we can reference this first
    private List<WikiDataEntryData> savedRecommendations;

    private SearchInterestsListener searchInterestsListener;

    public interface SearchInterestsListener {
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //so we can access the search view
        setHasOptionsMenu(true);

        searcher = new EntitySearcher(
                new WikiDataAPISearchConnector(
                        WikiBaseEndpointConnector.JAPANESE)
        );
        firebaseLog = FirebaseAnalytics.getInstance(getActivity());
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseLog.setCurrentScreen(getActivity(), TAG, TAG);
        firebaseLog.setUserId(userID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_search_interests, container, false);
        list = view.findViewById(R.id.search_results_result_list);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        searchInterestsListener.setToolbarState(
                new ToolbarState("", true, false, null)
        );
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        implementListeners(context);
    }

    //must implement to account for lower APIs
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        implementListeners(activity);
    }

    private void implementListeners(Context context){
        try {
            searchInterestsListener = (SearchInterestsListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setQueryHint(getString(R.string.search_interests_search_hint));
        //since we are doing real time, we can disable the submit button
        searchView.setSubmitButtonEnabled(false);
        //make it so the user can search without having to tap the search box (UX)
        searchView.setIconified(false);
        //not sure if we want the user to be able to iconify the searchView
        //because there's nothing else really on the app bar
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.requestFocusFromTouch();

        addSearchFunctionality();
    }

    private void addSearchFunctionality(){

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(
                FirebaseDBHeaders.USER_INTERESTS + "/" +
                userID
        );

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    WikiDataEntryData data = child.getValue(WikiDataEntryData.class);
                    userInterests.add(data);
                }



                list.setLayoutManager(new LinearLayoutManager(getContext()));
                SearchResultsAdapter.SearchResultsAdapterListener searchResultsAdapterListener = getSearchResultsAdapterListener();
                adapter = new SearchResultsAdapter(searchResultsAdapterListener);
                list.setAdapter(adapter);

                //only want to attach the listener after user info is initially loaded
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(final String s) {
                        //after a short time after the query is entered,
                        //if no new text has been entered, search.
                        //if there is new text, do nothing
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (searchView.getQuery().toString().equals(s)) {
                                    if (s.length() > 1) {
                                        //search
                                        populateResults(s);

                                    }
                                }
                            }
                        }, 200);

                        return true;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private SearchResultsAdapter.SearchResultsAdapterListener getSearchResultsAdapterListener(){
        return new SearchResultsAdapter.SearchResultsAdapterListener() {
            @Override
            public void onAddInterest(final WikiDataEntryData data) {
                //add the interest
                UserInterestAdder userInterestAdder = new UserInterestAdder();
                userInterestAdder.findPronunciationAndCategoryThenAdd(data);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, data.getWikiDataID());
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, data.getLabel());
                firebaseLog.logEvent(FirebaseAnalyticsHeaders.EVENT_ADD_ITEM, bundle);

                //also to the list we have saved locally
                userInterests.add(data);

                //reset recommendation count
                recommendationCt = defaultRecommendationCt;

                //clear the search text
                searchView.setQuery("", false);
                searchView.clearFocus();

                //populate list with recommended items
                populateRecommendations(data);
            }

            @Override
            public void onLoadMoreRecommendations(WikiDataEntryData data){
                recommendationCt += incrementRecommendationCt;
                //check first if we can still populate the recommendations with
                //the initial list we grabbed
                if (savedRecommendations.size() >= recommendationCt){
                    List<WikiDataEntryData> toDisplay = savedRecommendations.subList(0, recommendationCt);
                    adapter.showRecommendations(toDisplay);
                } else {
                    populateRecommendations(data);
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalyticsHeaders.PARAMS_ACTION_TYPE, "Load More Recommendations");
                bundle.putInt(FirebaseAnalytics.Param.VALUE, recommendationCt);
                firebaseLog.logEvent(FirebaseAnalyticsHeaders.EVENT_ACTION, bundle);
            }
        };
    }

    private void populateRecommendations(WikiDataEntryData data){
        adapter.setRecommendationWikiDataEntryData(data);
        final DatabaseReference recommendationRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.RECOMMENDATION_MAP + "/" +
                        data.getWikiDataID()
        );
        //pigeon-hole so we are guaranteed to get recommendations
        int fetchCt = userInterests.size() + recommendationCt;
        Query recommendationRefQuery = recommendationRef
                .orderByChild(FirebaseDBHeaders.RECOMMENDATION_MAP_EDGE_COUNT)
                .limitToLast(fetchCt);
        recommendationRefQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WikiDataEntryData> recommendations = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    WikiDataEntryData recommendationData = child.child(FirebaseDBHeaders.RECOMMENDATION_MAP_EDGE_DATA)
                            .getValue(WikiDataEntryData.class);
                    recommendations.add(recommendationData);
                }
                //we need to reverse this because the recommendations are ordered by count
                // (1,3,5,10, etc)
                //so we can get the most recommended one on top
                Collections.reverse(recommendations);
                recommendations.removeAll(userInterests);
                savedRecommendations = new ArrayList<>(recommendations);
                if (recommendations.size() >= recommendationCt) {
                    List<WikiDataEntryData> toDisplay = recommendations.subList(0, recommendationCt);
                    adapter.showRecommendations(toDisplay);
                } else {
                    adapter.showRecommendations(recommendations);
                    //hide the show more footer so the user can't load more (there aren't any)
                    adapter.removeFooter();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void populateResults(String query){
        try {
            SearchConnection conn = new SearchConnection();
            SearchConnectionHelperClass helper = new SearchConnectionHelperClass(query, currentRowCt, queryOrderSent++);
            conn.execute(helper);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private class SearchConnectionHelperClass {
        private final String query;
        private final int maxRowCount;
        private final int searchOrder;

        SearchConnectionHelperClass(String query, int maxRowCount, int searchOrder) {
            this.query = query;
            this.maxRowCount = maxRowCount;
            this.searchOrder = searchOrder;
        }

        public String getQuery() {
            return query;
        }

        int getMaxRowCount() {
            return maxRowCount;
        }

        int getSearchOrder() {
            return searchOrder;
        }
    }

    private class SearchConnection extends AsyncTask< SearchConnectionHelperClass, Integer, List<WikiDataEntryData> > {
        private String query;
        @Override
        protected List<WikiDataEntryData> doInBackground(SearchConnectionHelperClass... queryList){
            SearchConnectionHelperClass helper = queryList[0];
            query = helper.getQuery();
            int maxRowCount = helper.getMaxRowCount();
            final int searchOrder = helper.getSearchOrder();
            List<WikiDataEntryData> result = new ArrayList<>();
            try {
                result = searcher.search(query, maxRowCount);
                //if another query that came after this one finished first,
                //don't execute this query
                lock.lock();
                try {
                    if (queryOrderReceived > searchOrder) {
                        lock.unlock();
                        return null;
                    }
                    queryOrderReceived = searchOrder;
                } finally {
                    lock.unlock();
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<WikiDataEntryData> result){
            //if the user exited the screen and we can't update the list
            if (!SearchInterests.this.isVisible()){
                return;
            }
            //don't do anything if this query comes before another one already reflected
            if (result == null){
                return;
            }
            //the adapter might not be loaded yet
            if (adapter != null){
                //filter out all of the user's interests
                result.removeAll(userInterests);
                //remove disambiguation pages (we will never need them)
                removeDisambiguationPages(result);
                //display empty state if the results are empty
                if (result.size() == 0){
                    WikiDataEntryData emptyState = new WikiDataEntryData();
                    emptyState.setWikiDataID(adapter.VIEW_TYPE_EMPTY_STATE_WIKIDATA_ID);
                    emptyState.setLabel(query);
                    result.add(emptyState);
                }
                adapter.updateEntries(result);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, query);
                firebaseLog.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
            }
        }
    }

    private void removeDisambiguationPages(List<WikiDataEntryData> result){
        for (Iterator<WikiDataEntryData> iterator = result.iterator(); iterator.hasNext();){
            WikiDataEntryData data = iterator.next();
            String description = data.getDescription();
            //not sure if these cover every case
            if (description.equals("ウィキペディアの曖昧さ回避ページ") ||
                    description.equals("ウィキメディアの曖昧さ回避ページ") ||
                    description.equals("Wikipedia disambiguation page") ||
                    description.equals("Wikimedia disambiguation page")){
                iterator.remove();
            }
        }
    }
}
