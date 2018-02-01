package com.linnca.pelicann.lessondetails;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.AndroidLocalStorageManager;
import com.linnca.pelicann.db.AndroidNetworkConnectionChecker;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.ToolbarState;

import java.util.List;
import java.util.Locale;

import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;
import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessonlist.LessonCategory;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonscript.LessonScriptManager;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;

public class LessonScript extends Fragment {
    public static final String TAG = "LessonScript";
    public static final String BUNDLE_LESSON_CATEGORY = "bundleLessonCategory";
    private LessonScriptManager manager;
    private LessonScriptListener listener;
    private RecyclerView list;
    private ProgressBar loading;
    private TextToSpeech textToSpeech;
    private LessonScriptAdapter adapter;

    public interface LessonScriptListener {
        void setToolbarState(ToolbarState state);
        void lessonScriptToQuestion(LessonInstanceData lessonInstanceData, List<QuestionData>questions);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Database db = (Database)getArguments().getSerializable(MainActivity.BUNDLE_DATABASE);
        NetworkConnectionChecker networkConnectionChecker = new AndroidNetworkConnectionChecker(getContext());
        AndroidLocalStorageManager localStorageManager = new AndroidLocalStorageManager(getContext());
        LessonScriptManager.LessonScriptManagerListener managerListener = getLessonScriptManagerListener();
        manager = new LessonScriptManager(localStorageManager, db, networkConnectionChecker, managerListener);

        try {
            textToSpeech = ((MainActivity) getActivity()).getTextToSpeech();
        } catch (Exception e){
            //if we can't cast the class to MainActivity
            // (or if anything else goes wrong),
            //just create a new instance of textToSpeech
            textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    textToSpeech.setLanguage(Locale.US);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        LessonCategory category = (LessonCategory) getArguments().getSerializable(BUNDLE_LESSON_CATEGORY);
        manager.loadLessonScript(category);
        View view = inflater.inflate(R.layout.fragment_lesson_script, container, false);
        list = view.findViewById(R.id.lesson_script_sentence_list);
        loading = view.findViewById(R.id.lesson_script_loading);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        LessonCategory category = (LessonCategory) getArguments().getSerializable(BUNDLE_LESSON_CATEGORY);
        String title;
        if (category == null){
            title = getString(R.string.lesson_description_title);
        } else {
            title = category.getTitleJP();
        }
        listener.setToolbarState(
                new ToolbarState(title, false, false, null)
        );
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
            listener = (LessonScriptListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private LessonScriptManager.LessonScriptManagerListener getLessonScriptManagerListener(){
        return new LessonScriptManager.LessonScriptManagerListener() {
            @Override
            public void onLessonScriptLoaded(Script lessonScript, LessonInstanceData data) {
                showScript(lessonScript, data);
            }
        };
    }

    private void showScript(Script script, LessonInstanceData data){
        hideLoading();
        LessonScriptAdapter.LessonScriptAdapterListener adapterListener =
                getAdapterListener(data);
        int lessonNumber = manager.getLessonNumber();
        int totalLessonCt = manager.getTotalLessonCt();
        if (adapter == null || list.getAdapter() == null) {
            adapter = new LessonScriptAdapter(script, lessonNumber, totalLessonCt,
                    adapterListener, textToSpeech);
            list.setLayoutManager(new LinearLayoutManager(getContext()));
            list.setAdapter(adapter);
        } else {
            adapter.updateScript(script, lessonNumber, adapterListener);
        }
    }

    private LessonScriptAdapter.LessonScriptAdapterListener getAdapterListener(final LessonInstanceData data){
        return new LessonScriptAdapter.LessonScriptAdapterListener() {
            @Override
            public void toQuestion() {
                Lesson lesson = manager.getCurrentLesson();
                List<QuestionData> questions = lesson.createQuestions(data.getEntityPropertyData());
                listener.lessonScriptToQuestion(data, questions);
            }
            @Override
            public void toPrev(){
                if (manager.hasPreviousLesson()) {
                    showLoading();
                    manager.toPreviousLesson();
                }
            }
            @Override
            public void toNext(){
                if (manager.hasNextLesson()) {
                    showLoading();
                    manager.toNextLesson();
                }
            }
        };
    }

    private void hideLoading(){
        loading.setVisibility(View.GONE);
    }

    private void showLoading(){
        loading.setVisibility(View.VISIBLE);
        adapter.isLoading();
    }
}
