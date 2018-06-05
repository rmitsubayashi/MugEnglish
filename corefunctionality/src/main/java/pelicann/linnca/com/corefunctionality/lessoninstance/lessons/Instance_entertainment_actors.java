package pelicann.linnca.com.corefunctionality.lessoninstance.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Entertainment_actors;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessoninstance.TermAdjuster;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.lessonscript.StringUtils;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class Instance_entertainment_actors extends LessonInstanceGenerator {
    public Instance_entertainment_actors(){
        super();
        this.lessonKey = Entertainment_actors.KEY;
        this.uniqueEntities = 1;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?person ?personLabel ?personENLabel " +
                " ?awardLabel ?awardENLabel " +
                " ?yearLabel " +
                " ?gender " +
                " ?picLabel " +
                "WHERE " +
                "{" +
                "    ?person   p:P166 [ " +
                "              ps:P166  ?award ; " +
                "              pq:P585 ?year " +
                "             ]; " +
                "             wdt:P21    ?gender ; " +
                "             rdfs:label ?personENLabel . " +
                "    ?award wdt:P31    wd:Q19020 ; " +
                "           rdfs:label ?awardENLabel . " +
                "    OPTIONAL { ?person    wdt:P18    ?pic } . " + //image if possible
                "    FILTER (LANG(?personENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?awardENLabel) = '" +
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
            String awardEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "awardENLabel");
            String awardJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "awardLabel");
            Translation awardTranslation = new Translation(awardEN, awardJP);
            String year = SPARQLDocumentParserHelper.findValueByNodeName(head, "yearLabel");
            //just get year
            year = year.substring(0,4);
            Translation yearTranslation = new Translation(year, year);
            String gender = SPARQLDocumentParserHelper.findValueByNodeName(head, "gender");
            gender = WikiDataEntity.getWikiDataIDFromReturnedResult(gender);
            boolean isMale = TermAdjuster.isMale(gender);
            Translation genderTranslation = StringUtils.getGenderPronoun(isMale, StringUtils.CASE_SUBJECTIVE);
            Translation genderTranslation2 = StringUtils.getGenderPronoun(isMale, StringUtils.CASE_POSSESSIVE);
            List<Translation> properties = new ArrayList<>();
            properties.add(personTranslation);
            properties.add(awardTranslation);
            properties.add(yearTranslation);
            properties.add(genderTranslation);
            properties.add(genderTranslation2);
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
