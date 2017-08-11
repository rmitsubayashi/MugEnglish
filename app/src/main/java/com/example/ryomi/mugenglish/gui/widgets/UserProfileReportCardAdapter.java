package com.example.ryomi.mugenglish.gui.widgets;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.db.FirebaseDBHeaders;
import com.example.ryomi.mugenglish.db.datawrappers.LessonData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserProfileReportCardAdapter extends RecyclerView.Adapter<UserProfileReportCardViewHolder> {
    private List<UserProfileReportCardData> rowData;
    private LayoutInflater layoutInflater;
    public UserProfileReportCardAdapter(List<UserProfileReportCardData> rowData, Context context){
        this.rowData = rowData;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void onBindViewHolder(final UserProfileReportCardViewHolder holder, int position){
        UserProfileReportCardData data = rowData.get(position);
        holder.setRecordCtTextView(data.getRecordCt());
        holder.setAccuracyTextView(data.getAccuracy());
        holder.setInstanceCtTextView(data.getInstanceCt());

        if (data.getThemeName().equals("")) {
            //fetch theme name
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                    FirebaseDBHeaders.LESSONS + "/" + data.getThemeID()
            );
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    LessonData lessonData = dataSnapshot.getValue(LessonData.class);
                    holder.setTheme(lessonData.getTitle());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            holder.setTheme(data.getThemeName());
        }
    }

    @Override
    public int getItemCount(){
        return rowData.size();
    }

    @Override
    public UserProfileReportCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int position){
        return new UserProfileReportCardViewHolder(
          layoutInflater.inflate(R.layout.inflatable_user_profile_report_card_item, viewGroup, false)
        );
    }
}
