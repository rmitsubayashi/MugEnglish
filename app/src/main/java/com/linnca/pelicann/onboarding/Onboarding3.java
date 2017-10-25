package com.linnca.pelicann.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.userinterestcontrols.StarterPacks;

public class Onboarding3 extends Fragment {
    private ViewGroup manButtonLayout;
    private TextView manLabel;
    private ImageView manImage;
    private ViewGroup womanButtonLayout;
    private TextView womanLabel;
    private ImageView womanImage;
    private ViewGroup boyButtonLayout;
    private TextView boyLabel;
    private ImageView boyImage;
    private ViewGroup girlButtonLayout;
    private TextView girlLabel;
    private ImageView girlImage;
    
    private final String SAVED_STATE_CURRENT_SELECTION = "currentSelection";
    
    private int currentSelection = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_onboarding3, parent, false);
        manButtonLayout = view.findViewById(R.id.onboarding3_man_layout);
        manLabel = view.findViewById(R.id.onboarding3_man_text);
        manImage = view.findViewById(R.id.onboarding3_man_image);
        womanButtonLayout = view.findViewById(R.id.onboarding3_woman_layout);
        womanLabel = view.findViewById(R.id.onboarding3_woman_text);
        womanImage = view.findViewById(R.id.onboarding3_woman_image);
        boyButtonLayout = view.findViewById(R.id.onboarding3_boy_layout);
        boyLabel = view.findViewById(R.id.onboarding3_boy_text);
        boyImage = view.findViewById(R.id.onboarding3_boy_image);
        girlButtonLayout = view.findViewById(R.id.onboarding3_girl_layout);
        girlLabel = view.findViewById(R.id.onboarding3_girl_text);
        girlImage = view.findViewById(R.id.onboarding3_girl_image);

        setListeners();

        if (savedInstanceState != null){
            int savedSelection = savedInstanceState.getInt(SAVED_STATE_CURRENT_SELECTION);
            select(savedSelection);
            currentSelection = savedSelection;
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_STATE_CURRENT_SELECTION, currentSelection);
    }

    public int getCurrentSelection(){
        return currentSelection;
    }

    private void setListeners(){
        manButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select(StarterPacks.MAN);
            }
        });
        womanButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select(StarterPacks.WOMAN);
            }
        });
        boyButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select(StarterPacks.BOY);
            }
        });
        girlButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select(StarterPacks.GIRL);
            }
        });
    }
    
    private void select(int newSelection){
        deselect(currentSelection);
        switch (newSelection){
            case StarterPacks.MAN :
                selectImage(manImage);
                selectLabel(manLabel);
                break;
            case StarterPacks.WOMAN :
                selectImage(womanImage);
                selectLabel(womanLabel);
                break;
            case StarterPacks.BOY :
                selectImage(boyImage);
                selectLabel(boyLabel);
                break;
            case StarterPacks.GIRL :
                selectImage(girlImage);
                selectLabel(girlLabel);
                break;
            default:
                return;
        }

        currentSelection = newSelection;
    }

    private void selectImage(ImageView imageView){
        imageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.lblue500));
    }

    private void selectLabel(TextView textView){
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.lblue500));
    }
    
    private void deselect(int selection){
        if (selection == -1)
            return;
        switch (selection){
            case StarterPacks.MAN :
                deselectImage(manImage);
                deselectLabel(manLabel);
                break;
            case StarterPacks.WOMAN :
                deselectImage(womanImage);
                deselectLabel(womanLabel);
                break;
            case StarterPacks.BOY :
                deselectImage(boyImage);
                deselectLabel(boyLabel);
                break;
            case StarterPacks.GIRL :
                deselectImage(girlImage);
                deselectLabel(girlLabel);
                break;
            default:
                deselectImage(manImage);
                deselectLabel(manLabel);
                deselectImage(womanImage);
                deselectLabel(womanLabel);
                deselectImage(boyImage);
                deselectLabel(boyLabel);
                deselectImage(girlImage);
                deselectLabel(girlLabel);
        }
    }
    
    private void deselectImage(ImageView imageView){
        imageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.gray500));
    }
    
    private void deselectLabel(TextView textView){
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.gray500));
    }
}
