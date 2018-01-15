package pelicann.linnca.com.corefunctionality.lessongeneration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GrammarRules {
	private GrammarRules(){}
	
	public static String uppercaseFirstLetterOfSentence(String sentence){
		char firstChar = sentence.charAt(0);
		char uppercaseFirstChar = Character.toUpperCase(firstChar);
		return uppercaseFirstChar + sentence.substring(1);
	}

	public static String articleBeforePosition(String positionName){
		//official title so no need to add an article
		if (Character.isUpperCase(positionName.charAt(0))){
			return positionName;
		}

		if (positionName.startsWith("member ")){
			return "a " + positionName;
		}

		if (positionName.startsWith("the ")){
			return positionName;
		}

		return "the " + positionName;
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

	//not sure if this is the rule
	public static String definiteArticleBeforeWar(String warName){
		if (warName.substring(0,4).equals("The ") || warName.substring(0,4).equals("the ")){
			return warName;
		}

		if (warName.equals("World War II") || warName.equals("World War I") || warName.equals("WWI") || warName.equals("WWII")){
			return warName;
		}

		return "the " + warName;
	}

	//I think it's guaranteed that awards should start with the?
	public static String definiteArticleBeforeAward(String awardName){
		String startsWithThe = awardName.substring(0,4);
		if (!startsWithThe.equals("The ") && !startsWithThe.equals("the ") )
			awardName = "the " + awardName;
		return awardName;
	}

	//a lot of exceptions
	public static String definiteArticleBeforeCountry(String countryName){
		//check if it already starts with the
		if (countryName.substring(0,4).equals("The ") || countryName.substring(0,4).equals("the ")){
			return countryName;
		}

		//on wikidata it's already the grenadines
		if (countryName.equals("Saint Vincent and Grenadines")){
			return "Saint Vincent and the Grenadines";
		}

		if (countryName.equals("Bahamas")){
			//capital T
			return "The Bahamas";
		}

		if (countryName.equals("Gambia")){
			//also capital T
			return "The Gambia";
		}

		if (countryName.equals("United Kingdom")){
			return "the United Kingdom";
		}

		Set<String> pluralExceptions = new HashSet<>();
		pluralExceptions.add("Saint Kitts and Nevis");
		pluralExceptions.add("Belarus");
		pluralExceptions.add("Laos");
		pluralExceptions.add("Honduras");
		pluralExceptions.add("Cyprus");
		pluralExceptions.add("Mauritius");
		pluralExceptions.add("Barbados");

		if (countryName.charAt(countryName.length()-1) == 's' && !pluralExceptions.contains(countryName)){
			if (countryName.contains(" of ")){
				//we have an exception with Seychelles and the Bahamas but it's not too relevant
				countryName = countryName.replace(" of ", " of the ");
			}
			return "the " + countryName;
		}

		if (countryName.contains(" of ")){
			return "the " + countryName;
		}

		return countryName;
	}
	
	public static String possessiveCaseOfSingularNoun(String singularNoun){
		return singularNoun + "'s";
	}

	//this is exception based.
	//in the future use a complete, trie based dictionary such as the one in
	//https://github.com/EamonNerbonne/a-vs-an/
	//returns a + the phrase
	public static String indefiniteArticleBeforeNoun(String phrase) {
		Pattern pattern;
		Matcher matcher;
		String word, lowercaseWord;

		if (phrase.length() == 0) {
			return "";
		}

		// Getting the first word
		pattern = Pattern.compile("(\\w+)\\s*.*");
		matcher = pattern.matcher(phrase);
		if(matcher.matches()) {
			word = matcher.group(1);
		} else {
			return "an " + phrase;
		}

		lowercaseWord = word.toLowerCase();

		// Specific start of words that should be preceded by 'an'
		String [] altCases = { "euler", "heir", "honest", "hono" };
		for (String altCase : altCases) {
			if (lowercaseWord.startsWith(altCase)) {
				return "an " + phrase;
			}
		}

		if (lowercaseWord.startsWith("hour") && !lowercaseWord.startsWith("houri")) {
			return "an " + phrase;
		}


		// Single letter word which should be preceded by 'an'
		if (lowercaseWord.length() == 1) {
			if ("aedhilmnorsx".contains(lowercaseWord)) {
				return "an " + phrase;
			} else {
				return "a " + phrase;
			}
		}

		// Capital words which should likely be preceded by 'an'
		if (word.matches("(?!FJO|[HLMNS]Y.|RY[EO]|SQU|(F[LR]?|[HL]|MN?|N|RH?|S[CHKLMNPTVW]?|X(YL)?)[AEIOU])[FHLMNRSX][A-Z]")) {
			return "an " + phrase;
		}

		// Special cases where a word that begins with a vowel should be preceded by 'a'
		String [] regexes = { "^e[uw]", "^onc?e\\b", "^uni([^nmd]|mo)", "^u[bcfhjkqrst][aeiou]" };

		for (String regex : regexes) {
			if (lowercaseWord.matches(regex+".*")) {
				return "a " + phrase;
			}
		}

		// Special capital words (UK, UN)
		if (word.matches("^U[NK][AIEO].*")) {
			return "a " + phrase;
		} else if (word.equals(word.toUpperCase())) {
			if ("aedhilmnorsx".contains(lowercaseWord.substring(0, 1)) ) {
				return "an " + phrase;
			} else {
				return "a " + phrase;
			}
		}

		// Basic method of words that begin with a vowel being preceded by 'an'
		if ("aeiou".contains(lowercaseWord.substring(0, 1))) {
			return "an " + phrase;
		}

		// Instances where y followed by specific letters is preceded by 'an'
		if (lowercaseWord.matches("^y(b[lor]|cl[ea]|fere|gg|p[ios]|rou|tt).*")) {
			return "an " + phrase;
		}

		return "a " + phrase;
	}

	public static String commasInASeries(List<String> series, String conjunction){
		if (series.size() == 0){
			return "";
		}
		else if (series.size() == 1){
			return series.get(0);
		} else if (series.size() == 2){
			return series.get(0) + " " + conjunction + " " + series.get(1);
		} else {
			String result = "";
			for (int i=0; i<series.size()-1; i++){
				result += series.get(i) + ", ";
			}
			result += conjunction + " " + series.get(series.size()-1);

			return result;
		}
	}
}
