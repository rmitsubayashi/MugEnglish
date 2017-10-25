package com.linnca.pelicann.questions;


import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
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

    static void shuffleArray(char[] stringArray){
        Random random = new Random(System.currentTimeMillis());
        //Fisher-Yates
        int startIndex = stringArray.length-1;
        for (int i=startIndex; i>0; i--){
            int shuffleToIndex = random.nextInt(i+1);
            char temp = stringArray[i];
            stringArray[i] = stringArray[shuffleToIndex];
            stringArray[shuffleToIndex] = temp;
        }
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

    //higher the number, the more similar it will be to the original
    static List<String> createMisspelledWords(String word, int odds, int ct){
        List<String> results = new ArrayList<>(ct);
        for (int i=0; i<ct; i++) {
            String result = word;
            int tempOdds = odds;
            while ( (result.equals(word) || results.contains(result))
                    && tempOdds != 0) {
                result = createMisspelledWordHelper(word, odds);
                tempOdds--;
            }

            if (result.equals(word)) {
                char[] letters = word.toCharArray();
                shuffleArray(letters);
                result = String.valueOf(letters);
            }
            results.add(result);
        }

        return results;

    }
    private static String createMisspelledWordHelper(String word, int odds){
        //make sure the word somehow resembles the word
        Random random = new Random(System.currentTimeMillis());
        int nextRandom;

        //single consonant between vowels <-> double consonants
        //ie sine -> sinne, letter -> leter, bass -> bas
        int wordLetterCt = word.length();
        String tempWord = word;
        int changeCt = 0;
        for (int i=0; i<wordLetterCt; i++){
            char c = tempWord.charAt(i);
            //it can't be in between two letters
            if (i == 0 || i == wordLetterCt-1)
                continue;

            //not a character
            if (!Character.isLetter(c))
                continue;

            //no need to check
            if (isVowel(c)){
                continue;
            }

            if (tempWord.charAt(i+1) == c){
                nextRandom = random.nextInt(odds);
                if (nextRandom == 0){
                    String doubleConsonant = Character.toString(c) + Character.toString(c);
                    word = word.replaceFirst(doubleConsonant, Character.toString(c));
                    changeCt--;
                }
            }

            if (isVowel(tempWord.charAt(i-1)) && isVowel(tempWord.charAt(i+1))){
                nextRandom = random.nextInt(odds);
                if (nextRandom == 0){
                    String part1 = word.substring(0, i+changeCt);
                    String part2 = word.substring(i+changeCt, word.length());
                    word = part1 + Character.toString(c) + part2;
                    changeCt++;
                }
            }
        }

        //switch similar letters
        //i  <-> y
        //t  <-> th
        //o  <-> oh
        //ck <-> k
        //r  <-> l
        //a  <-> e
        //e  <-> ee
        //aw <-> ow
        //w  <-> wh
        //n  <-> m
        //c  <-> s
        //x   -> cks
        //u   -> a
        //b  <-> v
        //f  <-> ph
        //g  <-> j

        tempWord = word;
        StringBuilder builder = new StringBuilder(word);
        wordLetterCt = word.length();
        changeCt = 0;
        for (int i=0; i<wordLetterCt; i++){
            char letter = tempWord.charAt(i);
            if (letter == 't'){
                //make sure we don't go out of range
                if (i+1 != wordLetterCt){
                    char nextLetter = tempWord.charAt(i+1);
                    nextRandom = random.nextInt(odds);
                    if (nextLetter == 'h'){
                        //th -> t
                        if (nextRandom == 0){
                            builder.deleteCharAt(i+1 + changeCt);
                            changeCt--;
                        }
                    } else {
                        //t -> th
                        if (nextRandom == 0){
                            builder.insert(i+1+ changeCt, 'h');
                        }
                    }
                }
            }

            if (letter == 'y'){
                //if not the first letter (always consonant)
                if (i != 0){
                    nextRandom =random.nextInt(odds);
                    if (nextRandom == 0){
                        builder.replace(i+changeCt, i+changeCt+1, "i");
                    }
                }
            } else if (letter == 'i'){
                //if not the first letter (never consonant ie igloo, instant)
                if (i != 0){
                    nextRandom =random.nextInt(odds);
                    if (nextRandom == 0){
                        builder.replace(i+changeCt, i+changeCt+1, "y");
                    }
                }
            }

            if (letter == 'a'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "e");
                }
            } else if (letter == 'e'){
                //make sure we don't go out of range
                if (i+1 != wordLetterCt) {
                    char nextLetter = tempWord.charAt(i + 1);
                    nextRandom = random.nextInt(odds);
                    if (nextLetter == 'e'){
                        if (nextRandom == 0){
                            builder.insert(i+1 + changeCt, 'e');
                            changeCt++;
                        }
                    } else {
                        if (nextRandom == 0) {
                            builder.replace(i + changeCt, i + changeCt + 1, "a");
                        }
                    }
                }
            }

            //wh is most often at start of word
            if (letter == 'w' && i == 0){
                //make sure we don't go out of range
                if (i+1 != wordLetterCt){
                    char nextLetter = tempWord.charAt(i+1);
                    nextRandom = random.nextInt(odds);
                    if (nextLetter == 'h'){
                        //wh -> w
                        if (nextRandom == 0){
                            builder.deleteCharAt(i+1 + changeCt);
                            changeCt--;
                        }
                    } else {
                        //w -> wh
                        if (nextRandom == 0){
                            builder.insert(i+1 + changeCt, 'h');
                            changeCt++;
                        }
                    }
                }
            }

            if (letter == 'o'){
                //make sure we don't go out of range
                if (i+1 != wordLetterCt){
                    char nextLetter = tempWord.charAt(i+1);
                    nextRandom = random.nextInt(odds);
                    if (nextLetter == 'h'){
                        //oh -> o
                        if (nextRandom == 0){
                            builder.deleteCharAt(i+1 + changeCt);
                            changeCt--;
                        }
                    } else if (nextLetter == 'w'){
                        //ow -> aw
                        if (nextRandom == 0){
                            builder.replace(i+changeCt, i+changeCt+1, "a");
                        }

                    }else{
                        //o -> oh
                        if (nextRandom == 0){
                            builder.insert(i+1+ changeCt, 'h');
                            changeCt++;
                        }
                    }
                }
            }

            if (letter == 'l'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "r");
                }
            } else if (letter == 'r'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "l");
                }
            }

            if (letter == 'm'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "n");
                }
            } else if (letter == 'n'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "m");
                }
            }

            if (letter == 'v'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "b");
                }
            } else if (letter == 'b'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "v");
                }
            }

            if (letter == 'g'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "j");
                }
            } else if (letter == 'j'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "g");
                }
            }

            if (letter == 's'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "c");
                }
            } else if (letter == 'c'){
                //make sure we don't go out of range
                if (i+1 != wordLetterCt) {
                    char nextLetter = tempWord.charAt(i + 1);
                    nextRandom = random.nextInt(odds);
                    if (nextLetter == 'k') {
                        if (nextRandom == 0) {
                            builder.deleteCharAt(i + changeCt);
                            changeCt--;
                        }
                    } else {
                        if (nextRandom == 0){
                            builder.replace(i+changeCt, i+changeCt+1, "s");
                        }
                    }
                }
            }

            if (letter == 'x'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "cks");
                    changeCt += 2;
                }
            }

            if (letter == 'u'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "a");
                }
            }

            if (letter == 'f'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "ph");
                    changeCt++;
                }
            } else if (letter == 'p'){
                //make sure we don't go out of range
                if (i+1 != wordLetterCt) {
                    char nextLetter = tempWord.charAt(i + 1);
                    nextRandom = random.nextInt(odds);
                    if (nextLetter == 'h') {
                        if (nextRandom == 0) {
                            builder.replace(i+changeCt, i+changeCt+2, "f");
                            changeCt--;
                        }
                    }
                }
            }
        }

        word = builder.toString();

        return word;

    }

    private static boolean isVowel(char c){
        return c == 'a' || c == 'e' || c == 'i'
                || c == 'o' || c == 'u';
    }
}
