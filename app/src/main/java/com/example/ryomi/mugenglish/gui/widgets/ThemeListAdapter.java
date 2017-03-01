package com.example.ryomi.mugenglish.gui.widgets;


import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.db.FirebaseDBHeaders;
import com.example.ryomi.mugenglish.db.datawrappers.AchievementStars;
import com.example.ryomi.mugenglish.db.datawrappers.ThemeData;
import com.example.ryomi.mugenglish.gui.ThemeDetails;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ThemeListAdapter
        extends FirebaseRecyclerAdapter<ThemeData, ThemeListViewHolder> {

    private ProgressBar loading;

    public ThemeListAdapter(Query query, ProgressBar loading){
        super(ThemeData.class, R.layout.inflatable_theme_list_list_item,
                ThemeListViewHolder.class, query);
        this.loading = loading;
    }

    @Override
    public void populateViewHolder(ThemeListViewHolder holder, ThemeData data, int position) {
        final Context fContext = holder.itemView.getContext();
        final int[] blueShades = {
                ContextCompat.getColor(fContext, R.color.lblue300),
                ContextCompat.getColor(fContext, R.color.lblue500),
                ContextCompat.getColor(fContext, R.color.lblue700)};

        holder.setText(data.getTitle());
        String imageString = data.getImage();
        int imageID = GUIUtils.stringToDrawableID(imageString, fContext);
        holder.setIcon(imageID);
        //holder.setIconColor(blueShades[position%4]);

        String userID = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        List<ImageView> stars = holder.getStars();
        String themeID = data.getId();
        populateStars(themeID, userID, stars);

        final int fPosition = position;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThemeData data = ThemeListAdapter.this.getItem(fPosition);
                int colorPos = fPosition % (blueShades.length);
                int color = blueShades[colorPos];
                Intent intent = new Intent(fContext, ThemeDetails.class);
                intent.putExtra("themeData",data);
                intent.putExtra("backgroundColor",color);
                fContext.startActivity(intent);
            }
        });
    }


    /*@Override
    public void onBindViewHolder(ThemeListViewHolder holder, final int position){
        super.onBindViewHolder(holder, position);
        final DatabaseReference ref = getRef(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ref.removeValue();
            }
        });

    }*/

    @Override
    public void onDataChanged(){
        loading.setVisibility(View.INVISIBLE);

    }

    private void populateStars(String themeID, String userID, final List<ImageView> starList){
        final AchievementStars result = new AchievementStars();
        //if the user is not logged in he should not have any stars
        if (userID.equals("")){
            result.setFirstInstance(false);
            result.setRepeatInstance(false);
            result.setSecondInstance(false);
            GUIUtils.populateStarsImageView(starList,result);
            return;
        }

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(FirebaseDBHeaders.ACHIEVEMENTS+"/"+userID+"/"+themeID);
        //want to update it when we've completed an achievement
        //so listen continuously
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //exists
                if (dataSnapshot.exists()){
                    AchievementStars copy = dataSnapshot.getValue(AchievementStars.class);
                    result.setFirstInstance(copy.getFirstInstance());
                    result.setRepeatInstance(copy.getRepeatInstance());
                    result.setSecondInstance(copy.getSecondInstance());
                } else {
                    //doesn't exist so return no stars
                    result.setFirstInstance(false);
                    result.setRepeatInstance(false);
                    result.setSecondInstance(false);
                }

                GUIUtils.populateStarsImageView(starList,result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                result.setFirstInstance(false);
                result.setRepeatInstance(false);
                result.setSecondInstance(false);
                GUIUtils.populateStarsImageView(starList,result);
            }
        });
    }


}
