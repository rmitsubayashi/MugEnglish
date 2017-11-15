package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.lessongenerator.SportsHelper;
import com.linnca.pelicann.lessongenerator.TermAdjuster;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_MultipleChoice;
import com.linnca.pelicann.questions.Question_SentencePuzzle;
import com.linnca.pelicann.questions.Question_Spelling_Suggestive;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NAME_possessive_husband_wife_plays_SPORT_he_is_a_OCCUPATION extends Lesson{
    public static final String KEY = "NAME_possessive_husband_wife_plays_SPORT_he_is_a_OCCUPATION";

    private List<QueryResult> queryResults = new ArrayList<>();

    private class QueryResult {
        private final String personID;
        private final String personJP;
        private final String spouseEN;
        private final String spouseJP;
        private final String sportID;
        private final String sportNameEN;
        private final String sportNameJP;
        private final String personTitleEN;
        private final String personTitleJP;
        private final String genderPronounEN;
        private final String genderPronounJP;
        private final String occupationEN;
        private final String occupationJP;
        //we need these for creating questions.
        //we will get them from firebase
        private String verb = "";
        private String object = "";


        private QueryResult( String personID, String personJP, 
                             String spouseEN, String spouseJP,
                             String sportID, String sportNameEN, String sportNameJP,
                             String personGenderID,
                             String occupationEN, String occupationJP){
            this.personID = personID;
            this.personJP = personJP;
            this.spouseEN = spouseEN;
            this.spouseJP = spouseJP;
            this.sportID = sportID;
            this.sportNameEN = sportNameEN;
            this.sportNameJP = sportNameJP;
            this.occupationEN = occupationEN;
            this.occupationJP = occupationJP;
            this.personTitleEN = getPersonTitleEN(personGenderID);
            this.personTitleJP = getPersonTitleJP(personGenderID);
            this.genderPronounEN = getPersonPronounEN(personGenderID);
            this.genderPronounJP = getPersonPronounJP(personGenderID);
            //temporary. will update by connecting to db
            this.verb = "play";
            //also temporary
            this.object = sportNameEN;
        }

        private String getPersonTitleEN(String id){
            switch (id){
                case "Q6581097":
                    return "husband";
                case "Q6581072":
                    return "wife";
                default:
                    return "husband";
            }
        }

        private String getPersonTitleJP(String id){
            switch (id){
                case "Q6581097":
                    return "夫";
                case "Q6581072":
                    return "妻";
                default:
                    return "夫";
            }
        }

        private String getPersonPronounEN(String id){
            switch (id){
                case "Q6581097":
                    return "he";
                case "Q6581072":
                    return "she";
                default:
                    return "he";
            }
        }

        private String getPersonPronounJP(String id){
            switch (id){
                case "Q6581097":
                    return "彼";
                case "Q6581072":
                    return "彼女";
                default:
                    return "彼";
            }
        }
    }

    public NAME_possessive_husband_wife_plays_SPORT_he_is_a_OCCUPATION(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 4;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        return
                "SELECT ?person ?personLabel ?gender " +
                        " ?spouseEN ?spouseLabel " +
                        " ?sport ?sportEN ?sportLabel " +
                        " ?occupationEN ?occupationLabel " +
                        "		WHERE " +
                        "		{ " +
                        "           {?person wdt:P31 wd:Q5} UNION " + //is human
                        "           {?person wdt:P31 wd:Q15632617} ." + //or fictional human
                        "			?person wdt:P641 ?sport . " + //plays sport
                        "           ?person wdt:P106 ?occupation . " + //has an occupation
                        "           ?occupation wdt:P425 ?sport . " + //of that sport
                        "           ?person wdt:P26 ?spouse . " + //has a spouse
                        "           ?person wdt:P21 ?gender . " + //get gender of person
                        "           ?spouse rdfs:label ?spouseEN . " + //English label
                        "           ?occupation rdfs:label ?occupationEN . " + //English label
                        "           ?sport rdfs:label ?sportEN . " + //English label
                        "           FILTER (LANG(?spouseEN) = '" +
                        WikiBaseEndpointConnector.ENGLISH + "') . " +
                        "           FILTER (LANG(?sportEN) = '" +
                        WikiBaseEndpointConnector.ENGLISH + "') . " +
                        "           FILTER (LANG(?occupationEN) = '" +
                        WikiBaseEndpointConnector.ENGLISH + "') . " +
                        "           SERVICE wikibase:label {bd:serviceParam wikibase:language '" +
                        WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
                        WikiBaseEndpointConnector.ENGLISH + "' } " +
                        "           BIND (wd:%s as ?person) " +
                        "		}";
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
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            String spouseEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "spouseEN");
            String spouseJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "spouseLabel");
            String sportID = SPARQLDocumentParserHelper.findValueByNodeName(head, "sport");
            sportID = LessonGeneratorUtils.stripWikidataID(sportID);
            String sportNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "sportLabel");
            String sportNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "sportEN");
            sportNameEN = TermAdjuster.adjustSportsEN(sportNameEN);
            String genderID = SPARQLDocumentParserHelper.findValueByNodeName(head, "gender");
            genderID = LessonGeneratorUtils.stripWikidataID(genderID);
            String occupationJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "occupationLabel");
            String occupationEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "occupationEN");
            occupationEN = TermAdjuster.adjustOccupationEN(occupationEN);

            QueryResult qr = new QueryResult(personID, personJP,
                    spouseEN, spouseJP,
                    sportID, sportNameEN, sportNameJP,
                    genderID,
                    occupationEN, occupationJP);

            queryResults.add(qr);

        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> fillInBlankMultipleChoiceQuestion = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoiceQuestion);

            List<QuestionData> multipleChoiceQuestion = createMultipleChoiceQuestion(qr);
            questionSet.add(multipleChoiceQuestion);

            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            List<QuestionData> spellingQuestion = createSpellingQuestion(qr);
            questionSet.add(spellingQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personJP, vocabularyWords));

        }
    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord sport = new VocabularyWord("",qr.sportNameEN, qr.sportNameJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        VocabularyWord occupation = new VocabularyWord("", qr.occupationEN, qr.occupationJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(3);
        words.add(sport);
        words.add(occupation);

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
        OnResultListener onResultListener = new OnResultListener() {
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
        db.getSports(sportIDs, onResultListener);
    }

    private String formatSentenceEN(QueryResult qr){
        String verbObject = SportsHelper.getVerbObject(qr.verb, qr.object, SportsHelper.PRESENT3RD);
        String occupation = GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN);
        String sentence1 = qr.spouseEN + "'s " + qr.personTitleEN + " " + verbObject + ".";
        String sentence2 = qr.genderPronounEN + " is " + occupation + ".";
        sentence1 = GrammarRules.uppercaseFirstLetterOfSentence(sentence1);
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n" + sentence2;
    }

    private String formatSentenceJP(QueryResult qr){
        String sentence1 = qr.spouseJP + "の" + qr.personTitleJP + "は" + qr.sportNameJP + "をします。";
        String sentence2 = qr.genderPronounJP + "は" + qr.occupationJP + "です。";
        return sentence1 + "\n" + sentence2;
    }

    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.spouseEN);
        pieces.add("'s");
        pieces.add(qr.personTitleEN);
        String verb = SportsHelper.inflectVerb(qr.verb, SportsHelper.PRESENT3RD);
        pieces.add(verb);
        if (!qr.object.equals(""))
            pieces.add(qr.object);

        pieces.add(qr.genderPronounEN);
        pieces.add("is");
        pieces.add(GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN));

        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        String question = formatSentenceJP(qr);
        List<String> choices = puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);
        return questionDataList;
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String verbObject = SportsHelper.getVerbObject(qr.verb, qr.object, SportsHelper.PRESENT3RD);
        String occupation = GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN);
        String sentence1 = qr.spouseEN + "'s " + qr.personTitleEN + " " + verbObject + ".";
        String sentence2 = Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + " is " + occupation + ".";
        sentence1 = GrammarRules.uppercaseFirstLetterOfSentence(sentence1);
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n" + sentence2;
    }

    private String fillInBlankMultipleChoiceAnswer(QueryResult qr){
        return qr.genderPronounEN;
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("he");
        choices.add("she");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        QuestionData data = new QuestionData();
        String question = fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer(qr);
        List<String> choices = fillInBlankMultipleChoiceChoices();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);
        return questionDataList;
    }

    private String multipleChoiceQuestion(QueryResult qr){
        String verbObject = SportsHelper.getVerbObject(qr.verb, qr.object, SportsHelper.PRESENT3RD);
        String sentence1 = qr.spouseEN + "'s " + qr.personTitleEN + " " + verbObject + ".";
        String sentence2 = qr.genderPronounEN + " is a ...";
        sentence1 = GrammarRules.uppercaseFirstLetterOfSentence(sentence1);
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n" + sentence2;
    }

    private String multipleChoiceAnswer(QueryResult qr){
        return qr.occupationEN;
    }
    
    //don't need to worry about players having multiple sports
    //because of context clues indicating the correct sport.
    private Map<String, String> multipleChoiceChoices(){
        //since there are multiple professions with different names
        // (ie professional baseball player & baseball player)
        // the ID should be the sport name (no duplicate)
        Map<String, String> choices = new HashMap<>(7);
        choices.put("Q2736","soccer player");
        choices.put("Q5369","baseball player");
        choices.put("Q31920","swimmer");
        choices.put("Q38108","figure skater");
        choices.put("Q847","tennis player");
        choices.put("Q3930","table tennis player");
        choices.put("Q1734","volleyball player");
        return choices;
    }

    private List<QuestionData> createMultipleChoiceQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(3);
        String question = multipleChoiceQuestion(qr);
        String answer = multipleChoiceAnswer(qr);
        //7 total
        Map<String, String> allChoices = multipleChoiceChoices();
        //remove current
        allChoices.remove(qr.sportID);
        //at most 6
        List<String> choiceList = new ArrayList<>(allChoices.values());
        Collections.shuffle(choiceList);
        for (int i=0; i<3; i++) {
            //grabbing 2 each (*3 = 6)
            List<String> choices = new ArrayList<>(2);
            choices.add(choiceList.get(i*2));
            choices.add(choiceList.get(i*2+1));
            choices.add(answer);
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(qr.personJP);
            data.setQuestionType(Question_MultipleChoice.QUESTION_TYPE);
            data.setQuestion(question);
            data.setChoices(choices);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questionDataList.add(data);
        }
        return questionDataList;
    }

    private List<QuestionData> createSpellingQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_Spelling_Suggestive.QUESTION_TYPE);
        data.setQuestion(qr.occupationJP);
        data.setChoices(null);
        data.setAnswer(qr.occupationEN);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);
        return questionDataList;
    }
}