package com.linnca.pelicann.mainactivity.widgets;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class GUIUtils {
    //request code for firebase sign in
    public static final int REQUEST_CODE_SIGN_IN = 190;
    //which sign in methods to display to the user
    public static final int SIGN_IN_PROVIDER_ALL = 0;
    //these are for searching via facebook or twitter
    public static final int SIGN_IN_PROVIDER_FACEBOOK = 1;
    public static final int SIGN_IN_PROVIDER_TWITTER = 2;

    private GUIUtils(){}
    public static int stringToDrawableID(String imageString, Context context){
        return context.getResources().getIdentifier(imageString, "drawable",
                context.getApplicationInfo().packageName);
    }

    public static int getDp(int num, Context context){
        return (int)(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, num, context.getResources().getDisplayMetrics()));

    }

    public static Intent getSignInIntent(int provider){
        return AuthUI.getInstance().createSignInIntentBuilder()
                .setProviders(getSelectedProviders(provider))
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .build();
    }

    private static List<AuthUI.IdpConfig> getSelectedProviders(int provider) {
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();

        if (provider == SIGN_IN_PROVIDER_FACEBOOK){
            selectedProviders.add(
                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER)
                            .setPermissions(getFacebookPermissions())
                            .build());
        } else if (provider == SIGN_IN_PROVIDER_TWITTER){
            selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());
        } else if (provider == SIGN_IN_PROVIDER_ALL){
            selectedProviders.add(
                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER)
                            .setPermissions(getFacebookPermissions())
                            .build());
            selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());
            selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
            /*selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                        .setPermissions(getGooglePermissions())
                        .build());*/
        }

        return selectedProviders;
    }

    public static boolean loggedInWithFacebook(){
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            return false;
        } else {
            for (UserInfo info : FirebaseAuth.getInstance().getCurrentUser().getProviderData()){
                if (info.getProviderId().equals(FacebookAuthProvider.PROVIDER_ID))
                    return true;

            }
        }

        return false;
    }

    public static boolean loggedInWithTwitter(){
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            return false;
        } else {
            for (UserInfo info : FirebaseAuth.getInstance().getCurrentUser().getProviderData()){
                if (info.getProviderId().equals(TwitterAuthProvider.PROVIDER_ID))
                    return true;

            }
        }

        return false;
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

    public static void hideKeyboard(View targetViewOfKeyboard){
        if (targetViewOfKeyboard == null){
            return;
        }
        targetViewOfKeyboard.clearFocus();
        InputMethodManager imm = (InputMethodManager) targetViewOfKeyboard.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(targetViewOfKeyboard.getWindowToken(), 0);
        }
    }
}
