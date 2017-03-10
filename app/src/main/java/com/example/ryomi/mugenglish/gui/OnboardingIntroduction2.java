package com.example.ryomi.mugenglish.gui;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.WikiDataAPISearchConnector;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.datawrappers.WikiDataEntryData;
import com.example.ryomi.mugenglish.gui.widgets.GUIUtils;
import com.example.ryomi.mugenglish.gui.widgets.OnboardingIntroductionBundle;
import com.example.ryomi.mugenglish.gui.widgets.OnboardingNextListener;
import com.example.ryomi.mugenglish.userinterestcontrols.EntitySearcher;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OnboardingIntroduction2 extends Fragment{
    private SearchView searchview;
    private LinearLayout choicesLayout;
    private ProgressBar loading;
    private LayoutInflater inflater;
    private OnboardingNextListener nextListener;

    public OnboardingIntroduction2(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.inflater = inflater;
        View view = inflater.inflate(R.layout.fragment_onboarding_introduction2, container, false);
        choicesLayout = (LinearLayout) view.findViewById(R.id.onboarding_introduction2_choices);
        loading = (ProgressBar) view.findViewById(R.id.onboarding_introduction2_loading);
        searchview = (SearchView)view.findViewById(R.id.onboarding_introduction2_searchview);
        searchview.setSubmitButtonEnabled(true);
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        animate();
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

    private void animate(){
        if (getView() == null)
            return;
        final TextView dialogue = (TextView) getView().findViewById(R.id.onboarding_introduction2_dialogue);
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        Animation animation = AnimationUtils.loadAnimation(
                                OnboardingIntroduction2.this.getContext(), R.anim.slide_out_left);
                        animation.setDuration(500);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                dialogue.setVisibility(View.GONE);
                                animate2();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        dialogue.startAnimation(animation);

                    }
                }, 2000
        );
    }

    private void animate2(){
        if (getView() == null)
            return;
        final TextView smallDialogue = (TextView) getView().findViewById(R.id.onboarding_introduction2_dialogue_small);
        Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.slide_in_left);
        //slower than the slide out since this looks faster because it's slower
        animation.setDuration(1000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                smallDialogue.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animate3();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        smallDialogue.startAnimation(animation);
    }

    private void animate3(){
        if (getView() == null)
            return;
        TextView instructions = (TextView)getView().findViewById(R.id.onboarding_introduction2_instructions);
        instructions.setVisibility(View.VISIBLE);
        searchview.setVisibility(View.VISIBLE);
    }

    private class NameSearchTask extends AsyncTask<String, Integer, List<OnboardingIntroductionBundle> > {
        @Override
        protected void onPreExecute(){
            loading.setVisibility(View.VISIBLE);
            choicesLayout.removeAllViews();
        }

        @Override
        protected List<OnboardingIntroductionBundle> doInBackground(String... params){
            String searchName = params[0];
            WikiDataAPISearchConnector searchConnector = new WikiDataAPISearchConnector(WikiBaseEndpointConnector.JAPANESE);
            EntitySearcher entitySearcher = new EntitySearcher(searchConnector);
            List<OnboardingIntroductionBundle> result = new ArrayList<>();
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
                        String genderEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "genderLabel");
                        String englishNameLabel = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
                        OnboardingIntroductionBundle OnboardingIntroductionBundle = new OnboardingIntroductionBundle(data, genderEN, englishNameLabel);
                        result.add(OnboardingIntroductionBundle);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<OnboardingIntroductionBundle> result){
            handleSearchResult(result);
        }
    }

    private String formatQuery(String id){
        return "SELECT ?genderLabel ?personLabel " +
                "WHERE " +
                "{ " +
                "?person wdt:P31 wd:Q5 . " +
                "  ?person wdt:P21 ?gender . " +
                "SERVICE wikibase:label { bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' } " +
                "  BIND ( wd:"+id+" AS ?person) " +
                "}";
    }

    private void handleSearchResult(List<OnboardingIntroductionBundle> results){
        loading.setVisibility(View.GONE);
        if (results == null){

        } else {
            if (results.size() == 1){
                //go ahead and go to next screen
                Bundle bundle = new Bundle();
                bundle.putSerializable(null, results.get(0));
                nextListener.nextScreen(bundle);
            } else {
                //have the user choose which one to go with
                populateChoices(results);
            }
        }

        //go to next page?
    }

    private void populateChoices(List<OnboardingIntroductionBundle> results){
        for (OnboardingIntroductionBundle result : results){
            Button button = (Button)inflater.inflate(
                    R.layout.inflatable_onboarding_introduction2_choice, choicesLayout, false);
            button.setText(result.getData().getLabel());
            final Bundle bundle = new Bundle();
            bundle.putSerializable(null, result);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nextListener.nextScreen(bundle);
                }
            });
            choicesLayout.addView(button);
        }
    }

    //implements the listeners for some of the preferences
    private void implementListeners(Context context) {
        try {
            nextListener = (OnboardingNextListener)context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString() + " must implement nextListener");
        }
    }
}
