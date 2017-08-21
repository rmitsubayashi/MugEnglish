package com.linnca.pelicann.gui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.linnca.pelicann.R;
import com.linnca.pelicann.gui.widgets.OnboardingNextListener;

public class OnboardingIntroduction4 extends Fragment {
    private OnboardingNextListener nextListener;
    public OnboardingIntroduction4(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_onboarding_introduction4, container, false);
        Button start = (Button)view.findViewById(R.id.onboarding_introduction4_start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for now, this will go to theme list
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

    //implements the listeners for some of the preferences
    private void implementListeners(Context context) {
        try {
            nextListener = (OnboardingNextListener)context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString() + " must implement nextListener");
        }
    }
}
