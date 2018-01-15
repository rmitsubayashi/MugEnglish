package com.linnca.pelicann.userinterests;

import com.linnca.pelicann.mainactivity.ToolbarSpinnerAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class UserInterestFilter {
    private List<WikiDataEntity> originalList;
    private List<WikiDataEntity> filteredList;
    //default filter is no filter
    private int currentFilter = ToolbarSpinnerAdapter.FILTER_ALL;

    public UserInterestFilter(){
        filteredList = new ArrayList<>();
        originalList = new ArrayList<>();
    }

    public void setUserInterests(List<WikiDataEntity> list){
        originalList = new ArrayList<>(list.size());
        filteredList = new ArrayList<>(list.size());
        for (WikiDataEntity toCopy : list){
            WikiDataEntity copy = new WikiDataEntity(toCopy);
            filteredList.add(copy);
            originalList.add(copy);
        }
        setFilter(currentFilter);
    }

    List<WikiDataEntity> getFilteredList(){return filteredList;}

    public int size(){
        return filteredList.size();
    }

    public WikiDataEntity get(int position){
        if (position >= size()){
            return null;
        }
        return filteredList.get(position);
    }

    public int getFilter(){
        return currentFilter;
    }

    public void setFilter(int filter){
        //we should remove all filters first and return to the original list
        filteredList.clear();
        filteredList.addAll(originalList);

        //then, we can filter.
        List<Integer> toMatchFilter = new ArrayList<>();
        if (filter == ToolbarSpinnerAdapter.FILTER_PERSON){
            toMatchFilter.add(WikiDataEntity.CLASSIFICATION_PERSON);
        }
        if (filter == ToolbarSpinnerAdapter.FILTER_PLACE){
            toMatchFilter.add(WikiDataEntity.CLASSIFICATION_PLACE);
        }
        if (filter == ToolbarSpinnerAdapter.FILTER_OTHER){
            toMatchFilter.add(WikiDataEntity.CLASSIFICATION_OTHER);
            //also all unidentified.
            //this is why we need a list of filters and
            // not a single variable
            toMatchFilter.add(WikiDataEntity.CLASSIFICATION_NOT_SET);
        }
        if (filter == ToolbarSpinnerAdapter.FILTER_ALL){
            toMatchFilter.add(WikiDataEntity.CLASSIFICATION_PERSON);
            toMatchFilter.add(WikiDataEntity.CLASSIFICATION_PLACE);
            toMatchFilter.add(WikiDataEntity.CLASSIFICATION_OTHER);
            toMatchFilter.add(WikiDataEntity.CLASSIFICATION_NOT_SET);
        }
        for (Iterator<WikiDataEntity> iterator = filteredList.iterator(); iterator.hasNext();){
            WikiDataEntity data = iterator.next();
            //no matter the filter,
            //we should show the empty state and no network state
            if (data.getWikiDataID().equals(UserInterestAdapter.EMPTY_STATE_TAG) ||
                    data.getWikiDataID().equals(UserInterestAdapter.NO_NETWORK_TAG)){
                continue;
            }
            if (!toMatchFilter.contains(data.getClassification())){
                iterator.remove();
            }
        }

        //update the filter
        currentFilter = filter;
    }
}
