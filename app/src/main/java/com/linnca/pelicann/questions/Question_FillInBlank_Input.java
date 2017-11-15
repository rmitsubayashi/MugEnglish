package com.linnca.pelicann.questions;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.linnca.pelicann.R;

//we only have one blank per question
//to make it easier for the user to solve

public class Question_FillInBlank_Input extends Question_General {
    public static final int QUESTION_TYPE = 4;
    public static final String FILL_IN_BLANK_TEXT = "@blankText@";
    public  static final String FILL_IN_BLANK_NUMBER = "@blankNum@";
    private EditText questionInput;
    private TextView questionTextView;
    private Button submitButton;
    private TextView instructions;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.questionType = QUESTION_TYPE;
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
        instructions = view.findViewById(R.id.question_fill_in_blank_input_instructions);

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

        String blank = QuestionUtils.createBlank(answer);

        //the blanks can either be text or numbers, but only one blank
        if (question.contains(FILL_IN_BLANK_NUMBER)){
            questionInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            question = question.replace(FILL_IN_BLANK_NUMBER, blank);
            instructions.setText(R.string.question_fill_in_blank_input_number_instructions);
        } else if (question.contains(FILL_IN_BLANK_TEXT)){
            questionInput.setInputType(InputType.TYPE_CLASS_TEXT);
            question = question.replace(FILL_IN_BLANK_TEXT, blank);
            instructions.setText(R.string.question_fill_in_blank_input_text_instructions);
        }

        //color underline
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(question);
        int startIndex = question.indexOf('_');//Emoji haha
        int endIndex = question.lastIndexOf('_') + 1;
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                ContextCompat.getColor(getContext(),R.color.lblue700)
        );
        stringBuilder.setSpan(colorSpan,startIndex,endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        stringBuilder.setSpan(boldSpan,startIndex,endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);


        questionTextView.setText(
                QuestionUtils.clickToSpeechTextViewSpannable(questionTextView, question, stringBuilder, textToSpeech)
        );

        //slightly larger than the answer
        questionInput.setMinEms(answer.length() + 1);
        questionInput.invalidate();

        //set button
        submitButton.setOnClickListener(getResponseListener());
    }

    @Override
    protected void doSomethingOnFeedbackOpened(boolean correct, String response){
        QuestionUtils.disableTextToSpeech(questionTextView);
    }
}
