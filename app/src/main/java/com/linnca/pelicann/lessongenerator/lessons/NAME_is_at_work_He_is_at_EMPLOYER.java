package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NAME_is_at_work_He_is_at_EMPLOYER extends Lesson {
    public static final String KEY = "NAME_is_at_work_He_is_at_EMPLOYER";

    private final List<QueryResult> queryResults = new ArrayList<>();

    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String employerID;
        private final String employerEN;
        private final String employerJP;
        private final String genderEN;
        private final String genderJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String employerID,
                String employerEN,
                String employerJP,
                boolean isMale
                )
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.employerID = employerID;
            this.employerEN = employerEN;
            this.employerJP = employerJP;
            this.genderEN = isMale ? "he" : "she";
            this.genderJP = isMale ? "彼" : "彼女";
        }
    }

    public NAME_is_at_work_He_is_at_EMPLOYER(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //since there aren't that many Japanese employers available,
        //just get the employer name and convert it to a employer by adding "~人"
        return "SELECT ?person ?personLabel ?personEN " +
                " ?employer ?employerEN ?employerLabel " +
                " ?gender " +
                "WHERE " +
                "{" +
                "    {?person wdt:P31 wd:Q5} UNION " + //is human
                "    {?person wdt:P31 wd:Q15632617} . " + //or fictional human
                "    ?person wdt:P21 ?gender . " + //has gender
                "    ?person wdt:P108 ?employer . " + //has an employer
                "    ?person rdfs:label ?personEN . " + //English label
                "    ?employer rdfs:label ?employerEN . " + //English label
                "    FILTER (LANG(?employerEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
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
            String employerID = SPARQLDocumentParserHelper.findValueByNodeName(head, "employerName");
            employerID = LessonGeneratorUtils.stripWikidataID(employerID);
            String employerJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "employerLabel");
            String employerEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "employerEN");
            String genderID = SPARQLDocumentParserHelper.findValueByNodeName(head, "gender");
            genderID = LessonGeneratorUtils.stripWikidataID(genderID);
            boolean isMale;
            switch (genderID){
                case "Q6581097":
                    isMale = true;
                    break;
                case "Q6581072":
                    isMale = false;
                    break;
                default:
                    isMale = true;
            }

            QueryResult qr = new QueryResult(personID, personEN, personJP, employerID, employerEN, employerJP, isMale);
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

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);
            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord work = new VocabularyWord("","work", "仕事",
                NAME_is_at_work_EN(qr), qr.personJP + "は働いています。", KEY);
        VocabularyWord employer = new VocabularyWord("",qr.employerEN, qr.employerJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(work);
        words.add(employer);
        return words;
    }

    /* Note that some of these employers may need the article 'the' before it.
     * We can't guarantee that all of them will be accurate...
     * Just make sure to let the user be aware that there may be some mistakes
     * */
    private String NAME_is_at_work_EN(QueryResult qr){
        String sentence = qr.personEN + " is at work.";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceEN(QueryResult qr) {
        String sentence1 = NAME_is_at_work_EN(qr);
        String sentence2 = qr.genderEN + " is at " + qr.employerEN + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n" + sentence2;
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は働いています。"+qr.genderJP+"は"+qr.employerJP+"にいます。";
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence = NAME_is_at_work_EN(qr);
        String sentence2 = Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + " is at " + qr.employerEN + ".";
        return sentence + "\n" + sentence2;
    }


    private String fillInBlankMultipleChoiceAnswer(QueryResult qr){
        return GrammarRules.uppercaseFirstLetterOfSentence(qr.genderEN);
    }

    private List<String> fillInBlankMultipleChoiceChoices(QueryResult qr){
        List<String> choices = new ArrayList<>(2);
        choices.add("He");
        choices.add("She");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer(qr);
        List<QuestionData> questionDataList = new ArrayList<>();
        List<String> choices = fillInBlankMultipleChoiceChoices(qr);
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

        return questionDataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = qr.personEN + " is " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + ".";
        String sentence2 = qr.genderEN + " is at " + qr.employerEN + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence + "\n" + sentence2;
    }

    private String fillInBlankAnswer(){
        return "at work";
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        List<QuestionData> questionDataList = new ArrayList<>();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        questionDataList.add(data);

        return questionDataList;
    }



    //TODO preposition question
}
