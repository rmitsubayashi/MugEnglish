package com.example.ryomi.myenglish.userinterestcontrols;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.ryomi.myenglish.connectors.EndpointConnector;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;

public class EntitySearcher {
	/*
	* The only reason we have two connectors is because the search API
	* for WikiData doesn't return the Japanese description (only Japanese label)
	* so we have to make another request to the WikiData API to get the
	* Japanese description
	* */
	private EndpointConnector searchConnector;
	private EndpointConnector getConnector;

	//if there happens to be an API that only requires one
	public EntitySearcher(EndpointConnector searchConnector){
		this.searchConnector = searchConnector;
		this.getConnector = null;
	}

	//for when we need two
	public EntitySearcher(EndpointConnector searchConenctor, EndpointConnector getConnector){
		this.searchConnector = searchConenctor;
		this.getConnector = getConnector;
	}
	
	//検索結果を返す
	//まず検索でWikiData ID を入手する
	//次にそのIDでlabelとdescriptionを入手する
	//*検索では日本語のdescriptionを入手できない
	public List<WikiDataEntryData> search(String searchText, Integer searchLimit) throws Exception{
		 new ArrayList<WikiDataEntryData>();
		List<String> idList = fetchIDs(searchText, searchLimit);
		//now query for the label and description
		List<WikiDataEntryData> searchResults = fetchAllData(idList);

		return searchResults;
	}

	private List<String> fetchIDs(String query, Integer searchLimit) throws Exception{
		//first fetch ids
		String[] queryWrapper = {query, searchLimit.toString()};
		Document idResultDOM = searchConnector.fetchDOMFromGetRequest(queryWrapper);
		NodeList idResultNodes = idResultDOM.getElementsByTagName("entity");

		List<String> idList = new ArrayList<>();
		int nodeCt = idResultNodes.getLength();
		for (int i=0; i<nodeCt; i++){
			Node n = idResultNodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element e = (Element)n;
				if (e.hasAttribute("id")) {
					String wikiDataID = e.getAttribute("id");
					idList.add(wikiDataID);
				}
			}

		}


		return idList;
	}

	private List<WikiDataEntryData> fetchAllData(List<String> idList) throws Exception{
		List<WikiDataEntryData> searchResults = new ArrayList<>();
		String getQuery = formatIDsForQuery(idList);
		Document resultDOM = getConnector.fetchDOMFromGetRequest(getQuery);
		NodeList resultNodes = resultDOM.getElementsByTagName("entity");
		int nodeCt = resultNodes.getLength();
		for (int i=0; i<nodeCt; i++){
			Node n = resultNodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element e = (Element)n;
				//the results are returned in order
				//ie id2|id1 -----> <id2><id1>
				//but we can't guarantee that this will be the case.
				//they might change the API
				//so just grab the ID here too

				String wikiDataID = "";
				String label = "";
				String description = "";

				if (e.hasAttribute("id")) {
					wikiDataID = e.getAttribute("id");
				}

				Node labelNode = e.getElementsByTagName("label").item(0);
				if (labelNode.getNodeType() == Node.ELEMENT_NODE){
					Element labelE = (Element)labelNode;
					if (labelE.hasAttribute("value")){
						label = labelE.getAttribute("value");
					}
				}

				//there are entities with no description
				//so check if there is a description first
				Node descriptionNode = e.getElementsByTagName("description").item(0);
				if (descriptionNode != null) {
					if (descriptionNode.getNodeType() == Node.ELEMENT_NODE) {
						Element descriptionE = (Element) descriptionNode;
						if (descriptionE.hasAttribute("value")) {
							description = descriptionE.getAttribute("value");
						}
					}
				}

				searchResults.add(new WikiDataEntryData(label, description, wikiDataID));
			}
		}

		return searchResults;
	}

	//for the WikiData API we need to format the query string like
	// id1|id2|id3|...|id(n)
	private String formatIDsForQuery(List<String> ids){
		String query = "";
		for (String wikiDataID : ids){
			query += wikiDataID + "|";
		}
		//remove last |
		if (query.length() > 0)
			query = query.substring(0,query.length()-1);

		return query;
	}
	
}
