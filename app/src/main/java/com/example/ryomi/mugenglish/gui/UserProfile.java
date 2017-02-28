package com.example.ryomi.mugenglish.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.db.FirebaseDBHeaders;
import com.example.ryomi.mugenglish.db.datawrappers.InstanceRecord;
import com.example.ryomi.mugenglish.gui.widgets.GUIUtils;
import com.example.ryomi.mugenglish.gui.widgets.UserProfilePagerAdapter;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProfile extends AppCompatActivity implements Preferences.LogoutListener{
    DatabaseReference ref;
    ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        BottomNavigationView nav = (BottomNavigationView)findViewById(R.id.user_profile_bottom_navigation_view);
        GUIUtils.prepareBottomNavigationView(this, nav);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadUser();

        } else {
            startActivityForResult(
                    GUIUtils.getSignInIntent(GUIUtils.SIGN_IN_PROVIDER_ALL),
                    GUIUtils.REQUEST_CODE_SIGN_IN
            );
        }
    }

    private void loadUser(){
        Toolbar appBar = (Toolbar) findViewById(R.id.user_profile_tool_bar);
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        appBar.setTitle(userName);
        setSupportActionBar(appBar);

        //get user data needed to populate views adn update views
        populateUserData();



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
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GUIUtils.REQUEST_CODE_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            loadUser();
        }
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == RESULT_OK){
            loadUser();
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                Toast.makeText(this, "nope", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void populateUserData(){
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.INSTANCE_RECORDS + "/" + userID
        );
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<InstanceRecord> records = new ArrayList<>();
                //two layers down
                for (DataSnapshot theme : dataSnapshot.getChildren()){
                    for (DataSnapshot instance : theme.getChildren()){
                        for (DataSnapshot instanceRecord : instance.getChildren()){

                            InstanceRecord record = instanceRecord.getValue(InstanceRecord.class);
                            records.add(record);

                        }
                    }
                }
                //once we fetched the data, continue to populate the tabs
                populateTabs(records);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref.addValueEventListener(listener);
    }

    private void populateTabs(List<InstanceRecord> records){
        TabLayout tabLayout = (TabLayout) findViewById(R.id.user_profile_tab_layout);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.user_profile_pager);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.user_profile_tab_hours_studied));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.user_profile_tab_report_card));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setSelectedTabIndicatorHeight(GUIUtils.getDp(4,this));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        UserProfilePagerAdapter adapter =
                new UserProfilePagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), records);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    @Override
    public void logout(){
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }
        if (FirebaseAuth.getInstance().getCurrentUser().getProviderId().equals(FirebaseAuthProvider.PROVIDER_ID))
            return;
        FirebaseAuth.getInstance().signOut();
        //remove this fragment
        getFragmentManager().popBackStack();
        //send user to main page(theme list)?
        Intent intent =new Intent(this, ThemeList.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        ref.removeEventListener(listener);
    }
}
