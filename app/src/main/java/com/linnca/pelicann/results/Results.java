package com.linnca.pelicann.results;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseAnalyticsHeaders;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionAttempt;

import java.util.List;

//after we are finished with the questions,
//we redirect to this fragment
// and save everything in the database

public class Results extends Fragment {
    private final String TAG = "Results";
    private FirebaseAnalytics firebaseLog;
    public static final String BUNDLE_INSTANCE_RECORD = "bundleInstanceRecord";
    private InstanceRecord instanceRecord;
    private ResultsManager resultsManager;
    private TextView correctCtTextView;
    private LinearLayout newStarsLayout;
    private Button finishButton;
    private Button reviewButton;

    private ResultsListener resultsListener;

    public interface ResultsListener {
        void resultsToLessonCategories();
        void resultsToReview(InstanceRecord instanceRecord);
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instanceRecord = (InstanceRecord) getArguments().getSerializable(BUNDLE_INSTANCE_RECORD);
        resultsManager = new ResultsManager(instanceRecord, new ResultsManager.ResultsManagerListener() {

        });
        //this won't affect when we check for new achievements
        resultsManager.saveInstanceRecord();
        firebaseLog = FirebaseAnalytics.getInstance(getActivity());
        firebaseLog.setCurrentScreen(getActivity(), TAG, TAG);
        firebaseLog.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_results, container, false);
        newStarsLayout = view.findViewById(R.id.results_new_star_layout);
        correctCtTextView = view.findViewById(R.id.results_questions_correct);
        reviewButton = view.findViewById(R.id.results_review);
        finishButton = view.findViewById(R.id.results_finish);
        setLayout();
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        resultsListener.setToolbarState(
                new ToolbarState(getString(R.string.results_app_bar_title), false, instanceRecord.getLessonId())
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
            resultsListener = (ResultsListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private void setLayout(){
        populateCorrectCount();

        boolean needToReview = false;
        for (QuestionAttempt attempt : instanceRecord.getAttempts()){
            if (!attempt.getCorrect()){
                needToReview = true;
                break;
            }
        }
        if (needToReview){
            reviewButton.setVisibility(View.VISIBLE);
            reviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalyticsHeaders.PARAMS_ACTION_TYPE, "Review");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, instanceRecord.getId());
                    firebaseLog.logEvent(FirebaseAnalyticsHeaders.EVENT_ACTION, bundle);
                    resultsListener.resultsToReview(instanceRecord);
                }
            });
            //change the layout of the finish button to recommend review
            // (make it borderless)
            finishButton.setBackgroundResource(R.drawable.transparent_button);
            finishButton.setTextColor(ContextCompat.getColor(getContext(), R.color.lblue500));
            finishButton.setText(R.string.results_finish_review);
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalyticsHeaders.PARAMS_ACTION_TYPE, "Finish Instead of Review");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, instanceRecord.getId());
                    firebaseLog.logEvent(FirebaseAnalyticsHeaders.EVENT_ACTION, bundle);
                    resultsListener.resultsToLessonCategories();
                }
            });
        } else {
            //we don't log anything
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resultsListener.resultsToLessonCategories();
                }
            });
        }
    }

    private void populateCorrectCount(){
        String tempQuestionID = "";
        int correctCt = 0;
        int totalCt = 0;
        List<QuestionAttempt> attempts = instanceRecord.getAttempts();
        for (QuestionAttempt attempt : attempts){
            String questionID = attempt.getQuestionID();
            //there can only be one correct answer per question.
            // (there can be multiple incorrect answers)
            if (attempt.getCorrect())
                correctCt++;
            if (!tempQuestionID.equals(questionID)){
                totalCt++;
                tempQuestionID = questionID;
            }
        }
        String displayText = Integer.toString(correctCt) + " / " + Integer.toString(totalCt);
        correctCtTextView.setText(displayText);
        //change text color based on accuracy (the user can edit border line??)
        double correctPercentage = (double)correctCt / (double)totalCt;
        if (correctPercentage > 0.7){
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.lgreen500));
        } else if (correctPercentage > 0.5){
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.orange500));
        } else {
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red500));
        }
    }
}
