package com.linnca.pelicann.searchinterests;

import com.linnca.pelicann.db.MockFirebaseDB;
import com.linnca.pelicann.userinterests.WikiDataEntity;

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
        RecommendationGetter recommendationGetter = new RecommendationGetter(1, null,
                db, 0);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {
                called[0] = true;
            }
            @Override
            public void onNoConnection(){}
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntity>(),
                listener);
        assertTrue(called[0]);
    }

    @Test
    public void recommendations_getNewRecommendations_shouldReturnRecommendations(){
        List<WikiDataEntity> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(2,
                null, db, 0);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {
                assertEquals(2, results.size());
            }
            @Override
            public void onNoConnection(){}
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntity>(),
                listener);
    }

    @Test
    public void recommendations_getNewRecommendationsTwice_shouldResetRecommendationCount(){
        List<WikiDataEntity> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(2,
                null, db, 0);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {

            }
            @Override
            public void onNoConnection(){}
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntity>(),
                listener);

        RecommendationGetter.RecommendationGetterListener listener2 = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {
                assertEquals(2, results.size());
            }
            @Override
            public void onNoConnection(){}
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntity>(),
                listener2);

    }

    @Test
    public void recommendations_loadMoreRecommendations_shouldReturnMoreRecommendations(){
        List<WikiDataEntity> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(1,
                null, db, 1);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {
                assertEquals(1, results.size());
            }
            @Override
            public void onNoConnection(){}
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntity>(),
                listener);
        RecommendationGetter.RecommendationGetterListener listener2 = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {
                assertEquals(2, results.size());
            }
            @Override
            public void onNoConnection(){}
        };
        recommendationGetter.loadMoreRecommendations(new ArrayList<WikiDataEntity>(),
                listener2);
    }


    @Test
    public void recommendations_requestNewRecommendationsMoreThanInDatabase_shouldNotShowLoadMoreButton(){
        List<WikiDataEntity> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(3,
                null, db, 0);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {
                assertFalse(showLoadMoreButton);
            }
            @Override
            public void onNoConnection(){}
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntity>(),
                listener);
    }

    @Test
    public void recommendations_requestMoreRecommendationsMoreThanInDatabase_shouldNotShowLoadMoreButton(){
        List<WikiDataEntity> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(2,
                null, db, 1);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {

            }
            @Override
            public void onNoConnection(){}
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntity>(),
                listener);
        RecommendationGetter.RecommendationGetterListener listener2 = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {
                assertFalse(showLoadMoreButton);
            }
            @Override
            public void onNoConnection(){}
        };
        recommendationGetter.loadMoreRecommendations(new ArrayList<WikiDataEntity>(),
                listener2);
    }

    @Test
    public void recommendations_requestNewRecommendationsEqualToInDatabase_shouldNotShowLoadMoreButton(){
        List<WikiDataEntity> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(2,
                null, db, 0);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {
                assertFalse(showLoadMoreButton);
            }
            @Override
            public void onNoConnection(){}
        };
        recommendationGetter.getNewRecommendations(new ArrayList<WikiDataEntity>(),
                listener);
    }

    @Test
    public void recommendations_getNewRecommendationsWithUserInterests_shouldCacheExtraRecommendations(){
        List<WikiDataEntity> recommendations = new ArrayList<>(3);
        recommendations.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntity("label3", "desc3", "wikidataID3", "label3", WikiDataEntity.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(1,
                null, db, 1);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {

            }
            @Override
            public void onNoConnection(){}
        };
        //we retrieve userInterestCt + recommendationCt so
        //we should retrieve 2 + 1 = 3 recommendations in total
        List<WikiDataEntity> userInterests = new ArrayList<>();
        userInterests.add(new WikiDataEntity("label4", "desc4", "wikidataID4", "label4", WikiDataEntity.CLASSIFICATION_OTHER));
        userInterests.add(new WikiDataEntity("label5", "desc5", "wikidataID5", "label5", WikiDataEntity.CLASSIFICATION_OTHER));
        recommendationGetter.getNewRecommendations(userInterests,
                listener);
        //clear the database so we know we aren't pulling data from the database when we load more
        db.recommendations.clear();
        RecommendationGetter.RecommendationGetterListener listener2 = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {
                assertEquals(2, results.size());
            }
            @Override
            public void onNoConnection(){}

        };
        recommendationGetter.loadMoreRecommendations(userInterests,
                listener2);
    }

    @Test
    public void recommendations_loadMoreRecommendationsWithCacheEqualToNextRecommendationCt_shouldGetRecommendationsFromDatabase(){
        List<WikiDataEntity> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        RecommendationGetter recommendationGetter = new RecommendationGetter(1,
                null, db, 1);
        RecommendationGetter.RecommendationGetterListener listener = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {

            }
            @Override
            public void onNoConnection(){}
        };
        //we retrieve userInterestCt + recommendationCt so
        //we should retrieve 1 + 1 = 2 recommendations in total
        List<WikiDataEntity> userInterests = new ArrayList<>();
        userInterests.add(new WikiDataEntity("label3", "desc3", "wikidataID3", "label3", WikiDataEntity.CLASSIFICATION_OTHER));
        recommendationGetter.getNewRecommendations(userInterests,
                listener);
        //clear the database so we know we retrieved the next data from the database
        db.recommendations.clear();
        RecommendationGetter.RecommendationGetterListener listener2 = new RecommendationGetter.RecommendationGetterListener() {
            @Override
            public void onGetRecommendations(List<WikiDataEntity> results, boolean showLoadMoreButton) {
                assertEquals(0, results.size());
            }
            @Override
            public void onNoConnection(){}
        };
        recommendationGetter.loadMoreRecommendations(userInterests,
                listener2);
    }

}
