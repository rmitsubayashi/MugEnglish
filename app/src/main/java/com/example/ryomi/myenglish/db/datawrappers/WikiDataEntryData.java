package com.example.ryomi.myenglish.db.datawrappers;

public class WikiDataEntryData {
	private String label;
	private String wikiDataID;
	private String description;
	
	public WikiDataEntryData(){
	}

	public WikiDataEntryData(String label, String description, String wikiDataID){
		this.label = label;
		this.description = description;
		this.wikiDataID = wikiDataID;
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
}
