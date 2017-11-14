package com.linnca.pelicann.connectors;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WikiDataAPISearchConnector extends WikiBaseEndpointConnector {
	public static final String ENTITY_TAG = "entity";
	
	public WikiDataAPISearchConnector(){
		super();
	}
	
	public WikiDataAPISearchConnector(String language){
		super(language);
	}

	//we want both the search query and the number of results to search
	//so we format the string like
	// number|query
	protected String formatURL(String parameterValue) throws Exception{
		String encodedSearchText = URLEncoder.encode(parameterValue, StandardCharsets.UTF_8.name());
		int numResults = 7;
		//default search limit is 7
		String url = "https://wikidata.org/w/api.php?action=wbsearchentities&format=xml" +
			"&uselang=" + WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + //so the description will be in JP
			"&search=" + encodedSearchText +
			"&language=" + WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER +
			"&limit=" + numResults;
			//"&utf8=1";//the description will be encoded in unicode otherwise. only for json
		url = super.formatRequestLanguage(url);
		return url;
	}
}
