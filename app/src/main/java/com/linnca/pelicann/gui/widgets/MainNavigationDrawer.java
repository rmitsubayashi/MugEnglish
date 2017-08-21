package com.linnca.pelicann.gui.widgets;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

public class MainNavigationDrawer extends DrawerLayout {
    public MainNavigationDrawer(Context context){
        super(context);
    }

    public void populateDrawer(){
        MenuItem item;
        String title;
        String key;

        //manually enter items into the drawer
        //(no need to sync with the DB)
        //if the user clicks on some lesson we removed,
        //just tell them they need to update

    }
}
