package com.linnca.pelicann.mainactivity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.lessoncategorylist.LessonCategoryList;
import com.linnca.pelicann.lessonscript.LessonScript;
import com.linnca.pelicann.preferences.PreferencesListener;
import com.linnca.pelicann.questions.QuestionFragmentInterface;
import com.linnca.pelicann.questions.VerbQuestionStart;
import com.linnca.pelicann.results.Results;
import com.linnca.pelicann.results.ReviewResults;
import com.linnca.pelicann.results.VerbQuestionResults;
import com.linnca.pelicann.searchinterests.SearchInterests;
import com.linnca.pelicann.userinterests.UserInterests;
import com.linnca.pelicann.userprofile.UserProfile_HoursStudied;

import java.util.List;
import java.util.Locale;

import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessonlist.LessonCategory;
import pelicann.linnca.com.corefunctionality.lessonquestions.InstanceRecord;
import pelicann.linnca.com.corefunctionality.lessonquestions.InstanceReviewManager;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionManager;
import pelicann.linnca.com.corefunctionality.lessonquestions.VerbQuestionManager;
import pelicann.linnca.com.corefunctionality.userprofile.AppUsageLog;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        UserInterests.UserInterestListener,
        SearchInterests.SearchInterestsListener,
        QuestionFragmentInterface.QuestionListener,
        VerbQuestionStart.VerbQuestionStartListener,
        Results.ResultsListener,
        VerbQuestionResults.VerbQuestionResultsListener,
        ReviewResults.ReviewResultsListener,
        PreferencesListener,
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback,
        LessonCategoryList.LessonCategoryListListener,
        LessonScript.LessonScriptListener,
        UserProfile_HoursStudied.UserProfile_HoursStudiedListener
{
    private final String TAG = "MainActivity";
    private Database db;
    public final static String BUNDLE_DATABASE = "bundleDatabase";
    private final String SAVED_STATE_PREFERENCES = "savedStatePreferences";
    private boolean savedStatePreferences = false;
    private boolean searchIconVisible = false;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private boolean hamburgerEnabled = false;
    private boolean toolbarBackListenerAttached = false;
    private MainActivityFragmentManager fragmentManager;
    private boolean navigationItemSelected = false;
    private int selectedNavigationItemID = -1;
    private long startAppTimestamp;

    private QuestionManager questionManager;
    private InstanceReviewManager instanceReviewManager;
    private VerbQuestionManager verbQuestionManager;
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
        //setting the color scheme based on user's settings
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //list preferences can only store string arrays
        String themeString = preferences.getString(getString(R.string.preferences_general_themeColor_key),
                Integer.toString(ThemeColorChanger.BLUE));
        int theme;
        try {
            theme = Integer.parseInt(themeString);
        } catch (ClassCastException e){
            theme = ThemeColorChanger.BLUE;
        }
        ThemeColorChanger.setTheme(this, theme);

        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.tool_bar);
        //to make sure the toolbar text view is not null
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        fragmentManager = new MainActivityFragmentManager(getSupportFragmentManager());
        questionManager = new QuestionManager(getQuestionManagerListener());
        instanceReviewManager = new InstanceReviewManager(getInstanceReviewManagerListener());
        verbQuestionManager = new VerbQuestionManager(getVerbQuestionManagerListener());

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
                        case R.id.main_navigation_drawer_settings :
                            fragmentManager.rootToSettings(db);
                            break;
                        case R.id.main_navigation_drawer_lessons :
                            fragmentManager.rootToLessonCategoryList(db);
                            break;
                        case R.id.main_navigation_drawer_verb_questions :
                            fragmentManager.rootToVerbQuestionStart();
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

        if (savedInstanceState != null &&
                savedInstanceState.getBoolean(SAVED_STATE_PREFERENCES)){
            checkNavigationItem(R.id.main_navigation_drawer_settings);
            fragmentManager.rootToSettings(db);
        } else {
            //initial fragment on launch
            setLessonView();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        startAppTimestamp = System.currentTimeMillis();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        //to make sure the animation doesn't trigger on launch,
        //the menu is defaulted to invisible
        animateMenuItem(menu.findItem(R.id.app_bar_search), searchIconVisible);
        return true;
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

        if (fragmentManager.isVisible(QuestionFragmentInterface.TAG)){
            if (questionManager.questionsStarted()) {
                questionManager.resetManager();
            }
            //we are just going back to the screen where the user can
            // once again start a review, so don't clear the data but
            // take it back to the initial state
            if (instanceReviewManager.reviewStarted()) {
                instanceReviewManager.resetCurrentQuestionIndex();
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
    public void onSaveInstanceState(Bundle outState){
        if (savedStatePreferences){
            outState.putBoolean(SAVED_STATE_PREFERENCES, true);
        }
        super.onSaveInstanceState(outState);
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
    public void updateTheme(){
        savedStatePreferences = true;
        //we want the changes to be reflected,
        // so re-create the activity.
        //we have to call setTheme before any views are written
        recreate();
    }

    @Override
    public void userInterestsToSearchInterests(){
        fragmentManager.userInterestsToSearchInterests(db);
        switchActionBarUpButton();
    }

    @Override
    public void verbQuestionStartToQuestion(){
        verbQuestionManager.startQuestions();
        switchActionBarUpButton();
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
            public void onQuestionsFinished(InstanceRecord instanceRecord,
                                            List<QuestionData> missedQuestions) {
                //we are updating the database in the result fragment since we need
                // to update the UI of the result fragment
                fragmentManager.questionToResults(db, instanceRecord);
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

    private VerbQuestionManager.VerbQuestionManagerListener getVerbQuestionManagerListener(){
        return new VerbQuestionManager.VerbQuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                if (firstQuestion){
                    fragmentManager.notQuestionFragmentToQuestion(questionData, questionNumber, totalQuestions);
                } else {
                    fragmentManager.questionToQuestion(questionData, questionNumber, totalQuestions);
                }
            }

            @Override
            public void onQuestionsFinished() {
                fragmentManager.questionToVerbQuestionResults(verbQuestionManager.getCorrectCt(), verbQuestionManager.getWrongWords());
            }
        };
    }

    @Override
    public void onRecordResponse(String answer, boolean correct){
        if (questionManager.questionsStarted()) {
            questionManager.saveResponse(answer, correct);
        } else if (verbQuestionManager.isVerbQuestionsStarted()){
            if (correct) {
                verbQuestionManager.incrementCorrectCt();
            } else {
                verbQuestionManager.addCurrentWrongWord();
            }
        }
    }

    //this is called by each question fragment.
    //the question fragment communicates with the main activity ->
    // the main activity communicates with the question manager ->
    // the question manager tells the main activity what to do next ->
    // the main activity creates the next question fragment
    @Override
    public void onNextQuestion(boolean correct, OnDBResultListener noConnectionListener){
        if (questionManager.questionsStarted()) {
            questionManager.nextQuestion();
        }
        else if (instanceReviewManager.reviewStarted()) {
            instanceReviewManager.nextQuestion(false);
        }
        else if (verbQuestionManager.isVerbQuestionsStarted()){
            verbQuestionManager.nextQuestion(false);
        }
    }

    @Override
    public void resultsToLessonList(){
        //we can remove all the data because the result fragment is
        // not accessible anymore.
        instanceReviewManager.resetManager();
        setLessonView();
    }

    @Override
    public void verbQuestionResultsToLessonList(){
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

    @Override
    public void lessonCategoryListToLessonScript(LessonCategory lessonCategory){
        fragmentManager.lessonCategoryListToLessonScript(lessonCategory, db);
        switchActionBarUpButton();
    }

    @Override
    public void lessonScriptToQuestion(LessonInstanceData lessonInstanceData, List<QuestionData> questions){
        questionManager.startQuestions(questions, lessonInstanceData);
    }

    private void setLessonView(){
        //finding the ID of the navigation drawer
        int navigationDrawerItemIDToSelect = R.id.main_navigation_drawer_lessons;
        checkNavigationItem(navigationDrawerItemIDToSelect);
        fragmentManager.rootToLessonCategoryList(db);

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
        String toolbarTitle = state.getTitle();
        toolbar.setTitle(toolbarTitle);
        //icons
        searchIconVisible = state.searchIconVisible();

        //this redraws the toolbar so the initial visibility is always false.
        //this is not the right way (nor the behavior I want) but this can come later...
        invalidateOptionsMenu();
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
