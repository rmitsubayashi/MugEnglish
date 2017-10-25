package com.linnca.pelicann.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.linnca.pelicann.R;
import com.linnca.pelicann.lessonlist.LessonList;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.userinterestcontrols.StarterPacks;
import com.linnca.pelicann.userinterestcontrols.UserInterestAdder;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import java.util.ArrayList;
import java.util.List;

public class Onboarding extends AppCompatActivity {
    private int pageIndex = 0;
    private final int maxPageIndex = 2;
    private Button nextButton;
    private Button finishButton;
    private final List<ImageView> indicators = new ArrayList<>(3);
    private ProgressBar loading;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPagerCustomDuration viewPager;
    private OnboardingPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        adapter = new OnboardingPagerAdapter(getSupportFragmentManager(), maxPageIndex+1);

        // Set up the ViewPager with the sections adapter.
        viewPager = findViewById(R.id.onboarding_viewpager);
        nextButton = findViewById(R.id.onboarding_next);
        finishButton = findViewById(R.id.onboarding_finish);
        loading = findViewById(R.id.onboarding_loading);
        viewPager.setAdapter(adapter);
        ImageView indicator1 = findViewById(R.id.onboarding_indicator1);
        ImageView indicator2 = findViewById(R.id.onboarding_indicator2);
        ImageView indicator3 = findViewById(R.id.onboarding_indicator3);
        indicators.add(indicator1);
        indicators.add(indicator2);
        indicators.add(indicator3);

        setActionListeners();

    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicators.size(); i++) {
            indicators.get(i).setColorFilter(
                    i == position ?
                    ContextCompat.getColor(this, R.color.orange500):
                    ContextCompat.getColor(this, R.color.gray500)
            );
        }
    }

    private void setActionListeners(){

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                pageIndex = position;
                updateIndicators(pageIndex);

                nextButton.setVisibility(position == maxPageIndex ? View.GONE : View.VISIBLE);
                finishButton.setVisibility(position == maxPageIndex ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //shouldn't happen since we are hiding this when on the last page,
                //but just in case
                if (viewPager.getCurrentItem() == maxPageIndex){
                    toApp();
                } else {
                    //transition is too fast so slow it down
                    viewPager.setScrollDurationFactor(5);
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                    //this also affects the speed of manual swipe (which is normal speed)
                    //so set it back to default
                    viewPager.setScrollDurationFactor(1);
                }
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toApp();
            }
        });

    }

    private void addStarterPack(int starterPackSelection){
        List<WikiDataEntryData> list = StarterPacks.getStarterPack(starterPackSelection);
        UserInterestAdder adder = new UserInterestAdder();
        adder.justAdd(list);
    }

    private void markOnboardingCompleted(){
        //set preference
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(
                Onboarding.this.getApplicationContext());
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean(getResources().getString(R.string.preferences_first_time_key), false);
        editor.apply();
    }

    private void showLoading(){
        viewPager.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        finishButton.setTextColor(ContextCompat.getColor(this, R.color.gray500));
    }

    private void toApp(){
        int startingPackSelection = adapter.getStarterPackSelection();
        if (startingPackSelection == -1){
            return;
        }

        markOnboardingCompleted();

        showLoading();

        FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                String newUserID = authResult.getUser().getUid();
                addStarterPack(adapter.getStarterPackSelection());
                Intent intent = new Intent(Onboarding.this, MainActivity.class);
                startActivity(intent);
                Onboarding.this.finish();
            }
        });

    }

}
