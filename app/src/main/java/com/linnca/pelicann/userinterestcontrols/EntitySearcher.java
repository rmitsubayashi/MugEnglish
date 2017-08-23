package com.linnca.pelicann.userinterestcontrols;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.WikiDataAPISearchConnector;
import com.linnca.pelicann.db.datawrappers.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class EntitySearcher {
	/*
	* The only reason we have two connectors is because the search API
	* for WikiData doesn't return the Japanese description (only Japanese label)
	* so we have to make another request to the WikiData API to get the
	* Japanese description
	* */
	private EndpointConnectorReturnsXML searchConnector;
	public static int LIMIT = 50;

	public EntitySearcher(WikiDataAPISearchConnector searchConnector){
		this.searchConnector = searchConnector;
	}
	
	//検索結果を返す
	public List<WikiDataEntryData> search(String query, Integer searchLimit) throws Exception{
		List<WikiDataEntryData> searchResults = new ArrayList<>();
		String[] queryWrapper = {query, searchLimit.toString()};
		Document resultDOM = searchConnector.fetchDOMFromGetRequest(queryWrapper);
		NodeList resultNodes = resultDOM.getElementsByTagName(WikiDataAPISearchConnector.ENTITY_TAG);

		int nodeCt = resultNodes.getLength();
		for (int i=0; i<nodeCt; i++){
			Node n = resultNodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				String wikiDataID = "";
				String label = "";
				String description = "";

				Element e = (Element)n;
				if (e.hasAttribute("id")) {
					wikiDataID = e.getAttribute("id");
				}
				if(e.hasAttribute("label")) {
					label = e.getAttribute("label");
				}

				if(e.hasAttribute("description")) {
					description = e.getAttribute("description");
				}

				//set pronunciation to label for now.
				//if there is a pronunciation field, use that
				searchResults.add(new WikiDataEntryData(label, description, wikiDataID, label, WikiDataEntryData.CLASSIFICATION_NOT_SET));
			}

		}

		return searchResults;
	}
}
