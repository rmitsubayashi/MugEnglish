package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import com.linnca.pelicann.mainactivity.GUIUtils;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import pelicann.linnca.com.corefunctionality.lessonscript.StringUtils;
import pelicann.linnca.com.corefunctionality.lessonquestions.RandomCharacterGenerator;

public class Question_Spelling extends QuestionFragmentInterface {
    private TableLayout grid;
    private TextView questionTextView;
    private Button submitButton;
    private TextView answerTextView;
    private final StringBuilder answerText = new StringBuilder();
    private ImageButton deleteButton;
    private int rowCt;
    private int columnCt;
    private GridContainer[][] gridContainers;
    private final Stack<Button> answerButtons = new Stack<>();
    //we still want a button for spaces
    private final char space = '空';
    //since we have a lot of random variables,
    //save so we don't have to create redundant objects
    private final Random random = new Random(System.currentTimeMillis());

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
        setGrid();
        populateGridButtons(inflater);
        setButtonActionListeners();
        inflateFeedback(inflater);

        return view;
    }

    @Override
    protected String getResponse(View clickedView){
        return answerText.toString();
    }

    private void populateQuestion(){
        questionTextView.setText(questionData.getQuestion());
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

    private void populateGridButtons(LayoutInflater inflater){
        //first guarantee that all columns/rows will be populated.
        List<Integer> filledPositions = new ArrayList<>();
        String shuffledLetters = StringUtils.shuffleString(questionData.getAnswer());

        int letterMkr = 0;
        Set<Integer> toFillY = new HashSet<>();
        for (int i=0; i<columnCt; i++){
            toFillY.add(i);
        }
        //first the x position
        for (int x=0; x<rowCt; x++){
            int y = random.nextInt(columnCt);
            char letter = shuffledLetters.charAt(letterMkr);
            addLetterButton(inflater, x, y, letter);
            letterMkr++;
            filledPositions.add(y + x * columnCt);
            toFillY.remove(y);
        }

        //now y
        for (Integer y : toFillY){
            int x = random.nextInt(rowCt);
            /* for an n column square,
             * the minimum letters needed to populate X -> populate Y is
             * 2n - 1,
             * and the minimum letter requirement for a n column square is
             * (n-1)^2.
             * for n=1 and n=2, we might not get to the Y (crash).
             * not handling n<2 when populating the x will cause cases like
             *  _______
             * |_o_|___|
             * |_d_|___|
             * but for two letters, it's not significant
             * */
            if (letterMkr >= shuffledLetters.length())
                break;
            char letter = shuffledLetters.charAt(letterMkr);
            //these are guaranteed not to overlap another letter
            addLetterButton(inflater, x, y, letter);

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

        int answerLength = questionData.getAnswer().length();
        while (letterMkr < answerLength){
            char letter = shuffledLetters.charAt(letterMkr);
            int toFillPosition = toFillPositions.get(toFillMkr);
            int x = toFillPosition / columnCt;
            int y = toFillPosition % columnCt;
            addLetterButton(inflater, x, y, letter);

            letterMkr++;
            toFillMkr++;
        }

        //random buttons to make predictions harder
        toFillPositions = toFillPositions.subList(toFillMkr, toFillPositions.size());
        int randomButtonCt = random.nextInt(toFillPositions.size());
        RandomCharacterGenerator randomCharacterGenerator = new RandomCharacterGenerator();
        for (int i=0; i<randomButtonCt; i++){
            char letter = randomCharacterGenerator.getRandomCharacter();
            int toFillPosition = toFillPositions.get(i);
            int x = toFillPosition / columnCt;
            int y = toFillPosition % columnCt;
            addLetterButton(inflater, x, y, letter);
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

    private void addLetterButton(LayoutInflater inflater, int x, int y, char letter){
        final GridContainer container = gridContainers[x][y];
        final Button letterButton = (Button)inflater.inflate(R.layout.inflatable_question_spelling_letter_button, container, false);
        //no reason to have it uppercase
        letter = Character.toLowerCase(letter);
        final char letterToAddToTextView = letter;
        if (letter == ' '){
            letter = space;
        }
        letterButton.setText(Character.toString(letter));
        randomLetterButtonPlacement(letterButton);

        letterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answerText.append(letterToAddToTextView);
                answerTextView.setText(answerText.toString());
                letterButton.setTextColor(ContextCompat.getColor(getContext(), R.color.gray500));
                letterButton.setEnabled(false);
                answerButtons.add(letterButton);
            }
        });
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
    }

    private void setButtonActionListeners(){
        submitButton.setOnClickListener(getResponseListener());
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (answerButtons.size() == 0){
                    return;
                }

                Button button = answerButtons.pop();
                button.setEnabled(true);
                button.setTextColor(ThemeColorChanger.getColorFromAttribute(
                        R.attr.color500, getContext()));
                answerText.deleteCharAt(answerText.length()-1);
                answerTextView.setText(answerText.toString());
            }
        });

    }



}
