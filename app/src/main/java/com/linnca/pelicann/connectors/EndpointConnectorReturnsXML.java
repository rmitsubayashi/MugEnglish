package com.linnca.pelicann.connectors;

import org.w3c.dom.Document;

public interface EndpointConnectorReturnsXML {
    Document fetchDOMFromGetRequest(String... query) throws Exception;
}
