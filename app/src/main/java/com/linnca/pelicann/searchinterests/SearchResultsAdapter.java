package com.linnca.pelicann.searchinterests;

import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import java.util.ArrayList;
import java.util.List;

class SearchResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String VIEW_TYPE_HEADER_WIKIDATA_ID = "header";
    private final String VIEW_TYPE_FOOTER_WIKIDATA_ID = "footer";
    final String VIEW_TYPE_EMPTY_STATE_WIKIDATA_ID = "emptyState";

    private final int VIEW_TYPE_HEADER = 1;
    private final int VIEW_TYPE_FOOTER = 2;
    private final int VIEW_TYPE_NORMAL = 3;
    private final int VIEW_TYPE_EMPTY = 4;

    private List<WikiDataEntryData> results = new ArrayList<>();
    private final SearchResultsAdapterListener searchResultsAdapterListener;

    private boolean showHeader = false;
    private boolean showFooter = false;
    private String headerLabel = "";
    private WikiDataEntryData recommendationWikiDataEntryData;

    //prevent fast double clicks on any of the buttons
    private long lastClickTime = 0;

    interface SearchResultsAdapterListener {
        void onAddInterest(WikiDataEntryData data);
        void onLoadMoreRecommendations(WikiDataEntryData wikiDataEntryData);
    }

    SearchResultsAdapter(SearchResultsAdapterListener listener){
        searchResultsAdapterListener = listener;
    }

    @Override
    public int getItemCount(){ return results.size(); }

    @Override
    public long getItemId(int position){ return results.get(position).hashCode(); }

    @Override
    public int getItemViewType(int position){
        WikiDataEntryData data = results.get(position);
        if (data.getWikiDataID().equals(VIEW_TYPE_HEADER_WIKIDATA_ID)){
            return VIEW_TYPE_HEADER;
        }
        if (data.getWikiDataID().equals(VIEW_TYPE_FOOTER_WIKIDATA_ID)){
            return VIEW_TYPE_FOOTER;
        }
        if (data.getWikiDataID().equals(VIEW_TYPE_EMPTY_STATE_WIKIDATA_ID)){
            return VIEW_TYPE_EMPTY;
        }
        return VIEW_TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER){
            itemView = inflater.inflate(R.layout.inflatable_search_interest_recommendations_header, parent, false);
            return new SearchResultsHeaderViewHolder(itemView);
        }
        if (viewType == VIEW_TYPE_FOOTER){
            itemView = inflater.inflate(R.layout.inflatable_search_interest_recommendations_footer, parent, false);
            return new SearchResultsFooterViewHolder(itemView);
        }
        if (viewType == VIEW_TYPE_EMPTY){
            itemView = inflater.inflate(R.layout.inflatable_search_interests_empty_state, parent, false);
            return new SearchResultsEmptyStateViewHolder(itemView);
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
        } else if (viewHolder instanceof SearchResultsHeaderViewHolder){
            ((SearchResultsHeaderViewHolder) viewHolder).setTitle(headerLabel);
        } else if (viewHolder instanceof SearchResultsFooterViewHolder){
            ((SearchResultsFooterViewHolder) viewHolder).setButtonListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchResultsAdapterListener.onLoadMoreRecommendations(recommendationWikiDataEntryData);
                }
            });
        } else if (viewHolder instanceof SearchResultsEmptyStateViewHolder){
            WikiDataEntryData data = results.get(position);
            ((SearchResultsEmptyStateViewHolder) viewHolder).setQuery(data.getLabel());
        }
    }

    //a whole new set of data.
    void updateEntries(List<WikiDataEntryData> newList){
        //animate footer/header removal
        if (showHeader){
            notifyItemRemoved(0);
        }
        if (showFooter){
            notifyItemRemoved(results.size()-2);
        }
        showHeader = false;
        showFooter = false;
        //so we don't keep the same reference
        results = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    void showRecommendations(List<WikiDataEntryData> newList){
        //if the header and footer is shown, that means we are already showing the user
        // recommendations. so, instead of refreshing a new set of recommendation,
        //insert the data so we can animate it better.
        //for cases when the user adds an item from recommendations,
        //we set showHeader & showFooter to false when we set the new WikiData ID
        if (showHeader && showFooter){
            moreRecommendations(newList);
            return;
        }

        //so we don't keep the same reference
        results = new ArrayList<>(newList);
        showHeader = true;
        showFooter = true;
        addHeader(results);
        addFooter(results);

        notifyDataSetChanged();
    }

    //add to current data (animate)
    private void moreRecommendations(List<WikiDataEntryData> newList){
        //remove the header and footer views
        int prevListCt = results.size()-2;
        int newListCt = newList.size();
        //no need to add more
        if (prevListCt == newListCt){
            return;
        }

        int recommendationsAdded = newListCt - prevListCt;


        results = new ArrayList<>(newList);
        addHeader(results);
        addFooter(results);
        notifyItemRangeInserted(prevListCt+1, recommendationsAdded);
    }

    void setRecommendationWikiDataEntryData(WikiDataEntryData data){
        this.recommendationWikiDataEntryData = data;
        headerLabel = data.getLabel();
        //to help with distinguishing between headers/footers shown
        //in more recommendations and headers/footers shown
        //in recommendations -> add
        showHeader = false;
        showFooter = false;
    }

    private void addHeader(List<WikiDataEntryData> data){
        WikiDataEntryData headerPlaceHolder = new WikiDataEntryData();
        headerPlaceHolder.setWikiDataID(VIEW_TYPE_HEADER_WIKIDATA_ID);
        data.add(0, headerPlaceHolder);
    }

    private void addFooter(List<WikiDataEntryData> data){
        WikiDataEntryData footerPlaceHolder = new WikiDataEntryData();
        footerPlaceHolder.setWikiDataID(VIEW_TYPE_FOOTER_WIKIDATA_ID);
        data.add(footerPlaceHolder);
    }

    void removeFooter(){
        if (showFooter){
            WikiDataEntryData footerView = results.get(results.size()-1);
            //just make sure the last item is a footer
            if (footerView.getWikiDataID().equals(VIEW_TYPE_FOOTER_WIKIDATA_ID)){
                results.remove(results.size()-1);
                notifyItemRemoved(results.size());
            }
        }
    }



}
