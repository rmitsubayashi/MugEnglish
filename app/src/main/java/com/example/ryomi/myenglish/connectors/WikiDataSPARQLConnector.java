package com.example.ryomi.myenglish.connectors;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WikiDataSPARQLConnector extends WikiBaseEndpointConnector {
	/*
	 * WikiDataのSPARQLエンドポイントに接続
	 */
	
	public WikiDataSPARQLConnector(){
		super();
	}
	
	public WikiDataSPARQLConnector(String language){
		super(language);
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
