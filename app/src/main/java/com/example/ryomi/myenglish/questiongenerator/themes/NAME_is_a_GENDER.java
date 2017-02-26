package com.example.ryomi.myenglish.questiongenerator.themes;

import com.example.ryomi.myenglish.connectors.EndpointConnectorReturnsXML;
import com.example.ryomi.myenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.myenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.questiongenerator.GrammarRules;
import com.example.ryomi.myenglish.questiongenerator.QGUtils;
import com.example.ryomi.myenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.myenglish.questiongenerator.Theme;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NAME_is_a_GENDER extends Theme{
    //placeholders
    private final String personNamePH = "personName";
    private final String personNameForeignPH = "personNameForeign";
    private final String personNameENPH = "personNameEN";
    private final String genderPH = "territoryEN";
    private final String femaleForeign = "女性";
    private final String femaleEN = "female";
    private final String maleForeign = "男性";
    private final String maleEN = "male";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String personID;
        private String personNameEN;
        private String personNameForeign;
        private String genderEN = "";
        private String genderForeign = "";

        private QueryResult(
                String personID,
                String personNameEN,
                String personNameForeign,
                String genderID)
        {
            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameForeign = personNameForeign;
            setGender(genderID);
        }

        private void setGender(String genderID){
            if (genderID.equals("Q6581072")){
                genderEN = femaleEN;
                genderForeign = femaleForeign;
            } else if (genderID.equals("Q6581097")){
                genderEN = maleEN;
                genderForeign = maleForeign;
            }
        }

    }

    public NAME_is_a_GENDER(EndpointConnectorReturnsXML connector, ThemeData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;

    }

    protected String getSPARQLQuery(){
        //find person name
        return "SELECT DISTINCT ?" + personNamePH + " ?" + personNameForeignPH + " ?" + personNameENPH + //Kyoto returns 2 results? so use distinct
                " ?" + genderPH + " " +
                "WHERE " +
                "{" +
                "    ?" + personNamePH + " wdt:P31 wd:Q5 . " + //is a person
                "    ?" + personNamePH + " wdt:P21 ?" + genderPH + " . " + //lgender
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + personNamePH + " rdfs:label ?" + personNameForeignPH + "  } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + personNamePH + " rdfs:label ?" + personNameENPH + " . " +
                "                           } . " + //English translation
                "    BIND (wd:%s as ?" + personNamePH + ") . " + //binding the ID of entity as ?person
                "} ";

    }

    protected void processResultsIntoClassWrappers() {
        Document document = super.documentOfTopics;
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, personNamePH);
            personID = QGUtils.stripWikidataID(personID);
            String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameENPH);
            String personNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameForeignPH);
            String genderID = SPARQLDocumentParserHelper.findValueByNodeName(head, genderPH);
            genderID = QGUtils.stripWikidataID(genderID);

            QueryResult qr = new QueryResult(personID, personNameEN, personNameForeign, genderID);
            queryResults.add(qr);
        }
    }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.personNameForeign);
        }
    }


    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();
            QuestionData sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            QuestionData trueFalseQuestionTrue = createTrueFalseQuestion(qr,QuestionUtils.TRUE_FALSE_QUESTION_TRUE);
            QuestionData trueFalseQuestionFalse = createTrueFalseQuestion(qr,QuestionUtils.TRUE_FALSE_QUESTION_FALSE);
            //random order of questions
            int i = new Random().nextInt();
            if (i%2 == 0) {
                questionSet.add(trueFalseQuestionTrue);
                questionSet.add(trueFalseQuestionFalse);
            }else{
                questionSet.add(trueFalseQuestionFalse);
                questionSet.add(trueFalseQuestionTrue);
            }

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.personID));
        }

    }

    private String NAME_is_a_GENDER_EN_correct(QueryResult qr){
        String sentence = qr.personNameEN + " is a " + qr.genderEN + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String NAME_is_a_GENDER_EN_incorrect(QueryResult qr){
        String gender;
        if (qr.genderEN.equals(maleEN))
            gender = femaleEN;
        else
            gender = maleEN;
        String sentence = qr.personNameEN + " is a " + gender + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceForeign(QueryResult qr){
        return qr.personNameForeign + "は" + qr.genderForeign + "です。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personNameEN);
        pieces.add("is");
        pieces.add("a " + qr.genderEN);
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private QuestionData createSentencePuzzleQuestion(QueryResult qr){
        String question = this.formatSentenceForeign(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.personNameForeign);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }

    private QuestionData createTrueFalseQuestion(QueryResult qr, String answer){
        String question;
        if (answer.equals(QuestionUtils.TRUE_FALSE_QUESTION_TRUE))
            question = this.NAME_is_a_GENDER_EN_correct(qr);
        else
            question = this.NAME_is_a_GENDER_EN_incorrect(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.personNameForeign);
        data.setQuestionType(QuestionTypeMappings.TRUE_FALSE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }
}
