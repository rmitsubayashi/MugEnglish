package com.linnca.pelicann.userinterestcontrols;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.WikiDataAPIGetConnector;
import com.linnca.pelicann.connectors.WikiDataAPISearchConnector;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

class EntityGetter {
    /*
    * The only reason we have two connectors is because the search API
    * for WikiData doesn't return the Japanese description (only Japanese label)
    * so we have to make another request to the WikiData API to get the
    * Japanese description
    * */
    private final EndpointConnectorReturnsXML getConnector;

    public EntityGetter(WikiDataAPIGetConnector getConnector){
        this.getConnector = getConnector;
    }

    //検索結果を返す
    public List<WikiDataEntryData> get(String... ids) throws Exception{
        List<WikiDataEntryData> searchResults = new ArrayList<>();
        String query = this.formatIDsForQuery(ids);
        Document resultDOM = getConnector.fetchDOMFromGetRequest(query);
        NodeList resultNodes = resultDOM.getElementsByTagName(WikiDataAPISearchConnector.ENTITY_TAG);

        int nodeCt = resultNodes.getLength();
        for (int i=0; i<nodeCt; i++){
            Node n = resultNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String wikiDataID = "";
                String label = "";
                String description = "";

                Element e = (Element)n;
                if (e.hasAttribute("id")) {
                    wikiDataID = e.getAttribute("id");
                }

                //the rest are in tags
                NodeList labelNodes = e.getElementsByTagName("label");
                //only one item
                Node labelNode = labelNodes.item(0);
                if (n.getNodeType() == Node.ELEMENT_NODE){
                    Element labelE =(Element)labelNode;
                    if (labelE.hasAttribute("value"))
                        label = labelE.getAttribute("value");
                }

                NodeList descriptionNodes = e.getElementsByTagName("description");
                //this might be empty if there is no description
                if (descriptionNodes.getLength() != 0) {
                    //only one item
                    Node descriptionNode = descriptionNodes.item(0);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        Element descriptionE = (Element) descriptionNode;
                        if (descriptionE.hasAttribute("value"))
                            description = descriptionE.getAttribute("value");
                    }
                }

                //set pronunciation to label for now.
                //if there is a pronunciation field, use that
                searchResults.add(new WikiDataEntryData(label, description, wikiDataID, label, WikiDataEntryData.CLASSIFICATION_NOT_SET));
            }

        }

        return searchResults;
    }

    //for the WikiData API we need to format the query string like
    // id1|id2|id3|...|id(n)
    private String formatIDsForQuery(String... ids){
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
