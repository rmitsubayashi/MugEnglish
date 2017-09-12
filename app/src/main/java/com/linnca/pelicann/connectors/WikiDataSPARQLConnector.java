package com.linnca.pelicann.connectors;

import org.w3c.dom.Document;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WikiDataSPARQLConnector extends WikiBaseEndpointConnector {
	/*
	 * WikiDataのSPARQLエンドポイントに接続
	 */
	public static String ALL_RESULTS_TAG = "results";
	public static final String RESULT_TAG = "result";
	
	public WikiDataSPARQLConnector(){
		super();
	}
	
	public WikiDataSPARQLConnector(String language){
		super(language);
	}

	public static int countResults(Document doc){
		return doc.getElementsByTagName(RESULT_TAG).getLength();

	}

	protected String formatURL(String... parameterValue) throws Exception{
		String languageInsertedQuery = super.formatRequestLanguage(parameterValue[0]);
		String url = "https://query.wikidata.org/sparql?query=";
		//http URLs have to be in ASCII characters
		String encodedQuery = URLEncoder.encode(languageInsertedQuery, StandardCharsets.UTF_8.name());
		url += encodedQuery;
		return url;
	}
}
