package pelicann.linnca.com.corefunctionality.lessoninstance.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Introduction_team_from;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

/*
* 0. person
* 1. team
* 2. team city
* 3. city
* 4. country
* */

public class Instance_introduction_team_from extends LessonInstanceGenerator {
    public Instance_introduction_team_from(){
        super();
        super.uniqueEntities = 1;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = Introduction_team_from.KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?person ?personLabel ?personENLabel " +
                " ?teamENLabel ?teamLabel " +
                " ?teamCityENLabel ?teamCityLabel " +
                " ?cityENLabel ?cityLabel " +
                " ?countryENLabel ?countryLabel " +
                "WHERE " +
                "{" +
                "    ?person   wdt:P641          ?sport ; " + //is a sports player
                "              wdt:P54           ?team ; " + //on a team
                "              wdt:P19           ?city ; " + //from a place
                "              rdfs:label        ?personENLabel . " +
                "    ?city     wdt:P17           ?country ; " + //in a country
                "              rdfs:label        ?cityENLabel . " +
                "    ?country  rdfs:label        ?countryENLabel . " +
                "    ?team     wdt:P159          ?teamCity ; " +//the team has a headquarter location
                "              rdfs:label        ?teamENLabel . " +
                "    ?teamCity wdt:P31/wdt:P279* wd:Q515 ; " + //which is a city
                "              rdfs:label        ?teamCityENLabel . " +
                "   FILTER (?city != ?teamCity) . " + //make sure the city and team city are different
                "    FILTER (LANG(?personENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?cityENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?countryENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?teamENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?teamCityENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?person) . " + //binding the ID of entity as ?company
                "} ";

    }

    @Override
    protected synchronized void processResultsIntoEntityPropertyData(Document document){
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");
            personID = WikiDataEntity.getWikiDataIDFromReturnedResult(personID);
            String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personENLabel");
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            Translation personTranslation = new Translation(personEN, personJP);
            String teamEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "teamENLabel");
            String teamJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "teamLabel");
            Translation teamTranslation = new Translation(teamEN, teamJP);
            String teamCityEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "teamCityENLabel");
            String teamCityJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "teamCityLabel");
            Translation teamCityTranslation = new Translation(teamCityEN, teamCityJP);
            String cityEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "cityENLabel");
            String cityJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "cityLabel");
            Translation cityTranslation = new Translation(cityEN, cityJP);
            String countryEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryENLabel");
            String countryJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryLabel");
            Translation countryTranslation = new Translation(countryEN, countryJP);

            List<Translation> properties = new ArrayList<>();
            properties.add(personTranslation);
            properties.add(teamTranslation);
            properties.add(teamCityTranslation);
            properties.add(cityTranslation);
            properties.add(countryTranslation);
            EntityPropertyData entityPropertyData = new EntityPropertyData();
            entityPropertyData.setLessonKey(lessonKey);
            entityPropertyData.setWikidataID(personID);
            entityPropertyData.setProperties(properties);

            newEntityPropertyData.add(entityPropertyData);
        }
    }
}
