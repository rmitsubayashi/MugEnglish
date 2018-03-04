package com.linnca.pelicann.onboarding;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.AndroidNetworkConnectionChecker;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;
import com.linnca.pelicann.searchinterests.SearchResultsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataAPISearchConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;
import pelicann.linnca.com.corefunctionality.searchinterests.RecommendationGetter;
import pelicann.linnca.com.corefunctionality.searchinterests.SearchHelper;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class Onboarding3v2 extends Fragment {
    //manages thread work
    private SearchHelper searchHelper;
    private ReentrantLock lock = new ReentrantLock();
    private final WikiBaseEndpointConnector wikiBaseEndpointConnector = new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE);
    private final int peopleToChoose = 3;
    //what the user has chosen
    private List<WikiDataEntity> people = new ArrayList<>(peopleToChoose);
    private SearchView searchview;
    private RecyclerView listview;
    private TextView instructionsTextview;
    private TextView itemsLeftTextview;
    private TextView itemsToAddTextview;
    private TextView finishedTextview;
    private SearchResultsAdapter adapter;
    private RecommendationGetter recommendationGetter;
    //to see if this is the first time the user typed
    private boolean typed = false;
    //to communicate with the activity once we are done adding all entities
    private Onboarding3v2Listener listener;
    //for handler result
    private final int SUCCESS = 1;
    private final int FAILURE = 2;

    interface Onboarding3v2Listener {
        void onAllEntitiesAdded(List<WikiDataEntity> allEntities);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        searchHelper = new SearchHelper(
                new WikiDataAPISearchConnector(WikiBaseEndpointConnector.JAPANESE,7),
                wikiBaseEndpointConnector
        );
        NetworkConnectionChecker networkConnectionChecker = new
                AndroidNetworkConnectionChecker(getContext());
        recommendationGetter = new RecommendationGetter(3, networkConnectionChecker, new FirebaseDB(), 3);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding3v2, parent, false);
        searchview = view.findViewById(R.id.onboarding3v2_searchview);
        listview = view.findViewById(R.id.onboarding3v2_list);
        instructionsTextview = view.findViewById(R.id.onboarding3v2_instructions);
        itemsLeftTextview = view.findViewById(R.id.onboarding3v2_items_left_textview);
        itemsToAddTextview = view.findViewById(R.id.onboarding3v2_items_to_add);
        finishedTextview = view.findViewById(R.id.onboarding3v2_finished);
        refreshItemsLeftViews();
        SearchResultsAdapter.SearchResultsAdapterListener searchResultsAdapterListener = getSearchResultsAdapterListener();
        adapter = new SearchResultsAdapter(searchResultsAdapterListener);
        listview.setAdapter(adapter);
        listview.setLayoutManager(new LinearLayoutManager(getContext()));
        searchview.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() > 0){
                    //if this is the first time the user typed,
                    //remove the instructions and show how many he has left
                    if (!typed){
                        instructionsTextview.setVisibility(View.GONE);
                        itemsLeftTextview.setVisibility(View.VISIBLE);
                    }
                    //check if the currently displayed results is empty.
                    //if it is empty, show a loading progress bar.
                    //make sure to check if there already is a loading progress bar
                    // displayed
                    lock.lock();
                    try {
                        if (adapter.getSearchResultSize() == 0 && !adapter.isLoading()) {
                            adapter.showLoading();
                        }
                    } finally {
                        lock.unlock();
                    }
                    final Handler handler = getSearchHandler(query);
                    SearchHelper.SearchHelperListener searchHelperListener = new SearchHelper.SearchHelperListener() {
                        @Override
                        public void onSuccess(List<WikiDataEntity> resultList) {
                            Message message = handler.obtainMessage(SUCCESS, resultList);
                            handler.sendMessage(message);
                        }

                        @Override
                        public void onFailure() {
                            Message message = handler.obtainMessage(FAILURE);
                            handler.sendMessage(message);
                        }
                    };
                    searchHelper.search(query, searchHelperListener);
                }
                return true;
            }
        });
        return view;
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
            listener = (Onboarding3v2Listener)context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    //used so the search thread can connect to the UI thread
    @SuppressWarnings("unchecked")
    private Handler getSearchHandler(final String query){
        //main looper makes sure the handler runs on the UI thread
        return new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message inputMessage){
                switch (inputMessage.what){
                    case SUCCESS :
                        List<WikiDataEntity> result;
                        try {
                            //I catch the class cast exception but Android Studio
                            // still shows the unchecked cast warning??
                            result = (List<WikiDataEntity>) inputMessage.obj;
                        } catch (ClassCastException e){
                            e.printStackTrace();
                            return;
                        }
                        //if the user exited the screen and we can't update the list
                        if (!Onboarding3v2.this.isVisible()){
                            return;
                        }
                        //don't do anything if the list is null
                        //(different from an empty list)
                        if (result == null){
                            return;
                        }

                        //the adapter might not be loaded yet
                        if (adapter != null) {
                            lock.lock();
                            try {
                                //filter out all of the already added items
                                result.removeAll(people);
                                //remove entities we will never need
                                searchHelper.removeDisambiguationPages(result);
                                searchHelper.removeWikiNewsArticlePages(result);
                                //display empty state if the results are empty
                                if (result.size() == 0) {
                                    WikiDataEntity emptyState = new WikiDataEntity();
                                    emptyState.setWikiDataID(adapter.VIEW_TYPE_EMPTY_STATE_WIKIDATA_ID);
                                    emptyState.setLabel(query);
                                    result.add(emptyState);
                                }
                                adapter.updateEntries(result);
                            } finally {
                                lock.unlock();
                            }

                        }
                        break;
                    case FAILURE:
                        Toast.makeText(getContext(), R.string.no_connection, Toast.LENGTH_SHORT)
                                .show();
                        adapter.setOffline();
                        break;

                }
            }
        };
    }

    private SearchResultsAdapter.SearchResultsAdapterListener getSearchResultsAdapterListener(){
        return new SearchResultsAdapter.SearchResultsAdapterListener() {
            @Override
            public void onAddInterest(final WikiDataEntity data) {
                //clear the search text
                searchview.setQuery("", false);
                searchview.clearFocus();
                //give the data to the adapter
                // so it can give feedback to the user when he adds it
                adapter.setAddedWikiDataEntity(data);
                people.add(data);

                adapter.showRecommendations(new ArrayList<WikiDataEntity>(1), false);
                refreshItemsLeftViews();
                recommendationGetter.getNewRecommendations(data, new ArrayList<WikiDataEntity>(1),
                    new RecommendationGetter.RecommendationGetterListener() {
                        @Override
                        public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {

                        }

                        @Override
                        public void onNoConnection() {

                        }
                    }
                );
            }

            @Override
            public void onLoadMoreRecommendations() {

            }
        };
    }





    private boolean enoughItems(){
        return  people.size() >= peopleToChoose;
    }

    private void refreshItemsLeftViews(){
        if (enoughItems()){
            searchview.setEnabled(false);
            searchview.setVisibility(View.GONE);
            itemsToAddTextview.setVisibility(View.GONE);
            itemsLeftTextview.setVisibility(View.GONE);
            finishedTextview.setVisibility(View.VISIBLE);
            listener.onAllEntitiesAdded(people);

            return;
        }

        //we allow users to add more than enough items
        int peopleLeft = peopleToChoose - people.size();
        peopleLeft = peopleLeft > 0 ? peopleLeft : 0;

        String itemsLeftText = getString(R.string.onboarding3v2_items_left,
                peopleLeft);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(itemsLeftText);
        //the text may change, so don't put the spans in manually.
        //we are assuming that we will never have to add more than 9
        // of each type &&
        // that the string will not contain any integers
        int itemsLeftTextLength = itemsLeftText.length();
        for (int i=0; i<itemsLeftTextLength; i++){
            char c = itemsLeftText.charAt(i);
            if (Character.isDigit(c)){
                //no need to style if the remaining count is 0
                // (to indicate that the user is done)
                if (c == '0')
                    continue;
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                        ThemeColorChanger.getColorFromAttribute(R.attr.color500, getContext())
                );
                spannableStringBuilder.setSpan(colorSpan, i, i+1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        itemsLeftTextview.setText(spannableStringBuilder);

        StringBuilder itemsToAddStringBuilder = new StringBuilder("");
        for (WikiDataEntity person : people){
            itemsToAddStringBuilder.append(person.getLabel());
            itemsToAddStringBuilder.append("  ");
        }
        itemsToAddTextview.setText(itemsToAddStringBuilder);
    }
}
