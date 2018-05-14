package pelicann.linnca.com.corefunctionality.lessoninstance.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Food_class;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Food_tv_introduction;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class Instance_food_class extends LessonInstanceGenerator {
    public Instance_food_class(){
        super();
        super.uniqueEntities = 1;
        super.lessonKey = Food_class.KEY;
        super.referenceLesson = Food_tv_introduction.KEY;
        super.referencePropertyIndex = 2;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?food ?foodLabel ?foodENLabel " +
                "?countryLabel ?demonymLabel " +
                "?picLabel " +
                "WHERE " +
                "{" +
                "   ?food rdfs:label ?foodENLabel . " +
                "   FILTER (LANG(?foodENLabel) = '" +
                    WikiBaseEndpointConnector.ENGLISH + "') . " +
                "   OPTIONAL { ?food wdt:P18 ?pic } . " +
                "   OPTIONAL { ?food wdt:P361 ?cuisine . " +
                "               ?cuisine wdt:P2341 ?country . " +
                "               ?country wdt:P1549 ?demonym . " +
                "               FILTER (LANG(?demonymLabel) = '" +
                                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    } . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?food) . " +
                "} ";
    }

    @Override
    protected synchronized void processResultsIntoEntityPropertyData(Document document){
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        System.out.println(resultLength + " results returned");
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String foodID = SPARQLDocumentParserHelper.findValueByNodeName(head, "food");
            foodID = WikiDataEntity.getWikiDataIDFromReturnedResult(foodID);
            String foodEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "foodENLabel");
            String foodJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "foodLabel");
            Translation foodTranslation = new Translation(foodEN, foodJP);
            String demonymEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "demonymLabel");
            String countryJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryLabel");
            Translation countryTranslation;
            if (countryJP == null || countryJP.equals("")) {
                countryTranslation = new Translation(Translation.NONE, Translation.NONE);
            } else {
                countryTranslation = new Translation(demonymEN, countryJP);
            }

            List<Translation> properties = new ArrayList<>();
            properties.add(foodTranslation);
            properties.add(countryTranslation);
            EntityPropertyData entityPropertyData = new EntityPropertyData();
            entityPropertyData.setLessonKey(lessonKey);
            entityPropertyData.setWikidataID(foodID);
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
