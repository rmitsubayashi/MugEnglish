package pelicann.linnca.com.corefunctionality.connectors;

public class WikipediaConnector extends WikiBaseEndpointConnector {
	//to connect to Wikipedia.
	//used mainly for getting text data (like sports helper)

	@Override
	protected String formatURL(String parameterValue) {
		//already encoded
		return "https://en.wikipedia.org/w/api.php?action=query&prop=revisions&"
				+ "titles=" + parameterValue +
				"&format=xml&rvprop=content";
	}
}
