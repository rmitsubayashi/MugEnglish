package pelicann.linnca.com.corefunctionality.searchinterests;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.BingAlsoSearchedConnector;
import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataAPISearchConnector;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class SimilarUserInterestGetter {
    //to get popular user interests
    private final Database db;
    //to get recommendations from Bing
    private final BingAlsoSearchedConnector bingConnector = new BingAlsoSearchedConnector();
    private WikiDataEntity addedInterest = null;

    //the interface is to determine what will happen to the UI.
    //the results here should only contain enough items to display (not the entire list)
    public interface SimilarUserInterestGetterListener {
        void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton);
        void onNoConnection();
    }

    public SimilarUserInterestGetter(Database db){
        this.db = db;
    }

    public void getNewRecommendations(final WikiDataEntity addedInterest,
                                      final SimilarUserInterestGetterListener SimilarUserInterestGetterListener){
        if (addedInterest == null || addedInterest.getLabel() == null){
            return;
        }
        this.addedInterest = addedInterest;
        //getPopularWikidataEntities(userInterests, SimilarUserInterestGetterListener);
        new Thread(){
            @Override
            public void run(){
                getPeopleAlsoSearchedFor(addedInterest.getLabel(), SimilarUserInterestGetterListener);
            }
        }.start();

    }

    private void getPeopleAlsoSearchedFor(final String addedInterestLabel, final SimilarUserInterestGetterListener SimilarUserInterestGetterListener){
        EndpointConnectorReturnsXML.OnFetchDOMListener listener = new EndpointConnectorReturnsXML.OnFetchDOMListener() {
            @Override
            public boolean shouldStop() {
                return true;
            }

            @Override
            public void onStop() {

            }

            @Override
            public void onFetchDOM(Document result) {
                List<String> alsoSearchedForLabels = new ArrayList<>(5);
                findAlsoSearchedForLabels(result.getDocumentElement(), "a", "title", alsoSearchedForLabels);
                //since these are only labels,
                // we need to search -> add
                for (String label : alsoSearchedForLabels){
                    search(label);
                }

            }

            @Override
            public void onError() {
                SimilarUserInterestGetterListener.onNoConnection();
            }
        };

        List<String> query = new ArrayList<>(1);
        query.add(addedInterestLabel);
        bingConnector.fetchDOMFromGetRequest(listener, query);

    }

    private void search(String label){
        EndpointConnectorReturnsXML.OnFetchDOMListener listener = new EndpointConnectorReturnsXML.OnFetchDOMListener() {
            @Override
            public boolean shouldStop() {
                return false;
            }

            @Override
            public void onStop() {

            }

            @Override
            public void onFetchDOM(Document result) {
                NodeList resultNodes = result.getElementsByTagName(WikiDataAPISearchConnector.ENTITY_TAG);
                int nodeCt = resultNodes.getLength();
                if (nodeCt == 0)
                    return;

                Node n = resultNodes.item(0);
                if (n.getNodeType() == Node.ELEMENT_NODE)
                {
                    String wikiDataID = "";
                    String label = "";

                    Element e = (Element)n;
                    if (e.hasAttribute("id")) {
                        wikiDataID = e.getAttribute("id");
                    }
                    if(e.hasAttribute("label")) {
                        label = e.getAttribute("label");
                    }

                    //we don't need the description
                    WikiDataEntity entity = new WikiDataEntity(label, "", wikiDataID, label);
                    db.addSimilarInterest(addedInterest.getWikiDataID(), entity);
                    db.addSimilarInterest(entity.getWikiDataID(), addedInterest);
                }
            }

            @Override
            public void onError() {

            }
        };
        List<String> labelList = new ArrayList<>(1);
        labelList.add(label);
        //only need the first one
        WikiDataAPISearchConnector connector = new WikiDataAPISearchConnector(WikiBaseEndpointConnector.JAPANESE, 1);
        connector.fetchDOMFromGetRequest(listener, labelList);
    }

    private void findAlsoSearchedForLabels(Node node, String tag, String attribute, List<String> result){
        if (node == null){
            return;
        }

        if (node.getNodeType() == Node.ELEMENT_NODE &&
                node.getNodeName().equals(tag)){
            Element e = (Element)node;
            if (e.hasAttribute(attribute)){
                String text = e.getAttribute(attribute);
                result.add(text);
            }
        }

        NodeList childNodes = node.getChildNodes();
        int childNodeCt = childNodes.getLength();
        for (int i=0; i<childNodeCt; i++){
            Node childNode =childNodes.item(i);
            findAlsoSearchedForLabels(childNode, tag, attribute, result);
        }
    }

}
