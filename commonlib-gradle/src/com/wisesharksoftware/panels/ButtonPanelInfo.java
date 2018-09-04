package com.wisesharksoftware.panels;

import java.util.List;

public class ButtonPanelInfo extends PanelInfo {
	private String name;
	private String type;
	private String launcher;
	private String imageOnResourceName;
	private String imageOffResourceName;
	private List<Category> categories;
	private List<Item> items;
	private int margin = 15;
	
	public ButtonPanelInfo(String name, String type, String launcher, 
			boolean generateThumbnail, String thumbnailSrc, String thumbnailBlendOn, String thumbnailBlendOff, String thumbnailSize,
			String imageOnResourceName,	String imageOffResourceName, List<Category> categories, List<Item> items, int margin) {
		super(name, type, launcher, 
				generateThumbnail, thumbnailSrc, thumbnailBlendOn, thumbnailBlendOff, thumbnailSize,  
				imageOnResourceName, imageOffResourceName, "", null, categories, items);
		this.margin = margin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getImageOnResourceName() {
		return imageOnResourceName;
	}

	public void setImageOnResourceName(String imageOnResourceName) {
		this.imageOnResourceName = imageOnResourceName;
	}

	public String getImageOffResourceName() {
		return imageOffResourceName;
	}

	public void setImageOffResourceName(String imageOffResourceName) {
		this.imageOffResourceName = imageOffResourceName;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public String getLauncher() {
		return launcher;
	}

	public void setLauncher(String launcher) {
		this.launcher = launcher;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	public int getMargin() {
		return margin;
	}

	public void setMargin(int margin) {
		this.margin = margin;
	}

}