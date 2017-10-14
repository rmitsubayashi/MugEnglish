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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.lessondetails.LessonDescription;
import com.linnca.pelicann.lessondetails.LessonDetails;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.LessonFactory;
import com.linnca.pelicann.lessonlist.LessonHierarchyViewer;
import com.linnca.pelicann.lessonlist.LessonList;
import com.linnca.pelicann.mainactivity.widgets.GUIUtils;
import com.linnca.pelicann.mainactivity.widgets.ToolbarSpinnerAdapter;
import com.linnca.pelicann.mainactivity.widgets.ToolbarSpinnerItem;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;
import com.linnca.pelicann.preferences.Preferences;
import com.linnca.pelicann.preferences.PreferencesDescriptionBeforeLessonWithExceptionRule;
import com.linnca.pelicann.preferences.PreferencesListener;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionManager;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.Question_Chat;
import com.linnca.pelicann.questions.Question_Chat_MultipleChoice;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_General;
import com.linnca.pelicann.questions.Question_MultipleChoice;
import com.linnca.pelicann.questions.Question_Puzzle_Piece;
import com.linnca.pelicann.questions.Question_Spelling;
import com.linnca.pelicann.questions.Question_Spelling_Suggestive;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.questions.Question_TrueFalse;
import com.linnca.pelicann.results.Results;
import com.linnca.pelicann.searchinterests.SearchInterests;
import com.linnca.pelicann.userinterests.UserInterests;
import com.linnca.pelicann.userprofile.AppUsageLog;
import com.linnca.pelicann.userprofile.UserProfile;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        LessonList.LessonListListener,
        UserInterests.UserInterestListener,
        SearchInterests.SearchInterestsListener,
        Question_General.QuestionListener,
        LessonDetails.LessonDetailsListener,
        Results.ResultsListener,
        PreferencesListener,
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback,
        LessonDescription.LessonDescriptionListener,
        UserProfile.UserProfileListener
{
    private final String TAG = "MainActivity";
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
    private FragmentManager fragmentManager;
    private boolean navigationItemSelected = false;
    private int selectedNavigationItemID = -1;
    private LessonHierarchyViewer lessonHierarchyViewer;
    private long startAppTimestamp;

    private String topmostFragmentTag = "";
    private final String FRAGMENT_USER_INTERESTS = "userInterests";
    private final String FRAGMENT_LESSON_LIST = "lessonList";
    private final String FRAGMENT_SETTINGS = "settings";
    private final String FRAGMENT_LESSON_DETAILS = "lessonDetails";
    private final String FRAGMENT_QUESTION = "question";
    private final String FRAGMENT_RESULTS = "results";
    private final String FRAGMENT_SEARCH_INTERESTS = "searchInterests";
    private final String FRAGMENT_LESSON_DESCRIPTION = "lessonDescription";

    private QuestionManager questionManager;
    //since initialization takes forever, initialize here and use this instance in all questions
    private TextToSpeech textToSpeech = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LessonFactory.saveGenericQuestions();
        lessonHierarchyViewer = new LessonHierarchyViewer();

        toolbar = findViewById(R.id.tool_bar);
        toolbarSpinner = toolbar.findViewById(R.id.tool_bar_spinner);
        setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();
        questionManager = new QuestionManager(getQuestionManagerListener());

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
                    Fragment newFragment;
                    Bundle bundle = new Bundle();
                    String newFragmentTag = "";
                    switch (selectedNavigationItemID){
                        case R.id.main_navigation_drawer_interests :
                            newFragment = new UserInterests();
                            newFragmentTag = FRAGMENT_USER_INTERESTS;
                            break;
                        case R.id.main_navigation_drawer_data :
                            newFragment = new UserProfile();
                            break;
                        case R.id.main_navigation_drawer_settings :
                            newFragment = new Preferences();
                            newFragmentTag = getString(R.string.preferences_main_key);
                            break;
                        case R.id.main_navigation_drawer_lesson_level1 :
                            newFragment = new LessonList();
                            bundle.putInt(LessonList.LESSON_LEVEL, 1);
                            newFragment.setArguments(bundle);
                            setLastSelectedLessonLevel(1);
                            newFragmentTag = FRAGMENT_LESSON_LIST;
                            break;
                        case R.id.main_navigation_drawer_lesson_level2 :
                            newFragment = new LessonList();
                            bundle.putInt(LessonList.LESSON_LEVEL, 1);
                            newFragment.setArguments(bundle);
                            setLastSelectedLessonLevel(1);
                            newFragmentTag = FRAGMENT_LESSON_LIST;
                            break;
                        default:
                            return;
                    }
                    clearBackStack();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.main_activity_fragment_container, newFragment, newFragmentTag);
                    fragmentTransaction.commit();
                    topmostFragmentTag = newFragmentTag;

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

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
        hamburgerEnabled = true;

        navigationView = findViewById(R.id.main_navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        setLessonView();
    }

    @Override
    protected void onStart(){
        super.onStart();
        startAppTimestamp = System.currentTimeMillis();


        /*
        DateTime temp1 = DateTime.now();
        temp1 = temp1.minusDays(1);
        DateTime temp2 = DateTime.now();
        temp2 = temp2.minusDays(1).plusHours(1);
        recordAppUsageLog(new AppUsageLog(temp1.getMillis(), temp2.getMillis()));

        temp1 = temp1.minusDays(2);
        temp2 = temp2.minusDays(2);
        recordAppUsageLog(new AppUsageLog(temp1.getMillis(), temp2.getMillis()));

        temp1 = temp1.minusDays(1);
        temp2 = temp2.minusDays(1);
        recordAppUsageLog(new AppUsageLog(temp1.getMillis(), temp2.getMillis()));

        temp1 = temp1.minusDays(1);
        temp2 = temp2.minusDays(1);
        recordAppUsageLog(new AppUsageLog(temp1.getMillis(), temp2.getMillis()));

        temp1 = temp1.minusDays(4);
        temp2 = temp2.minusDays(4);
        recordAppUsageLog(new AppUsageLog(temp1.getMillis(), temp2.getMillis()));*/

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
                    Fragment lessonDescriptionFragment = new LessonDescription();
                    Bundle bundle = new Bundle();
                    bundle.putString(LessonDescription.BUNDLE_LESSON_KEY, descriptionLessonKey);

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.stay,
                            0, R.anim.slide_out_bottom
                    );
                    if (fragmentManager.getBackStackEntryCount() != 0 ){
                        String fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
                        fragmentTransaction.addToBackStack(fragmentTag);

                        Fragment resultsFragment = fragmentManager.findFragmentByTag(FRAGMENT_RESULTS);
                        if (resultsFragment != null && resultsFragment.isVisible()){
                            //when we are at the results page, we should always show the exception rule
                            bundle.putBoolean(LessonDescription.BUNDLE_SHOW_EXCEPTION, true);
                        }
                    } else {
                        fragmentTransaction.addToBackStack(topmostFragmentTag);
                    }
                    lessonDescriptionFragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.main_activity_fragment_container, lessonDescriptionFragment, FRAGMENT_LESSON_DESCRIPTION);
                    fragmentTransaction.commit();
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
        //not necessary for a lot of cases, but can't help it
        GUIUtils.hideKeyboard(getCurrentFocus());

        Fragment questionFragment = fragmentManager.findFragmentByTag(FRAGMENT_QUESTION);
        if (questionFragment != null && questionFragment.isVisible()){
            if (questionManager.isQuestionsStarted())
                questionManager.resetManager(QuestionManager.QUESTIONS);
            //we are just going back to the start of the review
            if (questionManager.isReviewStarted())
                questionManager.resetReviewMarker();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(questionFragment);
            fragmentTransaction.commit();

            if (textToSpeech != null){
                textToSpeech.shutdown();
                textToSpeech = null;
            }
        }

        Fragment resultsFragment = fragmentManager.findFragmentByTag(FRAGMENT_RESULTS);
        if (resultsFragment != null && resultsFragment.isVisible()){
            questionManager.resetManager(QuestionManager.REVIEW);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(resultsFragment);
            fragmentTransaction.commit();
        }

        if (fragmentManager.getBackStackEntryCount() == 1){
            switchActionBarUpButton();
        }

        super.onBackPressed();
    }

    @Override
    public void lessonListToLessonDetails(LessonData lessonData){
        Fragment fragment = new LessonDetails();
        Bundle bundle = new Bundle();
        bundle.putSerializable(LessonDetails.BUNDLE_LESSON_DATA, lessonData);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, FRAGMENT_LESSON_DETAILS);
        fragmentTransaction.addToBackStack(FRAGMENT_LESSON_LIST);
        fragmentTransaction.commit();
        switchActionBarUpButton();

    }

    @Override
    public void lessonListToReview(String key){

    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat,
                                           PreferenceScreen preferenceScreen) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment;
        String preferenceKey = preferenceScreen.getKey();
        if (preferenceKey.equals(getString(R.string.preferences_main_key))) {
            fragment=new Preferences();
        } else if (preferenceKey.equals(getString(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_screen_key))){
            fragment = new PreferencesDescriptionBeforeLessonWithExceptionRule();
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.stay,
                    0, R.anim.slide_out_bottom
            );
            preferenceScreen.getTitle();
        } else {
            return true;
        }
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, preferenceScreen.getKey());
        fragmentTransaction.addToBackStack(preferenceScreen.getKey());
        fragmentTransaction.commit();
        switchActionBarUpButton();
        return true;
    }

    @Override
    public void userInterestsToSearchInterests(){
        Fragment fragment = new SearchInterests();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, FRAGMENT_SEARCH_INTERESTS);
        fragmentTransaction.addToBackStack(FRAGMENT_USER_INTERESTS);
        fragmentTransaction.commit();
        switchActionBarUpButton();
    }

    private QuestionManager.QuestionManagerListener getQuestionManagerListener(){
        return new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                Fragment fragment;
                switch (questionData.getQuestionType()){
                    case QuestionTypeMappings.FILL_IN_BLANK_INPUT :
                        fragment = new Question_FillInBlank_Input();
                        break;
                    case QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE :
                        fragment = new Question_FillInBlank_MultipleChoice();
                        break;
                    case QuestionTypeMappings.MULTIPLE_CHOICE :
                        fragment = new Question_MultipleChoice();
                        break;
                    case QuestionTypeMappings.SENTENCE_PUZZLE :
                        fragment = new Question_Puzzle_Piece();
                        break;
                    case QuestionTypeMappings.TRUE_FALSE :
                        fragment = new Question_TrueFalse();
                        break;
                    case QuestionTypeMappings.SPELLING_SUGGESTIVE :
                        fragment = new Question_Spelling_Suggestive();
                        break;
                    case QuestionTypeMappings.SPELLING :
                        fragment = new Question_Spelling();
                        break;
                    case QuestionTypeMappings.TRANSLATE_WORD :
                        fragment = new Question_TranslateWord();
                        break;
                    case QuestionTypeMappings.CHAT_MULTIPLE_CHOICE :
                        fragment = new Question_Chat_MultipleChoice();
                        break;
                    case QuestionTypeMappings.CHAT :
                        fragment = new Question_Chat();
                        break;
                    default:
                        Log.d(TAG, "Could not find question type");
                        return;
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable(Question_General.BUNDLE_QUESTION_DATA,
                        questionData);
                bundle.putInt(Question_General.BUNDLE_QUESTION_NUMBER, questionNumber);
                bundle.putInt(Question_General.BUNDLE_QUESTION_TOTAL_QUESTIONS, totalQuestions);
                fragment.setArguments(bundle);
                //do not add to the back stack because we don't want the user going back to a question
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (firstQuestion){
                    if (questionManager.isQuestionsStarted())
                        fragmentTransaction.addToBackStack(FRAGMENT_LESSON_DETAILS);
                    else if (questionManager.isReviewStarted())
                        fragmentTransaction.addToBackStack(FRAGMENT_RESULTS);
                }
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right
                );
                fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, FRAGMENT_QUESTION);
                fragmentTransaction.commit();
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord) {
                Fragment fragment = new Results();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Results.BUNDLE_INSTANCE_RECORD, instanceRecord);
                fragment.setArguments(bundle);

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment questionFragment = fragmentManager.findFragmentByTag(FRAGMENT_QUESTION);
                if (questionFragment != null) {
                    fragmentTransaction.remove(questionFragment);
                }
                fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, FRAGMENT_RESULTS);
                fragmentTransaction.commit();

                //although we might use it in the review or subsequent questions,
                //we should prioritize the resources we can save?
                if (textToSpeech != null){
                    textToSpeech.shutdown();
                    textToSpeech = null;
                }
            }

            @Override
            public void onReviewFinished(){
                //just go back to the lesson details screen?
                fragmentManager.popBackStack(FRAGMENT_LESSON_DETAILS, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                Fragment resultsFragment = fragmentManager.findFragmentByTag(FRAGMENT_RESULTS);
                if (resultsFragment != null){
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.remove(resultsFragment);
                    fragmentTransaction.commit();
                }

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
        questionManager.saveResponse(answer, correct);
    }

    @Override
    public void onNextQuestion(){
        questionManager.nextQuestion(false);
    }

    private void setLastSelectedLessonLevel(int level){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.preferences_last_selected_lesson_level), level);
        editor.apply();
    }

    @Override
    public void resultsToLessonCategories(){
        questionManager.resetManager(QuestionManager.REVIEW);
        setLessonView();
    }

    @Override
    public void resultsToReview(InstanceRecord instanceRecord){
        questionManager.startReview(instanceRecord);
    }

    private void setLessonView(){
        clearBackStack();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //this is the ID used by the fragment
        //default (the user has never selected an item) is countries for now
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
        Fragment lessonListFragment = new LessonList();
        Bundle bundle = new Bundle();
        bundle.putInt(LessonList.LESSON_LEVEL, lastSelectedLessonLevel);
        lessonListFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, lessonListFragment, FRAGMENT_LESSON_LIST);
        fragmentTransaction.commit();
        topmostFragmentTag = FRAGMENT_LESSON_LIST;

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

    private void clearBackStack(){
        fragmentManager.popBackStack(topmostFragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

    private TextView getToolbarTextView(){
        int childCount = toolbar.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof TextView) {
                return (TextView)child;
            }
        }
        return null;
    }

    @Override
    public void setToolbarState(ToolbarState state){
        String toolbarTitle = state.getTitle();
        if (toolbarTitle.equals(ToolbarState.NO_TITLE_WITH_SPINNER)){
            addSpinnerAdapter();
        }
        if (!toolbarTitle.equals(ToolbarState.NO_CHANGE)){
            setToolbarTitle(toolbarTitle);
        }

        //icons
        searchIconVisible = state.searchIconVisible();
        descriptionLessonKey = state.getDescriptionLessonKey();
        if (descriptionLessonKey == null) {
            descriptionIconVisible = false;
        } else {
            descriptionIconVisible = lessonHierarchyViewer.layoutExists(descriptionLessonKey);

        }

        //this redraws the toolbar so the initial visibility is always false.
        //this is not the right way (nor the behavior I want) but this can come later...
        invalidateOptionsMenu();
    }

    private void addSpinnerAdapter(){
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
                    Fragment userInterestFragment = fragmentManager.findFragmentByTag(FRAGMENT_USER_INTERESTS);
                    if (userInterestFragment != null && userInterestFragment.isVisible()){
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
                        ((UserInterests)userInterestFragment).filterUserInterests(filter);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
    }

    private void setToolbarTitle(final String title){
        //getSupportActionBar should never be null but just in case
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null){
            toolbar.setTitle(title);
            return;
        }
        //don't do anything if the titles are the same
        if (actionBar.getTitle() != null &&
                actionBar.getTitle().toString().equals(title)) {
            return;
        }

        if (actionBar.getTitle().equals("") && toolbarSpinner.getVisibility() == View.VISIBLE &&
                title.equals(ToolbarState.NO_TITLE_WITH_SPINNER)){
            //resetting the spinner.
            //this doesn't trigger the onItemSelected listener
            //attached to the spinner
            toolbarSpinner.setSelection(0);
            return;
        }


        final View view = getToolbarTextView();
        //there won't be a TextView if the toolbar doesn't have a title
        if (view == null && toolbarSpinner.getVisibility() == View.GONE){
            actionBar.setTitle(title);
        } else {
            AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                    fadeIn.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));

                    if (title.equals(ToolbarState.NO_TITLE_WITH_SPINNER)){
                        toolbarSpinner.setVisibility(View.VISIBLE);
                        actionBar.setTitle("");

                        toolbarSpinner.startAnimation(fadeIn);
                    } else {
                        actionBar.setTitle(title);
                        //try grabbing the view again if it was null at first.
                        //since we are populating the textView, it should exist
                        if (view == null) {
                            TextView populatedView = getToolbarTextView();
                            if (populatedView != null) {
                                populatedView.startAnimation(fadeIn);
                            }
                        } else {
                            view.startAnimation(fadeIn);
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            if (view == null){
                toolbarSpinner.startAnimation(fadeOut);
            } else {
                view.startAnimation(fadeOut);
            }
        }

        if (toolbarSpinner.getVisibility() == View.VISIBLE){
            toolbarSpinner.setVisibility(View.GONE);
            //resetting the spinner so the next time,
            //it will start at the default location
            //this doesn't trigger the onItemSelected listener
            //attached to the spinner
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

    //we are assuming a one-activity multiple-fragment structure.
    //if we ever start another activity from this main activity,
    //we need to change this
    private void recordAppUsageLog(AppUsageLog log){
        Log.d(TAG, "called record app usage log");
        //we can index by year -> month so
        //we don't need to fetch the whole log every time
        long startTime = log.getStartTimeStamp();
        DateTime dateTime = new DateTime(startTime);
        int month = dateTime.getMonthOfYear();
        int year = dateTime.getYear();
        String key = AppUsageLog.formatKey(month, year);

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference logRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.APP_USAGE + "/" +
                userID + "/" +
                key
        );
        logRef.push().setValue(log);

    }

    @Override
    protected void onStop(){
        super.onStop();
        long endAppTimeStamp = System.currentTimeMillis();
        recordAppUsageLog(new AppUsageLog(startAppTimestamp, endAppTimeStamp));
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if (textToSpeech != null){
            textToSpeech.shutdown();
        }
    }
}
