package com.example.ryomi.myenglish.gui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.gui.widgets.GUIUtils;
import com.example.ryomi.myenglish.gui.widgets.ThemeListAdapter;
import com.example.ryomi.myenglish.gui.widgets.ThemeListViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
* The list is synced with the db
* so if I change the data in the db
* it will automatically update here.
* Will this take up too much network data usage?
* */
public class ThemeList extends AppCompatActivity {
    private FirebaseRecyclerAdapter firebaseAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_list);

        final RecyclerView listView = (RecyclerView) findViewById(R.id.theme_list_list);
        listView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("themes");
        firebaseAdapter = new ThemeListAdapter(ThemeData.class, R.layout.inflatable_theme_list_list_item,
                ThemeListViewHolder.class, ref);

        listView.setAdapter(firebaseAdapter);

        Toolbar appBar = (Toolbar)findViewById(R.id.theme_list_tool_bar);
        appBar.setTitle(R.string.theme_list_app_bar_title);
        setSupportActionBar(appBar);

        BottomNavigationView nav = (BottomNavigationView)findViewById(R.id.theme_list_bottom_navigation_view);
        GUIUtils.prepareBottomNavigationView(this, nav);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.theme_list_app_bar_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.theme_list_app_bar_search).getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));



        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }


    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this,query,Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    protected void onDestroy(){
        super.onDestroy();
        firebaseAdapter.cleanup();
    }
}
