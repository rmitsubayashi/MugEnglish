package pelicann.linnca.com.corefunctionality.lessoninstance;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

class MockInstanceGenerator extends InstanceGenerator {
    MockInstanceGenerator(){
        super();
        super.uniqueEntities = 1;
        super.lessonKey = "mockKey";
    }

    @Override
    protected String getSPARQLQuery(){
        return "we are mocking this so just input the wikidata id %s";
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
            List<Translation> properties = new ArrayList<>();
            properties.add(personTranslation);
            EntityPropertyData entityPropertyData = new EntityPropertyData();
            entityPropertyData.setLessonKey(lessonKey);
            entityPropertyData.setWikidataID(personID);
            entityPropertyData.setProperties(properties);

            newEntityPropertyData.add(entityPropertyData);
        }
    }

}
