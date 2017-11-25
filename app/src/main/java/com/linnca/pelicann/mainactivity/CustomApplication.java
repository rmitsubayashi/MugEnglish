package com.linnca.pelicann.mainactivity;

import android.app.Application;
import android.view.ContextThemeWrapper;

public class CustomApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }


}
