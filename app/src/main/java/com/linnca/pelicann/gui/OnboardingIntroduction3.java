package com.linnca.pelicann.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.linnca.pelicann.gui.widgets.OnboardingIntroductionBundle;
import com.linnca.pelicann.gui.widgets.OnboardingNextListener;
import com.linnca.pelicann.userinterestcontrols.UserInterestAdder;

public class OnboardingIntroduction3 extends Fragment {
    private Button greatButton;
    private ImageView personImage;
    private TextView introduction;
    private TextView explanation;
    private OnboardingNextListener nextListener;

    public OnboardingIntroduction3(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_onboarding_introduction3, container, false);
        greatButton = (Button)view.findViewById(R.id.onboarding_introduction3_great);

        Bundle bundle = getArguments();
        final OnboardingIntroductionBundle data = (OnboardingIntroductionBundle)bundle.getSerializable(null);
        if (data == null) {
            Log.d(getClass().toString(), "Bundle is null");
            return view;
        }

        greatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //save data in firebase first?
                UserInterestAdder userInterestAdder = new UserInterestAdder();
                userInterestAdder.findPronunciationAndCategoryThenAdd(data.getData());
                nextListener.nextScreen();
            }
        });
        personImage = (ImageView)view.findViewById(R.id.onboarding_introduction3_person);
        explanation = (TextView)view.findViewById(R.id.onboarding_introduction3_explanation);
        String explanationString;
        if (data.isMale()){
            personImage.setImageResource(R.drawable.boy);
            explanationString = getString(R.string.onboarding_introduction3_explanation, "男性");
            explanation.setText(explanationString);
        } else if (data.isFemale()){
            personImage.setImageResource(R.drawable.girl);
            explanationString = getString(R.string.onboarding_introduction3_explanation,"女性");
            explanation.setText(explanationString);
        } else {
            Log.d(getClass().toString(), "Could not read gender");
        }
        introduction = (TextView)view.findViewById(R.id.onboarding_introduction3_introduction);
        String introductionString = getString(R.string.onboarding_introduction3_introduction, data.getEnglishName());
        introduction.setText(introductionString);

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
        final TextView title = (TextView)getView().findViewById(R.id.onboarding_introduction3_title);
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

                personImage.setVisibility(View.VISIBLE);
                Animation personAnimation = AnimationUtils.loadAnimation(
                        OnboardingIntroduction3.this.getContext(), R.anim.slide_in_right);
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
                personImage.startAnimation(personAnimation);
            }
        }, 2000);
    }

    private void animate2(){
        if (getView() == null)
            return;
        introduction.setVisibility(View.VISIBLE);
        animate3();
    }

    private void animate3(){
        if (getView() == null)
            return;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                explanation.setVisibility(View.VISIBLE);
                greatButton.setVisibility(View.VISIBLE);
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
