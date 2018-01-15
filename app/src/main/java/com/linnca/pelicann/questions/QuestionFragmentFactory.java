package com.linnca.pelicann.questions;


import android.support.v4.app.Fragment;

import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;

public class QuestionFragmentFactory {
    public static Fragment getQuestionFragment(int questionType){
        switch (questionType) {
            case QuestionTypeMappings.FILLINBLANK_INPUT:
                return new Question_FillInBlank_Input();
                
            case QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE:
                return new Question_FillInBlank_MultipleChoice();
                
            case QuestionTypeMappings.MULTIPLECHOICE:
                return new Question_MultipleChoice();
                
            case QuestionTypeMappings.SENTENCEPUZZLE:
                return new Question_SentencePuzzle();
                
            case QuestionTypeMappings.TRUEFALSE:
                return new Question_TrueFalse();
                
            case QuestionTypeMappings.SPELLING_SUGGESTIVE:
                return new Question_Spelling_Suggestive();
                
            case QuestionTypeMappings.SPELLING:
                return new Question_Spelling();
                
            case QuestionTypeMappings.TRANSLATEWORD:
                return new Question_TranslateWord();
                
            case QuestionTypeMappings.CHAT_MULTIPLECHOICE:
                return new Question_Chat_MultipleChoice();
                
            case QuestionTypeMappings.CHAT:
                return new Question_Chat();
                
            case QuestionTypeMappings.CHOOSECORRECTSPELLING:
                return new Question_ChooseCorrectSpelling();
                
            case QuestionTypeMappings.ACTIONS:
                return new Question_Actions();
                
            case QuestionTypeMappings.INSTRUCTIONS:
                return new Question_Instructions();
                
            default:
                //TODO instead of null, return a no question found fragment
                return null;
        }
    }
}
