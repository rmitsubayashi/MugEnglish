package com.linnca.pelicann.results;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.questions.InstanceRecord;

//this manages the results displayed to the user
class ResultsManager {
    private final FirebaseDatabase db;
    private final String userID;
    private final InstanceRecord instanceRecord;
    private final ResultsManagerListener resultsManagerListener;

    interface ResultsManagerListener {
        }

    ResultsManager(InstanceRecord instanceRecord, ResultsManagerListener listener){
        db = FirebaseDatabase.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.instanceRecord = instanceRecord;
        this.resultsManagerListener = listener;
    }

    void saveInstanceRecord(){
        DatabaseReference ref = db.getReference(
                FirebaseDBHeaders.INSTANCE_RECORDS + "/" + userID + "/" +
                instanceRecord.getLessonId() + "/" + instanceRecord.getInstanceId() +"/" + instanceRecord.getId());
        ref.setValue(instanceRecord);
    }
}
