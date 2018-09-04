package com.wisesharksoftware.panels;

public class Category{
	private String name;
	//private String imageOnResourceName;
	//private String imageOffResourceName;
	//private List<Item> items;
	public Category(String name/*, String imageOnResourceName, String imageOffResourceName, List<Item> items*/) {
		this.name = name;
		//this.imageOnResourceName = imageOnResourceName;
		//this.imageOffResourceName = imageOffResourceName;
		//this.items = items;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/*
	public String getImageOnResourceName() {
		return imageOnResourceName;
	}
	public String getImageOffResourceName() {
		return imageOffResourceName;
	}
	public void setImageResourceName(String imageOnResourceName, String imageOffResourceName) {
		this.imageOnResourceName = imageOnResourceName;
		this.imageOffResourceName = imageOffResourceName;
	}
	public List<Item> getItems() {
		return items;
	}
	public void setItems(List<Item> items) {
		this.items = items;
	}
	*/
}