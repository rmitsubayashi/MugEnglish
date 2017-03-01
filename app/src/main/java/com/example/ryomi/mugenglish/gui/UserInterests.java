package com.example.ryomi.mugenglish.gui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.db.FirebaseDBHeaders;
import com.example.ryomi.mugenglish.db.datawrappers.WikiDataEntryData;
import com.example.ryomi.mugenglish.gui.widgets.GUIUtils;
import com.example.ryomi.mugenglish.gui.widgets.UserInterestAdapter;
import com.example.ryomi.mugenglish.gui.widgets.UserInterestViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
* We are using an external library for the FABs
* because Android doesn't directly support FAB menus.
* We can make our own if we have time
* */
public class UserInterests extends AppCompatActivity {
    FirebaseRecyclerAdapter firebaseAdapter = null;
    ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interests);
        BottomNavigationView nav = (BottomNavigationView)findViewById(R.id.user_interests_bottom_navigation_view);
        GUIUtils.prepareBottomNavigationView(this, nav);

        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            loadUser();
        }
    }

    private void loadUser(){
        Toolbar appBar = (Toolbar)findViewById(R.id.user_interests_tool_bar);
        setSupportActionBar(appBar);

        setListListeners();
        populateFABs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_interests_app_bar_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.user_interests_app_bar_search).getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                updateAdapter(s);
                return true;
            }
        });

        return true;
    }
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.theme_details_item_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return true;
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    };

    //for search
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    //for search
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            updateAdapter(query);
        }
    }

    private void updateAdapter(String query){
        //clear adapter
        if (firebaseAdapter != null)
            firebaseAdapter.cleanup();

        RecyclerView listView = (RecyclerView)findViewById(R.id.user_interests_list);
        //update the list as necessary

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(FirebaseDBHeaders.USER_INTERESTS + "/"+userID);
        if (query.equals("")){
            //since firebase doesn't support multiple ordering,
            //we can't search for the characters typed i.e. "長友~"
            //and sort by the pronunciation "ながとも"
            //which is stupid, but until then, just sort by pronunciation
            //only when the user doesn't have anything typed.
            //shouldn't be too much of a bother once the user has searched for something...
            firebaseAdapter = new UserInterestAdapter(
                    WikiDataEntryData.class, R.layout.inflatable_user_interests_list_item,
                    UserInterestViewHolder.class, ref.orderByChild("pronunciation")
            );
        } else {
            //ends at string + (high unicode character)
            // which means all Japanese characters are included
            firebaseAdapter = new UserInterestAdapter(
                    WikiDataEntryData.class, R.layout.inflatable_user_interests_list_item,
                    UserInterestViewHolder.class, ref.orderByChild("label").startAt(query).endAt(query + "\uFFFF")
            );
        }


        listView.setAdapter(firebaseAdapter);
    }

    private void setListListeners(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.user_interests_list);
        //needed for the firebase adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (actionMode != null) {
                    return false;
                }

                // Start the CAB using the ActionMode.Callback defined above
                actionMode = UserInterests.this.startActionMode(mActionModeCallback);
                view.setSelected(true);
                return true;
            }
        });
        //updating the list with an empty query (default)
        updateAdapter("");



    }

    private void populateFABs(){

        FloatingActionButton facebookButton = (FloatingActionButton) findViewById(R.id.user_interests_fab_facebook);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(UserInterests.this, FacebookInterests.class);
                startActivity(intent);*/
                Toast.makeText(UserInterests.this,"近日公開",Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton searchButton = (FloatingActionButton) findViewById(R.id.user_interests_fab_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserInterests.this, SearchInterests.class);
                startActivity(intent);
            }
        });

        FloatingActionButton twitterButton = (FloatingActionButton) findViewById(R.id.user_interests_fab_twitter);
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UserInterests.this,"近日公開",Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (firebaseAdapter != null)
            firebaseAdapter.cleanup();
    }
}
