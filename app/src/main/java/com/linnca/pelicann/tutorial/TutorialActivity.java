package com.linnca.pelicann.tutorial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.linnca.pelicann.R;
import com.linnca.pelicann.lessondetails.LessonDescription;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_a_GENDER;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.Question_General;
import com.linnca.pelicann.questions.Question_Spelling_Suggestive;
import com.linnca.pelicann.questions.Question_TrueFalse;

public class TutorialActivity extends AppCompatActivity implements
        Tutorial_LessonDetails.Tutorial_LessonDetailsListener,
        Tutorial_ChoosePerson.Tutorial_ChoosePersonListener,
        Tutorial_ConfirmPerson.Tutorial_ConfirmPersonListener,
        LessonDescription.LessonDescriptionListener,
        Question_General.QuestionListener
{
    private OnboardingPersonBundle selectedPerson;
    private FragmentManager fragmentManager;

    private boolean descriptionFeatureCovered = false;

    private Toolbar toolbar;

    private int questionMkr = 0;


    private final String FRAGMENT_LESSON_DETAILS = "lessonDetails";
    private final String FRAGMENT_LESSON_DESCRIPTION = "lessonDescription";
    private final String FRAGMENT_CHOOSE_PERSON = "choosePerson";
    private final String FRAGMENT_CONFIRM_PERSON = "confirmPerson";
    private final String FRAGMENT_QUESTION1 = "question1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        fragmentManager = getSupportFragmentManager();
        toolbar = findViewById(R.id.tutorial_tool_bar);
        setSupportActionBar(toolbar);

        appStartToChoosePerson();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tutorial_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tutorial_app_bar_description:
                lessonDetailsToLessonDescription();
                //we will never need it again
                item.setVisible(false);
                //show the up button
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                return true;
            case android.R.id.home:
                removeLessonDescriptionFragment();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setToolbarState(ToolbarState state){
        toolbar.setTitle(state.getTitle());
    }

    //initial screen when user starts the app
    private void appStartToChoosePerson(){
        Fragment fragment = new Tutorial_ChoosePerson();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.tutorial_activity_fragment_container, fragment, FRAGMENT_CHOOSE_PERSON);
        fragmentTransaction.commit();

        toolbar.setVisibility(View.GONE);
    }

    @Override
    public void choosePersonToConfirmPerson(OnboardingPersonBundle person){
        this.selectedPerson = person;
        Fragment fragment = new Tutorial_ConfirmPerson();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Tutorial_ConfirmPerson.BUNDLE_SELECTED_PERSON, person);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.tutorial_activity_fragment_container, fragment, FRAGMENT_CONFIRM_PERSON);
        fragmentTransaction.commit();
    }

    @Override
    public void confirmPersonToLessonDetails(){
        Fragment fragment = new Tutorial_LessonDetails();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Tutorial_LessonDetails.BUNDLE_SELECTED_PERSON, selectedPerson);
        bundle.putBoolean(Tutorial_LessonDetails.BUNDLE_DESCRIPTION_FEATURE_COVERED, descriptionFeatureCovered);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.tutorial_activity_fragment_container, fragment, FRAGMENT_LESSON_DETAILS);
        fragmentTransaction.commit();

        toolbar.setVisibility(View.VISIBLE);
    }

    //using the normal description fragment
    public void lessonDetailsToLessonDescription(){
        Fragment fragment = new LessonDescription();
        Bundle bundle = new Bundle();
        bundle.putString(LessonDescription.BUNDLE_LESSON_KEY, NAME_is_a_GENDER.KEY);
        //doesn't matter
        bundle.putBoolean(LessonDescription.BUNDLE_SHOW_EXCEPTION, true);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.stay,
                0, R.anim.slide_out_bottom
        );
        fragmentTransaction.replace(R.id.tutorial_activity_fragment_container, fragment, FRAGMENT_LESSON_DESCRIPTION);
        fragmentTransaction.commit();

        descriptionFeatureCovered = true;
    }

    //basically replace the description with the new fragment
    public void removeLessonDescriptionFragment(){
        Fragment fragment = new Tutorial_LessonDetails();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Tutorial_LessonDetails.BUNDLE_SELECTED_PERSON, selectedPerson);
        bundle.putBoolean(Tutorial_LessonDetails.BUNDLE_DESCRIPTION_FEATURE_COVERED, descriptionFeatureCovered);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.stay, R.anim.slide_out_bottom,
                0, 0
        );
        fragmentTransaction.replace(R.id.tutorial_activity_fragment_container, fragment, FRAGMENT_LESSON_DETAILS);
        fragmentTransaction.commit();
    }

    @Override
    public void onNextQuestion(){
        if (questionMkr == 0){
            lessonDetailsToQuestion1();
        } else if (questionMkr == 1){
            question1ToQuestion2();
        } else if (questionMkr == 2){
            question2ToQuestion3();
        } else if (questionMkr == 3){
            question3ToFinalExplanation();
        }

    }

    //don't need to do anything
    @Override
    public void onRecordResponse(String response, boolean correct){}

    @Override
    public void lessonDetailsToQuestion1(){
        questionMkr = 1;
        QuestionData questionData = new QuestionData(null, null, null, QuestionTypeMappings.SPELLING_SUGGESTIVE,
                "男性", null, "man", null, null, null);
        Fragment fragment = new Question_Spelling_Suggestive();
        Bundle bundle = new Bundle();
        bundle.putInt(Question_General.BUNDLE_QUESTION_NUMBER, questionMkr);
        bundle.putInt(Question_General.BUNDLE_QUESTION_TOTAL_QUESTIONS, 3);
        bundle.putSerializable(Question_General.BUNDLE_QUESTION_DATA, questionData);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.tutorial_activity_fragment_container, fragment, FRAGMENT_QUESTION1);
        fragmentTransaction.commit();
    }

    private void question1ToQuestion2(){
        questionMkr = 2;
        QuestionData questionData = new QuestionData(null, null, null, QuestionTypeMappings.SPELLING_SUGGESTIVE,
                "女性", null, "woman", null, null, null);
        Fragment fragment = new Question_Spelling_Suggestive();
        Bundle bundle = new Bundle();
        bundle.putInt(Question_General.BUNDLE_QUESTION_NUMBER, questionMkr);
        bundle.putInt(Question_General.BUNDLE_QUESTION_TOTAL_QUESTIONS, 3);
        bundle.putSerializable(Question_General.BUNDLE_QUESTION_DATA, questionData);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.tutorial_activity_fragment_container, fragment, FRAGMENT_QUESTION1);
        fragmentTransaction.commit();
    }

    private void question2ToQuestion3(){
        questionMkr = 3;
        String question = selectedPerson.getEnglishName() + " is a man";
        boolean answer = selectedPerson.getGender() == OnboardingPersonBundle.GENDER_MALE;
        String answerString = Question_TrueFalse.getTrueFalseString(answer);
        QuestionData questionData = new QuestionData(null, null, null, QuestionTypeMappings.TRUE_FALSE,
                question, null, answerString, null, null, null);
        Fragment fragment = new Question_TrueFalse();
        Bundle bundle = new Bundle();
        bundle.putInt(Question_General.BUNDLE_QUESTION_NUMBER, questionMkr);
        bundle.putInt(Question_General.BUNDLE_QUESTION_TOTAL_QUESTIONS, 3);
        bundle.putSerializable(Question_General.BUNDLE_QUESTION_DATA, questionData);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.tutorial_activity_fragment_container, fragment, FRAGMENT_QUESTION1);
        fragmentTransaction.commit();
    }

    private void question3ToFinalExplanation(){
    }

    public void finalExplanationToApp(){
        //set preference
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext());
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean(getResources().getString(R.string.preferences_first_time_key), false);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //make sure the user can't go back
        finish();
    }
}
