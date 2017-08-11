package com.example.ryomi.mugenglish.questiongenerator.lessons;

import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.db.datawrappers.LessonData;
import com.example.ryomi.mugenglish.questiongenerator.GrammarRules;
import com.example.ryomi.mugenglish.questiongenerator.Lesson;
import com.example.ryomi.mugenglish.questiongenerator.QGUtils;
import com.example.ryomi.mugenglish.questiongenerator.QuestionDataWrapper;
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/*
 * このテーマは from A to B を勉強する。
 * PERSON went to INSTITUTION は事前に勉強しておく必要がある。
 * トピックは学校に通ったことがある人。
 * 大学、高校等は問わない。
 */

public class NAME_went_to_SCHOOL_from_START_to_END extends Lesson {
	public static final String KEY = "NAME_went_to_SCHOOL_from_START_to_END";
	public static final String TITLE = "|名前|は|入学年|から|卒業年|まで|学校|に通いました";

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
		private String personID;
		private String educationInstitutionNameEN;
		private String educationInstitutionNameForeign;
		private String personNameEN;
		private String personNameForeign;
		private String startDate;
		private String endDate;
		
		private QueryResult(
				String personID,
				String educationInstitutionNameEN, String educationInstitutionNameForeign,
				String personNameEN, String personNameForeign,
				String startDate, String endDate)
		{
			this.personID = personID;
			this.educationInstitutionNameEN = educationInstitutionNameEN;
			this.educationInstitutionNameForeign = educationInstitutionNameForeign;
			this.personNameEN = personNameEN;
			this.personNameForeign = personNameForeign;
			this.startDate = startDate;
			this.endDate = endDate;
		}
	}
	
	public NAME_went_to_SCHOOL_from_START_to_END(WikiBaseEndpointConnector connector, LessonData data){
		super(connector, data);
		super.questionSetsLeftToPopulate = 4;/*
		super.backupIDsOfTopics.add("Q5284");//Bill Gates
		super.backupIDsOfTopics.add("Q8027");//Elon Musk*/
	}

	@Override
	protected String getSPARQLQuery(){
		
		//find the education institution, start and end date of one individual
		//ex ビル・ゲーツ | ハーバード・カレッジ | Bill Gates | Harvard College | 1973-01-01T00:00:00Z | 1975-01-01T00:00:00Z
		return 	"SELECT ?" + personNamePH + " ?" + personNameForeignPH +  " ?" + personNameENPH +
				" ?" + educationInstitutionNameForeignPH + " ?" + educationInstitutionNameENPH +
				" ?" + startDatePH + " ?" + endDatePH + " " +
				"WHERE" +
				"{" +
				"    ?" + personNamePH + " wdt:P31 wd:Q5 . " + //is human
				"    ?" + personNamePH + " p:P69 ?educationStatement . " + //this person is educated somewhere
				"    ?educationStatement ps:P69 ?education . " + //grabbing name of institution
				"    ?educationStatement pq:P580 ?" + startDatePH + " . " + //grabbing start date
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
				"    FILTER (YEAR(?" + startDatePH + ") != YEAR(?" + endDatePH + ") ) . " + //so we can prevent from 2000 to 2000
				"} ";

		// Some of the institution translation are wrong
		//   EX: Univ. of Pennsylvania School of Arts and Sciences
		//     ->教科科学大学院

	}
	
	@Override
	protected void processResultsIntoClassWrappers(Document document){
		NodeList allResults = document.getElementsByTagName(
				WikiDataSPARQLConnector.RESULT_TAG
		);
		int resultLength = allResults.getLength();
		for (int i=0; i<resultLength; i++){
			Node head = allResults.item(i);
			String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, personNamePH);
			personID = QGUtils.stripWikidataID(personID);

			String educationInstitutionNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, educationInstitutionNameENPH);
			String educationInstitutionNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, educationInstitutionNameForeignPH);
			
			String startDateTimeValue = SPARQLDocumentParserHelper.findValueByNodeName(head, startDatePH);
			String endDateTimeValue = SPARQLDocumentParserHelper.findValueByNodeName(head, endDatePH);
			
			String startDate = this.getYearFromFullISO8601DateTime(startDateTimeValue);
			String endDate = this.getYearFromFullISO8601DateTime(endDateTimeValue);
			
			String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameENPH);
			String personNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameForeignPH);
			
			QueryResult qr = new QueryResult(
					personID,
					educationInstitutionNameEN, educationInstitutionNameForeign,
					personNameEN, personNameForeign,
					startDate, endDate);
			
			queryResults.add(qr);
		}
	}

	@Override
	protected int getQueryResultCt(){ return queryResults.size(); }

	@Override
	protected void saveResultTopics(){
		for (QueryResult qr : queryResults){
			topics.add(qr.personNameForeign);
		}
	}

	@Override
	protected void createQuestionsFromResults(){
		for (QueryResult qr : queryResults){
			List<QuestionData> questionSet = new ArrayList<>();
			QuestionData trueFalseQuestion = createTrueFalseQuestion(qr);
			questionSet.add(trueFalseQuestion);

			QuestionData puzzlePiecesQuestion = createPuzzlePiecesQuestion(qr);
			questionSet.add(puzzlePiecesQuestion);

			super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.personID));
		}
	}
	
	private String getYearFromFullISO8601DateTime(String fullISO8601DateTime){
		//we can assume the first four letters are the date
		// ex: 1990-01-01T00:00:00Z
		return fullISO8601DateTime.substring(0,4);
	}
	
	//correct sentences
	//
	private List<String> alternativeCorrectSentencesEN(QueryResult qr){
		List<String> sentences = new ArrayList<>();
		sentences.add(this.from_START_to_END_NAME_went_to_SCHOOL_EN_correct(qr));
		return sentences;
	}
	private String NAME_went_to_SCHOOL_from_START_to_END_EN_correct(QueryResult qr){
		//'the' を学校名の前につけるかどうか
		String institution = GrammarRules.definiteArticleBeforeSchoolName(qr.educationInstitutionNameEN);
		
		String sentence = qr.personNameEN + " went to " + institution + 
				" from " + qr.startDate + " to " + qr.endDate + ".";
		//人の名前は絶対大文字で始まるから、わざわざやらなくていいはず？
		sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
		return sentence;
	}
	
	private String from_START_to_END_NAME_went_to_SCHOOL_EN_correct(QueryResult qr){
		String institution = GrammarRules.definiteArticleBeforeSchoolName(qr.educationInstitutionNameEN);
		//正確にはFrom a to b, person went to C.
		//カンマは気にしない？
		return "From " + qr.startDate + " to " + qr.endDate + " " +
		qr.personNameEN + " went to " + institution + ".";
	}
	
	//incorrect sentences as questions
	//
	private String NAME_went_to_SCHOOL_from_END_to_START_EN_incorrect(QueryResult qr){
		String institution = GrammarRules.definiteArticleBeforeSchoolName(qr.educationInstitutionNameEN);
		return qr.personNameEN + " went to " + institution + " from " + qr.endDate + " to " + qr.startDate + ".";
	}
	
	//puzzle pieces for sentence puzzle question
	private List<String> puzzlePieces(QueryResult qr){
		List<String> pieces = new ArrayList<>();
		String institution = GrammarRules.definiteArticleBeforeSchoolName(qr.educationInstitutionNameEN);

		pieces.add(qr.personNameEN);
		pieces.add("went to");
		pieces.add(institution);
		pieces.add("from");
		pieces.add(qr.startDate);
		pieces.add("to");
		pieces.add(qr.endDate);
		return pieces;
	}

	private String puzzlePiecesAnswer(QueryResult qr){
		return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
	}
	
	//correct sentence in foreign language (need only one?)
	private String formatSentenceForeign(QueryResult qr){
		return qr.personNameForeign + "は" + qr.startDate + "年から" + qr.endDate + "年まで" + qr.educationInstitutionNameForeign + "に通いました。";
	}

	private QuestionData createTrueFalseQuestion(QueryResult qr){
		String question;
		String answer;
		if (new Random(System.currentTimeMillis()).nextInt() % 2 == 0) {
			question = NAME_went_to_SCHOOL_from_START_to_END_EN_correct(qr);
			answer = QuestionUtils.TRUE_FALSE_QUESTION_TRUE;
		}
		else {
			question = NAME_went_to_SCHOOL_from_END_to_START_EN_incorrect(qr);
			answer = QuestionUtils.TRUE_FALSE_QUESTION_FALSE;
		}

		QuestionData data = new QuestionData();
		data.setId("");
		data.setLessonId(super.lessonData.getId());
		data.setTopic(qr.personNameForeign);
		data.setQuestionType(QuestionTypeMappings.TRUE_FALSE);
		data.setQuestion(question);
		data.setChoices(null);
		data.setAnswer(answer);
		data.setAcceptableAnswers(null);
		data.setVocabulary(new ArrayList<String>());

		return data;
	}

	private QuestionData createPuzzlePiecesQuestion(QueryResult qr){
		String question = this.formatSentenceForeign(qr);
		List<String> alternatives = this.alternativeCorrectSentencesEN(qr);
		List<String> puzzlePieces = this.puzzlePieces(qr);
		String answer = puzzlePiecesAnswer(qr);

		QuestionData data = new QuestionData();
		data.setId("");
		data.setLessonId(super.lessonData.getId());
		data.setTopic(qr.personNameForeign);
		data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
		data.setQuestion(question);
		data.setChoices(puzzlePieces);
		data.setAnswer(answer);
		data.setAcceptableAnswers(alternatives);
		data.setVocabulary(new ArrayList<String>());

		return data;
	}
}
