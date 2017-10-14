package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.linnca.pelicann.R;

public class Question_TrueFalse extends Question_General {
    public static final String TRUE_FALSE_QUESTION_TRUE = "true";
    public static final String TRUE_FALSE_QUESTION_FALSE = "false";
    private TextView questionTextView;
    private Button trueButton;
    private Button falseButton;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_question_true_false, container, false);
        parentViewGroupForFeedback = view.findViewById(R.id.fragment_question_true_false);
        siblingViewGroupForFeedback = view.findViewById(R.id.question_true_false_main_layout);

        questionTextView = view.findViewById(R.id.question_true_false_question);
        trueButton = view.findViewById(R.id.question_true_false_true);
        falseButton = view.findViewById(R.id.question_true_false_false);

        populateQuestion();
        setButtonActionListeners();
        inflateFeedback(inflater);

        return view;
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
    protected String formatWrongFeedbackString(){
        //we really don't need feedback for true false questions
        return null;
    }

    @Override
    protected void doSomethingOnFeedbackOpened(){
        QuestionUtils.disableTextToSpeech(questionTextView);
    }

    private void populateQuestion(){
        String question = questionData.getQuestion();
        questionTextView.setText(
                QuestionUtils.clickToSpeechTextViewSpannable(
                        questionTextView, question, new SpannableString(question),textToSpeech)
        );
    }

    private void setButtonActionListeners(){
        trueButton.setTag(TRUE_FALSE_QUESTION_TRUE);
        falseButton.setTag(TRUE_FALSE_QUESTION_FALSE);

        trueButton.setOnClickListener(getResponseListener());
        falseButton.setOnClickListener(getResponseListener());

    }

    public static String getTrueFalseString(boolean tf){
        return tf ? TRUE_FALSE_QUESTION_TRUE : TRUE_FALSE_QUESTION_FALSE;
    }
}
