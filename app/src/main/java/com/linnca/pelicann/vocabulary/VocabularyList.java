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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class VocabularyList extends Fragment {
    private final String TAG = "VocabularyList";
    private String userID;
    private FirebaseDatabase db;
    private FirebaseAnalytics firebaseLog;
    private ViewGroup mainLayout;
    private RecyclerView listView;
    private VocabularyListListener listener;
    private VocabularyListAdapter adapter;
    private DatabaseReference vocabularyRef;
    private ValueEventListener vocabularyEventListener;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                      Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_vocabulary_list, container, false);
        mainLayout = view.findViewById(R.id.vocabulary_list_layout);
        listView = view.findViewById(R.id.vocabulary_list_list);
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            populateList();
            actionModeCallback = getActionModeCallback();
        }
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(new ToolbarState(getString(R.string.fragment_vocabulary_list_title),
                false, null));
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
        vocabularyRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                        userID
        );
        vocabularyEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<VocabularyListWord> words = new ArrayList<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    VocabularyListWord word = childSnapshot.getValue(VocabularyListWord.class);
                    words.add(word);
                }
                //alphabetical order
                Collections.sort(words);
                adapter = new VocabularyListAdapter(getVocabularyListAdapterListener(), words);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        vocabularyRef.addValueEventListener(vocabularyEventListener);
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
        int toRemoveWordCount = wordsToRemove.size();
        for (VocabularyListWord word : wordsToRemove){
            String key = word.getKey();
            removeVocabularyWord(key);
            removeVocabularyListWord(key);
        }
    }

    private void removeVocabularyListWord(String key){
        DatabaseReference wordRef = db
    }

    public void onDestroy(){
        super.onDestroy();
        if (vocabularyRef != null && vocabularyEventListener != null){
            vocabularyRef.removeEventListener(vocabularyEventListener);
        }
    }
}
