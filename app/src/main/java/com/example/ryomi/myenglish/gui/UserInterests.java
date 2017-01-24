package com.example.ryomi.myenglish.gui;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.ryomi.myenglish.gui.widgets.UserInterestAdapter;
import com.example.ryomi.myenglish.gui.widgets.UserInterestViewHolder;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
* We are using an external library for the FABs
* because Android doesn't directly support FAB menus.
* We can make our own if we have time
* */
public class UserInterests extends AppCompatActivity {
    FirebaseRecyclerAdapter firebaseAdapter;

    //holder for user interest list cells
    public static class ViewHolder  extends RecyclerView.ViewHolder {
        private final TextView label;
        private final TextView description;

        public ViewHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.user_interests_list_item_label);
            description = (TextView) itemView.findViewById(R.id.user_interests_list_item_description);
        }

        public void setLabel(String label) {
            this.label.setText(label);
        }

        public void setDescription(String description) {
            this.description.setText(description);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interests);

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
        /*firebaseAdapter = new FirebaseRecyclerAdapter<WikiDataEntryData, ViewHolder>(
                WikiDataEntryData.class, R.layout.inflatable_user_interests_list_item,
                ViewHolder.class, ref
        ) {
            @Override
            public void populateViewHolder(ViewHolder holder, WikiDataEntryData data, int position) {
                holder.setLabel(data.getLabel());
                holder.setDescription(data.getDescription());
                System.out.println("Called populate view...");
            }
        };*/
        firebaseAdapter = new UserInterestAdapter(
                WikiDataEntryData.class, R.layout.inflatable_user_interests_list_item,
                UserInterestViewHolder.class, ref
        );

        recyclerView.setAdapter(firebaseAdapter);



    }

    private void populateFABs(){
        FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.user_interests_fab_menu);
        FloatingActionButton button = new FloatingActionButton(this);
        button.setIcon(R.drawable.twitter);
        button.setColorNormal(Color.parseColor("#1da1f2"));
        menu.addButton(button);

    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        firebaseAdapter.cleanup();
    }
}
