package com.linnca.pelicann.lessonlist;

import com.linnca.pelicann.R;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.lessongenerator.lessons.Hello_my_name_is_NAME;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_a_GENDER;
import com.linnca.pelicann.lessongenerator.lessons.good_morning_afternoon_evening;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LessonHierarchyViewer {
    private final List<List<LessonListRow>> lessonLevels = new ArrayList<>();
    private Map<String, Integer> titleCount;
    private final String review = "ふくしゅう";

    public LessonHierarchyViewer(){
        populateLessons();
    }

    private void populateLessons(){
        final String greetings = "あいさつ";
        final int greetingsIconID = R.drawable.ic_hand;
        final String people = "にんげん";
        final int peopleIconID = R.drawable.ic_person;
        final String numbers = "すうじ";
        final int numbersIconID = R.drawable.ic_dialpad;
        final String places = "ばしょ";
        final int placesIconID = R.drawable.ic_places;
        final String occupations = "しょくぎょう";
        final String body = "からだ";
        final int bodyIconID = R.drawable.ic_body;
        final String action = "どうさ";
        final int actionIConID = R.drawable.ic_action;

        List<LessonListRow> lessonRows;
        LessonListRow row;
        LessonData col1Data;
        List<String> col1Prerequisites;
        LessonData col2Data;
        List<String> col2Prerequisites;
        LessonData col3Data;
        List<String> col3Prerequisites;

        lessonRows = new ArrayList<>(50);
        titleCount = new HashMap<>();

        row = new LessonListRow();
        col1Data = new LessonData(Hello_my_name_is_NAME.KEY, greetings, R.layout.fragment_description_hello_my_name_is_name, null, R.color.lblue300, greetingsIconID);
        col2Data = new LessonData(NAME_is_a_GENDER.KEY, people, R.layout.fragment_description_name_is_a_man, null, R.color.lblue700, peopleIconID);
        col3Data = new LessonData("people2_key", people, null, null, R.color.lblue700, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(Hello_my_name_is_NAME.KEY);
        col1Data = new LessonData(good_morning_afternoon_evening.KEY, greetings, null, col1Prerequisites, R.color.lblue300, greetingsIconID);
        col2Prerequisites = new ArrayList<>(2);
        col2Prerequisites.add(NAME_is_a_GENDER.KEY);
        col2Prerequisites.add("people2_key");
        col2Data = new LessonData("people3_key", people, null, col2Prerequisites, R.color.lblue700, peopleIconID);
        col3Data = new LessonData("numbers1_key", numbers, null, null, R.color.lblue500, numbersIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(good_morning_afternoon_evening.KEY);
        col1Data = new LessonData("greetings3_key", greetings, null, col1Prerequisites, R.color.lblue300, greetingsIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add("numbers1_key");
        col3Data = new LessonData("numbers2_key", numbers, null, col3Prerequisites, R.color.lblue500, numbersIconID);
        row.setCol1(col1Data);
        row.setCol2(null);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add("numbers2_key");
        col3Data = new LessonData("numbers3_key", numbers, null, col3Prerequisites, R.color.lblue500, numbersIconID);
        row.setCol1(null);
        row.setCol2(null);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        row.setReview(true);
        col2Prerequisites = new ArrayList<>(3);
        col2Prerequisites.add("numbers3_key");
        col2Prerequisites.add("greetings3_key");
        col2Prerequisites.add("people3_key");
        col2Data = new LessonData("id_review1", review, null, col2Prerequisites, 0, 0);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add("greetings3_key");
        col1Data = new LessonData("greetings4_key", greetings, null, col1Prerequisites, R.color.lblue300, greetingsIconID);
        col2Data = new LessonData("places1_key", places, null, null, R.color.lblue700, placesIconID);
        col3Data = new LessonData("occupations1_key", occupations, null, null, R.color.lblue500, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add("greetings4_key");
        col1Data = new LessonData("greetings5_key", greetings, null, col1Prerequisites, R.color.lblue300, greetingsIconID);
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add("places1_key");
        col2Data = new LessonData("places2_key", places, null, col2Prerequisites, R.color.lblue700, placesIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add("occupations1_key");
        col3Data = new LessonData("occupations2_key", occupations, null, col3Prerequisites, R.color.lblue500, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add("places2_key");
        col2Data = new LessonData("places3_key", places, null, col2Prerequisites, R.color.lblue700, placesIconID);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        row.setReview(true);
        col2Prerequisites = new ArrayList<>(3);
        col2Prerequisites.add("greetings5_key");
        col2Prerequisites.add("occupations2_key");
        col2Prerequisites.add("places3_key");
        col2Data = new LessonData("id_review2", review, null, col2Prerequisites, 0, 0);
        col2Data.setPrerequisiteLeeway(1);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add("occupations3_key");
        col1Data = new LessonData("occupations4_key", occupations, null, col1Prerequisites, R.color.lblue500, peopleIconID);
        col2Data = new LessonData("occupations3_key", occupations, null, null, R.color.lblue500, peopleIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add("occupations3_key");
        col3Data = new LessonData("occupations5_key", occupations, null, col3Prerequisites, R.color.lblue500, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add("numbers4_key");
        col1Data = new LessonData("numbers5_key", numbers, null, col1Prerequisites, R.color.lblue300, numbersIconID);
        col2Prerequisites = new ArrayList<>(2);
        col1Prerequisites.add("numbers3_key");
        col2Data = new LessonData("numbers4_key", numbers, null, col2Prerequisites, R.color.lblue300, numbersIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add("numbers4_key");
        col3Data = new LessonData("numbers6_key", numbers, null, col3Prerequisites, R.color.lblue300, numbersIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Data = new LessonData("body1_key", body, null, null, R.color.lblue700, bodyIconID);
        col3Data = new LessonData("action1_key", action, null, null, R.color.lblue700, actionIConID);
        row.setCol1(col1Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add("body1_key");
        col1Data = new LessonData("body2_key", body, null, col1Prerequisites, R.color.lblue700, bodyIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add("action1_key");
        col3Data = new LessonData("action2_key", action, null, col3Prerequisites, R.color.lblue700, actionIConID);
        row.setCol1(col1Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        adjustRowTitles(lessonRows);
        lessonLevels.add(lessonRows);

        titleCount.clear();

        /*
        new LessonData(NAME_is_a_OCCUPATION.KEY, "職業", R.layout.fragment_description_name_is_a_occupation));
        //workCategory.addLesson(new LessonData(NAME_plays_SPORT.KEY, "スポーツ選手", null));

        countriesAndPrefecturesCategory.addLesson(new LessonData(The_DEMONYM_flag_is_COLORS.KEY, "国旗の色", null));
        countriesAndPrefecturesCategory.addLesson(new LessonData(NAME_is_DEMONYM.KEY, "国民の名称", null));
        countriesAndPrefecturesCategory.addLesson(new LessonData(The_emergency_phone_number_of_COUNTRY_is_NUMBER.KEY, "緊急通報用電話番号", R.layout.fragment_description_the_emergency_phone_number_of_country_is_number));
        */
    }

    private void adjustRowTitles(List<LessonListRow> rows){
        for (LessonListRow row : rows){
            LessonData[] lessons = row.getLessons();
            //order is center -> left -> right
            if (lessons[1] != null){
                lessons[1].setTitle(formatTitle(lessons[1].getTitle()));
            }
            if (lessons[0] != null){
                lessons[0].setTitle(formatTitle(lessons[0].getTitle()));
            }
            if (lessons[2] != null){
                lessons[2].setTitle(formatTitle(lessons[2].getTitle()));
            }
        }
    }

    private String formatTitle(String title){
        //don't enumerate review titles
        if (title.equals(review)){
            return title;
        }
        if (titleCount.containsKey(title)){
            int newCt = titleCount.get(title) + 1;
            titleCount.put(title, newCt);
            return title + Integer.toString(newCt);
        } else {
            titleCount.put(title, 1);
            return title + "1";
        }
    }

    public List<LessonListRow> getLessonsAtLevel(int level){
        level = level - 1;
        return lessonLevels.get(level);
    }

    public LessonData getLessonData(String lessonKey){
        for (List<LessonListRow> lessonRows : lessonLevels) {
            for (LessonListRow row : lessonRows) {
                for (LessonData lessonData : row.getLessons()) {
                    if (lessonData.getKey().equals(lessonKey)) {
                        return lessonData;
                    }
                }
            }
        }
        return null;
    }

    private int getLessonLevel(String lessonKey){
        int levelCt = lessonLevels.size();
        for (int i=0; i<levelCt; i++) {
            List<LessonListRow> lessonRows = lessonLevels.get(i);
            for (LessonListRow row : lessonRows) {
                for (LessonData lessonData : row.getLessons()) {
                    if (lessonData.getKey().equals(lessonKey)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public boolean layoutExists(String lessonKey){
        for (List<LessonListRow> lessonRows : lessonLevels) {
            for (LessonListRow row : lessonRows) {
                for (LessonData lessonData : row.getLessons()) {
                    if (lessonData.getKey().equals(lessonKey)) {
                        return lessonData.getDescriptionLayout() != null;
                    }
                }
            }
        }

        return false;
    }

    public List<LessonData> getLessonsUnlockedByClearing(String lessonKey, List<String> allClearedKeys){
        List<LessonData> unlockedLessons = new ArrayList<>(5);
        //if the lesson is already cleared,
        //clearing that lesson shouldn't unlock anything
        if (allClearedKeys.contains(lessonKey)){
            return unlockedLessons;
        }
        for (List<LessonListRow> lessonRows : lessonLevels) {
            for (LessonListRow row : lessonRows) {
                for (LessonData lessonData : row.getLessons()) {
                    List<String> prerequisites = lessonData.getPrerequisiteKeys();
                    if (prerequisites.contains(lessonKey)) {
                        boolean unlocked = true;
                        for (String prerequisite : prerequisites){
                            if (prerequisite.equals(lessonKey)){
                                continue;
                            }
                            if (!allClearedKeys.contains(prerequisite)){
                                unlocked = false;
                                break;
                            }
                        }
                        if (unlocked){
                            unlockedLessons.add(new LessonData(lessonData));
                        }
                    }
                }
            }
        }

        return unlockedLessons;
    }
}
