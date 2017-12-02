package com.linnca.pelicann.results;

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

public class ReviewResults extends Fragment {
    public static final String TAG = "ReviewResults";
    private Button finishButton;
    private ReviewResultsListener listener;

    public interface ReviewResultsListener {
        void reviewResultsToLessonList();
        void setToolbarState(ToolbarState state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_results, container, false);
        finishButton = view.findViewById(R.id.review_results_finish);
        addListeners();
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(
                new ToolbarState(getString(R.string.results_app_bar_title), false, false, null)
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
            listener = (ReviewResultsListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private void addListeners(){
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.reviewResultsToLessonList();
            }
        });
    }
}
