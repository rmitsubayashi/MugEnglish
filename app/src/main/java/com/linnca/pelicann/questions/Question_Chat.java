package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linnca.pelicann.R;

import java.util.ArrayList;
import java.util.List;

public class Question_Chat extends Question_General {
    public static final int QUESTION_TYPE = 10;
    private TextView fromTextView;
    private LinearLayout chatItemsLayout;
    private EditText chatBoxEditText;
    private ImageButton submitButton;
    private List<TextView> chatItemTextViews;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_question_chat, container, false);
        parentViewGroupForFeedback = view.findViewById(R.id.fragment_question_chat);
        siblingViewGroupForFeedback = view.findViewById(R.id.question_chat_main_layout);

        fromTextView = view.findViewById(R.id.question_chat_from);
        chatItemsLayout = view.findViewById(R.id.question_chat_chat_list);
        chatBoxEditText = view.findViewById(R.id.question_chat_edit_text);
        submitButton = view.findViewById(R.id.question_chat_submit);

        keyboardFocusView = chatBoxEditText;
        
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
    protected void doSomethingOnFeedbackOpened(boolean correct, String response){
        //disable all tts listeners for the chat items
        for (TextView toDisable : chatItemTextViews){
            QuestionUtils.disableTextToSpeech(toDisable);
        }
        //add the answer chat item (to make it look like you responded)
        View answerChatItemView = getLayoutInflater().inflate(R.layout.inflatable_question_chat_item_user, chatItemsLayout, false);
        TextView answerTextView = answerChatItemView.findViewById(R.id.question_chat_item_message);
        if (correct){
            //the user might have typed in an alternate correct answer
            answerTextView.setText(response);
        } else {
            //show the correct answer
            answerTextView.setText(questionData.getAnswer());
        }
        chatItemsLayout.addView(answerChatItemView);
    }

    @Override
    protected void doSomethingAfterResponse(){
        chatBoxEditText.setText("");
    }

    private void setActionListeners(){
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

}
