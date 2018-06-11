package pelicann.linnca.com.corefunctionality.connectors;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import pelicann.linnca.com.corefunctionality.lessonscript.StringUtils;

public class BingAlsoSearchedConnector implements EndpointConnectorReturnsXML {

    public void fetchDOMFromGetRequest(OnFetchDOMListener listener, List<String> query){
        try {
            //we only search one
            HttpURLConnection conn = formatHttpConnection(query.get(0));
            InputStream resultInputStream = fetchHttpConnectionResponse(conn);
            //we can't parse the DOM directly because there is dirty HTML
            // (i.e. <a> without close tag).
            //cleaning it (via jtidy) takes too long (~5 seconds)
            String valueString;
            //the exact string displayed for the label on Bing
            if (StringUtils.containsJapanese(query.get(0))){
                valueString = "他の人は以下も";
            } else {
                valueString = "People also searched";
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(resultInputStream));
            StringBuilder result = new StringBuilder();
            String line;
            boolean titleLines = false;
            while((line = reader.readLine()) != null) {
                if (line.contains(valueString)){
                    titleLines = true;
                    int index = line.indexOf(valueString);
                    line = line.substring(index, line.length());
                }

                if (titleLines){
                    if (line.contains("<a title=")){
                        result.append(line);
                    }
                    if (line.contains("</ul>")){
                        break;
                    }
                }
            }

            String resultString = result.toString();
            int ulStartIndex = resultString.indexOf("<ul ");
            String ulStart = resultString.substring(ulStartIndex, resultString.length());
            int ulEndIndex = ulStart.indexOf("</ul>");
            String ulString = ulStart.substring(0, ulEndIndex+5);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(ulString)));
            document.getDocumentElement().normalize();
            listener.onFetchDOM(document);
        } catch (Exception e){
            e.printStackTrace();
            listener.onError();
        }
    }

    //should handle the exceptions better (look into this later)
    private HttpURLConnection formatHttpConnection(String parameterValue) throws Exception {
        String urlString = formatURL(parameterValue);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-agent", "Mozzila/5.0");

        return conn;
    }

    private String formatURL(String parameterValue) {
        //get language.
        //bing doesn't show 'people also searched' if the input language and search engine language
        // are mismatched
        String searchLanguage;
        if (StringUtils.containsJapanese(parameterValue)){
            searchLanguage = "ja-jp";
        } else {
            searchLanguage = "en-us";
        }
        String encodedSearchText;
        try {
            encodedSearchText = URLEncoder.encode(parameterValue, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
            encodedSearchText = parameterValue;
        }
        return "https://www.bing.com/search?q=" + encodedSearchText +
                "&setlang=" + searchLanguage;
    }


    private InputStream fetchHttpConnectionResponse(HttpURLConnection conn) throws Exception{
        return conn.getInputStream();
    }
}
