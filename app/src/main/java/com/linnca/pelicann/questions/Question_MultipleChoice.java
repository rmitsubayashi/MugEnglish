package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linnca.pelicann.R;

import java.util.List;

public class Question_MultipleChoice extends Question_General {
    public static final int QUESTION_TYPE = 2;
    private TextView questionTextView;
    private LinearLayout choicesLayout;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_question_multiple_choice, container, false);
        parentViewGroupForFeedback = view.findViewById(R.id.fragment_question_multiple_choice);
        siblingViewGroupForFeedback = view.findViewById(R.id.question_multiple_choice_main_layout);

        questionTextView = view.findViewById(R.id.question_multiple_choice_question);
        choicesLayout = view.findViewById(R.id.question_multiple_choice_choices_layout);

        populateQuestion();
        populateButtons(inflater);
        inflateFeedback(inflater);
        return view;
    }

    @Override
    protected String getResponse(View clickedView){
        return (String)clickedView.getTag();
    }

    @Override
    protected void doSomethingAfterWrongAnswer(View clickedView){
        clickedView.setEnabled(false);
    }

    private void populateQuestion(){
        String question = questionData.getQuestion();
        questionTextView.setText(
                QuestionUtils.clickToSpeechTextViewSpannable(questionTextView,question,new SpannableString(question), textToSpeech)
        );
    }

    @Override
    protected void doSomethingOnFeedbackOpened(boolean correct, String response){
        QuestionUtils.disableTextToSpeech(questionTextView);
    }

    private void populateButtons(LayoutInflater inflater){
        //dynamically add buttons because
        //we may have multiple choice questions with 3 or 4 questions
        List<String> choices = questionData.getChoices();
        QuestionUtils.shuffle(choices);

        for (String choice : choices){
            Button choiceButton = (Button)inflater.
                    inflate(R.layout.inflatable_question_multiple_choice_button, choicesLayout, false);
            choiceButton.setText(choice);
            //for checking answer
            choiceButton.setTag(choice);
            choiceButton.setOnClickListener(getResponseListener());
            if (QuestionUtils.isAlphanumeric(choice)) {
                final String fChoice = choice;
                choiceButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        QuestionUtils.startTextToSpeech(textToSpeech, fChoice);
                        return true;
                    }
                });
            }
            choicesLayout.addView(choiceButton);

        }
    }
}
