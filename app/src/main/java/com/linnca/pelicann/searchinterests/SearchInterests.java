package com.linnca.pelicann.searchinterests;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.linnca.pelicann.R;
import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataAPISearchConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseAnalyticsHeaders;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;
import com.linnca.pelicann.userinterestcontrols.AddUserInterestHelper;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SearchInterests extends Fragment {
    private FirebaseAnalytics firebaseLog;
    private Database db;
    private final String TAG = "SearchInterests";
    private EndpointConnectorReturnsXML connector;
    private SearchView searchView;
    private RecyclerView list;
    private SearchResultsAdapter adapter = null;
    //initial row count of search results
    private final int defaultRowCt = 10;
    private final int defaultRecommendationCt = 5;
    private final int incrementRecommendationCt = 5;
    private int recommendationCt = defaultRecommendationCt;
    //so we don't show search results queried before the currently shown result
    private AtomicInteger queryReceivedMaxOrder = new AtomicInteger(0);
    //so only one thread can edit the UI at one time
    private Lock lock = new ReentrantLock();
    //we can do continue=# to get the results from that number of results
    //increment when we want more rows
    private Integer currentRowCt = defaultRowCt;
    //so we can filter out user interests we don't need
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
        connector = new WikiDataAPISearchConnector(
                WikiBaseEndpointConnector.JAPANESE);
        firebaseLog = FirebaseAnalytics.getInstance(getActivity());
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseLog.setCurrentScreen(getActivity(), TAG, TAG);
        firebaseLog.setUserId(userID);
        try {
            db = (Database) getArguments().getSerializable(MainActivity.BUNDLE_DATABASE);
        } catch (Exception e){
            e.printStackTrace();
            //hard code a new database instance
            db = new FirebaseDB();
        }
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
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onUserInterestsQueried(List<WikiDataEntryData> queriedUserInterests) {
                userInterests.clear();
                userInterests.addAll(queriedUserInterests);
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
                                        search(s, queryReceivedMaxOrder.incrementAndGet());

                                    }
                                }
                            }
                        }, 300);

                        return true;
                    }
                });

                //we don't want to keep listening because
                // we want to show the recommendations after a
                // user has added an interest,
                // but if we kept on listening, it would
                // attach a new adapter instead
                db.cleanup();
            }
        };
        db.getUserInterests(onResultListener);
    }



    private SearchResultsAdapter.SearchResultsAdapterListener getSearchResultsAdapterListener(){
        return new SearchResultsAdapter.SearchResultsAdapterListener() {
            @Override
            public void onAddInterest(final WikiDataEntryData data) {
                //add the interest
                OnResultListener onResultListener = new OnResultListener() {
                    @Override
                    public void onUserInterestsAdded() {
                        //log event
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, data.getWikiDataID());
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, data.getLabel());
                        firebaseLog.logEvent(FirebaseAnalyticsHeaders.EVENT_ADD_ITEM, bundle);

                        //in the background thread, find the item's
                        //classification and pronunciation
                        AddUserInterestHelper addUserInterestHelper = new AddUserInterestHelper();
                        addUserInterestHelper.addClassification(data);
                        addUserInterestHelper.addPronunciation(data);

                        //also update the list we have saved locally
                        userInterests.add(data);

                        //reset recommendation count
                        recommendationCt = defaultRecommendationCt;

                        //clear the search text
                        searchView.setQuery("", false);
                        searchView.clearFocus();

                        //populate list with recommended items
                        populateRecommendations(data);
                    }
                };

                //we are only adding one, but the method can handle more than one
                List<WikiDataEntryData> dataList = new ArrayList<>(1);
                dataList.add(data);
                db.addUserInterests(dataList, onResultListener);
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
                    //grab more from the database
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
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onRecommendationsQueried(List<WikiDataEntryData> recommendations) {
                savedRecommendations = new ArrayList<>(recommendations);
                if (recommendations.size() > recommendationCt) {
                    List<WikiDataEntryData> toDisplay = recommendations.subList(0, recommendationCt);
                    adapter.showRecommendations(toDisplay);
                } else {
                    adapter.showRecommendations(recommendations);
                    //hide the show more footer so the user can't load more (there aren't any)
                    adapter.removeFooter();
                }
            }
        };
        db.getRecommendations(userInterests, data.getWikiDataID(),
                recommendationCt, onResultListener);
    }

    private void search(final String query, final int queryOrder){
        //main looper makes sure the handler runs on the UI thread
        final Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message inputMessage){
                List<WikiDataEntryData> result = (List<WikiDataEntryData>)inputMessage.obj;
                //if the user exited the screen and we can't update the list
                if (!SearchInterests.this.isVisible()){
                    return;
                }
                //don't do anything if the list is null
                //(different from an empty list)
                if (result == null){
                    return;
                }
                //check if this is the most recent.
                //if not, don't show this to the user
                if (queryReceivedMaxOrder.get() != queryOrder){
                    return;
                }

                //the adapter might not be loaded yet
                if (adapter != null){
                    lock.lock();
                    try {
                        //filter out all of the user's interests
                        result.removeAll(userInterests);
                        //remove disambiguation pages (we will never need them)
                        removeDisambiguationPages(result);
                        //display empty state if the results are empty
                        if (result.size() == 0) {
                            WikiDataEntryData emptyState = new WikiDataEntryData();
                            emptyState.setWikiDataID(adapter.VIEW_TYPE_EMPTY_STATE_WIKIDATA_ID);
                            emptyState.setLabel(query);
                            result.add(emptyState);
                        }
                        adapter.updateEntries(result);
                        //log
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, query);
                        firebaseLog.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
                    } finally {
                        lock.unlock();
                    }
                }
            }
        };
        EndpointConnectorReturnsXML.OnFetchDOMListener onFetchDOMListener = new EndpointConnectorReturnsXML.OnFetchDOMListener() {
            @Override
            public boolean shouldStop() {
                return false;
            }

            @Override
            public void onStop() {

            }

            @Override
            public void onFetchDOM(Document result) {
                NodeList resultNodes = result.getElementsByTagName(WikiDataAPISearchConnector.ENTITY_TAG);
                List<WikiDataEntryData> searchResults = new ArrayList<>();
                int nodeCt = resultNodes.getLength();
                for (int i=0; i<nodeCt; i++){
                    Node n = resultNodes.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE)
                    {
                        String wikiDataID = "";
                        String label = "";
                        String description = "";

                        Element e = (Element)n;
                        if (e.hasAttribute("id")) {
                            wikiDataID = e.getAttribute("id");
                        }
                        if(e.hasAttribute("label")) {
                            label = e.getAttribute("label");
                        }

                        if(e.hasAttribute("description")) {
                            description = e.getAttribute("description");
                        }

                        //set pronunciation to label for now.
                        //if there is a pronunciation field, use that
                        searchResults.add(new WikiDataEntryData(label, description, wikiDataID, label, WikiDataEntryData.CLASSIFICATION_NOT_SET));
                    }
                }
                Message message = handler.obtainMessage(0, searchResults);
                message.sendToTarget();
            }
        };
        List<String> queryList = new ArrayList<>(1);
        queryList.add(query);
        connector.fetchDOMFromGetRequest(onFetchDOMListener, queryList);
    }

    /*private class SearchConnectionHelperClass {
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
    }*/

    private void removeDisambiguationPages(List<WikiDataEntryData> result){
        for (Iterator<WikiDataEntryData> iterator = result.iterator(); iterator.hasNext();){
            WikiDataEntryData data = iterator.next();
            String description = data.getDescription();
            //not sure if these cover every case
            if (description != null &&
                    (description.equals("ウィキペディアの曖昧さ回避ページ") ||
                    description.equals("ウィキメディアの曖昧さ回避ページ") ||
                    description.equals("Wikipedia disambiguation page") ||
                    description.equals("Wikimedia disambiguation page"))
                ){
                iterator.remove();
            }
        }
    }
}
