package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.ChatQuestionItem;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class The_man_woman_reads_a_book extends Lesson {
    public static final String KEY = "The_man_woman_reads_a_book";

    public The_man_woman_reads_a_book(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.lessonKey = KEY;
    }
    @Override
    protected int getQueryResultCt(){return 0;}
    @Override
    protected String getSPARQLQuery(){
        return "";
    }
    @Override
    protected void createQuestionsFromResults(){}
    @Override
    protected void processResultsIntoClassWrappers(Document document){}

    @Override
    protected List<QuestionData> getGenericQuestions(){
        List<QuestionData> questions = new ArrayList<>(5);
        List<QuestionData> translateQuestion1 = translateQuestion1();
        questions.addAll(translateQuestion1);
        List<QuestionData> translateQuestion2 = translateQuestion2();
        questions.addAll(translateQuestion2);
        List<QuestionData> spellingQuestion = spellingQuestion();
        questions.addAll(spellingQuestion);
        List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion();
        questions.addAll(sentencePuzzleQuestion);
        List<QuestionData> fillInBlankInputQuestion = createFillInBlankInputQuestion();
        questions.addAll(fillInBlankInputQuestion);
        int questionCt = questions.size();
        for (int i=0; i<questionCt; i++){
            QuestionData data = questions.get(i);
            data.setId(LessonGeneratorUtils.formatGenericQuestionID(KEY, i+1));
        }

        return questions;

    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<List<String>> questionSet = new ArrayList<>();
        for (int i=1; i<=5; i++) {
            List<String> questions = new ArrayList<>();
            questions.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, i));
            questionSet.add(questions);
        }

        return questionSet;
    }

    private List<QuestionData> translateQuestion1(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion("女");
        data.setChoices(null);
        data.setAnswer("woman");
        data.setAcceptableAnswers(null);

        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> translateQuestion2(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion("男");
        data.setChoices(null);
        data.setAnswer("man");
        data.setAcceptableAnswers(null);

        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> spellingQuestion(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.SPELLING);
        data.setQuestion("本");
        data.setChoices(null);
        data.setAnswer("book");
        data.setAcceptableAnswers(null);

        dataList.add(data);

        return dataList;
    }

    private String formatSentenceJP(){
        return "男は本を読みます。";
    }

    private String formatSentenceJP2(){
        return "女は本を読みます。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(){
        List<String> pieces = new ArrayList<>();
        pieces.add("The man");
        pieces.add("reads");
        pieces.add("a book");
        return pieces;
    }

    private String puzzlePiecesAnswer(){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces());
    }

    private List<QuestionData> createSentencePuzzleQuestion(){
        String question = this.formatSentenceJP();
        List<String> choices = this.puzzlePieces();
        String answer = puzzlePiecesAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private String fillInBlankInputQuestion(){
        String sentence1 = formatSentenceJP2();
        String sentence2 = "The woman " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + " a book.";
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankInputAnswer(){
        return "reads";
    }

    private List<String> fillInBlankInputAcceptableAnswers(){
        List<String> answers = new ArrayList<>(1);
        answers.add("read");
        return answers;
    }

    private List<QuestionData> createFillInBlankInputQuestion(){
        String question = this.fillInBlankInputQuestion();
        String answer = fillInBlankInputAnswer();
        List<String> acceptableAnswers = fillInBlankInputAcceptableAnswers();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }
}
