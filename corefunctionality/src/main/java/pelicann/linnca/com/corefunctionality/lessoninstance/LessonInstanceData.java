package pelicann.linnca.com.corefunctionality.lessoninstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//used by the script to read data
public class LessonInstanceData implements Serializable{
    private String id;
    private String lessonKey;
    private List<EntityPropertyData> entityPropertyData = new ArrayList<>();
    private long timeStamp;

    public LessonInstanceData(){
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLessonKey() {
        return lessonKey;
    }

    public void setLessonKey(String lessonKey) {
        this.lessonKey = lessonKey;
    }

    public List<EntityPropertyData> getEntityPropertyData() {
        return entityPropertyData;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void addEntityPropertyData(EntityPropertyData data){
        this.entityPropertyData.add(data);
    }

    public boolean isUniqueEntity(EntityPropertyData data){
        String entityID = data.getWikidataID();
        for (EntityPropertyData d : this.entityPropertyData){
            if (d.getWikidataID().equals(entityID)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object object){

        if (object == null)
            return false;

        if (!(object instanceof LessonInstanceData))
            return false;
        LessonInstanceData data = (LessonInstanceData) object;
        return  (data.getId().equals(this.id));
    }

    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + id.hashCode();
        return result;
    }
}
