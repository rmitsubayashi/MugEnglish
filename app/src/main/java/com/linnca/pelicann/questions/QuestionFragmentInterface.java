package com.linnca.pelicann.questions;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.BottomSheetBehavior;
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
import android.widget.TextView;
import android.widget.Toast;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.GUIUtils;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.ToolbarState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.lessonquestions.MaxNumberOfQuestionAttemptsHelper;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionFeedbackFormatter;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionResponseChecker;

//sets methods common for all question GUIs
public abstract class QuestionFragmentInterface extends Fragment {
    public static final String TAG = "Question Fragment";
    public static final String BUNDLE_QUESTION_DATA = "bundleQuestionData";
    public static final String BUNDLE_QUESTION_NUMBER = "bundleQuestionNumber";
    public static final String BUNDLE_QUESTION_TOTAL_QUESTIONS = "bundleTotalQuestions";

    protected QuestionData questionData;
    private int questionNumber;
    private int totalQuestions;
    private final List<String> allWrongResponses = new ArrayList<>();
    //keep track of the number of attempts for this question
    private int attemptCt = 0;

    private BottomSheetBehavior behavior;
    private NestedScrollView feedback;
    private Button nextButton;
    protected ViewGroup parentViewGroupForFeedback;
    protected ViewGroup siblingViewGroupForFeedback;
    protected View keyboardFocusView;

    //we reset the feedback next button's properties when we click it,
    // but we might need to show it again in case we can't establish a
    // connection. so, save whether the feedback is for a correct or incorrect
    // response
    private Boolean responseCorrect = null;

    private QuestionListener questionListener;

    protected TextToSpeech textToSpeech;

    public interface QuestionListener {
        //this is for every time the user moves on to the next question
        void onNextQuestion(boolean correct, OnDBResultListener noConnectionListener);
        //this is for every response, whether the user moves on to
        // the next question or not
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
                        false, false, null)
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
    //we can disable choice after answer when you have multiple attempts.
    //this will be user friendly?
    protected void doSomethingAfterWrongAnswer(View clickedView){}
    //for example clearing a response, hiding the keyboard, etc.
    //should be overridden if using (not required)
    protected void doSomethingAfterResponse(){}
    //needed if we have text views that have clickable spans
    //even if the textViews are not clickable, these events fire
    protected void doSomethingOnFeedbackOpened(boolean correct, String response){}

    protected View.OnClickListener getResponseListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCt++;
                String answer = getResponse(view);
                //if the user got it right
                if (QuestionResponseChecker.checkResponse(questionData, answer)){
                    questionListener.onRecordResponse(answer, true);
                    openFeedback(true, answer);

                } else { //if the user didn't get it right
                    questionListener.onRecordResponse(answer, false);
                    allWrongResponses.add(answer);
                    //check if the user should be given another chance
                    int maxNumberOfAttempts = MaxNumberOfQuestionAttemptsHelper.getMaxNumberOfQuestionAttempts(
                            questionData,
                            new MaxNumberOfQuestionAttemptsHelper.UserGetter() {
                                @Override
                                public int getMaxNumberOfQuestionAttemptsSetByUser() {
                                    SharedPreferences sharedPreferences =
                                            PreferenceManager.getDefaultSharedPreferences(getContext());
                                    //the preference is still stored as a string
                                    String preferencesMaxAttemptsString = sharedPreferences.getString
                                            (getString(R.string.preferences_questions_numberOfAttemptsPerQuestion_key), "1");
                                    return Integer.parseInt(preferencesMaxAttemptsString);
                                }
                            }
                    );
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
        //if last question, better for the user to know that this is the last question
        // (the user will expect the result screen instead of another question)
        if (questionNumber == totalQuestions){
            nextButton.setText(R.string.question_feedback_finish);
        }
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        parentViewGroupForFeedback.addView(feedback);
    }

    private void openFeedback(final boolean correct, String response){
        //overridden if we need to do something
        doSomethingOnFeedbackOpened(correct, response);

        //save in case we need to format the feedback again
        responseCorrect = correct;
        if (correct){
            feedback.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_green500));
        } //else condition is default now
        TextView feedbackTitle = feedback.findViewById(R.id.question_feedback_title);
        final String description = QuestionFeedbackFormatter.formatFeedback(correct,
                questionData, response, allWrongResponses);
        if (correct){
            feedbackTitle.setText(R.string.question_feedback_correct);
        } else {
            feedbackTitle.setText(R.string.question_feedback_incorrect);
        }

        if (keyboardFocusView == null) {
            openFeedbackHelper(description);
        } else {
            if (GUIUtils.hideKeyboard(keyboardFocusView)) {
                //we want to wait until the keyboard close,
                // then show the feedback
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

    //what happens when we can't get a connection when going to the next
    // question
    private OnDBResultListener getNoConnectionListener(){
        return new OnDBResultListener() {
            @Override
            public void onNoConnection() {
                //enable the button
                enableFeedbackNextButton();
                //let the user know he doesn't have a connection
                Toast.makeText(getContext(), R.string.no_connection, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onSlowConnection() {
                super.onSlowConnection();
            }
        };
    }

    private void openFeedbackHelper(String description){
        //when we first display the question the bottom sheet is hidden & can be hidden.
        //we don't want the user to be able to hide the view because the next button is there
        //so make it non-hide-able
        behavior.setHideable(false);
        final TextView feedbackDescription =
                feedback.findViewById(R.id.question_feedback_description);
        if (description != null && description.length() > 0) {
            //if we have feedback, whether the answer is correct or incorrect,
            // we need to show the next button
            formatFeedbackNextButton(responseCorrect);
            feedbackDescription.setText(description);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            //no feedback, so
            // 1. if the answer is incorrect, we want to let the user check the question
            // again (some incorrect answers will have no feedback (i.e. t/f questions)
            // 2. if the answer is correct, we don't care if the user checks the question
            // again, so set a timer and automatically go to the next question
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            //make sure to call this after showing the bottom sheet
            //or the bottom sheet won't animate properly
            feedbackDescription.setVisibility(View.GONE);

            if (responseCorrect){
                nextButton.setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        questionListener.onNextQuestion(responseCorrect, getNoConnectionListener());
                    }
                }, 1000);
            } else {
                formatFeedbackNextButton(responseCorrect);
            }

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

    private void formatFeedbackNextButton(final boolean correct){
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableFeedbackNextButton();
                questionListener.onNextQuestion(correct, getNoConnectionListener());
            }
        });
        if (correct){
            nextButton.setTextColor(ContextCompat.getColor(getContext(), R.color.light_green500));
        } else {
            nextButton.setTextColor(ContextCompat.getColor(getContext(), R.color.orange500));
        }
    }

    private void disableFeedbackNextButton(){
        //prevent multiple clicks
        nextButton.setOnClickListener(null);
        nextButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        //make sure the user knows that he clicked it (in case it lags)
        nextButton.setBackgroundResource(R.drawable.transparent_button);
        nextButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        nextButton.setAlpha(0.3f);
    }

    private void enableFeedbackNextButton(){
        nextButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        nextButton.setBackgroundResource(R.drawable.white_button);
        nextButton.setAlpha(1f);
        formatFeedbackNextButton(responseCorrect);

    }

}
