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
import com.example.ryomi.myenglish.questiongenerator.Question;
import com.example.ryomi.myenglish.questiongenerator.Theme;
import com.example.ryomi.myenglish.questiongenerator.questions.TrueFalseQuestion;
import com.example.ryomi.myenglish.tools.IdentifyWhetherSportsUsePlayDoGo;

public class NAME_plays_SPORT extends Theme{
	private final String personNamePH = "personName";
	private final String personNameForeignPH = "personNameForeign";
	private final String personNameENPH = "personNameEN";
	private final String sportIDPH = "sportID";
	private final String sportNameForeignPH = "sportNameForeign";
	
	private List<QueryResult> queryResults;
	
	private class QueryResult {
		private String personNameEN;
		private String personNameForeign;
		private String sportID;
		private String sportNameForeign;
		
		private QueryResult(String personNameEN, String personNameForeign,
				String sportID, String sportNameForeign){
			this.personNameEN = personNameEN;
			this.personNameForeign = personNameForeign;
			this.sportID = sportID;
			this.sportNameForeign = sportNameForeign;
		}
	}
	
	private IdentifyWhetherSportsUsePlayDoGo sportHelper;
	
	public NAME_plays_SPORT(EndpointConnector connector){
		super(connector);
		super.themeID = ThemeMappings.NAME_plays_SPORT;
		super.name = "スポーツ";
		super.description = "有名人の好きなスポーツを勉強しよう！";
		super.themeTopicCount = 5;
		super.wikiDataIDPH = personNamePH;
		sportHelper = new IdentifyWhetherSportsUsePlayDoGo();
		queryResults = new ArrayList<QueryResult>();
		super.backupIDsOfTopics.add("Q10520");//Beckham
		super.backupIDsOfTopics.add("Q486359");//Pacquiao
		super.backupIDsOfTopics.add("Q41421");//Michael Jordan
		super.backupIDsOfTopics.add("Q39562");//Michael Phelps
		super.backupIDsOfTopics.add("Q5799");//Allyson Felix
	}
	
	protected String getSPARQLQuery(){
		String query = 
				"SELECT ?" + personNamePH + " ?" + personNameENPH + " ?" + personNameForeignPH + 
				" ?" + sportIDPH + " ?" + sportNameForeignPH +  " " +
				"		WHERE " +
				"		{ " +
				"		    ?" + personNamePH + " wdt:P31 wd:Q5 . " +
				"			?" + personNamePH + " wdt:P641 ?" + sportIDPH + "build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res " +
				"		    FILTER NOT EXISTS { ?" + personNamePH + " wdt:P570 ?dateDeath } . " +//死んでいない（played ではなくてplays）
				"		  	SERVICE wikibase:label { bd:serviceParam wikibase:language '" + 
							WikiBaseEndpointConnector.ENGLISH + "' . " +
				"				?" + personNamePH + " rdfs:label ?" + personNameENPH + " } . " +
				"		  	SERVICE wikibase:label { bd:serviceParam wikibase:language '" + 
							WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
							WikiBaseEndpointConnector.ENGLISH + "' . " +
				"				?" + personNamePH + " rdfs:label ?" + personNameForeignPH + " . " +
				"				?" + sportIDPH + " rdfs:label ?" + sportNameForeignPH + " } . " +
				"           BIND (wd:%s as ?" + personNamePH + ") " +
				"		}" +
				"       LIMIT " + themeTopicCount;
		
		return query;
	}
	
	protected void populateResults(Set<String> wikidataIDs) throws Exception {
		
		for (String entityID : wikidataIDs){
			String query = super.addEntityToQuery(entityID);
			Document resultDOM = connector.fetchDOMFromGetRequest(query);
			//check if the sport exists
			NodeList results = resultDOM.getElementsByTagName("result");
			if (results.getLength() == 0)
				continue;
			
			Node head = results.item(0);
			String id = SPARQLDocumentParserHelper.findValueByNodeName(head, sportIDPH);
			// ~entity/id になってるから削る
			int lastIndexID = id.lastIndexOf('/');
			id = id.substring(lastIndexID+1);
			if (sportHelper.sportExists(id)){
				this.addResultsToMainDocument(resultDOM);
			}
			else
				continue;
			
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
			String sportID = SPARQLDocumentParserHelper.findValueByNodeName(head, sportIDPH);
			// ~entity/id になってるから削る
			int lastIndexID = sportID.lastIndexOf('/');
			sportID = sportID.substring(lastIndexID+1);
			String sportNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, sportNameForeignPH);
			
			QueryResult qr = new QueryResult(personNameEN, personNameForeign,
					sportID, sportNameForeign);
			
			queryResults.add(qr);
			
		}
	}
	
	protected void createQuestionsFromResults(){
		for (QueryResult qr : queryResults){
			try {
				String trueFalseQuestion = this.NAME_plays_SPORT_EN_correct(qr);
				Question q = new TrueFalseQuestion(trueFalseQuestion, true);
				super.questions.add(q);
			} catch (Exception e){
				continue;
			}
		}
	}
	
	private String NAME_plays_SPORT_EN_correct(QueryResult qr) throws Exception{
		String verbObject = sportHelper.findVerbObject(qr.sportID, IdentifyWhetherSportsUsePlayDoGo.PRESENT3RD);
		String sentence = qr.personNameEN + " " + verbObject + "build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res";
		return sentence;
	}
	
	private String formatSentenceForeign(QueryResult qr){
		String sentence = qr.personNameForeign + "は" + qr.sportNameForeign + "をします。";
		return sentence;
	}
}
