package com.example.ryomi.myenglish.gui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import android.widget.SearchView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.connectors.WikiDataAPISearchConnector;
import com.example.ryomi.myenglish.db.FirebaseDBHeaders;
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
    private SearchResultsAdapter adapter = null;
    //initial row count of search results
    private final int defaultRowCt = 10;
    //we can do continue=# to get the results from that number of results
    //increment when we want more rows
    private Integer currentRowCt = defaultRowCt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_interests);

        //the only thing we need on the app bar
        Toolbar appBar = (Toolbar)findViewById(R.id.search_interests_tool_bar);
        setSupportActionBar(appBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searcher = new EntitySearcher(
                new WikiDataAPISearchConnector(
                        WikiBaseEndpointConnector.JAPANESE)
        );


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_interests_app_bar_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search_interests_app_bar_search).getActionView();
        searchView.setSubmitButtonEnabled(true);
        //make it so the user can search without having to tap the search box (UX)
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.requestFocusFromTouch();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        addSearchFunctionality(searchView);

        return true;
    }

    private void addSearchFunctionality(final SearchView searchView){
        //we have to instantiate this in the java file instead of the xml file
        //because we want this to be a header view of the listview
        //but we can't replicate that relation in the xml file.
        final ListView list = (ListView) findViewById(R.id.search_results_result_list);

        //making sure buttons for search results the user already has are disabled
        String userID = "temp";
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(FirebaseDBHeaders.USER_INTERESTS+"/"+userID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WikiDataEntryData> userInterests = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    WikiDataEntryData data = child.getValue(WikiDataEntryData.class);
                    userInterests.add(data);
                }

                adapter = new SearchResultsAdapter(SearchInterests.this, userInterests);
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
        }
    }
}
