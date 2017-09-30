package com.linnca.pelicann.questions;


import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class QuestionUtils {

    private static List<Character> characterList;

    static void shuffle(List<String> choices){
        Collections.shuffle(choices, new Random(System.currentTimeMillis()));
    }

    public static String formatPuzzlePieceAnswer(List<String> choices){
        String answer = "";
        for (String choice : choices){
            answer += choice + "|";
        }

        answer = answer.substring(0, answer.length()-1);

        return answer;
    }

    public static String formatChatQuestion(String from, List<ChatQuestionItem> chatItems){
        String question = from + "::";
        for (ChatQuestionItem item : chatItems){
            if (item.isUser()){
                question += "(u)" + item.getText();
            } else {
                question += "(o)" + item.getText();
            }
        }

        return question;
    }

    public static String getChatQuestionFrom(String questionString){
        int breakIndex = questionString.indexOf("::");
        return questionString.substring(0, breakIndex);
    }

    public static List<ChatQuestionItem> getChatQuestionChatItems(String questionString){
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

    static void populateRandomCharacterBasedOnProbability(){
        characterList = new ArrayList<>();
        //probability based on
        //https://en.wikipedia.org/wiki/Letter_frequency#Relative_frequencies_of_letters_in_the_English_language
        characterList.addAll(Collections.nCopies(82, 'a'));
        characterList.addAll(Collections.nCopies(15, 'b'));
        characterList.addAll(Collections.nCopies(28, 'c'));
        characterList.addAll(Collections.nCopies(43, 'd'));
        characterList.addAll(Collections.nCopies(127, 'e'));
        characterList.addAll(Collections.nCopies(22, 'f'));
        characterList.addAll(Collections.nCopies(20, 'g'));
        characterList.addAll(Collections.nCopies(61, 'h'));
        characterList.addAll(Collections.nCopies(70, 'i'));
        characterList.addAll(Collections.nCopies(15, 'j'));
        characterList.addAll(Collections.nCopies(77, 'k'));
        characterList.addAll(Collections.nCopies(40, 'l'));
        characterList.addAll(Collections.nCopies(24, 'm'));
        characterList.addAll(Collections.nCopies(47, 'n'));
        characterList.addAll(Collections.nCopies(75, 'o'));
        characterList.addAll(Collections.nCopies(19, 'p'));
        characterList.addAll(Collections.nCopies(1, 'q'));
        characterList.addAll(Collections.nCopies(60, 'r'));
        characterList.addAll(Collections.nCopies(63, 's'));
        characterList.addAll(Collections.nCopies(91, 't'));
        characterList.addAll(Collections.nCopies(28, 'u'));
        characterList.addAll(Collections.nCopies(10, 'v'));
        characterList.addAll(Collections.nCopies(24, 'w'));
        characterList.addAll(Collections.nCopies(2, 'x'));
        characterList.addAll(Collections.nCopies(20, 'y'));
        characterList.addAll(Collections.nCopies(1, 'z'));

    }

    static char getRandomCharacterBasedOnProbability(){
        if (characterList == null){
            populateRandomCharacterBasedOnProbability();
        }
        Random random = new Random();
        int index = random.nextInt(characterList.size());
        return characterList.get(index);
    }

    static void clearRandomCharacters(){
        if (characterList == null){
            return;
        }
        characterList.clear();
        characterList = null;
    }

    static Spannable clickToSpeechTextViewSpannable(final TextView textView, String text, Spannable span, final TextToSpeech tts){
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            final String possibleWord = text.substring(start, end);
            //if it's English and doesn't contain a blank
            if (isAlphanumeric(possibleWord) && !possibleWord.contains("__")) {
                ClickableSpan clickSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        startTextToSpeech(tts, possibleWord);
                    }
                    //prevent the text from looking like a link
                    @Override
                    public void updateDrawState(final TextPaint tp){

                    }
                };
                span.setSpan(clickSpan, start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return span;
    }

    static void startTextToSpeech(TextToSpeech tts, String str){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(str, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(str, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    static void disableTextToSpeech(TextView textView){
        //make sure the clickable spans are not triggered.
        //note this also prevents long clicks
        textView.setMovementMethod(null);
    }

    static boolean isAlphanumeric(String str){
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isDigit(c) && !Character.isLetter(c) && !Character.isSpaceChar(c))
                return false;
        }
        return true;
    }

    static String createBlank(String answer){
        //just slightly bigger than the answer
        int length = answer.length() + 1;
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<length; i++){
            builder.append("_");
        }
        return builder.toString();
    }
}
