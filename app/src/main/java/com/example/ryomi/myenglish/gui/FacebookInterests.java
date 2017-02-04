package com.example.ryomi.myenglish.gui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.userinterestcontrols.FacebookInterestFinder;

public class FacebookInterests extends AppCompatActivity {
    private TextView progressStringTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_interests);

        Intent intent = new Intent(this, FacebookInterestFinder.class);
        intent.putExtra("depth", FacebookInterestFinder.DEEP_SEARCH);
        startService(intent);

        ResponseReceiver receiver = new ResponseReceiver();
        IntentFilter filterProgressString = new IntentFilter(
                FacebookInterestFinder.BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_STRING);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filterProgressString);

        //save all views we will update
        progressStringTV = (TextView)findViewById(R.id.facebook_interests_progress_string);
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
            String stringToDisplay = intent.getStringExtra(
                    FacebookInterestFinder.BROADCAST_FACEBOOKINTERESTFINDER_PROGRESS_STRING);
            progressStringTV.setText(stringToDisplay);
            progressStringTV.invalidate();
        }
    }
}
