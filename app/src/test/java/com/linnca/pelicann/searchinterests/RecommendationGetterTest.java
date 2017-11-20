package com.linnca.pelicann.searchinterests;

import com.linnca.pelicann.db.MockFirebaseDB;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RecommendationGetterTest {
    private MockFirebaseDB db;

    @Before
    public void init(){
        db = new MockFirebaseDB();
    }
    @Test
    public void recommendations_getNewRecommendations_shouldCallListener(){
        final boolean[] called = new boolean[]{false};
        RecommendationGetter recommendationGetter = new RecommendationGetter(1,
                db, 0);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {
                called[0] = true;
            }
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntryData>(), "wikiDataID1",
                listener);
        assertTrue(called[0]);
    }

    @Test
    public void recommendations_getNewRecommendations_shouldReturnRecommendations(){
        List<WikiDataEntryData> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(2,
                db, 0);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {
                assertEquals(2, results.size());
            }
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntryData>(), "wikiDataID3",
                listener);
    }

    @Test
    public void recommendations_getNewRecommendationsTwice_shouldResetRecommendationCount(){
        List<WikiDataEntryData> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(2,
                db, 0);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {

            }
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntryData>(), "wikiDataID3",
                listener);

        RecommendationGetter.RecommendationGetterListener listener2 = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {
                assertEquals(2, results.size());
            }
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntryData>(), "wikiDataID4",
                listener2);

    }

    @Test
    public void recommendations_loadMoreRecommendations_shouldReturnMoreRecommendations(){
        List<WikiDataEntryData> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(1,
                db, 1);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {
                assertEquals(1, results.size());
            }
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntryData>(), "wikiDataID3",
                listener);
        RecommendationGetter.RecommendationGetterListener listener2 = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {
                assertEquals(2, results.size());
            }
        };
        recommendationGetter.loadMoreRecommendations(new ArrayList<WikiDataEntryData>(), "wikiDataID4",
                listener2);
    }


    @Test
    public void recommendations_requestNewRecommendationsMoreThanInDatabase_shouldNotShowLoadMoreButton(){
        List<WikiDataEntryData> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(3,
                db, 0);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {
                assertFalse(showLoadMoreButton);
            }
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntryData>(), "wikiDataID3",
                listener);
    }

    @Test
    public void recommendations_requestMoreRecommendationsMoreThanInDatabase_shouldNotShowLoadMoreButton(){
        List<WikiDataEntryData> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(2,
                db, 1);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {

            }
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntryData>(), "wikiDataID3",
                listener);
        RecommendationGetter.RecommendationGetterListener listener2 = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {
                assertFalse(showLoadMoreButton);
            }
        };
        recommendationGetter.loadMoreRecommendations(new ArrayList<WikiDataEntryData>(), "wikiDataID3",
                listener2);
    }

    @Test
    public void recommendations_requestNewRecommendationsEqualToInDatabase_shouldNotShowLoadMoreButton(){
        List<WikiDataEntryData> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(2,
                db, 0);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {
                assertFalse(showLoadMoreButton);
            }
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntryData>(), "wikiDataID3",
                listener);
    }

    @Test
    public void recommendations_getNewRecommendationsWithUserInterests_shouldCacheExtraRecommendations(){
        List<WikiDataEntryData> recommendations = new ArrayList<>(3);
        recommendations.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntryData("label3", "desc3", "wikidataID3", "label3", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(1,
                db, 1);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {

            }
        };
        //we retrieve userInterestCt + recommendationCt so
        //we should retrieve 2 + 1 = 3 recommendations in total
        List<WikiDataEntryData> userInterests = new ArrayList<>();
        userInterests.add(new WikiDataEntryData("label4", "desc4", "wikidataID4", "label4", WikiDataEntryData.CLASSIFICATION_OTHER));
        userInterests.add(new WikiDataEntryData("label5", "desc5", "wikidataID5", "label5", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendationGetter.getNewRecommendations(userInterests, "wikiDataID6",
                listener);
        //clear the database so we know we aren't pulling data from the database when we load more
        db.recommendations.clear();
        RecommendationGetter.RecommendationGetterListener listener2 = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {
                assertEquals(2, results.size());
            }
        };
        recommendationGetter.loadMoreRecommendations(userInterests, "wikiDataID6",
                listener2);
    }

    @Test
    public void recommendations_loadMoreRecommendationsWithCacheEqualToNextRecommendationCt_shouldGetRecommendationsFromDatabase(){
        List<WikiDataEntryData> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(1,
                db, 1);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {

            }
        };
        //we retrieve userInterestCt + recommendationCt so
        //we should retrieve 1 + 1 = 2 recommendations in total
        List<WikiDataEntryData> userInterests = new ArrayList<>();
        userInterests.add(new WikiDataEntryData("label3", "desc3", "wikidataID3", "label3", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendationGetter.getNewRecommendations(userInterests, "wikiDataID4",
                listener);
        //clear the database so we know we retrieved the next data from the database
        db.recommendations.clear();
        RecommendationGetter.RecommendationGetterListener listener2 = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntryData> results, boolean showLoadMoreButton) {
                assertEquals(0, results.size());
            }
        };
        recommendationGetter.loadMoreRecommendations(userInterests, "wikiDataID5",
                listener2);
    }

}