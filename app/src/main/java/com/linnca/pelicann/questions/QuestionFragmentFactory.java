package com.linnca.pelicann.questions;


import android.support.v4.app.Fragment;

public class QuestionFragmentFactory {
    public static Fragment getQuestionFragment(int questionType){
        Fragment fragment;
        switch (questionType) {
            case Question_FillInBlank_Input.QUESTION_TYPE:
                fragment = new Question_FillInBlank_Input();
                break;
            case Question_FillInBlank_MultipleChoice.QUESTION_TYPE:
                fragment = new Question_FillInBlank_MultipleChoice();
                break;
            case Question_MultipleChoice.QUESTION_TYPE:
                fragment = new Question_MultipleChoice();
                break;
            case Question_SentencePuzzle.QUESTION_TYPE:
                fragment = new Question_SentencePuzzle();
                break;
            case Question_TrueFalse.QUESTION_TYPE:
                fragment = new Question_TrueFalse();
                break;
            case Question_Spelling_Suggestive.QUESTION_TYPE:
                fragment = new Question_Spelling_Suggestive();
                break;
            case Question_Spelling.QUESTION_TYPE:
                fragment = new Question_Spelling();
                break;
            case Question_TranslateWord.QUESTION_TYPE:
                fragment = new Question_TranslateWord();
                break;
            case Question_Chat_MultipleChoice.QUESTION_TYPE:
                fragment = new Question_Chat_MultipleChoice();
                break;
            case Question_Chat.QUESTION_TYPE:
                fragment = new Question_Chat();
                break;
            case Question_ChooseCorrectSpelling.QUESTION_TYPE:
                fragment = new Question_ChooseCorrectSpelling();
                break;
            case Question_Actions.QUESTION_TYPE:
                fragment = new Question_Actions();
                break;
            default:
                fragment = null;
        }
        return fragment;
    }
}
