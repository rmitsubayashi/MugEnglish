package com.linnca.pelicann.results;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseAnalyticsHeaders;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionAttempt;
import com.linnca.pelicann.vocabulary.VocabularyWord;
import com.linnca.pelicann.vocabulary.VocabularyListWord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

//after we are finished with the questions,
//we redirect to this fragment
// and save everything in the database

public class Results extends Fragment {
    private final String TAG = "Results";
    private FirebaseAnalytics firebaseLog;
    private FirebaseDatabase db;
    private String userID;
    public static final String BUNDLE_INSTANCE_RECORD = "bundleInstanceRecord";
    private InstanceRecord instanceRecord;
    private ResultsManager resultsManager;
    private TextView correctCtTextView;
    private Button finishButton;
    private Button reviewButton;
    private TextView firstClearTextView;
    private LinearLayout vocabularyList;

    private ResultsListener resultsListener;

    public interface ResultsListener {
        void resultsToLessonCategories();
        void resultsToReview(InstanceRecord instanceRecord);
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instanceRecord = (InstanceRecord) getArguments().getSerializable(BUNDLE_INSTANCE_RECORD);
        resultsManager = new ResultsManager(instanceRecord, new ResultsManager.ResultsManagerListener() {
            @Override
            public void onLessonCleared(){
                firstClearTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        firstClearTextView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        resultsManager.saveInstanceRecord();
        db = FirebaseDatabase.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseLog = FirebaseAnalytics.getInstance(getActivity());
        firebaseLog.setCurrentScreen(getActivity(), TAG, TAG);
        firebaseLog.setUserId(userID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_results, container, false);
        correctCtTextView = view.findViewById(R.id.results_questions_correct);
        firstClearTextView = view.findViewById(R.id.results_first_clear);
        reviewButton = view.findViewById(R.id.results_review);
        finishButton = view.findViewById(R.id.results_finish);
        vocabularyList = view.findViewById(R.id.results_vocabulary_list);
        setLayout();
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        resultsListener.setToolbarState(
                new ToolbarState(getString(R.string.results_app_bar_title), false, instanceRecord.getLessonId())
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
            resultsListener = (ResultsListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private void setLayout(){
        populateCorrectCount();
        resultsManager.checkLessonCleared();
        getVocabulary();

        boolean needToReview = false;
        //user needs to review if the user gets a question wrong
        // (doesn't matter if the user gets it right on following attempts)
        for (QuestionAttempt attempt : instanceRecord.getAttempts()){
            if (!attempt.getCorrect()){
                needToReview = true;
                break;
            }
        }
        if (needToReview){
            reviewButton.setVisibility(View.VISIBLE);
            reviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalyticsHeaders.PARAMS_ACTION_TYPE, "Review");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, instanceRecord.getId());
                    firebaseLog.logEvent(FirebaseAnalyticsHeaders.EVENT_ACTION, bundle);
                    resultsListener.resultsToReview(instanceRecord);
                }
            });
            //change the layout of the finish button to recommend review
            // (make it borderless)
            finishButton.setBackgroundResource(R.drawable.transparent_button);
            finishButton.setTextColor(ContextCompat.getColor(getContext(), R.color.lblue500));
            finishButton.setText(R.string.results_finish_review);
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalyticsHeaders.PARAMS_ACTION_TYPE, "Finish Instead of Review");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, instanceRecord.getId());
                    firebaseLog.logEvent(FirebaseAnalyticsHeaders.EVENT_ACTION, bundle);
                    resultsListener.resultsToLessonCategories();
                }
            });
        } else {
            //we don't log anything
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resultsListener.resultsToLessonCategories();
                }
            });
        }
    }

    private void populateCorrectCount(){
        int[] result = ResultsManager.calculateCorrectCount(instanceRecord.getAttempts());
        int correctCt = result[0];
        int totalCt = result[1];

        String displayText = Integer.toString(correctCt) + " / " + Integer.toString(totalCt);
        correctCtTextView.setText(displayText);
        //change text color based on accuracy (the user can edit border line??)
        double correctPercentage = (double)correctCt / (double)totalCt;
        if (correctPercentage > 0.7){
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.lgreen500));
        } else if (correctPercentage > 0.5){
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.orange500));
        } else {
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red500));
        }
    }

    private void getVocabulary(){
        String instanceKey = instanceRecord.getInstanceId();
        DatabaseReference vocabularyRef = db.getReference(
                FirebaseDBHeaders.LESSON_INSTANCE_VOCABULARY + "/" +
                        instanceKey
        );
        vocabularyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> type =
                        new GenericTypeIndicator<List<String>>() {};
                List<String> vocabularyIDs = dataSnapshot.getValue(type);
                if (vocabularyIDs == null)
                    return;
                int vocabularyCt = vocabularyIDs.size();
                AtomicInteger vocabularyFetched = new AtomicInteger(0);
                List<VocabularyWord> words = Collections.synchronizedList(new ArrayList<VocabularyWord>(vocabularyCt));
                for (String id : vocabularyIDs){
                    fetchVocabularyWord(id, vocabularyCt, vocabularyFetched, words);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchVocabularyWord(String id, final int vocabularyToFetch, final AtomicInteger vocabularyFetched, final List<VocabularyWord> allWords){
        DatabaseReference vocabularyRef = db.getReference(
                FirebaseDBHeaders.VOCABULARY + "/" +
                        id
        );
        vocabularyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                VocabularyWord word = dataSnapshot.getValue(VocabularyWord.class);
                allWords.add(word);

                if (vocabularyFetched.incrementAndGet() == vocabularyToFetch){
                    Log.d(TAG, "populating vocabulary");
                    populateVocabularyItems(allWords);
                } else {
                    Log.d(TAG, ""+vocabularyFetched.get());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class VocabularyComparator implements Comparator<VocabularyWord>{
        public int compare(VocabularyWord word1, VocabularyWord word2){
            return word1.getWord().compareTo(word2.getWord());
        }
    }

    private void populateVocabularyItems(List<VocabularyWord> allWords){
        //cluster duplicates (same words but different example sentences)
        List<List<VocabularyWord>> filteredWords = new ArrayList<>(allWords.size());
        //sort alphabetically
        Collections.sort(allWords, new VocabularyComparator());
        for (VocabularyWord word : allWords){
            if (filteredWords.size() == 0){
                //first item
                List<VocabularyWord> cluster = new ArrayList<>(allWords.size());
                cluster.add(word);
                filteredWords.add(cluster);
                continue;
            }
            boolean matched = false;
            for (List<VocabularyWord> toCompareCluster : filteredWords){
                VocabularyWord toCompare = toCompareCluster.get(0);
                if (word.getWord().equals(toCompare.getWord()) &&
                        word.getMeaning().equals(toCompare.getMeaning())){
                    toCompareCluster.add(word);
                    matched = true;
                    break;
                }
            }
            if (!matched){
                //new word
                List<VocabularyWord> cluster = new ArrayList<>(allWords.size());
                cluster.add(word);
                filteredWords.add(cluster);
            }
        }

        checkIfAlreadyAdded(filteredWords);
    }

    private void checkIfAlreadyAdded(List<List<VocabularyWord>> list){
        final Random random = new Random();
        for (final List<VocabularyWord> words : list){
            int index = random.nextInt(words.size());
            final VocabularyWord wordToAdd = words.get(index);
            DatabaseReference wordRef = db.getReference(
                    FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                            userID
            );
            Query wordQuery = wordRef.orderByChild(FirebaseDBHeaders.VOCABULARY_LIST_WORD_WORD)
                    .equalTo(wordToAdd.getWord()).limitToFirst(1);
            wordQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //there is no word so we can add it
                    if (!dataSnapshot.exists()){
                        View view = createVocabularyItem(wordToAdd, true);
                        vocabularyList.addView(view);
                        return;
                    }
                    //only one
                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        View view;
                        VocabularyListWord word = child.getValue(VocabularyListWord.class);
                        //check if an item exists with the same word/meaning pair.
                        //if there is a word with different meanings,
                        //we should be able to add both meanings
                        if (word != null &&
                                word.getWord().equals(wordToAdd.getWord()) &&
                                word.getMeanings().contains(wordToAdd.getMeaning())){
                            view = createVocabularyItem(wordToAdd, false);
                        } else {
                            view = createVocabularyItem(wordToAdd, true);
                        }
                        vocabularyList.addView(view);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private View createVocabularyItem(final VocabularyWord word, boolean canAdd){
        View view = getLayoutInflater().inflate(R.layout.inflatable_result_vocabulary_item, vocabularyList, false);
        TextView wordView = view.findViewById(R.id.results_vocabulary_item_word);
        wordView.setText(word.getWord());
        TextView meaningView = view.findViewById(R.id.results_vocabulary_item_meaning);
        meaningView.setText(word.getMeaning());
        Button addButton = view.findViewById(R.id.results_vocabulary_item_add);
        if (canAdd) {
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //add vocabulary
                    addVocabularyWord(word);
                    //the user shouldn't be able to click the button anymore
                    disableAddButton((Button) view);
                }
            });
        } else {
            disableAddButton(addButton);
        }
        return view;
    }

    private void disableAddButton(Button button){
        button.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.gray500));
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        button.setOnClickListener(null);
        button.setText(R.string.results_vocabulary_item_added);
    }

    private void addVocabularyWord(final VocabularyWord word){
        //for displaying a list of words
        DatabaseReference listRef = db.getReference(
                FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                        userID
        );
        Query listQuery = listRef.orderByChild(FirebaseDBHeaders.VOCABULARY_LIST_WORD_WORD)
                .equalTo(word.getWord()).limitToFirst(1);
        listQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key;
                if (!dataSnapshot.exists()){
                    //this is a new word
                    DatabaseReference newItemRef = db.getReference(
                            FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                                    userID
                    );
                    key = newItemRef.push().getKey();
                    VocabularyListWord toSave = new VocabularyListWord(word, key);
                    newItemRef.child(key).setValue(toSave);
                    addVocabularyWordPt2(key, word);
                    return;
                }
                //only loops once
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    VocabularyListWord listWord = childSnapshot.getValue(VocabularyListWord.class);
                    if (childSnapshot.exists() && listWord != null) {
                        listWord.addMeaning(word.getMeaning());
                        key = childSnapshot.getKey();
                        db.getReference(
                                FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                                        userID + "/" +
                                        key
                        ).setValue(listWord);
                    } else {
                        //this is a new meaning
                        DatabaseReference newItemRef = db.getReference(
                                FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                                        userID
                        );
                        key = newItemRef.push().getKey();
                        VocabularyListWord toSave = new VocabularyListWord(word, key);
                        newItemRef.child(key).setValue(toSave);
                    }

                    addVocabularyWordPt2(key, word);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //we are going to add the whole word to the details list,
    //using the key used/created when adding the item to the list before this
    private void addVocabularyWordPt2(String key, VocabularyWord word){
        DatabaseReference detailsRef = db.getReference(
                FirebaseDBHeaders.VOCABULARY_DETAILS + "/" +
                        userID + "/" +
                        key
        );
        detailsRef.push().setValue(word);
    }
}
