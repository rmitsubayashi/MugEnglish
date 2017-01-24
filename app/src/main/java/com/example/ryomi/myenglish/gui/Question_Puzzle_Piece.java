package com.example.ryomi.myenglish.gui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;

import org.apmem.tools.layouts.FlowLayout;
import org.w3c.dom.Text;

import java.util.List;


public class Question_Puzzle_Piece extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question__puzzle__piece);
        populateQuestion();
        createChoiceButtons();
        setSubmitButtonListener();
    }

    private void populateQuestion(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        TextView questionTextView = (TextView) findViewById(R.id.question_puzzle_piece_question);
        questionTextView.setText(data.getQuestion());
    }

    private void createChoiceButtons(){
        QuestionManager manager = QuestionManager.getInstance();
        QuestionData data = manager.getQuestionData();
        List<String> choices = data.getChoices();
        QuestionUtils.shuffle(choices);


        //creating a new button every time looks expensive?
        final View.OnClickListener moveToAnswer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlowLayout answerLayout =
                        (FlowLayout) Question_Puzzle_Piece.this.findViewById(R.id.question_puzzle_piece_answer);
                FlowLayout choicesLayout = (FlowLayout) findViewById(R.id.question_puzzle_piece_choiceRow);
                view.setEnabled(false);
                final View finalView = view;
                Button answerButton =
                        (Button)getLayoutInflater().inflate(R.layout.inflatable_puzzle_piece_choice, null);
                answerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finalView.setEnabled(true);
                        FlowLayout answerLayout = (FlowLayout) findViewById(R.id.question_puzzle_piece_answer);
                        answerLayout.removeView(view);
                    }
                });
                answerButton.setText(((Button)view).getText());
                answerLayout.addView(answerButton);
            }
        };

        FlowLayout choicesLayout = (FlowLayout) findViewById(R.id.question_puzzle_piece_choiceRow);
        for (String choice : choices){
            Button button = (Button)getLayoutInflater().inflate(R.layout.inflatable_puzzle_piece_choice, null);
            button.setText(choice);

            button.setOnClickListener(moveToAnswer);

            choicesLayout.addView(button);
        }
    }

    private void setSubmitButtonListener(){
        Button button = (Button) findViewById(R.id.question_puzzle_piece_submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlowLayout answerLayout = (FlowLayout) findViewById(R.id.question_puzzle_piece_answer);
                int childCt = answerLayout.getChildCount();
                String answer = "";
                for (int i=0; i<childCt; i++){
                    Button choiceButton = (Button)answerLayout.getChildAt(i);
                    String choice = choiceButton.getText().toString();
                    answer += choice + "|";
                }
                //just in case nothing is selected
                if (answer.length() > 0){
                    answer = answer.substring(0,answer.length()-1);
                }

                QuestionManager manager = QuestionManager.getInstance();
                QuestionData data = manager.getQuestionData();
                String correctAnswer = data.getAnswer();
                if (answer.equals(correctAnswer)){
                    manager.nextQuestion();
                } else {
                    Toast.makeText(Question_Puzzle_Piece.this, correctAnswer ,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
