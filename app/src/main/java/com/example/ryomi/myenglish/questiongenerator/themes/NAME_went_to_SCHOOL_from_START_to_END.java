package com.example.ryomi.myenglish.questiongenerator.themes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.NodeList;

import com.example.ryomi.myenglish.connectors.EndpointConnector;
import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.db.database2classmappings.ThemeMappings;
import com.example.ryomi.myenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.questiongenerator.GrammarRules;
import com.example.ryomi.myenglish.questiongenerator.Question;
import com.example.ryomi.myenglish.questiongenerator.Theme;
import com.example.ryomi.myenglish.questiongenerator.questions.SentencePuzzleQuestion;
import com.example.ryomi.myenglish.questiongenerator.questions.TrueFalseQuestion;

import org.w3c.dom.Node;

import org.w3c.dom.Document;


/*
 * このテーマは from A to B を勉強する。
 * PERSON went to INSTITUTION は事前に勉強しておく必要がある。
 * トピックは学校に通ったことがある人。
 * 大学、高校等は問わない。
 */

public class NAME_went_to_SCHOOL_from_START_to_END extends Theme {
	//クエリーするときの名前
	private final String personNamePH = "personName";
	private final String personNameForeignPH = "personNameForeign";
	private final String personNameENPH = "personNameEN";
	private final String educationInstitutionNameForeignPH = "educationInstitutionNameForeign";
	private final String educationInstitutionNameENPH = "educationInstitutionNameEN";
	private final String startDatePH = "startDate";
	private final String endDatePH = "endDate";
	
	private List<QueryResult> queryResults = new ArrayList<>();
	private class QueryResult {
		private String educationInstitutionNameEN;
		private String educationInstitutionNameForeign;
		private String personNameEN;
		private String personNameForeign;
		private String startDate;
		private String endDate;
		
		private QueryResult(
				String educationInstitutionNameEN, String educationInstitutionNameForeign,
				String personNameEN, String personNameForeign,
				String startDate, String endDate)
		{
			this.educationInstitutionNameEN = educationInstitutionNameEN;
			this.educationInstitutionNameForeign = educationInstitutionNameForeign;
			this.personNameEN = personNameEN;
			this.personNameForeign = personNameForeign;
			this.startDate = startDate;
			this.endDate = endDate;
		}
	}
	
	public NAME_went_to_SCHOOL_from_START_to_END(EndpointConnector connector, ThemeData data){
		super(connector, data);
		super.themeTopicCount = 3;
		super.wikiDataIDPH = this.personNamePH;/*
		super.backupIDsOfTopics.add("Q5284");//Bill Gates
		super.backupIDsOfTopics.add("Q8027");//Elon Musk*/
	}
	
	protected String getSPARQLQuery(){
		
		//find the education institution, start and end date of one individual
		//ex ビル・ゲーツ | ハーバード・カレッジ | Bill Gates | Harvard College | 1973-01-01T00:00:00Z | 1975-01-01T00:00:00Z
		String query = 	"SELECT ?" + personNamePH + " ?" + personNameForeignPH +  " ?" + personNameENPH +
				" ?" + educationInstitutionNameForeignPH + " ?" + educationInstitutionNameENPH +
				" ?" + startDatePH + " ?" + endDatePH + " " +
				"WHERE" +
				"{" +
				"    ?" + personNamePH + " wdt:P31 wd:Q5 . " + //is human
				"    ?" + personNamePH + " p:P69 ?educationStatement . " + //this person is educated somewhere
				"    ?educationStatement ps:P69 ?education . " + //grabbing name of institution
				"    ?educationStatement pq:P580 ?" + startDatePH + "build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res " + //grabbing start date
				"    ?educationStatement pq:P582 ?" + endDatePH + " . " + //grabbing end date
				"	 SERVICE wikibase:label { bd:serviceParam wikibase:language '" + 
				     WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //these labels should be in the foreign language
				"    '" + WikiBaseEndpointConnector.ENGLISH + "' . " +  //fallback language is English
				"                           ?" + personNamePH + " rdfs:label ?" + personNameForeignPH + ". " + //grabbing foreign label of person
				"                           ?education rdfs:label ?" + educationInstitutionNameForeignPH + " . }" +
				"    SERVICE wikibase:label { bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //English labels
				"                           ?" + personNamePH + " rdfs:label ?" + personNameENPH + " . " +
				"                           ?education rdfs:label ?" + educationInstitutionNameENPH + " . } " +
				"    SERVICE wikibase:label { bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "'} " + //everything else is in English
				  
				"    BIND (wd:%s as ?" + personNamePH + ") . " + //binding the ID of entity as ?person
				"    FILTER (?" + startDatePH + " != ?" + endDatePH + ") . " + //so we can prevent from 2000 to 2000
				//still allows 2000/1/1 ~ 2000/1/2 but I haven't seen one entry with date + year
				"} " + 
				"LIMIT " + super.themeTopicCount + " "; //we only need enough results for the topic";
		
		//Problems with this query that may need to be handled
		// 1.This will not pick up any people still in college (do not have an end date)
		// 2.This will return the ID of the institution if the Foreign label does not exist
		//   instead of null or a blank string
		// 3.Some of the institution translation are wrong
		//   EX: Univ. of Pennsylvania School of Arts and Sciences
		//     ->教科科学大学院
		// 4. The ISO 8601 date format allows only year, but WikiData returns 1/1/year 0:00:00
		
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
	
	protected void processResultsIntoClassWrappers(){
		Document document = super.documentOfTopics;
		NodeList allResults = document.getElementsByTagName("result");
		int resultLength = allResults.getLength();
		for (int i=0; i<resultLength; i++){
			Node head = allResults.item(i);
			String educationInstitutionNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, educationInstitutionNameENPH);
			String educationInstitutionNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, educationInstitutionNameForeignPH);
			
			String startDateTimeValue = SPARQLDocumentParserHelper.findValueByNodeName(head, startDatePH);
			String endDateTimeValue = SPARQLDocumentParserHelper.findValueByNodeName(head, endDatePH);
			
			String startDate = this.getYearFromFullISO8601DateTime(startDateTimeValue);
			String endDate = this.getYearFromFullISO8601DateTime(endDateTimeValue);
			
			String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameENPH);
			String personNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameForeignPH);
			
			QueryResult qr = new QueryResult(
					educationInstitutionNameEN, educationInstitutionNameForeign,
					personNameEN, personNameForeign,
					startDate, endDate);
			
			queryResults.add(qr);
		}
	}
	
	protected void createQuestionsFromResults(){
		for (QueryResult qr : queryResults){
			String statementEN = this.NAME_went_to_SCHOOL_from_START_to_END_EN_correct(qr);
			Question q = new TrueFalseQuestion(statementEN, true);
			//super.questions.add(q);
			String statementForeign = this.formatSentenceForeign(qr);
			List<String> allCorrectSentencesEN = this.allCorrectSentencesEN(qr);
			List<String> puzzlePieces = this.puzzlePiecesEN(qr);
			Question q2 = new SentencePuzzleQuestion(statementForeign, allCorrectSentencesEN, puzzlePieces);
			//super.questions.add(q2);
			
			
		}
	}
	
	private String getYearFromFullISO8601DateTime(String fullISO8601DateTime){
		//we can assume the first four letters are the date
		// ex: 1990-01-01T00:00:00Z
		return fullISO8601DateTime.substring(0,4);
	}
	
	//correct sentences
	//
	private List<String> allCorrectSentencesEN(QueryResult qr){
		List<String> sentences = new ArrayList<String>();
		sentences.add(this.NAME_went_to_SCHOOL_from_START_to_END_EN_correct(qr));
		sentences.add(this.from_START_to_END_NAME_went_to_SCHOOL_EN_correct(qr));
		return sentences;
	}
	private String NAME_went_to_SCHOOL_from_START_to_END_EN_correct(QueryResult qr){
		//'the' を学校名の前につけるかどうか
		String institution = GrammarRules.definiteArticleBeforeSchoolName(qr.educationInstitutionNameEN);
		
		String sentence = qr.personNameEN + " went to " + institution + 
				" from " + qr.startDate + " to " + qr.endDate + "build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res";
		//人の名前は絶対大文字で始まるから、わざわざやらなくていいはず？
		sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
		return sentence;
	}
	
	private String from_START_to_END_NAME_went_to_SCHOOL_EN_correct(QueryResult qr){
		String institution = GrammarRules.definiteArticleBeforeSchoolName(qr.educationInstitutionNameEN);
		//正確にはFrom a to b, person went to C.
		//カンマは気にしない？
		return "From " + qr.startDate + " to " + qr.endDate + " " +
		qr.personNameEN + " went to " + institution + "build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res";
	}
	
	//incorrect sentences as questions
	//
	private String NAME_went_to_SCHOOL_to_START_from_END_EN_incorrect(QueryResult qr){
		String institution = GrammarRules.definiteArticleBeforeSchoolName(qr.educationInstitutionNameEN);
		return qr.personNameEN + " went to " + institution + " to " + qr.startDate + " from " + qr.endDate + "build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res";
	}
	
	//puzzle pieces for sentence puzzle question
	private List<String> puzzlePiecesEN(QueryResult qr){
		List<String> pieces = new ArrayList<String>();
		String institution = GrammarRules.definiteArticleBeforeSchoolName(qr.educationInstitutionNameEN);
		pieces.add(institution);
		pieces.add(qr.personNameEN);
		pieces.add(qr.startDate);
		pieces.add(qr.endDate);
		pieces.add("went to");
		pieces.add("from");
		pieces.add("to");
		pieces.add("build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res");
		return pieces;
	}
	
	//correct sentence in foreign language (need only one?)
	private String formatSentenceForeign(QueryResult qr){
		return qr.personNameForeign + "は" + qr.startDate + "年から" + qr.endDate + "年まで" + qr.educationInstitutionNameForeign + "に通いました。";
	}
}
