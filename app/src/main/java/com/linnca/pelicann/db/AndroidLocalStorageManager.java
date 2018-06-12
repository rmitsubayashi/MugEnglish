package com.linnca.pelicann.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import pelicann.linnca.com.corefunctionality.db.LocalStorageManager;

public class AndroidLocalStorageManager extends LocalStorageManager {
    private Context context;

    public AndroidLocalStorageManager(Context context){
        this.context = context;
    }

    @Override
    public int getLastSavedLessonIndexAtCategory(String category){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt
                (category, -1);
    }

    @Override
    public void saveLessonIndexAtCategory(String category, int index){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(category, index);
        editor.apply();
    }
}
