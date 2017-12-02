package com.linnca.pelicann.questions;


import android.support.v4.app.Fragment;

public class QuestionFragmentFactory {
    public static Fragment getQuestionFragment(int questionType){
        switch (questionType) {
            case Question_FillInBlank_Input.QUESTION_TYPE:
                return new Question_FillInBlank_Input();
                
            case Question_FillInBlank_MultipleChoice.QUESTION_TYPE:
                return new Question_FillInBlank_MultipleChoice();
                
            case Question_MultipleChoice.QUESTION_TYPE:
                return new Question_MultipleChoice();
                
            case Question_SentencePuzzle.QUESTION_TYPE:
                return new Question_SentencePuzzle();
                
            case Question_TrueFalse.QUESTION_TYPE:
                return new Question_TrueFalse();
                
            case Question_Spelling_Suggestive.QUESTION_TYPE:
                return new Question_Spelling_Suggestive();
                
            case Question_Spelling.QUESTION_TYPE:
                return new Question_Spelling();
                
            case Question_TranslateWord.QUESTION_TYPE:
                return new Question_TranslateWord();
                
            case Question_Chat_MultipleChoice.QUESTION_TYPE:
                return new Question_Chat_MultipleChoice();
                
            case Question_Chat.QUESTION_TYPE:
                return new Question_Chat();
                
            case Question_ChooseCorrectSpelling.QUESTION_TYPE:
                return new Question_ChooseCorrectSpelling();
                
            case Question_Actions.QUESTION_TYPE:
                return new Question_Actions();
                
            case Question_Instructions.QUESTION_TYPE:
                return new Question_Instructions();
                
            default:
                return null;
        }
    }
}
