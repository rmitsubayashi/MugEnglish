package com.example.ryomi.myenglish.db.datawrappers;

public class WikiDataEntryData{
	private String label;
	private String wikiDataID;
	private String description;
	private String pronunciation;
	
	public WikiDataEntryData(){
	}

	public WikiDataEntryData(String label, String description, String wikiDataID, String pronunciation){
		this.label = label;
		this.description = description;
		this.wikiDataID = wikiDataID;
		this.pronunciation = pronunciation;
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

	//we technically only need to check the wikiData id
	//but we might implement ore languages in the future
	@Override
	public boolean equals(Object object){
		if (object == null)
			return false;

		if (!(object instanceof WikiDataEntryData))
			return false;

		WikiDataEntryData data = (WikiDataEntryData)object;
		return  (data.getWikiDataID().equals(this.wikiDataID) &&
				data.getLabel().equals(this.label) &&
				data.getDescription().equals(this.description)
		);
	}

	@Override
	public int hashCode(){
		int result = 17;
		result = 31 * result + label.hashCode();
		result = 31 * result + wikiDataID.hashCode();
		result = 31 * result + description.hashCode();
		return result;
	}
}
