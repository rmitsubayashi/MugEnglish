package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.lessongenerator.StringUtils;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;

import java.util.Collections;
import java.util.List;

public class Question_FillInBlank_MultipleChoice extends QuestionFragmentInterface {
    public static final int QUESTION_TYPE = 5;
    public static final String FILL_IN_BLANK_MULTIPLE_CHOICE = "@blankMC@";
    private TextView questionTextView;
    private LinearLayout choicesLayout;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_question_fill_in_blank_multiple_choice, container, false);
        parentViewGroupForFeedback = view.findViewById(R.id.fragment_question_fill_in_blank_multiple_choice);
        siblingViewGroupForFeedback = view.findViewById(R.id.question_fill_in_blank_multiple_choice_main_layout);

        questionTextView = view.findViewById(R.id.question_fill_in_blank_multiple_choice_question);
        choicesLayout = view.findViewById(R.id.question_fill_in_blank_multiple_choice_choices_layout);

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

    @Override
    protected void doSomethingOnFeedbackOpened(boolean correct, String response){
        TextToSpeechHelper.disableTextToSpeech(questionTextView);
    }

    private void populateQuestion(){
        String question = questionData.getQuestion();
        String answer = questionData.getAnswer();
        String blank = Question_FillInBlank_Input.createBlank(answer);
        //in case there are more than one fill in the blanks
        question = question.replaceAll(FILL_IN_BLANK_MULTIPLE_CHOICE, blank);
        //color underline.
        //same code in fill in blank input
        int stringLength = question.length();
        boolean blankStarted = false;
        int blankStartIndex = -1;
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(question);
        for (int i=0; i<stringLength; i++) {
            char c = question.charAt(i);
            if (c == '_'){
                if (!blankStarted){
                    blankStarted = true;
                    blankStartIndex = i;
                }
            } else {
                if (blankStarted){
                    setBlank(blankStartIndex, i, stringBuilder);
                    blankStarted = false;
                }
            }

        }
        //if the question ends with a blank
        if (blankStarted){
            setBlank(blankStartIndex, stringLength, stringBuilder);
        }
        questionTextView.setText(
                TextToSpeechHelper.clickToSpeechTextViewSpannable(
                        questionTextView, question, stringBuilder, textToSpeech)
        );
    }

    private void setBlank(int startIndex, int endIndex, SpannableStringBuilder stringBuilder){
        //end of blank
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                ThemeColorChanger.getColorFromAttribute(
                        R.attr.color700, getContext())
        );
        stringBuilder.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        stringBuilder.setSpan(boldSpan, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

    }

    private void populateButtons(LayoutInflater inflater){
        //dynamically add buttons because
        //we may have multiple choice questions with 3 or 4 questions
        List<String> choices = questionData.getChoices();
        Collections.shuffle(choices);

        for (String choice : choices){
            Button choiceButton = (Button)inflater.
                    inflate(R.layout.inflatable_question_multiple_choice_button, choicesLayout, false);
            choiceButton.setText(choice);
            //for checking answer
            choiceButton.setTag(choice);
            choiceButton.setOnClickListener(getResponseListener());

            if (StringUtils.isAlphanumeric(choice)) {
                final String fChoice = choice;
                choiceButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        TextToSpeechHelper.startTextToSpeech(textToSpeech, fChoice);
                        return true;
                    }
                });
            }

            choicesLayout.addView(choiceButton);

        }
    }
}
