package com.example.ryomi.myenglish.questiongenerator;

public class GrammarRules {
	
	
	public static String uppercaseFirstLetterOfSentence(String sentence){
		char firstChar = sentence.charAt(0);
		char uppercaseFirstChar = Character.toUpperCase(firstChar);
		return uppercaseFirstChar + sentence.substring(1);
	}
	
	public static String definiteArticleBeforeSchoolName(String schoolName){
		//check if the school name already starts with 'the'
		if (schoolName.substring(0,4).equals("The ") || schoolName.substring(0,4).equals("the ")){
			return schoolName;
		}
		
		if (schoolName.contains(" of ") || schoolName.contains(" Of ")){
			schoolName = "the " + schoolName;
		}
		
		return schoolName;
	}
	
	public static String possessiveCaseOfSingularNoun(String singularNoun){
		return singularNoun + "'s";
	}
}
