package com.linnca.pelicann.gui;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

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
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.db.datawrappers.WikiDataEntryData;
import com.linnca.pelicann.gui.widgets.SearchResultsAdapter;
import com.linnca.pelicann.gui.widgets.ToolbarState;
import com.linnca.pelicann.userinterestcontrols.EntitySearcher;
import com.linnca.pelicann.userinterestcontrols.UserInterestAdder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SearchInterests extends Fragment {
    private EntitySearcher searcher;
    private SearchView searchView;
    private ListView list;
    private View headerView;
    private SearchResultsAdapter adapter = null;
    //initial row count of search results
    private int defaultRowCt = 10;
    private int recommendationCt = 5;
    //so we don't show search results queried before the curently shown result
    private int queryOrderSent = 0;
    private Lock lock = new ReentrantLock();
    private int queryOrderReceived = 0;
    //we can do continue=# to get the results from that number of results
    //increment when we want more rows
    private Integer currentRowCt = defaultRowCt;

    private SearchInterestsListener searchInterestsListener;

    interface SearchInterestsListener {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_search_interests, container, false);
        list = view.findViewById(R.id.search_results_result_list);
        headerView = inflater.inflate(R.layout.inflatable_search_interest_recommendations_header, list, false);

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        searchInterestsListener.setToolbarState(
                new ToolbarState(getString(R.string.fragment_search_interests_title), true, false, null)
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
        //since we are doing real time, we can disable the submit button
        searchView.setSubmitButtonEnabled(false);
        //make it so the user can search without having to tap the search box (UX)
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.requestFocusFromTouch();

        addSearchFunctionality();
    }

    private void addSearchFunctionality(){

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(
                FirebaseDBHeaders.USER_INTERESTS + "/" +
                userID
        );

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WikiDataEntryData> userInterests = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    WikiDataEntryData data = child.getValue(WikiDataEntryData.class);
                    userInterests.add(data);
                }

                SearchResultsAdapter.OnAddInterestListener onAddInterestListener = getOnAddInterestListener();
                adapter = new SearchResultsAdapter(getContext(), userInterests, onAddInterestListener);
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

    private SearchResultsAdapter.OnAddInterestListener getOnAddInterestListener(){
        return new SearchResultsAdapter.OnAddInterestListener() {
            @Override
            public void onAddInterest(WikiDataEntryData data) {
                //add the interest
                UserInterestAdder userInterestAdder = new UserInterestAdder();
                userInterestAdder.findPronunciationAndCategoryThenAdd(data);
                //change UI
                if (headerView == null) {
                    //shouldn't happen
                    return;
                }
                ((TextView) headerView.findViewById(R.id.search_interests_recommendations_title)).setText(
                        getString(R.string.search_interests_recommendations_title, data.getLabel())
                );
                //just want to make sure we aren't adding the header twice
                if (list.getHeaderViewsCount() == 0) {
                    list.addHeaderView(headerView);
                }
                //populate list with recommended items
                populateRecommendations(data.getWikiDataID());
            }
        };
    }

    private void populateRecommendations(String wikiDataID){
        //clear first
        adapter.updateEntries(new ArrayList<WikiDataEntryData>());
        DatabaseReference recommendationRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.RECOMMENDATION_MAP + "/" +
                        wikiDataID
        );
        Query recommendationRefQuery = recommendationRef
                .orderByChild(FirebaseDBHeaders.RECOMMENDATION_MAP_EDGE_COUNT)
                .limitToLast(recommendationCt);
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
                adapter.updateEntries(recommendations);
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
        private String query;
        private int maxRowCount;
        private int searchOrder;

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
        @Override
        protected List<WikiDataEntryData> doInBackground(SearchConnectionHelperClass... queryList){
            SearchConnectionHelperClass helper = queryList[0];
            String query = helper.getQuery();
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
                adapter.updateEntries(result);
            }
            if (headerView != null){
                list.removeHeaderView(headerView);
            }
        }
    }
}
