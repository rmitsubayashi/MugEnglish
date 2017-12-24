package com.linnca.pelicann.userinterestcontrols;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsJSON;
import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.PronunciationAPIConnector;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.lessongenerator.StringUtils;
import com.linnca.pelicann.userinterests.WikiDataEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//search for relevant pronunciation and classification.
//the pronunciation helps with ordering the user's interests, and
//the classification helps sort the user's interests and also helps
// find appropriate interests to use in lesson generation
public class AddUserInterestHelper {
    private final Database db = new FirebaseDB();
    private final WikiBaseEndpointConnector wikiBaseEndpointConnector = new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE);

    public void addPronunciation(WikiDataEntity dataToAdd){
        if (dataToAdd == null) {
            return;
        }
        //current pronunciation should be the same as the label
        String pronunciation = dataToAdd.getPronunciation();
        //this means we don't have to attempt to find the pronunciation of the Kanji
        if (!containsKanji(pronunciation)){
            //There are two scenarios here.
            //1. the pronunciation only contains Hiragana / Katakana
            //2. the pronunciation is in English (the label is in English).
            //If the pronunciation is in English, no reason to try and get a Japanese
            // pronunciation so leave it in English so we can order in English.

            //is in English, so don't do anything
            if (StringUtils.isAlphanumeric(pronunciation)){
                return;
            } else {
                //we want all the pronunciation to be in hiragana so we can order
                // lexicographically
                String toHiragana = zenkakuKatakanaToZenkakuHiragana(pronunciation);
                //update only if the new pronunciation is different
                if (!toHiragana.equals(pronunciation)){
                    db.setPronunciation(dataToAdd.getWikiDataID(), toHiragana);
                }
            }
        } else {
            searchPronunciation(dataToAdd.getWikiDataID());
        }

    }

    public void addClassification(WikiDataEntity dataToAdd){
        if (dataToAdd == null)
            return;

        final String dataID = dataToAdd.getWikiDataID();
        String query = getPersonSearchQuery(dataToAdd.getWikiDataID());
        String query2 = getPlaceSearchQuery(dataToAdd.getWikiDataID());
        List<String> queryList = new ArrayList<>(2);
        queryList.add(query);
        queryList.add(query2);
        EndpointConnectorReturnsXML.OnFetchDOMListener onFetchDOMListener = new EndpointConnectorReturnsXML.OnFetchDOMListener() {
            private AtomicInteger calledCt = new AtomicInteger(0);
            private AtomicBoolean matched = new AtomicBoolean(false);
            @Override
            public boolean shouldStop() {
                return false;
            }

            @Override
            public void onStop() {

            }

            @Override
            public void onFetchDOM(Document result) {
                NodeList allResults = result.getElementsByTagName(
                        WikiDataSPARQLConnector.RESULT_TAG
                );
                int resultLength = allResults.getLength();
                if (resultLength > 0) {
                    //the result can be either the person query or
                    // the place query.
                    String isPerson = SPARQLDocumentParserHelper.findValueByNodeName(allResults.item(0), "person");
                    if (isPerson != null && !isPerson.equals("")){
                        db.setClassification(dataID, WikiDataEntity.CLASSIFICATION_PERSON);
                    } else {
                        db.setClassification(dataID, WikiDataEntity.CLASSIFICATION_PLACE);
                    }
                    matched.set(true);
                }
                //2 = number of queries
                if (calledCt.incrementAndGet() == 2){
                    //since checking for called count and checking matched are done separately,
                    //there is a chance something will be called in between...
                    if (!matched.get()){
                        //check if we found a match.
                        //if no match, set the classification to other
                        db.setClassification(dataID, WikiDataEntity.CLASSIFICATION_OTHER);
                    }
                }
            }

            @Override
            public void onError(){

            }
        };
        wikiBaseEndpointConnector.fetchDOMFromGetRequest(onFetchDOMListener, queryList);

    }

    private void searchPronunciation(final String userInterestID){
        //first check wikiBase to see if there is a pronunciation field
        EndpointConnectorReturnsXML.OnFetchDOMListener onFetchDOMListener = new EndpointConnectorReturnsXML.OnFetchDOMListener() {
            @Override
            public boolean shouldStop() {
                return false;
            }

            @Override
            public void onStop() {

            }

            @Override
            public void onFetchDOM(Document result) {
                NodeList nodeList = result.getElementsByTagName(WikiDataSPARQLConnector.RESULT_TAG);
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
                    // Since the ordering of Firebase is lexicographical,
                    // "カラオケ"　will come after "まくら"
                    pronunciation = zenkakuKatakanaToZenkakuHiragana(pronunciation);

                    if (containsKanji(pronunciation)){
                        searchMecapiForPronunciation(userInterestID, pronunciation);
                    } else {
                        db.setPronunciation(userInterestID, pronunciation);
                    }

                }
            }

            @Override
            public void onError(){}
        };

        List<String> parameters = new ArrayList<>(1);
        parameters.add(getPronunciationQuery(userInterestID));
        wikiBaseEndpointConnector.fetchDOMFromGetRequest(onFetchDOMListener, parameters);
    }

    //Mecapi is a pronunciation API for Japanese
    private void searchMecapiForPronunciation(final String userInterestID, String word){
        PronunciationAPIConnector apiConnector = new PronunciationAPIConnector();
        EndpointConnectorReturnsJSON.OnFetchJSONListener onFetchJSONListener = new EndpointConnectorReturnsJSON.OnFetchJSONListener() {
            @Override
            public void onFetchJSONArray(JSONArray result) {
                int arrayLength = result.length();
                String pronunciationResult = "";
                for (int i=0; i<arrayLength; i++){
                    try {
                        JSONObject jsonObject = result.getJSONObject(i);
                        pronunciationResult += jsonObject.getString("pronounciation");
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }

                //all returned queries are in カタカナ
                pronunciationResult = zenkakuKatakanaToZenkakuHiragana(pronunciationResult);
                db.setPronunciation(userInterestID, pronunciationResult);
            }
        };
        List<String> queryList = new ArrayList<>();
        queryList.add(word);
        apiConnector.fetchJSONArrayFromGetRequest(onFetchJSONListener, queryList);

    }

    //not one query because multiple UNIONs time out
    private String getPersonSearchQuery(String wikidataID){
        return "SELECT ?person " +
                "WHERE " +
                "{" +
                "  {?person wdt:P31 wd:Q5} " + //is a person
                "  UNION {?person wdt:P31/wdt:P279* wd:Q15632617} " + //or a fictional person
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language 'en'. } . " +
                "  BIND (wd:" + wikidataID + " as ?person)" +
                "}";
    }

    private String getPlaceSearchQuery(String wikidataID){
        return "SELECT DISTINCT ?place " +
                "WHERE " +
                "{" +
                "  {?place wdt:P31/wdt:P279* wd:Q2221906} " +
                "  UNION {?place wdt:P31/wdt:P279* wd:Q3895768} . " +
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language 'en' . } " +
                "  BIND (wd:" + wikidataID + " as ?place) " +
                "}";
    }

    private String getPronunciationQuery(String wikiDataID){
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

    private boolean containsKanji(String s){
        int length = s.length();
        for (int i=0; i < length; i++){
            char c = s.charAt(i);
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS){
                return true;
            }
        }

        return false;
    }

    private String zenkakuKatakanaToZenkakuHiragana(String s) {
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
}
