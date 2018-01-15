package com.linnca.pelicann.results;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.AndroidNetworkConnectionChecker;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.lessonlist.LessonListViewerImplementation;
import com.linnca.pelicann.mainactivity.GUIUtils;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;
import com.linnca.pelicann.mainactivity.ToolbarState;

import java.util.List;

import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;
import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonData;
import pelicann.linnca.com.corefunctionality.lessonlist.UserLessonListViewer;
import pelicann.linnca.com.corefunctionality.questions.InstanceRecord;
import pelicann.linnca.com.corefunctionality.questions.QuestionAttempt;
import pelicann.linnca.com.corefunctionality.results.ResultsManager;
import pelicann.linnca.com.corefunctionality.results.ResultsVocabularyWord;

//after we are finished with the questions,
//we redirect to this fragment
// and save everything in the database

public class Results extends Fragment {
    public static final String TAG = "Results";
    private Database db;
    public static final String BUNDLE_INSTANCE_RECORD = "bundleInstanceRecord";
    public static final String BUNDLE_QUESTION_IDS = "bundleQuestionIDs";
    private InstanceRecord instanceRecord;
    private ResultsManager resultsManager;
    private TextView passedTextView;
    private TextView correctCtTextView;
    private TextView correctCtUnitTextView;
    private Button finishButton;
    private Button reviewButton;
    private TextView firstClearTextView;
    private TextView unlockedLessonTitle;
    private LinearLayout unlockedLessonList;
    private ProgressBar dailyLessonCtProgressBar;
    private TextView dailyLessonCtLabel;
    private Button dailyLessonCtSetButton;
    private ImageView dailyLessonCtCompleted;
    private LinearLayout vocabularyList;
    private ProgressBar vocabularyLoading;
    private TextView noVocabularyTextView;

    private boolean instanceUpdated = false;
    private String SAVED_STATE_INSTANCE_UPDATED = "savedStateInstanceUpdated";
    private int dailyLessonCt = -1;
    private String SAVED_STATE_DAILY_LESSON_CT = "savedStateDailyLessonCt";

    private ResultsListener resultsListener;

    public interface ResultsListener {
        void resultsToLessonList();
        void resultsToLessonDetails(LessonData lessonData);
        //if the user unlocked a review lesson,
        //we should direct him directly to the review
        void resultsToReviewLesson(LessonData lessonData);
        //this is for reviewing the lesson he just did
        void resultsToReview();
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            db = (Database) getArguments().getSerializable(MainActivity.BUNDLE_DATABASE);
        } catch (Exception e) {
            e.printStackTrace();
            //hard code a new database instance
            db = new FirebaseDB();
        }
        instanceRecord = (InstanceRecord) getArguments().getSerializable(BUNDLE_INSTANCE_RECORD);
        List<String> questionKeys = getArguments().getStringArrayList(BUNDLE_QUESTION_IDS);
        resultsManager = new ResultsManager(instanceRecord, questionKeys, db,
                new ResultsManager.ResultsManagerListener() {
                    @Override
                    public void onLessonFirstCleared(UserLessonListViewer previousList) {
                        populateFirstCleared(previousList);
                    }

                    @Override
                    public void onLessonCleared(boolean cleared){
                        notifyCleared(cleared);
                    }

                    @Override
                    public void onAddDailyLessonCt(int oldCt, int newCt){
                        dailyLessonCt = newCt;
                        setDailyLessonCtLayout(oldCt, newCt);
                    }
                });
        if (savedInstanceState != null) {
            instanceUpdated = savedInstanceState.getBoolean(SAVED_STATE_INSTANCE_UPDATED, false);
            dailyLessonCt = savedInstanceState.getInt(SAVED_STATE_DAILY_LESSON_CT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_results, container, false);
        passedTextView = view.findViewById(R.id.results_passed);
        correctCtTextView = view.findViewById(R.id.results_questions_correct_accuracy);
        correctCtUnitTextView = view.findViewById(R.id.results_questions_correct_accuracy_unit);
        firstClearTextView = view.findViewById(R.id.results_first_clear);
        reviewButton = view.findViewById(R.id.results_review);
        finishButton = view.findViewById(R.id.results_finish);
        unlockedLessonTitle = view.findViewById(R.id.results_unlocked_lesson_title);
        unlockedLessonList = view.findViewById(R.id.results_unlocked_lesson_list);
        dailyLessonCtProgressBar = view.findViewById(R.id.results_daily_lesson_ct_progress_bar);
        dailyLessonCtLabel = view.findViewById(R.id.results_daily_lesson_ct_progress_label);
        dailyLessonCtSetButton = view.findViewById(R.id.results_daily_lesson_ct_set);
        dailyLessonCtCompleted = view.findViewById(R.id.results_daily_lesson_ct_completed);
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

        if (!instanceUpdated) {
            NetworkConnectionChecker networkConnectionChecker = new
                    AndroidNetworkConnectionChecker(getContext());
            resultsManager.saveInstanceRecord(networkConnectionChecker, new LessonListViewerImplementation());
            instanceUpdated = true;
        }
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

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putBoolean(SAVED_STATE_INSTANCE_UPDATED, instanceUpdated);
        outState.putInt(SAVED_STATE_DAILY_LESSON_CT, dailyLessonCt);
        super.onSaveInstanceState(outState);
    }

    private void setLayout(){
        populateCorrectCount();
        //this will update the UI if this is the user's first time clearing
        // the lesson.
        NetworkConnectionChecker networkConnectionChecker = new
                AndroidNetworkConnectionChecker(getContext());
        resultsManager.clearLesson(networkConnectionChecker, new LessonListViewerImplementation());
        if (dailyLessonCt == -1) {
            resultsManager.addDailyLessonCt();
        } else {
            setDailyLessonCtLayout(dailyLessonCt, dailyLessonCt);
        }
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onLessonVocabularyQueried(List<ResultsVocabularyWord> words) {
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
                //in case there was no connection before
                vocabularyList.removeAllViews();
                for (ResultsVocabularyWord word : words) {
                    View view = createVocabularyItem(word);
                    vocabularyList.addView(view);
                }
            }

            @Override
            public void onNoConnection(){
                vocabularyLoading.post(new Runnable() {
                    @Override
                    public void run() {
                        vocabularyLoading.setVisibility(View.GONE);
                    }
                });
                TextView noVocabularyTextView = new TextView(getContext());
                noVocabularyTextView.setText(R.string.results_vocabulary_no_connection);
                noVocabularyTextView.setTextSize(GUIUtils.getDp(17, getContext()));
                vocabularyList.addView(noVocabularyTextView);
            }
        };
        NetworkConnectionChecker networkConnectionChecker2 = new
                AndroidNetworkConnectionChecker(getContext());
        db.getLessonVocabulary(networkConnectionChecker2, instanceRecord.getInstanceId(), onDBResultListener);

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
                    resultsListener.resultsToReview();
                }
            });
            //change the layout of the finish button to recommend review
            // (make it borderless)
            finishButton.setBackgroundResource(R.drawable.transparent_button);
            finishButton.setTextColor(ThemeColorChanger.getColorFromAttribute(
                    R.attr.color500, getContext()));
            finishButton.setText(R.string.results_finish_review);
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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

    private void notifyCleared(final boolean cleared){
        passedTextView.post(new Runnable() {
            @Override
            public void run() {
                if (cleared) {
                    passedTextView.setText(R.string.results_passed);
                } else {
                    passedTextView.setText(R.string.results_not_passed);
                }
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
                //if the unlocked item is a review, we want to redirect him to the review,
                // not the lesson details screen
                if (LessonData.isReview(lessonData.getKey())){
                    goToLessonButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            resultsListener.resultsToReviewLesson(lessonData);
                        }
                    });
                } else {
                    goToLessonButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            resultsListener.resultsToLessonDetails(lessonData);
                        }
                    });
                }
                unlockedLessonList.addView(view);
            }
        });
    }

    private void populateCorrectCount(){
        int[] result = ResultsManager.calculateCorrectCount(instanceRecord.getAttempts());
        int correctCt = result[0];
        int totalCt = result[1];

        int score = 100 * correctCt / totalCt;
        String displayText = Integer.toString(score);
        correctCtTextView.setText(displayText);
        //change text color based on accuracy (the user can edit border line??)
        if (score > 80){
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.light_green500));
            correctCtUnitTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.light_green500));
        } else if (score > 50){
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.orange500));
            correctCtUnitTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.orange500));
        } else {
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red500));
            correctCtUnitTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red500));
        }
    }

    private View createVocabularyItem(final ResultsVocabularyWord vocabularyWrapper){
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
                    OnDBResultListener onDBResultListener = new OnDBResultListener() {
                        @Override
                        public void onVocabularyWordAdded() {
                            super.onVocabularyWordAdded();
                        }
                    };
                    //add vocabulary
                    db.addVocabularyWord(vocabularyWrapper.getVocabularyWord(), onDBResultListener);
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

    //if we need to grab the lessons per day from the preferences
    private void setDailyLessonCtLayout(int oldCt, int newCt){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        //the preference is still stored as a string
        String notSet = "not set";
        String preferencesLessonsPerDayString = sharedPreferences.getString
                (getString(R.string.preferences_general_lessons_per_day_key), notSet);
        //if the user hasn't set lessons per day in the settings
        if (preferencesLessonsPerDayString.equals(notSet)){
            showDailyLessonCtSettingsButton(oldCt, newCt);
            return;
        }
        int lessonsPerDay = Integer.parseInt(preferencesLessonsPerDayString);
        setDailyLessonCtLayout(oldCt, newCt, lessonsPerDay);
    }

    //if we don't need to grab the lessons per day from the preferences
    // (the user just set it)
    private void setDailyLessonCtLayout(int oldCt, int newCt, int lessonsPerDay){
        String labelText = Integer.toString(newCt) + "/" +
                Integer.toString(lessonsPerDay);
        dailyLessonCtLabel.setText(labelText);

        dailyLessonCtProgressBar.setMax(lessonsPerDay * 1000);
        //can't figure out how to set the background path of the progress bar,
        //so set the whole background path as a secondary progress bar (100%)
        // and make it look like that's the background
        dailyLessonCtProgressBar.setSecondaryProgress(lessonsPerDay * 1000);

        if (oldCt == newCt){
            //if the user came back from another screen
            if (newCt >= lessonsPerDay){
                showDailyLessonCtCompleted(false);
            } else {
                dailyLessonCtProgressBar.setProgress(newCt * 1000);
            }
        } else if (newCt > lessonsPerDay){
            //if the user has already completed the daily lesson count
            // prior to this lesson
            showDailyLessonCtCompleted(false);
        } else {
            dailyLessonCtProgressBar.setProgress(oldCt * 1000);
            ProgressBarAnimation progressBarAnimation = new ProgressBarAnimation(
                    dailyLessonCtProgressBar, oldCt*1000, newCt*1000);
            progressBarAnimation.setDuration(1000);
            //if the user will complete the daily lesson ct with this lesson
            if (newCt == lessonsPerDay){
                //we should show the completed image
                // after showing progress
                progressBarAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showDailyLessonCtCompleted(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            dailyLessonCtProgressBar.startAnimation(progressBarAnimation);
        }
    }

    private void showDailyLessonCtCompleted(boolean animateFadeOut){
        if (animateFadeOut) {
            dailyLessonCtProgressBar.animate().alpha(0f)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            dailyLessonCtProgressBar.setVisibility(View.GONE);
                        }
                    });
        } else {
            dailyLessonCtProgressBar.setVisibility(View.GONE);
        }
        dailyLessonCtCompleted.setVisibility(View.VISIBLE);
        dailyLessonCtCompleted.animate().alpha(1f)
            .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
            .setListener(null);
    }

    private void showDailyLessonCtSettingsButton(final int oldCt, final int newCt){
        dailyLessonCtProgressBar.setVisibility(View.GONE);
        dailyLessonCtSetButton.setVisibility(View.VISIBLE);
        dailyLessonCtSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDailyLessonCtSettingsDialog(oldCt, newCt);
            }
        });
    }


    private void showDailyLessonCtSettingsDialog(final int oldCt, final int newCt){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.preferences_general_lessons_per_day_dialog_label);
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        int[] attrs = new int[] { R.attr.dialogPreferredPadding };
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        int margin = ta.getDimensionPixelSize(0, GUIUtils.getDp(24, getContext()));
        ta.recycle();
        FrameLayout wrapper = new FrameLayout(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, 0, margin, 0);
        input.setLayoutParams(params);
        wrapper.addView(input);
        builder.setView(wrapper);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String inputText = input.getText().toString();
                if (inputText.equals("")){
                    return;
                }
                int inputInt = Integer.parseInt(inputText);
                if (checkValidIntegerInput(inputInt)){
                    setDailyLessonCtPreference(inputInt);
                    dailyLessonCtSetButton.setVisibility(View.GONE);
                    dailyLessonCtSetButton.setOnClickListener(null);
                    dailyLessonCtProgressBar.setVisibility(View.VISIBLE);
                    setDailyLessonCtLayout(oldCt, newCt, inputInt);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        builder.create().show();
    }

    private boolean checkValidIntegerInput(int i){
        return i > 0;
    }

    private void setDailyLessonCtPreference(int i){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        //the preference fragment can only put in strings,
        // so make sure this is also a string
        editor.putString(getString(R.string.preferences_general_lessons_per_day_key), Integer.toString(i));
        editor.apply();
    }

    @Override
    public void onStop(){
        super.onStop();
        db.cleanup();
    }
}
