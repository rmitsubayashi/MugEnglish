package com.example.ryomi.myenglish.userinterestcontrols;

import android.content.Context;

import com.example.ryomi.myenglish.connectors.FacebookAPIConnector;
import com.example.ryomi.myenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.connectors.WikiDataAPISearchConnector;
import com.example.ryomi.myenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.example.ryomi.myenglish.questiongenerator.QGUtils;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.Set;


public class FacebookInterestFinder {
    public static final int SHALLOW_SEARCH = 1;
    public static final int DEEP_SEARCH = 2;

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

    public FacebookInterestFinder(Context context){
        FacebookSdk.sdkInitialize(context);
    }

    public Set<WikiDataEntryData> findUserInterests(int depth) throws Exception{
        searchDepth = depth;

        Set<WikiDataEntryData> results = new HashSet<>();

        //first search facebook for all interests
        AccessToken.refreshCurrentAccessTokenAsync();
        String accessToken = AccessToken.getCurrentAccessToken().getToken();
        String pageID = FacebookAPIConnector.SELF_USER_ID;
        String[] paramsForUserInfo = {accessToken, pageID, fieldsForUserInfo};

        JSONObject userInfo = facebookConnector.fetchJSONObjectFromGetRequest(paramsForUserInfo);
        Set educationResult = searchEducation(userInfo);
        results.addAll(educationResult);
        Set hometownResult = searchHometown(userInfo);
        results.addAll(hometownResult);

        for (WikiDataEntryData result : results){
            System.out.println(result.getLabel());
        }

        return results;
    }

    /* first check the facebook url
     * then the official site link? then name?
     * not sure about the order of the last two, but we should do the facebook url first
     * since it has the most guaranteed match rate
     * */
    //facebook query type is for when we query wikidata.
    //wikidata divides up the facebook ids into peoples/organizations and location id
    private WikiDataEntryData findWikiDataEntry(String name, String facebookID, String facebookQueryType) throws Exception{
        //search facebook url first
        AccessToken.refreshCurrentAccessTokenAsync();
        String accessToken = AccessToken.getCurrentAccessToken().getToken();
        String[] paramsForPageInfo = {accessToken, facebookID, fieldsForPageInfo};
        JSONObject pageInfo = facebookConnector.fetchJSONObjectFromGetRequest(paramsForPageInfo);

        //the facebook id on the wikidata db can either be the actual id
        // or a url to the facebook page
        //so check both
        String facebookIDQuery = searchByFacebookIDQuery(facebookID, facebookQueryType);
        Document facebookIDResults = wikiDataSPARQLConnector.fetchDOMFromGetRequest(facebookIDQuery);
        Set<WikiDataEntryData> facebookIDEntry = getEntriesFromSPARQL(facebookIDResults, "entry");
        for (WikiDataEntryData data : facebookIDEntry){
            //return first (and only) data if it exists
            return data;
        }

        String facebookURL = pageInfo.getString("link");
        facebookURL = stripOfficialSite(facebookURL);
        String facebookURLQuery = searchByFacebookIDQuery(facebookURL, facebookQueryType);
        Document facebookURLResults = wikiDataSPARQLConnector.fetchDOMFromGetRequest(facebookURLQuery);
        Set<WikiDataEntryData> facebookURLEntry = getEntriesFromSPARQL(facebookURLResults, "entry");
        for (WikiDataEntryData data : facebookURLEntry){
            //return first (and only) data if it exists
            return data;
        }


        if (pageInfo.has("website")) {
            //now search official site
            String officialSiteURL = pageInfo.getString("website");
            String officialSiteQuery = searchByOfficialSiteQuery(officialSiteURL);
            Document officialSiteResults = wikiDataSPARQLConnector.fetchDOMFromGetRequest(officialSiteQuery);
            Set<WikiDataEntryData> officialSiteEntry = getEntriesFromSPARQL(officialSiteResults, "entry");
            for (WikiDataEntryData data : officialSiteEntry){
                return data;
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
            for (WikiDataEntryData data : alternateOfficialSiteEntry){
                return data;
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
        String query =
            "SELECT ?entry ?entryLabel ?entryDescription " +
            "WHERE " +
            "{" +
            "    ?entry wdt:" + type + " '" + facebookID + "' . " +
            "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
            WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
            WikiBaseEndpointConnector.ENGLISH + "' } . " +
            "} " +
            "LIMIT 1 "; //should only return 1 result but just to make sure

        return query;
    }

    //for shallow search to match the facebook item to an entity
    private String searchByOfficialSiteQuery(String officialSite){
        String query =
                "SELECT ?entry ?entryLabel ?entryDescription " +
                "WHERE " +
                "{" +
                "    ?entry wdt:P856 '" + officialSite + "' . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
                WikiBaseEndpointConnector.ENGLISH + "' } . " +
                "}" +
                "LIMIT 1 "; //should only return 1 result but just to make sure

        return query;
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

                return new WikiDataEntryData(label, description, wikiDataID);
            }
        }

        //couldn't find a match
        return null;
    }

    private Set<WikiDataEntryData> getEntriesFromSPARQL(Document document, String identifier){
        Set<WikiDataEntryData> result = new HashSet<>();
        NodeList resultNodes = document.getElementsByTagName("result");
        int nodeCt = resultNodes.getLength();
        for (int i=0; i<nodeCt; i++){
            Node head = resultNodes.item(i);
            String label = SPARQLDocumentParserHelper.findValueByNodeName(head, identifier + "Label");
            if (label.equals(""))//no match
                continue;

            String description = SPARQLDocumentParserHelper.findValueByNodeName(head, identifier + "Description");
            String id = SPARQLDocumentParserHelper.findValueByNodeName(head, identifier);
            id = QGUtils.stripWikidataID(id);

            WikiDataEntryData data = new WikiDataEntryData(label, description, id);
            result.add(data);
        }

        return result;
    }

    private Set<WikiDataEntryData> searchEducation(JSONObject jsonObject) throws Exception{
        Set<WikiDataEntryData> result = new HashSet<>();
        if (jsonObject.has("education")) {
            JSONArray allSchools = jsonObject.getJSONArray("education");
            int arrayLength = allSchools.length();
            for (int i = 0; i < arrayLength; i++) {
                JSONObject school = allSchools.getJSONObject(i);
                JSONObject schoolGeneralInfo = school.getJSONObject("school");
                String schoolName = schoolGeneralInfo.getString("name");
                String schoolID = schoolGeneralInfo.getString("id");
                WikiDataEntryData data = findWikiDataEntry(schoolName, schoolID, FACEBOOK_GENERAL_SEARCH);
                if (data != null)
                    result.add(data);
            }

            //deep search
            if (searchDepth == DEEP_SEARCH){
                //search again just for the entities we found matches for.
                //prevent concurrent modification
                Set<WikiDataEntryData> tempResults = new HashSet<>(result);
                for (WikiDataEntryData school : tempResults){
                    String schoolID = school.getWikiDataID();
                    Set<WikiDataEntryData> deepResult = educationDeepSearch(schoolID);
                    result.addAll(deepResult);
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
            if (data != null)
                result.add(data);

            if (searchDepth == DEEP_SEARCH) {
                if (result.size() == 1) {
                    Set<WikiDataEntryData> deepData = hometownDeepSearch(hometownID);
                    result.addAll(deepData);
                }
            }
        }

        return result;

    }

    private Set<WikiDataEntryData> hometownDeepSearch(String wikiDataID) throws Exception{
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


}
