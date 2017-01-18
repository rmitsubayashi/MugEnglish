package com.example.ryomi.myenglish.gui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
* We will create tabs later
* */
public class UserProfile extends AppCompatActivity {
    FirebaseRecyclerAdapter firebaseAdapter;

    //holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
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
        setContentView(R.layout.activity_user_profile);

        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            populateList();
        }
    }

    private void populateList(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.user_interests_list);
        //recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("userInterests/"+userID);
        firebaseAdapter = new FirebaseRecyclerAdapter<WikiDataEntryData, ViewHolder>(
                WikiDataEntryData.class, R.layout.inflatable_user_interests_list_item,
                ViewHolder.class, ref
        ) {
            @Override
            public void populateViewHolder(ViewHolder holder, WikiDataEntryData data, int position) {
                holder.setLabel(data.getLabel());
                holder.setDescription(data.getDescription());
                System.out.println("Called populate view...");
            }
        };

        recyclerView.setAdapter(firebaseAdapter);

    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        firebaseAdapter.cleanup();
    }
}
