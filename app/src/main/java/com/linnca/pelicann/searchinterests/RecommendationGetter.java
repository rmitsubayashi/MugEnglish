package com.linnca.pelicann.searchinterests;

import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import java.util.ArrayList;
import java.util.List;

class RecommendationGetter {
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
    private List<WikiDataEntryData> savedRecommendations = new ArrayList<>();

    //the interface is to determine what will happen to the UI.
    //the results here should only contain enough items to display (not the entire list)
    interface RecommendationGetterListener {
        void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton);
    }

    RecommendationGetter(int defaultRecommendationCt, Database db, int loadMoreRecommendationCt){
        this.db = db;
        this.defaultRecommendationCt = defaultRecommendationCt;
        this.loadMoreRecommendationCt = loadMoreRecommendationCt;
    }

    int getToDisplayRecommendationCt(){
        return toDisplayRecommendationCt;
    }

    void getNewRecommendations(List<WikiDataEntryData> userInterests, String wikiDataID,
                            RecommendationGetterListener recommendationGetterListener){
        //reset the recommendation count to the default
        toDisplayRecommendationCt = defaultRecommendationCt;
        getRecommendations(userInterests, wikiDataID, recommendationGetterListener);
    }

    void loadMoreRecommendations(List<WikiDataEntryData> userInterests, String wikiDataID,
                                 RecommendationGetterListener recommendationGetterListener){
        toDisplayRecommendationCt += loadMoreRecommendationCt;
        //check first if we can still populate the recommendations with
        //the initial list we grabbed.
        if (savedRecommendations.size() > toDisplayRecommendationCt){
            //we technically can display the list if we have recommendations equal to the display count,
            // but then we don't know whether we should show the load more button or not.
            List<WikiDataEntryData> toDisplay = savedRecommendations.subList(0, toDisplayRecommendationCt);
            recommendationGetterListener.onGetRecommendations(toDisplay, true);
        } else {
            //grab more from the database
            getRecommendations(userInterests, wikiDataID, recommendationGetterListener);
        }
    }

    private void getRecommendations(final List<WikiDataEntryData> userInterests, String wikiDataID,
                                    final RecommendationGetterListener recommendationGetterListener){
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onRecommendationsQueried(List<WikiDataEntryData> recommendations) {
                //clearing out all of the user interests is handled in the database,
                // but this guarantees that user interests will be left out
                recommendations.removeAll(userInterests);
                //save all of the list for future use
                savedRecommendations.clear();
                savedRecommendations.addAll(recommendations);
                boolean showLoadMoreButton = true;
                //if we have more than we need
                if (recommendations.size() > toDisplayRecommendationCt){
                    recommendations = recommendations.subList(0, toDisplayRecommendationCt);
                } else {
                    showLoadMoreButton = false;
                }
                recommendationGetterListener.onGetRecommendations(recommendations, showLoadMoreButton);
            }
        };

        //we get the to display recommendation count + 1 so we know
        // whether we can load more recommendations
        db.getRecommendations(userInterests, wikiDataID,
                toDisplayRecommendationCt+1, onResultListener);
    }

}
