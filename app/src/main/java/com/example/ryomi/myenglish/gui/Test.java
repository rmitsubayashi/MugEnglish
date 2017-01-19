package com.example.ryomi.myenglish.gui;

import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ui.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class Test extends AppCompatActivity {
    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Button button = (Button) findViewById(R.id.checkLoginStatus);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                TextView tv = (TextView) findViewById(R.id.test_output);
                if (auth.getCurrentUser() == null){
                    tv.setText("User not signed in");
                } else{
                    tv.setText(auth.getCurrentUser().getUid());
                }
            }
        });

        Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                AuthUI authUI = AuthUI.getInstance();

                if (auth.getCurrentUser() != null) {
                    TextView tv = (TextView) findViewById(R.id.test_output);
                    tv.setText("Already logged in");
                    auth.signOut();
                } else {
                    // not signed in
                    startActivityForResult(
                            AuthUI.getInstance().createSignInIntentBuilder()
                                    .setProviders(getSelectedProviders())
                                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        });

        Button addInterest =(Button) findViewById(R.id.add_interest);
        addInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Test.this, ThemeList.class);
                startActivity(intent);

            }
        });

        Button readInterest = (Button) findViewById(R.id.readInterest);
        readInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    String userID = auth.getCurrentUser().getUid();
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference ref = db.getReference();
                    DatabaseReference refChildren = ref.child("userInterests").child(userID);
                    ValueEventListener postQuery = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            TextView tv = (TextView) findViewById(R.id.test_output);
                            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                WikiDataEntryData data = singleSnapshot.getValue(WikiDataEntryData.class);
                                tv.append(data.getLabel());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };

                    refChildren.addValueEventListener(postQuery);

                }
            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }

    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == ResultCodes.RESULT_NO_NETWORK) {
            System.out.println("Success!!!!");
            finish();
            return;
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button

                return;
            }
            System.out.println("Result code: " + resultCode);
        }
    }

    private List<AuthUI.IdpConfig> getSelectedProviders() {
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();

        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());

        /*
        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());
        */
        selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER)
                        .setPermissions(getFacebookPermissions())
                        .build());

        /*selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                        .setPermissions(getGooglePermissions())
                        .build());*/
        return selectedProviders;
    }

    private List<String> getFacebookPermissions(){
        List<String> result = new ArrayList<>();
        result.add("public_profile");
        result.add("user_likes");
        result.add("user_hometown");
        result.add("user_games_activity");
        result.add("user_events");
        result.add("user_education_history");
        result.add("user_birthday");
        result.add("user_location");
        result.add("user_religion_politics");
        result.add("user_tagged_places");
        result.add("user_work_history");
        return result;
}

}
