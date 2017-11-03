package com.linnca.pelicann.vocabulary;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VocabularyList extends Fragment {
    private final String TAG = "VocabularyList";
    private String userID;
    private Database db;
    private FirebaseAnalytics firebaseLog;
    private RecyclerView listView;
    private VocabularyListListener listener;
    private VocabularyListAdapter adapter;
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    public interface VocabularyListListener {
        void vocabularyListToVocabularyDetails(String key);
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseLog = FirebaseAnalytics.getInstance(getActivity());
        firebaseLog.setCurrentScreen(getActivity(), TAG, TAG);
        firebaseLog.setUserId(userID);
        db = new FirebaseDB();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                      Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_vocabulary_list, container, false);
        listView = view.findViewById(R.id.vocabulary_list_list);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(new ToolbarState(getString(R.string.fragment_vocabulary_list_title),
                false, false, null));

        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            populateList();
            actionModeCallback = getActionModeCallback();
        }
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
            listener = (VocabularyListListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private void populateList(){
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onVocabularyListQueried(List<VocabularyListWord> vocabularyList) {
                //alphabetical order
                Collections.sort(vocabularyList);
                adapter = new VocabularyListAdapter(getVocabularyListAdapterListener(), vocabularyList);
                listView.setAdapter(adapter);
            }
        };
        db.getVocabularyList(onResultListener);
    }

    private VocabularyListAdapter.VocabularyListAdapterListener getVocabularyListAdapterListener(){
        return new VocabularyListAdapter.VocabularyListAdapterListener() {
            @Override
            public void onItemClicked(int position) {
                if (actionMode != null) {
                    toggleSelection(position);
                } else {
                    VocabularyListWord word = adapter.getItemAt(position);
                    listener.vocabularyListToVocabularyDetails(word.getKey());
                }
            }

            @Override
            public boolean onItemLongClicked(int position) {
                if (actionMode == null) {
                    actionMode = getActivity().startActionMode(actionModeCallback);
                }

                toggleSelection(position);

                return true;
            }
        };
    }

    private ActionMode.Callback getActionModeCallback(){
        return new ActionMode.Callback() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate (R.menu.vocabulary_list_item_menu, menu);
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
                    case R.id.vocabulary_list_item_menu_delete:
                        removeSelectedVocabularyWords();
                        mode.finish();
                        return true;

                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.clearSelection();
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
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();

        }
    }

    private void removeSelectedVocabularyWords(){
        List<VocabularyListWord> wordsToRemove = adapter.getSelectedItems();
        List<String> keys = new ArrayList<>(wordsToRemove.size());
        for (VocabularyListWord word : wordsToRemove){
            String key = word.getKey();
            keys.add(key);
        }
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onVocabularyListItemsRemoved() {
                //show the undo bar
            }
        };
        db.removeVocabularyListItems(keys, onResultListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        db.cleanup();
    }
}
