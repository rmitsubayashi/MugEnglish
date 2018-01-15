package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.linnca.pelicann.R;

import pelicann.linnca.com.corefunctionality.questions.QuestionSerializer;

public class Question_TrueFalse extends QuestionFragmentInterface {
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
    protected void doSomethingOnFeedbackOpened(boolean correct, String response){
        TextToSpeechHelper.disableTextToSpeech(questionTextView);
    }

    private void populateQuestion(){
        String question = questionData.getQuestion();
        questionTextView.setText(
                TextToSpeechHelper.clickToSpeechTextViewSpannable(
                        questionTextView, question, new SpannableString(question),textToSpeech)
        );
    }

    private void setButtonActionListeners(){
        trueButton.setTag(QuestionSerializer.serializeTrueFalseAnswer(true));
        falseButton.setTag(QuestionSerializer.serializeTrueFalseAnswer(false));

        trueButton.setOnClickListener(getResponseListener());
        falseButton.setOnClickListener(getResponseListener());

    }
}
