package pelicann.linnca.com.corefunctionality.lessonscript;

import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.LocalStorageManager;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;
import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lesson.LessonFactory;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.InstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessonlist.LessonCategory;

public class LessonScriptManager {
    private final LocalStorageManager localStorageManager;
    private final Database db;
    private final NetworkConnectionChecker networkConnectionChecker;
    private final LessonScriptManagerListener listener;
    private LessonCategory currentLessonCategory;
    private int currentLessonIndex = -1;

    public interface LessonScriptManagerListener {
        void onLessonScriptLoaded(Script lessonScript, LessonInstanceData lessonInstanceData);
        void onNoConnection();
    }

    public LessonScriptManager(LocalStorageManager localStorageManager, Database db,
                               NetworkConnectionChecker networkConnectionChecker,
                               LessonScriptManagerListener listener){
        this.localStorageManager = localStorageManager;
        this.db = db;
        this.networkConnectionChecker = networkConnectionChecker;
        this.listener = listener;
    }

    //we only need the lesson category.
    //if this is the user's first time on the category,
    // load the first lesson of the category.
    // if not, get the last saved lesson
    public void loadLessonScript(LessonCategory lessonCategory){
        this.currentLessonCategory = lessonCategory;
        int lastSavedLesson = localStorageManager.getLastSavedLessonIndexAtCategory(lessonCategory.getKey());
        //if this is the user's first time, show the first lesson
        if (lastSavedLesson == -1){
            lastSavedLesson = 0;
            localStorageManager.saveLessonIndexAtCategory(lessonCategory.getKey(), 0);
        }

        currentLessonIndex = lastSavedLesson;

        final String lessonKey = lessonCategory.getLessonKey(lastSavedLesson);
        //if this doesn't exist, we create a lesson.
        //this is called inside this method
        getLessonInstanceFromDB(lessonKey);
    }

    private void getLessonInstanceFromDB(final String lessonKey){
        //try to find a lesson already created by the user
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstanceData){
                if (lessonInstanceData.size() == 0){
                    //none created by the user,
                    // so we need to create an instance for the user
                    createNewLessonInstance();
                } else {
                    LessonInstanceData instance = lessonInstanceData.get(0);
                    List<EntityPropertyData> data = instance.getEntityPropertyData();
                    Script script = createScript(data);
                    listener.onLessonScriptLoaded(script, instance);
                }
            }

            @Override
            public void onNoConnection() {
                listener.onNoConnection();
            }
        };

        db.getMostRecentLessonInstance(networkConnectionChecker, lessonKey, onDBResultListener);

    }

    private void createNewLessonInstance(){
        Lesson lesson = getCurrentLesson();
        InstanceGenerator.LessonInstanceGeneratorListener lessonInstanceGeneratorListener =
                new InstanceGenerator.LessonInstanceGeneratorListener() {
                    @Override
                    public void onLessonCreated(LessonInstanceData lessonInstanceData) {
                        Script script = createScript(lessonInstanceData.getEntityPropertyData());
                        listener.onLessonScriptLoaded(script, lessonInstanceData);
                    }

                    @Override
                    public void onNoConnection() {

                    }
                };
        lesson.createLessonInstance(
                new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                db, lessonInstanceGeneratorListener, networkConnectionChecker);
    }

    private Script createScript(List<EntityPropertyData> data){
        Lesson lesson = getCurrentLesson();
        return lesson.createScript(data);
    }

    public boolean hasNextLesson(){
        if (currentLessonIndex == -1){
            return false;
        }
        return currentLessonIndex < currentLessonCategory.getLessonCount();
    }

    public void toNextLesson(){
        if (currentLessonIndex == -1){
            return;
        }
        int nextLessonIndex = currentLessonIndex + 1;
        String nextLessonKey = currentLessonCategory.getLessonKey(nextLessonIndex);
        if (nextLessonKey != null){
            localStorageManager.saveLessonIndexAtCategory(currentLessonCategory.getKey(), nextLessonIndex);
            currentLessonIndex = nextLessonIndex;
            //if this doesn't exist, we create a lesson.
            //this is called inside this method
            getLessonInstanceFromDB(nextLessonKey);
        }
    }

    public boolean hasPreviousLesson(){
        return currentLessonIndex > 0;
    }

    public void toPreviousLesson(){
        if (currentLessonIndex == -1){
            return;
        }
        int previousLessonIndex = currentLessonIndex - 1;
        String previousLessonKey = currentLessonCategory.getLessonKey(previousLessonIndex);
        if (previousLessonKey != null){
            localStorageManager.saveLessonIndexAtCategory(currentLessonCategory.getKey(), previousLessonIndex);
            currentLessonIndex = previousLessonIndex;
            //if this doesn't exist, we create a lesson.
            //this is called inside this method
            getLessonInstanceFromDB(previousLessonKey);
        }
    }

    public int getLessonNumber(){
        //we want the number, not the index
        return currentLessonIndex + 1;
    }

    public int getTotalLessonCt(){
        return currentLessonCategory.getLessonCount();
    }

    public Lesson getCurrentLesson(){
        if (currentLessonCategory == null || currentLessonIndex == -1){
            return null;
        }
        return LessonFactory.getLesson(currentLessonCategory.getLessonKey(currentLessonIndex));
    }

}
