package com.linnca.pelicann.gui;

import android.os.AsyncTask;
import android.os.Bundle;
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
import com.linnca.pelicann.userinterestcontrols.EntitySearcher;
import com.linnca.pelicann.userinterestcontrols.UserInterestAdder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchInterests extends Fragment {
    private EntitySearcher searcher;
    private SearchView searchView;
    private ListView list;
    private View headerView;
    private SearchResultsAdapter adapter = null;
    //initial row count of search results
    private int defaultRowCt = 10;
    private int recommendationCt = 5;
    //we can do continue=# to get the results from that number of results
    //increment when we want more rows
    private Integer currentRowCt = defaultRowCt;

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
        list = (ListView) view.findViewById(R.id.search_results_result_list);
        headerView = inflater.inflate(R.layout.inflatable_search_interest_recommendations_header, list, false);

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setSubmitButtonEnabled(true);
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
                    public boolean onQueryTextChange(String s) {
                        if (s.length() > 1)
                            populateResults(s);
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
            String[] queryList = {query, currentRowCt.toString()};
            conn.execute(queryList);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private class SearchConnection extends AsyncTask< String[], Integer, List<WikiDataEntryData> > {
        @Override
        protected List<WikiDataEntryData> doInBackground(String[]... queryList){
            String[] query = queryList[0];
            List<WikiDataEntryData> result = new ArrayList<>();
            try {
                result = searcher.search(query[0], Integer.parseInt(query[1]));
            } catch (Exception e){
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<WikiDataEntryData> result){
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
