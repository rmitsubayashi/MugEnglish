package pelicann.linnca.com.corefunctionality.db;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

//tests the implementation of the mock database
public class TestMockFirebaseDB {
    private MockFirebaseDB db;

    @Before
    public void init(){
        db = new MockFirebaseDB();
    }

    //for the methods called,
    //we can use Mockito, but doing it without any frameworks works just as well
    // (a little less cleaner code and readability)
    // and with shorter run time
    // (one Mockito test can take ~500 ms,
    // plain JUnit test would take ~5 ms)
    @Test
    public void searchEntityPropertyData_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onEntityPropertyDataSearched(List<EntityPropertyData> quesdatationSets, List<WikiDataEntity> userInterestsSearched) {
                called[0] = true;
            }
        };
        //OnDBResultListener onDBResultListener = mock(OnDBResultListener.class);
        db.searchEntityPropertyData(null, "", new ArrayList<WikiDataEntity>(),
                0, new ArrayList<EntityPropertyData>(), onDBResultListener);
        assertTrue(called[0]);
        //verify(onDBResultListener, times(2)).onQuestionsQueried(new ArrayList<String>(), new ArrayList<WikiDataEntity>());
    }

    @Test
    public void addEntityPropertyData_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onEntityPropertyDataAdded(EntityPropertyData data) {
                called[0] = true;
            }
        };
        //need at least one entity property data for the listener to be called
        List<EntityPropertyData> data = new ArrayList<>();
        data.add(new EntityPropertyData("","","wikiDataID1",null,null));
        db.addEntityPropertyData("", data,
                onDBResultListener);
        assertTrue(called[0]);
    }

    @Test
    public void addLessonInstance_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
                called[0] = true;
            }
        };
        db.addLessonInstance(null, new LessonInstanceData(), new ArrayList<String>(),
                onDBResultListener);
    }

    @Test
    public void getLessonInstances_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
                called[0] = true;
            }
        };
        db.getLessonInstances(null, "", false, onDBResultListener);
    }

    @Test
    public void getUserInterests_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsQueried(List<WikiDataEntity> userInterests) {
                called[0] = true;
            }
        };
        db.getUserInterests(null, false, onDBResultListener);
    }

    @Test
    public void removeUserInterests_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsRemoved() {
                called[0] = true;
            }
        };
        db.removeUserInterests(new ArrayList<WikiDataEntity>(), onDBResultListener);
    }

    @Test
    public void addUserInterests_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsAdded() {
                called[0] = true;
            }
        };
        db.addUserInterests(null, new ArrayList<WikiDataEntity>(), onDBResultListener);
    }

    @Test
    public void userInterests_addUserInterests_userInterestListShouldContainUserInterests(){
        final List<WikiDataEntity> newInterests = new ArrayList<>(2);
        newInterests.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1"));
        newInterests.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2"));
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsAdded() {
                List<WikiDataEntity> updatedList = db.userInterests;
                boolean matched = true;
                for (WikiDataEntity data : updatedList){
                    if (!newInterests.contains(data)){
                        matched = false;
                        break;
                    }
                }
                assertTrue(matched);
            }
        };
        db.addUserInterests(null, newInterests, onDBResultListener);
    }

    @Test
    public void userInterests_addUserInterests_userInterestListShouldOnlyContainUserInterests(){
        final List<WikiDataEntity> newInterests = new ArrayList<>(2);
        newInterests.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1"));
        newInterests.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2"));
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsAdded() {
                List<WikiDataEntity> updatedList = db.userInterests;
                assertEquals(newInterests.size(), updatedList.size());
            }
        };
        db.addUserInterests(null, newInterests, onDBResultListener);
    }

    @Test
    public void userInterests_removeUserInterests_userInterestsShouldBeRemoved(){
        final List<WikiDataEntity> oldInterests = new ArrayList<>(2);
        oldInterests.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1"));
        oldInterests.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2"));
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsRemoved() {
                List<WikiDataEntity> updatedList = db.userInterests;
                boolean matched = false;
                for (WikiDataEntity data : oldInterests){
                    if (updatedList.contains(data)){
                        matched = true;
                        break;
                    }
                }
                assertFalse(matched);
            }
        };
        db.userInterests = new ArrayList<>(oldInterests);
        db.removeUserInterests(oldInterests, onDBResultListener);
    }

    @Test
    public void userInterests_removeUserInterests_onlyUserInterestsToRemoveShouldBeRemoved(){
        List<WikiDataEntity> oldInterests = new ArrayList<>(2);
        oldInterests.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1"));
        oldInterests.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2"));
        oldInterests.add(new WikiDataEntity("label3", "desc3", "wikidataID3", "label3"));
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsRemoved() {
                assertEquals(1, db.userInterests.size());
            }
        };
        List<WikiDataEntity> toRemoveInterests = new ArrayList<>(oldInterests);
        toRemoveInterests.remove(0);
        db.userInterests = new ArrayList<>(oldInterests);
        db.removeUserInterests(toRemoveInterests, onDBResultListener);
    }

    @Test
    public void userInterests_getUserInterests_shouldGetUserInterests(){
        final List<WikiDataEntity> newInterests = new ArrayList<>(2);
        newInterests.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1"));
        newInterests.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2"));
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsQueried(List<WikiDataEntity> userInterests) {
                boolean matched = true;
                for (WikiDataEntity interest : userInterests){
                    if (!newInterests.contains(interest)){
                        matched = false;
                        break;
                    }
                }
                assertTrue(matched);
            }
        };
        db.addUserInterests(null, newInterests, onDBResultListener);
    }

    @Test
    public void entityPropertyData_addEntityPropertyData_shouldAddData(){
        List<EntityPropertyData> data = new ArrayList<>(1);
        String id = "wikiDataID1";
        data.add(new EntityPropertyData("","",id,null,null));
        db.addEntityPropertyData("", data,
                new OnDBResultListener() {
        });
        assertEquals(1, db.entityPropertyData.size());
        assertEquals(1, db.entityPropertyData.get(id).size());
    }

    @Test
    public void entityPropertyData_addMoreThanOnePerWikidataID_shouldAddBoth(){
        List<EntityPropertyData> data = new ArrayList<>(2);
        String id = "wikiDataID1";
        data.add(new EntityPropertyData("1","",id,null,null));
        data.add(new EntityPropertyData("2","",id,null,null));


        OnDBResultListener onDBResultListener = new OnDBResultListener() {};
        db.addEntityPropertyData("lessonID1", data, onDBResultListener);
        assertEquals(2, db.entityPropertyData.get(id).size());
    }

}
