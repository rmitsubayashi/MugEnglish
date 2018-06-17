package pelicann.linnca.com.corefunctionality.lessoninstance.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Emergency_phone;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.InstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class Instance_emergency_phone extends InstanceGenerator {
    public Instance_emergency_phone(){
        super();
        super.uniqueEntities = 1;
        super.lessonKey = Emergency_phone.KEY;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?person ?personLabel ?personENLabel " +
                " ?phoneNumberENLabel " +
                " ?country ?countryLabel ?countryENLabel " +
                " ?picLabel " +
                "WHERE " +
                "{" +
                "    ?person   wdt:P27   ?country; " + //has a blog
                "              rdfs:label ?personENLabel . " +
                "    ?country wdt:P2852 ?phoneNumber . " +
                "    ?country rdfs:label ?countryENLabel . " +
                "    ?phoneNumber rdfs:label ?phoneNumberENLabel . " +
                "    OPTIONAL { ?person    wdt:P18    ?pic } . " + //image if possible
                "    FILTER (LANG(?personENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?countryENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?phoneNumberENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?person) . " + //binding the ID of entity as ?person
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
            String phoneNumber = SPARQLDocumentParserHelper.findValueByNodeName(head, "phoneNumberENLabel");
            Translation phoneNumberTranslation = new Translation(phoneNumber, phoneNumber);
            String countryID = SPARQLDocumentParserHelper.findValueByNodeName(head, "country");
            countryID = WikiDataEntity.getWikiDataIDFromReturnedResult(countryID);
            String countryEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryENLabel");
            String countryJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryLabel");
            Translation countryTranslation = new Translation(countryEN, countryJP);
            countryTranslation.setWikidataID(countryID);
            List<Translation> properties = new ArrayList<>();
            properties.add(personTranslation);
            properties.add(phoneNumberTranslation);
            properties.add(countryTranslation);
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
