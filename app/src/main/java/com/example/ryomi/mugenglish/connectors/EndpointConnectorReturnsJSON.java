package com.example.ryomi.mugenglish.connectors;


import org.json.JSONObject;

interface EndpointConnectorReturnsJSON {
    JSONObject fetchJSONObjectFromGetRequest(String... query) throws Exception;
}
