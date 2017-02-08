package com.example.ryomi.myenglish.gui.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.AchievementStars;
import com.example.ryomi.myenglish.gui.ThemeList;
import com.example.ryomi.myenglish.gui.UserInterests;
import com.example.ryomi.myenglish.gui.UserProfile;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class GUIUtils {
    //request code for firebase sign in
    public static int REQUEST_CODE_SIGN_IN = 190;
    //to save navigation bar state across activities
    private static int BOTTOM_NAVIGATION_BAR_STATE = 0;

    private GUIUtils(){}
    public static int stringToDrawableID(String imageString, Context context){
        int imageID = context.getResources().getIdentifier(imageString, "drawable",
                context.getApplicationInfo().packageName);
        return imageID;
    }

    public static void populateStarsImageView(List<ImageView> imageViews, AchievementStars achievementStars){
        int starCt = imageViews.size();
        List<Boolean> starsEnabled = new ArrayList<>();
        starsEnabled.add(achievementStars.getFirstInstance());
        starsEnabled.add(achievementStars.getRepeatInstance());
        starsEnabled.add(achievementStars.getSecondInstance());

        for(int i=0; i<starCt; i++){
            ImageView imageView = imageViews.get(i);
            Boolean starEnabled = starsEnabled.get(i);
            //should not happen but just in case
            if (starEnabled == null){
                imageView.setImageResource(R.drawable.star_disabled);
            } else if (starEnabled){
                imageView.setImageResource(R.drawable.star);
            } else {
                imageView.setImageResource(R.drawable.star_disabled);
            }
        }
    }

    //with the menu we are only updating one set of stars.
    //to make it visually pleasing, add a delay between each star update.
    //this creates a flow? kinda effect
    public static void populateStarsMenu(List<MenuItem> menuItems, AchievementStars achievementStars, final Activity activity){
        int starCt = menuItems.size();
        List<Boolean> starsEnabled = new ArrayList<>();
        starsEnabled.add(achievementStars.getFirstInstance());
        starsEnabled.add(achievementStars.getRepeatInstance());
        starsEnabled.add(achievementStars.getSecondInstance());

        int delayMultiplier = 1;
        for(int i=0; i<starCt; i++){
            final MenuItem item = menuItems.get(i);
            Boolean starEnabled = starsEnabled.get(i);
            //default icon is disabled
            //so just change if necessary
            if (starEnabled) {
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        ImageView iv = (ImageView)activity.getLayoutInflater().inflate(R.layout.inflatable_star, null);
                        Animation rotation = AnimationUtils.loadAnimation(activity, R.anim.star_rotation);
                        iv.startAnimation(rotation);
                        item.setActionView(iv);
                    }
                },300 * delayMultiplier);
                delayMultiplier++;
            }
        }
    }

    public static void prepareBottomNavigationView(Activity activity, BottomNavigationView nav){
        final Activity fActivity = activity;

        Menu menu = nav.getMenu();
        for (int i=0; i<menu.size(); i++){
            menu.getItem(i).setChecked(false);
        }
        menu.getItem(BOTTOM_NAVIGATION_BAR_STATE).setChecked(true);

        nav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Intent intent = null;
                        switch (item.getItemId()) {

                            case R.id.bottom_navigation_study:
                                BOTTOM_NAVIGATION_BAR_STATE = 0;
                                intent = new Intent(fActivity, ThemeList.class);
                                break;
                            case R.id.bottom_navigation_user_profile:
                                BOTTOM_NAVIGATION_BAR_STATE = 1;
                                intent = new Intent(fActivity, UserProfile.class);
                                break;
                            case R.id.bottom_navigation_favorites:
                                BOTTOM_NAVIGATION_BAR_STATE = 2;
                                intent = new Intent(fActivity, UserInterests.class);
                                break;
                        }

                        fActivity.startActivity(intent);
                        fActivity.finish();
                        fActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        return true;
                    }
                });
    }

    public static Intent getSignInIntent(){
        return AuthUI.getInstance().createSignInIntentBuilder()
                .setProviders(getSelectedProviders())
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .build();
    }

    private static List<AuthUI.IdpConfig> getSelectedProviders() {
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();

        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());


        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());

        selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER)
                        .setPermissions(getFacebookPermissions())
                        .build());

        /*selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                        .setPermissions(getGooglePermissions())
                        .build());*/
        return selectedProviders;
    }

    private static List<String> getFacebookPermissions(){
        List<String> result = new ArrayList<>();
        result.add("public_profile");
        result.add("user_likes");
        result.add("user_hometown");
        result.add("user_games_activity");
        result.add("user_events");
        result.add("user_education_history");
        result.add("user_birthday");
        result.add("user_location");
        result.add("user_religion_politics");
        result.add("user_tagged_places");
        result.add("user_work_history");
        result.add("user_actions.video");
        result.add("user_actions.music");
        result.add("user_actions.books");
        return result;
    }
}
