package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.linnca.pelicann.R;

import org.apmem.tools.layouts.FlowLayout;

import java.util.Collections;
import java.util.List;


public class Question_SentencePuzzle extends QuestionFragmentInterface {
    public static final int QUESTION_TYPE = 3;
    private FlowLayout answerLayout;
    private FlowLayout choicesLayout;
    private TextView questionTextView;
    private Button submitButton;
    private ScrollView scrollView;
    private ViewGroup grandparentLayout;
    private int buttonLayout;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //determine whether to use small or big buttons
        List<String> choices = questionData.getChoices();
        int maxChoiceLength = -1;
        int choicesLength = 0;
        for (String choice : choices){
            int choiceLength = choice.length();
            choicesLength += choiceLength;
            if (choiceLength > maxChoiceLength){
                maxChoiceLength = choiceLength;
            }
        }

        //arbitrary values
        if (choicesLength > 60){
            buttonLayout = R.layout.inflatable_question_sentence_puzzle_puzzle_piece_small;
        } else if (choicesLength > 40){
            //if there's a large piece, more likely to create unneeded rows
            if (maxChoiceLength > 15){
                buttonLayout = R.layout.inflatable_question_sentence_puzzle_puzzle_piece_small;
            } else {
                buttonLayout = R.layout.inflatable_question_sentence_puzzle_puzzle_piece;
            }
        } else {
            buttonLayout = R.layout.inflatable_question_sentence_puzzle_puzzle_piece;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_question_sentence_puzzle, container, false);
        parentViewGroupForFeedback = view.findViewById(R.id.fragment_question_sentence_puzzle);
        siblingViewGroupForFeedback = view.findViewById(R.id.question_sentence_puzzle_main_layout);

        answerLayout = view.findViewById(R.id.question_sentence_puzzle_answer);
        choicesLayout = view.findViewById(R.id.question_sentence_puzzle_choice_row);
        questionTextView = view.findViewById(R.id.question_sentence_puzzle_question);
        submitButton = view.findViewById(R.id.question_sentence_puzzle_submit);
        scrollView = view.findViewById(R.id.question_sentence_puzzle_answer_scroll);
        //same view
        grandparentLayout = siblingViewGroupForFeedback;

        populateQuestion();
        createChoiceButtons(inflater);
        setSubmitButtonListener();
        inflateFeedback(inflater);

        return view;
    }

    //for the puzzles the clicked view is a submit button
    //so it's not relevant
    @Override
    protected String getResponse(View clickedView){
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

    public static String formatAnswer(List<String> choices){
        StringBuilder answer = new StringBuilder("");
        for (String choice : choices){
            answer.append(choice);
            answer.append("|");
        }

        return answer.substring(0, answer.length()-1);
    }

    //no need to enable text to speech because this will always be Japanese??
    private void populateQuestion(){
        questionTextView.setText(questionData.getQuestion());
    }


    private void createChoiceButtons(final LayoutInflater inflater){
        List<String> choices = questionData.getChoices();
        Collections.shuffle(choices);

        //creating a new button every time is expensive?
        for (final String choice : choices){
            Button choiceButton = (Button)inflater.inflate(buttonLayout, choicesLayout, false);
            choiceButton.setText(choice);

            choiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    view.setEnabled(false);
                    final View finalView = view;
                    final Button answerButton =
                            (Button)inflater.inflate(buttonLayout, answerLayout, false);
                    answerButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finalView.setEnabled(true);
                            answerLayout.removeView(view);
                        }
                    });
                    final String answerText = ((Button)view).getText().toString();
                    answerButton.setText(answerText);

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

                    answerButton.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            TextToSpeechHelper.startTextToSpeech(textToSpeech, answerText);
                            return true;
                        }
                    });
                    answerLayout.addView(answerButton);
                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    },1000);
                }
            });

            choiceButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    TextToSpeechHelper.startTextToSpeech(textToSpeech, choice);
                    return true;
                }
            });

            choicesLayout.addView(choiceButton);
        }
    }

    private void setSubmitButtonListener(){
        submitButton.setOnClickListener(getResponseListener());
    }


}
