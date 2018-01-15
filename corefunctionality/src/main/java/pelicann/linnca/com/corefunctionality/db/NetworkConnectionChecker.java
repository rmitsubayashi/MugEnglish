package pelicann.linnca.com.corefunctionality.db;

import java.util.concurrent.atomic.AtomicBoolean;

public interface NetworkConnectionChecker {
    //OnDBResultListener : what we do when we don't have a connection
    //AtomicBoolean : if we get a response from the database before calling this,
    // we set the value to true so the networkConnectionChecker doesn't have to check
    void checkConnection(OnDBResultListener noConnectionListener, AtomicBoolean called);
    void stop();
}
