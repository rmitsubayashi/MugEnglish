package com.linnca.pelicann.questions;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.lessongenerator.FeedbackPair;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.widgets.GUIUtils;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

//sets methods common for all question GUIs
public abstract class Question_General extends Fragment {
    protected final String TAG = "Question Fragment";
    static final int UNLIMITED_ATTEMPTS = -1;
    public static final String BUNDLE_QUESTION_DATA = "bundleQuestionData";
    public static final String BUNDLE_QUESTION_NUMBER = "bundleQuestionNumber";
    public static final String BUNDLE_QUESTION_TOTAL_QUESTIONS = "bundleTotalQuestions";

    QuestionData questionData;
    private int questionNumber;
    private int totalQuestions;
    private final List<String> allWrongResponses = new ArrayList<>();

    private int maxNumberOfAttempts;
    private int attemptCt = 0;

    private BottomSheetBehavior behavior;
    private NestedScrollView feedback;
    private Button nextButton;
    ViewGroup parentViewGroupForFeedback;
    ViewGroup siblingViewGroupForFeedback;
    View keyboardFocusView;

    private QuestionListener questionListener;

    protected TextToSpeech textToSpeech;

    public interface QuestionListener {
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
        try {
            textToSpeech = ((MainActivity) getActivity()).getTextToSpeech();
        } catch (ClassCastException e){
            //if we can't cast the class to MainActivity,
            //just create a new instance of textToSpeech
            textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    textToSpeech.setLanguage(Locale.US);
                }
            });
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        questionListener.setToolbarState(
                new ToolbarState(getContext().getString(R.string.question_title, questionNumber, totalQuestions),
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
    //we can disable choice after answer when you have multiple attempts.
    //this will be user friendly?
    protected void doSomethingAfterWrongAnswer(View clickedView){}
    //for example clearing a response, hiding the keyboard, etc.
    //should be overridden if using (not required)
    protected void doSomethingAfterResponse(){}
    //needed if we have text views that have clickable spans
    //even if the textViews are not clickable, these events fire
    protected void doSomethingOnFeedbackOpened(){}

    //formatting may be different for certain question types, but this should be the base
    protected String formatWrongFeedbackString(){
        //no specific feedback to give to the user
        if (questionData.getFeedback() != null) {
            List<FeedbackPair> feedbackPairs = questionData.getFeedback();
            List<String> implicitAllWrongResponses = null;
            for (FeedbackPair feedbackPair : feedbackPairs) {
                List<String> responsesToCompare = feedbackPair.getResponse();
                if (feedbackPair.getResponseCheckType() == FeedbackPair.IMPLICIT) {
                    //format the answer and compare
                    if (implicitAllWrongResponses == null) {
                        implicitAllWrongResponses = new ArrayList<>();
                        for (String wrongResponse : allWrongResponses) {
                            implicitAllWrongResponses.add(formatAnswer(wrongResponse));
                        }
                    }

                    for (String responseToCompare : responsesToCompare){
                        responseToCompare = formatAnswer(responseToCompare);
                        if (implicitAllWrongResponses.contains(responseToCompare)){
                            return feedbackPair.getFeedback();
                        }
                    }
                } else if (feedbackPair.getResponseCheckType() == FeedbackPair.EXPLICIT) {
                    //we should check directly to avoid formatAnswer() hiding the feedback
                    //we want to match
                    if (!Collections.disjoint(responsesToCompare, allWrongResponses)) {
                        return feedbackPair.getFeedback();
                    }
                }
            }
        }

        String answer = questionData.getAnswer();
        return "正解: " + answer;
    }

    //same for this
    private String formatCorrectFeedbackString(String response){
        if (questionData.getFeedback() != null){
            List<FeedbackPair> feedbackPairs = questionData.getFeedback();
            for (FeedbackPair feedbackPair : feedbackPairs) {
                List<String> responses = feedbackPair.getResponse();
                if (feedbackPair.getResponseCheckType() == FeedbackPair.EXPLICIT) {
                    if (responses.contains(response)) {
                        return feedbackPair.getFeedback();
                    }
                } else if (feedbackPair.getResponseCheckType() == FeedbackPair.IMPLICIT){
                    String formattedResponse = formatAnswer(response);
                    for (String r : responses){
                        String fr = formatAnswer(r);
                        if (fr.equals(formattedResponse)){
                            return feedbackPair.getFeedback();
                        }
                    }
                }
            }
        }

        return null;
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
    private boolean checkAnswer(String response){
        List<String> allAnswers = new ArrayList<>();
        allAnswers.add(questionData.getAnswer());
        if (questionData.getAcceptableAnswers() != null){
            allAnswers.addAll(questionData.getAcceptableAnswers());
        }

        int answersLength = allAnswers.size();
        for (int i=0; i<answersLength; i++){
            allAnswers.set(i, formatAnswer(allAnswers.get(i)));
        }
        response = formatAnswer(response);

        return allAnswers.contains(response);
    }

    private String formatAnswer(String answer){
        //we still accept technically wrong answers for example
        //names should always be capitalized.
        //this should be considered correct and
        //reinforced in the feedback section
        return answer.toLowerCase().trim();
    }

    protected View.OnClickListener getResponseListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCt++;
                String answer = getResponse(view);
                if (checkAnswer(answer)){
                    questionListener.onRecordResponse(answer, true);
                    openFeedback(true, answer);

                } else {
                    questionListener.onRecordResponse(answer, false);
                    allWrongResponses.add(answer);
                    if (attemptCt == maxNumberOfAttempts){
                        //the user used up all his attempts
                        openFeedback(false, answer);
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
                                doSomethingAfterWrongAnswer(finalView);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        siblingViewGroupForFeedback.startAnimation(wrongAnswerAnimation);
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
        //if last question, better for the user to know that this is the last question
        // (the user will expect the result screen instead of another question)
        if (questionNumber == totalQuestions){
            nextButton.setText(R.string.question_feedback_finish);
        }
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        parentViewGroupForFeedback.addView(feedback);
    }

    private void openFeedback(boolean correct, String response){
        //overridden if we need to do something
        doSomethingOnFeedbackOpened();

        if (correct){
            feedback.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lgreen500));
            nextButton.setTextColor(ContextCompat.getColor(getContext(), R.color.lgreen500));
        } //else condition is default now
        TextView feedbackTitle = feedback.findViewById(R.id.question_feedback_title);
        final String description;
        if (correct){
            feedbackTitle.setText(R.string.question_feedback_correct);
            description = formatCorrectFeedbackString(response);
        } else {
            feedbackTitle.setText(R.string.question_feedback_incorrect);
            description = formatWrongFeedbackString();
        }

        if (keyboardFocusView == null) {
            openFeedbackHelper(description);
        } else {
            if (GUIUtils.hideKeyboard(keyboardFocusView)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openFeedbackHelper(description);
                    }
                }, 700);
            } else {
                openFeedbackHelper(description);
            }
        }

        //we don't want the user to be able to interact with the background,
        //but we want them to be able to see it
        disableBackground(siblingViewGroupForFeedback);
        siblingViewGroupForFeedback.setAlpha(0.5f);
    }

    private void openFeedbackHelper(String description){
        //when we first display the question the bottom sheet is hidden & can be hidden.
        //whether the answer was correct or not,
        //we don't want the user to be able to hide the view because the net button is there
        //so make it non-hide-able
        behavior.setHideable(false);
        final TextView feedbackDescription =
                feedback.findViewById(R.id.question_feedback_description);
        if (description != null && description.length() > 0) {
            feedbackDescription.setText(description);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            //we might not have feedback
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            //make sure to call this after showing the bottom sheet
            //or the bottom sheet won't animate properly
            feedbackDescription.setVisibility(View.GONE);

        }
    }

    private void disableBackground(View view) {
        view.setClickable(false);
        //setting clickable to false doesn't do anything to listeners attached to buttons
        //we can set the listeners to null but we still can touch the buttons
        // and they will hover.
        //setting enabled to false changes some of the buttons' backgrounds so we want to avoid that/
        //so just intercept all touch events
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                disableBackground(child);
            }
        }
    }




}
