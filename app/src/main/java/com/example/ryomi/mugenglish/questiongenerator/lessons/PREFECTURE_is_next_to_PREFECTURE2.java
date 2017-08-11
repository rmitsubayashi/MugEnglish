package com.example.ryomi.mugenglish.questiongenerator.lessons;

import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.mugenglish.db.datawrappers.LessonData;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.questiongenerator.GrammarRules;
import com.example.ryomi.mugenglish.questiongenerator.QGUtils;
import com.example.ryomi.mugenglish.questiongenerator.QuestionDataWrapper;
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.mugenglish.questiongenerator.Lesson;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PREFECTURE_is_next_to_PREFECTURE2 extends Lesson {
    public static final String KEY = "PREFECTURE_is_next_to_PREFECTURE2";
    //placeholders
    private final String prefecture1PH = "prefecture1";
    private final String prefecture1ForeignPH = "prefecture1Foreign";
    private final String prefecture1ENPH = "prefecture1EN";
    private final String prefecture2ENPH = "prefecture2EN";
    private final String prefecture2ForeignPH = "prefecture2Foreign";

    //to help organize the prefectures so we know all prefectures that border a certain prefecture
    private Map<String, List<QueryResult>> queryResultMap = new HashMap<>();
    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String prefecture1ID;
        private String prefecture1EN;
        private String prefecture1Foreign;
        private String prefecture2EN;
        private String prefecture2Foreign;

        private QueryResult(
                String prefecture1ID,
                String prefecture1EN,
                String prefecture1Foreign,
                String prefecture2EN,
                String prefecture2Foreign)
        {
            this.prefecture1ID = prefecture1ID;
            this.prefecture1EN = removePrefectureTag(prefecture1EN);
            this.prefecture1Foreign = prefecture1Foreign;
            this.prefecture2EN = removePrefectureTag(prefecture2EN);
            this.prefecture2Foreign = prefecture2Foreign;
        }

        private String removePrefectureTag(String prefecture){
            return prefecture.replace(" Prefecture","");
        }
    }

    private List<String> prefectures = new ArrayList<>();

    private void populatePrefectures() {
        prefectures.add("Tokyo");
        prefectures.add("Kagoshima");
        prefectures.add("Tochigi");
        prefectures.add("Miyagi");
        prefectures.add("Iwate");
        prefectures.add("Aomori");
        prefectures.add("Fukushima");
        prefectures.add("Chiba");
        prefectures.add("Aichi");
        prefectures.add("Akita");
        prefectures.add("Ibaraki");
        prefectures.add("Kyōto");
        prefectures.add("Ōsaka");
        prefectures.add("Fukuoka");
        prefectures.add("Ehime");
        prefectures.add("Yamagata");
        prefectures.add("Yamaguchi");
        prefectures.add("Kanagawa");
        prefectures.add("Nagano");
        prefectures.add("Saitama");
        prefectures.add("Mie");
        prefectures.add("Gunma");
        prefectures.add("Hyōgo");
        prefectures.add("Miyazaki");
        prefectures.add("Kumamoto");
        prefectures.add("Gifu");
        prefectures.add("Ishikawa");
        prefectures.add("Nara");
        prefectures.add("Wakayama");
        prefectures.add("Shizuoka");
        prefectures.add("Shiga");
        prefectures.add("Niigata");
        prefectures.add("Yamanashi");
        prefectures.add("Shimane");
        prefectures.add("Toyama");
        prefectures.add("Okayama");
        prefectures.add("Fukui");
        prefectures.add("Ōita");
        prefectures.add("Tottori");
        prefectures.add("Kōchi");
        prefectures.add("Saga");
        prefectures.add("Tokushima");
        prefectures.add("Kagawa");
        prefectures.add("Nagasaki");
        prefectures.add("Hiroshima");
        prefectures.add("Okinawa");
        prefectures.add("Hokkaidō");
    }

    public PREFECTURE_is_next_to_PREFECTURE2(WikiBaseEndpointConnector connector, LessonData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 4;
        populatePrefectures();
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?" + prefecture1PH + " ?" + prefecture1ForeignPH + " ?" + prefecture1ENPH +
                " ?" + prefecture2ENPH + " ?" + prefecture2ForeignPH + " " +
                "WHERE " +
                "{" +
                "    ?" + prefecture1PH + " wdt:P31 wd:Q50337; " + //is a JP prefecture
                "                          wdt:P47 ?prefecture2 . " + //borders prefecture2
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + prefecture1PH + " rdfs:label ?" + prefecture1ForeignPH + " . " +
                "                           ?prefecture2 rdfs:label ?" + prefecture2ForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "'," +
                "       '" + WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "' . " + //fallback language Japanese
                "                           ?" + prefecture1PH + " rdfs:label ?" + prefecture1ENPH + " . " +
                "                           ?prefecture2 rdfs:label ?" + prefecture2ENPH + " . " +
                "                           } . " +
                "    BIND (wd:%s as ?" + prefecture1PH + ") . " +
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
            String prefecture1ID = SPARQLDocumentParserHelper.findValueByNodeName(head, prefecture1PH);
            prefecture1ID = QGUtils.stripWikidataID(prefecture1ID);
            String prefecture1EN = SPARQLDocumentParserHelper.findValueByNodeName(head, prefecture1ENPH);
            String prefecture1Foreign = SPARQLDocumentParserHelper.findValueByNodeName(head, prefecture1ForeignPH);
            String prefecture2EN = SPARQLDocumentParserHelper.findValueByNodeName(head, prefecture2ENPH);
            String prefecture2Foreign = SPARQLDocumentParserHelper.findValueByNodeName(head, prefecture2ForeignPH);

            QueryResult qr = new QueryResult(prefecture1ID, prefecture1EN, prefecture1Foreign, prefecture2EN, prefecture2Foreign);
            queryResults.add(qr);

            //we want to categorize the prefectures so we can do an accurate true/false question
            if (queryResultMap.containsKey(prefecture1EN)){
                List<QueryResult> value = queryResultMap.get(prefecture1EN);
                value.add(qr);
            } else {
                List<QueryResult> list = new ArrayList<>();
                list.add(qr);
                queryResultMap.put(prefecture1ID, list);
            }
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.prefecture1Foreign);
        }
    }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();

            QuestionData fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            QuestionData trueFalseQuestionTrue = createTrueFalseQuestion(qr,true);
            QuestionData trueFalseQuestionFalse = createTrueFalseQuestion(qr,false);
            //random order of questions
            int i = new Random().nextInt();
            if (i%2 == 0) {
                questionSet.add(trueFalseQuestionTrue);
                questionSet.add(trueFalseQuestionFalse);
            }else{
                questionSet.add(trueFalseQuestionFalse);
                questionSet.add(trueFalseQuestionTrue);
            }

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.prefecture1ID));
        }

    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = qr.prefecture1EN + " is next to " + QuestionUtils.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private List<String> fillInBlankChoices(QueryResult qr){
        //copy so we don't change the original copy
        List<String> prefecturesCopy = new ArrayList<>(prefectures);
        List<QueryResult> borderingPrefectures = queryResultMap.get(qr.prefecture1ID);
        List<String> englishBorderingPrefectures = new ArrayList<>(borderingPrefectures.size());
        for (QueryResult bp : borderingPrefectures){
            englishBorderingPrefectures.add(bp.prefecture2EN);
        }

        prefecturesCopy.removeAll(englishBorderingPrefectures);
        prefecturesCopy.remove(qr.prefecture1EN);
        Collections.shuffle(prefecturesCopy);

        return prefecturesCopy.subList(0,2);
    }

    private String fillInBlankAnswer(QueryResult qr){
        return qr.prefecture2EN;
    }

    private QuestionData createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer(qr);
        List<String> choices = fillInBlankChoices(qr);
        choices.add(answer);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonData.getId());
        data.setTopic(qr.prefecture1Foreign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }

    private String trueFalseQuestion(QueryResult qr, boolean correct){
        String prefecture2;
        if (correct)
            prefecture2 = qr.prefecture2EN;
        else {
            List<String> prefecturesCopy = new ArrayList<>(prefectures);
            List<QueryResult> borderingPrefectures = queryResultMap.get(qr.prefecture1ID);
            List<String> englishBorderingPrefectures = new ArrayList<>(borderingPrefectures.size());
            for (QueryResult bp : borderingPrefectures){
                englishBorderingPrefectures.add(bp.prefecture2EN);
            }

            prefecturesCopy.removeAll(englishBorderingPrefectures);
            prefecturesCopy.remove(qr.prefecture1EN);
            Collections.shuffle(prefecturesCopy);
            prefecture2 = prefecturesCopy.get(0);
        }

        String sentence = qr.prefecture1EN + " is next to " + prefecture2 + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private QuestionData createTrueFalseQuestion(QueryResult qr, boolean correct){
        String question = trueFalseQuestion(qr, correct);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonData.getId());
        data.setTopic(qr.prefecture1Foreign);
        data.setQuestionType(QuestionTypeMappings.TRUE_FALSE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(correct ? QuestionUtils.TRUE_FALSE_QUESTION_TRUE : QuestionUtils.TRUE_FALSE_QUESTION_FALSE);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }
}
