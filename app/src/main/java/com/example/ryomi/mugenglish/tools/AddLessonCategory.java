package com.example.ryomi.mugenglish.tools;

import com.example.ryomi.mugenglish.db.FirebaseDBHeaders;
import com.example.ryomi.mugenglish.db.datawrappers.LessonCategory;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddLessonCategory {
    public static void run(){

        addCategory("Be動詞","1.1.1");
        //addCategory("現在進行形","1.2.1");
        addCategory("前置詞","1.2.1");
        //addCategory("未来系","1.3.1");
        addCategory("所有格","1.3.1");
        addCategory("過去形","1.4.1");
        addCategory("助動詞","1.4.2");
        //addCategory("疑問文（助動詞）","1.5.1");
        //addCategory("疑問文（疑問詞）","1.5.2");
        //addCategory("疑問文（Be動詞）","1.6.1");
        //addCategory("過去形（不規則）","1.7.1");
        //addCategory("現在形","1.8.1");
        //addCategory("冠詞","1.8.2");
        //addCategory("形容詞","1.9.1");
        //addCategory("副詞","1.9.2");
    }


    private static void addCategory(String title, String index){
        LessonCategory category = new LessonCategory();
        category.setIndex(index);
        category.setTitle(title);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.LESSON_CATEGORIES
        );
        String key = ref.push().getKey();
        DatabaseReference ref2 = ref.child(key);
        category.setId(key);
        ref2.setValue(category);

    }
}
