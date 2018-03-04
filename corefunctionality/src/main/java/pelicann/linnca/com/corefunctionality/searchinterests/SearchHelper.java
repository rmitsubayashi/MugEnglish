package pelicann.linnca.com.corefunctionality.searchinterests;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataAPISearchConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

/* We want to search using the wbsearchentities call
*  because it's fast. However, we won't be able to filter
*  for just people. So, for each result,
*  send a SPARQL query and check if it's a person
* */

public class SearchHelper {
    //helps search
    private WikiBaseEndpointConnector searchConnector;
    //checks if person
    private WikiBaseEndpointConnector sparqlConnector;
    private AtomicInteger searchRequestCt = new AtomicInteger(0);
    private ScheduledThreadPoolExecutor executor;
    private List<ScheduledFuture> queuedTasks = new ArrayList<>();

    public interface SearchHelperListener {
        void onSuccess(List<WikiDataEntity> resultList);
        void onFailure();
    }

    public SearchHelper(WikiBaseEndpointConnector searchConnector,
                        WikiBaseEndpointConnector sparqlConnector){
        this.searchConnector = searchConnector;
        this.sparqlConnector = sparqlConnector;
        this.executor = new ScheduledThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors()
        );
    }

    public void search(final String query, final SearchHelperListener listener){
        //update the latest request number
        final int currentSearchRequestCt = searchRequestCt.incrementAndGet();
        //every thread in the thread pool should be removed since this is
        // the newest search request (other search requests are not relevant anymore)
        for (ScheduledFuture scheduledTask : queuedTasks){
            //boolean is whether we should interrupt the thread if running
            scheduledTask.cancel(true);
        }
        queuedTasks.clear();

        //wait some time before sending the request
        //so we have a better idea of whether the user is finished typing
        ScheduledFuture future = executor.schedule(new Runnable() {
            @Override
            public void run() {
                sendSearchRequest(query, currentSearchRequestCt, listener);
            }
        }, 500, TimeUnit.MILLISECONDS);

        queuedTasks.add(future);
    }

    private void sendSearchRequest(String query, final int requestNumber, final SearchHelperListener listener){
        //if this isn't the latest request, don't search since we only want to show
        // the most recent search
        if (searchRequestCt.get() > requestNumber){
            return;
        }
        EndpointConnectorReturnsXML.OnFetchDOMListener onFetchDOMListener = new EndpointConnectorReturnsXML.OnFetchDOMListener() {
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
                List<String> searchResultWikidataIDs = new ArrayList<>();
                int nodeCt = resultNodes.getLength();
                for (int i=0; i<nodeCt; i++){
                    Node n = resultNodes.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE)
                    {
                        String wikiDataID = "";

                        Element e = (Element)n;
                        if (e.hasAttribute("id")) {
                            wikiDataID = e.getAttribute("id");
                            searchResultWikidataIDs.add(wikiDataID);
                        }
                    }
                }
                //also check the most recent here.
                //if not the most recent at this point, don't update the UI
                if (requestNumber < searchRequestCt.get()){
                    return;
                }
                checkIfPeople(searchResultWikidataIDs, requestNumber, listener);
            }

            @Override
            public void onError(){
                listener.onFailure();
            }
        };
        List<String> queryList = new ArrayList<>(1);
        queryList.add(query);
        searchConnector.fetchDOMFromGetRequest(onFetchDOMListener, queryList);
    }

    public void removeWikiNewsArticlePages(List<WikiDataEntity> result){
        for (Iterator<WikiDataEntity> iterator = result.iterator(); iterator.hasNext();){
            WikiDataEntity data = iterator.next();
            String description = data.getDescription();
            //not sure if these cover every case
            if (description != null &&
                    (description.equals("ウィキニュースの記事") ||
                            description.equals("Wikinews article"))
                    ){
                iterator.remove();
            }
        }
    }

    //after getting search results, check if each of the search results is a person.
    //when we are done checking through every result,
    // update the UI
    private void checkIfPeople(final List<String> wikidataIDs, final int requestNumber, final SearchHelperListener listener){
        List<String> queries = new ArrayList<>(wikidataIDs.size());
        for (String wikidataID : wikidataIDs){
            String query = getPersonSearchQuery(wikidataID);
            queries.add(query);
        }
        EndpointConnectorReturnsXML.OnFetchDOMListener onFetchDOMListener = new EndpointConnectorReturnsXML.OnFetchDOMListener() {
            private AtomicInteger count = new AtomicInteger(wikidataIDs.size());
            private List<WikiDataEntity> peopleResults = Collections.synchronizedList(new ArrayList<WikiDataEntity>(wikidataIDs.size()));
            @Override
            public boolean shouldStop() {
                System.out.println(requestNumber < searchRequestCt.get());
                //if not the most recent at this point, we can stop here
                return requestNumber < searchRequestCt.get();
            }

            @Override
            public void onStop() {
                System.out.println("TAG onStop()");
                //also check the most recent here.
                //if not the most recent at this point, don't update the UI
                if (requestNumber < searchRequestCt.get()){
                    return;
                }

                for (WikiDataEntity entity : peopleResults){
                    System.out.println("TAG " + entity.getLabel());
                }
                listener.onSuccess(peopleResults);
            }

            @Override
            public void onFetchDOM(Document result) {
                NodeList allResults = result.getElementsByTagName(
                        WikiDataSPARQLConnector.RESULT_TAG
                );
                int resultLength = allResults.getLength();
                if (resultLength > 0) {
                    Node head = allResults.item(0);
                    String id = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");
                    id = WikiDataEntity.getWikiDataIDFromReturnedResult(id);
                    String label = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
                    System.out.println("is person " + label);
                    String description = SPARQLDocumentParserHelper.findValueByNodeName(head, "personDescription");
                    //set pronunciation to label for now
                    peopleResults.add(new WikiDataEntity(label, description, id, label));
                }

                if (count.decrementAndGet() == 0){
                    this.onStop();
                }
            }

            @Override
            public void onError() {
                listener.onFailure();
            }
        };



        sparqlConnector.fetchDOMFromGetRequest(onFetchDOMListener, queries);
    }

    private String getPersonSearchQuery(String wikidataID){
        return "SELECT ?person ?personLabel ?personDescription " +
                "WHERE " +
                "{" +
                "  {?person wdt:P31 wd:Q5} " + //is a person
                "  UNION {?person wdt:P31/wdt:P279* wd:Q15632617} " + //or a fictional person
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "  BIND (wd:" + wikidataID + " as ?person)" +
                "}";
    }

    public void removeDisambiguationPages(List<WikiDataEntity> result){
        for (Iterator<WikiDataEntity> iterator = result.iterator(); iterator.hasNext();){
            WikiDataEntity data = iterator.next();
            String description = data.getDescription();
            //not sure if these cover every case
            if (description != null &&
                    (description.equals("ウィキペディアの曖昧さ回避ページ") ||
                            description.equals("ウィキメディアの曖昧さ回避ページ") ||
                            description.equals("Wikipedia disambiguation page") ||
                            description.equals("Wikimedia disambiguation page"))
                    ){
                iterator.remove();
            }
        }
    }
}
