package com.example.ryomi.myenglish.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeInstanceData;
import com.example.ryomi.myenglish.gui.widgets.GUIUtils;
import com.example.ryomi.myenglish.questiongenerator.Theme;
import com.example.ryomi.myenglish.questiongenerator.ThemeFactory;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ThemeDetails extends AppCompatActivity {
    private ThemeData themeData;
    private View selectedInstanceLayout = null;
    private int themeColor;
    private LinearLayout instanceList;
    private Toolbar appBar;
    private ProgressBar loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_details);
        Intent intent = getIntent();
        if (intent.hasExtra("themeData") && intent.hasExtra("backgroundColor")){
            //get data
            themeData = (ThemeData)intent.getSerializableExtra("themeData");
            appBar = (Toolbar)findViewById(R.id.theme_details_tool_bar);
            setSupportActionBar(appBar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            populateData();
            int color = intent.getIntExtra("backgroundColor",0);
            setThemeColor(color);
            adjustLayout();
            addActionListeners();

        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.theme_details_item_menu, menu);
        ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView = v;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        item.
        ThemeInstanceData data = (ThemeInstanceData)view.getTag();
        switch (item.getItemId()) {
            case R.id.theme_details_item_menu_more_info:
                Toast.makeText(this, data.getTopics().get(0), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.theme_details_item_menu_delete:
                //deleteInstance(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void populateData(){
        TextView titleTextView = (TextView) ThemeDetails.this.findViewById(R.id.theme_details_title);
        titleTextView.setText(themeData.getTitle());
        ImageView imageView = (ImageView) findViewById(R.id.theme_details_icon);
        int imageID = GUIUtils.stringToDrawableID(themeData.getImage(),this);
        imageView.setImageResource(imageID);

        TextView descriptionTextView = (TextView) ThemeDetails.this.findViewById(R.id.theme_details_description);
        if (Build.VERSION.SDK_INT >= 24) {
            descriptionTextView.setText(Html.fromHtml(themeData.getDescription(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            descriptionTextView.setText((Html.fromHtml(themeData.getDescription())));
        }

        loadingSpinner = (ProgressBar) findViewById(R.id.theme_details_loading_spinner);
        //grab list of instances
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            instanceList = (LinearLayout) findViewById(R.id.theme_details_instanceList);
            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("themeInstances/"+userID+"/"+themeData.getId());
            ValueEventListener getThemeInstancesData = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    loadingSpinner.setVisibility(View.GONE);
                    long newRowCt = dataSnapshot.getChildrenCount();
                    int currentRowCt = instanceList.getChildCount();
                    List<ThemeInstanceData> newRows = new ArrayList<>();
                    //save list first
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        newRows.add(child.getValue(ThemeInstanceData.class));
                    }
                    //first row
                    if (currentRowCt == 0) {
                        for (ThemeInstanceData data : newRows){
                            RelativeLayout row = createInstanceItem(data);
                            instanceList.addView(row);
                        }
                    }

                    //adding row
                    else if (newRowCt > currentRowCt){
                        addRows(newRows);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            ref2.orderByChild("createdTimestamp").addValueEventListener(getThemeInstancesData);

        } else //not user
        {
            loadingSpinner.setVisibility(View.GONE);
        }
    }

    private void addRows(List<ThemeInstanceData> newData){
        //expensive?
        Set<ThemeInstanceData> childData = new HashSet<>();
        int childCt = instanceList.getChildCount();
        for (int i=0; i<childCt; i++){
            ThemeInstanceData data = (ThemeInstanceData)instanceList.getChildAt(i).getTag();
            childData.add(data);
        }

        for (ThemeInstanceData data : newData){
            if (!childData.contains(data)){
                instanceList.addView(createInstanceItem(data));
            }
        }

    }

    private RelativeLayout createInstanceItem(ThemeInstanceData data){
        RelativeLayout row = (RelativeLayout) ThemeDetails.this.getLayoutInflater().inflate(
                R.layout.inflatable_theme_details_instance_list_item, instanceList, false
        );

        TextView topicsTextView = (TextView)row.findViewById(R.id.theme_details_item_topics);
        String topics = "";
        for (String topic : data.getTopics()){
            topics += topic + " + ";
        }
        topics = topics.substring(0,topics.length()-3);
        topicsTextView.setText(topics);

        DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.JAPAN);
        String dateString = dateFormat.format(new Date(data.getCreatedTimestamp()));
        TextView dateCreatedTextView = (TextView)row.findViewById(R.id.theme_details_item_date_created);
        String createdLabel = getResources().getString(R.string.theme_details_created);
        dateCreatedTextView.setText(createdLabel + ": " + dateString);
        dateCreatedTextView.setTextColor(themeColor);
        //save data in the row so we can retrieve it later
        row.setTag(data);
        //the row acts like a button group

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateRow(view);
            }
        });

        registerForContextMenu(row);

        return row;
    }


    private void setThemeColor(int color){
        themeColor = color;
        //background for whole activity
        ScrollView activity = (ScrollView) findViewById(R.id.activity_theme_details);
        activity.setBackgroundColor(color);
        //status bar (post-lollipop)
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
        //action bar
        appBar.setBackgroundColor(color);

    }

    private void adjustLayout(){

        //only show description if the user has enabled it (default is enabled)

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(ThemeDetails.this.getApplicationContext());
        boolean showDescription = sharedPreferences.getBoolean
                (getString(R.string.preferences_questions_descriptionBeforeQuestions_key), true);
        if (!showDescription) {
            TextView descriptionTextView = (TextView) ThemeDetails.this.findViewById(R.id.theme_details_description);
            descriptionTextView.setVisibility(View.GONE);
        }
    }

    private void addActionListeners(){

        Button playButton = (Button) findViewById(R.id.theme_details_playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startQuestions();
            }
        });

        Button createButton = (Button) findViewById(R.id.theme_details_newInstance);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewInstance();
            }
        });


    }

    private void activateRow(View activateView){
        //the row is already activated
        //then deactivate that row
        if(activateView.equals(selectedInstanceLayout)) {
            deactivateSelectedRow();
            return;
        }

        deactivateSelectedRow();
        activateView.setBackgroundColor(themeColor);
        int white = ContextCompat.getColor(this, R.color.white);
        TextView createdTV = (TextView)activateView.findViewById(R.id.theme_details_item_date_created);
        createdTV.setTextColor(white);
        selectedInstanceLayout = activateView;
    }

    private void deactivateSelectedRow(){
        //there is no active row
        if (selectedInstanceLayout == null)
            return;

        int white = ContextCompat.getColor(this, R.color.white);
        selectedInstanceLayout.setBackgroundColor(white);
        TextView createdTV = (TextView)selectedInstanceLayout.findViewById(R.id.theme_details_item_date_created);
        createdTV.setTextColor(themeColor);

        selectedInstanceLayout = null;
    }

    private void startQuestions(){
        //if it's not checked the old instance button has to be checked
        if (selectedInstanceLayout != null){
            ThemeInstanceData data = (ThemeInstanceData)selectedInstanceLayout.getTag();
            QuestionManager manager = QuestionManager.getInstance();
            manager.startQuestions(data, this);
        }
        else {
            Toast.makeText(this,R.string.theme_details_did_not_choose_instance,Toast.LENGTH_SHORT).show();
        }
    }

    private void createNewInstance(){
        loadingSpinner.setVisibility(View.VISIBLE);
        Theme theme = ThemeFactory.createTheme(themeData);
        //first part connects to Firebase
        // thus running on the main UI thread.
        //the second part (connecting to wikidata)
        // runs on an async task
        theme.createInstance();
    }

}
