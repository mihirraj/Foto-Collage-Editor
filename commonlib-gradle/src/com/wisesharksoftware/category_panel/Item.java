package com.wisesharksoftware.category_panel;

public class Item{
	private String name;
	private String imageOnResourceName;
	private String imageOffResourceName;
	public Item(String name_, String imageOnResourceName_, String imageOffResourceName_) {
		name = name_;
		imageOnResourceName = imageOnResourceName_;
		imageOffResourceName = imageOffResourceName_;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImageOnResourceName() {
		return imageOnResourceName;
	}
	public String getImageOffResourceName() {
		return imageOffResourceName;
	}
	
	
	public void setImageOnResourceName(String imageResourceName) {
		this.imageOnResourceName = imageResourceName;
	}
	public void setImageOffResourceName(String imageResourceName) {
		this.imageOffResourceName = imageResourceName;
	}
}