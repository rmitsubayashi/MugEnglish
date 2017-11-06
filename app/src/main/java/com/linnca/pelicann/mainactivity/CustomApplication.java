package com.linnca.pelicann.mainactivity;

import android.app.Application;

public class CustomApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
