package com.example.ryomi.mugenglish.questiongenerator.themes;

import com.example.ryomi.mugenglish.connectors.EndpointConnectorReturnsXML;
import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.FirebaseDBHeaders;
import com.example.ryomi.mugenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.db.datawrappers.ThemeData;
import com.example.ryomi.mugenglish.questiongenerator.QGUtils;
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.mugenglish.questiongenerator.Theme;
import com.example.ryomi.mugenglish.tools.SportsHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NAME_is_playing_SPORT extends Theme{
    private final String personNamePH = "personName";
    private final String personNameForeignPH = "personNameForeign";
    private final String personNameENPH = "personNameEN";
    private final String sportIDPH = "sportID";
    private final String sportNameENPH = "sportNameEN";
    private final String sportNameForeignPH = "sportNameForeign";

    private List<QueryResult> queryResults = new ArrayList<>();

    private class QueryResult {
        private String personID;
        private String personNameEN;
        private String personNameForeign;
        private String sportID;
        private String sportNameForeign;
        //we need these for creating questions.
        //we will get them from firebase
        private String verb = "";
        private String object = "";

        private QueryResult( String personID,
                             String personNameEN, String personNameForeign,
                             String sportID, String sportNameEN, String sportNameForeign){
            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameForeign = personNameForeign;
            this.sportID = sportID;
            this.sportNameForeign = sportNameForeign;
            //temporary. will replace if possible
            this.verb = "play";
            this.object = sportNameEN;
        }
    }

    public NAME_is_playing_SPORT(EndpointConnectorReturnsXML connector, ThemeData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 3;
		/*
		super.backupIDsOfTopics.add("Q10520");//Beckham
		super.backupIDsOfTopics.add("Q486359");//Pacquiao
		super.backupIDsOfTopics.add("Q41421");//Michael Jordan
		super.backupIDsOfTopics.add("Q39562");//Michael Phelps
		super.backupIDsOfTopics.add("Q5799");//Allyson Felix*/
    }

    @Override
    protected String getSPARQLQuery(){
        return
                "SELECT ?" + personNamePH + " ?" + personNameENPH + " ?" + personNameForeignPH +
                        " ?" + sportIDPH + " ?" + sportNameENPH + " ?" + sportNameForeignPH +  " " +
                        "		WHERE " +
                        "		{ " +
                        "		    ?" + personNamePH + " wdt:P31 wd:Q5 . " +
                        "			?" + personNamePH + " wdt:P641 ?" + sportIDPH + " . " +
                        "		    FILTER NOT EXISTS { ?" + personNamePH + " wdt:P570 ?dateDeath } . " +//死んでいない（played ではなくてplays）
                        "		  	SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                        WikiBaseEndpointConnector.ENGLISH + "' . " +
                        "				?" + personNamePH + " rdfs:label ?" + personNameENPH + " . " +
                        "               ?" + sportIDPH + " rdfs:label ?" + sportNameENPH + " } . " +
                        "		  	SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                        WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
                        WikiBaseEndpointConnector.ENGLISH + "' . " +
                        "				?" + personNamePH + " rdfs:label ?" + personNameForeignPH + " . " +
                        "				?" + sportIDPH + " rdfs:label ?" + sportNameForeignPH + " } . " +
                        "           BIND (wd:%s as ?" + personNamePH + ") " +
                        "		}";
    }

    @Override
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
            String sportID = SPARQLDocumentParserHelper.findValueByNodeName(head, sportIDPH);
            // ~entity/id になってるから削る
            sportID = QGUtils.stripWikidataID(sportID);
            String sportNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, sportNameForeignPH);
            String sportNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, sportNameENPH);

            QueryResult qr = new QueryResult(personID,
                    personNameEN, personNameForeign,
                    sportID, sportNameEN, sportNameForeign);

            queryResults.add(qr);

        }
    }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.personNameForeign);
        }
    }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();
            QuestionData trueFalseQuestion = createTrueFalseQuestion(qr);
            questionSet.add(trueFalseQuestion);

            QuestionData sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID));

        }
    }

    //we want to read from the database and then create the questions
    @Override
    protected void accessDBWhenCreatingQuestions(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(FirebaseDBHeaders.UTILS + "/sportsVerbMapping");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (QueryResult qr : queryResults){
                    String id = qr.sportID;
                    if (dataSnapshot.hasChild(id)){
                        String verb = (String)dataSnapshot.child(id).child("verb").getValue();
                        String object = (String)dataSnapshot.child(id).child("name").getValue();
                        qr.verb = verb;
                        if (object != null)
                            qr.object = object;
                        else
                            qr.object = "";
                    }

                }

                NAME_is_playing_SPORT.this.saveResultTopics();
                NAME_is_playing_SPORT.this.createQuestionsFromResults();
                NAME_is_playing_SPORT.super.saveNewQuestions();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String NAME_plays_SPORT_EN_correct(QueryResult qr){
        String verbObject = SportsHelper.getVerbObject(qr.verb, qr.object, SportsHelper.PRESENTPARTICIPLE);
        return qr.personNameEN + " is " + verbObject + ".";
    }

    private String formatSentenceForeign(QueryResult qr){
        return qr.personNameForeign + "は" + qr.sportNameForeign + "をしています。";
    }

    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personNameEN);
        pieces.add("is");
        String verb = SportsHelper.inflectVerb(qr.verb, SportsHelper.PRESENTPARTICIPLE);
        pieces.add(verb);
        if (!qr.object.equals(""))
            pieces.add(qr.object);

        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private QuestionData createTrueFalseQuestion(QueryResult qr){
        String question = this.NAME_plays_SPORT_EN_correct(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.personNameForeign);
        data.setQuestionType(QuestionTypeMappings.TRUE_FALSE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(QuestionUtils.TRUE_FALSE_QUESTION_TRUE);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }

    private QuestionData createSentencePuzzleQuestion(QueryResult qr){
        String question = formatSentenceForeign(qr);
        List<String> choices = puzzlePieces(qr);
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
}

