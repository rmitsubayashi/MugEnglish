package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NAME_is_DEMONYM extends Lesson {
    public static final String KEY = "NAME_is_DEMONYM";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String demonymEN;
        private final String demonymJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String countryJP,
                String demonymEN)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.demonymEN = demonymEN;
            this.demonymJP = convertCountryToDemonym(countryJP);
        }

        private String convertCountryToDemonym(String country){
            //first check to make sure it's Japanese
            Pattern p = Pattern.compile("[a-zA-Z]");
            Matcher m = p.matcher(country);

            //if it's written in English, return the demonym for English
            //(the demonym in English is instantiated already)
            if(m.find()){
                return demonymEN;
            } else {
                boolean katakanaStarted = false;
                for (int i=0; i<country.length(); i++){
                    char c = country.charAt(i);
                    if (isKatakana(c)){
                        katakanaStarted = true;
                    } else {
                        if (katakanaStarted){
                            country = country.substring(0,i);
                            break;
                        }
                    }
                }

                return country + "人";
            }
        }

        private boolean isKatakana(char c){
            return (c >= 'ァ' && c <= 'ヿ');
        }
    }

    public NAME_is_DEMONYM(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //since there aren't that many Japanese demonyms available,
        //just get the country name and convert it to a demonym by adding "~人"
        return "SELECT ?person ?personLabel ?personEN " +
                " ?demonymEN ?countryLabel " +
                "WHERE " +
                "{" +
                "    {?person wdt:P31 wd:Q5} UNION " + //is human
                "    {?person wdt:P31 wd:Q15632617} ." + //or fictional human
                "    ?person wdt:P27 ?country . " + //has a country of citizenship
                "    ?country wdt:P1549 ?demonymEN . " + //and the country has a demonym
                "    ?person rdfs:label ?personEN . " + //English label
                "    FILTER (LANG(?demonymEN) = '" +
                    WikiBaseEndpointConnector.ENGLISH + "') . " + //just get the English demonym
                "    FILTER (STR(?demonymEN) != 'United States') . " + //United States is noted as a demonym (can't edit out?)
                "    FILTER (LANG(?personEN) = '" +
                    WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" +
                    WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
                    WikiBaseEndpointConnector.ENGLISH + "' } " +
                "    BIND (wd:%s as ?person) . " + //binding the ID of entity as ?person
                "} ";

    }

    @Override
    protected void processResultsIntoClassWrappers(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");
            personID = LessonGeneratorUtils.stripWikidataID(personID);
            String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personEN");
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            String countryJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryLabel");
            String demonymEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "demonymEN");

            QueryResult qr = new QueryResult(personID, personEN, personJP, countryJP, demonymEN);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord demonym = new VocabularyWord("",qr.demonymEN, qr.demonymJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(demonym);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        String sentence = qr.personEN + " is " + qr.demonymEN + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.demonymJP + "です。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        pieces.add("is");
        pieces.add(qr.demonymEN);
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        String question = this.formatSentenceJP(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = qr.personEN + " is " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String fillInBlankAnswer(QueryResult qr){
        return qr.demonymEN;
    }

    private Queue<String> fillInBlankOptions(QueryResult qr){
        List<String> optionList = new ArrayList<>();
        optionList.add("French");
        optionList.add("Japanese");
        optionList.add("American");
        optionList.add("Korean");
        optionList.add("Chinese");
        optionList.add("German");
        optionList.add("Russian");
        optionList.add("British");
        optionList.add("Vietnamese");
        //remove if it is in the list so we don't choose it at first.
        //insert later
        optionList.remove(qr.demonymEN);
        Collections.shuffle(optionList);
        return new LinkedList<>(optionList);
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer(qr);
        List<QuestionData> questionDataList = new ArrayList<>();
        Queue<String> options = fillInBlankOptions(qr);
        while (options.size() > 2) {
            List<String> choices = new ArrayList<>();
            choices.add(options.remove());
            choices.add(options.remove());
            choices.add(answer);
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(qr.personJP);
            data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
            data.setQuestion(question);
            data.setChoices(choices);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);


            questionDataList.add(data);
        }

        return questionDataList;
    }
}
