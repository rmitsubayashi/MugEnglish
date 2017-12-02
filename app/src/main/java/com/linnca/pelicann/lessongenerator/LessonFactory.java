package com.linnca.pelicann.lessongenerator;

//create an instance of the required lesson class

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.lessons.COMPANY_makes_PRODUCT;
import com.linnca.pelicann.lessongenerator.lessons.COUNTRY_drives_on_the_left_right;
import com.linnca.pelicann.lessongenerator.lessons.COUNTRY_possessive_area_is_AREA;
import com.linnca.pelicann.lessongenerator.lessons.COUNTRY_possessive_government_is_a_GOVERNMENT;
import com.linnca.pelicann.lessongenerator.lessons.COUNTRY_possessive_population_is_POPULATION;
import com.linnca.pelicann.lessongenerator.lessons.Find_stand_sit_on_a_bed;
import com.linnca.pelicann.lessongenerator.lessons.Goodbye_bye;
import com.linnca.pelicann.lessongenerator.lessons.Hello_my_name_is_NAME;
import com.linnca.pelicann.lessongenerator.lessons.Hello_my_name_is_NAME_I_am_a_OCCUPATION;
import com.linnca.pelicann.lessongenerator.lessons.Hello_my_name_is_NAME_I_am_from_CITY;
import com.linnca.pelicann.lessongenerator.lessons.Hi_hey_whats_up;
import com.linnca.pelicann.lessongenerator.lessons.How_are_you_doing;
import com.linnca.pelicann.lessongenerator.lessons.I_turned_left_right;
import com.linnca.pelicann.lessongenerator.lessons.NAME_created_art;
import com.linnca.pelicann.lessongenerator.lessons.NAME_creates_art;
import com.linnca.pelicann.lessongenerator.lessons.NAME_drives_a_car;
import com.linnca.pelicann.lessongenerator.lessons.NAME_drove_from_CITY;
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
import com.linnca.pelicann.lessongenerator.lessons.NAME_plays_SPORT_SPORT_is_a_individual_team_sport;
import com.linnca.pelicann.lessongenerator.lessons.NAME_plays_SPORT_SPORT_is_a_water_sport;
import com.linnca.pelicann.lessongenerator.lessons.NAME_possessive_first_last_name_is_NAME;
import com.linnca.pelicann.lessongenerator.lessons.NAME_possessive_husband_wife_plays_SPORT_he_is_a_OCCUPATION;
import com.linnca.pelicann.lessongenerator.lessons.NAME_possessive_mother_father_is_NAME2;
import com.linnca.pelicann.lessongenerator.lessons.NAME_speaks_LANGUAGE;
import com.linnca.pelicann.lessongenerator.lessons.NAME_spoke_at_TED;
import com.linnca.pelicann.lessongenerator.lessons.NAME_was_a_OCCUPATION;
import com.linnca.pelicann.lessongenerator.lessons.NAME_worked_for_the_government_He_was_a_politician;
import com.linnca.pelicann.lessongenerator.lessons.NAME_works_at_EMPLOYER;
import com.linnca.pelicann.lessongenerator.lessons.NAME_works_for_EMPLOYER;
import com.linnca.pelicann.lessongenerator.lessons.NAME_works_for_the_government_He_is_a_politician;
import com.linnca.pelicann.lessongenerator.lessons.NAME_writes_books;
import com.linnca.pelicann.lessongenerator.lessons.NAME_writes_songs;
import com.linnca.pelicann.lessongenerator.lessons.NAME_wrote_a_book;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_0_3;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_10s;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_11_19;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_21_99;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_4_6;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_7_9;
import com.linnca.pelicann.lessongenerator.lessons.Numbers_hundred_billion;
import com.linnca.pelicann.lessongenerator.lessons.PLACE_is_a_city_town;
import com.linnca.pelicann.lessongenerator.lessons.PLACE_is_a_country_city;
import com.linnca.pelicann.lessongenerator.lessons.Stand_up_sit_down;
import com.linnca.pelicann.lessongenerator.lessons.TEAM_is_a_SPORT_team;
import com.linnca.pelicann.lessongenerator.lessons.Thanks_no_problem;
import com.linnca.pelicann.lessongenerator.lessons.The_DEMONYM_flag_is_COLORS;
import com.linnca.pelicann.lessongenerator.lessons.The_emergency_phone_number_of_COUNTRY_is_NUMBER;
import com.linnca.pelicann.lessongenerator.lessons.The_man_woman_reads_a_book;
import com.linnca.pelicann.lessongenerator.lessons.Turn_left_right_go_straight;
import com.linnca.pelicann.lessongenerator.lessons.Walk_run;
import com.linnca.pelicann.lessongenerator.lessons.good_morning_afternoon_evening;

public final class LessonFactory {
    private static final String TAG = "LessonFactory";
    private LessonFactory(){}
    public static Lesson parseLesson(String lessonKey, EndpointConnectorReturnsXML connector, Database db, Lesson.LessonListener listener){
        switch (lessonKey) {
            case NAME_is_DEMONYM.KEY :
                return new NAME_is_DEMONYM(
                        connector,
                        db, listener);
            case NAME_is_a_OCCUPATION.KEY :
                return new NAME_is_a_OCCUPATION(
                        connector,
                        db, listener
                );
            case The_DEMONYM_flag_is_COLORS.KEY :
                return new The_DEMONYM_flag_is_COLORS(
                        connector,
                        db, listener
                );
            case The_emergency_phone_number_of_COUNTRY_is_NUMBER.KEY :{
                return new The_emergency_phone_number_of_COUNTRY_is_NUMBER(
                        connector,
                        db, listener
                );
            }
            case Hello_my_name_is_NAME.KEY :{
                return new Hello_my_name_is_NAME(
                        connector,
                        db, listener
                );
            }
            case good_morning_afternoon_evening.KEY :{
                return new good_morning_afternoon_evening(
                        connector,
                        db, listener
                );
            }
            case NAME_is_AGE_years_old.KEY :{
                return new NAME_is_AGE_years_old(
                        connector,
                        db, listener
                );
            }
            case NAME_plays_SPORT.KEY :{
                return new NAME_plays_SPORT(
                        connector,
                        db, listener
                );
            }
            case COMPANY_makes_PRODUCT.KEY :{
                return new COMPANY_makes_PRODUCT(
                        connector,
                        db, listener
                );
            }
            case NAME_works_at_EMPLOYER.KEY :{
                return new NAME_works_at_EMPLOYER(
                        connector,
                        db, listener
                );
            }
            case NAME_is_at_work_He_is_at_EMPLOYER.KEY :{
                return new NAME_is_at_work_He_is_at_EMPLOYER(
                        connector,
                        db, listener
                );
            }
            case PLACE_is_a_country_city.KEY :{
                return new PLACE_is_a_country_city(
                        connector,
                        db, listener
                );
            }
            case Walk_run.KEY :{
                return new Walk_run(
                        connector,
                        db, listener
                );
            }
            case Stand_up_sit_down.KEY :{
                return new Stand_up_sit_down(
                        connector,
                        db, listener
                );
            }
            case NAME_is_from_COUNTRY.KEY :{
                return new NAME_is_from_COUNTRY(
                        connector,
                        db, listener
                );
            }
            case Hello_my_name_is_NAME_I_am_from_CITY.KEY :{
                return new Hello_my_name_is_NAME_I_am_from_CITY(
                        connector,
                        db, listener
                );
            }
            case Hello_my_name_is_NAME_I_am_a_OCCUPATION.KEY :{
                return new Hello_my_name_is_NAME_I_am_a_OCCUPATION(
                        connector,
                        db, listener
                );
            }
            case Numbers_0_3.KEY :{
                return new Numbers_0_3(
                        connector,
                        db, listener
                );
            }
            case Numbers_4_6.KEY :{
                return new Numbers_4_6(
                        connector,
                        db, listener
                );
            }
            case Numbers_7_9.KEY :{
                return new Numbers_7_9(
                        connector,
                        db, listener
                );
            }
            case Numbers_11_19.KEY :{
                return new Numbers_11_19(
                        connector,
                        db, listener
                );
            }
            case Numbers_10s.KEY :{
                return new Numbers_10s(
                        connector,
                        db, listener
                );
            }
            case Numbers_21_99.KEY :{
                return new Numbers_21_99(
                        connector,
                        db, listener
                );
            }
            case Goodbye_bye.KEY :{
                return new Goodbye_bye(
                        connector,
                        db, listener
                );
            }
            case How_are_you_doing.KEY :{
                return new How_are_you_doing(
                        connector,
                        db, listener
                );
            }
            case NAME_is_a_GENDER.KEY :{
                return new NAME_is_a_GENDER(
                        connector,
                        db, listener
                );
            }
            case Hi_hey_whats_up.KEY :{
                return new Hi_hey_whats_up(
                        connector,
                        db, listener
                );
            }
            case NAME_is_AGE_years_old_NAME_is_a_GENDER.KEY : {
                return new NAME_is_AGE_years_old_NAME_is_a_GENDER(
                        connector,
                        db, listener
                );
            }
            case Numbers_hundred_billion.KEY : {
                return new Numbers_hundred_billion(
                        connector,
                        db, listener
                );
            }
            case NAME_possessive_mother_father_is_NAME2.KEY : {
                return new NAME_possessive_mother_father_is_NAME2(
                        connector,
                        db, listener
                );
            }
            case NAME_possessive_first_last_name_is_NAME.KEY : {
                return new NAME_possessive_first_last_name_is_NAME(
                        connector,
                        db, listener
                );
            }
            case NAME_is_NAME2_possessive_husband_wife.KEY : {
                return new NAME_is_NAME2_possessive_husband_wife(
                        connector,
                        db, listener
                );
            }
            case COUNTRY_possessive_government_is_a_GOVERNMENT.KEY : {
                return new COUNTRY_possessive_government_is_a_GOVERNMENT(
                        connector,
                        db, listener
                );
            }
            case COUNTRY_possessive_population_is_POPULATION.KEY : {
                return new COUNTRY_possessive_population_is_POPULATION(
                        connector,
                        db, listener
                );
            }
            case COUNTRY_possessive_area_is_AREA.KEY : {
                return new COUNTRY_possessive_area_is_AREA(
                        connector,
                        db, listener
                );
            }
            case NAME_writes_books.KEY : {
                return new NAME_writes_books(
                        connector,
                        db, listener
                );
            }
            case NAME_writes_songs.KEY : {
                return new NAME_writes_songs(
                        connector,
                        db, listener
                );
            }
            case NAME_creates_art.KEY : {
                return new NAME_creates_art(
                        connector,
                        db, listener
                );
            }
            case NAME_works_for_the_government_He_is_a_politician.KEY : {
                return new NAME_works_for_the_government_He_is_a_politician(
                        connector,
                        db, listener
                );
            }
            case NAME_works_for_EMPLOYER.KEY : {
                return new NAME_works_for_EMPLOYER(
                        connector,
                        db, listener
                );
            }
            case NAME_plays_SPORT_SPORT_is_a_water_sport.KEY : {
                return new NAME_plays_SPORT_SPORT_is_a_water_sport(
                        connector,
                        db, listener
                );
            }
            case NAME_plays_SPORT_SPORT_is_a_individual_team_sport.KEY : {
                return new NAME_plays_SPORT_SPORT_is_a_individual_team_sport(
                        connector,
                        db, listener
                );
            }
            case TEAM_is_a_SPORT_team.KEY : {
                return new TEAM_is_a_SPORT_team(
                        connector,
                        db, listener
                );
            }
            case NAME_possessive_husband_wife_plays_SPORT_he_is_a_OCCUPATION.KEY : {
                return new NAME_possessive_husband_wife_plays_SPORT_he_is_a_OCCUPATION(
                        connector,
                        db, listener
                );
            }
            case NAME_speaks_LANGUAGE.KEY : {
                return new NAME_speaks_LANGUAGE(
                        connector,
                        db, listener
                );
            }
            case Find_stand_sit_on_a_bed.KEY : {
                return new Find_stand_sit_on_a_bed(
                        connector,
                        db, listener
                );
            }
            case Turn_left_right_go_straight.KEY : {
                return new Turn_left_right_go_straight(
                        connector,
                        db, listener
                );
            }
            case NAME_drives_a_car.KEY : {
                return new NAME_drives_a_car(
                        connector,
                        db, listener
                );
            }
            case COUNTRY_drives_on_the_left_right.KEY : {
                return new COUNTRY_drives_on_the_left_right(
                        connector,
                        db, listener
                );
            }
            case The_man_woman_reads_a_book.KEY : {
                return new The_man_woman_reads_a_book(
                        connector,
                        db, listener
                );
            }
            case Thanks_no_problem.KEY : {
                return new Thanks_no_problem(
                        connector,
                        db, listener
                );
            }
            case PLACE_is_a_city_town.KEY : {
                return new PLACE_is_a_city_town(
                        connector,
                        db, listener
                );
            }
            case I_turned_left_right.KEY : {
                return new I_turned_left_right(
                        connector,
                        db, listener
                );
            }
            case NAME_spoke_at_TED.KEY : {
                return new NAME_spoke_at_TED(
                        connector,
                        db, listener
                );
            }
            case NAME_drove_from_CITY.KEY : {
                return new NAME_drove_from_CITY(
                        connector,
                        db, listener
                );
            }
            case NAME_wrote_a_book.KEY : {
                return new NAME_wrote_a_book(
                        connector,
                        db, listener
                );
            }
            case NAME_created_art.KEY : {
                return new NAME_created_art(
                        connector,
                        db, listener
                );
            }
            case NAME_played_SPORT.KEY : {
                return new NAME_played_SPORT(
                        connector,
                        db, listener
                );
            }
            case NAME_was_a_OCCUPATION.KEY : {
                return new NAME_was_a_OCCUPATION(
                        connector,
                        db, listener
                );
            }
            case NAME_worked_for_the_government_He_was_a_politician.KEY : {
                return new NAME_worked_for_the_government_He_was_a_politician(
                        connector,
                        db, listener
                );
            }

            default:
                return null;
        }
    }

    public static void saveGenericQuestions(Database db){

        Lesson lesson = new good_morning_afternoon_evening(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Hello_my_name_is_NAME(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new COMPANY_makes_PRODUCT(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new PLACE_is_a_country_city(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Walk_run(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Stand_up_sit_down(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Numbers_0_3(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Numbers_4_6(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Numbers_7_9(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Numbers_11_19(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Numbers_10s(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Numbers_21_99(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Goodbye_bye(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new How_are_you_doing(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Hi_hey_whats_up(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Numbers_hundred_billion(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new NAME_possessive_mother_father_is_NAME2(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new COUNTRY_possessive_government_is_a_GOVERNMENT(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new COUNTRY_possessive_population_is_POPULATION(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new COUNTRY_possessive_area_is_AREA(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new NAME_works_for_the_government_He_is_a_politician(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new NAME_plays_SPORT_SPORT_is_a_water_sport(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new NAME_plays_SPORT_SPORT_is_a_individual_team_sport(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Find_stand_sit_on_a_bed(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Turn_left_right_go_straight(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new COUNTRY_drives_on_the_left_right(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new NAME_drives_a_car(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new The_man_woman_reads_a_book(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new Thanks_no_problem(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new PLACE_is_a_city_town(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new I_turned_left_right(null, db, null);
        lesson.saveGenericQuestions();
        lesson = new NAME_worked_for_the_government_He_was_a_politician(null, db, null);
        lesson.saveGenericQuestions();

    }
}
