package com.linnca.pelicann.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.gui.widgets.OnboardingNextListener;

public class OnboardingIntroduction1 extends Fragment {
    private Button whoAreYou;
    private OnboardingNextListener nextListener;
    public OnboardingIntroduction1(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_onboarding_introduction1, container, false);
        whoAreYou = (Button)view.findViewById(R.id.onboarding_introduction1_who_are_you);
        whoAreYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextListener.nextScreen();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        implementListeners(context);
    }

    //must implement to account for lower APIs
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        implementListeners(activity);
    }

    @Override
    public void onStart(){
        super.onStart();
        //starts a series of animations
        animate1();
    }

    //we want to wait a few seconds for the user to see the welcome message
    private void animate1(){
        if (getView() == null)
            return;
        final TextView title = (TextView)getView().findViewById(R.id.onboarding_introduction1_title);
        final ImageView person = (ImageView)getView().findViewById(R.id.onboarding_introduction1_person);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation titleAnimation = new AlphaAnimation(1f, 0f);
                titleAnimation.setDuration(500);
                titleAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        title.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                person.setVisibility(View.VISIBLE);
                Animation personAnimation = AnimationUtils.loadAnimation(
                        OnboardingIntroduction1.this.getContext(), R.anim.slide_in_right);
                personAnimation.setDuration(1000);
                personAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        animate2();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                title.startAnimation(titleAnimation);
                person.startAnimation(personAnimation);
            }
        }, 2000);
    }

    private void animate2(){
        if (getView() == null)
            return;
        final TextView introduction = (TextView)getView().findViewById(R.id.onboarding_introduction1_introduction);
        introduction.setVisibility(View.VISIBLE);
        animate3();
    }

    private void animate3(){
        if (getView() == null)
            return;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                whoAreYou.setVisibility(View.VISIBLE);
            }
        }, 1000);
    }

    //implements the listeners for some of the preferences
    private void implementListeners(Context context) {
        try {
            nextListener = (OnboardingNextListener)context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString() + " must implement nextListener");
        }
    }


}
