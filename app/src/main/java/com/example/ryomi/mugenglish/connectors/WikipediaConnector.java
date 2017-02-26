package com.example.ryomi.mugenglish.connectors;

public class WikipediaConnector extends WikiBaseEndpointConnector {
	public  WikipediaConnector(){
		
	}
	
	protected String formatURL(String... parameterValue) throws Exception{
		//already encoded
		return "https://en.wikipedia.org/w/api.php?action=query&prop=revisions&"
				+ "titles=" + parameterValue[0] +
				"&format=xml&rvprop=content";
	}
}
