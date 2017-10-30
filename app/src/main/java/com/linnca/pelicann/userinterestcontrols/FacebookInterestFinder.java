package com.linnca.pelicann.userinterestcontrols;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

//import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.linnca.pelicann.R;
import com.linnca.pelicann.connectors.FacebookAPIConnector;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataAPISearchConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FacebookInterestFinder {//extends IntentService{
    /*
    private static String TAG = "FacebookInterestFinder";
    public static final int SHALLOW_SEARCH = 1;
    public static final int DEEP_SEARCH = 2;

    public static final String BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_WORD = "FacebookInterestFinder WORD";
    //the string to display where we are searching (ie education, likes)
    public static final String BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_STRING = "FacebookInterestFinder PROGRESS STRING";
    //progress to show on the progress bar
    public static final String BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_PERCENT = "FacebookInterestFinder PROGRESS PERCENT";

    private final static String FACEBOOK_GENERAL_SEARCH = "P2013";
    private final static String FACEBOOK_LOCATION_SEARCH = "P1997";
    private final String fieldsForUserInfo =    "education," +
                                                "hometown," +
                                                "location," +
                                                "video.watches," +
                                                "video.wants_to_watch";

    private final String fieldsForPageInfo =    "link," +
                                                "website";

    private WikiBaseEndpointConnector wikiDataSPARQLConnector = new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE);
    private  WikiBaseEndpointConnector wikiDataSearchConnector = new WikiDataAPISearchConnector(WikiBaseEndpointConnector.JAPANESE);
    private FacebookAPIConnector facebookConnector = new FacebookAPIConnector();

    private int searchDepth;
    private int facebookEntityCt;
    //0~1. adjust when sending to the progress bar
    private double currentPercent = 0;

    public FacebookInterestFinder(){
        super("FacebookInterestFinder Service");
    }

    @Override
    protected void onHandleIntent(Intent workIntent){
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return;
        int depth = workIntent.getIntExtra("depth",SHALLOW_SEARCH);
        try {
            Set<WikiDataEntryData> result = findUserInterests(depth);
            addUserInterests(result);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            sendProgressPercentToUI(1);
        }
    }

    private Set<WikiDataEntryData> findUserInterests(int depth) throws Exception{
        searchDepth = depth;

        Set<WikiDataEntryData> results = new HashSet<>();

        //first search facebook for all interests
        AccessToken.refreshCurrentAccessTokenAsync();
        String accessToken = AccessToken.getCurrentAccessToken().getToken();
        String pageID = FacebookAPIConnector.SELF_USER_ID;
        String[] paramsForUserInfo = {accessToken, pageID, fieldsForUserInfo};

        JSONObject userInfo = facebookConnector.fetchJSONObjectFromGetRequest(paramsForUserInfo);
        facebookEntityCt = countEntities(userInfo);

        sendProgressStringToUI(getResources().getString(R.string.facebook_interests_search_schools));
        Set educationResult = searchEducation(userInfo);
        results.addAll(educationResult);
        //do multiple queries while displaying this string
        sendProgressStringToUI(getResources().getString(R.string.facebook_interests_search_locations));
        Set hometownResult = searchHometown(userInfo);
        results.addAll(hometownResult);
        sendProgressStringToUI("");
        Set locationResult = searchLocation(userInfo);
        results.addAll(locationResult);
        //also checked-in places

        for (WikiDataEntryData result : results){
            Log.d(TAG,result.getLabel());
        }

        return results;
    }

    private void addUserInterests(final Set<WikiDataEntryData> interests){
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.USER_INTERESTS + "/" + userID);
        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                for (WikiDataEntryData interest : interests) {
                    //add
                    UserInterestAdder userInterestAdder = new UserInterestAdder();
                    userInterestAdder.findPronunciationAndCategoryThenAdd(interest);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    //for loading screen
    private void sendProgressStringToUI(String str){
        Intent intent = new Intent(BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_STRING);
        intent.putExtra(BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_STRING, str);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendProgressPercentToUI(double percent){
        int adjustedPercent = (int)(percent * 100);
        Intent intent = new Intent(BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_PERCENT);
        intent.putExtra(BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_PERCENT, adjustedPercent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendWordToUI(String word){
        Intent intent = new Intent(BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_WORD);
        intent.putExtra(BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_WORD, word);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private int countEntities(JSONObject facebookObject){
        String facebookString = facebookObject.toString();
        Pattern namePattern = Pattern.compile("\"name\":");
        Matcher matcher = namePattern.matcher(facebookString);
        int ct = 0;
        while (matcher.find()){
            ct++;
        }

        return ct;
    }

     // first check the facebook url
     // then the official site link? then name?
     // not sure about the order of the last two, but we should do the facebook url first
     // since it has the most guaranteed match rate
     //
    //facebook query type is for when we query wikiData.
    //wikiData divides up the facebook ids into peoples/organizations and location id
    private WikiDataEntryData findWikiDataEntry(String name, String facebookID, String facebookQueryType) throws Exception{
        //search facebook url first
        AccessToken.refreshCurrentAccessTokenAsync();
        String accessToken = AccessToken.getCurrentAccessToken().getToken();
        String[] paramsForPageInfo = {accessToken, facebookID, fieldsForPageInfo};
        JSONObject pageInfo = facebookConnector.fetchJSONObjectFromGetRequest(paramsForPageInfo);

        //the facebook id on the wikiData db can either be the actual id
        // or a url to the facebook page
        //so check both
        String facebookIDQuery = searchByFacebookIDQuery(facebookID, facebookQueryType);
        Document facebookIDResults = wikiDataSPARQLConnector.fetchDOMFromGetRequest(facebookIDQuery);
        Set<WikiDataEntryData> facebookIDEntry = getEntriesFromSPARQL(facebookIDResults, "entry");
        if (facebookIDEntry.size() != 0){
            //return first (and only) data if it exists
            return facebookIDEntry.iterator().next();
        }

        String facebookURL = pageInfo.getString("link");
        facebookURL = stripOfficialSite(facebookURL);
        String facebookURLQuery = searchByFacebookIDQuery(facebookURL, facebookQueryType);
        Document facebookURLResults = wikiDataSPARQLConnector.fetchDOMFromGetRequest(facebookURLQuery);
        Set<WikiDataEntryData> facebookURLEntry = getEntriesFromSPARQL(facebookURLResults, "entry");
        if (facebookURLEntry.size() != 0){
            //return first (and only) data if it exists
            return facebookURLEntry.iterator().next();
        }


        if (pageInfo.has("website")) {
            //now search official site
            String officialSiteURL = pageInfo.getString("website");
            String officialSiteQuery = searchByOfficialSiteQuery(officialSiteURL);
            Document officialSiteResults = wikiDataSPARQLConnector.fetchDOMFromGetRequest(officialSiteQuery);
            Set<WikiDataEntryData> officialSiteEntry = getEntriesFromSPARQL(officialSiteResults, "entry");
            if (officialSiteEntry.size() != 0){
                return officialSiteEntry.iterator().next();
            }
            //we can also search for www.website.com/
            //because wikidata is pretty inconsistent with this
            String alternateOfficialSiteURL;
            if (officialSiteURL.endsWith("/")) {
                alternateOfficialSiteURL = officialSiteURL.substring(0, officialSiteURL.length() - 1);
            } else {
                alternateOfficialSiteURL = officialSiteURL + "/";
            }
            String alternateOfficialSiteQuery = searchByOfficialSiteQuery(alternateOfficialSiteURL);
            Document alternateOfficialSiteResults = wikiDataSPARQLConnector.fetchDOMFromGetRequest(alternateOfficialSiteQuery);
            Set<WikiDataEntryData> alternateOfficialSiteEntry = getEntriesFromSPARQL(alternateOfficialSiteResults, "entry");
            if (alternateOfficialSiteEntry.size() != 0){
                return alternateOfficialSiteEntry.iterator().next();
            }
        }

        //search name
        String[] paramsForNameSearch = {name,"1"};
        Document nameResults = wikiDataSearchConnector.fetchDOMFromGetRequest(paramsForNameSearch);
        WikiDataEntryData nameEntity = getEntryFromSearchAPI(nameResults);
        if (nameEntity != null){
            //we found a match
            return  nameEntity;
        }

        //we couldn't find anything
        return null;
    }


    //for shallow search to match the facebook item to an entity
    private String searchByFacebookIDQuery(String facebookID, String type){
        return
            "SELECT ?entry ?entryLabel ?entryDescription " +
            "WHERE " +
            "{" +
            "    ?entry wdt:" + type + " '" + facebookID + "' . " +
            "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
            WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
            WikiBaseEndpointConnector.ENGLISH + "' } . " +
            "} " +
            "LIMIT 1 "; //should only return 1 result but just to make sure
    }

    //for shallow search to match the facebook item to an entity
    private String searchByOfficialSiteQuery(String officialSite){
        return
                "SELECT ?entry ?entryLabel ?entryDescription " +
                "WHERE " +
                "{" +
                "    ?entry wdt:P856 '" + officialSite + "' . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
                WikiBaseEndpointConnector.ENGLISH + "' } . " +
                "}" +
                "LIMIT 1 "; //should only return 1 result but just to make sure
    }

    private String stripOfficialSite(String officialSite) {
        //we only need whatever is after the main url for the wikidata query
        String trimmedOfficialSite = officialSite.replace("https://www.facebook.com/", "");
        //there is an excess / at the end
        trimmedOfficialSite = trimmedOfficialSite.substring(0, trimmedOfficialSite.length() - 1);
        return trimmedOfficialSite;
    }

    private WikiDataEntryData getEntryFromSearchAPI(Document document){
        NodeList resultNodes = document.getElementsByTagName("entity");
        //should return 0 or 1
        //since we set a limit of 1 to the query
        int resultCt = resultNodes.getLength();
        if (resultCt > 0){
            Node n = resultNodes.item(0);
            if (n.getNodeType() == Node.ELEMENT_NODE)
            {
                String wikiDataID = "";
                String label = "";
                String description = "";

                Element e = (Element)n;
                if (e.hasAttribute("id")) {
                    wikiDataID = e.getAttribute("id");
                }
                if(e.hasAttribute("label")) {
                    label = e.getAttribute("label");
                }

                if(e.hasAttribute("description")) {
                    description = e.getAttribute("description");
                }

                return new WikiDataEntryData(label, description, wikiDataID, label, WikiDataEntryData.CLASSIFICATION_NOT_SET);
            }
        }

        //couldn't find a match
        return null;
    }

    private Set<WikiDataEntryData> getEntriesFromSPARQL(Document document, String identifier){
        Set<WikiDataEntryData> result = new HashSet<>();
        NodeList resultNodes = document.getElementsByTagName(WikiDataSPARQLConnector.RESULT_TAG);
        int nodeCt = resultNodes.getLength();
        for (int i=0; i<nodeCt; i++){
            Node head = resultNodes.item(i);
            String label = SPARQLDocumentParserHelper.findValueByNodeName(head, identifier + "Label");
            if (label.equals(""))//no match
                continue;

            String description = SPARQLDocumentParserHelper.findValueByNodeName(head, identifier + "Description");
            String id = SPARQLDocumentParserHelper.findValueByNodeName(head, identifier);
            id = LessonGeneratorUtils.stripWikidataID(id);

            WikiDataEntryData data = new WikiDataEntryData(label, description, id, label, WikiDataEntryData.CLASSIFICATION_NOT_SET);
            result.add(data);
        }

        return result;
    }

    private Set<WikiDataEntryData> searchEducation(JSONObject jsonObject) throws Exception{
        Set<WikiDataEntryData> result = new HashSet<>();
        if (jsonObject.has("education")) {
            JSONArray allSchools = jsonObject.getJSONArray("education");
            int schoolCt = allSchools.length();
            double incrementBy = 1.0 / facebookEntityCt;
            if (searchDepth == DEEP_SEARCH)
                incrementBy /= 2;
            for (int i = 0; i < schoolCt; i++) {
                JSONObject school = allSchools.getJSONObject(i);
                JSONObject schoolGeneralInfo = school.getJSONObject("school");
                String schoolName = schoolGeneralInfo.getString("name");
                String schoolID = schoolGeneralInfo.getString("id");
                WikiDataEntryData data = findWikiDataEntry(schoolName, schoolID, FACEBOOK_GENERAL_SEARCH);
                if (data != null) {
                    result.add(data);
                    sendWordToUI(data.getLabel());
                }
                currentPercent += incrementBy;
                sendProgressPercentToUI(currentPercent);
            }

            //deep search
            if (searchDepth == DEEP_SEARCH){
                //search again just for the entities we found matches for.
                int remainingResultCt = result.size();
                //we might have lost some so recalculate increment by
                double totalIncrement =  1.0 * schoolCt / facebookEntityCt / 2.0 ;
                if (remainingResultCt == 0){
                    currentPercent += totalIncrement;
                    sendProgressPercentToUI(currentPercent);
                } else {
                    incrementBy = totalIncrement / remainingResultCt;
                    //prevent concurrent modification
                    Set<WikiDataEntryData> tempResults = new HashSet<>(result);
                    for (WikiDataEntryData school : tempResults) {
                        String schoolID = school.getWikiDataID();
                        Set<WikiDataEntryData> deepResult = educationDeepSearch(schoolID);
                        result.addAll(deepResult);
                        currentPercent += incrementBy;
                        sendProgressPercentToUI(currentPercent);
                    }
                }
            }
        }

        return result;
    }


    private Set<WikiDataEntryData> educationDeepSearch(String wikiDataID) throws Exception{
        Set<WikiDataEntryData> result = new HashSet<>();
        String query =
                "SELECT DISTINCT ?namedAfterLabel ?foundedByLabel ?location1Label ?location2Label ?childrenSchoolsLabel ?parentSchoolLabel " +
                "        ?namedAfterDescription ?foundedByDescription ?location1Description ?location2Description ?childrenSchoolsDescription ?parentSchoolDescription " +
                "    ?namedAfter ?foundedBy ?location1 ?location2 ?childrenSchools ?parentSchool " +
                "WHERE " +
                "{ " +
                "    ?school ?property ?value . " +
                "    OPTIONAL {?school wdt:P138 ?namedAfter} . " +
                "    OPTIONAL {?school wdt:P112 ?foundedBy} . " +
                "    OPTIONAL {?school wdt:P131 ?location1} . " +
                "    OPTIONAL {?school wdt:P276 ?location2} . " +
                "    OPTIONAL {?school wdt:P355 ?childrenSchools} . " +
                "    OPTIONAL {?school wdt:P749 ?parentSchool} . " +
                "	 SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                     WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
                     WikiBaseEndpointConnector.ENGLISH + "' . " +
                "    } . " +
                "   BIND (wd:"+wikiDataID+" as ?school) . " + //without binding, the query timed out
                "}";


        Document resultsDOM = wikiDataSPARQLConnector.fetchDOMFromGetRequest(query);
        Set<WikiDataEntryData> namedAfter = getEntriesFromSPARQL(resultsDOM,"namedAfter");
        Set<WikiDataEntryData> foundedBy = getEntriesFromSPARQL(resultsDOM,"foundedBy");
        Set<WikiDataEntryData> location1 = getEntriesFromSPARQL(resultsDOM,"location1");
        Set<WikiDataEntryData> location2 = getEntriesFromSPARQL(resultsDOM,"location2");
        Set<WikiDataEntryData> childrenSchools = getEntriesFromSPARQL(resultsDOM,"childrenSchools");
        Set<WikiDataEntryData> parentSchool = getEntriesFromSPARQL(resultsDOM,"parentSchool");

        result.addAll(namedAfter);
        result.addAll(foundedBy);
        result.addAll(location1);
        result.addAll(location2);
        result.addAll(childrenSchools);
        result.addAll(parentSchool);


        return result;
    }

    private Set<WikiDataEntryData> searchHometown(JSONObject jsonObject) throws Exception{
        //only one hometown allowed on fb
        Set<WikiDataEntryData> result = new HashSet<>();
        if (jsonObject.has("hometown")) {
            JSONObject hometown = jsonObject.getJSONObject("hometown");
            String hometownName = hometown.getString("name");
            String hometownID = hometown.getString("id");

            //the names are always in English
            //it seems to be city, state(country)
            //ie Kuwana, Mie
            //   Manchester, United Kingdom
            String[] hometownNameParts = hometownName.split(", ");
            String hometownCity = hometownNameParts[0];
            WikiDataEntryData data = findWikiDataEntry(hometownCity, hometownID, FACEBOOK_LOCATION_SEARCH);
            if (data != null) {
                result.add(data);
                sendWordToUI(data.getLabel());
            }
            double incrementBy = 1.0 / facebookEntityCt;
            if (searchDepth == DEEP_SEARCH)
                incrementBy /= 2;
            currentPercent += incrementBy;
            sendProgressPercentToUI(currentPercent);

            if (searchDepth == DEEP_SEARCH) {
                if (result.size() == 1) {
                    Set<WikiDataEntryData> deepData = cityDeepSearch(hometownID);
                    result.addAll(deepData);
                }
                currentPercent += incrementBy;
                sendProgressPercentToUI(currentPercent);
            }
        }

        return result;

    }

    //used for all queries regarding cities
    private Set<WikiDataEntryData> cityDeepSearch(String wikiDataID) throws Exception{
        Set<WikiDataEntryData> result = new HashSet<>();

        String query =
                "SELECT DISTINCT ?sharesBorderWithLabel ?inLabel ?sharesBorderWithDescription ?inDescription " +
                "       ?sharesBorderWith ?in " +
                "WHERE " +
                "{ " +
                "   ?city ?property ?value . " +
                "   OPTIONAL {?city wdt:P47 ?sharesBorderWith } . " +
                "   OPTIONAL {?city wdt:P131 ?in } . " +
                "	 SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
                WikiBaseEndpointConnector.ENGLISH + "' . " +
                "    } . " +
                "   BIND (wd:"+wikiDataID+" as ?city) . " + //without binding, the query timed out
                "}";

        Document resultsDOM = wikiDataSPARQLConnector.fetchDOMFromGetRequest(query);
        Set<WikiDataEntryData> sharesBorderWith = getEntriesFromSPARQL(resultsDOM, "sharesBorderWith");
        Set<WikiDataEntryData> in = getEntriesFromSPARQL(resultsDOM, "in");

        result.addAll(sharesBorderWith);
        result.addAll(in);
        return result;
    }

    //looks like there is only one location??
    //the main current location can be fetched but not the past visited locations
    private Set<WikiDataEntryData> searchLocation(JSONObject jsonObject) throws Exception{
        //only one hometown allowed on fb
        Set<WikiDataEntryData> result = new HashSet<>();
        if (jsonObject.has("location")) {
            JSONObject location = jsonObject.getJSONObject("location");
            String locationName = location.getString("name");
            String locationID = location.getString("id");

            //the names are always in English
            //it seems to be city, state(country)
            //ie Kuwana, Mie
            //   Manchester, United Kingdom
            String[] locationNameParts = locationName.split(", ");
            String locationCity = locationNameParts[0];
            WikiDataEntryData data = findWikiDataEntry(locationCity, locationID, FACEBOOK_LOCATION_SEARCH);
            if (data != null) {
                result.add(data);
                sendWordToUI(data.getLabel());
            }

            double incrementBy = 1.0 / facebookEntityCt;
            if (searchDepth == DEEP_SEARCH)
                incrementBy /= 2;
            currentPercent += incrementBy;
            sendProgressPercentToUI(currentPercent);

            if (searchDepth == DEEP_SEARCH) {
                if (result.size() == 1) {
                    Set<WikiDataEntryData> deepData = cityDeepSearch(locationID);
                    result.addAll(deepData);
                }
                currentPercent += incrementBy;
                sendProgressPercentToUI(currentPercent);
            }
        }

        return result;
    }

    */
}
