package com.linnca.pelicann.gui.widgets;

import com.linnca.pelicann.R;
import com.linnca.pelicann.questiongenerator.lessons.NAME_is_DEMONYM;
import com.linnca.pelicann.questiongenerator.lessons.NAME_is_a_OCCUPATION;
import com.linnca.pelicann.questiongenerator.lessons.The_DEMONYM_flag_is_COLORS;

import java.util.HashMap;
import java.util.Map;

public class LessonDescriptionLayoutHelper {
    private Map<String, Integer> layoutIDs = new HashMap<>();

    public LessonDescriptionLayoutHelper(){
        populateLayoutIDs();
    }

    public boolean layoutExists(String lessonKey){
        return lessonKey != null && layoutIDs.containsKey(lessonKey);
    }


    public Integer getLayoutID(String lessonKey){
        if (lessonKey == null)
            return null;
        if (!layoutExists(lessonKey))
            return null;
        return layoutIDs.get(lessonKey);
    }

    private void populateLayoutIDs(){
        //layoutIDs.put(NAME_is_DEMONYM.KEY, null);
        //layoutIDs.put(The_DEMONYM_flag_is_COLORS.KEY, null);
        layoutIDs.put(NAME_is_a_OCCUPATION.KEY, R.layout.fragment_description_name_is_a_occupation);
    }
}
