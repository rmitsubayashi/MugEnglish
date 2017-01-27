package com.example.ryomi.myenglish.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;

public class Question_TrueFalse extends Question_General {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QuestionManager.getInstance().setCurrentContext(this);
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
