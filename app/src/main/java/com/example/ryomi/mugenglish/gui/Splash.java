package com.example.ryomi.mugenglish.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.tools.AddTheme;
import com.example.ryomi.mugenglish.tools.AddThemeCategory;
import com.facebook.FacebookSdk;
import com.google.firebase.auth.FirebaseAuth;

public class Splash extends AppCompatActivity{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //we initialize the sdk so if the user signs in with fb
        //the token will automatically be saved
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            FirebaseAuth.getInstance().signInAnonymously();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        //if this is the user's first time opening the application, send him through a tutorial
        boolean firstTime = preferences.getBoolean(getResources().getString(R.string.preferences_first_time_key), true);
        Intent intent;
        if (firstTime){
            intent = new Intent(this, Onboarding.class);
        } else {
            intent = new Intent(this, ThemeList.class);
        }
        startActivity(intent);
        //we don't want user to go back to this screen
        finish();
    }

}
