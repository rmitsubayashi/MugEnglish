package com.example.ryomi.myenglish.gui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;

import org.apmem.tools.layouts.FlowLayout;

import java.util.List;


public class Question_Puzzle_Piece extends Question_General {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        populateQuestion();
        createChoiceButtons();
        setSubmitButtonListener();
    }

    @Override
    protected int getLayoutResourceID(){
        return R.layout.activity_question_puzzle_piece;
    }

    //for the puzzles the clicked view is a submit button
    //so it's not relevant
    @Override
    protected String getResponse(View clickedView){
        FlowLayout answerLayout = (FlowLayout) findViewById(R.id.question_puzzle_piece_answer);
        int childCt = answerLayout.getChildCount();
        String answer = "";
        for (int i=0; i<childCt; i++){
            Button choiceButton = (Button)answerLayout.getChildAt(i);
            String choice = choiceButton.getText().toString();
            answer += choice + "|";
        }
        //just in case nothing is selected
        if (answer.length() > 0){
            answer = answer.substring(0,answer.length()-1);
        }

        return answer;
    }

    @Override
    protected int getMaxPossibleAttempts(){
        //technically not unlimited but it doesn't matter too much
        return Question_General.UNLIMITED_ATTEMPTS;
    }

    @Override
    protected boolean disableChoiceAfterWrongAnswer(){
        return false;
    }

    @Override
    protected ViewGroup getParentViewForFeedback(){
        return (ViewGroup)findViewById(R.id.activity_question_puzzle_piece);
    }

    @Override
    protected ViewGroup getSiblingViewForFeedback(){
        return (ViewGroup)findViewById(R.id.question_puzzle_piece_main_layout);
    }

    private void populateQuestion(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        TextView questionTextView = (TextView) findViewById(R.id.question_puzzle_piece_question);
        questionTextView.setText(data.getQuestion());
    }


    private void createChoiceButtons(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        List<String> choices = data.getChoices();
        QuestionUtils.shuffle(choices);


        //creating a new button every time is expensive?

        FlowLayout choicesLayout = (FlowLayout) findViewById(R.id.question_puzzle_piece_choiceRow);
        final FlowLayout answerLayout =
                (FlowLayout) findViewById(R.id.question_puzzle_piece_answer);
        final ScrollView scrollView = (ScrollView)findViewById(R.id.question_puzzle_piece_answer_scroll);
        final RelativeLayout grandparentLayout = (RelativeLayout)findViewById(R.id.question_puzzle_piece_main_layout);
        for (String choice : choices){
            Button choiceButton = (Button)getLayoutInflater().inflate(R.layout.inflatable_question_puzzle_piece_choice, choicesLayout, false);
            choiceButton.setText(choice);

            choiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    view.setEnabled(false);
                    final View finalView = view;
                    final Button answerButton =
                            (Button)getLayoutInflater().inflate(R.layout.inflatable_question_puzzle_piece_choice, answerLayout, false);
                    answerButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finalView.setEnabled(true);
                            answerLayout.removeView(view);
                        }
                    });
                    answerButton.setText(((Button)view).getText());

                    //when we add the view and try to get coordinates, it doesn't return the right value.
                    //so listen for when the button is about to be drawn and animate it then with
                    // the right coordinates.
                    //the tag is to make sure there is no infinite loop
                    // (animate -> onPreDraw -> animate -> onPreDraw...)
                    answerButton.setTag(true);
                    answerButton.getViewTreeObserver().addOnPreDrawListener(
                            new ViewTreeObserver.OnPreDrawListener() {
                                @Override
                                public boolean onPreDraw() {
                                    if (!(boolean)answerButton.getTag())
                                        return true;
                                    int[] from = new int[2];
                                    finalView.getLocationOnScreen(from);
                                    int[] to = new int[2];
                                    answerButton.getLocationOnScreen(to);

                                    TranslateAnimation animation = new TranslateAnimation(
                                            TranslateAnimation.ABSOLUTE, from[0]-to[0],
                                            TranslateAnimation.ABSOLUTE, 0,
                                            TranslateAnimation.ABSOLUTE, from[1]-to[1],
                                            TranslateAnimation.ABSOLUTE, 0);
                                    animation.setDuration(1000);
                                    animation.setInterpolator(new DecelerateInterpolator());
                                    //we want the moving text to show above other views,
                                    //but doing so shows the text over the main question view as well.
                                    //until I figure out how to fix that,
                                    // make it show just during the animation
                                    animation.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {
                                            grandparentLayout.setClipChildren(false);
                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            grandparentLayout.setClipChildren(true);
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {

                                        }
                                    });
                                    answerButton.startAnimation(animation);
                                    answerButton.setTag(false);
                                    return true;
                                }
                            }
                    );

                    answerLayout.addView(answerButton);
                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    },1000);
                }
            });

            choicesLayout.addView(choiceButton);
        }
    }

    private void setSubmitButtonListener(){
        Button button = (Button) findViewById(R.id.question_puzzle_piece_submit);
        button.setOnClickListener(getResponseListener());
    }


}
