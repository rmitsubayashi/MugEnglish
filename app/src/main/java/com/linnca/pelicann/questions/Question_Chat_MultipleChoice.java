package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linnca.pelicann.R;

import java.util.ArrayList;
import java.util.List;

public class Question_Chat_MultipleChoice extends Question_General {
    private TextView fromTextView;
    private LinearLayout chatItemsLayout;
    private LinearLayout choicesLayout;
    private EditText chatBoxEditText;
    private ImageButton submitButton;
    private List<TextView> chatItemTextViews;
    private boolean choicesLayoutHidden = true;
    private String lastSelectedChoiceTag = "";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_question_chat_multiple_choice, container, false);
        parentViewGroupForFeedback = view.findViewById(R.id.fragment_question_chat_multiple_choice);
        siblingViewGroupForFeedback = view.findViewById(R.id.question_chat_multiple_choice_main_layout);

        fromTextView = view.findViewById(R.id.question_chat_multiple_choice_from);
        chatItemsLayout = view.findViewById(R.id.question_chat_multiple_choice_chat_list);
        choicesLayout = view.findViewById(R.id.question_chat_multiple_choice_choice_layout);
        chatBoxEditText = view.findViewById(R.id.question_chat_multiple_choice_edit_text);
        submitButton = view.findViewById(R.id.question_chat_multiple_choice_submit);

        populateButtons(inflater);
        setActionListeners();
        inflateFeedback(inflater);
        setChatLayout(inflater);
        return view;
    }

    @Override
    protected String getResponse(View clickedView){
        return chatBoxEditText.getText().toString();
    }

    @Override
    protected int getMaxPossibleAttempts(){
        //it should be the number of choices - 1 (the last one is obvious)
        int choiceCt = questionData.getChoices().size();
        //if there's one choice, it's intentionally obvious
        return (choiceCt == 1 ? 1 : choiceCt - 1);
    }

    @Override
    protected void doSomethingAfterWrongAnswer(View clickedView){
        //this is called after the editText is cleared so we need some way
        //of saving the selected item
        Button buttonToDisable = choicesLayout.findViewWithTag(lastSelectedChoiceTag);
        if (buttonToDisable != null){
            buttonToDisable.setEnabled(false);
        }
        lastSelectedChoiceTag = "";
    }

    @Override
    protected void doSomethingAfterFeedbackOpened(){
        for (TextView toDisable : chatItemTextViews){
            QuestionUtils.disableTextToSpeech(toDisable);
        }
        View answerChatItemView = getLayoutInflater().inflate(R.layout.inflatable_question_chat_item_user, chatItemsLayout, false);
        TextView answerTextView = answerChatItemView.findViewById(R.id.question_chat_item_message);
        answerTextView.setText(questionData.getAnswer());
        chatItemsLayout.addView(answerChatItemView);
    }

    @Override
    protected void doSomethingAfterResponse(){
        chatBoxEditText.setText("");
    }

    private void populateButtons(LayoutInflater inflater){
        //dynamically add buttons because
        //we may have multiple choice questions with 3 or 4 questions
        List<String> choices = questionData.getChoices();
        QuestionUtils.shuffle(choices);

        for (String choice : choices){
            Button choiceButton = (Button)inflater.
                    inflate(R.layout.inflatable_question_multiple_choice_button, choicesLayout, false);
            choiceButton.setText(choice);
            choiceButton.setTag(choice);
            final String fChoice = choice;
            choiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chatBoxEditText.setText(fChoice);
                    lastSelectedChoiceTag = fChoice;
                    toggleChoicesLayout();
                }
            });
            if (QuestionUtils.isAlphanumeric(choice)) {
                choiceButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        QuestionUtils.startTextToSpeech(textToSpeech, fChoice);
                        return true;
                    }
                });
            }
            choicesLayout.addView(choiceButton);

        }
    }

    private void setActionListeners(){
        chatBoxEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleChoicesLayout();
            }
        });
        submitButton.setOnClickListener(getResponseListener());
    }

    private void setChatLayout(LayoutInflater inflater){
        final int chatItemDisplayDelay = 200;
        chatBoxEditText.setEnabled(false);
        String question = questionData.getQuestion();
        String from = QuestionUtils.getChatQuestionFrom(question);
        fromTextView.setText(from);
        List<ChatQuestionItem> chatItems = QuestionUtils.getChatQuestionChatItems(question);

        chatItemTextViews = new ArrayList<>(chatItems.size());
        int chatItemCt = chatItems.size();
        for (int i=0; i<chatItemCt; i++){
            final String text = chatItems.get(i).getText();
            boolean isUser = chatItems.get(i).isUser();
            if (text.equals(ChatQuestionItem.USER_INPUT)){
                chatItemsLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chatBoxEditText.setEnabled(true);
                        //so the first click will open the choices.
                        //if not focused, the first click will just focus
                        chatBoxEditText.requestFocus();
                    }
                }, i*chatItemDisplayDelay);

                break;
            }
            final View chatItemLayout;
            if (isUser){
                chatItemLayout =inflater.inflate(R.layout.inflatable_question_chat_item_user,
                        chatItemsLayout, false);
            } else {
                chatItemLayout = inflater.inflate(R.layout.inflatable_question_chat_item_other,
                        chatItemsLayout, false);
            }
            TextView messageTextView = chatItemLayout.findViewById(R.id.question_chat_item_message);
            messageTextView.setText(text);
            messageTextView.setText(
                    QuestionUtils.clickToSpeechTextViewSpannable(messageTextView, text, new SpannableString(text), textToSpeech)
            );
            chatItemTextViews.add(messageTextView);
            chatItemsLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    chatItemsLayout.addView(chatItemLayout);
                }
            }, i*chatItemDisplayDelay);
        }

    }

    private void toggleChoicesLayout(){
        if (choicesLayoutHidden){
            choicesLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            siblingViewGroupForFeedback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleChoicesLayout();
                }
            });
            //scrollview intercepts the click event so add a listener
            //to the child view as well
            chatItemsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleChoicesLayout();
                }
            });
            choicesLayoutHidden = false;
        } else {
            choicesLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            siblingViewGroupForFeedback.setOnClickListener(null);
            choicesLayoutHidden = true;
            chatItemsLayout.setOnClickListener(null);
        }
    }

}
