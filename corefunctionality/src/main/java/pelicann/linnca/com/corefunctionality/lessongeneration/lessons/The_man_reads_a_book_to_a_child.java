package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.questions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

public class The_man_reads_a_book_to_a_child extends Lesson {
    public static final String KEY = "The_man_reads_a_book_to_a_child";

    public The_man_reads_a_book_to_a_child(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;
    }
    @Override
    protected synchronized int getQueryResultCt(){return 0;}
    @Override
    protected String getSPARQLQuery(){
        return "";
    }
    @Override
    protected synchronized void createQuestionsFromResults(){}
    @Override
    protected void processResultsIntoClassWrappers(Document document){}

    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<List<QuestionData>> questionSet = new ArrayList<>(6);
        List<QuestionData> translateQuestion1 = translateQuestion1();
        questionSet.add(translateQuestion1);
        List<QuestionData> translateQuestion2 = translateQuestion2();
        questionSet.add(translateQuestion2);
        List<QuestionData> spellingQuestion = spellingQuestion();
        questionSet.add(spellingQuestion);
        List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion();
        questionSet.add(sentencePuzzleQuestion);
        List<QuestionData> fillInBlankInputQuestion = createFillInBlankInputQuestion();
        questionSet.add(fillInBlankInputQuestion);
        List<QuestionData> fillInBlankMultipleChoiceQuestion = createFillInBlankMultipleChoiceQuestion();
        questionSet.add(fillInBlankMultipleChoiceQuestion);

        return questionSet;

    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> translate = preGenericQuestions.subList(0,2);
        Collections.shuffle(translate);
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(new VocabularyWord("", "read","読む",
                "The man reads a book to a child.","その男は子供に本を読みます。", KEY));
        words.add(new VocabularyWord("", "child","子供",
                "The man reads a book to a child.","その男は子供に本を読みます。", KEY));
        return words;
    }

    private List<QuestionData> translateQuestion1(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
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

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
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

        data.setQuestionType(QuestionTypeMappings.SPELLING);
        data.setQuestion("本");
        data.setChoices(null);
        data.setAnswer("book");
        data.setAcceptableAnswers(null);

        dataList.add(data);

        return dataList;
    }

    private String formatSentenceJP(){
        return "その男は子供に本を読みます。";
    }

    private String formatSentenceJP2(){
        return "その女は子供に本を読みます。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(){
        List<String> pieces = new ArrayList<>();
        pieces.add("the man");
        pieces.add("reads");
        pieces.add("a book");
        pieces.add("to");
        pieces.add("a child");
        return pieces;
    }

    private String puzzlePiecesAnswer(){
        return QuestionSerializer.serializeSentencePuzzleAnswer(puzzlePieces());
    }

    private List<String> puzzlePieces2(){
        List<String> pieces = new ArrayList<>();
        pieces.add("the woman");
        pieces.add("reads");
        pieces.add("a book");
        pieces.add("to");
        pieces.add("a child");
        return pieces;
    }

    private String puzzlePiecesAnswer2(){
        return QuestionSerializer.serializeSentencePuzzleAnswer(puzzlePieces2());
    }

    private List<QuestionData> createSentencePuzzleQuestion(){
        List<QuestionData> dataList = new ArrayList<>();
        //a man question and a woman question
        String question = this.formatSentenceJP();
        List<String> choices = this.puzzlePieces();
        String answer = puzzlePiecesAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.SENTENCEPUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        dataList.add(data);

        question = this.formatSentenceJP2();
        choices = this.puzzlePieces2();
        answer = puzzlePiecesAnswer2();
        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.SENTENCEPUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankInputQuestion(){
        String sentence1 = formatSentenceJP();
        String sentence2 = "The man " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT + " a book to a child.";
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankInputQuestion2(){
        String sentence1 = formatSentenceJP2();
        String sentence2 = "The woman " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT + " a book to a child.";
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
        List<QuestionData> dataList = new ArrayList<>();
        //one man and one woman question
        String question = this.fillInBlankInputQuestion();
        String answer = fillInBlankInputAnswer();
        List<String> acceptableAnswers = fillInBlankInputAcceptableAnswers();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        dataList.add(data);

        question = this.fillInBlankInputQuestion2();
        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankMultipleChoiceQuestion1(){
        String sentence1 = formatSentenceJP();
        String sentence2 =  "The " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " reads a " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " to a " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        return sentence1 + "\n\n" + sentence2;
    }

    private List<String> fillInBlankMultipleChoiceChoices1(){
        List<String> choices = new ArrayList<>(4);
        choices.add("   man : book  : child ");
        choices.add(" child : book  : man ");
        choices.add("  book : child : man ");
        choices.add(" child :  man  : book ");
        return choices;
    }

    private String fillInBlankMultipleChoiceAnswers1(){
        return "   man : book  : child ";
    }

    private String fillInBlankMultipleChoiceQuestion2(){
        String sentence1 = formatSentenceJP2();
        String sentence2 =  "The " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " reads a " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " to a " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        return sentence1 + "\n\n" + sentence2;
    }

    private List<String> fillInBlankMultipleChoiceChoices2(){
        List<String> choices = new ArrayList<>(4);
        choices.add(" woman : book  : child ");
        choices.add(" child : book  : woman ");
        choices.add("  book : child : woman ");
        choices.add(" child : woman : book ");
        return choices;
    }

    private String fillInBlankMultipleChoiceAnswers2(){
        return " woman : book  : child ";
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(){
        List<QuestionData> dataList = new ArrayList<>();
        //one man and one woman question
        String question = this.fillInBlankMultipleChoiceQuestion1();
        String answer = fillInBlankMultipleChoiceAnswers1();
        List<String> choices = fillInBlankMultipleChoiceChoices1();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        dataList.add(data);

        question = this.fillInBlankMultipleChoiceQuestion2();
        answer = fillInBlankMultipleChoiceAnswers2();
        choices = fillInBlankMultipleChoiceChoices2();
        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        dataList.add(data);

        return dataList;
    }
}
