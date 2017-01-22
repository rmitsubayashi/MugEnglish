package com.example.ryomi.myenglish.gui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;

public class Question_MultipleChoice extends AppCompatActivity {
    private QuestionManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question__multiple_choice);

        Intent intent = getIntent();

        populateQuestion();
    }

    private void populateQuestion(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        TextView tv = (TextView) findViewById(R.id.testQuestion);
        tv.setText(data.getAnswer());
    }
}
