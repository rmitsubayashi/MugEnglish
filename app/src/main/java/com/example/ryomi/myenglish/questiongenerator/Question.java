package com.example.ryomi.myenglish.questiongenerator;

public abstract class Question {
	public static String BLANK_MARKER = "@blank@";
	//identifier for database
	protected int questionType;
	//hierarchy
	//if one question should come before another, it will be listed here
	// 0:should be the first question
	// 5:default
	// 10:should be last question
	protected int hierarchy;
	
	protected String wikiDataID;
	
	//問題文と答えは同一のデータ型と限らないから、
	//それぞれの問題の種類ごとに設定する
	//for now this will be a string to display on console
	public abstract String createQuestionGUI();
	//all answers will be passed as a string
	public abstract boolean checkAnswer(String answer);
	
	public Question(){
		this.hierarchy = 5;
	}
	
	public void setHierarchy(int hierarchy){
		//validate
		if (hierarchy < 0 || hierarchy > 10){
			return;
		}
		
		this.hierarchy = hierarchy;
	}
	
	public int getHierarchy(){
		return this.hierarchy;
	}
	
}
