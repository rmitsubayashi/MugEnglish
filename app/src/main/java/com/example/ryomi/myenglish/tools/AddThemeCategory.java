package com.example.ryomi.myenglish.tools;

import com.example.ryomi.myenglish.db.FirebaseDBHeaders;
import com.example.ryomi.myenglish.db.datawrappers.ThemeCategory;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddThemeCategory {
    public static void run(){

        addCategory("Copula","1.1.1");
        addCategory("Present Progressive","1.2.1");
        addCategory("Prepositions","1.2.2");
        addCategory("Future","1.3.1");
        addCategory("Possessive","1.3.2");
        addCategory("Past Regular","1.4.1");
        addCategory("Present + Past Auxiliary","1.4.2");
        addCategory("Auxiliary Questions","1.5.1");
        addCategory("Intro to WH Questions","1.5.2");
        addCategory("Copula Questions","1.6.1");
        addCategory("Past Irregular","1.7.1");
        addCategory("Present Progressive Irregular","1.7.2");
        addCategory("Present Verbs","1.8.1");
        addCategory("Articles","1.8.2");
        addCategory("Adjectives","1.9.1");
        addCategory("Adverb","1.9.2");
    }


    private static void addCategory(String title, String index){
        ThemeCategory category = new ThemeCategory();
        category.setIndex(index);
        category.setTitle(title);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.THEME_CATEGORIES
        );
        String key = ref.push().getKey();
        DatabaseReference ref2 = ref.child(key);
        category.setId(key);
        ref2.setValue(category);

    }
}
