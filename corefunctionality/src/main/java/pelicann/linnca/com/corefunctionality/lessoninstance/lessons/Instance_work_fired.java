package pelicann.linnca.com.corefunctionality.lessoninstance.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Work_fired;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Work_hired;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.InstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessoninstance.TermAdjuster;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.lessonscript.StringUtils;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class Instance_work_fired extends InstanceGenerator {
    public Instance_work_fired(){
        super();
        super.uniqueEntities = 1;
        super.lessonKey = Work_fired.KEY;
        super.referenceLesson = Work_hired.KEY;
        super.referencePropertyIndex = 0;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT " +
                "?person ?personLabel ?personENLabel " +
                "?gender " +
                "?employer ?employerLabel ?employerENLabel " +
                "?headQuartersLabel ?headQuartersENLabel " +
                "?picLabel " +
                "WHERE " +
                "{" +
                "   ?person rdfs:label ?personENLabel . " +
                "   FILTER (LANG(?personENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "   ?person wdt:P21 ?gender . " +
                "   ?person wdt:P108 ?employer . " +
                "   ?employer wdt:P159 ?headQuarters . " + //for next lesson
                "   ?employer rdfs:label ?employerENLabel . " +
                "   FILTER (LANG(?employerENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "   ?headQuarters rdfs:label ?headQuartersENLabel . " +
                "   FILTER (LANG(?headQuartersENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "   OPTIONAL { ?employer wdt:P18 ?pic } . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?person) . " +
                "} ";
    }

    @Override
    protected synchronized void processResultsIntoEntityPropertyData(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i = 0; i < resultLength; i++) {
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");
            personID = WikiDataEntity.getWikiDataIDFromReturnedResult(personID);
            String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personENLabel");
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            Translation personTranslation = new Translation(personEN, personJP);

            String employerEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "employerENLabel");
            String employerJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "employerLabel");
            Translation employerTranslation = new Translation(employerEN, employerJP);
            String employerID = SPARQLDocumentParserHelper.findValueByNodeName(head, "employer");
            employerID = WikiDataEntity.getWikiDataIDFromReturnedResult(employerID);
            employerTranslation.setWikidataID(employerID);
            String headQuartersEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "headQuartersENLabel");
            String headQuartersJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "headQuartersLabel");
            Translation headQuartersTranslation = new Translation(headQuartersEN, headQuartersJP);
            String genderID = SPARQLDocumentParserHelper.findValueByNodeName(head, "gender");
            genderID = WikiDataEntity.getWikiDataIDFromReturnedResult(genderID);
            boolean isMale = TermAdjuster.isMale(genderID);
            Translation genderTranslation = StringUtils.getGenderPronoun(isMale, StringUtils.CASE_SUBJECTIVE);
            List<Translation> properties = new ArrayList<>();
            properties.add(personTranslation);
            properties.add(employerTranslation);
            properties.add(headQuartersTranslation);
            properties.add(genderTranslation);
            EntityPropertyData entityPropertyData = new EntityPropertyData();
            entityPropertyData.setLessonKey(lessonKey);
            entityPropertyData.setWikidataID(personID);
            entityPropertyData.setProperties(properties);

            String pic = SPARQLDocumentParserHelper.findValueByNodeName(head, "picLabel");
            if (pic != null && !pic.equals("")) {
                pic = WikiDataSPARQLConnector.cleanImageURL(pic);
            }
            entityPropertyData.setImageURL(pic);

            newEntityPropertyData.add(entityPropertyData);
        }
    }
}
