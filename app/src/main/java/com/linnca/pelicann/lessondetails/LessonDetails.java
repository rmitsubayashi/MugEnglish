package com.linnca.pelicann.lessondetails;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseAnalyticsHeaders;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonFactory;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionAttempt;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.Seconds;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LessonDetails extends Fragment {
    private final String TAG = "LessonDetails";
    private FirebaseDatabase db;
    private String userID;
    private FirebaseAnalytics firebaseLog;
    public static final String BUNDLE_LESSON_DATA = "lessonData";
    private LessonData lessonData;
    private RecyclerView list;
    private FloatingActionButton createButton;
    private ProgressBar createProgressBar;
    private TextView noItemTextView;
    private ProgressBar loading;
    private ViewGroup mainLayout;

    private LessonDetailsAdapter firebaseAdapter;
    private LessonDetailsListener lessonDetailsListener;

    public interface LessonDetailsListener {
        void lessonDetailsToQuestions(LessonInstanceData lessonInstanceData, String lessonKey);
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        db = FirebaseDatabase.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseLog = FirebaseAnalytics.getInstance(getActivity());
        firebaseLog.setCurrentScreen(getActivity(), TAG, TAG);
        firebaseLog.setUserId(userID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_lesson_details, container, false);
        list = view.findViewById(R.id.lesson_details_instanceList);
        noItemTextView = view.findViewById(R.id.lesson_details_no_items);
        loading = view.findViewById(R.id.lesson_details_loading);
        mainLayout = view.findViewById(R.id.fragment_lesson_details);
        createButton = view.findViewById(R.id.lesson_details_add);
        createProgressBar = view.findViewById(R.id.lesson_details_add_progress_bar);
        Bundle arguments = getArguments();
        if (arguments.getSerializable(BUNDLE_LESSON_DATA) != null) {
            //get data
            lessonData = (LessonData) arguments.getSerializable(BUNDLE_LESSON_DATA);

            addActionListeners();
            populateData();
            setLessonColor(lessonData.getColorID());
        }

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        lessonDetailsListener.setToolbarState(
                new ToolbarState(lessonData.getTitle(), false, lessonData.getKey())
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
            lessonDetailsListener = (LessonDetailsListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.lesson_details_item_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        LessonInstanceData longClickData = firebaseAdapter.getLongClickPositionData();
        switch (item.getItemId()) {
            case R.id.lesson_details_item_menu_more_info:
                //open a dialog with details (access records)
                getInstanceDetails(longClickData);
                return true;
            case R.id.lesson_details_item_menu_delete:
                DatabaseReference instanceRef = FirebaseDatabase.getInstance().getReference(
                        FirebaseDBHeaders.LESSON_INSTANCES + "/"+
                                userID+"/"+
                                lessonData.getKey() + "/" +
                                longClickData.getId()
                );
                instanceRef.removeValue();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void populateData(){

        //grab list of instances
        DatabaseReference lessonInstancesRef = db.getReference(
                FirebaseDBHeaders.LESSON_INSTANCES + "/"+userID+"/"+ lessonData.getKey());
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseAdapter = new LessonDetailsAdapter(lessonInstancesRef, noItemTextView, loading, lessonDetailsListener, lessonData.getKey());
        //when we create a new instance, remove the progress spinner
        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

            }
        });



        list.setAdapter(firebaseAdapter);

        registerForContextMenu(list);
    }


    private void setLessonColor(int color){
        //background for whole activity
        mainLayout.setBackgroundColor(ContextCompat.getColor(getContext(), color));

        /*
        //status bar (post-lollipop)
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
        //action bar
        appBar.setBackgroundColor(color);
        */

    }

    private void addActionListeners(){
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewInstance();
            }
        });
    }

    private void createNewInstance(){
        createButton.setEnabled(false);
        createButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.gray500)));

        createProgressBar.setVisibility(View.VISIBLE);
        //load lesson class
        Lesson lesson = LessonFactory.parseLesson(lessonData.getKey(),
                new Lesson.LessonListener() {
                    private DateTime startTime = DateTime.now();
            @Override
            public void onLessonCreated() {
                //since these will be called from a separate thread, we want to make sure
                // these run on the UI thread
                //( not sure if this achieves it though. These are called even though we destroy the fragment)
                createButton.post(new Runnable() {
                    @Override
                    public void run() {
                        if (LessonDetails.this.isVisible()) {
                            createButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.orange500)));
                            createButton.setEnabled(true);
                        }
                    }
                });
                createProgressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        if (LessonDetails.this.isVisible()) {
                            Animation fadeoutAnimation = new AlphaAnimation(1f,0f);
                            fadeoutAnimation.setDuration(500);
                            fadeoutAnimation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    createProgressBar.setVisibility(View.INVISIBLE);
                                    //reset alpha so the progress bar shows the next time around
                                    createProgressBar.setAlpha(1f);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            createProgressBar.startAnimation(fadeoutAnimation);

                        }
                    }
                });

                DateTime finishTime = DateTime.now();
                int millisecondsTaken = new Period(startTime, finishTime, PeriodType.millis()).getMillis();
                Bundle bundle = new Bundle();
                bundle.putInt(FirebaseAnalytics.Param.VALUE, millisecondsTaken);
                firebaseLog.logEvent(FirebaseAnalyticsHeaders.EVENT_LOAD, bundle);

            }
        });

        //first part connects to Firebase
        // thus running on the main UI thread.
        //the second part (connecting to wikidata)
        // runs on an async task
        if (lesson != null) {
            lesson.createInstance();
        }

        //the list listens for inserts and removes the loading spinner
    }

    private void getInstanceDetails(final LessonInstanceData instanceData){
        String instanceID = instanceData.getId();
        DatabaseReference recordsRef = db.getReference(
                FirebaseDBHeaders.INSTANCE_RECORDS + "/" +
                userID + "/" +
                lessonData.getKey() + "/" +
                instanceID
        );
        recordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<InstanceRecord> allRecords = new ArrayList<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot recordSnapshot : dataSnapshot.getChildren()){
                    InstanceRecord record = recordSnapshot.getValue(InstanceRecord.class);
                    allRecords.add(record);
                }

                showInstanceDetailDialog(instanceData, allRecords);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showInstanceDetailDialog(LessonInstanceData instanceData, List<InstanceRecord> allRecords){
        View dialogView = getLayoutInflater().inflate(R.layout.inflatable_lesson_details_instance_details_dialog, null);
        TextView lastPlayedTextView = dialogView.findViewById(R.id.lesson_details_instance_details_last_played);
        TextView lastPlayedScoreTextView = dialogView.findViewById(R.id.lesson_details_instance_details_last_played_score);
        TextView playedNumberTextView = dialogView.findViewById(R.id.lesson_details_instance_details_played_number);
        TextView averageScoreTextView = dialogView.findViewById(R.id.lesson_details_instance_details_average_score);
        if (allRecords == null || allRecords.size() == 0){
            playedNumberTextView.setText(Integer.toString(0));
            averageScoreTextView.setText("0%");
            lastPlayedTextView.setText(R.string.lesson_details_instance_details_last_played_never);
            lastPlayedScoreTextView.setText("0%");
        } else {
            //the records are already in date order by default
            InstanceRecord lastRecord = allRecords.get(allRecords.size() - 1);
            List<QuestionAttempt> lastRecordQuestions = lastRecord.getAttempts();
            //should this be the time completed or time started??
            long lastPlayedTimestamp = lastRecordQuestions.get(0).getStartTime();
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.JAPAN);
            String dateString = dateFormat.format(new Date(lastPlayedTimestamp));
            lastPlayedTextView.setText(dateString);
            int correctCt = 0;
            int totalCt = 0;
            String tempQuestionID = "";
            for (QuestionAttempt attempt : lastRecordQuestions) {
                String questionID = attempt.getQuestionID();
                //there can only be one correct answer per question.
                // (there can be multiple incorrect answers)
                if (attempt.getCorrect())
                    correctCt++;
                if (!tempQuestionID.equals(questionID)) {
                    totalCt++;
                    tempQuestionID = questionID;
                }
            }
            //out of 100%
            int lastPlayedPercentage = correctCt * 100 / totalCt;
            String lastPlayedScore = lastPlayedPercentage + "%";
            lastPlayedScoreTextView.setText(lastPlayedScore);

            playedNumberTextView.setText(Integer.toString(allRecords.size()));

            //reset
            correctCt = 0;
            totalCt = 0;
            for (InstanceRecord instanceRecord : allRecords) {
                //reset
                tempQuestionID = "";
                List<QuestionAttempt> attempts = instanceRecord.getAttempts();
                for (QuestionAttempt attempt : attempts) {
                    String questionID = attempt.getQuestionID();
                    //there can only be one correct answer per question.
                    // (there can be multiple incorrect answers)
                    if (attempt.getCorrect())
                        correctCt++;
                    if (!tempQuestionID.equals(questionID)) {
                        totalCt++;
                        tempQuestionID = questionID;
                    }
                }
            }
            int totalAveragePercentage = correctCt * 100 / totalCt;
            String totalAverageScore = totalAveragePercentage + "%";
            averageScoreTextView.setText(totalAverageScore);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        builder.create().show();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (firebaseAdapter != null)
            firebaseAdapter.cleanup();
    }

}
