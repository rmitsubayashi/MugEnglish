package com.linnca.pelicann.onboarding;

import android.animation.ArgbEvaluator;
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

import com.linnca.pelicann.R;
import com.linnca.pelicann.lessonlist.LessonList;

import java.util.ArrayList;
import java.util.List;

public class Onboarding extends AppCompatActivity implements OnboardingNextListener {
    private int pageIndex = 0;
    private final int maxPageIndex = 3;
    private Button finishButton;
    private final List<ImageView> indicators = new ArrayList<>(3);

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
        adapter = new OnboardingPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = findViewById(R.id.onboarding_viewpager);
        finishButton = findViewById(R.id.onboarding_finish);
        viewPager.setAdapter(adapter);
        ImageView indicator1 = findViewById(R.id.onboarding_indicator1);
        ImageView indicator2 = findViewById(R.id.onboarding_indicator2);
        ImageView indicator3 = findViewById(R.id.onboarding_indicator3);
        ImageView indicator4 = findViewById(R.id.onboarding_indicator4);
        indicators.add(indicator1);
        indicators.add(indicator2);
        indicators.add(indicator3);
        indicators.add(indicator4);

        setActionListeners();

    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicators.size(); i++) {
            indicators.get(i).setBackgroundResource(
                    i == position ? R.drawable.orange_circle : R.drawable.white_circle
            );
        }
    }

    private void setActionListeners(){
        //for changing background color smoothly
        final ArgbEvaluator evaluator = new ArgbEvaluator();
        final int color1 = ContextCompat.getColor(this, R.color.lblue900);
        final int color2 = ContextCompat.getColor(this, R.color.lblue700);
        final int color3 = ContextCompat.getColor(this, R.color.lblue500);
        final int color4 = ContextCompat.getColor(this, R.color.lblue300);
        final int[] colorList = new int[]{color1, color2, color3, color4};

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int colorUpdate = (Integer) evaluator.evaluate(positionOffset, colorList[position], colorList[position == maxPageIndex ? position : position + 1]);
                viewPager.setBackgroundColor(colorUpdate);
            }
            @Override
            public void onPageSelected(int position) {
                pageIndex = position;
                updateIndicators(pageIndex);
                viewPager.setBackgroundColor(colorList[position]);

                //nextButton.setVisibility(position == maxPageIndex ? View.GONE : View.VISIBLE);
                finishButton.setVisibility(position == maxPageIndex ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Onboarding.this, LessonList.class);
                startActivity(intent);
                //set preference
                /*SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(
                        Onboarding.this.getApplicationContext());
                SharedPreferences.Editor editor = preference.edit();
                editor.putBoolean(getString(R.string.preferences_first_time_key), false);*/
                Onboarding.this.finish();
            }
        });

    }

    @Override
    public void nextScreen(){
        //if this is the last page, go on to the theme list page
        if (pageIndex == maxPageIndex){
            nextPage();
            return;
        }

        nextFragment();
    }

    //same but with an attachment and
    //destroy next view so we can re-instantate it with the new bundle
    @Override
    public void nextScreen(Bundle bundle){
        //if this is the last page, go on to the theme list page
        if (pageIndex == maxPageIndex){
            nextPage();
            return;
        }
        adapter.updateBundle(bundle);
        nextFragment();
    }

    private void nextFragment(){
        pageIndex = pageIndex + 1;
        adapter.updateMaxPageCount(pageIndex);
        //transition is too fast so slow it down
        viewPager.setScrollDurationFactor(5);
        viewPager.setCurrentItem(pageIndex);
        //this also affects the speed of manual swipe (which is normal)
        //so set it back to default
        viewPager.setScrollDurationFactor(1);
    }

    private void nextPage(){
        //set preference
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(
                Onboarding.this.getApplicationContext());
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean(getResources().getString(R.string.preferences_first_time_key), false);
        editor.apply();
        Intent intent = new Intent(Onboarding.this, LessonList.class);
        startActivity(intent);
        Onboarding.this.finish();
    }

}
