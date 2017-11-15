package com.linnca.pelicann.userinterests;

import com.linnca.pelicann.mainactivity.widgets.ToolbarSpinnerAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserInterestFilter {
    private List<WikiDataEntryData> originalList;
    private List<WikiDataEntryData> filteredList;
    //default filter is no filter
    private int currentFilter = ToolbarSpinnerAdapter.FILTER_ALL;

    public UserInterestFilter(){
        filteredList = new ArrayList<>();
        originalList = new ArrayList<>();
    }

    public void setUserInterests(List<WikiDataEntryData> list){
        originalList = new ArrayList<>(list.size());
        filteredList = new ArrayList<>(list.size());
        for (WikiDataEntryData toCopy : list){
            WikiDataEntryData copy = new WikiDataEntryData(toCopy);
            filteredList.add(copy);
            originalList.add(copy);
        }
        setFilter(currentFilter);
    }

    public List<WikiDataEntryData> getFilteredList(){return filteredList;}

    public int size(){
        return filteredList.size();
    }

    public WikiDataEntryData get(int position){
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
            toMatchFilter.add(WikiDataEntryData.CLASSIFICATION_PERSON);
        }
        if (filter == ToolbarSpinnerAdapter.FILTER_PLACE){
            toMatchFilter.add(WikiDataEntryData.CLASSIFICATION_PLACE);
        }
        if (filter == ToolbarSpinnerAdapter.FILTER_OTHER){
            toMatchFilter.add(WikiDataEntryData.CLASSIFICATION_OTHER);
            //also all unidentified.
            //this is why we need a list of filters and
            // not a single variable
            toMatchFilter.add(WikiDataEntryData.CLASSIFICATION_NOT_SET);
        }
        if (filter == ToolbarSpinnerAdapter.FILTER_ALL){
            toMatchFilter.add(WikiDataEntryData.CLASSIFICATION_PERSON);
            toMatchFilter.add(WikiDataEntryData.CLASSIFICATION_PLACE);
            toMatchFilter.add(WikiDataEntryData.CLASSIFICATION_OTHER);
            toMatchFilter.add(WikiDataEntryData.CLASSIFICATION_NOT_SET);
        }
        for (Iterator<WikiDataEntryData> iterator = filteredList.iterator(); iterator.hasNext();){
            WikiDataEntryData data = iterator.next();
            if (!toMatchFilter.contains(data.getClassification())){
                iterator.remove();
            }
        }

        //update the filter
        currentFilter = filter;
    }
}
