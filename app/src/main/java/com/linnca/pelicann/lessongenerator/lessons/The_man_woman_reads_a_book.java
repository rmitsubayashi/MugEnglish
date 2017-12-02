package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_SentencePuzzle;
import com.linnca.pelicann.questions.Question_Spelling;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class The_man_woman_reads_a_book extends Lesson {
    public static final String KEY = "The_man_woman_reads_a_book";

    public The_man_woman_reads_a_book(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
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
            data.setId(formatGenericQuestionID(KEY, i+1));
        }

        return questions;

    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<List<String>> questionSet = new ArrayList<>();
        for (int i=1; i<=5; i++) {
            List<String> questions = new ArrayList<>();
            questions.add(formatGenericQuestionID(KEY, i));
            questionSet.add(questions);
        }

        return questionSet;
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(new VocabularyWord(formatGenericQuestionVocabularyID(lessonKey, "read"),
                "read","読む","The man reads a book.","男は本を読みます。", KEY));
        return words;
    }

    @Override
    protected List<String> getGenericQuestionVocabularyIDs(){
        List<String> ids =new ArrayList<>(1);
        ids.add(formatGenericQuestionVocabularyID(lessonKey, "read"));
        return ids;
    }

    private List<QuestionData> translateQuestion1(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
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
        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
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
        data.setQuestionType(Question_Spelling.QUESTION_TYPE);
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
        return Question_SentencePuzzle.formatAnswer(puzzlePieces());
    }

    private List<QuestionData> createSentencePuzzleQuestion(){
        String question = this.formatSentenceJP();
        List<String> choices = this.puzzlePieces();
        String answer = puzzlePiecesAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
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
        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }
}
