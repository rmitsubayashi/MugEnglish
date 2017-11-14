package com.linnca.pelicann.connectors;

public class WikipediaConnector extends WikiBaseEndpointConnector {
	public  WikipediaConnector(){
		
	}

	@Override
	protected String formatURL(String parameterValue) throws Exception{
		//already encoded
		return "https://en.wikipedia.org/w/api.php?action=query&prop=revisions&"
				+ "titles=" + parameterValue +
				"&format=xml&rvprop=content";
	}
}
