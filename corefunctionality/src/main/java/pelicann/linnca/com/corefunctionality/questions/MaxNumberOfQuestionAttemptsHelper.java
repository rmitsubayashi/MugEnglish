package pelicann.linnca.com.corefunctionality.questions;

public class MaxNumberOfQuestionAttemptsHelper {
    private static final int UNLIMITED_ATTEMPTS = -1;
    public interface UserGetter {
        int getMaxNumberOfQuestionAttemptsSetByUser();
    }
    
    private MaxNumberOfQuestionAttemptsHelper(){}
    
    public static int getMaxNumberOfQuestionAttempts(QuestionData questionData, UserGetter userGetter){
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
            case QuestionTypeMappings.ACTIONS :
                return UNLIMITED_ATTEMPTS;
            case QuestionTypeMappings.CHAT :
                return UNLIMITED_ATTEMPTS;
            case QuestionTypeMappings.CHAT_MULTIPLECHOICE :
                //if there's one choice, it's intentionally obvious
                return (choiceCt == 1 ? 1 : choiceCt - 1);
            case QuestionTypeMappings.CHOOSECORRECTSPELLING :
                //since the choices we create are dynamically created
                // (the choice list in the question data is initially empty),
                //make sure we update the question data
                return choiceCt - 1;
            case QuestionTypeMappings.FILLINBLANK_INPUT :
                return UNLIMITED_ATTEMPTS;
            case QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE :
                return (choiceCt == 1 ? 1 : choiceCt - 1);
            case QuestionTypeMappings.MULTIPLECHOICE :
                return (choiceCt == 1 ? 1 : choiceCt - 1);
            case QuestionTypeMappings.SENTENCEPUZZLE :
                return UNLIMITED_ATTEMPTS;
            case QuestionTypeMappings.SPELLING:
                return UNLIMITED_ATTEMPTS;
            case QuestionTypeMappings.SPELLING_SUGGESTIVE :
                return 1;
            case QuestionTypeMappings.TRANSLATEWORD:
                return UNLIMITED_ATTEMPTS;
            case QuestionTypeMappings.TRUEFALSE:
                return 1;
            default:
                return UNLIMITED_ATTEMPTS;
        }
    }
}
