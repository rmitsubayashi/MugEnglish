package pelicann.linnca.com.corefunctionality.lessonquestions.questions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionGenerator;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionResponseChecker;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;

public class Questions_work_fired extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(translation());
        questions.add(multipleChoice(data));
        questions.add(multipleChoice2(data));
        questions.add(spelling());
        questions.add(instruction());
        return questions;
    }

    private QuestionData multipleChoice(EntityPropertyData data) {
        String question = "What happened to " + data.getPropertyAt(0).getEnglish() + "?";
        String answer = data.getPropertyAt(3).getEnglish() + " got fired.";
        answer = GrammarRules.uppercaseFirstLetterOfSentence(answer);
        List<String> choices =new ArrayList<>(3);
        choices.add(answer);
        String choice1 = data.getPropertyAt(3).getEnglish() + " got a new job.";
        choice1 = GrammarRules.uppercaseFirstLetterOfSentence(choice1);
        choices.add(choice1);
        String choice2 = data.getPropertyAt(3).getEnglish() + " is working.";
        choice2 = GrammarRules.uppercaseFirstLetterOfSentence(choice2);
        choices.add(choice2);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);

        return questionData;
    }

    private QuestionData multipleChoice2(EntityPropertyData data){
        String question = "Where did " + data.getPropertyAt(0).getEnglish() + " work?";
        String answer = data.getPropertyAt(1).getEnglish();
        List<String> choices = getOtherCompanies(data.getPropertyAt(1).getWikidataID());
        choices.add(answer);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);

        return questionData;
    }

    private List<String> getOtherCompanies(String wikiDataIDOfAnswer){
        List<Translation> companies = new ArrayList<>();
        companies.add(new Translation("Q95","Google","Google"));
        companies.add(new Translation("Q53268","Toyota","トヨタ自動車"));
        companies.add(new Translation("Q380","Facebook Inc.","フェイスブック"));
        companies.add(new Translation("Q990804","Brother Industries","ブラザー工業"));
        companies.add(new Translation("Q53227","Sharp Corporation","シャープ"));
        companies.add(new Translation("Q8093","Nintendo","任天堂"));
        companies.add(new Translation("Q166621","Mizuho Financial Group","みずほフィナンシャルグループ"));
        companies.add(new Translation("Q193326","Goldman Sachs","ゴールドマン・サックス"));
        int companyCt = companies.size();
        for (int i=companyCt-1; i<=0; i--){
            Translation company = companies.get(i);
            if (company.getWikidataID().equals(wikiDataIDOfAnswer)){
                companies.remove(i);
                break;
            }
        }

        List<String> choices = new ArrayList<>(3);
        Collections.shuffle(companies);
        for (int i=0; i<3; i++){
            choices.add(companies.get(i).getEnglish());
        }

        return choices;
    }

    private QuestionData translation() {
        String question = "見る";
        String answer = "look";
        String acceptableAnswer = "see";
        String acceptableAnswer2 = "watch";
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addAcceptableAnswer(acceptableAnswer);
        questionData.addAcceptableAnswer(acceptableAnswer2);
        questionData.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);

        return questionData;
    }

    private QuestionData spelling(){
        String question = "首になる";
        String answer = "fired";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.SPELLING);

        return questionData;
    }

    private QuestionData instruction() {
        String question = "Where do you work?";
        String answer = QuestionResponseChecker.ANYTHING;

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);

        return questionData;
    }

}
