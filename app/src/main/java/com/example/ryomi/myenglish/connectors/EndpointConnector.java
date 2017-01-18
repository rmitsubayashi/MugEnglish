package com.example.ryomi.myenglish.connectors;

import org.w3c.dom.Document;

public interface EndpointConnector {
    Document fetchDOMFromGetRequest(String... query) throws Exception;
}
