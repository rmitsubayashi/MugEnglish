package pelicann.linnca.com.corefunctionality.db;

import java.util.List;

import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public abstract class DBUserInterestListener {
    public abstract void onUserInterestsQueried(List<WikiDataEntity> userInterests);
    public abstract void onUserInterestsAdded();
    public abstract void onUserInterestsRemoved();
}
