package com.linnca.pelicann.questions;

class MaxNumberOfQuestionAttemptsHelper {
    private static final int UNLIMITED_ATTEMPTS = -1;
    interface UserGetter {
        int getMaxNumberOfQuestionAttemptsSetByUser();
    }
    
    private MaxNumberOfQuestionAttemptsHelper(){}
    
    static int getMaxNumberOfQuestionAttempts(QuestionData questionData, UserGetter userGetter){
        int maxPossibleAttempts = getMaxPossibleAttemptsPerQuestionType(questionData);
        int userMaxAttempts = userGetter.getMaxNumberOfQuestionAttemptsSetByUser();
        if (maxPossibleAttempts == UNLIMITED_ATTEMPTS){
            //the question allow unlimited attempts
            //so restrict the user's attempts to the number set in the preferences
            return userMaxAttempts;
        } else {
            if (userMaxAttempts <= maxPossibleAttempts){
                //the user has set a number of attempts less than the maximum possible attempts
                //so only allow the user to attempt the number of times he set in the preferences
                return userMaxAttempts;
            } else {
                //the max possible attempts is less than the number the user set in the preferences
                //so only allow the max possible attempts
                return maxPossibleAttempts;
            }
        }
    }
    private static int getMaxPossibleAttemptsPerQuestionType(QuestionData questionData){
        int choiceCt = questionData.getChoices() == null ?
                0 : questionData.getChoices().size();
        int questionType = questionData.getQuestionType();
        switch (questionType){
            case Question_Actions.QUESTION_TYPE :
                return UNLIMITED_ATTEMPTS;
            case Question_Chat.QUESTION_TYPE :
                return UNLIMITED_ATTEMPTS;
            case Question_Chat_MultipleChoice.QUESTION_TYPE :
                //if there's one choice, it's intentionally obvious
                return (choiceCt == 1 ? 1 : choiceCt - 1);
            case Question_ChooseCorrectSpelling.QUESTION_TYPE :
                //since the choices we create are dynamically created
                // (the choice list in the question data is initially empty),
                //make sure we update the question data
                return choiceCt - 1;
            case Question_FillInBlank_Input.QUESTION_TYPE :
                return UNLIMITED_ATTEMPTS;
            case Question_FillInBlank_MultipleChoice.QUESTION_TYPE :
                return (choiceCt == 1 ? 1 : choiceCt - 1);
            case Question_MultipleChoice.QUESTION_TYPE :
                return (choiceCt == 1 ? 1 : choiceCt - 1);
            case Question_SentencePuzzle.QUESTION_TYPE :
                return UNLIMITED_ATTEMPTS;
            case Question_Spelling.QUESTION_TYPE :
                return UNLIMITED_ATTEMPTS;
            case Question_Spelling_Suggestive.QUESTION_TYPE :
                return 1;
            case Question_TranslateWord.QUESTION_TYPE :
                return UNLIMITED_ATTEMPTS;
            case Question_TrueFalse.QUESTION_TYPE :
                return 1;
            default:
                return UNLIMITED_ATTEMPTS;
        }
    }
}
