package com.linnca.pelicann.searchinterests;

import android.content.Context;
import android.util.Log;

import com.linnca.pelicann.connectors.BingAlsoSearchedConnector;
import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataAPISearchConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.OnDBResultListener;
import com.linnca.pelicann.lessongenerator.StringUtils;
import com.linnca.pelicann.userinterests.WikiDataEntity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class RecommendationGetter {
    //to get connection status
    private Context context;
    //to get popular user interests
    private Database db;
    //to get recommendations from Bing
    private BingAlsoSearchedConnector bingConnector = new BingAlsoSearchedConnector();
    private int toDisplayRecommendationCt = 0;
    private int defaultRecommendationCt;
    //how many recommendations to add when the user loads
    // more recommendations
    private int loadMoreRecommendationCt;
    //we get more recommendations than we need
    // to guarantee populating recommendations,
    //so save the leftovers and when the user loads more,
    // we can check this first
    private List<WikiDataEntity> savedRecommendations = new ArrayList<>();
    private WikiDataEntity addedInterest = null;

    //the interface is to determine what will happen to the UI.
    //the results here should only contain enough items to display (not the entire list)
    public interface RecommendationGetterListener {
        void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton);
        void onNoConnection();
    }

    public RecommendationGetter(int defaultRecommendationCt, Context context, Database db, int loadMoreRecommendationCt){
        this.context = context;
        this.db = db;
        this.defaultRecommendationCt = defaultRecommendationCt;
        this.loadMoreRecommendationCt = loadMoreRecommendationCt;
    }

    int getToDisplayRecommendationCt(){
        return toDisplayRecommendationCt;
    }

    public void getNewRecommendations(final WikiDataEntity addedInterest, List<WikiDataEntity> userInterests,
                            final RecommendationGetterListener recommendationGetterListener){
        this.addedInterest = addedInterest;
        //reset the recommendation count to the default
        toDisplayRecommendationCt = defaultRecommendationCt;
        //getPopularWikidataEntities(userInterests, recommendationGetterListener);
        new Thread(){
            @Override
            public void run(){
                getPeopleAlsoSearchedFor(addedInterest.getLabel(), recommendationGetterListener);
            }
        }.start();

    }

    public void loadMoreRecommendations(List<WikiDataEntity> userInterests,
                                 RecommendationGetterListener recommendationGetterListener){
        toDisplayRecommendationCt += loadMoreRecommendationCt;
        //check first if we can still populate the recommendations with
        //the initial list we grabbed.
        if (savedRecommendations.size() > toDisplayRecommendationCt){
            //we technically can display the list if we have recommendations equal to the display count,
            // but then we don't know whether we should show the load more button or not.
            List<WikiDataEntity> toDisplay = new ArrayList<>(
                    savedRecommendations.subList(0, toDisplayRecommendationCt));
            recommendationGetterListener.onGetRecommendations(toDisplay, true);
        } else {
            //grab more from the database
            getPopularWikidataEntities(userInterests, recommendationGetterListener);
        }
    }

    private void getPopularWikidataEntities(final List<WikiDataEntity> userInterests,
                                    final RecommendationGetterListener recommendationGetterListener){
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestRankingsQueried(List<WikiDataEntity> rankings) {
                //clearing out all of the user interests is handled in the database,
                // but this guarantees that user interests will be left out
                rankings.removeAll(userInterests);
                //save all of the list for future use
                savedRecommendations.clear();
                savedRecommendations.addAll(rankings);

                boolean showLoadMoreButton = true;
                //if we have more than we need
                if (rankings.size() > toDisplayRecommendationCt){
                    rankings = rankings.subList(0, toDisplayRecommendationCt);
                } else {
                    showLoadMoreButton = false;
                }
                recommendationGetterListener.onGetRecommendations(rankings, showLoadMoreButton);
            }

            @Override
            public void onNoConnection(){
                recommendationGetterListener.onNoConnection();
            }
        };

        //we get the to display recommendation count + 1 so we know
        // whether we can load more recommendations.
        // we want to get at least the number of the user interests
        // so we guarantee that the returned list will contain new items
        int toGetUserInterestCt = userInterests.size() +
                toDisplayRecommendationCt + 1;
        db.getPopularUserInterests(context, toGetUserInterestCt, onDBResultListener);
    }

    private void getPeopleAlsoSearchedFor(final String addedInterestLabel, final RecommendationGetterListener recommendationGetterListener){
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
                recommendationGetterListener.onNoConnection();
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
                    WikiDataEntity entity = new WikiDataEntity(label, "", wikiDataID, label, WikiDataEntity.CLASSIFICATION_NOT_SET);
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

    private Node findNodeByValue(Node node, String value){
        if (node != null){
            if (node.getTextContent() != null &&
                    node.getTextContent().startsWith(value)){
                return node;
            }
            NodeList childNodes = node.getChildNodes();
            int childNodeCt = childNodes.getLength();
            for (int i=0; i<childNodeCt; i++){
                Node childNode = childNodes.item(i);
                if (childNode != null){
                    childNode = findNodeByValue(childNode, value);
                    if (childNode != null)
                        return childNode;
                }
            }
        }

        return null;
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
