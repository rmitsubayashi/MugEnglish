package com.example.ryomi.myenglish.questiongenerator.themes;

import com.example.ryomi.myenglish.connectors.EndpointConnectorReturnsXML;
import com.example.ryomi.myenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.questiongenerator.GrammarRules;
import com.example.ryomi.myenglish.questiongenerator.QGUtils;
import com.example.ryomi.myenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.myenglish.questiongenerator.Theme;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NAME_possessive_blood_type_is_BLOODTYPE extends Theme{
	//placeholders
	private final String personNamePH = "personName";
	private final String personNameForeignPH = "personNameForeign";
	private final String personNameENPH = "personNameEN";
	private final String bloodTypePH = "bloodType";
	
	private List<QueryResult> queryResults = new ArrayList<>();
	private class QueryResult {
		private String personID;
		private String personNameEN;
		private String personNameForeign;
		private String bloodType;
		
		private QueryResult(
				String personID,
				String personNameEN,
				String personNameForeign,
				String bloodType)
		{
			this.personID = personID;
			this.personNameEN = personNameEN;
			this.personNameForeign = personNameForeign;
			this.bloodType = bloodType;
		}
	}
	
	public NAME_possessive_blood_type_is_BLOODTYPE(EndpointConnectorReturnsXML connector, ThemeData data){
		super(connector, data);
		super.themeTopicCount = 3;
		super.questionsLeftToPopulate = 6;
		/*
		super.backupIDsOfTopics.add("Q211553"); //Ken Watanabe
		super.backupIDsOfTopics.add("Q22686"); //Donald Trump
		super.backupIDsOfTopics.add("Q9696");//John F Kennedy*/
		
	}
	
	protected String getSPARQLQuery(){
		//find person name and blood type
		return "SELECT ?" + personNamePH + " ?" + personNameForeignPH + " ?" + personNameENPH +
				" ?" + bloodTypePH + "Label " +
				"WHERE " +
				"{" +
				"    ?" + personNamePH + " wdt:P31 wd:Q5 . " + //is human
				"    ?" + personNamePH + " wdt:P1853 ?" + bloodTypePH + " . " + //has a blood type
				"    SERVICE wikibase:label {bd:serviceParam wikibase:language '" +
				     WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
				"    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
				"                           ?" + personNamePH + " rdfs:label ?" + personNameForeignPH + "} . " +
				"    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
				"                           ?" + personNamePH + " rdfs:label ?" + personNameENPH + "} . " + //English translation
				"    SERVICE wikibase:label { bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "'} . " + //everything else is in English
				  
				"    BIND (wd:%s as ?" + personNamePH + ") . " + //binding the ID of entity as ?person
				"} " +
				"LIMIT " + super.themeTopicCount;

	}
	
	protected void processResultsIntoClassWrappers() {
		Document document = super.documentOfTopics;
		NodeList allResults = document.getElementsByTagName("result");
		int resultLength = allResults.getLength();
		for (int i=0; i<resultLength; i++){
			Node head = allResults.item(i);
			String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, personNamePH);
			personID = QGUtils.stripWikidataID(personID);
			String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameENPH);
			String personNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameForeignPH);
			String bloodType = SPARQLDocumentParserHelper.findValueByNodeName(head, bloodTypePH+"Label");
			
			QueryResult qr = new QueryResult(personID, personNameEN, personNameForeign, bloodType);
			queryResults.add(qr);
		}
	}

	@Override
	protected void saveResultTopics(){
		for (QueryResult qr : queryResults){
			topics.add(qr.personNameForeign);
		}
	}

	
	protected void createQuestionsFromResults(){
		for (QueryResult qr : queryResults){
			QuestionData sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
			super.newQuestions.add(sentencePuzzleQuestion);

			QuestionData multipleChoiceQuestion = createMultipleChoiceQuestion(qr);
			super.newQuestions.add(multipleChoiceQuestion);
		}
		
	}
	
	private List<String> alternativeCorrectSentences(QueryResult qr){
		List<String> sentences = new ArrayList<>();
		sentences.add(this.NAME_possessive_blood_type_is_BLOODTYPE_EN_correct(qr));
		return sentences;
	}
	
	private String NAME_possessive_blood_type_is_BLOODTYPE_EN_correct(QueryResult qr){
		String possessiveName = GrammarRules.possessiveCaseOfSingularNoun(qr.personNameEN);
		String sentence = possessiveName + " blood type is " + qr.bloodType + ".";
		//no need since all names are capitalized?
		sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
		return sentence;
	}
	
	private String formatSentenceForeign(QueryResult qr){
		return qr.personNameForeign + "の血液型は" + qr.bloodType + "型です。";
	}
	
	//puzzle pieces for sentence puzzle question
	private List<String> puzzlePieces(QueryResult qr){
		List<String> pieces = new ArrayList<>();
		String possessiveName = GrammarRules.possessiveCaseOfSingularNoun(qr.personNameEN);
		pieces.add(possessiveName);
		pieces.add("blood type");
		pieces.add("is");
		pieces.add(qr.bloodType);
		return pieces;
	}

	private String puzzlePiecesAnswer(QueryResult qr){
		return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
	}
	
	//multiple choice question asks what the blood type of that person is
	//but not something like A, B, or AB
	//because that's impossible...
	private String multipleChoiceQuestion(QueryResult qr){
		String possessiveName = GrammarRules.possessiveCaseOfSingularNoun(qr.personNameEN);
		return "What is " + possessiveName + " blood type?";
	}
	
	private String multipleChoiceAnswer(QueryResult qr){
		return qr.bloodType;
	}
	
	private List<String> multipleChoiceWrongAnswers(){
		List<String> wrongAnswers = new ArrayList<>();
		wrongAnswers.add("blood");
		wrongAnswers.add("red");
		return wrongAnswers;
	}

	private QuestionData createSentencePuzzleQuestion(QueryResult qr){
		String question = this.formatSentenceForeign(qr);
		List<String> choices = this.puzzlePieces(qr);
		String answer = puzzlePiecesAnswer(qr);
		QuestionData data = new QuestionData();
		data.setId("");
		data.setThemeId(super.themeData.getId());
		data.setTopicId(qr.personID);
		data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
		data.setQuestion(question);
		data.setChoices(choices);
		data.setAnswer(answer);
		data.setAcceptableAnswers(null);
		data.setVocabulary(new ArrayList<String>());

		return data;
	}

	private QuestionData createMultipleChoiceQuestion(QueryResult qr){
		String question = this.multipleChoiceQuestion(qr);
		String answer = this.multipleChoiceAnswer(qr);
		List<String> choices = this.multipleChoiceWrongAnswers();
		//add correct answer to list of wrong answers
		choices.add(answer);

		QuestionData data = new QuestionData();
		data.setId("");
		data.setThemeId(super.themeData.getId());
		data.setTopicId(qr.personID);
		data.setQuestionType(QuestionTypeMappings.MULTIPLE_CHOICE);
		data.setQuestion(question);
		data.setChoices(choices);
		data.setAnswer(answer);
		data.setAcceptableAnswers(null);
		data.setVocabulary(new ArrayList<String>());

		return data;
	}
}
