package com.linnca.pelicann.gui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.database2classmappings.QuestionTypeMappings;
import com.linnca.pelicann.db.datawrappers.InstanceRecord;
import com.linnca.pelicann.db.datawrappers.LessonData;
import com.linnca.pelicann.db.datawrappers.LessonInstanceData;
import com.linnca.pelicann.db.datawrappers.QuestionData;
import com.linnca.pelicann.questiongenerator.LessonHierarchyViewer;
import com.linnca.pelicann.questionmanager.QuestionManager;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        Preferences.LogoutListener,
        LessonList.LessonListListener,
        UserInterests.UserInterestListener,
        Question_General.QuestionListener,
        LessonDetails.LessonDetailsListener,
        Results.ResultsListener
{
    private final String TAG = "MainActivity";
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;
    private boolean navigationItemSelected = false;
    private CharSequence selectedNavigationItemTitle;
    private int selectedNavigationItemID = -1;

    private QuestionManager questionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        questionManager = new QuestionManager(getQuestionManagerListener());

        drawerLayout = findViewById(R.id.activity_main);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,drawerLayout, toolbar, R.string.lesson_list_navigation_drawer_open, R.string.lesson_list_navigation_drawer_close){

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //if the user selected an item, do this
                //(this is called after the animation finishes
                // so better ux)
                if (navigationItemSelected){
                    Fragment newFragment;
                    Bundle bundle = new Bundle();
                    switch (selectedNavigationItemID){
                        case R.id.main_navigation_drawer_interests :
                            newFragment = new UserInterests();
                            break;
                        case R.id.main_navigation_drawer_data :
                            newFragment = new UserProfile();
                            break;
                        case R.id.main_navigation_drawer_settings :
                            newFragment = new Preferences();
                            break;
                        case R.id.main_navigation_drawer_lesson_work :
                            newFragment = new LessonList();
                            bundle.putInt(LessonList.LESSON_CATEGORY_ID, LessonHierarchyViewer.ID_WORK);
                            newFragment.setArguments(bundle);
                            setLastSelectedLessonCategory(LessonHierarchyViewer.ID_WORK);
                            break;
                        case R.id.main_navigation_drawer_lesson_countries :
                            newFragment = new LessonList();
                            bundle.putInt(LessonList.LESSON_CATEGORY_ID, LessonHierarchyViewer.ID_COUNTRIES);
                            newFragment.setArguments(bundle);
                            setLastSelectedLessonCategory(LessonHierarchyViewer.ID_COUNTRIES);
                            break;
                        default:
                            return;
                    }
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.main_activity_fragment_container, newFragment);
                    fragmentTransaction.commit();

                    toolbar.setTitle(selectedNavigationItemTitle);

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

        navigationView = findViewById(R.id.main_navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        setLessonView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem){
        menuItem.setChecked(true);
        navigationItemSelected = true;
        checkNavigationItem(menuItem.getItemId());
        selectedNavigationItemTitle = menuItem.getTitle();
        //we want the action to trigger after the drawer closes
        //(for better ux) so close the drawer here
        //and set the action on the onCloseDrawer listener for the drawerLayout
        drawerLayout.closeDrawer(navigationView, true);
        return false;
    }

    @Override
    public void logout(){
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }
        if (FirebaseAuth.getInstance().getCurrentUser().getProviderId().equals(FirebaseAuthProvider.PROVIDER_ID))
            return;
        FirebaseAuth.getInstance().signOut();
        /*
        //remove this fragment
        getFragmentManager().popBackStack();
        //send user to main page(theme list)?
        Intent intent =new Intent(this, LessonList.class);
        startActivity(intent);
        finish();*/
    }

    @Override
    public void lessonListToLessonDetails(LessonData lessonData, int backgroundColor){
        Fragment fragment = new LessonDetails();
        Bundle bundle = new Bundle();
        bundle.putSerializable(LessonDetails.BUNDLE_LESSON_DATA, lessonData);
        bundle.putInt(LessonDetails.BUNDLE_BACKGROUND_COLOR, backgroundColor);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void userInterestsToSearchInterests(){
        Fragment fragment = new SearchInterests();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private QuestionManager.QuestionManagerListener getQuestionManagerListener(){
        return new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData) {
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
                    default:
                        return;
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable(Question_General.BUNDLE_QUESTION_DATA,
                        questionData);
                fragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right
                );
                fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment);
                fragmentTransaction.commit();
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord) {
                Fragment fragment = new Results();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Results.BUNDLE_INSTANCE_RECORD, instanceRecord);
                fragment.setArguments(bundle);

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment);
                fragmentTransaction.commit();
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
        questionManager.nextQuestion();
    }

    private void setLastSelectedLessonCategory(int lessonCategoryID){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.preferences_last_selected_lesson_category), lessonCategoryID);
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //this is the ID used by the fragment
        //default (the user has never selected an item) is countries for now
        int lastSelectedLessonCategoryID = preferences.getInt(getString(R.string.preferences_last_selected_lesson_category), LessonHierarchyViewer.ID_COUNTRIES);
        //finding the ID of the navigation drawer
        int navigationDrawerItemIDToSelect;
        switch (lastSelectedLessonCategoryID){
            case LessonHierarchyViewer.ID_COUNTRIES :
                navigationDrawerItemIDToSelect = R.id.main_navigation_drawer_lesson_countries;
                break;
            case LessonHierarchyViewer.ID_WORK :
                navigationDrawerItemIDToSelect = R.id.main_navigation_drawer_lesson_work;
                break;
            default:
                navigationDrawerItemIDToSelect = R.id.main_navigation_drawer_lesson_countries;
        }
        checkNavigationItem(navigationDrawerItemIDToSelect);
        Fragment lessonListFragment = new LessonList();
        Bundle bundle = new Bundle();
        bundle.putInt(LessonList.LESSON_CATEGORY_ID, lastSelectedLessonCategoryID);
        lessonListFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, lessonListFragment);
        fragmentTransaction.commit();
    }

    private void checkNavigationItem(int id){
        if (selectedNavigationItemID != -1){
            navigationView.getMenu().findItem(selectedNavigationItemID).setChecked(false);
        }
        navigationView.getMenu().findItem(id).setChecked(true);
        selectedNavigationItemID = id;
    }
}
