package pelicann.linnca.com.corefunctionality.connectors;

import org.w3c.dom.Document;

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

	public static int countResults(Document doc){
		return doc.getElementsByTagName(RESULT_TAG).getLength();

	}

	protected String formatURL(String parameterValue) throws Exception{
		String languageInsertedQuery = super.formatRequestLanguage(parameterValue);
		String url = "https://query.wikidata.org/sparql?query=";
		//http URLs have to be in ASCII characters
		String encodedQuery = URLEncoder.encode(languageInsertedQuery, StandardCharsets.UTF_8.name());
		url += encodedQuery;
		return url;
	}
}
