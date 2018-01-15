package com.linnca.pelicann.userprofile;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessondetails.LessonData;
import pelicann.linnca.com.corefunctionality.lessonlist.LessonListRow;

//holder for report card list cells.
//very similar to the lesson list view holders.
//if changing this, make sure to see if any changes are needed for the lesson list
class UserProfile_ReportCard_RowViewHolder extends RecyclerView.ViewHolder {
    private final ViewGroup col1Layout;
    private final TextView col1Icon;
    private final TextView col1Text;
    private final View col1TopRight;
    private final View col1Top;
    private final View col1Bottom;
    private final View col1Right;
    private final ViewGroup col2Layout;
    private final TextView col2Icon;
    private final TextView col2Text;
    private final View col2TopLeft;
    private final View col2TopRight;
    private final View col2Top;
    private final View col2Bottom;
    private final View col2Left;
    private final View col2Right;
    private final ViewGroup col3Layout;
    private final TextView col3Icon;
    private final TextView col3Text;
    private final View col3TopLeft;
    private final View col3Top;
    private final View col3Bottom;
    private final View col3Left;

    UserProfile_ReportCard_RowViewHolder(View itemView) {
        super(itemView);
        col1Layout = itemView.findViewById(R.id.user_profile_report_card_item_col1);
        col1Icon = itemView.findViewById(R.id.user_profile_report_card_list_item_col1_image);
        col1Text = itemView.findViewById(R.id.user_profile_report_card_list_item_col1_text);
        col1Right = itemView.findViewById(R.id.user_profile_report_card_list_item_col1_right);
        col1TopRight = itemView.findViewById(R.id.user_profile_report_card_list_item_col1_top_right);
        col1Top = itemView.findViewById(R.id.user_profile_report_card_list_item_col1_top);
        col1Bottom = itemView.findViewById(R.id.user_profile_report_card_list_item_col1_bottom);
        col2Layout = itemView.findViewById(R.id.user_profile_report_card_item_col2);
        col2Icon = itemView.findViewById(R.id.user_profile_report_card_list_item_col2_image);
        col2Text = itemView.findViewById(R.id.user_profile_report_card_list_item_col2_text);
        col2Right = itemView.findViewById(R.id.user_profile_report_card_list_item_col2_right);
        col2TopRight = itemView.findViewById(R.id.user_profile_report_card_list_item_col2_top_right);
        col2Left = itemView.findViewById(R.id.user_profile_report_card_list_item_col2_left);
        col2TopLeft = itemView.findViewById(R.id.user_profile_report_card_list_item_col2_top_left);
        col2Top = itemView.findViewById(R.id.user_profile_report_card_list_item_col2_top);
        col2Bottom = itemView.findViewById(R.id.user_profile_report_card_list_item_col2_bottom);
        col3Layout = itemView.findViewById(R.id.user_profile_report_card_item_col3);
        col3Icon = itemView.findViewById(R.id.user_profile_report_card_list_item_col3_image);
        col3Text = itemView.findViewById(R.id.user_profile_report_card_list_item_col3_text);
        col3Left = itemView.findViewById(R.id.user_profile_report_card_list_item_col3_left);
        col3TopLeft = itemView.findViewById(R.id.user_profile_report_card_list_item_col3_top_left);
        col3Top = itemView.findViewById(R.id.user_profile_report_card_list_item_col3_top);
        col3Bottom = itemView.findViewById(R.id.user_profile_report_card_list_item_col3_bottom);
    }

    void setRowInfo(LessonListRow row) {
        final LessonData[] data = row.getLessons();
        ViewGroup[] colLayouts = new ViewGroup[]{col1Layout, col2Layout, col3Layout};
        TextView[] colTextViews =new TextView[]{col1Text, col2Text, col3Text};
        TextView[] colIcons = new TextView[]{col1Icon, col2Icon, col3Icon};
        for (int i=0; i<3; i++) {
            final LessonData colData = data[i];
            ViewGroup colLayout = colLayouts[i];
            TextView colText = colTextViews[i];
            TextView colIcon = colIcons[i];
            if (colData == null) {
                colLayout.setOnClickListener(null);
                colLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return true;
                    }
                });
                colText.setText("");
                colIcon.setText(null);
            } else {
                colLayout.setOnTouchListener(null);
                colText.setText(colData.getTitle());
                colIcon.setText("");
            }
        }
    }

    void setRowData(int rowNumber, int averageScore, int scoreToClear, final UserProfile_ReportCardAdapter.ReportCardListener listener){
        ViewGroup[] colLayouts = new ViewGroup[]{col1Layout, col2Layout, col3Layout};
        TextView[] colTextViews =new TextView[]{col1Text, col2Text, col3Text};
        TextView[] colIcons = new TextView[]{col1Icon, col2Icon, col3Icon};
        Context context = itemView.getContext();
        ViewGroup colLayout = colLayouts[rowNumber-1];
        TextView colText = colTextViews[rowNumber-1];
        TextView colIcon = colIcons[rowNumber-1];
        boolean colCleared = averageScore >= scoreToClear;
        boolean colActive = averageScore != UserProfile_ReportCardAdapter.NOT_CLEARED;

        colLayout.setOnClickListener(
                colActive ?
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                listener.onItemClicked();
                            }
                        } : null
        );
        int color = colCleared ?
                ThemeColorChanger.getColorFromAttribute(R.attr.color500, context) :
                ContextCompat.getColor(context, R.color.gray500);
        colText.setTextColor(color);
        //if the user hasn't unlocked it, display 0
        if (!colActive)
            averageScore = 0;
        String averageScoreString = Integer.toString(averageScore);
        if (averageScoreString.length() == 1){
            colIcon.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
        } else if (averageScoreString.length() == 2){
            colIcon.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        } else {
            colIcon.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        }
        colIcon.setTextColor(color);
        //animate since there is a lag between fetching and updating data
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(context.getResources().getInteger(android.R.integer.config_shortAnimTime));
        colIcon.setText(averageScoreString);
        colIcon.startAnimation(fadeIn);
    }

    void connectRows(LessonListRow currentRow, LessonListRow rowBefore, LessonListRow rowAfter){
        LessonData[] rowLessons = currentRow.getLessons();
        //whether we should extend the bottom of each item of the current row
        if (rowAfter != null) {
            View[] bottomViews = new View[]{col1Bottom, col2Bottom, col3Bottom};
            int rowLoopCt = rowLessons.length;
            for (int i=0; i<rowLoopCt; i++) {
                boolean extendBottom = false;
                LessonData lesson = rowLessons[i];
                if (lesson == null){
                    bottomViews[i].setVisibility(View.INVISIBLE);
                    continue;
                }
                LessonData[] afterLessons = rowAfter.getLessons();
                for (LessonData afterLesson : afterLessons) {
                    if (afterLesson != null &&
                            afterLesson.getPrerequisiteKeys() != null &&
                            afterLesson.getPrerequisiteKeys().contains(lesson.getKey())) {
                        extendBottom = true;
                        break;
                    }
                }
                bottomViews[i].setVisibility(extendBottom ? View.VISIBLE : View.INVISIBLE);
            }
        } else {
            col1Bottom.setVisibility(View.INVISIBLE);
            col2Bottom.setVisibility(View.INVISIBLE);
            col3Bottom.setVisibility(View.INVISIBLE);
        }

        //whether we should connect the sides
        if (rowLessons[0] == null || rowLessons[1] == null) {
            col1Right.setVisibility(View.INVISIBLE);
            col2Left.setVisibility(View.INVISIBLE);
        } else if ((rowLessons[0].getPrerequisiteKeys() != null &&
                rowLessons[0].getPrerequisiteKeys().contains(rowLessons[1].getKey())) ||
                (rowLessons[1].getPrerequisiteKeys() != null &&
                        rowLessons[1].getPrerequisiteKeys().contains(rowLessons[0].getKey()))){
            col1Right.setVisibility(View.VISIBLE);
            col2Left.setVisibility(View.VISIBLE);
        } else {
            col1Right.setVisibility(View.INVISIBLE);
            col2Left.setVisibility(View.INVISIBLE);
        }

        if (rowLessons[1] == null || rowLessons[2] == null){
            col2Right.setVisibility(View.INVISIBLE);
            col3Left.setVisibility(View.INVISIBLE);
        } else if ((rowLessons[1].getPrerequisiteKeys() != null &&
                rowLessons[1].getPrerequisiteKeys().contains(rowLessons[2].getKey())) ||
                (rowLessons[2].getPrerequisiteKeys() != null &&
                        rowLessons[2].getPrerequisiteKeys().contains(rowLessons[1].getKey()))){
            col2Right.setVisibility(View.VISIBLE);
            col3Left.setVisibility(View.VISIBLE);
        } else {
            col2Right.setVisibility(View.INVISIBLE);
            col3Left.setVisibility(View.INVISIBLE);
        }

        //whether we should extend the top of each item of the current row
        if (rowBefore != null) {
            View[] topViews = new View[]{col1Top, col2Top, col3Top};
            int rowLoopCt = rowLessons.length;
            for (int i=0; i<rowLoopCt; i++) {
                boolean extendTop = false;
                LessonData lesson = rowLessons[i];
                if (lesson == null || lesson.getPrerequisiteKeys() == null){
                    topViews[i].setVisibility(View.INVISIBLE);
                    continue;
                }
                LessonData[] beforeLessons = rowBefore.getLessons();
                List<String> lessonPrerequisites =lesson.getPrerequisiteKeys();
                for (LessonData beforeLesson: beforeLessons) {
                    if (beforeLesson != null &&
                            lessonPrerequisites.contains(beforeLesson.getKey())) {
                        extendTop = true;
                        break;
                    }
                }
                topViews[i].setVisibility(extendTop ? View.VISIBLE : View.INVISIBLE);
            }
        } else {
            col1Top.setVisibility(View.INVISIBLE);
            col2Top.setVisibility(View.INVISIBLE);
            col3Top.setVisibility(View.INVISIBLE);
        }

        //whether to connect top left/top right
        boolean leftVisible = false;
        boolean rightVisible = false;

        if (rowBefore != null) {
            LessonData[] rowBeforeLessons = rowBefore.getLessons();
            LessonData col1Lesson = rowLessons[0];
            if (col1Lesson != null &&
                    col1Lesson.getPrerequisiteKeys() != null){
                if (rowBeforeLessons[1] != null &&
                        col1Lesson.getPrerequisiteKeys().contains(rowBeforeLessons[1].getKey())){
                    leftVisible = true;
                }
                if (rowBeforeLessons[2] != null &&
                        col1Lesson.getPrerequisiteKeys().contains(rowBeforeLessons[2].getKey())){
                    leftVisible = true;
                    rightVisible = true;
                }
            }
            LessonData col2Lesson = rowLessons[1];
            if (col2Lesson != null &&
                    col2Lesson.getPrerequisiteKeys() != null){
                if (rowBeforeLessons[0] != null &&
                        col2Lesson.getPrerequisiteKeys().contains(rowBeforeLessons[0].getKey())){
                    leftVisible = true;
                }
                if (rowBeforeLessons[2] != null &&
                        col2Lesson.getPrerequisiteKeys().contains(rowBeforeLessons[2].getKey())){
                    rightVisible = true;
                }
            }
            LessonData col3Lesson = rowLessons[2];
            if (col3Lesson != null &&
                    col3Lesson.getPrerequisiteKeys() != null){
                if (rowBeforeLessons[1] != null &&
                        col3Lesson.getPrerequisiteKeys().contains(rowBeforeLessons[1].getKey())){
                    rightVisible = true;
                }
                if (rowBeforeLessons[0] != null &&
                        col3Lesson.getPrerequisiteKeys().contains(rowBeforeLessons[0].getKey())){
                    leftVisible = true;
                    rightVisible = true;
                }
            }
        }

        if (leftVisible){
            col1TopRight.setVisibility(View.VISIBLE);
            col2TopLeft.setVisibility(View.VISIBLE);
        } else {
            col1TopRight.setVisibility(View.INVISIBLE);
            col2TopLeft.setVisibility(View.INVISIBLE);
        }
        if (rightVisible){
            col2TopRight.setVisibility(View.VISIBLE);
            col3TopLeft.setVisibility(View.VISIBLE);
        } else {
            col2TopRight.setVisibility(View.INVISIBLE);
            col3TopLeft.setVisibility(View.INVISIBLE);
        }
    }
}