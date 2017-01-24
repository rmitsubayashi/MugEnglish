package com.example.ryomi.myenglish.gui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;

import java.util.List;

public class Question_MultipleChoice extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question__multiple_choice);
        populateQuestion();
        populateButtons();
    }

    private void populateQuestion(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        String question = data.getQuestion();

        TextView questionTextView = (TextView) findViewById(R.id.question_multiple_choice_question);
        questionTextView.setText(question);
    }

    private void populateButtons(){
        //dynamically add buttons because
        //we may have multiple choice questions with 3 or 4 questions
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        List<String> choices = data.getChoices();
        QuestionUtils.shuffle(choices);
        String answer = data.getAnswer();

        LinearLayout choicesLayout = (LinearLayout) findViewById(R.id.question_multiple_choice_choices_layout);

        View.OnClickListener correctListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionManager.getInstance().nextQuestion();
            }
        };

        View.OnClickListener incorrectListener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(Question_MultipleChoice.this, ((Button)view).getText().toString(),Toast.LENGTH_SHORT).show();
            }
        };

        for (String choice : choices){
            Button choiceButton =
                    (Button)getLayoutInflater().inflate(R.layout.inflatable_question_multiple_choice_button, null);
            choiceButton.setText(choice);

            if (choice.equals(answer)){
                choiceButton.setOnClickListener(correctListener);
            } else {
                choiceButton.setOnClickListener(incorrectListener);
            }

            choiceButton.setBackgroundColor(ContextCompat.getColor(this,R.color.lblue500));

            choicesLayout.addView(choiceButton);

        }

    }
}
