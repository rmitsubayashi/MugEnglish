package com.example.ryomi.mugenglish.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.db.FirebaseDBHeaders;
import com.example.ryomi.mugenglish.db.datawrappers.AchievementStars;
import com.example.ryomi.mugenglish.db.datawrappers.ThemeData;
import com.example.ryomi.mugenglish.db.datawrappers.ThemeInstanceData;
import com.example.ryomi.mugenglish.gui.widgets.GUIUtils;
import com.example.ryomi.mugenglish.gui.widgets.ThemeDetailsAdapter;
import com.example.ryomi.mugenglish.questiongenerator.Theme;
import com.example.ryomi.mugenglish.questiongenerator.ThemeFactory;
import com.example.ryomi.mugenglish.questionmanager.QuestionManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ThemeDetails extends AppCompatActivity {
    private ThemeData themeData;
    private Toolbar appBar;
    private RecyclerView list;
    private FloatingActionButton createButton;
    private TextView noItemTextView;
    private ProgressBar loading;

    private FirebaseRecyclerAdapter firebaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_details);
        Intent intent = getIntent();
        if (intent.hasExtra("themeData") && intent.hasExtra("backgroundColor")) {
            //get data
            themeData = (ThemeData) intent.getSerializableExtra("themeData");
            appBar = (Toolbar) findViewById(R.id.theme_details_tool_bar);
            setSupportActionBar(appBar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            list = (RecyclerView) findViewById(R.id.theme_details_instanceList);
            noItemTextView = (TextView) findViewById(R.id.theme_details_no_items);
            loading = (ProgressBar) findViewById(R.id.theme_details_loading);

            addActionListeners();
            populateData();
            int color = intent.getIntExtra("backgroundColor", 0);
            setThemeColor(color);
            adjustLayout();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.theme_details_app_bar_menu, menu);
        final MenuItem star1 = menu.findItem(R.id.theme_details_star1);
        final MenuItem star2 = menu.findItem(R.id.theme_details_star2);
        final MenuItem star3 = menu.findItem(R.id.theme_details_star3);
        final List<MenuItem> stars = new ArrayList<>(3);
        stars.add(star1);
        stars.add(star2);
        stars.add(star3);
        final AchievementStars result = new AchievementStars();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            result.setFirstInstance(false);
            result.setRepeatInstance(false);
            result.setSecondInstance(false);
            GUIUtils.populateStarsMenu(stars, result, this);
            return true;
        }
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(FirebaseDBHeaders.ACHIEVEMENTS + "/"
                + userID + "/" + themeData.getId());
        //want to update it when we've completed an achievement
        //so listen continuously
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //exists
                if (dataSnapshot.exists()) {
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

                GUIUtils.populateStarsMenu(stars, result, ThemeDetails.this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                result.setFirstInstance(false);
                result.setRepeatInstance(false);
                result.setSecondInstance(false);
                GUIUtils.populateStarsMenu(stars, result, ThemeDetails.this);
            }
        });

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.theme_details_item_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ThemeInstanceData data = ((ThemeDetailsAdapter)list.getAdapter()).getLongClickPosition();
        switch (item.getItemId()) {
            case R.id.theme_details_item_menu_more_info:
                //open fragment?
                return true;
            case R.id.theme_details_item_menu_delete:
                removeInstance(data);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void populateData(){
        /*LinearLayout titleLayout =
                (LinearLayout) getLayoutInflater().inflate(R.layout.inflatable_theme_details_title, list, false);
        TextView titleTextView = (TextView) titleLayout.findViewById(R.id.theme_details_title);
        titleTextView.setText(themeData.getTitle());
        ImageView iconView = (ImageView)titleLayout.findViewById(R.id.theme_details_icon);
        int imageID = GUIUtils.stringToDrawableID(themeData.getImage(),this);
        iconView.setImageResource(imageID);*/

        //grab list of instances
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference(
                    FirebaseDBHeaders.THEME_INSTANCES + "/"+userID+"/"+themeData.getId());
            list.setLayoutManager(new LinearLayoutManager(this));
            firebaseAdapter = new ThemeDetailsAdapter(ref2, noItemTextView, loading);
            //when we create a new instance, remove the progress spinner
            firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    createButton.setIndeterminate(false);
                    createButton.setEnabled(true);
                }
            });



            list.setAdapter(firebaseAdapter);

            registerForContextMenu(list);

        }

    }


    private void setThemeColor(int color){
        //background for whole activity
        findViewById(R.id.activity_theme_details).setBackgroundColor(color);
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
    }

    private void addActionListeners(){
        createButton = (FloatingActionButton)findViewById(R.id.theme_details_add);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewInstance();
            }
        });

    }

    private void createNewInstance(){
        //default is false (nothing shown since progress is 0%)
        createButton.setIndeterminate(true);
        createButton.setEnabled(false);
        //load theme class
        Theme theme = ThemeFactory.createTheme(themeData);
        //first part connects to Firebase
        // thus running on the main UI thread.
        //the second part (connecting to wikidata)
        // runs on an async task
        theme.createInstance();

        //the list listens for inserts and removes the loading spinner
    }

    private void removeInstance(ThemeInstanceData data){
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return;

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = db.getReference(FirebaseDBHeaders.THEME_INSTANCES + "/" +
                userID + "/" + data.getThemeId() + "/" + data.getId());
        ref.removeValue();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (firebaseAdapter != null)
            firebaseAdapter.cleanup();
    }

}
