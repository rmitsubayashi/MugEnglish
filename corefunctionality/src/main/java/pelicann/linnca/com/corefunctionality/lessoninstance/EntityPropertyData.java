package pelicann.linnca.com.corefunctionality.lessoninstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityPropertyData {
    private String key;
    private String lessonKey;
    //need to sync with db
    private String wikidataID;
    //all the properties needed for a lesson.
    //order matters.
    private List<Translation> properties;
    private List<String> vocabularyIDs;

    public EntityPropertyData() {
    }

    public EntityPropertyData(String key, String lessonKey, String wikidataID,
                              List<Translation> properties, List<String> vocabularyIDs) {
        this.key = key;
        this.lessonKey = lessonKey;
        this.wikidataID = wikidataID;
        this.properties = properties;
        this.vocabularyIDs = vocabularyIDs;
    }

    public EntityPropertyData(EntityPropertyData copy){
        this.key = copy.key;
        this.lessonKey = copy.lessonKey;
        this.wikidataID = copy.wikidataID;
        this.properties = new ArrayList<>(copy.properties);
        this.vocabularyIDs = new ArrayList<>(copy.vocabularyIDs);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLessonKey() {
        return lessonKey;
    }

    public void setLessonKey(String lessonKey) {
        this.lessonKey = lessonKey;
    }

    public List<Translation> getProperties() {
        return properties;
    }

    public void setProperties(List<Translation> properties) {
        this.properties = properties;
    }

    public Translation getPropertyAt(int index){
        if (index >= properties.size()){
            return null;
        }
        return properties.get(index);
    }

    public List<String> getVocabularyIDs() {
        return vocabularyIDs;
    }

    public void setVocabularyIDs(List<String> vocabularyIDs) {
        this.vocabularyIDs = vocabularyIDs;
    }

    public String getWikidataID() {
        return wikidataID;
    }

    public void setWikidataID(String wikidataID) {
        this.wikidataID = wikidataID;
    }

    public boolean isUnique(Collection<EntityPropertyData> list){
        for (EntityPropertyData toCompare : list){
            if (toCompare.getWikidataID().equals(this.wikidataID)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object object){

        if (object == null)
            return false;

        if (!(object instanceof EntityPropertyData))
            return false;
        EntityPropertyData data = (EntityPropertyData) object;
        //we only check the ID because the label and description might change
        //if a user adds the entity data after it has been modified
        return  (data.getKey().equals(this.key));
    }

    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + key.hashCode();
        return result;
    }
}
