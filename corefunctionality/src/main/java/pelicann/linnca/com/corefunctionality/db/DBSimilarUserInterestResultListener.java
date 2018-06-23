package pelicann.linnca.com.corefunctionality.db;

import java.util.List;

import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public abstract class DBSimilarUserInterestResultListener {
    public abstract void onSimilarUserInterestsQueried(List<WikiDataEntity> userInterests);

}
