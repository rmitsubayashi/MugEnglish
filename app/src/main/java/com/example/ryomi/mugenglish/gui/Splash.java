package com.example.ryomi.mugenglish.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.ryomi.mugenglish.db.datawrappers.ThemeCategory;
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

        Intent intent = new Intent(this, ThemeList.class);
        startActivity(intent);
        //we don't want user to go back to this screen
        finish();
    }

}
