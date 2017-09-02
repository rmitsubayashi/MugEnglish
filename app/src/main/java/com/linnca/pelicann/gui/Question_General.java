package com.linnca.pelicann.gui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.datawrappers.QuestionData;
import com.linnca.pelicann.gui.widgets.ToolbarState;

import java.util.ArrayList;
import java.util.List;

//sets methods common for all question guis
public abstract class Question_General extends Fragment {
    private final String TAG = "Question_General";
    public static int UNLIMITED_ATTEMPTS = -1;
    public static String BUNDLE_QUESTION_DATA = "bundleQuestionData";
    public static String BUNDLE_QUESTION_NUMBER = "bundleQuestionNumber";
    public static String BUNDLE_QUESTION_TOTAL_QUESTIONS = "bundleTotalQuestions";

    protected QuestionData questionData;
    private int questionNumber;
    private int totalQuestions;

    protected int maxNumberOfAttempts;
    protected int attemptCt = 0;
    protected boolean disableChoiceAfterWrongAnswer;
    private BottomSheetBehavior behavior;
    private NestedScrollView feedback;
    private Button nextButton;
    protected ViewGroup parentViewGroupForFeedback;
    protected ViewGroup siblingViewGroupForFeedback;

    private QuestionListener questionListener;

    interface QuestionListener {
        void onNextQuestion();
        void onRecordResponse(String response, boolean correct);
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        questionData = (QuestionData)args.getSerializable(BUNDLE_QUESTION_DATA);
        questionNumber = args.getInt(BUNDLE_QUESTION_NUMBER);
        totalQuestions = args.getInt(BUNDLE_QUESTION_TOTAL_QUESTIONS);
        setMaxNumberOfAttempts();
        disableChoiceAfterWrongAnswer = disableChoiceAfterWrongAnswer();
    }

    @Override
    public void onStart(){
        super.onStart();
        questionListener.setToolbarState(
                new ToolbarState(getContext().getString(R.string.question_title, questionNumber, totalQuestions),
                        false, questionData.getLessonId())
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
            questionListener = (QuestionListener)context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    //need this to record response
    protected abstract String getResponse(View clickedView);
    //how many chances that are possibly allowed for each question type
    // (t/f is only one, m/c can have more)
    protected abstract int getMaxPossibleAttempts();
    //whether to disable choice after answer when you have multiple attempts.
    //this will be user friendly?
    protected abstract boolean disableChoiceAfterWrongAnswer();
    //for example clearing a response, hiding the keyboard, etc.
    //should be overridden if using (not required)
    protected void doSomethingAfterResponse(){

    }
    //formatting may be different for certain question types, but this should be the base
    protected String getFeedback(){
        String answer = questionData.getAnswer();
        return "正解: " + answer;
    }

    private void setMaxNumberOfAttempts(){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        //the preference is still stored as a string
        String preferencesMaxAttemptsString = sharedPreferences.getString
                (getString(R.string.preferences_questions_numberOfAttemptsPerQuestion_key), "1");
        int preferencesMaxAttempts = Integer.parseInt(preferencesMaxAttemptsString);
        int maxPossibleAttempts = getMaxPossibleAttempts();
        if (maxPossibleAttempts == UNLIMITED_ATTEMPTS){
            //the question allow unlimited attempts
            //so restrict the user's attempts to the number set in the preferences
            maxNumberOfAttempts = preferencesMaxAttempts;
        } else {
            if (preferencesMaxAttempts <= maxPossibleAttempts){
                //the user has set a number of attempts less than the maximum possible attempts
                //so only allow the user to attempt the number of times he set in the preferences
                maxNumberOfAttempts = preferencesMaxAttempts;
            } else {
                //the max possible attempts is less than the number the user set in the preferences
                //so only allow the max possible attempts
                maxNumberOfAttempts = maxPossibleAttempts;
            }
        }
    }

    //might be different for different question types??
    protected boolean checkAnswer(String response){
        List<String> allAnswers = new ArrayList<>();
        allAnswers.add(questionData.getAnswer());
        if (questionData.getAcceptableAnswers() != null){
            allAnswers.addAll(questionData.getAcceptableAnswers());
        }
        return allAnswers.contains(response);
    }

    protected View.OnClickListener getResponseListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCt++;
                String answer = getResponse(view);
                if (checkAnswer(answer)){
                    questionListener.onRecordResponse(answer, true);
                    openFeedback(true);
                } else {
                    questionListener.onRecordResponse(answer, false);
                    if (attemptCt == maxNumberOfAttempts){
                        //the user used up all his attempts
                        openFeedback(false);
                    } else {
                        //the user still has attempts remaining
                        final View finalView = view;
                        Animation wrongAnswerAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                        //we want to disable the button (if we have to)
                        // after the wrong animation ends
                        wrongAnswerAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                if (disableChoiceAfterWrongAnswer) {
                                    finalView.setEnabled(false);
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        view.startAnimation(wrongAnswerAnimation);
                    }
                }

                doSomethingAfterResponse();
            }
        };
    }

    protected void inflateFeedback(LayoutInflater inflater){
        feedback = (NestedScrollView) inflater
                .inflate(R.layout.inflatable_question_feedback, parentViewGroupForFeedback, false);
        behavior = BottomSheetBehavior.from(feedback);
        nextButton = feedback.findViewById(R.id.question_feedback_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                questionListener.onNextQuestion();
            }
        });
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        parentViewGroupForFeedback.addView(feedback);
    }

    private void openFeedback(boolean correct){
        //when we first display the question the bottom sheet is hidden & can be hidden.
        //whether the answer was correct or not,
        //we don't want the user to be able to hide the view because the net button is there
        //so make it non-hide-able
        behavior.setHideable(false);

        if (correct){
            feedback.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lgreen500));
            nextButton.setTextColor(ContextCompat.getColor(getContext(), R.color.lgreen500));
        } //else condition is default now
        TextView feedbackTitle = feedback.findViewById(R.id.question_feedback_title);
        if (correct){
            feedbackTitle.setText(R.string.question_feedback_correct);
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            feedbackTitle.setText(R.string.question_feedback_incorrect);
            TextView feedbackDescription =
                    feedback.findViewById(R.id.question_feedback_description);
            feedbackDescription.setText(getFeedback());
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        //we don't want the user to be able to interact with the background,
        //but we want them ot be able to see it
        disableBackground(siblingViewGroupForFeedback);
        siblingViewGroupForFeedback.setAlpha(0.5f);
    }

    private void disableBackground(View view) {
        view.setClickable(false);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                disableBackground(child);
            }
        }
    }




}
