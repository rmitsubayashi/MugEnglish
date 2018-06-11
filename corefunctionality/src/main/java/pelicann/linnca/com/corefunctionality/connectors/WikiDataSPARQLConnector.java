package pelicann.linnca.com.corefunctionality.connectors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WikiDataSPARQLConnector extends WikiBaseEndpointConnector {
	/*
	 * WikiDataのSPARQLエンドポイントに接続
	 */
	//when we request an XML file from SPARQL, each result
	// will have this tag
	public static final String RESULT_TAG = "result";
	
	public WikiDataSPARQLConnector(){
		super();
	}

	//we plug in the language (only Japanese now)
	public WikiDataSPARQLConnector(String language){
		super(language);
	}

	protected String formatURL(String parameterValue){
		String languageInsertedQuery = super.formatRequestLanguage(parameterValue);
		String url = "https://query.wikidata.org/sparql?query=";
		//http URLs have to be in ASCII characters
		String encodedQuery;
		try {
			encodedQuery = URLEncoder.encode(languageInsertedQuery, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e){
			e.printStackTrace();
			//should also log the query..
			encodedQuery = languageInsertedQuery;
		}
		url += encodedQuery;
		return url;
	}

	// the url given from SPARQL queries redirect 4~5 times.
	// in Android (Glide API), it gives a 'too many redirects' error.
	// so, get it to a redirect string further down (not the furthest down)
	public static String cleanImageURL(String url){
		if (url.startsWith("http") && !url.startsWith("https")){
			url = url.replaceFirst("http", "https");
		}

		url = url.replaceAll("%20", "_");

		return url;
	}
}
