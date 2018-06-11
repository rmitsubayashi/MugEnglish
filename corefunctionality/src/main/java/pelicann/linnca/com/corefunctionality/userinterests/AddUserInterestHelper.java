package pelicann.linnca.com.corefunctionality.userinterests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsJSON;
import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.connectors.PronunciationAPIConnector;
import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessonscript.StringUtils;

//search for relevant pronunciation and classification.
//the pronunciation helps with ordering the user's interests, and
//the classification helps sort the user's interests and also helps
// find appropriate interests to use in lesson generation
public class AddUserInterestHelper {
    private final Database db;
    private final WikiBaseEndpointConnector wikiBaseEndpointConnector = new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE);

    public AddUserInterestHelper(Database db){
        this.db = db;
    }

    public void addPronunciation(WikiDataEntity dataToAdd){
        if (dataToAdd == null) {
            return;
        }
        //current pronunciation should be the same as the label
        String pronunciation = dataToAdd.getPronunciation();
        //this means we don't have to attempt to find the pronunciation of the Kanji
        if (!StringUtils.containsKanji(pronunciation)){
            System.out.println("not kanji");
            //There are two scenarios here.
            //1. the pronunciation only contains Hiragana / Katakana
            //2. the pronunciation is in English (the label is in English).
            //If the pronunciation is in English, no reason to try and get a Japanese
            // pronunciation so leave it in English so we can order in English.

            //is in English, so don't do anything.
            //alphabet will automatically be sorted alphabetically before all Japanese
            // pronunciation (like the Android app icon sorting)
            if (!StringUtils.isAlphanumeric(pronunciation)) {
                //we want all the pronunciation to be in hiragana so we can order
                // lexicographically
                String toHiragana = StringUtils.zenkakuKatakanaToZenkakuHiragana(pronunciation);
                //update only if the new pronunciation is different.
                //this is to reduce calls to the DB
                if (!toHiragana.equals(pronunciation)){
                    db.setPronunciation(dataToAdd.getWikiDataID(), toHiragana);
                }
            }
        } else {
            searchPronunciation(dataToAdd.getWikiDataID());
        }

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
                System.out.println("Called wikidata");
                NodeList nodeList = result.getElementsByTagName(WikiDataSPARQLConnector.RESULT_TAG);
                int nodeCt = nodeList.getLength();
                //this should not happen since we are asking for the label of a given id
                // but just in case
                if (nodeCt != 0){
                    //first and only item
                    Node n = nodeList.item(0);
                    String pronunciation = SPARQLDocumentParserHelper.findValueByNodeName(n, "pronunciationLabel");
                    if (pronunciation == null || pronunciation.equals("")) {
                        pronunciation = SPARQLDocumentParserHelper.findValueByNodeName(n, "entityLabel");
                    }

                    //set pronunciation to ひらがな.
                    // Since the ordering of Firebase is lexicographical,
                    // "カラオケ"　will come after "まくら"
                    pronunciation = StringUtils.zenkakuKatakanaToZenkakuHiragana(pronunciation);
                    System.out.println(pronunciation);
                    if (StringUtils.containsKanji(pronunciation)){
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
                pronunciationResult = StringUtils.zenkakuKatakanaToZenkakuHiragana(pronunciationResult);
                db.setPronunciation(userInterestID, pronunciationResult);
            }
        };
        List<String> queryList = new ArrayList<>();
        queryList.add(word);
        apiConnector.fetchJSONArrayFromGetRequest(onFetchJSONListener, queryList);

    }

    private String getPronunciationQuery(String wikiDataID){
        return "SELECT DISTINCT ?entityLabel ?pronunciationLabel " +
                "WHERE " +
                "{ " +
                "  BIND (wd:" + wikiDataID + " as ?entity) . " +
                "  OPTIONAL {?entity wdt:P1814 ?pronunciation} . " +
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" +
                WikiBaseEndpointConnector.ENGLISH + "' } . " +
                "} " +
                "LIMIT 1";
    }
}
