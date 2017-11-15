package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.linnca.pelicann.R;

public class Question_TranslateWord extends Question_General {
    public static final int QUESTION_TYPE = 8;
    private EditText questionInput;
    private TextView wordToTranslateTextView;
    private Button submitButton;
    private TextView instructions;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //as of now, this is the only way
        //I can stick the response feedback bottom sheet on the bottom.
        //(if I have the keyboard open while submitting an answer,
        //the bottom sheet comes above the keyboard even though
        //it is closing
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_question_translate_word, container, false);
        parentViewGroupForFeedback = view.findViewById(R.id.fragment_question_translate_word);
        siblingViewGroupForFeedback = view.findViewById(R.id.question_translate_word_main_layout);

        wordToTranslateTextView = view.findViewById(R.id.question_translate_word_word_to_translate);
        submitButton = view.findViewById(R.id.question_translate_word_submit);
        questionInput = view.findViewById(R.id.question_translate_word_input);
        instructions = view.findViewById(R.id.question_translate_word_instructions);

        keyboardFocusView = questionInput;

        createQuestionLayout();
        inflateFeedback(inflater);
        return view;
    }

    @Override
    protected String getResponse(View clickedView){
        return questionInput.getText().toString();
    }

    private void createQuestionLayout(){
        String question = questionData.getQuestion();
        String answer = questionData.getAnswer();
        instructions.setText(R.string.question_translate_word_number_instructions);

        wordToTranslateTextView.setText(
                QuestionUtils.clickToSpeechTextViewSpannable(wordToTranslateTextView, question, new SpannableString(question), textToSpeech)
        );
        if (question.length() < 10){
            wordToTranslateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        } else {
            wordToTranslateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        }

        if (answer.length() < 10){
            questionInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        } else {
            questionInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        }
        //slightly larger than the answer
        questionInput.setMinEms(answer.length() + 1);
        questionInput.invalidate();

        //set button
        submitButton.setOnClickListener(getResponseListener());
    }

    @Override
    protected void doSomethingOnFeedbackOpened(boolean correct, String response){
        QuestionUtils.disableTextToSpeech(wordToTranslateTextView);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //change it back to default
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
    }
}