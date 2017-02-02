package com.example.ryomi.myenglish.gui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;

//sets methods common for all question guis
public abstract class Question_General extends AppCompatActivity {
    public static int UNLIMITED_ATTEMPTS = -1;
    protected int maxNumberOfAttempts;
    protected int attemptCt = 0;
    protected boolean disableChoiceAfterWrongAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceID());
        QuestionManager.getInstance().setCurrentContext(this);
        setMaxNumberOfAttempts();
        disableChoiceAfterWrongAnswer = disableChoiceAfterWrongAnswer();
    }

    //to instantiate the activity
    protected abstract int getLayoutResourceID();
    //need this to record response
    protected abstract String getResponse(View clickedView);
    //how many chances are possibly allowed for each question type
    protected abstract int getMaxPossibleAttempts();
    //whether to disable choice after answer when you have multiple attempts.
    //this will be user friendly?
    protected abstract boolean disableChoiceAfterWrongAnswer();

    private void setMaxNumberOfAttempts(){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //the preference is still stored as a string
        String preferencesMaxAttemptsString = sharedPreferences.getString
                (getString(R.string.preference_questions_numberOfAttemptsPerQuestion_key), "1");
        int preferencesMaxAttempts = Integer.parseInt(preferencesMaxAttemptsString);
        int maxPossibleAttempts = getMaxPossibleAttempts();
        if (maxPossibleAttempts == UNLIMITED_ATTEMPTS){
            //the question allow unlimited attempts
            //so restrict the user's attempts to the number set in the preferences
            maxNumberOfAttempts = preferencesMaxAttempts;
        } else {
            if (preferencesMaxAttempts <= maxPossibleAttempts){
                //the user has set a number of attempts less than the maximum possible attempts
                //so only allow the user to attempt the number of times he set in the preferences
                maxNumberOfAttempts = preferencesMaxAttempts;
            } else {
                //the max possible attempts is less than the number the user set in the preferences
                //so only allow the max possible attempts
                maxNumberOfAttempts = maxPossibleAttempts;
            }
        }
    }

    //might be different for different question types??
    protected boolean checkAnswer(String response){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        String answer = data.getAnswer();
        if(response.equals(answer))
            return true;
        else
            return false;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        //also reset manager
        QuestionManager.getInstance().resetManager();
    }

    protected View.OnClickListener getResponseListener(){
        View.OnClickListener responseListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCt++;
                String answer = getResponse(view);
                if (checkAnswer(answer)){
                    QuestionManager.getInstance().recordResponse(answer, true);
                    QuestionManager.getInstance().nextQuestion();
                } else {
                    QuestionManager.getInstance().recordResponse(answer, false);
                    if (attemptCt == maxNumberOfAttempts){
                        //the user used up all his attempts
                        QuestionManager.getInstance().nextQuestion();
                    } else {
                        Toast.makeText(Question_General.this, answer, Toast.LENGTH_SHORT).show();
                        if (disableChoiceAfterWrongAnswer)
                            view.setEnabled(false);
                    }
                }
            }
        };

        return responseListener;
    }



}
