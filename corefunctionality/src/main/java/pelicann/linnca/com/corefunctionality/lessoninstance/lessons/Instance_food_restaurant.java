package pelicann.linnca.com.corefunctionality.lessoninstance.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Food_restaurant;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Food_tv_introduction;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class Instance_food_restaurant extends LessonInstanceGenerator {
    public Instance_food_restaurant(){
        super();
        super.uniqueEntities = 1;
        super.lessonKey = Food_restaurant.KEY;
        super.referenceLesson = Food_tv_introduction.KEY;
        super.referencePropertyIndex = 2;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?food ?foodLabel ?foodENLabel " +
                "?ingredientLabel ?ingredientENLabel " +
                "?picLabel " +
                "WHERE " +
                "{" +
                "    ?food rdfs:label ?foodENLabel . " +
                "    OPTIONAL { ?food     wdt:P18    ?pic } . " + //image of food if possible
                "    OPTIONAL { ?food     wdt:P527    ?ingredient ." +
                "               ?ingredient rdfs:label ?ingredientENLabel . " +
                "               FILTER (LANG(?ingredientENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                " } ." + //has part(ingredient)
                "    FILTER (LANG(?foodENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
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
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String foodID = SPARQLDocumentParserHelper.findValueByNodeName(head, "food");
            foodID = WikiDataEntity.getWikiDataIDFromReturnedResult(foodID);
            String foodEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "foodENLabel");
            String foodJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "foodLabel");
            Translation foodTranslation = new Translation(foodEN, foodJP);
            String ingredientEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "ingredientENLabel");
            String ingredientJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "ingredientLabel");
            Translation ingredientTranslation;
            if (ingredientEN == null || ingredientEN.equals("")) {
                ingredientTranslation = new Translation(Translation.NONE, Translation.NONE);
            } else {
                ingredientTranslation = new Translation(ingredientEN, ingredientJP);
            }
            String pic = SPARQLDocumentParserHelper.findValueByNodeName(head, "picLabel");
            if (pic == null || pic.equals("")){
                pic = Translation.NONE;
            } else {
                pic = WikiDataSPARQLConnector.cleanImageURL(pic);
            }
            Translation picTranslation = new Translation(pic, pic);

            List<Translation> properties = new ArrayList<>();
            properties.add(foodTranslation);
            properties.add(ingredientTranslation);
            properties.add(picTranslation);
            EntityPropertyData entityPropertyData = new EntityPropertyData();
            entityPropertyData.setLessonKey(lessonKey);
            entityPropertyData.setWikidataID(foodID);
            entityPropertyData.setProperties(properties);

            newEntityPropertyData.add(entityPropertyData);
        }
    }
}
