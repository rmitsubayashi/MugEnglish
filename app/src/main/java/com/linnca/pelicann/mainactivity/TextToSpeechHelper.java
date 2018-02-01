package com.linnca.pelicann.mainactivity;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import java.text.BreakIterator;
import java.util.Locale;

import pelicann.linnca.com.corefunctionality.lessonscript.StringUtils;

public class TextToSpeechHelper {
    public static Spannable clickToSpeechTextViewSpannable(final TextView textView, String text, Spannable span, final TextToSpeech tts){
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            final String possibleWord = text.substring(start, end);
            //if it's English and doesn't contain a blank
            if (StringUtils.isAlphanumeric(possibleWord) && !possibleWord.contains("__")) {
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

    public static void startTextToSpeech(TextToSpeech tts, String str){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(str, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(str, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public static void disableTextToSpeech(TextView textView){
        //make sure the clickable spans are not triggered.
        //note this also prevents long clicks
        textView.setMovementMethod(null);
    }
}
