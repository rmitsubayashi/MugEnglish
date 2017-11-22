package com.linnca.pelicann.mainactivity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.lessondetails.LessonDescription;
import com.linnca.pelicann.lessondetails.LessonDetails;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.LessonFactory;
import com.linnca.pelicann.lessonlist.LessonListViewer;
import com.linnca.pelicann.lessonlist.LessonList;
import com.linnca.pelicann.mainactivity.widgets.GUIUtils;
import com.linnca.pelicann.mainactivity.widgets.ToolbarSpinnerAdapter;
import com.linnca.pelicann.mainactivity.widgets.ToolbarSpinnerItem;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;
import com.linnca.pelicann.preferences.PreferencesListener;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.InstanceReviewManager;
import com.linnca.pelicann.questions.LessonsReviewManager;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionManager;
import com.linnca.pelicann.questions.Question_General;
import com.linnca.pelicann.results.Results;
import com.linnca.pelicann.results.ReviewResults;
import com.linnca.pelicann.searchinterests.SearchInterests;
import com.linnca.pelicann.userinterests.UserInterests;
import com.linnca.pelicann.userprofile.AppUsageLog;
import com.linnca.pelicann.userprofile.UserProfile;
import com.linnca.pelicann.vocabulary.VocabularyDetails;
import com.linnca.pelicann.vocabulary.VocabularyList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        LessonList.LessonListListener,
        UserInterests.UserInterestListener,
        SearchInterests.SearchInterestsListener,
        Question_General.QuestionListener,
        LessonDetails.LessonDetailsListener,
        Results.ResultsListener,
        ReviewResults.ReviewResultsListener,
        PreferencesListener,
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback,
        LessonDescription.LessonDescriptionListener,
        UserProfile.UserProfileListener,
        VocabularyList.VocabularyListListener,
        VocabularyDetails.VocabularyDetailsListener
{
    private final String TAG = "MainActivity";
    private Database db;
    public final static String BUNDLE_DATABASE = "bundleDatabase";
    private boolean searchIconVisible = false;
    private boolean descriptionIconVisible = false;
    private String descriptionLessonKey;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private Spinner toolbarSpinner;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private boolean hamburgerEnabled = false;
    private boolean toolbarBackListenerAttached = false;
    private MainActivityFragmentManager fragmentManager;
    private boolean navigationItemSelected = false;
    private int selectedNavigationItemID = -1;
    private LessonListViewer lessonListViewer;
    private long startAppTimestamp;

    private QuestionManager questionManager;
    private InstanceReviewManager instanceReviewManager;
    private LessonsReviewManager lessonsReviewManager;
    //since initialization takes forever, initialize here and use this instance in all questions
    private TextToSpeech textToSpeech = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            db = (Database) getIntent().getSerializableExtra(MainActivity.BUNDLE_DATABASE);
        } catch (Exception e){
            e.printStackTrace();
            //hard code a new database instance
            db = new FirebaseDB();
        }

        setContentView(R.layout.activity_main);

        LessonFactory.saveGenericQuestions(db);
        lessonListViewer = new LessonListViewer();

        toolbar = findViewById(R.id.tool_bar);
        toolbarSpinner = toolbar.findViewById(R.id.tool_bar_spinner);
        //to make sure the toolbar text view is not null
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        setSpinnerAdapter();

        fragmentManager = new MainActivityFragmentManager(getSupportFragmentManager());
        questionManager = new QuestionManager(db, getQuestionManagerListener());
        instanceReviewManager = new InstanceReviewManager(getInstanceReviewManagerListener());

        drawerLayout = findViewById(R.id.main_activity_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //if the user selected an item, do this
                //(this is called after the animation finishes
                // so better ux)
                if (navigationItemSelected){
                    switch (selectedNavigationItemID){
                        case R.id.main_navigation_drawer_interests :
                            fragmentManager.rootToUserInterests(db);
                            break;
                        case R.id.main_navigation_drawer_data :
                            fragmentManager.rootToUserProfile(db);
                            break;
                        case R.id.main_navigation_drawer_vocabulary :
                            fragmentManager.rootToVocabularyList(db);
                            break;
                        case R.id.main_navigation_drawer_settings :
                            fragmentManager.rootToSettings(db);
                            break;
                        case R.id.main_navigation_drawer_lesson_level1 :
                            fragmentManager.rootToLessonList(db, 1);
                            setLastSelectedLessonLevel(1);
                            break;
                        case R.id.main_navigation_drawer_lesson_level2 :
                            fragmentManager.rootToLessonList(db, 2);
                            setLastSelectedLessonLevel(2);
                            break;
                        default:
                            return;
                    }

                    //reset so this won't be called if user plainly closes navigation drawer
                    navigationItemSelected = false;
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else the hamburger icon wont show up
        actionBarDrawerToggle.syncState();
        hamburgerEnabled = true;

        navigationView = findViewById(R.id.main_navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        //initial fragment on launch
        setLessonView();
    }

    @Override
    protected void onStart(){
        super.onStart();
        startAppTimestamp = System.currentTimeMillis();

        /*
        DateTime temp1 = DateTime.now();
        temp1 = temp1.minusMonths(1);
        DateTime temp2 = DateTime.now();
        temp2 = temp2.minusMonths(1).plusHours(1);
        db.addAppUsageLog(new AppUsageLog(temp1.getMillis(), temp2.getMillis()));

        temp1 = temp1.minusMonths(1);
        temp2 = temp2.minusMonths(1).plusHours(1);
        db.addAppUsageLog(new AppUsageLog(temp1.getMillis(), temp2.getMillis()));*/
        /*
        SportsHelper helper = new SportsHelper();
        helper.run();*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_bar_menu, menu);
        //change the icon to white
        menu.findItem(R.id.app_bar_description).getIcon().setColorFilter(
                ContextCompat.getColor(this, R.color.white),
                PorterDuff.Mode.SRC_ATOP
        );
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        //to make sure the animation doesn't trigger on launch,
        //the menu is defaulted to invisible
        animateMenuItem(menu.findItem(R.id.app_bar_search), searchIconVisible);
        animateMenuItem(menu.findItem(R.id.app_bar_description), descriptionIconVisible);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.app_bar_description :
                //we set this to null when we don't have a description
                //associated with the current fragment.
                //technically this is not needed as we are also hiding the icon
                if (descriptionLessonKey != null){
                    fragmentManager.fragmentToLessonDescription(descriptionLessonKey);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem){
        menuItem.setChecked(true);
        navigationItemSelected = true;
        checkNavigationItem(menuItem.getItemId());
        //we want the action to trigger after the drawer closes
        //(for better ux) so close the drawer here
        //and set the action on the onCloseDrawer listener for the drawerLayout
        drawerLayout.closeDrawer(navigationView, true);
        return false;
    }

    @Override
    public void onBackPressed(){
        //default behavior for when a drawer is open is to close it
        if (drawerLayout.isDrawerOpen(navigationView)){
            drawerLayout.closeDrawer(navigationView, true);
            return;
        }

        //have to handle this in the activity or it gets really pain-in-the-ass-y.
        //not necessary for a lot of cases
        GUIUtils.hideKeyboard(getCurrentFocus());

        if (fragmentManager.isVisible(Question_General.TAG)){
            if (questionManager.questionsStarted()) {
                questionManager.resetManager();
            }
            //we are just going back to the screen where the user can
            // once again start a review, so don't clear the data but
            // take it back to the initial state
            if (instanceReviewManager.reviewStarted()) {
                instanceReviewManager.resetCurrentQuestionIndex();
            }

            if (lessonsReviewManager != null &&
                    lessonsReviewManager.reviewStarted()) {
                //release resources
                lessonsReviewManager = null;
            }

            fragmentManager.fromQuestionToFragment();

            if (textToSpeech != null){
                textToSpeech.shutdown();
                textToSpeech = null;
            }
        } else if (fragmentManager.isVisible(Results.TAG)){
            //the user will bot be able to access the results screen again
            // so clear all data
            instanceReviewManager.resetManager();
        }

        if (fragmentManager.fragmentAfterBackPressIsRoot()){
            switchActionBarUpButton();
        }

        super.onBackPressed();
    }

    @Override
    public void lessonListToLessonDetails(LessonData lessonData){
        fragmentManager.lessonListToLessonDetails(db, lessonData);
        switchActionBarUpButton();
    }

    @Override
    public void lessonListToReview(int lessonLevel, String reviewKey){
        //since this will not be called a lot
        // (unlike the question and instance review manager),
        // instantiate it locally
        lessonsReviewManager = new LessonsReviewManager(db, getLessonsReviewManagerListener());
        lessonsReviewManager.startReview(lessonLevel, reviewKey);
        switchActionBarUpButton();
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat,
                                           PreferenceScreen preferenceScreen) {

        boolean successful = fragmentManager.toPreferenceScreen(this, preferenceScreen);
        if (successful) {
            switchActionBarUpButton();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void userInterestsToSearchInterests(){
        fragmentManager.userInterestsToSearchInterests(db);
        switchActionBarUpButton();
    }

    @Override
    public void vocabularyListToVocabularyDetails(String key){
        fragmentManager.vocabularyListToVocabularyDetails(db, key);
        switchActionBarUpButton();
    }

    @Override
    public void vocabularyDetailsToLessonDetails(String lessonKey){
        //make sure when the user presses the back button after the redirect,
        //the user goes to the lesson list screen
        fragmentManager.vocabularyDetailsToLessonDetails(db, lessonKey, lessonListViewer);

        int lessonLevel = lessonListViewer.getLessonLevel(lessonKey);
        //select the proper item in the navigation drawer
        switch (lessonLevel){
            case 1 :
                navigationView.getMenu().findItem(R.id.main_navigation_drawer_lesson_level1).setChecked(true);
                break;
            case 2 :
                navigationView.getMenu().findItem(R.id.main_navigation_drawer_lesson_level2).setChecked(true);
                break;
        }
        navigationView.getMenu().findItem(R.id.main_navigation_drawer_vocabulary).setChecked(false);


    }

    private QuestionManager.QuestionManagerListener getQuestionManagerListener(){
        return new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                if (firstQuestion){
                    fragmentManager.notQuestionFragmentToQuestion(questionData, questionNumber, totalQuestions);
                } else {
                    fragmentManager.questionToQuestion(questionData, questionNumber, totalQuestions);
                }
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, ArrayList<String> questionIDs,
                                            List<QuestionData> missedQuestions) {
                //we are updating the database in the result fragment since we need
                // to update the UI of the result fragment
                fragmentManager.questionToResults(db, instanceRecord, questionIDs);
                //save the missed questions in the instance review manager since
                //we are resetting the question manager
                instanceReviewManager.setQuestions(missedQuestions);

                //although we might use it in the review or subsequent questions,
                //we should prioritize the resources we can save right now?
                if (textToSpeech != null){
                    textToSpeech.shutdown();
                    textToSpeech = null;
                }
            }
        };
    }

    private InstanceReviewManager.InstanceReviewManagerListener getInstanceReviewManagerListener(){
        return new InstanceReviewManager.InstanceReviewManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                if (firstQuestion){
                    fragmentManager.notQuestionFragmentToQuestion(questionData, questionNumber, totalQuestions);
                } else {
                    fragmentManager.questionToQuestion(questionData, questionNumber, totalQuestions);
                }
            }

            @Override
            public void onReviewFinished() {
                fragmentManager.questionToReviewResults();
                if (textToSpeech != null){
                    textToSpeech.shutdown();
                    textToSpeech = null;
                }
            }
        };
    }

    private LessonsReviewManager.LessonReviewManagerListener getLessonsReviewManagerListener(){
        return new LessonsReviewManager.LessonReviewManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                if (firstQuestion){
                    fragmentManager.notQuestionFragmentToQuestion(questionData, questionNumber, totalQuestions);
                } else {
                    fragmentManager.questionToQuestion(questionData, questionNumber, totalQuestions);
                }
            }

            @Override
            public void onReviewFinished(int lessonLevel, String reviewID) {
                fragmentManager.questionToReviewResults();
                //we won't need a reference to the review manager anymore
                lessonsReviewManager = null;
                //mark the review lesson as cleared
                OnResultListener clearLessonOnResultListener = new OnResultListener() {
                    @Override
                    public void onClearedLessonAdded(boolean firstTimeCleared) {
                        super.onClearedLessonAdded(firstTimeCleared);
                    }
                };
                db.addClearedLesson(lessonLevel, reviewID, clearLessonOnResultListener);
                //the user will never need the review again,
                // so remove the review questions we stored in the database
                OnResultListener removeReviewOnResultListener = new OnResultListener() {
                    @Override
                    public void onReviewQuestionsRemoved() {
                        super.onReviewQuestionsRemoved();
                    }
                };
                db.removeReviewQuestions(removeReviewOnResultListener);

                if (textToSpeech != null){
                    textToSpeech.shutdown();
                    textToSpeech = null;
                }
            }
        };
    }

    @Override
    public void lessonDetailsToQuestions(LessonInstanceData lessonInstanceData, String lessonKey){
        questionManager.startQuestions(lessonInstanceData, lessonKey);
    }

    @Override
    public void onRecordResponse(String answer, boolean correct){
        if (questionManager.questionsStarted()) {
            questionManager.saveResponse(answer, correct);
        }
    }

    //this is called by each question fragment.
    //the question fragment communicates with the main activity ->
    // the main activity communicates with the question manager ->
    // the question manager tells the main activity what to do next ->
    // the main activity creates the next question fragment
    @Override
    public void onNextQuestion(boolean correct){
        if (questionManager.questionsStarted()) {
            questionManager.nextQuestion(false);
        }
        if (instanceReviewManager.reviewStarted()) {
            instanceReviewManager.nextQuestion(false);
        }
        //we might not have instantiated this yet
        if (lessonsReviewManager != null){
            if (correct){
                lessonsReviewManager.nextQuestion(false);
            } else {
                lessonsReviewManager.returnQuestionToStack();
                lessonsReviewManager.nextQuestion(false);
            }
        }
    }

    private void setLastSelectedLessonLevel(int level){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.preferences_last_selected_lesson_level), level);
        editor.apply();
    }

    @Override
    public void resultsToLessonList(){
        //we can remove all the data because the result fragment is
        // not accessible anymore.
        instanceReviewManager.resetManager();
        setLessonView();
    }

    @Override
    public void reviewResultsToLessonList(){
        setLessonView();
    }

    @Override
    public void resultsToReview(){
        instanceReviewManager.startReview();
    }

    private void setLessonView(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //default is lowest level (1)
        int lastSelectedLessonLevel = preferences.getInt(getString(R.string.preferences_last_selected_lesson_level), 1);
        //finding the ID of the navigation drawer
        int navigationDrawerItemIDToSelect;
        switch (lastSelectedLessonLevel){
            case 1 :
                navigationDrawerItemIDToSelect = R.id.main_navigation_drawer_lesson_level1;
                break;
            case 2 :
                navigationDrawerItemIDToSelect = R.id.main_navigation_drawer_lesson_level2;
                break;
            default:
                navigationDrawerItemIDToSelect = R.id.main_navigation_drawer_lesson_level1;
        }
        checkNavigationItem(navigationDrawerItemIDToSelect);
        fragmentManager.rootToLessonList(db, lastSelectedLessonLevel);

        if (!hamburgerEnabled){
            switchActionBarUpButton();
        }
    }

    private void animateMenuItem(final MenuItem menuItem, final boolean toVisibility){
        boolean currentlyVisible = menuItem.isVisible();
        //no need to animate
        if (currentlyVisible == toVisibility){
            return;
        }

        ValueAnimator valueAnimator;
        final Drawable iconDrawable = menuItem.getIcon();
        //we are fading it out
        if (currentlyVisible) {
             valueAnimator = ValueAnimator.ofInt(255, 0);
        } else {
            valueAnimator = ValueAnimator.ofInt(0,255);
            iconDrawable.setAlpha(0);
            menuItem.setVisible(true);

        }
        valueAnimator.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                iconDrawable.setAlpha((int)valueAnimator.getAnimatedValue());
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                menuItem.setVisible(toVisibility);
                iconDrawable.setAlpha(255);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        valueAnimator.start();
    }

    //the navigation view doesn't handle checking items under sub-headers
    // so we have to handle the logic manually
    private void checkNavigationItem(int id){
        if (selectedNavigationItemID != -1){
            navigationView.getMenu().findItem(selectedNavigationItemID).setChecked(false);
        }
        navigationView.getMenu().findItem(id).setChecked(true);
        selectedNavigationItemID = id;
    }

    private void switchActionBarUpButton(){
        if (hamburgerEnabled){
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(actionBarDrawerToggle.getDrawerArrowDrawable(), "progress", 1);
            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    hamburgerEnabled = false;
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    if (!toolbarBackListenerAttached){
                        actionBarDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onUpPressed();
                            }
                        });
                        toolbarBackListenerAttached = true;
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            objectAnimator.start();
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            actionBarDrawerToggle.setToolbarNavigationClickListener(null);
            toolbarBackListenerAttached = false;
            hamburgerEnabled = true;
            ObjectAnimator.ofFloat(actionBarDrawerToggle.getDrawerArrowDrawable(), "progress", 0).start();
        }
    }

    private void onUpPressed(){
        onBackPressed();
    }

    @Override
    public void setToolbarState(ToolbarState state){
        boolean spinnerVisible = state.spinnerVisible();
        if (spinnerVisible){
            //reset the spinner
            toolbarSpinner.setSelection(0);
        }
        toolbarSpinner.setVisibility(spinnerVisible ? View.VISIBLE : View.GONE);
        String toolbarTitle = state.getTitle();
        toolbar.setTitle(toolbarTitle);
        //icons
        searchIconVisible = state.searchIconVisible();
        descriptionLessonKey = state.getDescriptionLessonKey();
        if (descriptionLessonKey == null) {
            descriptionIconVisible = false;
        } else {
            descriptionIconVisible = lessonListViewer.layoutExists(descriptionLessonKey);

        }

        //this redraws the toolbar so the initial visibility is always false.
        //this is not the right way (nor the behavior I want) but this can come later...
        invalidateOptionsMenu();
    }

    private void setSpinnerAdapter(){
        if (toolbarSpinner.getAdapter() == null){
            List<ToolbarSpinnerItem> toolbarSpinnerItems = new ArrayList<>();
            toolbarSpinnerItems.add(
                    new ToolbarSpinnerItem(getString(R.string.user_interests_filter_all), R.drawable.ic_all)
            );
            toolbarSpinnerItems.add(
                    new ToolbarSpinnerItem(getString(R.string.user_interests_filter_people), R.drawable.ic_person)
            );
            toolbarSpinnerItems.add(
                    new ToolbarSpinnerItem(getString(R.string.user_interests_filter_places), R.drawable.ic_places)
            );
            toolbarSpinnerItems.add(
                    new ToolbarSpinnerItem(getString(R.string.user_interests_filter_other), R.drawable.ic_other)
            );
            ToolbarSpinnerAdapter adapter = new ToolbarSpinnerAdapter(this, toolbarSpinnerItems);
            toolbarSpinner.setAdapter(adapter);
            toolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    if (fragmentManager.isVisible(UserInterests.TAG)){
                        //since we don't have ids, differentiate the items by position
                        int filter;
                        switch (position){
                            case 0 :
                                filter = ToolbarSpinnerAdapter.FILTER_ALL;
                                break;
                            case 1 :
                                filter = ToolbarSpinnerAdapter.FILTER_PERSON;
                                break;
                            case 2 :
                                filter = ToolbarSpinnerAdapter.FILTER_PLACE;
                                break;
                            case 3 :
                                filter = ToolbarSpinnerAdapter.FILTER_OTHER;
                                break;
                            default :
                                filter = ToolbarSpinnerAdapter.FILTER_ALL;
                        }
                        ((UserInterests)fragmentManager.getFragment(UserInterests.TAG))
                                .filterUserInterests(filter);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else {
            toolbarSpinner.setSelection(0);
        }
    }

    public TextToSpeech getTextToSpeech(){
        if (textToSpeech == null){
            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    textToSpeech.setLanguage(Locale.US);
                }
            });
        }

        return textToSpeech;
    }

    @Override
    protected void onStop(){
        super.onStop();
        long endAppTimeStamp = System.currentTimeMillis();
        db.addAppUsageLog(new AppUsageLog(startAppTimestamp, endAppTimeStamp));
        db.cleanup();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if (textToSpeech != null){
            textToSpeech.shutdown();
        }
    }
}
