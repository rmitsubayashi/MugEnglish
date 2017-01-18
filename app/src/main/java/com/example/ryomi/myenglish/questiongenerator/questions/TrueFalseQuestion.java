package com.example.ryomi.myenglish.questiongenerator.questions;

import com.example.ryomi.myenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.myenglish.questiongenerator.Question;

public class TrueFalseQuestion extends Question{
	private String question;
	private boolean answer;
	public TrueFalseQuestion(String question, boolean answer){
		super.questionType = QuestionTypeMappings.TRUE_FALSE;
		this.question = question;
		this.answer = answer;
	}
	
	public String createQuestionGUI(){
		return this.question + "\nA:本当\nB:嘘";
	}
	
	public boolean checkAnswer(String answer){
		return (answer.equals("A") && this.answer) || (answer.equals("B") && !this.answer);
	}
}
