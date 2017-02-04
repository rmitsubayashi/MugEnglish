package com.example.ryomi.myenglish.gui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.connectors.WikiDataAPISearchConnector;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.example.ryomi.myenglish.gui.widgets.SearchResultsAdapter;
import com.example.ryomi.myenglish.userinterestcontrols.EntitySearcher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchInterests extends AppCompatActivity {
    private EntitySearcher searcher;
    //initial row count of search results
    private final int defaultRowCt = 10;
    //we can do continue=# to get the results from that number of results
    //increment when we want more rows
    private Integer currentRowCt = defaultRowCt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_interests);

        searcher = new EntitySearcher(
                new WikiDataAPISearchConnector(
                        WikiBaseEndpointConnector.JAPANESE)
        );

        createGUI();


    }

    private void createGUI(){
        //we have to instantiate this in the java file instead of the xml file
        //because we want this to be a header view of the listview
        //but we can't replicate that relation in the xml file.
        LinearLayout searchWrapper = (LinearLayout)getLayoutInflater().inflate(R.layout.inflatable_search_interests_search_bar, null);
        final SearchView searchView = (SearchView)searchWrapper.findViewById(R.id.search_interests_search_bar_view);
        //font
        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) searchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.parseColor("#737373"));

        //make it so the user can search without having to tap the search box (UX)
        searchView.setIconified(false);
        searchView.setFocusable(true);
        searchView.requestFocusFromTouch();


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        //attaches the manager to the search view
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //we want the submit button
        searchView.setSubmitButtonEnabled(true);

        //making sure buttons for search results the user already has are disabled
        String userID = "temp";
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("userInterests/"+userID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WikiDataEntryData> userInterests = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    WikiDataEntryData data = child.getValue(WikiDataEntryData.class);
                    userInterests.add(data);
                }

                ListView list = (ListView) findViewById(R.id.search_results_result_list);
                list.setAdapter(new SearchResultsAdapter(SearchInterests.this, userInterests));

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


        //dynamic padding
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        int paddingHorizontal = (int)(width * 0.1);
        int paddingTop = (int)(height * 0.1);
        int paddingBottom = (int)(height * 0.05);
        searchWrapper.setPadding(paddingHorizontal,paddingTop,
                paddingHorizontal,paddingBottom);

        ListView list = (ListView) findViewById(R.id.search_results_result_list);
        list.addHeaderView(searchWrapper);
    }

    //we don't need to handle the intent in the
    //onCreate constructor because we will never call this from an
    //outside activity?
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            populateResults(query);
        }
    }

    private void populateResults(String query){
        ListView resultList = (ListView) findViewById(R.id.search_results_result_list);
        try {
            SearchConnection conn = new SearchConnection();
            String[] queryList = {query, currentRowCt.toString()};
            List<WikiDataEntryData> results = conn.execute(queryList).get();
            HeaderViewListAdapter headerAdapter = (HeaderViewListAdapter)(resultList.getAdapter());
            //the adapter might not be loaded yet
            if (headerAdapter.getWrappedAdapter() != null){
                SearchResultsAdapter mainAdapter =
                        (SearchResultsAdapter) (headerAdapter.getWrappedAdapter());
                mainAdapter.updateEntries(results);
            }
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
    }
}
