package com.linnca.pelicann.gui.widgets;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.db.datawrappers.AchievementStars;
import com.linnca.pelicann.db.datawrappers.LessonData;
import com.linnca.pelicann.gui.LessonList;

import java.util.List;

public class LessonListAdapter
        extends RecyclerView.Adapter<LessonListViewHolder> {
    List<LessonData> data;
    LessonList.LessonListListener listener;

    public LessonListAdapter(List<LessonData> lessons, LessonList.LessonListListener listener){
        this.data = lessons;
        this.listener = listener;
    }

    @Override
    public long getItemId(int position){ return position; }

    @Override
    public int getItemCount(){return data.size();}

    @Override
    public LessonListViewHolder onCreateViewHolder(ViewGroup parent, int viewtype){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflatable_lesson_list_list_item, parent, false);
        return new LessonListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LessonListViewHolder holder, int position) {
        final LessonData lessonData = data.get(position);
        final Context fContext = holder.itemView.getContext();
        final int[] blueShades = {
                ContextCompat.getColor(fContext, R.color.lblue300),
                ContextCompat.getColor(fContext, R.color.lblue500),
                ContextCompat.getColor(fContext, R.color.lblue700)};

        holder.setText(lessonData.getTitle());

        String userID = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        List<ImageView> stars = holder.getStars();
        String lessonKey = lessonData.getKey();
        populateStars(lessonKey, userID, stars);

        final int fPosition = position;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int colorPos = fPosition % (blueShades.length);
                int color = blueShades[colorPos];
                listener.lessonListToLessonDetails(lessonData, color);
            }
        });
    }

    private void populateStars(String lessonID, String userID, final List<ImageView> starList){
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
        DatabaseReference ref = db.getReference(FirebaseDBHeaders.ACHIEVEMENTS+"/"+userID+"/"+lessonID);
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
