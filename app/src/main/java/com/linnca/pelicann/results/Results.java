package com.linnca.pelicann.results;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseAnalyticsHeaders;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.lessonlist.UserLessonListViewer;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionAttempt;

import java.util.List;

//after we are finished with the questions,
//we redirect to this fragment
// and save everything in the database

public class Results extends Fragment {
    public static final String TAG = "Results";
    private FirebaseAnalytics firebaseLog;
    private Database db;
    public static final String BUNDLE_INSTANCE_RECORD = "bundleInstanceRecord";
    public static final String BUNDLE_QUESTION_IDS = "bundleQuestionIDs";
    private InstanceRecord instanceRecord;
    private ResultsManager resultsManager;
    private TextView correctCtTextView;
    private Button finishButton;
    private Button reviewButton;
    private TextView firstClearTextView;
    private TextView unlockedLessonTitle;
    private LinearLayout unlockedLessonList;
    private LinearLayout vocabularyList;
    private ProgressBar vocabularyLoading;
    private TextView noVocabularyTextView;

    private ResultsListener resultsListener;

    public interface ResultsListener {
        void resultsToLessonList();
        void resultsToLessonDetails(LessonData lessonData);
        void resultsToReview();
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            db = (Database) getArguments().getSerializable(MainActivity.BUNDLE_DATABASE);
        } catch (Exception e){
            e.printStackTrace();
            //hard code a new database instance
            db = new FirebaseDB();
        }
        instanceRecord = (InstanceRecord) getArguments().getSerializable(BUNDLE_INSTANCE_RECORD);
        List<String> questionKeys = getArguments().getStringArrayList(BUNDLE_QUESTION_IDS);
        resultsManager = new ResultsManager(instanceRecord, questionKeys, db,
                new ResultsManager.ResultsManagerListener() {
            @Override
            public void onLessonFirstCleared(UserLessonListViewer previousList){
                populateFirstCleared(previousList);
            }
        });

        resultsManager.saveInstanceRecord();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
        unlockedLessonTitle = view.findViewById(R.id.results_unlocked_lesson_title);
        unlockedLessonList = view.findViewById(R.id.results_unlocked_lesson_list);
        vocabularyList = view.findViewById(R.id.results_vocabulary_list);
        noVocabularyTextView = view.findViewById(R.id.results_no_vocabulary);
        vocabularyLoading = view.findViewById(R.id.results_vocabulary_loading);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        resultsListener.setToolbarState(
                new ToolbarState(getString(R.string.results_app_bar_title), false, false, instanceRecord.getLessonId())
        );

        setLayout();
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
        //this will update the UI if this is the user's first time clearing
        // the lesson
        resultsManager.clearLesson();
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onLessonVocabularyQueried(List<NewVocabularyWrapper> words) {
                vocabularyLoading.post(new Runnable() {
                    @Override
                    public void run() {
                        vocabularyLoading.setVisibility(View.GONE);
                    }
                });
                if (words == null) {
                    noVocabularyTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            noVocabularyTextView.setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }
                for (NewVocabularyWrapper word : words) {
                    View view = createVocabularyItem(word);
                    vocabularyList.addView(view);
                }
            }
        };
        db.getLessonVocabulary(instanceRecord.getInstanceId(), onResultListener);

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
                    resultsListener.resultsToReview();
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
                    resultsListener.resultsToLessonList();
                }
            });
        } else {
            //we don't log anything
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resultsListener.resultsToLessonList();
                }
            });
        }
    }

    private void populateFirstCleared(UserLessonListViewer previousList){
        //make sure the view is loaded
        firstClearTextView.post(new Runnable() {
            @Override
            public void run() {
                firstClearTextView.setVisibility(View.VISIBLE);
            }
        });
        //add unlocked items to the list
        String clearedLessonKey = instanceRecord.getLessonId();
        List<LessonData> unlockedLessons = previousList.getLessonsUnlockedByClearing(clearedLessonKey);
        for (LessonData lessonData : unlockedLessons){
            addUnlockedLessonItem(lessonData);
        }
        final int unlockedLessonTitleID;
        if (unlockedLessons.size() != 0){
            unlockedLessonTitleID = R.string.results_unlocked_lesson_title;
        } else {
            unlockedLessonTitleID = R.string.results_no_unlocked_lessons_title;
        }
        unlockedLessonTitle.post(new Runnable() {
            @Override
            public void run() {
                unlockedLessonTitle.setText(unlockedLessonTitleID);
                unlockedLessonTitle.setVisibility(View.VISIBLE);
            }
        });
    }

    private void addUnlockedLessonItem(final LessonData lessonData){
        //make sure the view is loaded
        unlockedLessonList.post(new Runnable() {
            @Override
            public void run() {
                View view = getLayoutInflater().inflate(R.layout.inflatable_results_unlocked_lesson_item,
                        unlockedLessonList, false);
                TextView titleTextView = view.findViewById(R.id.results_unlocked_lesson_item_lesson_title);
                titleTextView.setText(lessonData.getTitle());
                Button goToLessonButton = view.findViewById(R.id.results_unlocked_lesson_item_go_to_lesson);
                goToLessonButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resultsListener.resultsToLessonDetails(lessonData);
                    }
                });
                unlockedLessonList.addView(view);
            }
        });
    }

    private void populateCorrectCount(){
        int[] result = ResultsManager.calculateCorrectCount(instanceRecord.getAttempts());
        int correctCt = result[0];
        int totalCt = result[1];

        String displayText = Integer.toString(correctCt) + " / " + Integer.toString(totalCt);
        correctCtTextView.setText(displayText);
        //change text color based on accuracy (the user can edit border line??)
        double correctPercentage = (double)correctCt / (double)totalCt;
        if (correctPercentage > 0.8){
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.lgreen500));
        } else if (correctPercentage > 0.5){
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.orange500));
        } else {
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red500));
        }
    }

    private View createVocabularyItem(final NewVocabularyWrapper vocabularyWrapper){
        View view = getLayoutInflater().inflate(R.layout.inflatable_result_vocabulary_item, vocabularyList, false);
        TextView wordView = view.findViewById(R.id.results_vocabulary_item_word);
        wordView.setText(vocabularyWrapper.getVocabularyWord().getWord());
        TextView meaningView = view.findViewById(R.id.results_vocabulary_item_meaning);
        meaningView.setText(vocabularyWrapper.getVocabularyWord().getMeaning());
        Button addButton = view.findViewById(R.id.results_vocabulary_item_add);
        if (vocabularyWrapper.isNew()) {
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OnResultListener onResultListener = new OnResultListener() {
                        @Override
                        public void onVocabularyWordAdded() {
                            super.onVocabularyWordAdded();
                        }
                    };
                    //add vocabulary
                    db.addVocabularyWord(vocabularyWrapper.getVocabularyWord(), onResultListener);
                    //the user shouldn't be able to click the button anymore.
                    //this should be done immediately after, not
                    //after the vocabulary word has successfully been added
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

    @Override
    public void onStop(){
        super.onStop();
        db.cleanup();
    }
}
