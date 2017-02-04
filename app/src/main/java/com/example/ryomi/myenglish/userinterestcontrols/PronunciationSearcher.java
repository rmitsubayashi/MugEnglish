package com.example.ryomi.myenglish.userinterestcontrols;

import com.example.ryomi.myenglish.connectors.PronunciationAPIConnector;
import com.example.ryomi.myenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.connectors.WikiDataSPARQLConnector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PronunciationSearcher {
    private WikiDataSPARQLConnector connector =
            new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE);

    public PronunciationSearcher(){}

    public String getPronunciationFromWikiBase(String wikiDataID) throws Exception{
        String result = "";
        String query = getQuery(wikiDataID);
        Document resultDOM = connector.fetchDOMFromGetRequest(query);

        NodeList nodeList = resultDOM.getElementsByTagName("result");
        int nodeCt = nodeList.getLength();
        //this should not happen since we are asking for the label of a given id
        // but just in case
        if (nodeCt != 0){
            //first and only item
            Node n = nodeList.item(0);
            String pronunciation = SPARQLDocumentParserHelper.findValueByNodeName(n, "pronunciationLabel");
            if (pronunciation.equals(""))
                pronunciation = SPARQLDocumentParserHelper.findValueByNodeName(n, "entityLabel");

            //set pronunciation to ひらがな.
            // Since the ordering of Firebase is on lexicographical ordering,
            // "カラオケ"　will come after "まくら"
            pronunciation = zenkakuKatakanaToZenkakuHiragana(pronunciation);

            result = pronunciation;

        }

        //if the result still contains 漢字
        //search again using the mecap api
        if (containsKanji(result)){

        }

        return result;
    }

    public String getPronunciationFromMecap(String query) throws Exception{
        PronunciationAPIConnector apiConnector = new PronunciationAPIConnector();
        JSONArray jsonArray = apiConnector.fetchJSONArrayFromGetRequest(query);
        int arrayLength = jsonArray.length();
        String pronunciation = "";
        for (int i=0; i<arrayLength; i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            pronunciation += jsonObject.getString("pronounciation");
        }

        //all returned queries are in カタカナ
        pronunciation = zenkakuKatakanaToZenkakuHiragana(pronunciation);
        System.out.println("mecap: " + pronunciation);
        return pronunciation;
    }

    //make this public so we can access this
    // even if the connection fails for some reason
    public String zenkakuKatakanaToZenkakuHiragana(String s) {
        StringBuilder sb = new StringBuilder(s);
        int length = sb.length();
        for (int i = 0; i < length; i++) {
            char c = sb.charAt(i);
            if (c >= 'ァ' && c <= 'ン') {
                sb.setCharAt(i, (char)(c - 'ァ' + 'ぁ'));
            } else if (c == 'ヵ') {
                sb.setCharAt(i, 'か');
            } else if (c == 'ヶ') {
                sb.setCharAt(i, 'け');
            } else if (c == 'ヴ') {
                sb.setCharAt(i, 'う');
                sb.insert(i + 1, '゛');
                i++;
            }
        }
        return sb.toString();
    }

    //making this public for the same reason
    public boolean containsKanji(String s){
        int length = s.length();
        for (int i=0; i < length; i++){
            char c = s.charAt(i);
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS){
                return true;
            }
        }

        return false;
    }

    private String getQuery(String wikiDataID){
        return "SELECT DISTINCT ?entityLabel ?pronunciationLabel " +
                "WHERE " +
                "{ " +
                "  ?entity ?property ?value . " +
                "  OPTIONAL {?entity wdt:P1814 ?pronunciation} . " +
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                    WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" +
                    WikiBaseEndpointConnector.ENGLISH + "' } . " +
                "  BIND (wd:" + wikiDataID + " as ?entity) " +
                "} " +
                "LIMIT 1";
    }
}
