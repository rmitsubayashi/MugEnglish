package com.linnca.pelicann.searchinterests;

import android.os.Handler;
import android.os.Message;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.WikiDataAPISearchConnector;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class SearchHelper {
    private EndpointConnectorReturnsXML connector;
    private AtomicInteger searchRequestCt = new AtomicInteger(0);
    private ScheduledThreadPoolExecutor executor;
    private List<ScheduledFuture> queuedTasks = new ArrayList<>();

    SearchHelper(EndpointConnectorReturnsXML connector){
        this.connector = connector;
        this.executor = new ScheduledThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors()
        );
    }

    void search(final Handler handler, final String query){
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
                sendSearchRequest(handler, query, currentSearchRequestCt);
            }
        }, 500, TimeUnit.MILLISECONDS);

        queuedTasks.add(future);
    }

    private void sendSearchRequest(final Handler handler, String query, final int requestNumber){
        //if this isn't the latest request, don't search since we only want to show
        // the most recent search
        if (searchRequestCt.get() != requestNumber){
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
                List<WikiDataEntryData> searchResults = new ArrayList<>();
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

                        //set pronunciation to label for now
                        searchResults.add(new WikiDataEntryData(label, description, wikiDataID, label, WikiDataEntryData.CLASSIFICATION_NOT_SET));
                    }
                }
                //also check the most recent here.
                //if not the most recent at this point, don't update the UI
                if (requestNumber != searchRequestCt.get()){
                    return;
                }

                Message message = handler.obtainMessage(0, searchResults);
                message.sendToTarget();
            }
        };
        List<String> queryList = new ArrayList<>(1);
        queryList.add(query);
        connector.fetchDOMFromGetRequest(onFetchDOMListener, queryList);
    }
}
