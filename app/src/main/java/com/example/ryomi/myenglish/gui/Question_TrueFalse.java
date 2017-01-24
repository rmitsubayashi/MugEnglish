package com.example.ryomi.myenglish.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;

public class Question_TrueFalse extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_true_false);
        populateQuestion();
        setButtonActionListeners();
    }

    private void populateQuestion(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        TextView questionTextView = (TextView) findViewById(R.id.question_true_false_question);
        questionTextView.setText(data.getQuestion());
    }

    private void setButtonActionListeners(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        final String answer = data.getAnswer();

        Button trueButton = (Button) findViewById(R.id.question_true_false_true);
        Button falseButton = (Button) findViewById(R.id.question_true_false_false);

        View.OnClickListener correctListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionManager.getInstance().nextQuestion();
            }
        };

        View.OnClickListener incorrectListener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(Question_TrueFalse.this, ((Button)view).getText().toString(),Toast.LENGTH_SHORT).show();
            }
        };

        if (answer.equals(QuestionUtils.TRUE_FALSE_QUESTION_TRUE)) {
            trueButton.setOnClickListener(correctListener);
            falseButton.setOnClickListener(incorrectListener);
        } else {
            trueButton.setOnClickListener(incorrectListener);
            falseButton.setOnClickListener(correctListener);
        }
    }
}
