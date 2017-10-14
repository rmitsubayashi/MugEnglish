package com.linnca.pelicann.userinterests;

import java.io.Serializable;

public class WikiDataEntryData implements Serializable{
	//everything in Japanese
	private String label;
	private String wikiDataID;
	private String description;
	private String pronunciation;
	private int classification;

	public static final int CLASSIFICATION_PERSON = 0;
	public static final int CLASSIFICATION_PLACE = 1;
	public static final int CLASSIFICATION_OTHER = 2;
	public static final int CLASSIFICATION_NOT_SET = -1;
	
	public WikiDataEntryData(){
	}

	public WikiDataEntryData(String label, String description, String wikiDataID, String pronunciation, int classification){
		this.label = label;
		this.description = description;
		this.wikiDataID = wikiDataID;
		this.pronunciation = pronunciation;
		this.classification = classification;
	}

	public WikiDataEntryData(WikiDataEntryData copy){
		this.label = copy.label;
		this.description = copy.description;
		this.wikiDataID = copy.wikiDataID;
		this.pronunciation = copy.pronunciation;
		this.classification = copy.classification;
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

	public int getClassification() {
		return classification;
	}

	public void setClassification(int classification) {
		this.classification = classification;
	}

	@Override
	public boolean equals(Object object){
		if (object == null)
			return false;

		if (!(object instanceof WikiDataEntryData))
			return false;

		WikiDataEntryData data = (WikiDataEntryData)object;
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
