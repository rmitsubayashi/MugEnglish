package com.example.ryomi.myenglish.tools;


import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//Util to add theme
public class AddTheme {
    private AddTheme(){}

    public static void run( String title,
                            String category,
                            String description,
                            String image
                            ){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref = db.getReference("themes/");
            if (ref != null){
                String key = ref.push().getKey();
                DatabaseReference uniqueRef = db.getReference("themes/"+key);
                ThemeData themeData = new ThemeData(key,image,title,category,description);
                uniqueRef.setValue(themeData);
            }
        }
    }
}
