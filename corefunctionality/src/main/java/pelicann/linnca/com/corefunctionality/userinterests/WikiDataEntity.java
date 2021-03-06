package pelicann.linnca.com.corefunctionality.userinterests;

import java.io.Serializable;

public class WikiDataEntity implements Serializable{
	//everything in Japanese
	private String label;
	private String wikiDataID;
	private String description;
	private String pronunciation;
	
	public WikiDataEntity(){
	}

	public WikiDataEntity(String label, String description, String wikiDataID, String pronunciation){
		this.label = label;
		this.description = description;
		this.wikiDataID = wikiDataID;
		this.pronunciation = pronunciation;
	}

	public WikiDataEntity(WikiDataEntity copy){
		this.label = copy.label;
		this.description = copy.description;
		this.wikiDataID = copy.wikiDataID;
		this.pronunciation = copy.pronunciation;
	}

	public void setLabel(String label){
		this.label = label;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public void setWikiDataID(String wikiDataID){
		this.wikiDataID = wikiDataID;
	}
	
	public String getLabel(){
		return this.label;
	}
	
	public String getWikiDataID(){
		return this.wikiDataID;
	}

	public String getDescription() {return this.description; }

	public String getPronunciation() {
		return pronunciation;
	}

	public void setPronunciation(String pronunciation) {
		this.pronunciation = pronunciation;
	}

	//when we retrieve data from WikiData and want to get the ID
	public static String getWikiDataIDFromReturnedResult(String str){
		int lastIndexID = str.lastIndexOf('/');
		return str.substring(lastIndexID+1);
	}

	@Override
	public boolean equals(Object object){

		if (object == null)
			return false;

		if (!(object instanceof WikiDataEntity))
			return false;
		WikiDataEntity data = (WikiDataEntity)object;
		//we only check the ID because the label and description might change
		//if a user adds the entity data after it has been modified
		return  (data.getWikiDataID().equals(this.wikiDataID));
	}

	@Override
	public int hashCode(){
		int result = 17;
		result = 31 * result + wikiDataID.hashCode();
		return result;
	}
}
