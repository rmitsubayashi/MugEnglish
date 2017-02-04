package com.example.ryomi.myenglish.connectors;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class FacebookAPIConnector implements EndpointConnectorReturnsJSON {
    public static String SELF_USER_ID = "me";

    public FacebookAPIConnector(){}

    @Override
    public JSONObject fetchJSONObjectFromGetRequest(String... params) throws Exception {
        HttpURLConnection conn = formatHttpConnection(params);

        InputStream is = conn.getInputStream();
        //convert input stream to string
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String str = s.hasNext() ? s.next() : "";

        JSONObject jsonObject = new JSONObject(str);

        return jsonObject;
    }

    private HttpURLConnection formatHttpConnection(String... parameterValue) throws Exception{
        String urlString = formatURL(parameterValue);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-agent", "Mozzila/5.0");

        return conn;
    }

    protected String formatURL(String... params){
        String accessToken = params[0];
        String pageID = params[1];
        String fields = params[2];

        return "https://graph.facebook.com/"+pageID+"?fields="+fields+"&access_token="+accessToken;
    }
}
