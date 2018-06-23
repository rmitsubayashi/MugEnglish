package pelicann.linnca.com.corefunctionality.db;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public abstract class DBEntityPropertyDataResultListener {
    public abstract void onEntityPropertyDataSearched(List<EntityPropertyData> found, List<WikiDataEntity> searched);
    //for each
    public abstract void onEntityPropertyDataAdded(EntityPropertyData added);
    //all
    public abstract void onAllEntityPropertyDataAdded();
    public abstract void onRandomEntityPropertyDataQueried(List<EntityPropertyData> result);
}
