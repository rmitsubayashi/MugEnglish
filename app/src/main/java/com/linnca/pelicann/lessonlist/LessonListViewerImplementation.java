package com.linnca.pelicann.lessonlist;

import com.linnca.pelicann.R;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.lessongenerator.lessons.COMPANY_makes_PRODUCT;
import com.linnca.pelicann.lessongenerator.lessons.COUNTRY_drives_on_the_left_right;
import com.linnca.pelicann.lessongenerator.lessons.COUNTRY_possessive_area_is_AREA;
import com.linnca.pelicann.lessongenerator.lessons.COUNTRY_possessive_government_is_a_GOVERNMENT;
import com.linnca.pelicann.lessongenerator.lessons.COUNTRY_possessive_population_is_POPULATION;
import com.linnca.pelicann.lessongenerator.lessons.Find_stand_sit_on_a_bed;
import com.linnca.pelicann.lessongenerator.lessons.Good_morning_afternoon_evening;
import com.linnca.pelicann.lessongenerator.lessons.Goodbye_bye;
import com.linnca.pelicann.lessongenerator.lessons.Hello_my_name_is_NAME;
import com.linnca.pelicann.lessongenerator.lessons.Hello_my_name_is_NAME_I_am_from_CITY;
import com.linnca.pelicann.lessongenerator.lessons.Hi_hey_whats_up;
import com.linnca.pelicann.lessongenerator.lessons.How_are_you_doing;
import com.linnca.pelicann.lessongenerator.lessons.NAME_drove_from_CITY_to_CITY2;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_young_He_is_AGE;
import com.linnca.pelicann.lessongenerator.lessons.NAME_spoke_at_TED_he_spoke_about_SUBJECT;
import com.linnca.pelicann.lessongenerator.lessons.NAME_wrote_a_book_about_SUBJECT;
import com.linnca.pelicann.lessongenerator.lessons.Thanks_no_problem_youre_welcome;
import com.linnca.pelicann.lessongenerator.lessons.The_man_reads_a_book_to_a_child;
import com.linnca.pelicann.lessongenerator.lessons.Walk_turn_create_PAST;
import com.linnca.pelicann.lessongenerator.lessons.NAME_creates_art;
import com.linnca.pelicann.lessongenerator.lessons.NAME_drives_a_car_to_CITY;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_AGE_years_old;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_AGE_years_old_NAME_is_a_GENDER;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_DEMONYM;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_NAME2_possessive_husband_wife;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_a_GENDER;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_a_OCCUPATION;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_at_work_He_is_at_EMPLOYER;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_from_COUNTRY;
import com.linnca.pelicann.lessongenerator.lessons.NAME_played_SPORT;
import com.linnca.pelicann.lessongenerator.lessons.NAME_plays_SPORT;
import com.linnca.pelicann.lessongenerator.lessons.NAME_plays_SPORT_It_is_a_individual_team_sport;
import com.linnca.pelicann.lessongenerator.lessons.NAME_plays_SPORT_It_is_a_water_sport;
import com.linnca.pelicann.lessongenerator.lessons.NAME_possessive_first_last_name_is_NAME;
import com.linnca.pelicann.lessongenerator.lessons.NAME_possessive_husband_wife_plays_SPORT_he_is_a_OCCUPATION;
import com.linnca.pelicann.lessongenerator.lessons.NAME_possessive_mother_father_is_NAME2;
import com.linnca.pelicann.lessongenerator.lessons.NAME_speaks_LANGUAGE;
import com.linnca.pelicann.lessongenerator.lessons.NAME_was_a_OCCUPATION;
import com.linnca.pelicann.lessongenerator.lessons.NAME_worked_for_the_government_He_was_a_politician;
import com.linnca.pelicann.lessongenerator.lessons.NAME_works_at_EMPLOYER;
import com.linnca.pelicann.lessongenerator.lessons.NAME_works_for_EMPLOYER;
import com.linnca.pelicann.lessongenerator.lessons.NAME_works_for_the_government_He_is_a_politician;
import com.linnca.pelicann.lessongenerator.lessons.NAME_writes_books;
import com.linnca.pelicann.lessongenerator.lessons.NAME_writes_songs;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_0_3;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_10s;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_11_19;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_21_99;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_4_6;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_7_9;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_hundred_billion;
import com.linnca.pelicann.lessongenerator.lessons.PLACE_is_a_country_city;
import com.linnca.pelicann.lessongenerator.lessons.Stand_up_sit_down;
import com.linnca.pelicann.lessongenerator.lessons.TEAM_is_a_SPORT_team;
import com.linnca.pelicann.lessongenerator.lessons.The_DEMONYM_flag_is_COLORS;
import com.linnca.pelicann.lessongenerator.lessons.The_emergency_phone_number_of_COUNTRY_is_NUMBER;
import com.linnca.pelicann.lessongenerator.lessons.Turn_left_right_go_straight;
import com.linnca.pelicann.lessongenerator.lessons.Walk_run;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LessonListViewerImplementation extends LessonListViewer{
    @Override
    protected void populateLessons(){
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
        final int actionIconID = R.drawable.ic_action;

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
        col1Data = new LessonData(Hello_my_name_is_NAME.KEY, greetings, R.layout.description_hello_my_name_is_name, null, 70, R.attr.color300, greetingsIconID);
        col2Data = new LessonData(NAME_is_a_GENDER.KEY, people, R.layout.description_name_is_a_gender, null, 80, R.attr.color700, peopleIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(NAME_is_a_GENDER.KEY);
        col3Data = new LessonData(NAME_is_AGE_years_old.KEY, people, R.layout.description_name_is_age_years_old, col3Prerequisites, 60, R.attr.color700, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(Hello_my_name_is_NAME.KEY);
        col1Data = new LessonData(Good_morning_afternoon_evening.KEY, greetings, R.layout.description_good_morning_afternoon_evening, col1Prerequisites, 90, R.attr.color300, greetingsIconID);
        col2Data = new LessonData(Numbers_0_3.KEY, numbers, R.layout.description_numbers_0_3, null, 50, R.attr.color500, numbersIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(NAME_is_AGE_years_old.KEY);
        col3Data = new LessonData(NAME_is_AGE_years_old_NAME_is_a_GENDER.KEY, people, R.layout.description_name_is_age_years_old_name_is_a_gender, col3Prerequisites, 70, R.attr.color700, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(Good_morning_afternoon_evening.KEY);
        col1Data = new LessonData(Goodbye_bye.KEY, greetings, R.layout.description_good_bye_bye, col1Prerequisites, 90, R.attr.color300, greetingsIconID);
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(Numbers_0_3.KEY);
        col2Data = new LessonData(Numbers_4_6.KEY, numbers, R.layout.description_numbers_4_6, col2Prerequisites, 70, R.attr.color500, numbersIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(null);
        lessonRows.add(row);

        row = new LessonListRow();
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(Numbers_4_6.KEY);
        col2Data = new LessonData(Numbers_7_9.KEY, numbers, R.layout.description_numbers_7_9, col2Prerequisites, 90, R.attr.color500, numbersIconID);
        row.setCol1(null);
        row.setCol2(col2Data);
        row.setCol3(null);
        lessonRows.add(row);

        row = new LessonListRow();
        row.setReview(true);
        col2Prerequisites = new ArrayList<>(3);
        col2Prerequisites.add(Numbers_7_9.KEY);
        col2Prerequisites.add(Goodbye_bye.KEY);
        col2Prerequisites.add(NAME_is_AGE_years_old_NAME_is_a_GENDER.KEY);
        col2Data = new LessonData(getNextReviewID(), review, null, col2Prerequisites, 70, 0, 0);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Data = new LessonData(How_are_you_doing.KEY, greetings, R.layout.description_how_are_you_doing, null, 70, R.attr.color300, greetingsIconID);
        col2Data = new LessonData(PLACE_is_a_country_city.KEY, places, R.layout.description_place_is_a_country_city, null, 90, R.attr.color700, placesIconID);
        col3Data = new LessonData(NAME_is_a_OCCUPATION.KEY, occupations, R.layout.description_name_is_a_occupation, null, 70, R.attr.color500, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(How_are_you_doing.KEY);
        col1Data = new LessonData(Hi_hey_whats_up.KEY, greetings, R.layout.description_hi_hey_whats_up, col1Prerequisites, 85, R.attr.color300, greetingsIconID);
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(PLACE_is_a_country_city.KEY);
        col2Data = new LessonData(NAME_is_from_COUNTRY.KEY, places, R.layout.description_name_is_from_country, col2Prerequisites, 60, R.attr.color700, placesIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(NAME_is_from_COUNTRY.KEY);
        col2Data = new LessonData(Hello_my_name_is_NAME_I_am_from_CITY.KEY, places, R.layout.description_name_is_from_city, col2Prerequisites, 65, R.attr.color700, placesIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(NAME_is_from_COUNTRY.KEY);
        col3Data = new LessonData(NAME_is_DEMONYM.KEY, people, R.layout.description_name_is_demonym, col3Prerequisites, 70, R.attr.color700, peopleIconID);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        row.setReview(true);
        col2Prerequisites = new ArrayList<>(4);
        col2Prerequisites.add(Hi_hey_whats_up.KEY);
        col2Prerequisites.add(NAME_is_a_OCCUPATION.KEY);
        col2Prerequisites.add(Hello_my_name_is_NAME_I_am_from_CITY.KEY);
        col2Prerequisites.add(NAME_is_DEMONYM.KEY);
        col2Data = new LessonData(getNextReviewID(), review, null, col2Prerequisites, 70, 0, 0);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Data = new LessonData(Stand_up_sit_down.KEY, action, R.layout.description_stand_up_sit_down, null, 90, R.attr.color700, actionIconID);
        col2Data = new LessonData(NAME_plays_SPORT.KEY, occupations, R.layout.description_name_plays_sports, null, 70, R.attr.color500, peopleIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(NAME_plays_SPORT.KEY);
        col3Data = new LessonData(COMPANY_makes_PRODUCT.KEY, occupations, R.layout.description_company_makes_product, col3Prerequisites, 70, R.attr.color500, placesIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(Stand_up_sit_down.KEY);
        col1Data = new LessonData(Walk_run.KEY, action, R.layout.description_walk_run, col1Prerequisites, 80, R.attr.color700, actionIconID);
        col2Data = new LessonData(Numbers_10s.KEY, numbers, R.layout.description_numbers_10s, null, 95, R.attr.color300, numbersIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(COMPANY_makes_PRODUCT.KEY);
        col3Data = new LessonData(NAME_works_at_EMPLOYER.KEY, occupations, R.layout.description_name_works_at_employer, col3Prerequisites, 70, R.attr.color500, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(Numbers_10s.KEY);
        col2Data = new LessonData(Numbers_11_19.KEY, numbers, R.layout.description_numbers_11_19, col2Prerequisites, 90, R.attr.color300, numbersIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(NAME_works_at_EMPLOYER.KEY);
        col3Data = new LessonData(NAME_is_at_work_He_is_at_EMPLOYER.KEY, occupations, R.layout.description_name_is_at_work_he_is_at_employer, col3Prerequisites, 80, R.attr.color500, peopleIconID);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(Numbers_11_19.KEY);
        col2Data = new LessonData(Numbers_21_99.KEY, numbers, R.layout.description_numbers_21_99, col2Prerequisites, 100, R.attr.color300, numbersIconID);
        col3Prerequisites = new ArrayList<>(2);
        col3Prerequisites.add(Numbers_21_99.KEY);
        col3Prerequisites.add(NAME_is_at_work_He_is_at_EMPLOYER.KEY);
        col3Data = new LessonData(NAME_is_young_He_is_AGE.KEY, people, R.layout.description_name_is_young_he_is_age, col3Prerequisites, 70, R.attr.color700, peopleIconID);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        row.setReview(true);
        col2Prerequisites = new ArrayList<>(3);
        col2Prerequisites.add(Walk_run.KEY);
        col2Prerequisites.add(Numbers_21_99.KEY);
        col2Prerequisites.add(NAME_is_young_He_is_AGE.KEY);
        col2Data = new LessonData(getNextReviewID(), review, null, col2Prerequisites, 70, 0, 0);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Data = new LessonData(NAME_possessive_mother_father_is_NAME2.KEY, people, R.layout.description_name_possessive_father_mother_is_name2, null, 85, R.attr.color700, peopleIconID);
        col2Data = new LessonData(Numbers_hundred_billion.KEY, numbers, R.layout.description_numbers_hundred_billion, null, 80, R.attr.color300, numbersIconID);
        col3Data = new LessonData(COUNTRY_possessive_government_is_a_GOVERNMENT.KEY, places, R.layout.description_country_possessive_government_is_a_government, null, 50, R.attr.color700, placesIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(NAME_possessive_mother_father_is_NAME2.KEY);
        col1Data = new LessonData(NAME_possessive_first_last_name_is_NAME.KEY, people, R.layout.description_name_possessive_first_last_name_is_name, col1Prerequisites, 70, R.attr.color700, peopleIconID);
        col3Prerequisites = new ArrayList<>(2);
        col3Prerequisites.add(COUNTRY_possessive_government_is_a_GOVERNMENT.KEY);
        col3Prerequisites.add(Numbers_hundred_billion.KEY);
        col3Data = new LessonData(COUNTRY_possessive_population_is_POPULATION.KEY, places, R.layout.description_country_possessive_population_is_population, col3Prerequisites, 80, R.attr.color700, placesIconID);
        row.setCol1(col1Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(NAME_possessive_first_last_name_is_NAME.KEY);
        col1Data = new LessonData(NAME_is_NAME2_possessive_husband_wife.KEY, people, R.layout.description_name_is_name_possessive_husband_wife, col1Prerequisites, 70, R.attr.color700, peopleIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(COUNTRY_possessive_population_is_POPULATION.KEY);
        col3Data = new LessonData(COUNTRY_possessive_area_is_AREA.KEY, places, R.layout.description_country_possessive_area_is_area, col3Prerequisites, 50, R.attr.color700, placesIconID);
        row.setCol1(col1Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        row.setReview(true);
        col2Prerequisites = new ArrayList<>(2);
        col2Prerequisites.add(NAME_is_NAME2_possessive_husband_wife.KEY);
        col2Prerequisites.add(COUNTRY_possessive_area_is_AREA.KEY);
        col2Data = new LessonData(getNextReviewID(), review, null, col2Prerequisites, 70, 0, 0);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Data = new LessonData(NAME_writes_books.KEY, occupations, R.layout.description_name_writes_books, null, 75, R.attr.color300, peopleIconID);
        col2Data = new LessonData(NAME_plays_SPORT_It_is_a_water_sport.KEY, occupations, R.layout.description_name_plays_sport_it_is_a_water_sport, null, 60, R.attr.color300, peopleIconID);
        col3Data = new LessonData(NAME_works_for_the_government_He_is_a_politician.KEY, occupations, R.layout.description_name_works_for_the_government_he_is_a_politician, null, 60, R.attr.color300, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(NAME_writes_books.KEY);
        col1Data = new LessonData(NAME_writes_songs.KEY, occupations, R.layout.description_name_writes_songs, col1Prerequisites, 80, R.attr.color300, peopleIconID);
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(NAME_plays_SPORT_It_is_a_water_sport.KEY);
        col2Data = new LessonData(NAME_plays_SPORT_It_is_a_individual_team_sport.KEY, occupations, R.layout.description_name_plays_sport_it_is_a_individual_sport, col2Prerequisites, 80, R.attr.color300, peopleIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(NAME_works_for_the_government_He_is_a_politician.KEY);
        col3Data = new LessonData(NAME_works_for_EMPLOYER.KEY, occupations, R.layout.description_name_works_for_employer, col3Prerequisites, 70, R.attr.color300, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(NAME_writes_songs.KEY);
        col1Data = new LessonData(NAME_creates_art.KEY, occupations, R.layout.description_name_creates_art, col1Prerequisites, 70, R.attr.color300, peopleIconID);
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(NAME_plays_SPORT_It_is_a_individual_team_sport.KEY);
        col2Data = new LessonData(TEAM_is_a_SPORT_team.KEY, occupations, null, col2Prerequisites, 50, R.attr.color300, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        row.setReview(true);
        col2Prerequisites = new ArrayList<>(3);
        col2Prerequisites.add(NAME_creates_art.KEY);
        col2Prerequisites.add(TEAM_is_a_SPORT_team.KEY);
        col2Prerequisites.add(NAME_works_for_EMPLOYER.KEY);
        col2Data = new LessonData(getNextReviewID(), review, null, col2Prerequisites, 70, 0, 0);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Data = new LessonData(Find_stand_sit_on_a_bed.KEY, action, R.layout.description_find_stand_sit_on_a_bed, null, 70, R.attr.color300, actionIconID);
        col2Data = new LessonData(NAME_drives_a_car_to_CITY.KEY, people, R.layout.description_name_drives_a_car_to_city, null, 80, R.attr.color500, peopleIconID);
        col3Data = new LessonData(Turn_left_right_go_straight.KEY, action, R.layout.description_turn_left_right_go_straight, null, 80, R.attr.color300, actionIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Data = new LessonData(Thanks_no_problem_youre_welcome.KEY, greetings, null, null, 100, R.attr.color500, greetingsIconID);
        col2Prerequisites = new ArrayList<>(3);
        col2Prerequisites.add(Find_stand_sit_on_a_bed.KEY);
        col2Prerequisites.add(NAME_drives_a_car_to_CITY.KEY);
        col2Prerequisites.add(Turn_left_right_go_straight.KEY);
        col2Data = new LessonData(COUNTRY_drives_on_the_left_right.KEY, places, R.layout.description_country_drives_on_the_left_right, col2Prerequisites, 70, R.attr.color300, placesIconID);
        col3Data = new LessonData(NAME_speaks_LANGUAGE.KEY, people, R.layout.description_name_speaks_language, null, 90, R.attr.color500, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Data = new LessonData(The_man_reads_a_book_to_a_child.KEY, people, R.layout.description_the_man_reads_a_book_to_a_child, null, 70, R.attr.color700, peopleIconID);
        //col2Data = new LessonData(PLACE_is_a_city_town.KEY, places, null, null, 70, R.attr.color300, peopleIconID);
        col3Data = new LessonData(NAME_possessive_husband_wife_plays_SPORT_he_is_a_OCCUPATION.KEY, occupations, null, null, 85, R.attr.color700, peopleIconID);
        row.setCol1(col1Data);
        //row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        row.setReview(true);
        col2Prerequisites = new ArrayList<>(6);
        col2Prerequisites.add(Thanks_no_problem_youre_welcome.KEY);
        col2Prerequisites.add(COUNTRY_drives_on_the_left_right.KEY);
        col2Prerequisites.add(NAME_speaks_LANGUAGE.KEY);
        col2Prerequisites.add(The_man_reads_a_book_to_a_child.KEY);
        //col2Prerequisites.add(PLACE_is_a_city_town.KEY);
        col2Prerequisites.add(NAME_possessive_husband_wife_plays_SPORT_he_is_a_OCCUPATION.KEY);
        col2Data = new LessonData(getNextReviewID(), review, null, col2Prerequisites, 70, 0, 0);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col2Data = new LessonData(Walk_turn_create_PAST.KEY, action, R.layout.description_walk_turn_create_past, null, 85, R.attr.color300, actionIconID);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(NAME_spoke_at_TED_he_spoke_about_SUBJECT.KEY);
        col1Data = new LessonData(NAME_drove_from_CITY_to_CITY2.KEY, people, R.layout.description_name_drove_from_city_to_city2, col1Prerequisites, 60, R.attr.color500, peopleIconID);
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(Walk_turn_create_PAST.KEY);
        col2Data = new LessonData(NAME_spoke_at_TED_he_spoke_about_SUBJECT.KEY, people, R.layout.description_name_spoke_at_ted_he_spoke_about_subject, col2Prerequisites, 65, R.attr.color500, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(NAME_drove_from_CITY_to_CITY2.KEY);
        col2Data = new LessonData(NAME_was_a_OCCUPATION.KEY, occupations, R.layout.description_name_was_a_occupation, col2Prerequisites, 80, R.attr.color700, peopleIconID);
        col3Prerequisites = new ArrayList<>(1);
        col3Prerequisites.add(NAME_was_a_OCCUPATION.KEY);
        col3Data = new LessonData(NAME_wrote_a_book_about_SUBJECT.KEY, occupations, R.layout.description_name_wrote_a_book_about_subject, col3Prerequisites, 70, R.attr.color500, peopleIconID);
        row.setCol2(col2Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        col1Prerequisites = new ArrayList<>(1);
        col1Prerequisites.add(NAME_played_SPORT.KEY);
        col1Data = new LessonData(NAME_worked_for_the_government_He_was_a_politician.KEY, occupations, null, col1Prerequisites, 70, R.attr.color300, peopleIconID);
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(NAME_wrote_a_book_about_SUBJECT.KEY);
        col2Data = new LessonData(NAME_played_SPORT.KEY, occupations, R.layout.description_name_played_sport, col2Prerequisites, 60, R.attr.color500, peopleIconID);
        row.setCol1(col1Data);
        row.setCol2(col2Data);
        lessonRows.add(row);

        row = new LessonListRow();
        row.setReview(true);
        col2Prerequisites = new ArrayList<>(1);
        col2Prerequisites.add(NAME_worked_for_the_government_He_was_a_politician.KEY);
        col2Data = new LessonData(getNextReviewID(), review, null, col2Prerequisites, 70, 0, 0);
        row.setCol2(col2Data);
        lessonRows.add(row);

        adjustRowTitles(lessonRows);
        lessonLevels.add(lessonRows);

        titleCount.clear();
        nextLevelResetReviewIDCt();
        //make sure we don't clear but overwrite the reference
        lessonRows = new ArrayList<>(50);

        row = new LessonListRow();
        col1Data = new LessonData(The_emergency_phone_number_of_COUNTRY_is_NUMBER.KEY, numbers, R.layout.description_the_emergency_phone_number_of_country_is_number, null, 70, R.attr.color300, numbersIconID);
        col3Data = new LessonData(The_DEMONYM_flag_is_COLORS.KEY, places, R.layout.description_the_demonym_flag_is_colors, null, 70, R.attr.color700, placesIconID);
        row.setCol1(col1Data);
        row.setCol3(col3Data);
        lessonRows.add(row);

        row = new LessonListRow();
        row.setReview(true);
        col2Prerequisites = new ArrayList<>(2);
        col2Prerequisites.add(The_DEMONYM_flag_is_COLORS.KEY);
        col2Prerequisites.add(The_emergency_phone_number_of_COUNTRY_is_NUMBER.KEY);
        col2Data = new LessonData(getNextReviewID(), review, null, col2Prerequisites, 70, 0, 0);
        row.setCol2(col2Data);
        lessonRows.add(row);

        adjustRowTitles(lessonRows);
        lessonLevels.add(lessonRows);

        titleCount.clear();
    }
}
