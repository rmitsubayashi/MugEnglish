package pelicann.linnca.com.corefunctionality.connectors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WikiDataAPISearchConnector extends WikiBaseEndpointConnector {
	public static final String ENTITY_TAG = "entity";
	//how many entities to search
	private final int limitNumber;

	public WikiDataAPISearchConnector(String language, int limitNumber){
		super(language);
		this.limitNumber = limitNumber;
	}

	//we want both the search query and the number of results to search
	//so we format the string like
	// number|query
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
