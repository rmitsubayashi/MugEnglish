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
import com.example.ryomi.myenglish.questiongenerator.Theme;

public class NAME_went_to_SCHOOL_So_did_NAME2 extends Theme{
	
	private final String personPH = "person";
	private final String personForeignPH = "personForeign";
	private final String personENPH = "personEN";
	private final String person2ForeignPH = "person2Foreign";
	private final String person2ENPH = "person2EN";
	private final String schoolForeignPH = "schoolForeign";
	private final String schoolENPH = "schoolEN";
	private final String sitelinksPH = "sitelinks";
	
	private List<QueryResult> queryResults;
	private class QueryResult {
		private String personEN;
		private String personForeign;
		private String person2EN;
		private String person2Foreign;
		private String schoolEN;
		private String schoolForeign;
		
		private QueryResult(
			String personEN, String personForeign,
			String person2EN, String person2Foreign,
			String schoolEN, String schoolForeign)
		{
			this.personEN = personEN;
			this.personForeign = personForeign;
			this.person2EN = person2EN;
			this.person2Foreign = person2Foreign;
			this.schoolEN = schoolEN;
			this.schoolForeign = schoolForeign;
		}
	}
	
	public NAME_went_to_SCHOOL_So_did_NAME2(EndpointConnector connector){
		super(connector);
		super.themeID = ThemeMappings.NAME_went_to_SCHOOL_So_did_NAME2;
		super.name = "so did";
		super.description = "So did を勉強しよう！";
		super.themeTopicCount = 3;
		super.wikiDataIDPH = this.personPH;
		queryResults = new ArrayList<QueryResult>();
		super.backupIDsOfTopics.add("Q281734");//Nagatomo
		super.backupIDsOfTopics.add("Q37876");//Natalie Portman
		super.backupIDsOfTopics.add("Q175535");//Matt Damon
	}
	
	protected String getSPARQLQuery(){
		String query = 
				"SELECT ?" + personPH + " ?" + personENPH + " ?" + personForeignPH + 
				" ?" + person2ENPH + " ?" + person2ForeignPH + 
				" ?" + schoolENPH + " ?" + schoolForeignPH +
				" ?" + sitelinksPH + " " +
				"WHERE " +
				"{ " +
				"    ?" + personPH + " wdt:P69 ?education . " +
				"    ?person2 wdt:P69 ?education . " +
				"    ?person2 wikibase:sitelinks ?" + sitelinksPH + "build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res " +
				    
				"	SERVICE wikibase:label { bd:serviceParam wikibase:language '" + 
				                            WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "'," +
				"                           '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
				"                           ?" + personPH + " rdfs:label ?" + personForeignPH + " . " +
				"                           ?person2 rdfs:label ?" + person2ForeignPH + " . " +
				"                           ?education rdfs:label ?" + schoolForeignPH + " . } " +
				"    SERVICE wikibase:label { bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
				"                           ?" + personPH + " rdfs:label ?" + personENPH + " . " +
				"                           ?person2 rdfs:label ?" + person2ENPH + " . " +
				"                           ?education rdfs:label ?" + schoolENPH + " . } " +
				  
				"    BIND (wd:%s as ?" + personPH + ") " +
				  
				" } " +
				"LIMIT " + super.themeTopicCount;
		
		return query;
	}

	protected void populateResults(Set<String> wikidataIDs) throws Exception{
		for (String entityID : wikidataIDs){
			String query = super.addEntityToQuery(entityID);
			Document resultDOM = connector.fetchDOMFromGetRequest(query);
			//reorder based on popularity and get top few
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
		System.out.println(resultLength);
		for (int i=0; i<resultLength; i++){
			Node head = allResults.item(i);
			String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, personENPH);
			String personForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, personForeignPH);
			String person2EN = SPARQLDocumentParserHelper.findValueByNodeName(head, person2ENPH);
			String person2Foreign = SPARQLDocumentParserHelper.findValueByNodeName(head, person2ForeignPH);
			String schoolEN = SPARQLDocumentParserHelper.findValueByNodeName(head, schoolENPH);
			String schoolForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, schoolForeignPH);
			
			QueryResult qr = new QueryResult(personEN, personForeign, 
					person2EN, person2Foreign,
					schoolEN, schoolForeign);
			queryResults.add(qr);
		}
	}
	
	protected void createQuestionsFromResults(){
		System.out.println("called");
		for (QueryResult qr : queryResults){
			String statement = this.NAME_went_to_SCHOOL_So_did_NAME2_EN_correct(qr);
			System.out.println(statement);
		}
		
	}
	
	private String NAME_went_to_SCHOOL_So_did_NAME2_EN_correct(QueryResult qr){
		String schoolName = GrammarRules.definiteArticleBeforeSchoolName(qr.schoolEN);
		String sentence = qr.personEN + " went to " + schoolName + ". So did " + qr.person2EN + "build/intermediates/exploded-aar/com.android.support/animated-vector-drawable/25.1.0/res";
		return sentence;
	}
}
