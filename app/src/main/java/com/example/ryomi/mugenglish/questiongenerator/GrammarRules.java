package com.example.ryomi.mugenglish.questiongenerator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	//this is exception based.
	//in the futrure use a complete, trie based dictionary such as the one in
	//https://github.com/EamonNerbonne/a-vs-an/
	public static String indefiniteArticleBeforeNoun(String phrase) {
		Pattern pattern;
		Matcher matcher;
		String word, lowercaseWord;

		if (phrase.length() == 0) {
			return "a";
		}

		// Getting the first word
		pattern = Pattern.compile("(\\w+)\\s*.*");
		matcher = pattern.matcher(phrase);
		if(matcher.matches()) {
			word = matcher.group(1);
		} else {
			return "an";
		}

		lowercaseWord = word.toLowerCase();

		// Specific start of words that should be preceded by 'an'
		String [] altCases = { "euler", "heir", "honest", "hono" };
		for (String altCase : altCases) {
			if (lowercaseWord.startsWith(altCase)) {
				return "an";
			}
		}

		if (lowercaseWord.startsWith("hour") && !lowercaseWord.startsWith("houri")) {
			return "an";
		}


		// Single letter word which should be preceded by 'an'
		if (lowercaseWord.length() == 1) {
			if ("aedhilmnorsx".contains(lowercaseWord)) {
				return "an";
			} else {
				return "a";
			}
		}

		// Capital words which should likely be preceded by 'an'
		if (word.matches("(?!FJO|[HLMNS]Y.|RY[EO]|SQU|(F[LR]?|[HL]|MN?|N|RH?|S[CHKLMNPTVW]?|X(YL)?)[AEIOU])[FHLMNRSX][A-Z]")) {
			return "an";
		}

		// Special cases where a word that begins with a vowel should be preceded by 'a'
		String [] regexes = { "^e[uw]", "^onc?e\\b", "^uni([^nmd]|mo)", "^u[bcfhjkqrst][aeiou]" };

		for (String regex : regexes) {
			if (lowercaseWord.matches(regex+".*")) {
				return "a";
			}
		}

		// Special capital words (UK, UN)
		if (word.matches("^U[NK][AIEO].*")) {
			return "a";
		} else if (word.equals(word.toUpperCase())) {
			if ("aedhilmnorsx".contains(lowercaseWord.substring(0, 1)) ) {
				return "an";
			} else {
				return "a";
			}
		}

		// Basic method of words that begin with a vowel being preceded by 'an'
		if ("aeiou".contains(lowercaseWord.substring(0, 1))) {
			return "an";
		}

		// Instances where y followed by specific letters is preceded by 'an'
		if (lowercaseWord.matches("^y(b[lor]|cl[ea]|fere|gg|p[ios]|rou|tt).*")) {
			return "an";
		}

		return "a";
	}
}
