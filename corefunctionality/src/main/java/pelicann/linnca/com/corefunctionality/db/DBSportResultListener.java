package pelicann.linnca.com.corefunctionality.db;

public abstract class DBSportResultListener {
    public abstract void onSportQueried(String wikiDataID, String verb, String object);
    public abstract void onSportsQueried();
}
