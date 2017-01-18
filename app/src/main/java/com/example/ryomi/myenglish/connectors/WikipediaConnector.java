package com.example.ryomi.myenglish.connectors;

public class WikipediaConnector extends WikiBaseEndpointConnector {
	public  WikipediaConnector(){
		
	}
	
	protected String formatURL(String... parameterValue) throws Exception{
		//already encoded
		String url = "https://en.wikipedia.org/w/api.php?action=query&prop=revisions&"
				+ "titles=" + parameterValue[0] +
				"&format=xml&rvprop=content";
		return url;
	}
}
