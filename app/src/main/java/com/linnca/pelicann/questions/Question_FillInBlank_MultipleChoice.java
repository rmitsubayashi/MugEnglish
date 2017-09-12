package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import com.linnca.pelicann.mainactivity.widgets.GUIUtils;
import com.linnca.pelicann.lessongenerator.QuestionUtils;

import java.util.List;

public class Question_FillInBlank_MultipleChoice extends Question_General {
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
    protected int getMaxPossibleAttempts(){
        //it should be the number of choices - 1 (the last one is obvious)
        int choiceCt = questionData.getChoices().size();
        return (choiceCt - 1);
    }

    @Override
    protected boolean disableChoiceAfterWrongAnswer(){
        return true;
    }

    private void populateQuestion(){
        String question = questionData.getQuestion();
        String answer = questionData.getAnswer();
        String blank = GUIUtils.createBlank(answer);
        question = question.replace(QuestionUtils.FILL_IN_BLANK_MULTIPLE_CHOICE, blank);
        //color underline.
        //same code in fill in blank input
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


            choicesLayout.addView(choiceButton);

        }
    }
}
