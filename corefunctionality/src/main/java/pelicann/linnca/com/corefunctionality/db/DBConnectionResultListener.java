package pelicann.linnca.com.corefunctionality.db;

public abstract class DBConnectionResultListener {
    public abstract void onNoConnection();
    public abstract void onSlowConnection();
}
