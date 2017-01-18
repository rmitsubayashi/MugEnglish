package com.example.ryomi.myenglish.connectors;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WikiDataAPIGetConnector extends WikiBaseEndpointConnector {
	/*
	 * WikiDataのAPIエンドポイントに接続
	 */

    public WikiDataAPIGetConnector(){
        super();
    }

    public WikiDataAPIGetConnector(String language){
        super(language);
    }

    protected String formatURL(String... parameterValue) throws Exception{
        String encodedSearchText = URLEncoder.encode(parameterValue[0], StandardCharsets.UTF_8.name());
        //%7C = '|'
        //it's used for multiple parameters for the props(what we want to fetch)
        //and also for the ids
        String url = "https://wikidata.org/w/api.php?action=wbgetentities&format=xml&ids=" + encodedSearchText +
                "&props=labels%7Cdescriptions&languages=" + WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER +
                "&languagefallback=1";
        url = super.formatRequestLanguage(url);
        return url;
    }
}
