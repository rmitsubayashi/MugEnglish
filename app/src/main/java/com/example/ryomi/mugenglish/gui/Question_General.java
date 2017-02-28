package com.example.ryomi.mugenglish.gui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.questionmanager.QuestionManager;

import java.util.ArrayList;
import java.util.List;

//sets methods common for all question guis
public abstract class Question_General extends AppCompatActivity {
    public static int UNLIMITED_ATTEMPTS = -1;
    protected int maxNumberOfAttempts;
    protected int attemptCt = 0;
    protected boolean disableChoiceAfterWrongAnswer;
    private BottomSheetBehavior behavior;
    private NestedScrollView feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceID());
        setMaxNumberOfAttempts();
        disableChoiceAfterWrongAnswer = disableChoiceAfterWrongAnswer();
        inflateFeedback();
    }

    //to instantiate the activity
    protected abstract int getLayoutResourceID();
    //need this to record response
    protected abstract String getResponse(View clickedView);
    //how many chances are possibly allowed for each question type
    protected abstract int getMaxPossibleAttempts();
    //whether to disable choice after answer when you have multiple attempts.
    //this will be user friendly?
    protected abstract boolean disableChoiceAfterWrongAnswer();
    //inflate feedback layout
    protected abstract ViewGroup getParentViewForFeedback();
    //disable this view when the feedback pops up
    protected abstract ViewGroup getSiblingViewForFeedback();
    //for example clearing a response, hiding the keyboard, etc.
    //should be overridden if using
    protected void doSomethingAfterResponse(){

    }
    //formatting may be different for certain question types, but this should be the base
    protected String getFeedback(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        String answer = data.getAnswer();
        return "正解: " + answer;
    }

    private void setMaxNumberOfAttempts(){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        List<String> allAnswers = new ArrayList<>();
        allAnswers.add(data.getAnswer());
        if (data.getAcceptableAnswers() != null){
            allAnswers.addAll(data.getAcceptableAnswers());
        }
        return allAnswers.contains(response);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        //also reset manager
        QuestionManager.getInstance().resetManager();
    }

    protected View.OnClickListener getResponseListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCt++;
                String answer = getResponse(view);
                if (checkAnswer(answer)){
                    QuestionManager.getInstance().recordResponse(answer, true);
                    openFeedback(true);
                } else {
                    QuestionManager.getInstance().recordResponse(answer, false);
                    if (attemptCt == maxNumberOfAttempts){
                        //the user used up all his attempts
                        openFeedback(false);
                    } else {
                        Toast.makeText(Question_General.this, answer, Toast.LENGTH_SHORT).show();
                        if (disableChoiceAfterWrongAnswer)
                            view.setEnabled(false);
                    }
                }

                doSomethingAfterResponse();
            }
        };
    }

    private void inflateFeedback(){
        ViewGroup parentView = getParentViewForFeedback();
        feedback = (NestedScrollView) getLayoutInflater()
                .inflate(R.layout.inflatable_question_feedback, parentView, false);
        behavior = BottomSheetBehavior.from(feedback);
        Button nextButton = (Button)feedback.findViewById(R.id.question_feedback_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionManager.getInstance().nextQuestion(Question_General.this);
            }
        });
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        getParentViewForFeedback().addView(feedback);
    }

    private void openFeedback(boolean correct){
        //when we first display the question the bottom sheet is hideable & hidden
        //whether the answer was correct or not,
        //we don't want the user to be able to hide the view
        behavior.setHideable(false);

        TextView feedbackTitle = (TextView)feedback.findViewById(R.id.question_feedback_title);
        if (correct){
            feedbackTitle.setText(R.string.question_feedback_correct);
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            feedbackTitle.setText(R.string.question_feedback_incorrect);
            TextView feedbackDescription =
                    (TextView)feedback.findViewById(R.id.question_feedback_description);
            feedbackDescription.setText(getFeedback());
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }


        disableBackground(getSiblingViewForFeedback());
        getSiblingViewForFeedback().setAlpha(0.5f);
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
