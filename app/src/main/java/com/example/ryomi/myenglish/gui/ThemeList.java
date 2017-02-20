package com.example.ryomi.myenglish.gui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.FirebaseDBHeaders;
import com.example.ryomi.myenglish.gui.widgets.GUIUtils;
import com.example.ryomi.myenglish.gui.widgets.ThemeListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
 * the list is stored in the database instaed of it being hard-coded in code
 * so that users (teachers) can rearrange the ordering/content for their class
 */
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
        DatabaseReference ref = db.getReference(FirebaseDBHeaders.THEMES);
        ProgressBar loading = (ProgressBar) findViewById(R.id.theme_list_loading);
        firebaseAdapter = new ThemeListAdapter(ref, loading);

        listView.setAdapter(firebaseAdapter);

        Toolbar appBar = (Toolbar)findViewById(R.id.theme_list_tool_bar);
        setSupportActionBar(appBar);

        //Initializing NavigationView
        DatabaseReference ref2 = db.getReference(

        );
        NavigationView navigationView = (NavigationView) findViewById(R.id.theme_list_navigation_drawer);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
             @Override
             public boolean onNavigationItemSelected(MenuItem menuItem) {
                 return true;
             }
         });

        // Initializing Drawer Layout and ActionBarToggle
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.activity_theme_list);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,drawerLayout,appBar,R.string.theme_list_navigation_drawer_open, R.string.theme_list_navigation_drawer_close){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

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
        if (firebaseAdapter != null)
            firebaseAdapter.cleanup();
    }
}
