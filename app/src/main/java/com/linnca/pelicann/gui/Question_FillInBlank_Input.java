package com.linnca.pelicann.gui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.gui.widgets.GUIUtils;
import com.linnca.pelicann.questiongenerator.QuestionUtils;

//we only have one blank per question
//to make it easier for the user to solve

public class Question_FillInBlank_Input extends Question_General {
    private EditText questionInput;
    private TextView questionTextView;
    private Button submitButton;

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
        View view = inflater.inflate(R.layout.fragment_question_fill_in_blank_input, container, false);
        parentViewGroupForFeedback = view.findViewById(R.id.fragment_question_fill_in_blank_input);
        siblingViewGroupForFeedback = view.findViewById(R.id.question_fill_in_blank_input_main_layout);

        questionTextView = view.findViewById(R.id.question_fill_in_blank_input_question);
        submitButton = view.findViewById(R.id.question_fill_in_blank_input_submit);
        questionInput = view.findViewById(R.id.question_fill_in_blank_input_input);
        createQuestionLayout();
        inflateFeedback(inflater);
        return view;
    }

    @Override
    protected String getResponse(View clickedView){
        return questionInput.getText().toString();
    }

    @Override
    protected int getMaxPossibleAttempts(){
        return Question_General.UNLIMITED_ATTEMPTS;
    }

    @Override
    protected boolean disableChoiceAfterWrongAnswer(){
        return false;
    }

    private void createQuestionLayout(){
        String question = questionData.getQuestion();
        String answer = questionData.getAnswer();

        String blank = GUIUtils.createBlank(answer);

        //the blanks can either be text or numbers, but only one blank
        if (question.contains(QuestionUtils.FILL_IN_BLANK_NUMBER)){
            questionInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            question = question.replace(QuestionUtils.FILL_IN_BLANK_NUMBER, blank);
        } else if (question.contains(QuestionUtils.FILL_IN_BLANK_TEXT)){
            questionInput.setInputType(InputType.TYPE_CLASS_TEXT);
            question = question.replace(QuestionUtils.FILL_IN_BLANK_TEXT, blank);
        }

        //color underline
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(question);
        int startIndex = question.indexOf('_');//Emoji haha
        int endIndex = question.lastIndexOf('_') + 1;
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                ContextCompat.getColor(getContext(),R.color.linkColor)
        );
        stringBuilder.setSpan(colorSpan,startIndex,endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        stringBuilder.setSpan(boldSpan,startIndex,endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        questionTextView.setText(stringBuilder);

        //slightly larger than the answer
        questionInput.setMinEms(answer.length() + 1);
        questionInput.invalidate();

        //set button
        submitButton.setOnClickListener(getResponseListener());
    }

    //hide keyboard
    @Override
    protected void doSomethingAfterResponse(){
        questionInput.clearFocus();
        InputMethodManager imm = (InputMethodManager) questionInput.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(questionInput.getWindowToken(), 0);
        }


    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //change it back to default
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
    }
}
