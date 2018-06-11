package com.linnca.pelicann.mainactivity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceScreen;

import com.linnca.pelicann.R;
import com.linnca.pelicann.lessoncategorylist.LessonCategoryList;
import com.linnca.pelicann.lessonscript.LessonScript;
import com.linnca.pelicann.preferences.Preferences;
import com.linnca.pelicann.questions.QuestionFragmentFactory;
import com.linnca.pelicann.questions.QuestionFragmentInterface;
import com.linnca.pelicann.questions.VerbQuestionStart;
import com.linnca.pelicann.results.Results;
import com.linnca.pelicann.results.ReviewResults;
import com.linnca.pelicann.results.VerbQuestionResults;
import com.linnca.pelicann.searchinterests.SearchInterests;
import com.linnca.pelicann.userinterests.UserInterests;
import com.linnca.pelicann.userprofile.UserProfile_HoursStudied;

import java.io.Serializable;
import java.util.List;

import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessonlist.LessonCategory;
import pelicann.linnca.com.corefunctionality.lessonquestions.InstanceRecord;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.WordDefinitionPair;

//manages all the fragment transactions
class MainActivityFragmentManager {
    private FragmentManager fragmentManager;

    MainActivityFragmentManager(FragmentManager fm){
        this.fragmentManager = fm;
    }

    boolean isVisible(String fragmentTag){
        Fragment fragmentToFind = fragmentManager.findFragmentByTag(fragmentTag);
        return fragmentToFind != null && fragmentToFind.isVisible();
    }

    private Fragment getFragment(String fragmentTag){
        return fragmentManager.findFragmentByTag(fragmentTag);
    }

    boolean fragmentAfterBackPressIsRoot(){
        return fragmentManager.getBackStackEntryCount() == 1;
    }

    void rootToUserInterests(Database db){
        clearBackStack();
        Fragment fragment = new UserInterests();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MainActivity.BUNDLE_DATABASE, db);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, UserInterests.TAG);
        fragmentTransaction.commit();
    }

    void rootToVerbQuestionStart(){
        clearBackStack();
        Fragment fragment = new VerbQuestionStart();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, VerbQuestionStart.TAG);
        fragmentTransaction.commit();
    }

    void rootToUserProfile(Database db){
        clearBackStack();
        Fragment fragment = new UserProfile_HoursStudied();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MainActivity.BUNDLE_DATABASE, db);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, UserProfile_HoursStudied.TAG);
        fragmentTransaction.commit();
    }

    void rootToSettings(Database db){
        clearBackStack();
        Fragment fragment = new Preferences();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MainActivity.BUNDLE_DATABASE, db);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, Preferences.TAG);
        fragmentTransaction.commit();
    }

    void lessonCategoryListToLessonScript(LessonCategory lessonCategory, Database db){
        Fragment fragment = new LessonScript();
        Bundle bundle = new Bundle();
        bundle.putSerializable(LessonScript.BUNDLE_LESSON_CATEGORY, lessonCategory);
        bundle.putSerializable(MainActivity.BUNDLE_DATABASE, db);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, LessonScript.TAG);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    void rootToLessonCategoryList(Database db){
        clearBackStack();
        Fragment fragment = new LessonCategoryList();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MainActivity.BUNDLE_DATABASE, db);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, LessonCategoryList.TAG);
        fragmentTransaction.commit();
    }

    void userInterestsToSearchInterests(Database db){
        Fragment fragment = new SearchInterests();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MainActivity.BUNDLE_DATABASE, db);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, SearchInterests.TAG);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    void notQuestionFragmentToQuestion(QuestionData questionData, int questionNumber, int totalQuestionCount){
        if (questionData == null){
            toErrorPage();
            return;
        }

        Fragment fragment = QuestionFragmentFactory.getQuestionFragment(questionData.getQuestionType());
        Bundle bundle = new Bundle();
        bundle.putSerializable(QuestionFragmentInterface.BUNDLE_QUESTION_DATA,
                questionData);
        bundle.putInt(QuestionFragmentInterface.BUNDLE_QUESTION_NUMBER, questionNumber);
        bundle.putInt(QuestionFragmentInterface.BUNDLE_QUESTION_TOTAL_QUESTIONS, totalQuestionCount);
        fragment.setArguments(bundle);
        //do not add to the back stack because we don't want the user going back to a question
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        //sliding animation from one question to the next
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right
        );
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, QuestionFragmentInterface.TAG);
        fragmentTransaction.commit();
    }

    void questionToQuestion(QuestionData questionData, int questionNumber, int totalQuestionCount){
        if (questionData == null){
            toErrorPage();
            return;
        }

        Fragment fragment = QuestionFragmentFactory.getQuestionFragment(questionData.getQuestionType());
        Bundle bundle = new Bundle();
        bundle.putSerializable(QuestionFragmentInterface.BUNDLE_QUESTION_DATA,
                questionData);
        bundle.putInt(QuestionFragmentInterface.BUNDLE_QUESTION_NUMBER, questionNumber);
        bundle.putInt(QuestionFragmentInterface.BUNDLE_QUESTION_TOTAL_QUESTIONS, totalQuestionCount);
        fragment.setArguments(bundle);
        //do not add to the back stack because we don't want the user going back to a question
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //sliding animation from one question to the next
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right
        );
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, QuestionFragmentInterface.TAG);
        fragmentTransaction.commit();
    }

    void fromQuestionToFragment(){
        Fragment questionFragment = getFragment(QuestionFragmentInterface.TAG);
        if (questionFragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(questionFragment);
            fragmentTransaction.commit();
        }
    }

    void questionToResults(Database db, InstanceRecord instanceRecord){
        //saving the instance data handled in the results fragment (results manager)
        Fragment fragment = new Results();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Results.BUNDLE_INSTANCE_RECORD, instanceRecord);
        bundle.putSerializable(MainActivity.BUNDLE_DATABASE, db);
        fragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment questionFragment = fragmentManager.findFragmentByTag(QuestionFragmentInterface.TAG);
        if (questionFragment != null) {
            fragmentTransaction.remove(questionFragment);
            fragmentTransaction.commit();
            fragmentTransaction = fragmentManager.beginTransaction();
            //this would remove the lesson details -> question transaction
            fragmentManager.popBackStack();
        }
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, Results.TAG);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    void questionToVerbQuestionResults(int correctCt, List<WordDefinitionPair> wrongWords){
        Fragment fragment = new VerbQuestionResults();
        Bundle bundle = new Bundle();
        bundle.putSerializable(VerbQuestionResults.BUNDLE_CORRECT_CT, correctCt);
        bundle.putSerializable(VerbQuestionResults.BUNDLE_WRONG_WORDS, (Serializable)wrongWords);
        fragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment questionFragment = fragmentManager.findFragmentByTag(QuestionFragmentInterface.TAG);
        if (questionFragment != null) {
            fragmentTransaction.remove(questionFragment);
            fragmentTransaction.commit();
            fragmentTransaction = fragmentManager.beginTransaction();
            //this would remove the lesson details -> question transaction
            fragmentManager.popBackStack();
        }
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, VerbQuestionResults.TAG);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    void questionToReviewResults(){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment questionFragment = fragmentManager.findFragmentByTag(QuestionFragmentInterface.TAG);
        if (questionFragment != null) {
            fragmentTransaction.remove(questionFragment);
            fragmentTransaction.commit();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentManager.popBackStack();
        }
        Fragment fragment = new ReviewResults();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, ReviewResults.TAG);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    void fragmentToLessonDescription(String descriptionLessonKey){
        Fragment fragment = new LessonScript();
        Bundle bundle = new Bundle();
        //bundle.putString(LessonScript.BUNDLE_LESSON_KEY, descriptionLessonKey);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.stay,
                0, R.anim.slide_out_bottom
        );
        if (fragmentManager.getBackStackEntryCount() != 0 ){
            String fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
            fragmentTransaction.addToBackStack(fragmentTag);

            Fragment resultsFragment = fragmentManager.findFragmentByTag(Results.TAG);
            if (resultsFragment != null && resultsFragment.isVisible()){
                //when we are at the results page, we should always show the exception rule
                //bundle.putBoolean(LessonScript.BUNDLE_SHOW_EXCEPTION, true);
            }
        } else {
            fragmentTransaction.addToBackStack(null);
        }
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, LessonScript.TAG);
        fragmentTransaction.commit();
    }

    void resultsToLessonDetails(Database db){
        /*
        //removes the lessonDetails -> results transaction
        fragmentManager.popBackStack();
        //removes the lessonList -> lessonDetails transaction
        fragmentManager.popBackStack();
        Fragment fragment = new LessonDetails();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MainActivity.BUNDLE_DATABASE, db);
        bundle.putSerializable(LessonDetails.BUNDLE_LESSON_DATA, lessonData);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, LessonDetails.TAG);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();*/
    }

    boolean toPreferenceScreen(Context context, PreferenceScreen preferenceScreen){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment;
        String preferenceKey = preferenceScreen.getKey();
        if (preferenceKey.equals(
                context.getString(R.string.preferences_main_key))) {
            fragment = new Preferences();
        } else {
            return false;
        }
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment, preferenceScreen.getKey());
        fragmentTransaction.addToBackStack(preferenceScreen.getKey());
        fragmentTransaction.commit();
        return true;
    }

    private void toErrorPage(){

    }

    private void clearBackStack(){
        //passing null as the name clears the whole back stack
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }


}
