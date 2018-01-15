package pelicann.linnca.com.corefunctionality.lessonlist;

import pelicann.linnca.com.corefunctionality.lessondetails.LessonData;

public class LessonListRow {
    private final int lessonsPerRow = 3;
    private LessonData[] lessons = new LessonData[lessonsPerRow];
    private boolean isReview = false;

    public LessonListRow(){}

    public void setCol1(LessonData data){
        lessons[0] = data;
    }

    public void setCol2(LessonData data){
        lessons[1] = data;
    }

    public void setCol3(LessonData data){
        lessons[2] = data;
    }

    public void setReview(boolean isReview){
        this.isReview = isReview;
    }

    public boolean isReview(){
        return isReview;
    }

    public LessonData[] getLessons(){
        return lessons;
    }

}
