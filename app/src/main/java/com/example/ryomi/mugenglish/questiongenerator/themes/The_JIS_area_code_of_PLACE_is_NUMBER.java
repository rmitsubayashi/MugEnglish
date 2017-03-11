package com.example.ryomi.mugenglish.questiongenerator.themes;

import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.db.datawrappers.ThemeData;
import com.example.ryomi.mugenglish.questiongenerator.GrammarRules;
import com.example.ryomi.mugenglish.questiongenerator.QGUtils;
import com.example.ryomi.mugenglish.questiongenerator.QuestionDataWrapper;
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.mugenglish.questiongenerator.Theme;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class The_JIS_area_code_of_PLACE_is_NUMBER extends Theme{
    //placeholders
    private final String placeNamePH = "placeName";
    private final String placeNameForeignPH = "placeNameForeign";
    private final String placeNameENPH = "placeNameEN";
    private final String areaCodePH = "areaCodePH";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String placeID;
        private String placeNameEN;
        private String placeNameForeign;
        private String areaCode;

        private QueryResult(
                String placeID,
                String placeNameEN,
                String placeNameForeign,
                String areaCode)
        {
            this.placeID = placeID;
            this.placeNameEN = placeNameEN;
            this.placeNameForeign = placeNameForeign;
            this.areaCode = areaCode;
        }
    }

    public The_JIS_area_code_of_PLACE_is_NUMBER(WikiBaseEndpointConnector connector, ThemeData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;

    }

    @Override
    protected String getSPARQLQuery(){
        //find city name
        return "SELECT ?" + placeNamePH + " ?" + placeNameForeignPH + " ?" + placeNameENPH +
                " ?" + areaCodePH + " " +
                "WHERE " +
                "{" +
                "    ?" + placeNamePH + " wdt:P429 ?" + areaCodePH + " . " + //has a JIS area code
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + placeNamePH + " rdfs:label ?" + placeNameForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + placeNamePH + " rdfs:label ?" + placeNameENPH + " . " +
                "                           ?territory rdfs:label ?" + areaCodePH + " . " +
                "                           } . " + //English translation
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                        WikiBaseEndpointConnector.ENGLISH + "' }" + //for area code
                "    BIND (wd:%s as ?" + placeNamePH + ") . " + //binding the ID of entity as ?city
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
            String placeID = SPARQLDocumentParserHelper.findValueByNodeName(head, placeNamePH);
            placeID = QGUtils.stripWikidataID(placeID);
            String placeNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, placeNameENPH);
            String placeNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, placeNameForeignPH);
            String areaCode = SPARQLDocumentParserHelper.findValueByNodeName(head, areaCodePH);

            QueryResult qr = new QueryResult(placeID, placeNameEN, placeNameForeign, areaCode);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.placeNameForeign);
        }
    }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();

            QuestionData fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            QuestionData fillInBlankInputQuestion2 = createFillInBlankInputQuestion2(qr);
            questionSet.add(fillInBlankInputQuestion2);

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.placeID));
        }

    }

    private String The_JIS_area_code_of_PLACE_is_NUMBER_correct_EN(QueryResult qr){
        String areaCodeString = qr.areaCode;
        String areaCodeWords = "";
        for (char c : areaCodeString.toCharArray()){
            int num = Integer.parseInt(String.valueOf(c));
            String numberWord = QGUtils.convertIntToWord(num);
            areaCodeWords += numberWord + " ";
        }
        areaCodeWords = areaCodeWords.substring(0, areaCodeWords.length()-1);
        String sentence = "The JIS area code of " + qr.placeNameEN + " is " + areaCodeWords + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceForeign(QueryResult qr){
        return qr.placeNameForeign + "のJIS地名コードは" + qr.areaCode + "です。";
    }

    private String fillInBlankInputQuestion(QueryResult qr){
        String sentence1 = The_JIS_area_code_of_PLACE_is_NUMBER_correct_EN(qr) + "\n";
        String sentence2 = qr.placeNameForeign + "のJIS地名コードは" + QuestionUtils.FILL_IN_BLANK_NUMBER +
                "です。";
        return sentence1 + sentence2;
    }

    private String fillInBlankInputAnswer(QueryResult qr){
        return qr.areaCode;
    }

    private QuestionData createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);
        String answer = fillInBlankInputAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.placeNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }

    private String fillInBlankInputQuestion2(QueryResult qr){
        String areaCodeWithoutCheckDigit = qr.areaCode.length() > 5 ? qr.areaCode.substring(0,5) : qr.areaCode ;
        String areaCodeWithoutCheckDigitWords = "";
        for (char c : areaCodeWithoutCheckDigit.toCharArray()){
            int num = Integer.parseInt(String.valueOf(c));
            String numWord = QGUtils.convertIntToWord(num);
            areaCodeWithoutCheckDigitWords += numWord + " ";
        }
        areaCodeWithoutCheckDigitWords =areaCodeWithoutCheckDigitWords.substring(0, areaCodeWithoutCheckDigitWords.length()-1);
        String sentence1 ="The area code of " + qr.placeNameEN + " without the check digit is " + areaCodeWithoutCheckDigitWords + ".\n";
        String sentence2 = "The check digit of the JIS area code of " + qr.placeNameEN + " is " + QuestionUtils.FILL_IN_BLANK_NUMBER + ".";
        return sentence1 + sentence2;
    }

    private String fillInBlankInputAnswer2(QueryResult qr){
        if (qr.areaCode.length() == 6){
            return qr.areaCode.substring(qr.areaCode.length()-1);
        } else {
            Integer result = 0;
            int multiplyBy = 6;
            for (char c : qr.areaCode.toCharArray()){
                int num = Integer.parseInt(String.valueOf(c));
                result += multiplyBy * num;
                multiplyBy--;
            }
            result = result % 11;
            result = 11 - result;

            return result.toString();
        }
    }

    private QuestionData createFillInBlankInputQuestion2(QueryResult qr){
        String question = this.fillInBlankInputQuestion2(qr);
        String answer = fillInBlankInputAnswer2(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.placeNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }


}
