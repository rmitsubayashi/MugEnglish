package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.TextToSpeechHelper;

//the question replaces the instructions with the question and just
// has an open text field for the user to follow the instructions
//(can't think of a better name)
public class Question_Instructions extends QuestionFragmentInterface {
    private EditText questionInput;
    private Button submitButton;
    private TextView instructions;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_question_instructions, container, false);
        parentViewGroupForFeedback = view.findViewById(R.id.fragment_question_instructions);
        siblingViewGroupForFeedback = view.findViewById(R.id.question_instructions_main_layout);

        submitButton = view.findViewById(R.id.question_instructions_submit);
        questionInput = view.findViewById(R.id.question_instructions_input);
        instructions = view.findViewById(R.id.question_instructions_instructions);

        keyboardFocusView = questionInput;

        createQuestionLayout();
        inflateFeedback(inflater);
        return view;
    }

    @Override
    protected String getResponse(View clickedView){
        return questionInput.getText().toString();
    }

    private void createQuestionLayout(){
        String instructionText = questionData.getQuestion();
        instructions.setText(
                TextToSpeechHelper.clickToSpeechTextViewSpannable(instructions, instructionText, new SpannableString(instructionText), textToSpeech)
        );

        //set submit
        submitButton.setOnClickListener(getResponseListener());
        //also for keyboard
        questionInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            submitButton.performClick();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void doSomethingOnFeedbackOpened(boolean correct, String response){
        TextToSpeechHelper.disableTextToSpeech(instructions);
    }
}
