package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.widgets.GUIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Question_Spelling_Suggestive extends Question_General {
    private TableLayout grid;
    private TextView questionTextView;
    private Button submitButton;
    private TextView answerTextView;
    private ImageButton deleteButton;
    private int rowCt;
    private int columnCt;
    private GridContainer[][] gridContainers;
    private Button[] answerButtons;
    private int answerTextProgress = 0;
    //we still want a button for spaces
    private final char space = '空';
    //since we have a lot of random variables,
    //save so we don't have to create redundant objects
    private final Random random = new Random(System.currentTimeMillis());

    //most of the logic the same as question_spelling, but different in
    //significant ways
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_question_spelling, container, false);
        parentViewGroupForFeedback = view.findViewById(R.id.fragment_question_spelling);
        siblingViewGroupForFeedback = view.findViewById(R.id.question_spelling_main_layout);
        grid = view.findViewById(R.id.question_spelling_grid);
        questionTextView = view.findViewById(R.id.question_spelling_question);
        submitButton = view.findViewById(R.id.question_spelling_submit);
        answerTextView = view.findViewById(R.id.question_spelling_answer);
        deleteButton = view.findViewById(R.id.question_spelling_delete);

        populateQuestion();
        adjustLayout();
        setGrid();
        populateGridButtons(inflater);
        inflateFeedback(inflater);

        nextLetter();
        return view;
    }

    @Override
    protected String getResponse(View clickedView){
        return questionData.getAnswer().substring(0, answerTextProgress);
    }

    @Override
    protected int getMaxPossibleAttempts(){
        //technically not infinite
        return Question_General.UNLIMITED_ATTEMPTS;
    }

    @Override
    protected boolean disableChoiceAfterWrongAnswer(){
        return false;
    }

    private void populateQuestion(){
        questionTextView.setText(questionData.getQuestion());
        //we are showing the answer as default
        answerTextView.setText(questionData.getAnswer());
        answerTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.gray500));
    }

    private void adjustLayout(){
        //we don't need the submit button
        submitButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
    }

    private void setGrid(){
        int answerLength = questionData.getAnswer().length();

        rowCt = 1;
        columnCt = 1;
        int gridCt = 1;
        //right now, have at least one empty space.
        //may need to adjust
        while (answerLength >= gridCt){
            rowCt++;
            //if it goes too wide, stop and go down
            columnCt = columnCt < 5 ? columnCt + 1 : columnCt;
            gridCt = rowCt * columnCt;
        }

        gridContainers = new GridContainer[rowCt][columnCt];
        grid.setWeightSum(columnCt);

        for (int x=0; x<rowCt; x++){
            TableRow row = new TableRow(getContext());
            for (int y=0; y<columnCt; y++){
                GridContainer gridContainer = new GridContainer(getContext());
                TableRow.LayoutParams params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
                int margin = 12 - columnCt*2 > 0 ? 12 - columnCt*2 : 2;
                margin = GUIUtils.getDp(margin, getContext());
                params.setMargins(margin, margin, margin, margin);
                gridContainer.setLayoutParams(params);
                row.addView(gridContainer);
                gridContainers[x][y] = gridContainer;
            }
            grid.addView(row);
        }

    }

    //probably not the most efficient way but I'm tired...
    private class LetterWithIndex {
        LetterWithIndex(char letter, int index){
            this.letter = letter;
            this.index = index;
        }
        final char letter;
        final int index;
    }
    private void populateGridButtons(LayoutInflater inflater){
        //first guarantee that all columns/rows will be populated.
        List<Integer> filledPositions = new ArrayList<>();
        String answer =questionData.getAnswer();
        int answerLength = answer.length();
        answerButtons = new Button[answerLength];
        List<LetterWithIndex> lettersWithIndices = new ArrayList<>(answerLength);
        for (int i=0; i<answerLength; i++){
            lettersWithIndices.add(new LetterWithIndex(answer.charAt(i), i));
        }
        Collections.shuffle(lettersWithIndices);
        int letterMkr = 0;
        Set<Integer> toFillY = new HashSet<>();
        for (int i=0; i<columnCt; i++){
            toFillY.add(i);
        }
        //first the x position
        for (int x=0; x<rowCt; x++){
            int y = random.nextInt(columnCt);
            LetterWithIndex letterWithIndex = lettersWithIndices.get(letterMkr);
            addLetterButton(inflater, x, y, letterWithIndex);

            letterMkr++;
            filledPositions.add(y + x * columnCt);
            toFillY.remove(y);
        }

        //now y
        for (Integer y : toFillY){
            int x = random.nextInt(rowCt);
            LetterWithIndex letterWithIndex = lettersWithIndices.get(letterMkr);
            //these are guaranteed not to overlap another letter
            addLetterButton(inflater, x, y, letterWithIndex);

            letterMkr++;
            filledPositions.add(y + x * columnCt);
        }

        int gridCt = rowCt * columnCt;
        List<Integer> toFillPositions = new ArrayList<>();
        for (int i=0; i<gridCt; i++){
            toFillPositions.add(i);
        }
        toFillPositions.removeAll(filledPositions);
        Collections.shuffle(toFillPositions);
        int toFillMkr = 0;

        while (letterMkr < answerLength){
            LetterWithIndex letterWithIndex = lettersWithIndices.get(letterMkr);
            int toFillPosition = toFillPositions.get(toFillMkr);
            int x = toFillPosition / columnCt;
            int y = toFillPosition % columnCt;
            addLetterButton(inflater, x, y, letterWithIndex);

            letterMkr++;
            toFillMkr++;
        }
    }

    private void randomLetterButtonPlacement(Button button){
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int corner = random.nextInt(9);
        //0 1 2
        //3 4 5
        //6 7 8
        if (corner <= 2){
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else if (corner >= 6){
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        } else {
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        if (corner % 3 == 0){
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
        } else if (corner % 3 == 2){
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
        } else {
            params.addRule(RelativeLayout.CENTER_VERTICAL);
        }
        button.setLayoutParams(params);
    }

    private void addLetterButton(LayoutInflater inflater, int x, int y, LetterWithIndex letterWithIndex){
        final GridContainer container = gridContainers[x][y];
        final Button letterButton = (Button)inflater.inflate(R.layout.inflatable_question_spelling_letter_button, container, false);
        //no reason to have it uppercase
        char letter = letterWithIndex.letter;
        if (letter == ' '){
            letter = space;
        }
        letterButton.setText(Character.toString(letter));
        randomLetterButtonPlacement(letterButton);

        if (letterWithIndex.index != questionData.getAnswer().length()-1) {
            letterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nextLetter();
                }
            });
        } else {
            letterButton.setOnClickListener(getResponseListener());
        }

        ViewTreeObserver observer = container.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int containerWidth = container.getMeasuredWidth();
                letterButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, containerWidth/2);
                letterButton.setMinWidth(containerWidth/2);
                container.addView(letterButton);
            }
        });
        //disable initially
        letterButton.setEnabled(false);
        letterButton.setTextColor(ContextCompat.getColor(getContext(), R.color.gray500));
        answerButtons[letterWithIndex.index] = letterButton;
    }

    private void nextLetter(){
        Button nextLetterButton = answerButtons[answerTextProgress];
        nextLetterButton.setEnabled(true);
        nextLetterButton.setTextColor(ContextCompat.getColor(getContext(), R.color.lblue500));

        int prevButtonIndex = answerTextProgress-1;
        if (prevButtonIndex != -1){
            Button prevLetterButton = answerButtons[prevButtonIndex];
            prevLetterButton.setEnabled(false);
            prevLetterButton.setTextColor(ContextCompat.getColor(getContext(), R.color.gray500));
        }
        setAnswerSpannable();

        answerTextProgress++;
    }

    private void setAnswerSpannable(){
        //couldn't find a way to do this without creating a completely new spannable
        if (answerTextProgress == 0)
            return;
        SpannableString spannableString = new SpannableString(questionData.getAnswer());
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.orange500)),
                0, answerTextProgress, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        answerTextView.setText(spannableString);
    }

    @Override
    protected void doSomethingAfterResponse(){
        answerButtons[answerTextProgress-1].setTextColor(ContextCompat.getColor(getContext(), R.color.gray500));
        answerTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.orange500));
    }
}
