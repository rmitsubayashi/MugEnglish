package com.linnca.pelicann.connectors;

import org.w3c.dom.Document;

import java.util.List;

public interface EndpointConnectorReturnsXML {
    interface OnFetchDOMListener {
        boolean shouldStop();
        void onStop();
        void onFetchDOM(Document result);
    }
    void fetchDOMFromGetRequest(OnFetchDOMListener listener, List<String> query);
}
