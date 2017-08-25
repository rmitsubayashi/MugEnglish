package com.linnca.pelicann.gui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.db.datawrappers.LessonData;
import com.linnca.pelicann.db.datawrappers.LessonInstanceData;
import com.linnca.pelicann.gui.widgets.LessonDetailsAdapter;
import com.linnca.pelicann.questiongenerator.Lesson;
import com.linnca.pelicann.questiongenerator.LessonFactory;

public class LessonDetails extends Fragment {
    public static String BUNDLE_LESSON_DATA = "lessonData";
    public static String BUNDLE_BACKGROUND_COLOR = "backgroundColor";
    private LessonData lessonData;
    private RecyclerView list;
    private FloatingActionButton createButton;
    private TextView noItemTextView;
    private ProgressBar loading;
    private ViewGroup mainLayout;

    private LessonDetailsAdapter firebaseAdapter;
    private LessonDetailsListener lessonDetailsListener;

    public interface LessonDetailsListener {
        void lessonDetailsToQuestions(LessonInstanceData lessonInstanceData, String lessonKey);
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
        Bundle arguments = getArguments();
        if (arguments.getSerializable(BUNDLE_LESSON_DATA) != null) {
            //get data
            lessonData = (LessonData) arguments.getSerializable(BUNDLE_LESSON_DATA);

            addActionListeners();
            populateData();
            adjustLayout();

        }
        int color = arguments.getInt(BUNDLE_BACKGROUND_COLOR, 0);
        setLessonColor(color);

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
            lessonDetailsListener = (LessonDetailsListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lesson_details_app_bar_menu, menu);
        final MenuItem star1 = menu.findItem(R.id.lesson_details_star1);
        final MenuItem star2 = menu.findItem(R.id.lesson_details_star2);
        final MenuItem star3 = menu.findItem(R.id.lesson_details_star3);
        final List<MenuItem> stars = new ArrayList<>(3);
        stars.add(star1);
        stars.add(star2);
        stars.add(star3);
        final AchievementStars result = new AchievementStars();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            result.setFirstInstance(false);
            result.setRepeatInstance(false);
            result.setSecondInstance(false);
            GUIUtils.populateStarsMenu(stars, result, this);
            return true;
        }
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(FirebaseDBHeaders.ACHIEVEMENTS + "/"
                + userID + "/" + lessonData.getKey());
        //want to update it when we've completed an achievement
        //so listen continuously
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //exists
                if (dataSnapshot.exists()) {
                    AchievementStars copy = dataSnapshot.getValue(AchievementStars.class);
                    result.setFirstInstance(copy.getFirstInstance());
                    result.setRepeatInstance(copy.getRepeatInstance());
                    result.setSecondInstance(copy.getSecondInstance());
                } else {
                    //doesn't exist so return no stars
                    result.setFirstInstance(false);
                    result.setRepeatInstance(false);
                    result.setSecondInstance(false);
                }

                GUIUtils.populateStarsMenu(stars, result, LessonDetails.this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                result.setFirstInstance(false);
                result.setRepeatInstance(false);
                result.setSecondInstance(false);
                GUIUtils.populateStarsMenu(stars, result, LessonDetails.this);
            }
        });

        return true;
    }*/

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.lesson_details_item_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        DatabaseReference longClickRef = firebaseAdapter.getLongClickPosition();
        switch (item.getItemId()) {
            case R.id.lesson_details_item_menu_more_info:
                //open fragment?
                return true;
            case R.id.lesson_details_item_menu_delete:
                longClickRef.removeValue();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void populateData(){
        /*LinearLayout titleLayout =
                (LinearLayout) getLayoutInflater().inflate(R.layout.inflatable_lesson_details_title, list, false);
        TextView titleTextView = (TextView) titleLayout.findViewById(R.id.lesson_details_title);
        titleTextView.setText(lessonData.getTitle());
        ImageView iconView = (ImageView)titleLayout.findViewById(R.id.lesson_details_icon);
        int imageID = GUIUtils.stringToDrawableID(lessonData.getImage(),this);
        iconView.setImageResource(imageID);*/

        //grab list of instances
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference lessonInstancesRef = FirebaseDatabase.getInstance().getReference(
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
        mainLayout.setBackgroundColor(color);
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

    private void adjustLayout(){

        //only show description if the user has enabled it (default is enabled)
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(LessonDetails.this.getContext());
        boolean showDescription = sharedPreferences.getBoolean
                (getString(R.string.preferences_questions_descriptionBeforeQuestions_key), true);
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
        //load lesson class
        Lesson lesson = LessonFactory.parseLesson(lessonData.getKey(),
                new Lesson.LessonListener() {
            @Override
            public void onLessonCreated() {

            }
        });
        //first part connects to Firebase
        // thus running on the main UI thread.
        //the second part (connecting to wikidata)
        // runs on an async task
        if (lesson != null)
            lesson.createInstance();

        //the list listens for inserts and removes the loading spinner
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (firebaseAdapter != null)
            firebaseAdapter.cleanup();
    }

}
