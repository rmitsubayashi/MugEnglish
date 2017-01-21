package com.example.ryomi.myenglish.gui;

import android.animation.LayoutTransition;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.connectors.WikiDataAPIGetConnector;
import com.example.ryomi.myenglish.connectors.WikiDataAPISearchConnector;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.example.ryomi.myenglish.gui.widgets.SearchResultsAdapter;
import com.example.ryomi.myenglish.userinterestcontrols.EntitySearcher;

import java.util.ArrayList;
import java.util.List;

public class SearchInterests extends AppCompatActivity {
    private EntitySearcher searcher;
    //initital row count of search results
    private final int defaultRowCt = 20;
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
        SearchView searchView = (SearchView)getLayoutInflater().inflate(R.layout.inflatable_search_interests_search_bar, null);
        //font
        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) searchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.parseColor("#737373"));

        //make it so the user can search without having to tap the search box
        searchView.setIconified(false);
        searchView.setFocusable(true);
        searchView.requestFocusFromTouch();


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        //attaches the manager to the search view
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //we want the submit button
        searchView.setSubmitButtonEnabled(true);

        /*
        * Still need to work on a nice way to do search bar animation
        * */
        int searchBarId = searchView.getContext().getResources().getIdentifier("android:id/search_bar", null, null);
        LinearLayout searchBar = (LinearLayout) searchView.findViewById(searchBarId);
        searchBar.setLayoutTransition(new LayoutTransition());
        //end transition

        ListView list = (ListView) findViewById(R.id.search_results_result_list);
        list.setAdapter(new SearchResultsAdapter(this));

        LinearLayout centerWrapper = new LinearLayout(this);
        //this gravity makes sure the search icon starts in the center
        //not layout_gravity
        centerWrapper.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        centerWrapper.setLayoutParams(param);
        centerWrapper.setOrientation(LinearLayout.VERTICAL);
        centerWrapper.addView(searchView);
        //dynamic padding
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        int paddingHorizontal = (int)(width * 0.1);
        int paddingVertical = (int)(height * 0.1);
        centerWrapper.setPadding(paddingHorizontal,paddingVertical,
                paddingHorizontal,paddingVertical);

        list.addHeaderView(centerWrapper);
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
            ListView testOutput = (ListView) findViewById(R.id.search_results_result_list);
            try {
                SearchConnection conn = new SearchConnection();
                String[] queryList = {query, currentRowCt.toString()};
                List<WikiDataEntryData> results = conn.execute(queryList).get();
                HeaderViewListAdapter headerAdapter = (HeaderViewListAdapter)(testOutput.getAdapter());
                SearchResultsAdapter mainAdapter = (SearchResultsAdapter) (headerAdapter.getWrappedAdapter());
                mainAdapter.updateEntries(results);
            } catch (Exception e){
                e.printStackTrace();
            }
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
