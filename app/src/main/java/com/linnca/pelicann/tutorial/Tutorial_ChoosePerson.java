package com.linnca.pelicann.tutorial;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataAPISearchConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.mainactivity.widgets.GUIUtils;
import com.linnca.pelicann.userinterestcontrols.EntitySearcher;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Tutorial_ChoosePerson extends Fragment {
    private ViewGroup mainLayout;
    private Button searchButton;
    private SearchView searchView;
    private TextView searchError;
    private ProgressBar loading;
    private Tutorial_ChoosePersonListener listener;

    private final String SAVED_STATE_COMPLETED = "completed";

    private boolean completed = false;

    private int errorCount = 0;

    interface Tutorial_ChoosePersonListener {
        void choosePersonToConfirmPerson(OnboardingPersonBundle bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tutorial_choose_person, container, false);
        mainLayout = view.findViewById(R.id.tutorial_choose_person_main_layout);
        searchButton = view.findViewById(R.id.tutorial_choose_person_search_button);
        loading = view.findViewById(R.id.tutorial_choose_person_loading);
        searchView = view.findViewById(R.id.tutorial_choose_person_searchview);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(false);
        if (savedInstanceState != null){
            completed = savedInstanceState.getBoolean(SAVED_STATE_COMPLETED);
        }
        if (completed){
            disableInteractions();
            return view;
        }

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                NameSearchTask task = new NameSearchTask();
                task.execute(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //do not autocomplete since this takes a lot of transactions
                return false;
            }
        });
        searchError = view.findViewById(R.id.tutorial_choose_person_search_error);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = searchView.getQuery().toString();
                NameSearchTask task = new NameSearchTask();
                task.execute(query);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        implementListeners(context);
    }

    //must implement to account for lower APIs
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        implementListeners(activity);
    }

    //implements the listeners for some of the preferences
    private void implementListeners(Context context) {
        try {
            listener = (Tutorial_ChoosePersonListener) context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString() + " must implement listener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_STATE_COMPLETED, completed);
    }

    private class NameSearchTask extends AsyncTask<String, Integer, List<OnboardingPersonBundle> > {
        @Override
        protected void onPreExecute(){
            loading.setVisibility(View.VISIBLE);
            searchError.setVisibility(View.GONE);
            disableInteractions();
        }

        @Override
        protected List<OnboardingPersonBundle> doInBackground(String... params){
            String searchName = params[0];
            WikiDataAPISearchConnector searchConnector = new WikiDataAPISearchConnector(WikiBaseEndpointConnector.JAPANESE);
            EntitySearcher entitySearcher = new EntitySearcher(searchConnector);
            List<OnboardingPersonBundle> result = new ArrayList<>();
            List<WikiDataEntryData> searchResult;
            try {
                searchResult = entitySearcher.search(searchName, EntitySearcher.LIMIT);
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }

            WikiDataSPARQLConnector connector = new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE);
            //check if human
            for ( WikiDataEntryData data : searchResult ){
                String wikiDataID = data.getWikiDataID();
                String query = formatQuery(wikiDataID);
                try {
                    Document document = connector.fetchDOMFromGetRequest(query);
                    int nodeCt = WikiDataSPARQLConnector.countResults(document);
                    //this result is not a human with a gender so remove
                    if (nodeCt != 0){
                        //add gender
                        NodeList allResults = document.getElementsByTagName(
                                WikiDataSPARQLConnector.RESULT_TAG
                        );
                        Node head = allResults.item(0);
                        String genderID = SPARQLDocumentParserHelper.findValueByNodeName(head, "gender");
                        genderID = LessonGeneratorUtils.stripWikidataID(genderID);
                        int gender = OnboardingPersonBundle.getGender(genderID);
                        String englishNameLabel = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabelEN");
                        String japaneseNameLabel = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
                        OnboardingPersonBundle bundle = new OnboardingPersonBundle(data, gender, englishNameLabel, japaneseNameLabel);
                        result.add(bundle);
                        //once we get a result, stop.
                        //we are ignoring other less popular results
                        break;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<OnboardingPersonBundle> result){
            handleSearchResult(result);
        }
    }

    private String formatQuery(String id){
        //get everyone with a gender and person
        return "SELECT ?gender ?personLabel ?personLabelEN " +
                "WHERE " +
                "{ " +
                "  {?person wdt:P31 wd:Q5} UNION " + //is human
                "  {?person wdt:P31 wd:Q15632617} ." + //or fictional human
                "  ?person wdt:P21 ?gender . " + //has a gender
                "  ?person rdfs:label ?personLabelEN . " +
                "  FILTER (LANG(?personLabelEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "SERVICE wikibase:label { bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "' } " +
                "  BIND ( wd:"+id+" AS ?person) " +
                "}";
    }

    private void handleSearchResult(List<OnboardingPersonBundle> results){
        if (!isVisible()){
            return;
        }
        loading.setVisibility(View.GONE);
        if (results == null || results.size() == 0){
            showError();
            //allow the user to click again
            enableInteractions();
        } else {
            if (results.size() > 0){
                completed = true;
                GUIUtils.hideKeyboard(searchView);
                //go ahead and go to next screen
                //if there is more than one result, go with the first one.
                //reasoning ->
                // the non-popular names most likely have no descriptions
                // or only English ones.
                // since this is only to get the user to use the app,
                // don't display something that will discourage the user from
                // moving on
                listener.choosePersonToConfirmPerson(results.get(0));
            }
        }
    }

    private void showError(){
        searchError.setVisibility(View.VISIBLE);
        if (errorCount == 0){
            searchError.setText(R.string.tutorial_choose_person_not_found1);
        } else {
            searchError.setText(R.string.tutorial_choose_person_not_found2);
        }
        errorCount++;
    }

    private void disableInteractions(){
        //make sure a person can't submit a query while fetching data
        GUIUtils.hideKeyboard(searchView);
        mainLayout.setAlpha(0.5f);
        searchView.clearFocus();
        disableView(searchView);
        searchButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        searchButton.setEnabled(false);
    }

    private void enableInteractions(){
        //undo everything in disableInteractions()
        mainLayout.setAlpha(1f);
        searchView.requestFocus();
        enableView(searchView);
        searchButton.setOnTouchListener(null);
        searchButton.setEnabled(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            searchButton.setElevation(2);
        }
    }

    private void disableView(View view) {
        view.setClickable(false);
        //setting clickable to false doesn't do anything to listeners attached to buttons
        //we can set the listeners to null but we still can touch the buttons
        // and they will hover.
        //setting enabled to false changes some of the buttons' backgrounds so we want to avoid that/
        //so just intercept all touch events
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                disableView(child);
            }
        }
    }

    private void enableView(View view) {
        view.setClickable(true);
        //setting clickable to false doesn't do anything to listeners attached to buttons
        //we can set the listeners to null but we still can touch the buttons
        // and they will hover.
        //setting enabled to false changes some of the buttons' backgrounds so we want to avoid that/
        //so just intercept all touch events
        view.setOnTouchListener(null);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                enableView(child);
            }
        }
    }


}
