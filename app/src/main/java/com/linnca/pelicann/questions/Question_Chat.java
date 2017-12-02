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

public class Question_Chat extends QuestionFragmentInterface {
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
            TextToSpeechHelper.disableTextToSpeech(toDisable);
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

    //util methods also used in multiple choice version
    public static String formatQuestion(String from, List<ChatQuestionItem> chatItems){
        StringBuilder question = new StringBuilder(from + "::");
        for (ChatQuestionItem item : chatItems){
            if (item.isUser()){
                question.append("(u)");
            } else {
                question.append("(o)");
            }
            question.append(item.getText());
        }

        return question.toString();
    }

    public static List<ChatQuestionItem> getChatItemsFromString(String questionString){
        List<ChatQuestionItem> chatItems = new ArrayList<>();
        String[] split = questionString.split("::");
        //shouldn't happen, but just in case
        if (split.length < 2){
            return chatItems;
        }
        String chatItemsString = split[1];
        //we are assuming (u) or (o) is the first character
        int stringMkr = 3;
        boolean isUser = chatItemsString.substring(0, 3).equals("(u)");
        while (true) {
            //indexOf() returns first occurrence
            int userChatItemIndex = chatItemsString.indexOf("(u)",stringMkr);
            int otherChatItemIndex = chatItemsString.indexOf("(o)", stringMkr);
            if (userChatItemIndex == -1 && otherChatItemIndex == -1){
                String finalText = chatItemsString.substring(stringMkr, chatItemsString.length());
                ChatQuestionItem finalItem = new ChatQuestionItem(isUser, finalText);
                chatItems.add(finalItem);
                break;
            }
            int nextChatItemIndex;
            boolean tmpIsUser = isUser;
            if (userChatItemIndex == -1){
                nextChatItemIndex = otherChatItemIndex;
                isUser = false;
            } else if (otherChatItemIndex == -1){
                nextChatItemIndex = userChatItemIndex;
                isUser = true;
            } else {
                isUser = userChatItemIndex < otherChatItemIndex;
                nextChatItemIndex = isUser ?
                        userChatItemIndex : otherChatItemIndex;
            }
            String text = chatItemsString.substring(stringMkr, nextChatItemIndex);
            ChatQuestionItem item = new ChatQuestionItem(tmpIsUser, text);
            chatItems.add(item);
            //we want to start from after the ')'
            stringMkr = nextChatItemIndex + 3;
        }

        return chatItems;
    }

    //gets the person who sent the chat message
    public static String getPersonFromString(String questionString){
        int breakIndex = questionString.indexOf("::");
        return questionString.substring(0, breakIndex);
    }

    private void setActionListeners(){
        submitButton.setOnClickListener(getResponseListener());
    }

    private void setChatLayout(LayoutInflater inflater){
        final int chatItemDisplayDelay = 200;
        chatBoxEditText.setEnabled(false);
        String question = questionData.getQuestion();
        String from = getPersonFromString(question);
        fromTextView.setText(from);
        List<ChatQuestionItem> chatItems = getChatItemsFromString(question);

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
                    TextToSpeechHelper.clickToSpeechTextViewSpannable(messageTextView, text, new SpannableString(text), textToSpeech)
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
