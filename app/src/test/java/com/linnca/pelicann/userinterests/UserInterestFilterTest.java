package com.linnca.pelicann.userinterests;

import com.linnca.pelicann.mainactivity.ToolbarSpinnerAdapter;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UserInterestFilterTest {
    UserInterestFilter userInterestFilter;
    @Before
    public void init(){
        userInterestFilter = new UserInterestFilter();
    }

    @Test
    public void filter_allToPeople_shouldFilterProperly(){
        List<WikiDataEntity> sampleData = new ArrayList<>();
        sampleData.add(new WikiDataEntity("person1","description1", "wikiDataID1", "pronunciation1",
                WikiDataEntity.CLASSIFICATION_PERSON));
        sampleData.add(new WikiDataEntity("place2","description2", "wikiDataID2", "pronunciation2",
                WikiDataEntity.CLASSIFICATION_PLACE));
        sampleData.add(new WikiDataEntity("other3","description3", "wikiDataID3", "pronunciation3",
                WikiDataEntity.CLASSIFICATION_OTHER));
        sampleData.add(new WikiDataEntity("person4","description4", "wikiDataID4", "pronunciation4",
                WikiDataEntity.CLASSIFICATION_PERSON));
        userInterestFilter.setUserInterests(sampleData);
        assertTrue(userInterestFilter.getFilter() == ToolbarSpinnerAdapter.FILTER_ALL);
        userInterestFilter.setFilter(ToolbarSpinnerAdapter.FILTER_PERSON);
        assertTrue(userInterestFilter.getFilter() == ToolbarSpinnerAdapter.FILTER_PERSON);

        List<WikiDataEntity> filteredList = userInterestFilter.getFilteredList();
        assertEquals(sampleData.get(0), filteredList.get(0));
        assertEquals(sampleData.get(3), filteredList.get(1));
        assertEquals(2, userInterestFilter.size());
    }

    @Test
    public void filter_allToPlace_shouldFilterProperly(){
        List<WikiDataEntity> sampleData = new ArrayList<>();
        sampleData.add(new WikiDataEntity("person1","description1", "wikiDataID1", "pronunciation1",
                WikiDataEntity.CLASSIFICATION_PERSON));
        sampleData.add(new WikiDataEntity("place2","description2", "wikiDataID2", "pronunciation2",
                WikiDataEntity.CLASSIFICATION_PLACE));
        sampleData.add(new WikiDataEntity("other3","description3", "wikiDataID3", "pronunciation3",
                WikiDataEntity.CLASSIFICATION_OTHER));
        sampleData.add(new WikiDataEntity("notSet4","description4", "wikiDataID4", "pronunciation4",
                WikiDataEntity.CLASSIFICATION_NOT_SET));
        userInterestFilter.setUserInterests(sampleData);
        assertTrue(userInterestFilter.getFilter() == ToolbarSpinnerAdapter.FILTER_ALL);
        userInterestFilter.setFilter(ToolbarSpinnerAdapter.FILTER_PLACE);
        assertTrue(userInterestFilter.getFilter() == ToolbarSpinnerAdapter.FILTER_PLACE);

        List<WikiDataEntity> filteredList = userInterestFilter.getFilteredList();
        assertEquals(sampleData.get(1), filteredList.get(0));
        assertEquals(1, userInterestFilter.size());
    }

    @Test
    public void filter_allToOther_shouldFilterProperly(){
        List<WikiDataEntity> sampleData = new ArrayList<>();
        sampleData.add(new WikiDataEntity("person1","description1", "wikiDataID1", "pronunciation1",
                WikiDataEntity.CLASSIFICATION_PERSON));
        sampleData.add(new WikiDataEntity("place2","description2", "wikiDataID2", "pronunciation2",
                WikiDataEntity.CLASSIFICATION_PLACE));
        sampleData.add(new WikiDataEntity("other3","description3", "wikiDataID3", "pronunciation3",
                WikiDataEntity.CLASSIFICATION_OTHER));
        sampleData.add(new WikiDataEntity("notSet4","description4", "wikiDataID4", "pronunciation4",
                WikiDataEntity.CLASSIFICATION_NOT_SET));
        userInterestFilter.setUserInterests(sampleData);
        assertTrue(userInterestFilter.getFilter() == ToolbarSpinnerAdapter.FILTER_ALL);
        userInterestFilter.setFilter(ToolbarSpinnerAdapter.FILTER_OTHER);
        assertTrue(userInterestFilter.getFilter() == ToolbarSpinnerAdapter.FILTER_OTHER);

        List<WikiDataEntity> filteredList = userInterestFilter.getFilteredList();
        assertEquals(sampleData.get(2), filteredList.get(0));
        assertEquals(sampleData.get(3), filteredList.get(1));
        assertEquals(2, userInterestFilter.size());
    }

    @Test
    public void filter_peopleToAll_shouldFilterProperly(){
        List<WikiDataEntity> sampleData = new ArrayList<>();
        sampleData.add(new WikiDataEntity("person1","description1", "wikiDataID1", "pronunciation1",
                WikiDataEntity.CLASSIFICATION_PERSON));
        sampleData.add(new WikiDataEntity("place2","description2", "wikiDataID2", "pronunciation2",
                WikiDataEntity.CLASSIFICATION_PLACE));
        sampleData.add(new WikiDataEntity("other3","description3", "wikiDataID3", "pronunciation3",
                WikiDataEntity.CLASSIFICATION_OTHER));
        sampleData.add(new WikiDataEntity("person4","description4", "wikiDataID4", "pronunciation4",
                WikiDataEntity.CLASSIFICATION_PERSON));
        userInterestFilter.setFilter(ToolbarSpinnerAdapter.FILTER_PERSON);
        userInterestFilter.setUserInterests(sampleData);
        List<WikiDataEntity> filteredList = userInterestFilter.getFilteredList();
        assertEquals(sampleData.get(0), filteredList.get(0));
        assertEquals(sampleData.get(3), filteredList.get(1));
        assertEquals(2, userInterestFilter.size());

        userInterestFilter.setFilter(ToolbarSpinnerAdapter.FILTER_ALL);
        filteredList = userInterestFilter.getFilteredList();
        assertEquals(sampleData.get(0), filteredList.get(0));
        assertEquals(sampleData.get(1), filteredList.get(1));
        assertEquals(sampleData.get(2), filteredList.get(2));
        assertEquals(sampleData.get(3), filteredList.get(3));
        assertEquals(4, userInterestFilter.size());
    }

    @Test
    public void set_settingUserInterestsWhenFilterIsSet_shouldFilterTheNewlySetList(){
        List<WikiDataEntity> sampleData = new ArrayList<>();
        sampleData.add(new WikiDataEntity("person1","description1", "wikiDataID1", "pronunciation1",
                WikiDataEntity.CLASSIFICATION_PERSON));
        sampleData.add(new WikiDataEntity("place2","description2", "wikiDataID2", "pronunciation2",
                WikiDataEntity.CLASSIFICATION_PLACE));
        sampleData.add(new WikiDataEntity("other3","description3", "wikiDataID3", "pronunciation3",
                WikiDataEntity.CLASSIFICATION_OTHER));
        sampleData.add(new WikiDataEntity("notSet4","description4", "wikiDataID4", "pronunciation4",
                WikiDataEntity.CLASSIFICATION_NOT_SET));
        userInterestFilter.setFilter(ToolbarSpinnerAdapter.FILTER_PLACE);
        userInterestFilter.setUserInterests(sampleData);
        List<WikiDataEntity> filteredList = userInterestFilter.getFilteredList();
        assertEquals(sampleData.get(1), filteredList.get(0));
        assertEquals(1, userInterestFilter.size());
    }
}
