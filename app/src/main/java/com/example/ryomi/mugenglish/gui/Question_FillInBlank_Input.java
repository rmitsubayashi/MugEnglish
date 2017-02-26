package com.example.ryomi.mugenglish.gui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.gui.widgets.GUIUtils;
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.mugenglish.questionmanager.QuestionManager;

//we only have one blank per question
//to make it easier for the user to solve

public class Question_FillInBlank_Input extends Question_General {
    EditText questionInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createQuestionLayout();
    }

    @Override
    protected int getLayoutResourceID(){
        return R.layout.activity_question_fill_in_blank_input;
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

    @Override
    protected ViewGroup getParentViewForFeedback(){
        return (ViewGroup)findViewById(R.id.activity_question_fill_in_blank_input);
    }

    @Override
    protected ViewGroup getSiblingViewForFeedback(){
        return (ViewGroup)findViewById(R.id.question_fill_in_blank_input_main_layout);
    }

    private void createQuestionLayout(){
        TextView questionTextView = (TextView) findViewById(R.id.question_fill_in_blank_input_question);

        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        String question = data.getQuestion();
        String answer = data.getAnswer();

        questionInput = (EditText) findViewById(R.id.question_fill_in_blank_input_input);
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
                ContextCompat.getColor(this,R.color.linkColor)
        );
        stringBuilder.setSpan(colorSpan,startIndex,endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        stringBuilder.setSpan(boldSpan,startIndex,endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        questionTextView.setText(stringBuilder);

        //slightly larger than the answer
        questionInput.setMinEms(answer.length() + 1);
        questionInput.invalidate();

        //set button
        Button submitButton = (Button)findViewById(R.id.question_fill_in_blank_input_submit);
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
}
