package com.example.ryomi.myenglish.gui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.example.ryomi.myenglish.gui.widgets.GUIUtils;
import com.example.ryomi.myenglish.gui.widgets.UserInterestAdapter;
import com.example.ryomi.myenglish.gui.widgets.UserInterestViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
* We are using an external library for the FABs
* because Android doesn't directly support FAB menus.
* We can make our own if we have time
* */
public class UserInterests extends AppCompatActivity {
    FirebaseRecyclerAdapter firebaseAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interests);
        BottomNavigationView nav = (BottomNavigationView)findViewById(R.id.user_interests_bottom_navigation_view);
        GUIUtils.prepareBottomNavigationView(this, nav);

        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            setListListeners();
            populateFABs();
        }
    }

    private void setListListeners(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.user_interests_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("userInterests/"+userID);

        firebaseAdapter = new UserInterestAdapter(
                WikiDataEntryData.class, R.layout.inflatable_user_interests_list_item,
                UserInterestViewHolder.class, ref
        );

        recyclerView.setAdapter(firebaseAdapter);



    }

    private void populateFABs(){
        FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.user_interests_fab_menu);
        FloatingActionButton twitterButton = new FloatingActionButton(this);
        twitterButton.setIcon(R.drawable.twitter);
        twitterButton.setColorNormal(Color.parseColor("#1da1f2"));
        menu.addButton(twitterButton);

        FloatingActionButton facebookButton = new FloatingActionButton(this);
        facebookButton.setIcon(R.drawable.facebook);
        facebookButton.setColorNormal(Color.parseColor("#3b5998"));
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserInterests.this, FacebookInterests.class);
                startActivity(intent);
            }
        });
        menu.addButton(facebookButton);

        FloatingActionButton searchButton = new FloatingActionButton(this);
        searchButton.setIcon(R.drawable.ic_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserInterests.this, SearchInterests.class);
                startActivity(intent);
            }
        });

        menu.addButton(searchButton);

    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (firebaseAdapter != null)
            firebaseAdapter.cleanup();
    }
}
