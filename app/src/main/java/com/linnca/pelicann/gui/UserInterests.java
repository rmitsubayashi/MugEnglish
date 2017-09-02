package com.linnca.pelicann.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.db.datawrappers.WikiDataEntryData;
import com.linnca.pelicann.gui.widgets.ToolbarState;
import com.linnca.pelicann.gui.widgets.UserInterestAdapter;
import com.linnca.pelicann.userinterestcontrols.UserInterestAdder;

import java.util.ArrayList;
import java.util.List;

/*
* We are using an external library for the FABs
* because Android doesn't directly support FAB menus.
* We can make our own if we have time
* */
public class UserInterests extends Fragment {
    private final String TAG = "UserInterests";
    private ViewGroup mainLayout;
    private RecyclerView listView;
    private Query userInterestQuery;
    private ValueEventListener userInterestQueryListener;
    private UserInterestAdapter userInterestListAdapter = null;
    private Snackbar undoSnackBar;
    private RecyclerView.OnItemTouchListener undoOnTouchListener;
    private ActionMode actionMode;
    private FloatingActionButton searchFAB;
    private UserInterestListener userInterestListener;

    interface UserInterestListener {
        void userInterestsToSearchInterests();
        void setToolbarState(ToolbarState state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_user_interests, container, false);
        mainLayout = view.findViewById(R.id.user_interests_layout);
        listView = view.findViewById(R.id.user_interests_list);
        searchFAB = view.findViewById(R.id.user_interests_search_fab);
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            loadUser();
            populateFABs();
        }
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        userInterestListener.setToolbarState(
                new ToolbarState(ToolbarState.NO_TITLE_WITH_SPINNER,
                        false, null)
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
            userInterestListener = (UserInterestListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    //called by the main activity (which has access to the spinner for the filter)
    public void filterUserInterests(int filter){
        if (userInterestListAdapter != null)
            userInterestListAdapter.setFilter(filter);
    }

    private void loadUser(){
        setListListeners();
        //populateFABs();
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.lesson_details_item_menu, menu);
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
    /*
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
    } */

    private void setAdapter(){
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userInterestListAdapter = new UserInterestAdapter(
                userID,
                getUserInterestAdapterListener()
        );

        DatabaseReference userInterestRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseDBHeaders.USER_INTERESTS + "/" +
                        userID);
        //order alphabetically
        userInterestQuery = userInterestRef.orderByChild("pronunciation");

        userInterestQueryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WikiDataEntryData> userInterests = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    WikiDataEntryData interest = snapshot.getValue(WikiDataEntryData.class);
                    userInterests.add(interest);
                }

                userInterestListAdapter.setInterests(userInterests);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userInterestQuery.addValueEventListener(userInterestQueryListener);


        listView.setAdapter(userInterestListAdapter);
    }

    private void setListListeners(){
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (actionMode != null) {
                    return false;
                }

                // Start the CAB using the ActionMode.Callback defined above
                actionMode = getActivity().startActionMode(mActionModeCallback);
                view.setSelected(true);
                return true;
            }
        });
        setAdapter();
    }


    private void populateFABs(){
        searchFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInterestListener.userInterestsToSearchInterests();
            }
        });

    }

    private UserInterestAdapter.UserInterestAdapterListener getUserInterestAdapterListener(){
        return new UserInterestAdapter.UserInterestAdapterListener() {
            @Override
            public void onItemRemoved(WikiDataEntryData item) {
                showUndoSnackBar(item);
            }
        };
    }
    
    private void showUndoSnackBar(final WikiDataEntryData data){
        if (undoOnTouchListener != null) {
            listView.removeOnItemTouchListener(undoOnTouchListener);
            undoOnTouchListener = null;
        }

        undoSnackBar = Snackbar.make(mainLayout, R.string.user_interests_list_item_deleted,
                Snackbar.LENGTH_INDEFINITE);

        undoOnTouchListener = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (undoSnackBar.isShown()) {
                    undoSnackBar.dismiss();
                }
                //don't want the scroll listener attached forever
                listView.removeOnItemTouchListener(this);
                undoOnTouchListener = null;
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        };
        listView.addOnItemTouchListener(undoOnTouchListener);
        undoSnackBar.setAction(R.string.user_interests_list_item_deleted_undo,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //undo
                        UserInterestAdder userInterestAdder = new UserInterestAdder();
                        userInterestAdder.justAdd(data);
                        if (undoOnTouchListener != null) {
                            listView.removeOnItemTouchListener(undoOnTouchListener);
                            undoOnTouchListener = null;
                        }
                    }
                }
        );

        undoSnackBar.show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (userInterestQuery != null && userInterestQueryListener != null)
            userInterestQuery.removeEventListener(userInterestQueryListener);
    }
}
