package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

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

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessongeneration.FeedbackPair;
import pelicann.linnca.com.corefunctionality.lessongeneration.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.questions.QuestionSetData;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.questions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

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

    public NAME_is_from_COUNTRY(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;

    }

    @Override
    protected String getSPARQLQuery(){
        //since there aren't that many Japanese countrys available,
        //just get the country name and convert it to a country by adding "~人"
        return "SELECT ?person ?personLabel ?personEN " +
                " ?country ?countryEN ?countryLabel " +
                "WHERE " +
                "{" +
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
    protected synchronized void processResultsIntoClassWrappers(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");
            personID = WikiDataEntity.getWikiDataIDFromReturnedResult(personID);
            String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personEN");
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            String countryID = SPARQLDocumentParserHelper.findValueByNodeName(head, "country");
            countryID = WikiDataEntity.getWikiDataIDFromReturnedResult(countryID);
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
    protected synchronized int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected synchronized void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            List<QuestionData> fillInBlankQuestion2 = createFillInBlankQuestion2(qr);
            questionSet.add(fillInBlankQuestion2);

            List<QuestionData> fillInBlankQuestion3 = createFillInBlankQuestion3(qr);
            questionSet.add(fillInBlankQuestion3);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
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
        pieces.add("is");
        pieces.add("from");
        pieces.add(GrammarRules.definiteArticleBeforeCountry(qr.countryEN));
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionSerializer.serializeSentencePuzzleAnswer(puzzlePieces(qr));
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        String question = this.formatSentenceJP(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.SENTENCEPUZZLE);
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

        CountryHelper(String wikiDataID, String nameEN, String nameJP) {
            this.wikiDataID = wikiDataID;
            this.nameEN = nameEN;
            this.nameJP = nameJP;

        }
    }
    private List<CountryHelper> fillInBlankOptions(QueryResult qr){
        List<CountryHelper> optionList = new LinkedList<>();
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
        return optionList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = formatSentenceEN(qr);
        String sentence2 = qr.personJP + "は" + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
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
        List<CountryHelper> options = fillInBlankOptions(qr);
        while (options.size() > 2) {
            List<String> choices = new ArrayList<>();
            choices.add(options.get(0).nameJP);
            options.remove(0);
            choices.add(options.get(0).nameJP);
            options.remove(0);
            choices.add(answer);
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
            data.setQuestion(question);
            data.setChoices(choices);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);


            questionDataList.add(data);
        }

        return questionDataList;
    }

    private String fillInBlankQuestion2(QueryResult qr){
        String sentence = qr.personEN + " is from " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String fillInBlankAnswer2(QueryResult qr){
        return GrammarRules.definiteArticleBeforeCountry(qr.countryEN);
    }

    private FeedbackPair fillInBlankFeedback2(String answer, List<CountryHelper> falseCountries){
        //we are displaying choices the user may not have had yet,
        // so explain what they are
        List<String> responses = new ArrayList<>(falseCountries.size() + 1);
        responses.add(answer);
        StringBuilder feedback = new StringBuilder("");
        for (CountryHelper helper : falseCountries){
            responses.add(helper.nameEN);
            feedback.append(helper.nameEN);
            feedback.append(": ");
            feedback.append(helper.nameJP);
            feedback.append("\n");
        }
        return new FeedbackPair(responses, feedback.toString(), FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankQuestion2(QueryResult qr){
        String question = this.fillInBlankQuestion2(qr);
        String answer = fillInBlankAnswer2(qr);
        List<QuestionData> questionDataList = new ArrayList<>();
        List<CountryHelper> options = fillInBlankOptions(qr);
        while (options.size() > 2) {
            List<String> choices = new ArrayList<>();
            CountryHelper option1 = options.get(0);
            CountryHelper option2 = options.get(1);
            choices.add(option1.nameEN);
            choices.add(option2.nameEN);
            options.remove(0);
            options.remove(0);
            List<CountryHelper> falseCountries = new ArrayList<>(2);
            falseCountries.add(option1);
            falseCountries.add(option2);
            List<FeedbackPair> allFeedback = new ArrayList<>(1);
            allFeedback.add(fillInBlankFeedback2(answer, falseCountries));
            choices.add(answer);
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
            data.setQuestion(question);
            data.setChoices(choices);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);
            data.setFeedback(allFeedback);

            questionDataList.add(data);
        }

        return questionDataList;
    }

    private String fillInBlankQuestion3(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer3(QueryResult qr){
        return "is from " + GrammarRules.definiteArticleBeforeCountry(qr.countryEN);
    }

    private List<String> fillInBlankAcceptableAnswers3(QueryResult qr){
        List<String> answers = new ArrayList<>(1);
        //only add if we did add a 'the'
        if (!qr.countryEN.equals(GrammarRules.definiteArticleBeforeCountry(qr.countryEN))){
            answers.add("is from " + qr.countryEN);
        }
        //also accept the country name without 'the' if it was already there
        if (qr.countryEN.startsWith("the ")){
            answers.add(qr.countryEN.replace("the ", ""));
        } else if (qr.countryEN.startsWith("The ")){
            answers.add(qr.countryEN.replace("The ", ""));
        }
        return answers;

    }

    private FeedbackPair fillInBlankFeedback3(QueryResult qr){
        List<String> responses = new ArrayList<>(4);
        String response = " is from " + qr.countryEN.toLowerCase();
        String response2 = " is from " + qr.countryEN.toLowerCase() + ".";
        String response3 = " is from " +
                GrammarRules.definiteArticleBeforeCountry(qr.countryEN).toLowerCase();
        String response4 = " is from " +
                GrammarRules.definiteArticleBeforeCountry(qr.countryEN.toLowerCase()) + ".";
        responses.add(response);
        responses.add(response2);
        responses.add(response3);
        responses.add(response4);
        String feedback = "国の名前は大文字で始まります";
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankQuestion3(QueryResult qr){
        String question = this.fillInBlankQuestion3(qr);
        String answer = fillInBlankAnswer3(qr);
        List<String> acceptableAnswers = fillInBlankAcceptableAnswers3(qr);
        List<FeedbackPair> allFeedback = new ArrayList<>(1);
        allFeedback.add(fillInBlankFeedback3(qr));
        List<QuestionData> questionDataList = new ArrayList<>();

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setFeedback(allFeedback);

        questionDataList.add(data);

        return questionDataList;
    }
}
