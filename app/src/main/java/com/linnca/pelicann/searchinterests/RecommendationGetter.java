package com.linnca.pelicann.searchinterests;

import android.content.Context;

import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.OnDBResultListener;
import com.linnca.pelicann.userinterests.WikiDataEntity;

import java.util.ArrayList;
import java.util.List;

class RecommendationGetter {
    //to get connection status
    private Context context;
    private Database db;
    private int toDisplayRecommendationCt = 0;
    private int defaultRecommendationCt;
    //how many recommendations to add when the user loads
    // more recommendations
    private int loadMoreRecommendationCt;
    //we get more recommendations than we need
    // to guarantee populating recommendations,
    //so save the leftovers and when the user loads more,
    // we can check this first
    private List<WikiDataEntity> savedRecommendations = new ArrayList<>();

    //the interface is to determine what will happen to the UI.
    //the results here should only contain enough items to display (not the entire list)
    interface RecommendationGetterListener {
        void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton);
        void onNoConnection();
    }

    RecommendationGetter(int defaultRecommendationCt, Context context, Database db, int loadMoreRecommendationCt){
        this.context = context;
        this.db = db;
        this.defaultRecommendationCt = defaultRecommendationCt;
        this.loadMoreRecommendationCt = loadMoreRecommendationCt;
    }

    int getToDisplayRecommendationCt(){
        return toDisplayRecommendationCt;
    }

    void getNewRecommendations(List<WikiDataEntity> userInterests,
                            RecommendationGetterListener recommendationGetterListener){
        //reset the recommendation count to the default
        toDisplayRecommendationCt = defaultRecommendationCt;
        getRecommendations(userInterests, recommendationGetterListener);
    }

    void loadMoreRecommendations(List<WikiDataEntity> userInterests,
                                 RecommendationGetterListener recommendationGetterListener){
        toDisplayRecommendationCt += loadMoreRecommendationCt;
        //check first if we can still populate the recommendations with
        //the initial list we grabbed.
        if (savedRecommendations.size() > toDisplayRecommendationCt){
            //we technically can display the list if we have recommendations equal to the display count,
            // but then we don't know whether we should show the load more button or not.
            List<WikiDataEntity> toDisplay = new ArrayList<>(
                    savedRecommendations.subList(0, toDisplayRecommendationCt));
            recommendationGetterListener.onGetRecommendations(toDisplay, true);
        } else {
            //grab more from the database
            getRecommendations(userInterests, recommendationGetterListener);
        }
    }

    private void getRecommendations(final List<WikiDataEntity> userInterests,
                                    final RecommendationGetterListener recommendationGetterListener){
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestRankingsQueried(List<WikiDataEntity> rankings) {
                //clearing out all of the user interests is handled in the database,
                // but this guarantees that user interests will be left out
                rankings.removeAll(userInterests);
                //save all of the list for future use
                savedRecommendations.clear();
                savedRecommendations.addAll(rankings);

                boolean showLoadMoreButton = true;
                //if we have more than we need
                if (rankings.size() > toDisplayRecommendationCt){
                    rankings = rankings.subList(0, toDisplayRecommendationCt);
                } else {
                    showLoadMoreButton = false;
                }
                recommendationGetterListener.onGetRecommendations(rankings, showLoadMoreButton);
            }

            @Override
            public void onNoConnection(){
                recommendationGetterListener.onNoConnection();
            }
        };

        //we get the to display recommendation count + 1 so we know
        // whether we can load more recommendations.
        // we want to get at least the number of the user interests
        // so we guarantee that the returned list will contain new items
        int toGetUserInterestCt = userInterests.size() +
                toDisplayRecommendationCt + 1;
        db.getPopularUserInterests(context, toGetUserInterestCt, onDBResultListener);
    }

}
