package com.linnca.pelicann.userinterests;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.mainactivity.widgets.ToolbarSpinnerAdapter;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;
import com.linnca.pelicann.userinterestcontrols.UserInterestAdder;
import com.linnca.pelicann.userinterestcontrols.UserInterestRemover;

import java.util.ArrayList;
import java.util.List;

/*
* We are using an external library for the FABs
* because Android doesn't directly support FAB menus.
* We can make our own if we have time
* */
public class UserInterests extends Fragment {
    private final String TAG = "UserInterests";
    private FirebaseAnalytics firebaseLog;
    private ViewGroup mainLayout;
    private RecyclerView listView;
    private Query userInterestQuery;
    private ValueEventListener userInterestQueryListener;
    private UserInterestAdapter userInterestListAdapter = null;
    private Snackbar undoSnackBar;
    private RecyclerView.OnItemTouchListener undoOnTouchListener;
    private FloatingActionButton searchFAB;
    private UserInterestListener userInterestListener;
    private String userID;
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    public interface UserInterestListener {
        void userInterestsToSearchInterests();
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseLog = FirebaseAnalytics.getInstance(getActivity());
        firebaseLog.setCurrentScreen(getActivity(), TAG, TAG);
        firebaseLog.setUserId(userID);
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
            actionModeCallback = getActionModeCallback();
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.user_interests_item_menu, menu);
    }

    /*
    * UserInterestRemover.removeUserInterest(data, userID);
                showUndoSnackBar(data);
    * */

    //called by the main activity (which has access to the spinner for the filter)
    public void filterUserInterests(int filter){
        if (userInterestListAdapter != null)
            userInterestListAdapter.setFilter(filter);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Filter");
        bundle.putString(FirebaseAnalytics.Param.VALUE, ToolbarSpinnerAdapter.getSpinnerStateIdentifier(filter));
        firebaseLog.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void loadUser(){
        setListListeners();
        //populateFABs();
    }

    private void setAdapter(){
        userInterestListAdapter = new UserInterestAdapter(
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
        setAdapter();
        registerForContextMenu(listView);
    }


    private void populateFABs(){
        searchFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionMode != null){
                    actionMode.finish();
                }
                userInterestListener.userInterestsToSearchInterests();
            }
        });

    }

    private UserInterestAdapter.UserInterestAdapterListener getUserInterestAdapterListener(){
        return new UserInterestAdapter.UserInterestAdapterListener() {
            @Override
            public void onItemClicked(int position){
                if (actionMode != null) {
                    toggleSelection(position);
                }
            }
            @Override
            public boolean onItemLongClicked(int position){
                if (actionMode == null) {
                    actionMode = getActivity().startActionMode(actionModeCallback);
                }

                toggleSelection(position);

                return true;

            }
        };
    }
    
    private void showUndoSnackBar(final List<WikiDataEntryData> dataToRecover){
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
                        for (WikiDataEntryData data : dataToRecover) {
                            userInterestAdder.justAdd(data);
                        }
                        if (undoOnTouchListener != null) {
                            listView.removeOnItemTouchListener(undoOnTouchListener);
                            undoOnTouchListener = null;
                        }
                    }
                }
        );

        undoSnackBar.show();
    }

    private ActionMode.Callback getActionModeCallback(){
        return new ActionMode.Callback() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate (R.menu.user_interests_item_menu, menu);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    getActivity().getWindow().setStatusBarColor(
                            ContextCompat.getColor(getContext(), R.color.gray700)
                    );
                }
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.user_interest_item_menu_delete:
                        List<WikiDataEntryData> toRemove =
                                userInterestListAdapter.getSelectedItems();
                        for (WikiDataEntryData data : toRemove){
                            UserInterestRemover.removeUserInterest(data, userID);
                        }
                        showUndoSnackBar(toRemove);
                        mode.finish();
                        return true;

                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                userInterestListAdapter.clearSelection();
                actionMode = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    getActivity().getWindow().setStatusBarColor(
                            ContextCompat.getColor(getContext(), R.color.lblue700)
                    );
                }
            }
        };

    }

    private void toggleSelection(int position){
        userInterestListAdapter.toggleSelection(position);
        int count = userInterestListAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();

        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (userInterestQuery != null && userInterestQueryListener != null)
            userInterestQuery.removeEventListener(userInterestQueryListener);
    }
}
