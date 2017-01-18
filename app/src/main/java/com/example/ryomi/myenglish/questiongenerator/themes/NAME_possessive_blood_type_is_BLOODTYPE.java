package com.example.ryomi.myenglish.questiongenerator.themes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.ryomi.myenglish.connectors.EndpointConnector;
import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.db.database2classmappings.ThemeMappings;
import com.example.ryomi.myenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.myenglish.questiongenerator.GrammarRules;
import com.example.ryomi.myenglish.questiongenerator.Question;
import com.example.ryomi.myenglish.questiongenerator.Theme;
import com.example.ryomi.myenglish.questiongenerator.questions.MultipleChoiceQuestion;
import com.example.ryomi.myenglish.questiongenerator.questions.SentencePuzzleQuestion;

public class NAME_possessive_blood_type_is_BLOODTYPE extends Theme{
	//placeholders
	private final String personNamePH = "personName";
	private final String personNameForeignPH = "personNameForeign";
	private final String personNameENPH = "personNameEN";
	private final String bloodTypePH = "bloodType";
	
	private List<QueryResult> queryResults;
	private class QueryResult {
		private String personNameEN;
		private String personNameForeign;
		private String bloodType;
		
		private QueryResult(
				String personNameEN,
				String personNameForeign,
				String bloodType)
		{
			this.personNameEN = personNameEN;
			this.personNameForeign = personNameForeign;
			this.bloodType = bloodType;
		}
	}
	
	public NAME_possessive_blood_type_is_BLOODTYPE(EndpointConnector connector){
		super(connector);
		super.themeID = ThemeMappings.NAME_possessive_blood_type_is_BLOODTYPE;
		super.name = "~の";
		super.description = "\"~'s\" つまり「~の」を勉強しましょう！あの有名人の血液型は？？";
		super.themeTopicCount = 3;
		super.wikiDataIDPH = this.personNamePH;
		queryResults = new ArrayList<QueryResult>();
		super.backupIDsOfTopics.add("Q211553"); //Ken Watanabe
		super.backupIDsOfTopics.add("Q22686"); //Donald Trump
		super.backupIDsOfTopics.add("Q9696");//John F Kennedy
		
	}
	
	protected String getSPARQLQuery(){
		//find person name and blood type
		String query = "SELECT ?" + personNamePH + " ?" + personNameForeignPH + " ?" + personNameENPH +
				" ?" + bloodTypePH + "Label " +
				"WHERE " +
				"{" +
				"    ?" + personNamePH + " wdt:P31 wd:Q5 . " + //is human
				"    ?" + personNamePH + " wdt:P1853 ?" + bloodTypePH + "build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res " + //has a blood type
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
		
		return query;
	}
	
	protected void populateResults(Set<String> wikidataIDs) throws Exception{
		for (String entityID : wikidataIDs){
			String query = super.addEntityToQuery(entityID);
			Document resultDOM = connector.fetchDOMFromGetRequest(query);
			this.addResultsToMainDocument(resultDOM);
			if (this.countResults(documentOfTopics) >= themeTopicCount){
				break;
			}
		}
	}
	
	protected void processResultsIntoClassWrappers() {
		Document document = super.documentOfTopics;
		NodeList allResults = document.getElementsByTagName("result");
		int resultLength = allResults.getLength();
		for (int i=0; i<resultLength; i++){
			Node head = allResults.item(i);
			String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameENPH);
			String personNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameForeignPH);
			String bloodType = SPARQLDocumentParserHelper.findValueByNodeName(head, bloodTypePH+"Label");
			
			QueryResult qr = new QueryResult(personNameEN, personNameForeign, bloodType);
			queryResults.add(qr);
		}
	}
	
	protected void createQuestionsFromResults(){
		for (QueryResult qr : queryResults){
			String statementForeign = this.formatSentenceForeign(qr);
			List<String> puzzlePiecesEN = this.puzzlePiecesEN(qr);
			List<String> allCorrectSentencesEN = this.allCorrectSentencesEN(qr);
			Question q = new SentencePuzzleQuestion(statementForeign, allCorrectSentencesEN, puzzlePiecesEN);
			super.questions.add(q);
			
			String multipleChoiceQuestionEN = this.multipleChoiceQuestionEN(qr);
			String multipleChoiceAnswerEN = this.multipleChoiceAnswerEN();
			List<String> multipleChoiceWrongAnswersEN = this.multipleChoiceWrongAnswersEN();
			Question q2 = new MultipleChoiceQuestion(multipleChoiceQuestionEN,
					multipleChoiceAnswerEN, multipleChoiceWrongAnswersEN);
			super.questions.add(q2);
		}
		
	}
	
	private List<String> allCorrectSentencesEN(QueryResult qr){
		List<String> sentences = new ArrayList<String>();
		sentences.add(this.NAME_possessive_blood_type_is_BLOODTYPE_EN_correct(qr));
		return sentences;
	}
	
	private String NAME_possessive_blood_type_is_BLOODTYPE_EN_correct(QueryResult qr){
		String possessiveName = GrammarRules.possessiveCaseOfSingularNoun(qr.personNameEN);
		String sentence = possessiveName + " blood type is " + qr.bloodType + "build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res";
		//no need since all names are capitalized?
		sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
		return sentence;
	}
	
	private String formatSentenceForeign(QueryResult qr){
		String sentence = qr.personNameForeign + "の血液型は" + qr.bloodType + "型です。";
		return sentence;
	}
	
	//puzzle pieces for sentence puzzle question
	private List<String> puzzlePiecesEN(QueryResult qr){
		List<String> pieces = new ArrayList<String>();
		String possessiveName = GrammarRules.possessiveCaseOfSingularNoun(qr.personNameEN);
		pieces.add(possessiveName);
		pieces.add("blood type");
		pieces.add("is");
		pieces.add(qr.bloodType);
		pieces.add("build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res");
		return pieces;
		
	}
	
	//multiple choice question sentence
	//tests the possessive case
	private String multipleChoiceQuestionEN(QueryResult qr){
		return qr.personNameEN + Question.BLANK_MARKER + " blood type is " + 
				qr.bloodType + "build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res";
	}
	
	private String multipleChoiceAnswerEN(){
		return GrammarRules.possessiveCaseOfSingularNoun("");
	}
	
	private List<String> multipleChoiceWrongAnswersEN(){
		List<String> wrongAnswers = new ArrayList<String>();
		wrongAnswers.add("s'");
		wrongAnswers.add("'es");
		return wrongAnswers;
	}
}
