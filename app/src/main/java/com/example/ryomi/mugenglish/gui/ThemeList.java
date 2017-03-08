package com.example.ryomi.mugenglish.gui;

import android.app.SearchManager;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.db.FirebaseDBHeaders;
import com.example.ryomi.mugenglish.db.datawrappers.ThemeCategory;
import com.example.ryomi.mugenglish.gui.widgets.GUIUtils;
import com.example.ryomi.mugenglish.gui.widgets.ThemeListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 * the list is stored in the database instead of it being hard-coded in code
 * so that users (teachers) can rearrange the ordering/content for their class
 */
/*
* The list is synced with the db
* so if I change the data in the db
* it will automatically update here.
* Will this take up too much network data usage?
* */
public class ThemeList extends AppCompatActivity {
    private Toolbar appBar;
    private RecyclerView listView;
    private FirebaseRecyclerAdapter firebaseAdapter;
    private ProgressBar loading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_list);

        appBar = (Toolbar)findViewById(R.id.theme_list_tool_bar);
        setSupportActionBar(appBar);

        listView = (RecyclerView) findViewById(R.id.theme_list_list);
        listView.setLayoutManager(new LinearLayoutManager(this));
        loading = (ProgressBar) findViewById(R.id.theme_list_loading);

        populateNavigationDrawer();

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

    private void populateThemes(ThemeCategory category){
        if (firebaseAdapter != null)
            firebaseAdapter.cleanup();
        loading.setVisibility(View.VISIBLE);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(FirebaseDBHeaders.THEMES);
        Query query = ref.orderByChild(FirebaseDBHeaders.THEMES_category).equalTo(category.getIndex());
        firebaseAdapter = new ThemeListAdapter(query, loading);

        listView.setAdapter(firebaseAdapter);

        appBar.setTitle(category.getTitle());
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

    private void populateNavigationDrawer(){
        //Initializing NavigationView
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(
            FirebaseDBHeaders.THEME_CATEGORIES
        );
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<ThemeCategory> categories = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ThemeCategory category = snapshot.getValue(ThemeCategory.class);
                    categories.add(category);
                }

                //order by index
                //ie 1.1.1, 1.2.1, 10.2.2
                Collections.sort(categories, new ThemeCategoryIndexComparator());
                final DrawerLayout drawerLayout = (DrawerLayout) findViewById((R.id.activity_theme_list));
                NavigationView navigationDrawer = (NavigationView) findViewById(R.id.theme_list_navigation_drawer);
                Menu navigationDrawerMenu = navigationDrawer.getMenu();

                for (final ThemeCategory category :categories){
                    MenuItem item = navigationDrawerMenu.add(category.getTitle());
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            populateThemes(category);
                            drawerLayout.closeDrawers();
                            return false;
                        }
                    });
                }

                //populate themes with first category
                populateThemes(categories.get(0));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private class ThemeCategoryIndexComparator implements Comparator<ThemeCategory> {
        @Override
        public int compare(ThemeCategory category1, ThemeCategory category2) {
            String index1 = category1.getIndex();
            String index2 = category2.getIndex();
            String[] arr1 = index1.split("\\.");
            String[] arr2 = index2.split("\\.");

            int i=0;
            while(i<arr1.length || i<arr2.length){
                if(i<arr1.length && i<arr2.length){
                    if(Integer.parseInt(arr1[i]) < Integer.parseInt(arr2[i])){
                        return -1;
                    }else if(Integer.parseInt(arr1[i]) > Integer.parseInt(arr2[i])){
                        return 1;
                    }
                } else if(i<arr1.length){
                    if(Integer.parseInt(arr1[i]) != 0){
                        return 1;
                    }
                } else if(i<arr2.length){
                    if(Integer.parseInt(arr2[i]) != 0){
                        return -1;
                    }
                }

                i++;
            }

            return 0;
        }


    }




    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (firebaseAdapter != null)
            firebaseAdapter.cleanup();
    }
}
