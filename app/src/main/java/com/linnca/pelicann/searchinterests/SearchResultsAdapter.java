package com.linnca.pelicann.searchinterests;

import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.userinterests.WikiDataEntity;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String VIEW_TYPE_RECOMMENDATION_HEADER_WIKIDATA_ID = "recommendation header";
    private final String VIEW_TYPE_RECOMMENDATION_FOOTER_WIKIDATA_ID = "recommendation footer";
    public final String VIEW_TYPE_EMPTY_STATE_WIKIDATA_ID = "emptyState";
    private final String VIEW_TYPE_LOADING_WIKIDATA_ID = "loading";
    private final String VIEW_TYPE_INITIAL_WIKIDATA_ID = "initial";
    private final String VIEW_TYPE_OFFLINE_WIKIDATA_ID = "offline";
    private final String VIEW_TYPE_OFFLINE_AFTER_ADDING_WIKIDATA_ID = "offline after adding";

    private final int VIEW_TYPE_RECOMMENDATION_HEADER = 1;
    private final int VIEW_TYPE_RECOMMENDATION_FOOTER = 2;
    private final int VIEW_TYPE_NORMAL = 3;
    private final int VIEW_TYPE_EMPTY = 4;
    private final int VIEW_TYPE_LOADING = 5;
    private final int VIEW_TYPE_OFFLINE = 6;
    private final int VIEW_TYPE_OFFLINE_AFTER_ADDING = 7;
    private final int VIEW_TYPE_INITIAL = 8;

    private List<WikiDataEntity> results = new ArrayList<>();
    private final SearchResultsAdapterListener searchResultsAdapterListener;

    private boolean recommendationHeaderShown = false;
    //footer (load more recommendations) is only shown
    // when the user can load more recommendations
    private boolean recommendationFooterShown = false;
    private String headerLabel = "";
    private WikiDataEntity recommendationWikiDataEntity;

    //prevent fast double clicks on any of the buttons
    private long lastClickTime = 0;

    public interface SearchResultsAdapterListener {
        void onAddInterest(WikiDataEntity data);
        void onLoadMoreRecommendations();
    }

    public SearchResultsAdapter(SearchResultsAdapterListener listener){
        searchResultsAdapterListener = listener;
        //initial state
        WikiDataEntity initialData = new WikiDataEntity();
        initialData.setWikiDataID(VIEW_TYPE_INITIAL_WIKIDATA_ID);
        results.add(initialData);
    }

    @Override
    public int getItemCount(){
        return results.size();
    }

    @Override
    public long getItemId(int position){ return results.get(position).hashCode(); }

    @Override
    public int getItemViewType(int position){
        WikiDataEntity data = results.get(position);
        switch (data.getWikiDataID()){
            case VIEW_TYPE_RECOMMENDATION_HEADER_WIKIDATA_ID:
                return VIEW_TYPE_RECOMMENDATION_HEADER;
            case VIEW_TYPE_RECOMMENDATION_FOOTER_WIKIDATA_ID:
                return VIEW_TYPE_RECOMMENDATION_FOOTER;
            case VIEW_TYPE_EMPTY_STATE_WIKIDATA_ID:
                return VIEW_TYPE_EMPTY;
            case VIEW_TYPE_LOADING_WIKIDATA_ID:
                return VIEW_TYPE_LOADING;
            case VIEW_TYPE_INITIAL_WIKIDATA_ID:
                return VIEW_TYPE_INITIAL;
            case VIEW_TYPE_OFFLINE_WIKIDATA_ID:
                return VIEW_TYPE_OFFLINE;
            case VIEW_TYPE_OFFLINE_AFTER_ADDING_WIKIDATA_ID:
                return VIEW_TYPE_OFFLINE_AFTER_ADDING;
            default:
                return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_RECOMMENDATION_HEADER){
            itemView = inflater.inflate(R.layout.inflatable_search_interest_recommendations_header, parent, false);
            return new SearchResultsRecommendationHeaderViewHolder(itemView);
        }
        else if (viewType == VIEW_TYPE_RECOMMENDATION_FOOTER){
            itemView = inflater.inflate(R.layout.inflatable_search_interest_recommendations_footer, parent, false);
            return new SearchResultsRecommendationFooterViewHolder(itemView);
        }
        else if (viewType == VIEW_TYPE_EMPTY){
            itemView = inflater.inflate(R.layout.inflatable_search_interests_empty_state, parent, false);
            return new SearchResultsEmptyStateViewHolder(itemView);
        }
        else if (viewType == VIEW_TYPE_LOADING){
            itemView = inflater.inflate(R.layout.inflatable_search_interest_loading, parent, false);
            return new RecyclerView.ViewHolder(itemView) {};
        }
        else if (viewType == VIEW_TYPE_INITIAL){
            itemView = inflater.inflate(R.layout.inflatable_search_interest_initial_state, parent, false);
            return new RecyclerView.ViewHolder(itemView) {};
        } else if (viewType == VIEW_TYPE_OFFLINE){
            itemView = inflater.inflate(R.layout.inflatable_search_interests_offline, parent, false);
            return new RecyclerView.ViewHolder(itemView){};
        } else if (viewType == VIEW_TYPE_OFFLINE_AFTER_ADDING){
            itemView = inflater.inflate(R.layout.inflatable_search_interests_offline_after_adding, parent, false);
            return new SearchResultsOfflineAfterAddingViewHolder(itemView);
        }else {
            //everything else is a item
            itemView = inflater.inflate(R.layout.inflatable_search_interests_result_item, parent, false);
            return new SearchResultsViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position){
        if (viewHolder instanceof SearchResultsViewHolder){
            WikiDataEntity data = results.get(position);
            ((SearchResultsViewHolder) viewHolder).setLabel(data.getLabel());
            ((SearchResultsViewHolder) viewHolder).setDescription(data.getDescription());
            final WikiDataEntity fData = data;
            ((SearchResultsViewHolder)viewHolder).setButtonListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //make sure the user can't double click
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                        return;
                    }
                    //update the last clicked time
                    lastClickTime = SystemClock.elapsedRealtime();
                    //add the interest
                    searchResultsAdapterListener.onAddInterest(fData);
                }
            });
        } else if (viewHolder instanceof SearchResultsRecommendationHeaderViewHolder){
            ((SearchResultsRecommendationHeaderViewHolder) viewHolder)
                    .setTitle(headerLabel);
            //if there are no items, we should not show a rankings header
            ((SearchResultsRecommendationHeaderViewHolder) viewHolder)
                    .setRankingsHeaderVisibility(getSearchResultSize() != 0);
        } else if (viewHolder instanceof SearchResultsRecommendationFooterViewHolder){
            ((SearchResultsRecommendationFooterViewHolder) viewHolder)
                    .setButtonListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchResultsAdapterListener.onLoadMoreRecommendations();
                }
            });
        } else if (viewHolder instanceof SearchResultsEmptyStateViewHolder){
            WikiDataEntity data = results.get(position);
            ((SearchResultsEmptyStateViewHolder) viewHolder).setQuery(data.getLabel());
        } else if (viewHolder instanceof SearchResultsOfflineAfterAddingViewHolder){
            WikiDataEntity data = results.get(position);
            ((SearchResultsOfflineAfterAddingViewHolder) viewHolder).setTitle(data.getLabel());
        }
    }

    //the overridden getItemCount() includes extra items like
    // headers and footers
    public int getSearchResultSize(){
        int resultCt = 0;
        for (WikiDataEntity data : results){
            String wikiDataID = data.getWikiDataID();
            if (!wikiDataID.equals(VIEW_TYPE_LOADING_WIKIDATA_ID) &&
                    !wikiDataID.equals(VIEW_TYPE_EMPTY_STATE_WIKIDATA_ID) &&
                    !wikiDataID.equals(VIEW_TYPE_RECOMMENDATION_FOOTER_WIKIDATA_ID) &&
                    !wikiDataID.equals(VIEW_TYPE_RECOMMENDATION_HEADER_WIKIDATA_ID) &&
                    !wikiDataID.equals(VIEW_TYPE_INITIAL_WIKIDATA_ID) &&
                    !wikiDataID.equals(VIEW_TYPE_OFFLINE_WIKIDATA_ID)){
                resultCt++;
            }
        }
        return resultCt;
    }

    public boolean isLoading(){
        boolean isLoading = false;
        for (WikiDataEntity data : results){
            if (data.getWikiDataID().equals(VIEW_TYPE_LOADING_WIKIDATA_ID)){
                isLoading = true;
                break;
            }
        }
        return isLoading;
    }

    public void showLoading(){
        WikiDataEntity loadingData = new WikiDataEntity();
        loadingData.setWikiDataID(VIEW_TYPE_LOADING_WIKIDATA_ID);
        List<WikiDataEntity> dataList = new ArrayList<>(1);
        dataList.add(loadingData);
        updateEntries(dataList);
    }

    public void setOffline(){
        //if there are search results, don't show.
        //we do show a toast, so the user will be notified either way
        if (getSearchResultSize() != 0){
            return;
        }
        List<WikiDataEntity> offlineList = new ArrayList<>(1);
        WikiDataEntity offline = new WikiDataEntity();
        offline.setWikiDataID(VIEW_TYPE_OFFLINE_WIKIDATA_ID);
        offlineList.add(offline);

        updateEntries(offlineList);
    }

    public void setOfflineAfterAdding(WikiDataEntity addedItem){
        List<WikiDataEntity> offlineList = new ArrayList<>(1);
        WikiDataEntity offline = new WikiDataEntity();
        offline.setWikiDataID(VIEW_TYPE_OFFLINE_AFTER_ADDING_WIKIDATA_ID);
        //we need the label to display to the user
        offline.setLabel(addedItem.getLabel());
        offlineList.add(offline);

        updateEntries(offlineList);
    }

    //a whole new set of data.
    public void updateEntries(List<WikiDataEntity> newList){
        //animate footer/header removal
        if (recommendationHeaderShown){
            notifyItemRemoved(0);
        }
        if (recommendationFooterShown){
            notifyItemRemoved(results.size()-2);
        }
        recommendationHeaderShown = false;
        recommendationFooterShown = false;
        //so we don't keep the same reference
        results = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void showRecommendations(List<WikiDataEntity> newList, boolean showFooter){
        Log.d("SEARCH RESULTS ADAPTER", "showing recommencations");
        //if the header and footer is shown, that means we are already showing the user
        // recommendations. so, instead of refreshing a new set of recommendation,
        //insert the data so we can animate it better.
        //for cases when the user adds an item from recommendations,
        //we set recommendationHeaderShown & recommendationFooterShown to false when we set the new WikiData ID
        if (recommendationHeaderShown && recommendationFooterShown){
            moreRecommendations(newList, showFooter);
            return;
        }

        //so we don't keep the same reference
        results = new ArrayList<>(newList);
        recommendationHeaderShown = true;
        recommendationFooterShown = showFooter;
        addHeaderData(results);
        if (showFooter) {
            addFooterData(results);
        }

        notifyDataSetChanged();
    }

    //add to current data (animate)
    private void moreRecommendations(List<WikiDataEntity> newList, boolean showFooter){
        //remove the header and footer views
        int prevListCt = results.size()-2;
        int newListCt = newList.size();
        //no need to add more
        if (prevListCt == newListCt){
            removeFooter();
            return;
        }

        int recommendationsAdded = newListCt - prevListCt;

        addHeaderData(newList);
        if (showFooter) {
            addFooterData(newList);
        } else {
            removeFooter();
        }
        results = new ArrayList<>(newList);
        notifyItemRangeInserted(prevListCt+1, recommendationsAdded);
    }

    public void setAddedWikiDataEntity(WikiDataEntity data){
        this.recommendationWikiDataEntity = data;
        headerLabel = data.getLabel();
        //to help with distinguishing between headers/footers shown
        //in more recommendations and headers/footers shown
        //in recommendations -> add
        recommendationHeaderShown = false;
        recommendationFooterShown = false;
    }

    private void addHeaderData(List<WikiDataEntity> data){
        WikiDataEntity headerPlaceHolder = new WikiDataEntity();
        headerPlaceHolder.setWikiDataID(VIEW_TYPE_RECOMMENDATION_HEADER_WIKIDATA_ID);
        data.add(0, headerPlaceHolder);
    }

    private void addFooterData(List<WikiDataEntity> data){
        WikiDataEntity footerPlaceHolder = new WikiDataEntity();
        footerPlaceHolder.setWikiDataID(VIEW_TYPE_RECOMMENDATION_FOOTER_WIKIDATA_ID);
        data.add(footerPlaceHolder);
    }

    private void removeFooter(){
        //remove only if the footer is shown
        if (recommendationFooterShown){
            WikiDataEntity footerView = results.get(results.size()-1);
            //just make sure the last item is a footer
            if (footerView.getWikiDataID().equals(VIEW_TYPE_RECOMMENDATION_FOOTER_WIKIDATA_ID)){
                results.remove(results.size()-1);
                notifyItemRemoved(results.size()-1);
            }
            recommendationFooterShown = false;
        }
    }



}
