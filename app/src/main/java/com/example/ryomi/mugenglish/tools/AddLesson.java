package com.example.ryomi.mugenglish.tools;


import com.example.ryomi.mugenglish.db.FirebaseDBHeaders;
import com.example.ryomi.mugenglish.db.datawrappers.LessonData;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_is_DEMONYM;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_plays_SPORT;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_possessive_blood_type_is_BLOODTYPE;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_went_to_SCHOOL_So_did_NAME2;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_went_to_SCHOOL_from_START_to_END;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//Util to add theme
public class AddLesson {
    private AddLesson(){}

    public static void run(
                            String key,
                            String title,
                            String category,
                            String description,
                            String image
                            ){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref = db.getReference(FirebaseDBHeaders.LESSONS);
            if (ref != null){
                DatabaseReference uniqueRef = db.getReference(FirebaseDBHeaders.LESSONS + "/" + key);
                LessonData lessonData = new LessonData(key,image,title,category,description);
                uniqueRef.setValue(lessonData);
            }
        }
    }

    public static void runAll(){
        run(NAME_plays_SPORT.KEY,
                NAME_plays_SPORT.TITLE,
                "1.8.1", "desc", "sports");
        run(NAME_possessive_blood_type_is_BLOODTYPE.KEY,
                NAME_possessive_blood_type_is_BLOODTYPE.TITLE,
                "1.3.2","desc","health");
        run(NAME_went_to_SCHOOL_from_START_to_END.KEY,
                NAME_went_to_SCHOOL_from_START_to_END.TITLE,
                "2.0","desc","education");
        run(NAME_is_DEMONYM.KEY,
                NAME_is_DEMONYM.TITLE,
                "1.1.1","desc","country");
        /*
        run("職業っていっぱいあるんだね","1.1.1","desc","person");
        run("スポーツやりたい！","1.2.1","desc","sports");
        run("国、県、市町村","1.2.2","desc","city");
        run("男性だってとっくに知ってるわ","1.1.1","desc","person");
        run("こんなに歳をとってるんだ","1.3.1","desc","person");
        run("緊急時には110番","1.4.2","desc","health");
        run("JIS地名コードって何?","1.2.1","desc","city");
        run("あの本の出版社","1.4.2","desc","book");
        run("読書三到！","1.4.1","desc","book");
        run("学名は覚えなくていいよ","1.4.2","desc","animal");
        run("戦争はよくない","1.4.1","desc","battle");
        run("あの映画、あの賞","1.4.2","desc","film");
        run("映画の良し悪しは受賞歴でわかる","1.4.1","desc","film");
        run("この映画のジャンルは何かな？","1.1.1","desc","film");
        run("都道府県の位置覚えてるかな？","1.2.1","desc","city");
        run("高～い高～い","1.2.1","desc","person");
        run("公職多すぎ","1.2.1","desc","person");
        AddLesson.run("一番多い国旗の色は白！","1.1.1","desc","country");
        */
    }
}
