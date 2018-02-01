package pelicann.linnca.com.corefunctionality.db;

//used for temporary storage that isn't relevant for app functionality
// i.e. preferences, saved screens, etc.
public abstract class LocalStorageManager {
    public abstract int getLastSavedLessonIndexAtCategory(String category);
    public abstract void saveLessonIndexAtCategory(String category, int index);
}
