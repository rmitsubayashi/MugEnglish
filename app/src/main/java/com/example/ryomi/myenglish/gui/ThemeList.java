package com.example.ryomi.myenglish.gui;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.AchievementStars;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.gui.widgets.DynamicPaddedCell;
import com.example.ryomi.myenglish.gui.widgets.ThemeCellImageView;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/*
* The list is synced with the db
* so if I change the data in the db
* it will automatically update here.
* Will this take up too much network data usage?
* */
public class ThemeList extends AppCompatActivity {
    private FirebaseListAdapter<ThemeData> firebaseAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_list);
        final String[] blueShades = {"#19B5FE","#3498DB","#6BB9F0","#3A539B"};

        final GridView gridview = (GridView) findViewById(R.id.grid);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("themes");
        firebaseAdapter = new FirebaseListAdapter<ThemeData>(
                this, ThemeData.class, R.layout.inflatable_theme_list_list_item, ref
        ) {
            @Override
            protected void populateView(View v, ThemeData data, int position) {
                DynamicPaddedCell cell = (DynamicPaddedCell)v;
                //get stars
                List<ImageView> starList = new ArrayList<>();
                starList.add((ImageView)cell.findViewById(R.id.themeGrid_star1));
                starList.add((ImageView)cell.findViewById(R.id.themeGrid_star2));
                starList.add((ImageView)cell.findViewById(R.id.themeGrid_star3));

                //can be empty
                String userID = "";
                if (FirebaseAuth.getInstance().getCurrentUser() != null){
                    userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
                ThemeList.this.populateStars(data.getId(), userID, starList);

                //get image
                String imageString = data.getImage();
                int imageID = GUIUtils.stringToDrawableID(imageString, ThemeList.this);
                ThemeCellImageView mainImageView = (ThemeCellImageView) cell.findViewById(R.id.themeGrid_mainImage);
                mainImageView.setImageResource(imageID);

                //get text
                TextView textView = (TextView) cell.findViewById(R.id.themeGrid_text);
                textView.setText(data.getCategory());

                //set background
                cell.setBackgroundColor(Color.parseColor(blueShades[position%4]));
            }
        };

        gridview.setAdapter(firebaseAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                FirebaseListAdapter<ThemeData> adapter = (FirebaseListAdapter)gridview.getAdapter();
                ThemeData data = adapter.getItem(position);
                String idString = data.getId();
                //get view color
                String color = blueShades[position%4];

                Intent intent = new Intent(ThemeList.this, ThemeDetails.class);
                intent.putExtra("id",idString);
                intent.putExtra("backgroundColor",color);
                ThemeList.this.startActivity(intent);
            }
        });


    }

    private void populateStars(String themeID, String userID, final List<ImageView> starList){
        final AchievementStars result = new AchievementStars();
        //if the user is not logged in he should not have any stars
        if (userID.equals("")){
            result.setFirstInstance(false);
            result.setRepeatInstance(false);
            result.setSecondInstance(false);
            GUIUtils.populateStars(starList,result);
            return;
        }

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("achievements/"+userID+"/"+themeID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
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

                GUIUtils.populateStars(starList,result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                result.setFirstInstance(false);
                result.setRepeatInstance(false);
                result.setSecondInstance(false);
                GUIUtils.populateStars(starList,result);
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        firebaseAdapter.cleanup();
    }
}
