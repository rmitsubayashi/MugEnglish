package pelicann.linnca.com.corefunctionality.lessonquestions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;

//manages the execution of questions.
//this means that any new instances will be generated before calling this class

public class QuestionManager{
	private boolean questionsStarted = false;
	private LessonInstanceData lessonInstanceData = null;
	private List<QuestionData> questions;
	private String lessonKey = null;
	private final QuestionManagerListener questionManagerListener;
	private int questionMkr = -1;
	private int totalQuestions = 0;
	//store information about this run of the instance.
	//a user can run an instance multiple times,
	// getting multiple records for an instance
	private InstanceRecordManager instanceRecordManager;

	//save the missed questions for the instance review.
	//we can fetch them again from the question ID, but this prevents another connection to the database.
	//store in a set to prevent duplicates (we are adding every time we get a question attempt)
	private final Set<QuestionData> missedQuestionsForReview = new HashSet<>();

	public interface QuestionManagerListener {
		//not questionIndex but questionNumber (that we show the user)
		void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion);

		//arrayList so we can easily save it in a bundle
		void onQuestionsFinished(InstanceRecord instanceRecord,
                                 List<QuestionData> missedQuestions);
	}

	public QuestionManager(QuestionManagerListener listener){
		this.questionManagerListener = listener;
	}

	public void startQuestions(List<QuestionData> questions, LessonInstanceData data){
		if (questions == null || data == null)
			return;
		if(!questionsStarted) {
			questionsStarted = true;
			this.lessonInstanceData = data;
			totalQuestions = questions.size();
			this.lessonKey = data.getLessonKey();
			this.questions = questions;
			startNewInstanceRecord();
			nextQuestion();
		}
	}

	public boolean questionsStarted(){
        return questionsStarted;
    }

    //we need to know whether this is the first question
	//so we can put the previous fragment on the back stack.
	//the connection listener is to update the UI (whether it's the main activity
	// or a question fragment) when the connection fails
	public void nextQuestion(){
		//don't do anything if we haven't started anything
		if (!questionsStarted){
			return;
		}
		instanceRecordManager.setQuestionAttemptStartTimestamp();

		//if we are done with the questions
		if (questionMkr+1 == totalQuestions) {
			instanceRecordManager.markInstanceCompleted();
			questionManagerListener.onQuestionsFinished(instanceRecordManager.getInstanceRecord(),
					new ArrayList<>(missedQuestionsForReview));
			//the user will not be able to go back and redo this question again,
			// so we can reset everything
			resetManager();
			return;
		}
		//next question
		questionMkr++;
		questionManagerListener.onNextQuestion(questions.get(questionMkr), questionMkr+1, totalQuestions, questionMkr==0);

	}

	public void saveResponse(String response, Boolean correct){
		instanceRecordManager.addQuestionAttempt(questions.get(questionMkr).getId(), response, correct);

		//save incorrect responses for when the user reviews
		if (!correct){
			//the user may have multiple question attempts per question.
			//the set prevents duplicate questions
			missedQuestionsForReview.add(questions.get(questionMkr));
		}

	}

	private void startNewInstanceRecord(){
		instanceRecordManager = new InstanceRecordManager(lessonInstanceData.getId(),
				lessonKey);
	}

	public void resetManager(){
		questionsStarted = false;
		lessonInstanceData = null;
		lessonKey = null;
		questions = null;
		questionMkr = -1;
		totalQuestions = 0;
		instanceRecordManager = null;
		missedQuestionsForReview.clear();
	}
}
