package com.example.ryomi.myenglish.gui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.userinterestcontrols.FacebookInterestFinder;

public class FacebookInterests extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_interests);

        Async async = new Async();
        async.execute();
    }

    private class Async extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params){
            FacebookInterestFinder fif = new FacebookInterestFinder(FacebookInterests.this.getApplicationContext());
            try {
                fif.findUserInterests(FacebookInterestFinder.DEEP_SEARCH);
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }
}
