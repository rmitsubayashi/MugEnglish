package com.linnca.pelicann.vocabulary;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnDBResultListener;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.ToolbarState;

import java.util.List;

public class VocabularyDetails extends Fragment{
    public static final String TAG = "VocabularyDetails";
    public static final String BUNDLE_VOCABULARY_ID = "vocabularyID";

    private String key;
    private TextView wordTextView;
    private LinearLayout mainLayout;
    VocabularyDetailsListener listener;

    private Database db;

    public interface VocabularyDetailsListener {
        void setToolbarState(ToolbarState state);
        void vocabularyDetailsToLessonDetails(String lessonKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
            db = (Database) getArguments().getSerializable(MainActivity.BUNDLE_DATABASE);
        } catch (Exception e){
            e.printStackTrace();
            //hard code a new database instance
            db = new FirebaseDB();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_vocabulary_details, container, false);
        mainLayout = view.findViewById(R.id.vocabulary_details_main_layout);
        wordTextView = view.findViewById(R.id.vocabulary_details_word);
        key = getArguments().getString(BUNDLE_VOCABULARY_ID);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        implementListeners(context);
    }

    //must implement to account for lower APIs
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        implementListeners(activity);
    }

    private void implementListeners(Context context){
        try {
            listener = (VocabularyDetailsListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(new ToolbarState(getString(R.string.fragment_vocabulary_details_title),
                false, false, null));
        populateDetails();
    }

    private void populateDetails(){
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onVocabularyWordQueried(List<VocabularyWord> wordCluster) {
                for (VocabularyWord word : wordCluster) {
                    setTitle(word);
                    setDetailItem(word);
                }
            }
        };
        db.getVocabularyDetails(key, onDBResultListener);
    }

    private void setTitle(VocabularyWord word){
        if (wordTextView.getText().equals("")){
            wordTextView.setText(word.getWord());
        }
    }

    //there will only be 2~3? items per vocabulary word,
    //so it doesn't make too much sense to use a listView?
    private void setDetailItem(VocabularyWord word){
        if (word == null)
            return;

        View wordView = getLayoutInflater().inflate(R.layout.inflatable_vocabulary_details_item, mainLayout, false);
        TextView meaningView = wordView.findViewById(R.id.vocabulary_details_item_meaning);
        meaningView.setText(word.getMeaning());
        TextView exampleSentenceView = wordView.findViewById(R.id.vocabulary_details_item_example_sentence);
        exampleSentenceView.setText(word.getExampleSentence());
        TextView exampleSentenceTranslationView = wordView.findViewById(R.id.vocabulary_details_item_example_sentence_translation);
        exampleSentenceTranslationView.setText(word.getExampleSentenceTranslation());
        Button goToLessonButton = wordView.findViewById(R.id.vocabulary_details_item_go_to_lesson_button);
        final String lessonKey = word.getLessonID();
        goToLessonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.vocabularyDetailsToLessonDetails(lessonKey);
            }
        });

        mainLayout.addView(wordView);
    }

    @Override
    public void onStop(){
        super.onStop();
        db.cleanup();
    }

}
