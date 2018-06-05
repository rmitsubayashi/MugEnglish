package pelicann.linnca.com.corefunctionality.lessoninstance.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Introduction_age;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class Instance_introduction_age extends LessonInstanceGenerator {
    public Instance_introduction_age(){
        super();
        super.uniqueEntities = 1;
        super.lessonKey = Introduction_age.KEY;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?person ?personLabel ?personENLabel " +
                " ?firstNameLabel ?firstNameENLabel " +
                " ?picLabel " +
                " ?birthday " +
                "WHERE " +
                "{" +
                "    ?person   wdt:P569   ?birthday; " + //has a birthday
                "              rdfs:label ?personENLabel . " +
                "    FILTER NOT EXISTS { ?person wdt:P570 ?dateDeath } . " + //but not a death date
                "    OPTIONAL { ?person    wdt:P735   ?firstName . " + //first name if possible
                "               ?firstName rdfs:label ?firstNameENLabel } . " +
                "    OPTIONAL { ?person    wdt:P18    ?pic } . " + //image if possible
                "    FILTER (LANG(?personENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?firstNameENLabel) = '" +
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
            String birthday = SPARQLDocumentParserHelper.findValueByNodeName(head, "birthday");
            //we only need the date, not the time
            birthday = birthday.substring(0, 10);
            Translation birthdayTranslation = new Translation(birthday, birthday);
            List<Translation> properties = new ArrayList<>();
            properties.add(personTranslation);
            properties.add(birthdayTranslation);
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
