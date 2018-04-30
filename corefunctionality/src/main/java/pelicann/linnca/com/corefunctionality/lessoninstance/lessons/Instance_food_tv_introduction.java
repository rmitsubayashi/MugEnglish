package pelicann.linnca.com.corefunctionality.lessoninstance.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Food_tv_introduction;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessoninstance.TermAdjuster;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class Instance_food_tv_introduction extends LessonInstanceGenerator {
    public Instance_food_tv_introduction(){
        super();
        super.uniqueEntities = 1;
        super.lessonKey = Food_tv_introduction.KEY;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?person ?personLabel ?personENLabel " +
                " ?country ?countryLabel ?countryENLabel " +
                " ?food ?foodLabel ?foodENLabel " +
                " ?gender " +
                " ?picLabel " +
                "WHERE " +
                "{" +
                "    ?person   wdt:P21    ?gender . " +
                "    ?person   rdfs:label ?personENLabel . " +
                "    ?person   wdt:P27    ?country . " +
                "    ?country  rdfs:label ?countryENLabel . " +
                "    ?cuisine  wdt:P31    wd:Q1968435 . " +
                "    ?cuisine  wdt:P2341  ?country . " + //is a country's cuisine
                "    ?food     wdt:P361/wdt:P279* ?cuisine . " +
                "    ?food     wdt:P279   wd:Q746549 . " + //is a dish
                "    ?food     rdfs:label ?foodENLabel . " +
                "    OPTIONAL { ?food     wdt:P18    ?pic } . " + //image of food if possible
                "    FILTER (LANG(?personENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?countryENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?foodENLabel) = '" +
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
            String countryID = SPARQLDocumentParserHelper.findValueByNodeName(head, "country");
            countryID = WikiDataEntity.getWikiDataIDFromReturnedResult(countryID);
            String countryEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryENLabel");
            String countryJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryLabel");
            Translation countryTranslation = new Translation(countryEN, countryJP);
            countryTranslation.setWikidataID(countryID);
            String foodID = SPARQLDocumentParserHelper.findValueByNodeName(head, "food");
            foodID = WikiDataEntity.getWikiDataIDFromReturnedResult(foodID);
            String foodEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "foodENLabel");
            String foodJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "foodLabel");
            Translation foodTranslation = new Translation(foodEN, foodJP);
            //we need the food info for the next lesson
            foodTranslation.setWikidataID(foodID);
            String gender = SPARQLDocumentParserHelper.findValueByNodeName(head, "gender");
            gender = WikiDataEntity.getWikiDataIDFromReturnedResult(gender);
            boolean isMale = TermAdjuster.isMale(gender);
            Translation genderTranslation = new Translation();
            genderTranslation.setGenderPronoun(isMale ? Translation.MALE : Translation.FEMALE);

            List<Translation> properties = new ArrayList<>();
            properties.add(personTranslation);
            properties.add(countryTranslation);
            properties.add(foodTranslation);
            properties.add(genderTranslation);
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
