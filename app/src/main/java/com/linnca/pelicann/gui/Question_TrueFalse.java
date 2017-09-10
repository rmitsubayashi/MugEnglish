package com.linnca.pelicann.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.questiongenerator.QuestionUtils;

public class Question_TrueFalse extends Question_General {
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
        parentViewGroupForFeedback = (ViewGroup)view.findViewById(R.id.fragment_question_true_false);
        siblingViewGroupForFeedback = (ViewGroup)view.findViewById(R.id.question_true_false_main_layout);

        questionTextView = (TextView) view.findViewById(R.id.question_true_false_question);
        trueButton = (Button) view.findViewById(R.id.question_true_false_true);
        falseButton = (Button) view.findViewById(R.id.question_true_false_false);

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
    protected boolean disableChoiceAfterWrongAnswer(){
        //technically it's true but it doesn't matter because we only need one attempt
        //before showing the answer
        return true;
    }

    @Override
    protected String formatWrongFeedbackString(){
        //we really don't need feedback for true false questions
        return null;
    }

    private void populateQuestion(){
        questionTextView.setText(questionData.getQuestion());
    }

    private void setButtonActionListeners(){
        trueButton.setTag(QuestionUtils.TRUE_FALSE_QUESTION_TRUE);
        falseButton.setTag(QuestionUtils.TRUE_FALSE_QUESTION_FALSE);

        trueButton.setOnClickListener(getResponseListener());
        falseButton.setOnClickListener(getResponseListener());

    }
}
