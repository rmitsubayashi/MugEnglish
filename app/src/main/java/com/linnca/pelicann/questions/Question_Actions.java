package com.linnca.pelicann.questions;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.linnca.pelicann.R;

import java.util.List;

public class Question_Actions extends Question_General {
    public static final int QUESTION_TYPE = 12;
    public static String ANSWER_FINISHED = "@finished@";
    private Button startButton;
    private TextView actionView;
    private TextView timerView;
    private int secondsToComplete = 5;
    private int currentIteration = 0;
    private List<String> allActions;
    private Animation actionViewSlideOutAnimation;
    private Animation actionViewSlideInAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.questionType = QUESTION_TYPE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_question_actions, container, false);
        parentViewGroupForFeedback = view.findViewById(R.id.fragment_question_actions);
        siblingViewGroupForFeedback = view.findViewById(R.id.question_actions_main_layout);

        startButton = view.findViewById(R.id.question_actions_start);
        actionView = view.findViewById(R.id.question_actions_action);
        timerView = view.findViewById(R.id.question_actions_timer);

        allActions = questionData.getChoices();
        adjustTextSize();
        loadAnimations();
        setListeners();
        inflateFeedback(inflater);
        return view;
    }

    @Override
    protected String getResponse(View clickedView){
        return ANSWER_FINISHED;
    }

    private void adjustTextSize(){
        int maxLetterCt = 0;
        for (String action : allActions){
            int letterCt = action.length();
            if (letterCt > maxLetterCt)
                maxLetterCt = letterCt;
        }

        if (maxLetterCt > 15){
            actionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        }
    }

    private void loadAnimations(){
        actionViewSlideInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
        actionViewSlideOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left);
    }

    private void setListeners(){
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation fadeOutAnimation = new AlphaAnimation(1f, 0f);
                fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        startButton.clearAnimation();
                        startButton.setVisibility(View.GONE);
                        startAnimations();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                startButton.startAnimation(fadeOutAnimation);
            }
        });
    }

    private void startAnimations(){
        actionViewSlideInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                timerView.setText(Integer.toString(secondsToComplete));
                actionView.clearAnimation();
                actionViewSlideInAnimation.setAnimationListener(null);
                //let the user react to the change in UI
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        countDown(secondsToComplete);
                    }
                }, 1500);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        actionView.setText(allActions.get(currentIteration));
        actionView.startAnimation(actionViewSlideInAnimation);
    }

    //100 + 100 + 800 = 1 second
    private void countDown(final int currentNumber){
        ObjectAnimator animatorPt1 = ObjectAnimator.ofFloat(timerView, "rotationX", 0f, 90f);
        animatorPt1.setDuration(100);
        animatorPt1.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorPt1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator animatorPt2 = ObjectAnimator.ofFloat(timerView, "rotationX", 90f, 0f);
                animatorPt2.setDuration(100);
                animatorPt2.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorPt2.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        final int nextNumber;
                        if (currentNumber == 0){
                            currentIteration++;
                            if (currentIteration == allActions.size()){
                                finishAnimations();
                                return;
                            } else {
                                nextNumber = secondsToComplete;
                                switchActionView();
                            }
                        } else {
                            nextNumber = currentNumber - 1;
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                countDown(nextNumber);
                            }
                        }, 800);

                    }
                });
                int nextNumber = currentNumber == 0 ? secondsToComplete : currentNumber-1;
                timerView.setText(Integer.toString(nextNumber));
                animatorPt2.start();

            }
        });

        animatorPt1.start();
    }

    private void switchActionView(){
        actionViewSlideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                actionView.clearAnimation();
                actionView.setText(allActions.get(currentIteration));
                actionViewSlideInAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        actionView.clearAnimation();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                actionView.startAnimation(actionViewSlideInAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        actionView.startAnimation(actionViewSlideOutAnimation);
    }

    private void finishAnimations(){
        //not sure if works
        getResponseListener().onClick(getView());
    }
}