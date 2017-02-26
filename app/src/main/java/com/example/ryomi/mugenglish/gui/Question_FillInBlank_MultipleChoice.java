package com.example.ryomi.mugenglish.gui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.gui.widgets.GUIUtils;
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.mugenglish.questionmanager.QuestionManager;

import java.util.List;

public class Question_FillInBlank_MultipleChoice extends Question_General {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        populateQuestion();
        populateButtons();
    }

    @Override
    protected int getLayoutResourceID(){
        return R.layout.activity_question_fill_in_blank_multiple_choice;
    }

    @Override
    protected String getResponse(View clickedView){
        return (String)clickedView.getTag();
    }

    @Override
    protected int getMaxPossibleAttempts(){
        //it should be the number of choices - 1 (the last one is obvious)
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        int choiceCt = data.getChoices().size();
        return (choiceCt - 1);
    }

    @Override
    protected boolean disableChoiceAfterWrongAnswer(){
        return true;
    }

    @Override
    protected ViewGroup getParentViewForFeedback(){
        return (ViewGroup)findViewById(R.id.activity_question_fill_in_blank_multiple_choice);
    }

    @Override
    protected ViewGroup getSiblingViewForFeedback(){
        return (ViewGroup)findViewById(R.id.question_fill_in_blank_multiple_choice_main_layout);
    }

    private void populateQuestion(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        String question = data.getQuestion();
        String answer = data.getAnswer();
        String blank = GUIUtils.createBlank(answer);
        question = question.replace(QuestionUtils.FILL_IN_BLANK_MULTIPLE_CHOICE, blank);
        //color underline.
        //same code in fill in blank input
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(question);
        int startIndex = question.indexOf('_');//Emoji haha
        int endIndex = question.lastIndexOf('_') + 1;
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                ContextCompat.getColor(this,R.color.linkColor)
        );
        stringBuilder.setSpan(colorSpan,startIndex,endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        stringBuilder.setSpan(boldSpan,startIndex,endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);


        TextView questionTextView = (TextView) findViewById(R.id.question_fill_in_blank_multiple_choice_question);
        questionTextView.setText(stringBuilder);
    }

    private void populateButtons(){
        //dynamically add buttons because
        //we may have multiple choice questions with 3 or 4 questions
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        List<String> choices = data.getChoices();
        QuestionUtils.shuffle(choices);

        LinearLayout choicesLayout = (LinearLayout) findViewById(R.id.question_fill_in_blank_multiple_choice_choices_layout);

        for (String choice : choices){
            Button choiceButton = (Button)getLayoutInflater().
                    inflate(R.layout.inflatable_question_multiple_choice_button, choicesLayout, false);
            choiceButton.setText(choice);
            //for checking answer
            choiceButton.setTag(choice);
            choiceButton.setOnClickListener(getResponseListener());


            choicesLayout.addView(choiceButton);

        }
    }
}
