package com.example.ryomi.myenglish.questiongenerator.questions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.example.ryomi.myenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.myenglish.questiongenerator.Question;

public class MultipleChoiceQuestion extends Question{
	private String question;
	private int answerPosition;
	private List<String> choices; //正解1個+不正解2個
	private List<String> letters;
	
	public MultipleChoiceQuestion(
			String questionWithBlankMarker, String answer, List<String> wrongAnswers){
		super.questionType = QuestionTypeMappings.MULTIPLE_CHOICE;
		super.wikiDataID = wikiDataID;
		setChoices(answer, wrongAnswers);
		//まずsetChoicesをやる必要がある
		String questionWithBlank = addBlank(questionWithBlankMarker);
		this.question = questionWithBlank;
	}
	
	public String createQuestionGUI(){
		String gui = question + "\n";
		for (int i=0; i<3; i++){
			gui += letters.get(i) + ":" + choices.get(i) + "\n";
		}
		gui += "A B C どちらかを記入してください";
		return gui;
	}
	
	public boolean checkAnswer(String answer){
		int answerPos = letters.indexOf(answer);
		return (answerPos == answerPosition);
	}
	
	private void setLetters(){
		letters = new ArrayList<String>();
		letters.add("A");
		letters.add("B");
		letters.add("C");
	}
	
	private void setChoices(String answer, List<String> wrongAnswers){
		setLetters();
		Random r = new Random();
		answerPosition = r.nextInt(3);
		//just in case there are more than 3 wrong answers
		if (wrongAnswers.size() > 2)
			wrongAnswers.subList(2, wrongAnswers.size()).clear();
		
		Collections.shuffle(wrongAnswers);
		choices = new ArrayList<String>(wrongAnswers);
		choices.add(answerPosition, answer);
	}
	
	//空欄のサイズ問題ごとに調整する
	private String addBlank(String question){
		int maxSizeOfChoice = -1;
		for (String choice : choices){
			if (choice.length() > maxSizeOfChoice)
				maxSizeOfChoice = choice.length();
		}
		
		String blank = "";
		for (int i=0; i<maxSizeOfChoice; i++)
			blank += "_";
		return question.replace(BLANK_MARKER, blank);
	}
}
