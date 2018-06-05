package pelicann.linnca.com.corefunctionality.lessoninstance.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Sports_introduction;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class Instance_sports_introduction extends LessonInstanceGenerator {
    public Instance_sports_introduction(){
        super();
        super.uniqueEntities = 1;
        super.lessonKey = Sports_introduction.KEY;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?person ?personLabel ?personENLabel " +
                " ?positionLabel ?positionENLabel " +
                " ?teamLabel ?teamENLabel " +
                " ?number " +
                " ?picLabel " +
                "WHERE " +
                "{" +
                "    ?person   wdt:P641          ?sport ; " + //is a sports player
                "              wdt:P413          ?position ; " + //ie FW, MF, DF
                "              wdt:P54           ?team ; " +
                "              wdt:P1618         ?number ; " +
                "              rdfs:label        ?personENLabel . " +
                "    ?team      rdfs:label ?teamENLabel . " +
                "    ?position  rdfs:label ?positionENLabel . " +
                "    OPTIONAL { ?person    wdt:P18    ?pic } .  " + //optional picture
                "    FILTER (LANG(?personENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?teamENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?positionENLabel) = '" +
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
            String positionEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "positionENLabel");
            String positionJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "positionLabel");
            Translation positionTranslation = new Translation(positionEN, positionJP);
            String number = SPARQLDocumentParserHelper.findValueByNodeName(head, "number");
            Translation numberTranslation = new Translation(number, number);


            List<Translation> properties = new ArrayList<>();
            properties.add(personTranslation);
            properties.add(positionTranslation);
            properties.add(teamTranslation);
            properties.add(numberTranslation);
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
