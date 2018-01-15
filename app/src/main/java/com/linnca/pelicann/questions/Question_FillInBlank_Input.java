package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;

//we only have one blank per question
//to make it easier for the user to solve

public class Question_FillInBlank_Input extends QuestionFragmentInterface {
    public static final String FILL_IN_BLANK_TEXT = "@blankText@";
    public  static final String FILL_IN_BLANK_NUMBER = "@blankNum@";
    private EditText questionInput;
    private TextView questionTextView;
    private Button submitButton;
    private TextView instructions;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

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

    //used for fill in the blank questions
    static String createBlank(String answer){
        //just slightly bigger than the answer
        int length = answer.length() + 1;
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<length; i++){
            builder.append("_");
        }
        return builder.toString();
    }

    private void createQuestionLayout(){
        String question = questionData.getQuestion();
        String answer = questionData.getAnswer();

        String blank = createBlank(answer);

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
                ThemeColorChanger.getColorFromAttribute(R.attr.color700, getContext())
        );
        stringBuilder.setSpan(colorSpan,startIndex,endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        stringBuilder.setSpan(boldSpan,startIndex,endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);


        questionTextView.setText(
                TextToSpeechHelper.clickToSpeechTextViewSpannable(questionTextView, question, stringBuilder, textToSpeech)
        );

        //slightly larger than the answer
        questionInput.setMinEms(answer.length() + 1);
        questionInput.invalidate();

        //set submit
        submitButton.setOnClickListener(getResponseListener());
        //also for keyboard enter button
        questionInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            submitButton.performClick();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void doSomethingOnFeedbackOpened(boolean correct, String response){
        TextToSpeechHelper.disableTextToSpeech(questionTextView);
    }
}
