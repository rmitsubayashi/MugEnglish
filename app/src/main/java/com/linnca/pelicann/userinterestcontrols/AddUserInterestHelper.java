package com.linnca.pelicann.userinterestcontrols;

import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.List;

//search for relevant pronunciation and then add.
//we are sorting by pronunciation now, but later we might classify more generally like
//people, places, etc.
public class AddUserInterestHelper {
    private final Database db = new FirebaseDB();
    private final PronunciationSearcher pronunciationSearcher = new PronunciationSearcher();
    private final WikiBaseEndpointConnector wikiBaseEndpointConnector = new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE);

    public void findPronunciationAndCategoryThenAdd(List<WikiDataEntryData> dataToAdd, OnResultListener onResultListener){
        PronunciationCategorySearchThread thread = new PronunciationCategorySearchThread(dataToAdd, onResultListener);
        thread.start();
    }

    private class PronunciationCategorySearchThread extends Thread {
        private List<WikiDataEntryData> dataToAdd;
        private OnResultListener onResultListener;
        PronunciationCategorySearchThread(List<WikiDataEntryData> data, OnResultListener listener){
            super();
            this.dataToAdd = data;
            this.onResultListener = listener;
        }

        @Override
        public void run(){
            if (dataToAdd == null || dataToAdd.size() == 0)
                return;
            for (WikiDataEntryData data : dataToAdd) {
                String pronunciation;
                try {
                    pronunciation = pronunciationSearcher.getPronunciationFromWikiBase(data.getWikiDataID());
                } catch (Exception e) {
                    e.printStackTrace();
                    pronunciation = pronunciationSearcher.zenkakuKatakanaToZenkakuHiragana(data.getLabel());
                }

                if (pronunciationSearcher.containsKanji(pronunciation)) {
                    try {
                        pronunciation = pronunciationSearcher.getPronunciationFromMecap(pronunciation);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                data.setPronunciation(pronunciation);

                boolean classificationSet = false;
                try {
                    Document resultDOM = wikiBaseEndpointConnector.fetchDOMFromGetRequest(getPersonSearchQuery(data.getWikiDataID()));
                    NodeList allResults = resultDOM.getElementsByTagName(
                            WikiDataSPARQLConnector.RESULT_TAG
                    );
                    int resultLength = allResults.getLength();
                    if (resultLength > 0) {
                        data.setClassification(WikiDataEntryData.CLASSIFICATION_PERSON);
                        classificationSet = true;
                    }

                    if (!classificationSet) {
                        resultDOM = wikiBaseEndpointConnector.fetchDOMFromGetRequest(getPlaceSearchQuery(data.getWikiDataID()));
                        allResults = resultDOM.getElementsByTagName(
                                WikiDataSPARQLConnector.RESULT_TAG
                        );
                        resultLength = allResults.getLength();
                        if (resultLength > 0) {
                            data.setClassification(WikiDataEntryData.CLASSIFICATION_PLACE);
                            classificationSet = true;
                        }
                    }
                    if (!classificationSet) {
                        data.setClassification(WikiDataEntryData.CLASSIFICATION_OTHER);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            db.addUserInterests(dataToAdd, onResultListener);
        }
    }

    private String getPersonSearchQuery(String wikidataID){
        return "SELECT ?person " +
                "WHERE " +
                "{" +
                "  {?person wdt:P31 wd:Q5} " +
                "  UNION {?person wdt:P31/wdt:P279* wd:Q15632617} " +
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language 'en','ja'. } . " +
                "  BIND (wd:" + wikidataID + " as ?person)" +
                "}";
    }

    private String getPlaceSearchQuery(String wikidataID){
        return "SELECT DISTINCT ?place " +
                "WHERE " +
                "{" +
                "  {?place wdt:P31/wdt:P279* wd:Q2221906} " +
                "  UNION {?place wdt:P31/wdt:P279* wd:Q3895768} . " +
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language 'en,ja' . } " +
                "  BIND (wd:" + wikidataID + " as ?place) " +
                "}";
    }
}
