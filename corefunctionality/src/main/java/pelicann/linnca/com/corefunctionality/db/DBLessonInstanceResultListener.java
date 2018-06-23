package pelicann.linnca.com.corefunctionality.db;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;

public abstract class DBLessonInstanceResultListener {
    public abstract void onLessonInstanceAdded();
    public abstract void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances);
}
