package com.linnca.pelicann.questions;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.ToolbarState;

public class VerbQuestionStart extends Fragment {
    public static final String TAG = "VerbQuestionStart";
    private Button startButton;
    private VerbQuestionStartListener listener;

    public interface VerbQuestionStartListener {
        void setToolbarState(ToolbarState state);
        void verbQuestionStartToQuestion();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_verb_question_start, container, false);
        startButton = view.findViewById(R.id.verb_question_start);
        addActionListeners();
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(
                new ToolbarState(getString(R.string.drawer_verb_questions), false)
        );
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

    private void implementListeners(Context context){
        try {
            listener = (VerbQuestionStartListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private void addActionListeners(){
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.verbQuestionStartToQuestion();
            }
        });
    }
}
