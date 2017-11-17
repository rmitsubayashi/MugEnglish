package com.linnca.pelicann.searchinterests;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private SearchView searchView;
    private RecyclerView list;
    private SearchResultsAdapter adapter = null;
    //initial row count of search results
    private final int defaultRowCt = 10;
    //so only one thread can edit the UI at one time
    private Lock lock = new ReentrantLock();
    //we can do continue=# to get the results from that number of results
    //increment when we want more rows
    private Integer currentRowCt = defaultRowCt;
    //so we can filter out user interests we don't need
    private final List<WikiDataEntryData> userInterests = new ArrayList<>();
    //manages threads for searching
    private SearchHelper searchHelper;
    //helps get recommendations
    private RecommendationGetter recommendationGetter;

    private SearchInterestsListener searchInterestsListener;

    public interface SearchInterestsListener {
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //so we can access the search view
        setHasOptionsMenu(true);
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
        searchHelper = new SearchHelper(
                new WikiDataAPISearchConnector(WikiBaseEndpointConnector.JAPANESE)
        );
        recommendationGetter = new RecommendationGetter(
                5, db, 3
        );
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
        //we want to allow searching after we grab the user's current interests
        //because we need to be able to filter out user interests the user already has
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
                    public boolean onQueryTextChange(String query) {
                        if (query.length() > 0){
                            searchHelper.search(getSearchHandler(query), query);
                        }
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

                        //also update the list we have saved locally.
                        //we stop listening to the user interests after we initially retrieve it,
                        //so updating the list manually is required
                        userInterests.add(data);

                        //clear the search text
                        searchView.setQuery("", false);
                        searchView.clearFocus();

                        //give the data to the adapter
                        // so it can give feedback to teh user
                        adapter.setRecommendationWikiDataEntryData(data);
                        //get recommendations for the user
                        recommendationGetter.getNewRecommendations(userInterests, data.getWikiDataID(),
                                getRecommendationGetterListener());
                    }
                };

                //we are only adding one, but the method can handle more than one
                List<WikiDataEntryData> dataList = new ArrayList<>(1);
                dataList.add(data);
                db.addUserInterests(dataList, onResultListener);
            }

            @Override
            public void onLoadMoreRecommendations(WikiDataEntryData data){
                recommendationGetter.loadMoreRecommendations(userInterests, data.getWikiDataID(),
                        getRecommendationGetterListener());

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalyticsHeaders.PARAMS_ACTION_TYPE, "Load More Recommendations");
                bundle.putInt(FirebaseAnalytics.Param.VALUE, recommendationGetter.getToDisplayRecommendationCt());
                firebaseLog.logEvent(FirebaseAnalyticsHeaders.EVENT_ACTION, bundle);
            }
        };
    }

    private RecommendationGetter.RecommendationGetterListener getRecommendationGetterListener(){
        return new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {
                adapter.showRecommendations(results);
                if (!showLoadMoreButton){
                    adapter.removeFooter();
                }
            }
        };
    }

    //used so the search thread can connect to the UI thread
    private Handler getSearchHandler(final String query){
        //main looper makes sure the handler runs on the UI thread
        return new Handler(Looper.getMainLooper()){
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
    }

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
