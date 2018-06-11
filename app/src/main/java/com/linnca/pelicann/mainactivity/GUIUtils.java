package com.linnca.pelicann.mainactivity;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public final class GUIUtils {
    /*
    //request code for firebase sign in
    public static final int REQUEST_CODE_SIGN_IN = 190;
    //which sign in methods to display to the user
    public static final int SIGN_IN_PROVIDER_ALL = 0;
    //these are for searching via facebook or twitter
    public static final int SIGN_IN_PROVIDER_FACEBOOK = 1;
    public static final int SIGN_IN_PROVIDER_TWITTER = 2;*/

    private GUIUtils(){}

    public static int getDp(int num, Context context){
        return (int)(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, num, context.getResources().getDisplayMetrics()));

    }

    public static boolean hideKeyboard(View targetViewOfKeyboard){
        if (targetViewOfKeyboard == null){
            return false;
        }
        targetViewOfKeyboard.clearFocus();
        InputMethodManager imm = (InputMethodManager) targetViewOfKeyboard.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        return  imm != null &&
                imm.hideSoftInputFromWindow(targetViewOfKeyboard.getWindowToken(), 0);

    }

    //for listViews
    public static <Class>List<Integer> getItemIndexesToRemove(List<Class> oldList, List<Class> newList){
        List<Integer> indexesToRemove =new ArrayList<>(oldList.size());
        //make the list a set to make it easier to search
        Set<Class> newSet = new HashSet<>(newList);
        int itemsRemoved = 0;
        for (int i=0; i<oldList.size(); i++){
            Class oldItem = oldList.get(i);
            if (!newSet.contains(oldItem)){
                indexesToRemove.add(i-itemsRemoved);
                itemsRemoved++;
            }
        }
        return indexesToRemove;
    }
    
    public static <Class>List<Integer> getItemIndexesToAdd(List<Class> oldList, List<Class> newList){
        List<Integer> indexesToAdd =new ArrayList<>(newList.size());
        //make the list a set to make it easier to search
        Set<Class> oldSet = new HashSet<>(oldList);
        for (int i=0; i<newList.size(); i++){
            Class newItem = newList.get(i);
            if (!oldSet.contains(newItem)){
                indexesToAdd.add(i);
            }
        }

        return indexesToAdd;
    }
}
