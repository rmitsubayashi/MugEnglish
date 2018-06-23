package com.linnca.pelicann.db;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import java.util.concurrent.atomic.AtomicBoolean;

import pelicann.linnca.com.corefunctionality.db.DBConnectionResultListener;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;

public class AndroidNetworkConnectionChecker implements NetworkConnectionChecker{
    //to detect connection
    private Context context;
    //for UI
    private AtomicBoolean dataRetrievedFromDB;
    //if we need to stop listening without updating any UI
    // (for example when the user leaves the screen
    private AtomicBoolean needToStop = new AtomicBoolean(false);

    public AndroidNetworkConnectionChecker(Context context){
        super();
        this.context = context;
    }

    private class NetworkConnectionRunnable implements Runnable {
        //for network connection checker
        private DBConnectionResultListener connectionResultListener;

        NetworkConnectionRunnable(final DBConnectionResultListener uiListener){
            //we need to make a new one so we can add slow connection functionality
            // where if we are connected but haven't gotten a response yet,
            // we should try again
            this.connectionResultListener = new DBConnectionResultListener() {
                @Override
                public void onNoConnection() {
                    uiListener.onNoConnection();
                }
                @Override
                public void onSlowConnection(){
                    uiListener.onSlowConnection();
                    //wait again
                    new Handler().postDelayed(NetworkConnectionRunnable.this, 1000);
                }
            };
        }

        public void run(){
            if (needToStop.get()){
                return;
            }
            //after waiting, if we haven't gotten data
            // from the database
            if (!dataRetrievedFromDB.get()) {
                //check for a connection.
                if (isConnected()){
                    connectionResultListener.onSlowConnection();
                } else {
                    connectionResultListener.onNoConnection();
                }
            }
        }
    }

    @Override
    public void checkConnection(DBConnectionResultListener connectionResultListener, AtomicBoolean dataRetrievedFromDB){
        this.dataRetrievedFromDB = dataRetrievedFromDB;
        //context is here instead of in the initialization -> class variable
        // because contexts aren't serializable
        new Handler().postDelayed(new NetworkConnectionRunnable(connectionResultListener), 1000);
    }

    @Override
    public void stop(){
        needToStop.set(true);
    }

    private boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null)
            networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
