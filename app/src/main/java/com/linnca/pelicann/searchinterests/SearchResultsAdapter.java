package com.linnca.pelicann.searchinterests;

import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import java.util.ArrayList;
import java.util.List;

class SearchResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String VIEW_TYPE_RECOMMENDATION_HEADER_WIKIDATA_ID = "recommendation header";
    private final String VIEW_TYPE_RECOMMENDATION_FOOTER_WIKIDATA_ID = "recommendation footer";
    final String VIEW_TYPE_EMPTY_STATE_WIKIDATA_ID = "emptyState";
    final String VIEW_TYPE_LOADING_WIKIDATA_ID = "loading";
    private final String VIEW_TYPE_INITIAL_WIKIDATA_ID = "initial";

    private final int VIEW_TYPE_RECOMMENDATION_HEADER = 1;
    private final int VIEW_TYPE_RECOMMENDATION_FOOTER = 2;
    private final int VIEW_TYPE_NORMAL = 3;
    private final int VIEW_TYPE_EMPTY = 4;
    private final int VIEW_TYPE_LOADING = 5;
    private final int VIEW_TYPE_INITIAL = 6;

    private List<WikiDataEntryData> results = new ArrayList<>();
    private final SearchResultsAdapterListener searchResultsAdapterListener;

    private boolean recommendationHeaderShown = false;
    //footer (load more recommendations) is only shown
    // when the user can load more recommendations
    private boolean recommendationFooterShown = false;
    private String headerLabel = "";
    private WikiDataEntryData recommendationWikiDataEntryData;

    //prevent fast double clicks on any of the buttons
    private long lastClickTime = 0;

    interface SearchResultsAdapterListener {
        void onAddInterest(WikiDataEntryData data);
        void onLoadMoreRecommendations();
    }

    SearchResultsAdapter(SearchResultsAdapterListener listener){
        searchResultsAdapterListener = listener;
        //initial state
        WikiDataEntryData initialData = new WikiDataEntryData();
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
        WikiDataEntryData data = results.get(position);
        if (data.getWikiDataID().equals(VIEW_TYPE_RECOMMENDATION_HEADER_WIKIDATA_ID)){
            return VIEW_TYPE_RECOMMENDATION_HEADER;
        }
        if (data.getWikiDataID().equals(VIEW_TYPE_RECOMMENDATION_FOOTER_WIKIDATA_ID)){
            return VIEW_TYPE_RECOMMENDATION_FOOTER;
        }
        if (data.getWikiDataID().equals(VIEW_TYPE_EMPTY_STATE_WIKIDATA_ID)){
            return VIEW_TYPE_EMPTY;
        }
        if (data.getWikiDataID().equals(VIEW_TYPE_LOADING_WIKIDATA_ID)){
            return VIEW_TYPE_LOADING;
        }
        if (data.getWikiDataID().equals(VIEW_TYPE_INITIAL_WIKIDATA_ID)){
            return VIEW_TYPE_INITIAL;
        }
        return VIEW_TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_RECOMMENDATION_HEADER){
            itemView = inflater.inflate(R.layout.inflatable_search_interest_recommendations_header, parent, false);
            return new SearchResultsRecommendationHeaderViewHolder(itemView);
        }
        if (viewType == VIEW_TYPE_RECOMMENDATION_FOOTER){
            itemView = inflater.inflate(R.layout.inflatable_search_interest_recommendations_footer, parent, false);
            return new SearchResultsRecommendationFooterViewHolder(itemView);
        }
        if (viewType == VIEW_TYPE_EMPTY){
            itemView = inflater.inflate(R.layout.inflatable_search_interests_empty_state, parent, false);
            return new SearchResultsEmptyStateViewHolder(itemView);
        }
        if (viewType == VIEW_TYPE_LOADING){
            itemView = inflater.inflate(R.layout.inflatable_search_interest_loading, parent, false);
            return new RecyclerView.ViewHolder(itemView) {};
        }
        if (viewType == VIEW_TYPE_INITIAL){
            itemView = inflater.inflate(R.layout.inflatable_search_interest_initial_state, parent, false);
            return new RecyclerView.ViewHolder(itemView) {};
        }
        //everything else is a item
        itemView = inflater.inflate(R.layout.inflatable_search_interests_result_item, parent, false);
        return new SearchResultsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position){
        if (viewHolder instanceof SearchResultsViewHolder){
            WikiDataEntryData data = results.get(position);
            ((SearchResultsViewHolder) viewHolder).setLabel(data.getLabel());
            ((SearchResultsViewHolder) viewHolder).setDescription(data.getDescription());
            final WikiDataEntryData fData = data;
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
        } else if (viewHolder instanceof SearchResultsRecommendationFooterViewHolder){
            ((SearchResultsRecommendationFooterViewHolder) viewHolder)
                    .setButtonListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchResultsAdapterListener.onLoadMoreRecommendations();
                }
            });
        } else if (viewHolder instanceof SearchResultsEmptyStateViewHolder){
            WikiDataEntryData data = results.get(position);
            ((SearchResultsEmptyStateViewHolder) viewHolder).setQuery(data.getLabel());
        }
    }

    //the overridden getItemCount() includes extra items like
    // headers and footers
    int getSearchResultSize(){
        int resultCt = 0;
        for (WikiDataEntryData data : results){
            String wikiDataID = data.getWikiDataID();
            if (!wikiDataID.equals(VIEW_TYPE_LOADING_WIKIDATA_ID) &&
                    !wikiDataID.equals(VIEW_TYPE_EMPTY_STATE_WIKIDATA_ID) &&
                    !wikiDataID.equals(VIEW_TYPE_RECOMMENDATION_FOOTER_WIKIDATA_ID) &&
                    !wikiDataID.equals(VIEW_TYPE_RECOMMENDATION_HEADER_WIKIDATA_ID) &&
                    !wikiDataID.equals(VIEW_TYPE_INITIAL_WIKIDATA_ID)){
                resultCt++;
            }
        }
        return resultCt;
    }

    boolean isLoading(){
        boolean isLoading = false;
        for (WikiDataEntryData data : results){
            if (data.getWikiDataID().equals(VIEW_TYPE_LOADING_WIKIDATA_ID)){
                isLoading = true;
                break;
            }
        }
        return isLoading;
    }

    //a whole new set of data.
    void updateEntries(List<WikiDataEntryData> newList){
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

    void showRecommendations(List<WikiDataEntryData> newList, boolean showFooter){
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
    private void moreRecommendations(List<WikiDataEntryData> newList, boolean showFooter){
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

    void setRecommendationWikiDataEntryData(WikiDataEntryData data){
        this.recommendationWikiDataEntryData = data;
        headerLabel = data.getLabel();
        //to help with distinguishing between headers/footers shown
        //in more recommendations and headers/footers shown
        //in recommendations -> add
        recommendationHeaderShown = false;
        recommendationFooterShown = false;
    }

    private void addHeaderData(List<WikiDataEntryData> data){
        WikiDataEntryData headerPlaceHolder = new WikiDataEntryData();
        headerPlaceHolder.setWikiDataID(VIEW_TYPE_RECOMMENDATION_HEADER_WIKIDATA_ID);
        data.add(0, headerPlaceHolder);
    }

    private void addFooterData(List<WikiDataEntryData> data){
        WikiDataEntryData footerPlaceHolder = new WikiDataEntryData();
        footerPlaceHolder.setWikiDataID(VIEW_TYPE_RECOMMENDATION_FOOTER_WIKIDATA_ID);
        data.add(footerPlaceHolder);
    }

    private void removeFooter(){
        //remove only if the footer is shown
        if (recommendationFooterShown){
            WikiDataEntryData footerView = results.get(results.size()-1);
            //just make sure the last item is a footer
            if (footerView.getWikiDataID().equals(VIEW_TYPE_RECOMMENDATION_FOOTER_WIKIDATA_ID)){
                results.remove(results.size()-1);
                notifyItemRemoved(results.size()-1);
            }
            recommendationFooterShown = false;
        }
    }



}
