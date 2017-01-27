package com.example.ryomi.myenglish.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;

//sets methods common for all question guis
public abstract class Question_General extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceID());
    }

    //to instantiate the activity
    protected abstract int getLayoutResourceID();
    //need this to record response
    protected abstract String getResponse(View clickedView);

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
                String answer = getResponse(view);
                if (checkAnswer(answer)){
                    QuestionManager.getInstance().recordResponse(answer, true);
                    QuestionManager.getInstance().nextQuestion();
                } else {
                    QuestionManager.getInstance().recordResponse(answer, false);
                    Toast.makeText(Question_General.this, answer, Toast.LENGTH_SHORT).show();
                }
            }
        };

        return responseListener;
    }

}
