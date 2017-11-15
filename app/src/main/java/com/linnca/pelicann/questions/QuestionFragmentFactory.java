package com.linnca.pelicann.questions;


import android.support.v4.app.Fragment;

public class QuestionFragmentFactory {
    public static Fragment getQuestionFragment(int questionType){
        Fragment fragment;
        switch (questionType) {
            case QuestionTypeMappings.FILL_IN_BLANK_INPUT:
                fragment = new Question_FillInBlank_Input();
                break;
            case QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE:
                fragment = new Question_FillInBlank_MultipleChoice();
                break;
            case QuestionTypeMappings.MULTIPLE_CHOICE:
                fragment = new Question_MultipleChoice();
                break;
            case QuestionTypeMappings.SENTENCE_PUZZLE:
                fragment = new Question_Puzzle_Piece();
                break;
            case QuestionTypeMappings.TRUE_FALSE:
                fragment = new Question_TrueFalse();
                break;
            case QuestionTypeMappings.SPELLING_SUGGESTIVE:
                fragment = new Question_Spelling_Suggestive();
                break;
            case QuestionTypeMappings.SPELLING:
                fragment = new Question_Spelling();
                break;
            case QuestionTypeMappings.TRANSLATE_WORD:
                fragment = new Question_TranslateWord();
                break;
            case QuestionTypeMappings.CHAT_MULTIPLE_CHOICE:
                fragment = new Question_Chat_MultipleChoice();
                break;
            case QuestionTypeMappings.CHAT:
                fragment = new Question_Chat();
                break;
            case QuestionTypeMappings.CHOOSE_CORRECT_SPELLING:
                fragment = new Question_Choose_Correct_Spelling();
                break;
            case QuestionTypeMappings.ACTIONS:
                fragment = new Question_Actions();
                break;
            default:
                fragment = null;
        }
        return fragment;
    }
}
