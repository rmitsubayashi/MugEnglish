package pelicann.linnca.com.corefunctionality.lessongeneration;

//create an instance of the required lesson class

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.COMPANY_makes_PRODUCT;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.COUNTRY_drives_on_the_left_right;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.COUNTRY_possessive_area_is_AREA;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.COUNTRY_possessive_government_is_a_GOVERNMENT;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.COUNTRY_possessive_population_is_POPULATION;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Find_stand_sit_on_a_bed;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Good_morning_afternoon_evening;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Goodbye_bye;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Hello_my_name_is_NAME;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Hello_my_name_is_NAME_I_am_from_CITY;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Hi_hey_whats_up;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.How_are_you_doing;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_creates_art;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_drives_a_car_to_CITY;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_drove_from_CITY_to_CITY2;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_is_AGE_years_old;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_is_AGE_years_old_NAME_is_a_GENDER;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_is_DEMONYM;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_is_NAME2_possessive_husband_wife;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_is_a_GENDER;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_is_a_OCCUPATION;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_is_at_work_He_is_at_EMPLOYER;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_is_from_COUNTRY;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_is_young_He_is_AGE;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_played_SPORT;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_plays_SPORT;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_plays_SPORT_It_is_a_individual_team_sport;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_plays_SPORT_It_is_a_water_sport;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_possessive_first_last_name_is_NAME;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_possessive_husband_wife_plays_SPORT_he_is_a_OCCUPATION;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_possessive_mother_father_is_NAME2;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_speaks_LANGUAGE;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_spoke_at_TED_he_spoke_about_SUBJECT;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_was_a_OCCUPATION;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_worked_for_the_government_He_was_a_politician;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_works_at_EMPLOYER;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_works_for_EMPLOYER;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_works_for_the_government_He_is_a_politician;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_writes_books;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_writes_songs;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.NAME_wrote_a_book_about_SUBJECT;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Numbers_0_3;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Numbers_10s;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Numbers_11_19;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Numbers_21_99;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Numbers_4_6;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Numbers_7_9;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Numbers_hundred_billion;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.PLACE_is_a_city_town;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.PLACE_is_a_country_city;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Stand_up_sit_down;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.TEAM_is_a_SPORT_team;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Thanks_no_problem_youre_welcome;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.The_DEMONYM_flag_is_COLORS;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.The_emergency_phone_number_of_COUNTRY_is_NUMBER;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.The_man_reads_a_book_to_a_child;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Turn_left_right_go_straight;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Walk_run;
import pelicann.linnca.com.corefunctionality.lessongeneration.lessons.Walk_turn_create_PAST;

import java.util.ArrayList;
import java.util.List;

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
            case Good_morning_afternoon_evening.KEY :{
                return new Good_morning_afternoon_evening(
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
            case NAME_plays_SPORT_It_is_a_water_sport.KEY : {
                return new NAME_plays_SPORT_It_is_a_water_sport(
                        connector,
                        db, listener
                );
            }
            case NAME_plays_SPORT_It_is_a_individual_team_sport.KEY : {
                return new NAME_plays_SPORT_It_is_a_individual_team_sport(
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
            case NAME_drives_a_car_to_CITY.KEY : {
                return new NAME_drives_a_car_to_CITY(
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
            case The_man_reads_a_book_to_a_child.KEY : {
                return new The_man_reads_a_book_to_a_child(
                        connector,
                        db, listener
                );
            }
            case Thanks_no_problem_youre_welcome.KEY : {
                return new Thanks_no_problem_youre_welcome(
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
            case Walk_turn_create_PAST.KEY : {
                return new Walk_turn_create_PAST(
                        connector,
                        db, listener
                );
            }
            case NAME_spoke_at_TED_he_spoke_about_SUBJECT.KEY : {
                return new NAME_spoke_at_TED_he_spoke_about_SUBJECT(
                        connector,
                        db, listener
                );
            }
            case NAME_drove_from_CITY_to_CITY2.KEY : {
                return new NAME_drove_from_CITY_to_CITY2(
                        connector,
                        db, listener
                );
            }
            case NAME_wrote_a_book_about_SUBJECT.KEY : {
                return new NAME_wrote_a_book_about_SUBJECT(
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
            case NAME_is_young_He_is_AGE.KEY : {
                return new NAME_is_young_He_is_AGE(
                        connector,
                        db, listener
                );
            }

            default:
                return null;
        }
    }

    public static void saveGenericQuestions(Database db){
        List<Lesson> allLessons = getAllLessons(null, db, null);
        for (Lesson lesson : allLessons){
            lesson.saveGenericQuestions();
        }
    }

    private static List<Lesson> getAllLessons(EndpointConnectorReturnsXML connector, Database db, Lesson.LessonListener listener){
        List<Lesson> lessons = new ArrayList<>(100);
        lessons.add(new COMPANY_makes_PRODUCT(connector, db, listener));
        lessons.add(new COUNTRY_drives_on_the_left_right(connector, db, listener));
        lessons.add(new COUNTRY_possessive_area_is_AREA(connector, db, listener));
        lessons.add(new COUNTRY_possessive_government_is_a_GOVERNMENT(connector, db, listener));
        lessons.add(new COUNTRY_possessive_population_is_POPULATION(connector, db, listener));
        lessons.add(new Find_stand_sit_on_a_bed(connector, db, listener));
        lessons.add(new Good_morning_afternoon_evening(connector, db, listener));
        lessons.add(new Goodbye_bye(connector, db, listener));
        lessons.add(new Hello_my_name_is_NAME(connector, db, listener));
        lessons.add(new Hello_my_name_is_NAME_I_am_from_CITY(connector, db, listener));
        lessons.add(new Hi_hey_whats_up(connector, db, listener));
        lessons.add(new How_are_you_doing(connector, db, listener));
        lessons.add(new NAME_creates_art(connector, db, listener));
        lessons.add(new NAME_drives_a_car_to_CITY(connector, db, listener));
        lessons.add(new NAME_drove_from_CITY_to_CITY2(connector, db, listener));
        lessons.add(new NAME_is_a_GENDER(connector, db, listener));
        lessons.add(new NAME_is_a_OCCUPATION(connector, db, listener));
        lessons.add(new NAME_is_AGE_years_old(connector, db, listener));
        lessons.add(new NAME_is_AGE_years_old_NAME_is_a_GENDER(connector, db, listener));
        lessons.add(new NAME_is_at_work_He_is_at_EMPLOYER(connector, db, listener));
        lessons.add(new NAME_is_DEMONYM(connector, db, listener));
        lessons.add(new NAME_is_from_COUNTRY(connector, db, listener));
        lessons.add(new NAME_is_NAME2_possessive_husband_wife(connector, db, listener));
        lessons.add(new NAME_is_young_He_is_AGE(connector, db, listener));
        lessons.add(new NAME_played_SPORT(connector, db, listener));
        lessons.add(new NAME_plays_SPORT(connector, db, listener));
        lessons.add(new NAME_plays_SPORT_It_is_a_individual_team_sport(connector, db, listener));
        lessons.add(new NAME_plays_SPORT_It_is_a_water_sport(connector, db, listener));
        lessons.add(new NAME_possessive_first_last_name_is_NAME(connector, db, listener));
        lessons.add(new NAME_possessive_husband_wife_plays_SPORT_he_is_a_OCCUPATION(connector, db, listener));
        lessons.add(new NAME_possessive_mother_father_is_NAME2(connector, db, listener));
        lessons.add(new NAME_speaks_LANGUAGE(connector, db, listener));
        lessons.add(new NAME_spoke_at_TED_he_spoke_about_SUBJECT(connector, db, listener));
        lessons.add(new NAME_was_a_OCCUPATION(connector, db, listener));
        lessons.add(new NAME_worked_for_the_government_He_was_a_politician(connector, db, listener));
        lessons.add(new NAME_works_at_EMPLOYER(connector, db, listener));
        lessons.add(new NAME_works_for_EMPLOYER(connector, db, listener));
        lessons.add(new NAME_works_for_the_government_He_is_a_politician(connector, db, listener));
        lessons.add(new NAME_writes_books(connector, db, listener));
        lessons.add(new NAME_writes_songs(connector, db, listener));
        lessons.add(new NAME_wrote_a_book_about_SUBJECT(connector, db, listener));
        lessons.add(new Numbers_0_3(connector, db, listener));
        lessons.add(new Numbers_4_6(connector, db, listener));
        lessons.add(new Numbers_7_9(connector, db, listener));
        lessons.add(new Numbers_10s(connector, db, listener));
        lessons.add(new Numbers_11_19(connector, db, listener));
        lessons.add(new Numbers_21_99(connector, db, listener));
        lessons.add(new Numbers_hundred_billion(connector, db, listener));
        lessons.add(new PLACE_is_a_city_town(connector, db, listener));
        lessons.add(new PLACE_is_a_country_city(connector, db, listener));
        lessons.add(new Stand_up_sit_down(connector, db, listener));
        lessons.add(new TEAM_is_a_SPORT_team(connector, db, listener));
        lessons.add(new Thanks_no_problem_youre_welcome(connector, db, listener));
        lessons.add(new The_DEMONYM_flag_is_COLORS(connector, db, listener));
        lessons.add(new The_emergency_phone_number_of_COUNTRY_is_NUMBER(connector, db, listener));
        lessons.add(new The_man_reads_a_book_to_a_child(connector, db, listener));
        lessons.add(new Turn_left_right_go_straight(connector, db, listener));
        lessons.add(new Walk_run(connector, db, listener));
        lessons.add(new Walk_turn_create_PAST(connector, db, listener));

        return lessons;
    }
}
