package com.example.ryomi.mugenglish.tools;


import com.example.ryomi.mugenglish.db.FirebaseDBHeaders;
import com.example.ryomi.mugenglish.db.datawrappers.ThemeData;
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
            DatabaseReference ref = db.getReference(FirebaseDBHeaders.THEMES);
            if (ref != null){
                String key = ref.push().getKey();
                DatabaseReference uniqueRef = db.getReference(FirebaseDBHeaders.THEMES + "/" + key);
                ThemeData themeData = new ThemeData(key,image,title,category,description);
                uniqueRef.setValue(themeData);
            }
        }
    }

    public static void runAll(){
        run("意外！あの人、こんなスポーツもできるんだ","1.8.1","desc","sports");
        run("B型？絶対A型だと思ってた","1.3.2","desc","health");
        run("あの二人、同じ学校に通ってたんだ","2.0","desc","education");
        run("へー、あの人はこの学校出身なんだ","2.0","desc","education");
        run("この外国人、南アフリカ人なんだ","1.1.1","desc","country");
        run("職業っていっぱいあるんだね","1.1.1","desc","person");
        run("スポーツやりたい！","1.2.1","desc","sports");
        run("国、県、市町村","1.2.2","desc","city");
        run("男性だってとっくに知ってるわ","1.1.1","desc","person");
        run("こんなに歳をとってるんだ","1.3.1","desc","person");
        AddTheme.run("緊急時には110番","1.4.2","desc","health");

    }
}
