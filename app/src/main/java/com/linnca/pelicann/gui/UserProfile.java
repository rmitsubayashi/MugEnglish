package com.linnca.pelicann.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.db.datawrappers.InstanceRecord;
import com.linnca.pelicann.gui.widgets.GUIUtils;
import com.linnca.pelicann.gui.widgets.UserProfilePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class UserProfile extends Fragment{
    DatabaseReference ref;
    ValueEventListener listener;

    TabLayout tabLayout;
    ViewPager viewPager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadUser();

        } else {
            startActivityForResult(
                    GUIUtils.getSignInIntent(GUIUtils.SIGN_IN_PROVIDER_ALL),
                    GUIUtils.REQUEST_CODE_SIGN_IN
            );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        tabLayout = (TabLayout) view.findViewById(R.id.user_profile_tab_layout);
        viewPager = (ViewPager) view.findViewById(R.id.user_profile_pager);
        return view;
    }

    private void loadUser(){

        //get user data needed to populate views adn update views
        populateUserData();



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            }
        }
    }

    private void populateUserData(){
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return;
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

        tabLayout.addTab(tabLayout.newTab().setText(R.string.user_profile_tab_hours_studied));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.user_profile_tab_report_card));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setSelectedTabIndicatorHeight(GUIUtils.getDp(4,getContext()));
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
                new UserProfilePagerAdapter(getChildFragmentManager(), tabLayout.getTabCount(), records);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        ref.removeEventListener(listener);
    }
}
