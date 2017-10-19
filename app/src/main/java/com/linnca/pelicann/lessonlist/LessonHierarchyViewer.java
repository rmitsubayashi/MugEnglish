package com.linnca.pelicann.lessonlist;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.lessongenerator.lessons.COMPANY_makes_PRODUCT;
import com.linnca.pelicann.lessongenerator.lessons.Goodbye_bye;
import com.linnca.pelicann.lessongenerator.lessons.Hello_my_name_is_NAME;
import com.linnca.pelicann.lessongenerator.lessons.Hello_my_name_is_NAME_I_am_a_OCCUPATION;
import com.linnca.pelicann.lessongenerator.lessons.How_are_you_doing;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_AGE_years_old;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_DEMONYM;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_a_GENDER;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_a_OCCUPATION;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_at_work_He_is_at_EMPLOYER;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_from_CITY;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_from_COUNTRY;
import com.linnca.pelicann.lessongenerator.lessons.NAME_plays_SPORT;
import com.linnca.pelicann.lessongenerator.lessons.NAME_works_at_EMPLOYER;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_0_3;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_10s;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_11_19;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_21_99;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_4_6;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_7_9;
import com.linnca.pelicann.lessongenerator.lessons.PLACE_is_a_COUNTRY_CITY;
import com.linnca.pelicann.lessongenerator.lessons.Stand_up_sit_down;
import com.linnca.pelicann.lessongenerator.lessons.Walk_run;
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
        final int buildIconID = R.drawable.ic_build;
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
        col3Data = new LessonData(NAME_is_AGE_years_old.KEY, people, null, null, R.color.lblue700, peopleIconID);
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
        col2Prerequisites.add(NAME_is_AGE_years_old.KEY);
        col2Data = new LessonData("people3_key", people, null, col2Prerequisites, R.color.lblue700, peopleIconID);
        col3Data = new LessonData(Numbers_0_3.KEY, numbers, null, null, R.color.lblue500, numbersIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(good_morning_afternoon_evening.KEY);
        col1Data = new LessonData(Goodbye_bye.KEY, greetings, null, col1Prerequisites, R.color.lblue300, greetingsIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(Numbers_0_3.KEY);
        col3Data = new LessonData(Numbers_4_6.KEY, numbers, null, col3Prerequisites, R.color.lblue500, numbersIconID);
        row.setCol1(col1Data);
        row.setCol2(null);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(Numbers_4_6.KEY);
        col3Data = new LessonData(Numbers_7_9.KEY, numbers, null, col3Prerequisites, R.color.lblue500, numbersIconID);
        row.setCol1(null);
        row.setCol2(null);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        row.setReview(true);
        col2Prerequisites = new ArrayList<>(3);
        col2Prerequisites.add(Numbers_7_9.KEY);
        col2Prerequisites.add(Goodbye_bye.KEY);
        col2Prerequisites.add("people3_key");
        col2Data = new LessonData("id_review1", review, null, col2Prerequisites, 0, 0);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(Goodbye_bye.KEY);
        col1Data = new LessonData(How_are_you_doing.KEY, greetings, null, col1Prerequisites, R.color.lblue300, greetingsIconID);
        col2Data = new LessonData(PLACE_is_a_COUNTRY_CITY.KEY, places, null, null, R.color.lblue700, placesIconID);
        col3Data = new LessonData(NAME_is_a_OCCUPATION.KEY, occupations, null, null, R.color.lblue500, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(How_are_you_doing.KEY);
        col1Data = new LessonData("greetings5_key", greetings, null, col1Prerequisites, R.color.lblue300, greetingsIconID);
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(PLACE_is_a_COUNTRY_CITY.KEY);
        col2Data = new LessonData(NAME_is_from_COUNTRY.KEY, places, null, col2Prerequisites, R.color.lblue700, placesIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(NAME_is_a_OCCUPATION.KEY);
        col3Data = new LessonData(Hello_my_name_is_NAME_I_am_a_OCCUPATION.KEY, occupations, null, col3Prerequisites, R.color.lblue500, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(NAME_is_from_COUNTRY.KEY);
        col2Data = new LessonData(NAME_is_from_CITY.KEY, places, null, col2Prerequisites, R.color.lblue700, placesIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(NAME_is_from_COUNTRY.KEY);
        col3Data = new LessonData(NAME_is_DEMONYM.KEY, people, null, col3Prerequisites, R.color.lblue700, peopleIconID);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        row.setReview(true);
        col2Prerequisites = new ArrayList<>(3);
        col2Prerequisites.add("greetings5_key");
        col2Prerequisites.add(Hello_my_name_is_NAME_I_am_a_OCCUPATION.KEY);
        col2Prerequisites.add(NAME_is_from_CITY.KEY);
        col2Data = new LessonData("id_review2", review, null, col2Prerequisites, 0, 0);
        col2Data.setPrerequisiteLeeway(1);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(NAME_plays_SPORT.KEY);
        col1Data = new LessonData(COMPANY_makes_PRODUCT.KEY, occupations, null, col1Prerequisites, R.color.lblue500, buildIconID);
        col2Data = new LessonData(NAME_plays_SPORT.KEY, occupations, null, null, R.color.lblue500, peopleIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(NAME_plays_SPORT.KEY);
        col3Data = new LessonData(NAME_works_at_EMPLOYER.KEY, occupations, null, col3Prerequisites, R.color.lblue500, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(Numbers_10s.KEY);
        col1Data = new LessonData(Numbers_11_19.KEY, numbers, null, col1Prerequisites, R.color.lblue300, numbersIconID);
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(Numbers_7_9.KEY);
        col2Data = new LessonData(Numbers_10s.KEY, numbers, null, col2Prerequisites, R.color.lblue300, numbersIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(NAME_works_at_EMPLOYER.KEY);
        col3Data = new LessonData(NAME_is_at_work_He_is_at_EMPLOYER.KEY, occupations, null, col3Prerequisites, R.color.lblue500, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Data = new LessonData("body1_key", body, null, null, R.color.lblue700, bodyIconID);
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(Numbers_10s.KEY);
        col2Data = new LessonData(Numbers_21_99.KEY, numbers, null, col2Prerequisites, R.color.lblue300, numbersIconID);
        col3Data = new LessonData(Stand_up_sit_down.KEY, action, null, null, R.color.lblue700, actionIConID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add("body1_key");
        col1Data = new LessonData("body2_key", body, null, col1Prerequisites, R.color.lblue700, bodyIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(Stand_up_sit_down.KEY);
        col3Data = new LessonData(Walk_run.KEY, action, null, col3Prerequisites, R.color.lblue700, actionIConID);
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

    public void debugUnlockAllLessons(){
        for (List<LessonListRow> lessonRows : lessonLevels) {
            for (LessonListRow row : lessonRows) {
                for (LessonData lessonData : row.getLessons()) {
                    if (lessonData == null)
                        continue;
                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                            FirebaseDBHeaders.CLEARED_LESSONS + "/" +
                                    FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" +
                                    lessonData.getKey()
                    );
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()){
                                ref.setValue(true);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }
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
                    if (lessonData == null)
                        continue;
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
                    if (lessonData == null)
                        continue;
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
                    if (lessonData == null)
                        continue;
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
                    if (lessonData == null)
                        continue;
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
