package com.example.ryomi.myenglish.gui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;

public class Question_TrueFalse extends Question_General {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        populateQuestion();
        setButtonActionListeners();
    }

    @Override
    protected int getLayoutResourceID(){
        return R.layout.activity_question_true_false;
    }

    @Override
    protected String getResponse(View clickedView){
        return (String)clickedView.getTag();
    }

    @Override
    protected int getMaxPossibleAttempts(){
        //it's either true or false..
        return 1;
    }

    @Override
    protected boolean disableChoiceAfterWrongAnswer(){
        //technically it's true but it doesn't matter because we only need one attempt
        //before showing the answer
        return true;
    }

    @Override
    protected ViewGroup getParentViewForFeedback(){
        return (ViewGroup)findViewById(R.id.activity_question_true_false);
    }

    @Override
    protected ViewGroup getSiblingViewForFeedback(){
        return (ViewGroup)findViewById(R.id.question_true_false_main_layout);
    }

    private void populateQuestion(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        TextView questionTextView = (TextView) findViewById(R.id.question_true_false_question);
        questionTextView.setText(data.getQuestion());
    }

    private void setButtonActionListeners(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        final String answer = data.getAnswer();

        Button trueButton = (Button) findViewById(R.id.question_true_false_true);
        Button falseButton = (Button) findViewById(R.id.question_true_false_false);
        trueButton.setTag(QuestionUtils.TRUE_FALSE_QUESTION_TRUE);
        falseButton.setTag(QuestionUtils.TRUE_FALSE_QUESTION_FALSE);

        trueButton.setOnClickListener(getResponseListener());
        falseButton.setOnClickListener(getResponseListener());

    }
}
