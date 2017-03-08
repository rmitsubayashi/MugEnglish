package com.example.ryomi.mugenglish.gui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.mugenglish.questionmanager.QuestionManager;

import java.util.List;

public class Question_MultipleChoice extends Question_General {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        populateQuestion();
        populateButtons();
    }

    @Override
    protected int getLayoutResourceID(){
        return R.layout.activity_question_multiple_choice;
    }

    @Override
    protected String getResponse(View clickedView){
        return (String)clickedView.getTag();
    }

    @Override
    protected int getMaxPossibleAttempts(){
        //it should be the number of choices - 1 (the last one is obvious)
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        int choiceCt = data.getChoices().size();
        return (choiceCt - 1);
    }

    @Override
    protected boolean disableChoiceAfterWrongAnswer(){
        return true;
    }

    @Override
    protected ViewGroup getParentViewForFeedback(){
        return (ViewGroup)findViewById(R.id.activity_question_multiple_choice);
    }

    @Override
    protected ViewGroup getSiblingViewForFeedback(){
        return (ViewGroup)findViewById(R.id.question_multiple_choice_main_layout);
    }

    private void populateQuestion(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        String question = data.getQuestion();

        TextView questionTextView = (TextView) findViewById(R.id.question_multiple_choice_question);
        questionTextView.setText(question);
    }

    private void populateButtons(){
        //dynamically add buttons because
        //we may have multiple choice questions with 3 or 4 questions
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        List<String> choices = data.getChoices();
        QuestionUtils.shuffle(choices);

        LinearLayout choicesLayout = (LinearLayout) findViewById(R.id.question_multiple_choice_choices_layout);

        for (String choice : choices){
            Button choiceButton = (Button)getLayoutInflater().
                    inflate(R.layout.inflatable_question_multiple_choice_button, choicesLayout, false);
            choiceButton.setText(choice);
            //for checking answer
            choiceButton.setTag(choice);
            choiceButton.setOnClickListener(getResponseListener());
            choicesLayout.addView(choiceButton);

        }
    }
}
