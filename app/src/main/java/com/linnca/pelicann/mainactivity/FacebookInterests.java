package com.linnca.pelicann.mainactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.widgets.GUIUtils;
import com.linnca.pelicann.userinterestcontrols.FacebookInterestFinder;

public class FacebookInterests extends AppCompatActivity {
    /*
    private static final String TAG = "FacebookInterests";
    private TextView progressStringTV;
    private TextView progressWordTV;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_interests);

        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivityForResult(
                    GUIUtils.getSignInIntent(GUIUtils.SIGN_IN_PROVIDER_FACEBOOK),
                    GUIUtils.REQUEST_CODE_SIGN_IN
            );
        } else {
            if (!GUIUtils.loggedInWithFacebook()){
                startActivityForResult(
                        GUIUtils.getSignInIntent(GUIUtils.SIGN_IN_PROVIDER_FACEBOOK),
                        GUIUtils.REQUEST_CODE_SIGN_IN
                );
            }
        }

        setButtonActionListener();

        Intent intent = new Intent(this, FacebookInterestFinder.class);
        intent.putExtra("depth", FacebookInterestFinder.DEEP_SEARCH);
        startService(intent);

        ResponseReceiver receiver = new ResponseReceiver();
        IntentFilter filterProgressString = new IntentFilter(
                FacebookInterestFinder.BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_STRING);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filterProgressString);
        IntentFilter filterProgressWord = new IntentFilter(
                FacebookInterestFinder.BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_WORD);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filterProgressWord);
        IntentFilter filterProgressPercent = new IntentFilter(
                FacebookInterestFinder.BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_PERCENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filterProgressPercent);

        //save all views we will update
        progressStringTV = findViewById(R.id.facebook_interests_progress_string);
        progressWordTV = findViewById(R.id.facebook_interests_progress_word);
        progressBar = findViewById(R.id.facebook_interests_progress_percent);
    }

    private void setButtonActionListener(){

    }

    // Broadcast receiver for receiving status updates from the IntentService
    private class ResponseReceiver extends BroadcastReceiver
    {
        // Prevents instantiation
        private ResponseReceiver() {
        }
        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    FacebookInterestFinder.BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_STRING)) {
                String stringToDisplay = intent.getStringExtra(
                        FacebookInterestFinder.BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_STRING);
                progressStringTV.setText(stringToDisplay);
            } else if (intent.getAction().equals(
                    FacebookInterestFinder.BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_WORD)){
                String stringToDisplay = intent.getStringExtra(
                        FacebookInterestFinder.BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_WORD);
                progressWordTV.setText(stringToDisplay);
            } else if (intent.getAction().equals(
                    FacebookInterestFinder.BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_PERCENT)){
                int percent = intent.getIntExtra(
                        FacebookInterestFinder.BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_PERCENT, 0);

                ProgressBarAnimation animation = new ProgressBarAnimation(progressBar, (float)progressBar.getProgress(),
                        (float)percent);
                animation.setDuration(1000);
                progressBar.startAnimation(animation);
            }
        }
    }

    private class ProgressBarAnimation extends Animation {
        private ProgressBar progressBar;
        private float from;
        private float  to;

        public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
        }

    }*/
}
