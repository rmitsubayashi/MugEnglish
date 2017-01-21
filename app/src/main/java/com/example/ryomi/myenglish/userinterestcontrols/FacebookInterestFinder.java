package com.example.ryomi.myenglish.userinterestcontrols;

import android.content.Context;

import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.*;


public class FacebookInterestFinder {
    private final String fields = "birthday,education,hometown,location,favorite_athletes,favorite_teams,video.watches,video.wants_to_watch";

    public FacebookInterestFinder(Context context){
        FacebookSdk.sdkInitialize(context);
    }

    public List<WikiDataEntryData> findUserInterests() throws Exception{

        List<WikiDataEntryData> result = new ArrayList<>();
        String urlString = formatURL();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-agent", "Mozzila/5.0");

        InputStream is = conn.getInputStream();
        //convert input stream to string
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String str = s.hasNext() ? s.next() : "";

        JSONObject json = new JSONObject(str);
        System.out.println(json.get("id"));

        return result;
    }

    private String formatURL(){
        List<? extends UserInfo> list = FirebaseAuth.getInstance().getCurrentUser().getProviderData();
        AccessToken.refreshCurrentAccessTokenAsync();
        String accessToken = AccessToken.getCurrentAccessToken().getToken();

        String url = "https://graph.facebook.com/me?fields="+fields+"&access_token="+accessToken;
        return url;
    }


}
