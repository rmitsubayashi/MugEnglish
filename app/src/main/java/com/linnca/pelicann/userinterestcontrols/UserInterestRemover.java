package com.linnca.pelicann.userinterestcontrols;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.db.datawrappers.WikiDataEntryData;

public class UserInterestRemover {
    public static void removeUserInterest(final WikiDataEntryData data, String userID){
        DatabaseReference allUserInterestsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.USER_INTERESTS + "/" +
                        userID
        );

        allUserInterestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    WikiDataEntryData childData = child.getValue(WikiDataEntryData.class);
                    //don't want to remove a recommendation edge to itself (it doesn't exist)
                    if (!childData.equals(data)){
                        disconnectRecommendationEdge(childData.getWikiDataID(), data);
                        disconnectRecommendationEdge(data.getWikiDataID(), childData);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference userInterestRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.USER_INTERESTS + "/" +
                        userID + "/" +
                        data.getWikiDataID()
        );
        userInterestRef.removeValue();

    }

    private static void disconnectRecommendationEdge(final String fromInterestID, final WikiDataEntryData toInterest){
        DatabaseReference edgeRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.RECOMMENDATION_MAP + "/" +
                        fromInterestID + "/" +
                        toInterest.getWikiDataID() + "/" +
                        FirebaseDBHeaders.RECOMMENDATION_MAP_EDGE_COUNT
        );
        edgeRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long edgeWeight = mutableData.getValue(Long.class);
                if (edgeWeight != null){
                    if (edgeWeight == 1){
                        mutableData.setValue(null);
                        FirebaseDatabase.getInstance().getReference(
                                FirebaseDBHeaders.RECOMMENDATION_MAP + "/" +
                                        fromInterestID + "/" +
                                        toInterest.getWikiDataID()
                        ).removeValue();
                    } else {
                        mutableData.setValue(edgeWeight - 1);
                    }
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }
}
