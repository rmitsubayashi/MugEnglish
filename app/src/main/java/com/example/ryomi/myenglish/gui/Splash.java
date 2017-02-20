package com.example.ryomi.myenglish.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.FacebookSdk;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Splash extends AppCompatActivity{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //we initialize the sdk so if the user signs in with fb
        //the token will automatically be saved
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        addCategory("Copula","1.1.1");

        Intent intent = new Intent(this, ThemeList.class);
        startActivity(intent);
        //we don't want user to go back to this screen
        finish();
    }

    private void addCategory(String title, String index){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(

        );
    }

}
