package com.linnca.pelicann.tutorial;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.linnca.pelicann.R;

public class Tutorial_ConfirmPerson extends Fragment {
    private Tutorial_ConfirmPersonListener listener;
    public static String BUNDLE_SELECTED_PERSON = "confirmPersonSelectedPerson";
    private OnboardingPersonBundle personBundle;

    interface Tutorial_ConfirmPersonListener {
        void confirmPersonToLessonDetails();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        personBundle = (OnboardingPersonBundle)getArguments().getSerializable(BUNDLE_SELECTED_PERSON);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tutorial_confirm_person, container, false);
        Button start = view.findViewById(R.id.tutorial_confirm_person_start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for now, this will go to theme list
                listener.confirmPersonToLessonDetails();
            }
        });
        TextView titleTextView = view.findViewById(R.id.tutorial_confirm_person_title);
        String genderString;
        if (personBundle != null && personBundle.getGender() == OnboardingPersonBundle.GENDER_FEMALE){
            genderString = getString(R.string.tutorial_confirm_person_gender_female);
        } else {
            genderString = getString(R.string.tutorial_confirm_person_gender_male);
        }

        titleTextView.setText(getString(R.string.tutorial_confirm_person_title, personBundle.getJapaneseName(), genderString));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        implementListeners(context);
    }

    //implements the listeners for some of the preferences
    private void implementListeners(Context context) {
        try {
            listener = (Tutorial_ConfirmPersonListener) context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString() + " must implement nextListener");
        }
    }
}
