package com.linnca.pelicann.connectors;


import org.json.JSONObject;

interface EndpointConnectorReturnsJSON {
    JSONObject fetchJSONObjectFromGetRequest(String... query) throws Exception;
}
