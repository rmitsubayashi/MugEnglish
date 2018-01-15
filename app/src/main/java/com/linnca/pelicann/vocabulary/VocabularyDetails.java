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
import com.linnca.pelicann.db.AndroidNetworkConnectionChecker;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.mainactivity.GUIUtils;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.ToolbarState;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;
import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyListWord;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

public class VocabularyDetails extends Fragment{
    public static final String TAG = "VocabularyDetails";
    public static final String BUNDLE_VOCABULARY_WORD = "vocabularyWord";

    private VocabularyListWord word;
    private TextView wordTextView;
    private LinearLayout meaningsList;
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
        meaningsList = view.findViewById(R.id.vocabulary_details_meanings_list);
        wordTextView = view.findViewById(R.id.vocabulary_details_word);
        try {
            word = (VocabularyListWord) getArguments().getSerializable(BUNDLE_VOCABULARY_WORD);
        } catch (ClassCastException e){
            e.printStackTrace();
            word = new VocabularyListWord("", new ArrayList<String>(1), "");
        }
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
                //we might have the backup offline item, so clear that view
                meaningsList.removeAllViews();
                for (VocabularyWord word : wordCluster) {
                    setTitle(word);
                    setDetailItem(word);
                }
            }

            @Override
            public void onNoConnection(){
                //nothing has been loaded yet
                if (meaningsList.getChildCount() == 0){
                    setOfflineItems(word);
                }
            }
        };
        NetworkConnectionChecker networkConnectionChecker = new
                AndroidNetworkConnectionChecker(getContext());
        db.getVocabularyDetails(networkConnectionChecker, word.getKey(), onDBResultListener);
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

        View meaningView = getLayoutInflater().inflate(R.layout.inflatable_vocabulary_details_item, meaningsList, false);
        TextView meaningTextView = meaningView.findViewById(R.id.vocabulary_details_item_meaning);
        meaningTextView.setText(word.getMeaning());
        TextView exampleSentenceView = meaningView.findViewById(R.id.vocabulary_details_item_example_sentence);
        exampleSentenceView.setText(word.getExampleSentence());
        TextView exampleSentenceTranslationView = meaningView.findViewById(R.id.vocabulary_details_item_example_sentence_translation);
        exampleSentenceTranslationView.setText(word.getExampleSentenceTranslation());
        Button goToLessonButton = meaningView.findViewById(R.id.vocabulary_details_item_go_to_lesson_button);
        final String lessonKey = word.getLessonID();
        goToLessonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.vocabularyDetailsToLessonDetails(lessonKey);
            }
        });

        meaningsList.addView(meaningView);
    }
    
    private void setOfflineItems(VocabularyListWord word){
        wordTextView.setText(word.getWord());
        for (String meaning : word.getMeanings()) {
            View meaningView = getLayoutInflater().inflate(R.layout.inflatable_vocabulary_details_offline_item, meaningsList, false);
            TextView meaningTextView = meaningView.findViewById(R.id.vocabulary_details_offline_item_meaning);
            meaningTextView.setText(meaning);

            meaningsList.addView(meaningView);
        }
        //add a textView at the end to notify the user that he's offline
        TextView offlineTextView = new TextView(getContext());
        offlineTextView.setText(R.string.vocabulary_details_offline_notification);
        offlineTextView.setTextSize(GUIUtils.getDp(16, getContext()));
        meaningsList.addView(offlineTextView);
    }

    @Override
    public void onStop(){
        super.onStop();
        db.cleanup();
    }

}
