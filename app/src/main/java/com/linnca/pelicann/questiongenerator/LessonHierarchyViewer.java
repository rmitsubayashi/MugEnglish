package com.linnca.pelicann.questiongenerator;

import android.content.Context;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.datawrappers.LessonCategory;
import com.linnca.pelicann.db.datawrappers.LessonData;
import com.linnca.pelicann.questiongenerator.lessons.NAME_is_DEMONYM;
import com.linnca.pelicann.questiongenerator.lessons.NAME_is_a_OCCUPATION;
import com.linnca.pelicann.questiongenerator.lessons.The_DEMONYM_flag_is_COLORS;

import java.util.ArrayList;
import java.util.List;

public class LessonHierarchyViewer {
    public static final int ID_GREETINGS = 1;
    public static final int ID_WORK = 2;
    public static final int ID_COUNTRIES = 3;
    private List<LessonCategory> lessonCategories = new ArrayList<>();

    public LessonHierarchyViewer(Context context){
        populateLessons(context);
    }

    private void populateLessons(Context context){
        //we are using resources for the category titles because we are using them
        // in the menu resource file (navigation drawer).
        //the lesson titles can be strings because
        // we are populating the strings into a list to display.
        /*LessonCategory greetingsCategory = new LessonCategory(ID_GREETINGS, context.getString(R.string.lesson_category_greetings));
        greetingsCategory.addLesson(new LessonData());
        lessonCategories.add(greetingsCategory);*/

        LessonCategory workCategory = new LessonCategory(ID_WORK, context.getString(R.string.lesson_category_work));
        workCategory.addLesson(new LessonData(NAME_is_a_OCCUPATION.KEY, "職業", null));
        //workCategory.addLesson(new LessonData(NAME_plays_SPORT.KEY, "スポーツ選手", null));

        lessonCategories.add(workCategory);

        LessonCategory countriesAndPrefecturesCategory = new LessonCategory(ID_COUNTRIES, context.getString(R.string.lesson_category_countries));
        countriesAndPrefecturesCategory.addLesson(new LessonData(The_DEMONYM_flag_is_COLORS.KEY, "国旗の色", null));
        countriesAndPrefecturesCategory.addLesson(new LessonData(NAME_is_DEMONYM.KEY, "国民の名称", null));
        lessonCategories.add(countriesAndPrefecturesCategory);
    }

    public LessonCategory getLessonCategory(int categoryID){
        for (LessonCategory category : lessonCategories){
            int categoryIDToMatch = category.getId();
            if (categoryID == categoryIDToMatch){
                return category;
            }
        }

        return null;
    }
}
