package com.linnca.pelicann.connectors;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

//note: we can install this program on our own server
//if we end up using a server

public class PronunciationAPIConnector implements EndpointConnectorReturnsJSON {
    public PronunciationAPIConnector(){}

    @Override
    public JSONObject fetchJSONObjectFromGetRequest(String... parameters) throws Exception{
        HttpURLConnection conn = formatHttpConnection(parameters);

        InputStream is = conn.getInputStream();
        //convert input stream to string
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String str = s.hasNext() ? s.next() : "";

        JSONArray jsonArray = new JSONArray(str);

        return null;
    }

    //we are fetching an array, not object
    //so fix the implementation later
    public JSONArray fetchJSONArrayFromGetRequest(String... parameters) throws Exception{
        HttpURLConnection conn = formatHttpConnection(parameters);

        InputStream is = conn.getInputStream();
        //convert input stream to string
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String str = s.hasNext() ? s.next() : "";

        return new JSONArray(str);
    }

    private HttpURLConnection formatHttpConnection(String... parameterValue) throws Exception{
        String urlString = formatURL(parameterValue);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-agent", "Mozzila/5.0");

        return conn;
    }

    private String formatURL(String... parameter){
        String wordToQuery = parameter[0];
        return "http://yapi.ta2o.net/apis/mecapi.cgi?sentence="+wordToQuery+
                "&response=pronounciation&format=json";
    }
}
