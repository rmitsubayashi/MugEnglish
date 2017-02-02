package com.example.ryomi.myenglish.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.gui.widgets.GUIUtils;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

public class UserProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        BottomNavigationView nav = (BottomNavigationView)findViewById(R.id.user_profile_bottom_navigation_view);
        GUIUtils.prepareBottomNavigationView(this, nav);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadUser();

        } else {
            startActivityForResult(GUIUtils.getSignInIntent(), GUIUtils.REQUEST_CODE_SIGN_IN);
        }
    }

    private void loadUser(){
        Toolbar appBar = (Toolbar) findViewById(R.id.user_profile_tool_bar);
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        appBar.setTitle(userName);
        setSupportActionBar(appBar);

        Button logoutButton = (Button) findViewById(R.id.user_profile_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                //send user to main page(theme list)?
                Intent intent =new Intent(UserProfile.this, ThemeList.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_profile_app_bar_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_profile_app_bar_settings:
                UserProfile.this.getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Preferences())
                        .addToBackStack("preferences")
                        .commit();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GUIUtils.REQUEST_CODE_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            loadUser();
            return;
        }
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == RESULT_OK){

        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                Toast.makeText(this, "nope", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
}
