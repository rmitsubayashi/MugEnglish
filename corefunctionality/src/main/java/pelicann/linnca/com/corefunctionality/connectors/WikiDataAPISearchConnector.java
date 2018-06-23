package pelicann.linnca.com.corefunctionality.connectors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WikiDataAPISearchConnector extends WikiBaseEndpointConnector {
	//this is WikiData's search functionality
	//(a lot faster than SPARQL)
	public static final String ENTITY_TAG = "entity";
	//how many entities to search
	private final int limitNumber;

	public WikiDataAPISearchConnector(String language, int limitNumber){
		super(language);
		//if we decide we should change this (i.e. load more results...)
		// then we should be able to change this number
		this.limitNumber = limitNumber;
	}

	protected String formatURL(String parameterValue){
		String encodedSearchText;
		try {
			encodedSearchText = URLEncoder.encode(parameterValue, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e){
			e.printStackTrace();
			encodedSearchText = parameterValue;
		}

		//default search limit is 7
		String url = "https://wikidata.org/w/api.php?action=wbsearchentities&format=xml" +
			"&uselang=" + WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + //so the description will be in JP
			"&search=" + encodedSearchText +
			"&language=" + WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER +
			"&limit=" + limitNumber;
			//"&utf8=1";//the description will be encoded in unicode otherwise. only for json
		url = super.formatRequestLanguage(url);
		return url;
	}
}
