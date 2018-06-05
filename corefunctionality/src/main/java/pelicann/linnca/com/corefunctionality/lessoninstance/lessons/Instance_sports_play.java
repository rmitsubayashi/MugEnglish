package pelicann.linnca.com.corefunctionality.lessoninstance.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Sports_play;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessoninstance.TermAdjuster;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class Instance_sports_play extends LessonInstanceGenerator {
    public Instance_sports_play(){
        super();
        super.uniqueEntities = 1;
        super.lessonKey = Sports_play.KEY;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?person ?personLabel ?personENLabel " +
                " ?firstNameLabel ?firstNameENLabel " +
                " ?picLabel " +
                " ?sport ?sportENLabel ?sportLabel " +
                " ?occupationENLabel ?occupationLabel " +
                "WHERE " +
                "{" +
                "    ?person   wdt:P641          ?sport ; " + //is a sports player
                "              wdt:P106          ?occupation ; " + //with occupation
                "              rdfs:label        ?personENLabel . " +
                "    OPTIONAL { ?person    wdt:P735   ?firstName . " + //optional first name
                "               ?firstName rdfs:label ?firstNameENLabel } . " +
                "    OPTIONAL { ?sport    wdt:P18    ?pic } .  " + //optional picture
                "    ?occupation  wdt:P425   ?sport ; " + //occupation is of that sport
                "                 rdfs:label ?occupationENLabel . " +
                "    ?sport  rdfs:label ?sportENLabel . " +
                "    FILTER (LANG(?personENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?firstNameENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?occupationENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?sportENLabel) = '" +
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
            String firstNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "firstNameENLabel");
            if (firstNameEN == null || firstNameEN.equals("")){
                firstNameEN = personEN;
            }
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            String firstNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "firstNameLabel");
            if (firstNameJP == null || firstNameJP.equals("")){
                firstNameJP = personJP;
            }
            Translation personTranslation = new Translation(personEN, personJP);
            Translation firstNameTranslation = new Translation(firstNameEN, firstNameJP);
            String sportID = SPARQLDocumentParserHelper.findValueByNodeName(head, "sport");
            sportID = WikiDataEntity.getWikiDataIDFromReturnedResult(sportID);
            String sportEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "sportENLabel");
            sportEN = TermAdjuster.adjustSportsEN(sportEN);
            String sportJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "sportLabel");
            Translation sportTranslation = new Translation(sportEN, sportJP);
            sportTranslation.setWikidataID(sportID);
            String occupationEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "occupationENLabel");
            occupationEN = TermAdjuster.adjustOccupationEN(occupationEN);
            String occupationJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "occupationLabel");
            Translation occupationTranslation = new Translation(occupationEN, occupationJP);

            List<Translation> properties = new ArrayList<>();
            properties.add(personTranslation);
            properties.add(sportTranslation);
            properties.add(occupationTranslation);
            properties.add(firstNameTranslation);
            EntityPropertyData entityPropertyData = new EntityPropertyData();
            entityPropertyData.setLessonKey(lessonKey);
            entityPropertyData.setWikidataID(personID);
            entityPropertyData.setProperties(properties);

            String pic = SPARQLDocumentParserHelper.findValueByNodeName(head, "picLabel");
            if (pic != null && !pic.equals("")){
                pic = WikiDataSPARQLConnector.cleanImageURL(pic);
            }
            entityPropertyData.setImageURL(pic);

            newEntityPropertyData.add(entityPropertyData);
        }
    }
}
