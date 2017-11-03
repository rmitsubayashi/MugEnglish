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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class NAME_is_from_COUNTRY extends Lesson {
    public static final String KEY = "NAME_is_from_COUNTRY";

    private final List<QueryResult> queryResults = new ArrayList<>();
    //there may be people with multiple citizenship
    private final Map<String, List<String>> queryResultMap = new HashMap<>();

    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String countryEN;
        private final String countryJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String countryJP,
                String countryEN)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.countryEN = countryEN;
            this.countryJP = countryJP;
        }
    }

    public NAME_is_from_COUNTRY(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.questionSetsLeftToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //since there aren't that many Japanese countrys available,
        //just get the country name and convert it to a country by adding "~人"
        return "SELECT ?person ?personLabel ?personEN " +
                " ?country ?countryEN ?countryLabel " +
                "WHERE " +
                "{" +
                "    {?person wdt:P31 wd:Q5} UNION " + //is human
                "    {?person wdt:P31 wd:Q15632617} ." + //or fictional human
                "    ?person wdt:P27 ?country . " + //has a country of citizenship
                "    ?country rdfs:label ?countryEN . " + //English Label
                "    ?person rdfs:label ?personEN . " + //English label
                "    FILTER (LANG(?countryEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " + //just get the English country
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
            String countryID = SPARQLDocumentParserHelper.findValueByNodeName(head, "country");
            countryID = LessonGeneratorUtils.stripWikidataID(countryID);
            String countryJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryLabel");
            String countryEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryEN");

            QueryResult qr = new QueryResult(personID, personEN, personJP, countryJP, countryEN);
            queryResults.add(qr);

            if (queryResultMap.containsKey(personID)){
                List<String> value = queryResultMap.get(personID);
                value.add(countryID);
            } else {
                List<String> list = new ArrayList<>();
                list.add(countryID);
                queryResultMap.put(personID, list);
            }
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            List<QuestionData> fillInBlankQuestion2 = createFillInBlankQuestion2(qr);
            questionSet.add(fillInBlankQuestion2);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord country = new VocabularyWord("",qr.countryEN, qr.countryJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(country);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        String sentence = qr.personEN + " is from " +
                GrammarRules.definiteArticleBeforeCountry(qr.countryEN) + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.countryJP + "の出身です。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        pieces.add("is from");
        pieces.add(GrammarRules.definiteArticleBeforeCountry(qr.countryEN));
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

    private class CountryHelper {
        private String wikiDataID;
        private String nameEN;
        private String nameJP;

        public CountryHelper(String wikiDataID, String nameEN, String nameJP) {
            this.wikiDataID = wikiDataID;
            this.nameEN = nameEN;
            this.nameJP = nameJP;

        }
    }
    private Queue<String> fillInBlankOptions(QueryResult qr, boolean english){
        List<CountryHelper> optionList = new ArrayList<>();
        optionList.add(new CountryHelper("Q142","France","フランス"));
        optionList.add(new CountryHelper("Q17","Japan", "日本"));
        optionList.add(new CountryHelper("Q30","the United States of America","アメリカ合衆国"));
        optionList.add(new CountryHelper("Q884","South Korea","大韓民国"));
        optionList.add(new CountryHelper("Q148","China","中華人民共和国"));
        optionList.add(new CountryHelper("Q183","Germany","ドイツ"));
        optionList.add(new CountryHelper("Q159","Russia","ロシア"));
        optionList.add(new CountryHelper("Q145", "the United Kingdom", "イギリス"));
        optionList.add(new CountryHelper("Q145","Vietnam","ベトナム"));
        //remove if it is in the list so we don't choose it at first.
        //insert later
        List<String> countriesOfCitizenship = queryResultMap.get(qr.personID);
        for (Iterator<CountryHelper> iterator = optionList.iterator(); iterator.hasNext();){
            CountryHelper option = iterator.next();
            if (countriesOfCitizenship.contains(option.wikiDataID)){
                iterator.remove();
            }
        }
        Collections.shuffle(optionList);
        LinkedList<String> result = new LinkedList<>();
        for (CountryHelper option : optionList){
            if (english)
                result.add(option.nameEN);
            else
                result.add(option.nameJP);
        }
        return result;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = formatSentenceEN(qr);
        String sentence2 = qr.personJP + "は" + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                "の出身です。";
        return sentence + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer(QueryResult qr){
        return qr.countryJP;
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer(qr);
        List<QuestionData> questionDataList = new ArrayList<>();
        Queue<String> options = fillInBlankOptions(qr, false);
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

    private String fillInBlankQuestion2(QueryResult qr){
        String sentence = qr.personEN + " is from " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String fillInBlankAnswer2(QueryResult qr){
        return GrammarRules.definiteArticleBeforeCountry(qr.countryEN);
    }

    private List<QuestionData> createFillInBlankQuestion2(QueryResult qr){
        String question = this.fillInBlankQuestion2(qr);
        String answer = fillInBlankAnswer2(qr);
        List<QuestionData> questionDataList = new ArrayList<>();
        Queue<String> options = fillInBlankOptions(qr, true);
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
