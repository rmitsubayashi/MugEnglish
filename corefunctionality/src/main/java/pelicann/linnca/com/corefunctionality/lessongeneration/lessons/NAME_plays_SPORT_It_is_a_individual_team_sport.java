package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessongeneration.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.lessongeneration.SportsHelper;
import pelicann.linnca.com.corefunctionality.lessongeneration.TermAdjuster;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.questions.QuestionSetData;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

public class NAME_plays_SPORT_It_is_a_individual_team_sport extends Lesson{
    public static final String KEY = "NAME_plays_SPORT_It_is_a_individual_team_sport";

    private List<QueryResult> queryResults = new ArrayList<>();

    private class QueryResult {
        private String personID;
        private String personEN;
        private String personJP;
        private String sportID;
        private String sportNameEN;
        private String sportNameJP;
        private String sportTypeLabelEN;
        private String sportTypeLabelJP;
        //we need these for creating questions.
        //we will get them from firebase
        private String verb = "";
        private String object = "";

        private QueryResult( String personID,
                             String personEN, String personJP,
                             String sportID, String sportNameEN, String sportNameJP,
                             boolean isTeamSport){
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.sportID = sportID;
            this.sportNameEN = sportNameEN;
            this.sportNameJP = sportNameJP;
            //temporary. will update by connecting to db
            this.verb = "play";
            //also temporary
            this.object = sportNameEN;
            this.sportTypeLabelEN = isTeamSport ? "team sport" : "individual sport";
            this.sportTypeLabelJP = isTeamSport ? "団体競技" : "個人競技";
        }
    }

    public NAME_plays_SPORT_It_is_a_individual_team_sport(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;

    }

    @Override
    protected String getSPARQLQuery(){
        return
                "SELECT ?person ?personEN ?personLabel " +
                        " ?sport ?sportEN ?sportLabel " +
                        " ?instance " +
                        "		WHERE " +
                        "		{ " +
                        "			?person wdt:P641 ?sport . " + //plays sport
                        "           {?sport wdt:P279* wd:Q2755547 . " + //sport is either individual
                        "           BIND ('individual' AS ?instance) } UNION " +
                        "           {?sport wdt:P279* wd:Q216048 . " + //sport is either individual
                        "           BIND ('team' AS ?instance) } . " +
                        "           ?person rdfs:label ?personEN . " + //English label
                        "           ?sport rdfs:label ?sportEN . " + //English label
                        "           FILTER (LANG(?personEN) = '" +
                        WikiBaseEndpointConnector.ENGLISH + "') . " +
                        "           FILTER (LANG(?sportEN) = '" +
                        WikiBaseEndpointConnector.ENGLISH + "') . " +
                        "           SERVICE wikibase:label {bd:serviceParam wikibase:language '" +
                        WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
                        WikiBaseEndpointConnector.ENGLISH + "' } " +
                        "           BIND (wd:%s as ?person) " +
                        "		}";
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
            String sportID = SPARQLDocumentParserHelper.findValueByNodeName(head, "sport");
            // ~entity/id になってるから削る
            sportID = WikiDataEntity.getWikiDataIDFromReturnedResult(sportID);
            String sportNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "sportLabel");
            String sportNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "sportEN");
            sportNameEN = TermAdjuster.adjustSportsEN(sportNameEN);
            String sportType = SPARQLDocumentParserHelper.findValueByNodeName(head, "instance");
            boolean isTeamSport = sportType.equals("team");

            QueryResult qr = new QueryResult(personID,
                    personEN, personJP,
                    sportID, sportNameEN, sportNameJP,
                    isTeamSport);

            queryResults.add(qr);

        }
    }

    @Override
    protected synchronized int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected synchronized void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            List<QuestionData> translateQuestion = createTranslationQuestion(qr);
            questionSet.add(translateQuestion);

            List<QuestionData> spellingQuestion = createSpellingQuestion(qr);
            questionSet.add(spellingQuestion);

            List<QuestionData> trueFalseQuestion = createTrueFalseQuestion(qr);
            questionSet.add(trueFalseQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }
    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord sport = new VocabularyWord("",qr.sportNameEN, qr.sportNameJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        VocabularyWord sportType = new VocabularyWord("", qr.sportTypeLabelEN,qr.sportTypeLabelJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(3);
        words.add(sport);
        words.add(sportType);

        if (qr.object.equals("")) {
            VocabularyWord additionalWord = new VocabularyWord("", qr.verb, qr.sportNameJP + "をする",
                    formatSentenceEN(qr), formatSentenceJP(qr), KEY);
            words.add(additionalWord);
        }

        return words;
    }

    //we want to read from the database and then create the questions
    @Override
    protected void accessDBWhenCreatingQuestions(){
        Set<String> sportIDs = new HashSet<>(queryResults.size());
        for (QueryResult qr : queryResults){
            sportIDs.add(qr.sportID);
        }
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onSportQueried(String wikiDataID, String verb, String object) {
                //find all query results with the sport ID and update it
                for (QueryResult qr : queryResults){
                    if (qr.sportID.equals(wikiDataID)){
                        qr.verb = verb;
                        qr.object = object;
                    }
                }
            }

            @Override
            public void onSportsQueried() {
                createQuestionsFromResults();
                saveNewQuestions();
            }
        };
        db.getSports(sportIDs, onDBResultListener);
    }

    private String formatSentenceEN(QueryResult qr){
        String verbObject = SportsHelper.getVerbObject(qr.verb, qr.object, SportsHelper.PRESENT3RD);
        String sentence1 = qr.personEN + " " + verbObject + ".";
        String sentence2 = "It is " + GrammarRules.indefiniteArticleBeforeNoun(qr.sportTypeLabelEN) + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n" + sentence2;
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.sportNameJP + "をします。それは" + qr.sportTypeLabelJP + "です。";
    }

    private List<QuestionData> createTranslateQuestionGeneric(){
        String question = "団体競技";
        String answer = "team sport";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> createTranslateQuestionGeneric2(){
        String question = "individual sport";
        String answer = "個人競技";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<QuestionData> translateQuestion = createTranslateQuestionGeneric();
        List<QuestionData> translateQuestion2 = createTranslateQuestionGeneric2();
        List<List<QuestionData>> questionsSet = new ArrayList<>(2);
        questionsSet.add(translateQuestion);
        questionsSet.add(translateQuestion2);

        return questionsSet;

    }

    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        String verb = SportsHelper.inflectVerb(qr.verb, SportsHelper.PRESENT3RD);
        pieces.add(verb);
        if (!qr.object.equals(""))
            pieces.add(qr.object);
        pieces.add("it");
        pieces.add("is a");
        pieces.add(qr.sportTypeLabelEN);

        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionSerializer.serializeSentencePuzzleAnswer(puzzlePieces(qr));
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        String question = formatSentenceJP(qr);
        List<String> choices = puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setQuestionType(QuestionTypeMappings.SENTENCEPUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        questionDataList.add(data);
        return questionDataList;
    }

    private List<QuestionData> createTranslationQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        data.setQuestion(qr.object);
        data.setChoices(null);
        data.setAnswer(qr.sportNameJP);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);
        return questionDataList;
    }

    private List<QuestionData> createSpellingQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.CHOOSECORRECTSPELLING);
        data.setQuestion(qr.sportNameJP);
        data.setChoices(null);
        data.setAnswer(qr.object);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);
        return questionDataList;
    }

    private String trueFalseQuestionQuestion(QueryResult qr, boolean isTrue){
        if (isTrue){
            String sentence = qr.sportNameEN + " is " + GrammarRules.indefiniteArticleBeforeNoun(qr.sportTypeLabelEN) + ".";
            return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        } else {
            String wrongSportLabel = qr.sportTypeLabelEN.equals("team sport") ? "an individual sport" : "a team sport";
            String sentence = qr.sportNameEN + " is " + wrongSportLabel + ".";
            return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        }
    }

    private List<QuestionData> createTrueFalseQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(2);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRUEFALSE);
        data.setQuestion(trueFalseQuestionQuestion(qr, true));
        data.setChoices(null);
        data.setAnswer(QuestionSerializer.serializeTrueFalseAnswer(true));
        data.setAcceptableAnswers(null);

        questionDataList.add(data);

        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRUEFALSE);
        data.setQuestion(trueFalseQuestionQuestion(qr, false));
        data.setChoices(null);
        data.setAnswer(QuestionSerializer.serializeTrueFalseAnswer(false));
        data.setAcceptableAnswers(null);

        questionDataList.add(data);
        return questionDataList;
    }

}