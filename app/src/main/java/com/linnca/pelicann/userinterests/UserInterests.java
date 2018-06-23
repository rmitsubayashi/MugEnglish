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
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.AndroidNetworkConnectionChecker;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;
import com.linnca.pelicann.mainactivity.ToolbarState;

import java.util.List;

import pelicann.linnca.com.corefunctionality.db.DBConnectionResultListener;
import pelicann.linnca.com.corefunctionality.db.DBUserInterestListener;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class UserInterests extends Fragment {
    public static final String TAG = "UserInterests";
    private Database db;
    private ViewGroup mainLayout;
    private RecyclerView listView;
    private UserInterestAdapter userInterestListAdapter = null;
    private Snackbar undoSnackBar;
    private RecyclerView.OnItemTouchListener undoOnTouchListener;
    private FloatingActionButton searchFAB;
    private UserInterestListener userInterestListener;
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;
    private ProgressBar loading;

    public interface UserInterestListener {
        void userInterestsToSearchInterests();
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
            db = (Database) getArguments().getSerializable(MainActivity.BUNDLE_DATABASE);
        } catch (Exception e){
            e.printStackTrace();
            //hard code a new database instance
            db = new FirebaseDB();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_user_interests, container, false);
        mainLayout = view.findViewById(R.id.user_interests_layout);
        listView = view.findViewById(R.id.user_interests_list);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        registerForContextMenu(listView);
        searchFAB = view.findViewById(R.id.user_interests_search_fab);
        actionModeCallback = getActionModeCallback();
        populateFABs();
        loading = view.findViewById(R.id.user_interests_loading);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        userInterestListener.setToolbarState(
                new ToolbarState(getString(R.string.fragment_user_interests_title),
                        false)
        );
        setAdapter();
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

    private void setAdapter(){
        DBUserInterestListener userInterestListener = new DBUserInterestListener() {
            @Override
            public void onUserInterestsQueried(List<WikiDataEntity> userInterests) {
                if (userInterestListAdapter == null) {
                    userInterestListAdapter = new UserInterestAdapter(
                            getUserInterestAdapterListener()
                    );
                    listView.setAdapter(userInterestListAdapter);
                    loading.setVisibility(View.GONE);
                }

                //if the user has something selected while updating interests
                //(which shouldn't happen unless working from two devices)
                //we should de-select everything first
                if (actionMode != null)
                    actionMode.finish();
                userInterestListAdapter.setInterests(userInterests);
            }

            @Override
            public void onUserInterestsAdded(){}

            @Override
            public void onUserInterestsRemoved(){}
        };

        DBConnectionResultListener connectionResultListener = new DBConnectionResultListener() {
            @Override
            public void onNoConnection() {
                if (userInterestListAdapter == null) {
                    userInterestListAdapter = new UserInterestAdapter(
                            getUserInterestAdapterListener()
                    );
                    listView.setAdapter(userInterestListAdapter);
                    userInterestListAdapter.setOffline();
                    loading.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "user interest adapter is not null");
                }
                //don't do anything if we have a list already populated
            }

            @Override
            public void onSlowConnection() {

            }
        };

        NetworkConnectionChecker networkConnectionChecker = new
                AndroidNetworkConnectionChecker(getContext());
        db.getUserInterests(true, userInterestListener,
                connectionResultListener, networkConnectionChecker);
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
                if (actionMode == null) {
                    //normal click
                    showNoFeature();
                } else {
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

    private void showNoFeature(){
        Toast.makeText(getContext(), R.string.user_interests_no_feature, Toast.LENGTH_SHORT)
                .show();
    }
    
    private void showUndoSnackBar(final List<WikiDataEntity> dataToRecover){
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
                        DBUserInterestListener userInterestListener = new DBUserInterestListener() {
                            @Override
                            public void onUserInterestsAdded() {
                                if (undoOnTouchListener != null) {
                                    listView.removeOnItemTouchListener(undoOnTouchListener);
                                    undoOnTouchListener = null;
                                }
                            }

                            @Override
                            public void onUserInterestsQueried(List<WikiDataEntity> data){}

                            @Override
                            public void onUserInterestsRemoved(){}
                        };

                        DBConnectionResultListener connectionResultListener = new DBConnectionResultListener() {
                            @Override
                            public void onNoConnection() {

                            }

                            @Override
                            public void onSlowConnection() {

                            }
                        };
                        NetworkConnectionChecker networkConnectionChecker = new
                                AndroidNetworkConnectionChecker(getContext());
                        db.addUserInterests(dataToRecover, userInterestListener,
                                connectionResultListener, networkConnectionChecker);
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
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.user_interest_item_menu_delete:
                        final List<WikiDataEntity> toRemove =
                                userInterestListAdapter.getSelectedItems();
                        DBUserInterestListener userInterestListener = new DBUserInterestListener() {
                            @Override
                            public void onUserInterestsRemoved() {
                                showUndoSnackBar(toRemove);
                                mode.finish();
                            }
                            @Override
                            public void onUserInterestsAdded(){}

                            @Override
                            public void onUserInterestsQueried(List<WikiDataEntity> data){}
                        };
                        db.removeUserInterests(toRemove, userInterestListener);
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
                            ThemeColorChanger.getColorFromAttribute(R.attr.color700, getContext())
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
    public void onStop(){
        super.onStop();
        db.cleanup();
        userInterestListAdapter = null;
    }
}
