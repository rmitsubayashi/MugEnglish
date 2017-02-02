package com.example.ryomi.myenglish.connectors;


import org.json.JSONObject;

public interface EndpointConnectorReturnsJSON {
    JSONObject fetchJSONObjectFromGetRequest(String... query) throws Exception;
}
