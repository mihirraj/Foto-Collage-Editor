package com.wisesharksoftware.panels;

import java.util.List;

public class SliderPanelInfo extends PanelInfo {
	private String name;
	private String type;
	private String launcher;
	private String imageOnResourceName;
	private String imageOffResourceName;
	private String leftImage;
	private String rightImage;
	private List<Category> categories;
	private List<Item> items;
	private int max = 20;
	private int progress = 10;
	private String caption;
	
	public SliderPanelInfo(String name, String type, String caption, String launcher, 
			boolean generateThumbnail, String thumbnailSrc, String thumbnailBlendOn, String thumbnailBlendOff, String thumbnailSize,
			String imageOnResourceName,	String imageOffResourceName, List<String> productIds, List<Category> categories, List<Item> items, int max, int progress) {
		super(name, type, launcher, generateThumbnail, thumbnailSrc, thumbnailBlendOn, thumbnailBlendOff,  thumbnailSize, imageOnResourceName,
				imageOffResourceName, "", productIds, categories, items);
		this.max = max;
		this.progress = progress;
		this.caption = caption;
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

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
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
	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public String getLeftImage() {
		return leftImage;
	}

	public void setLeftImage(String leftImage) {
		this.leftImage = leftImage;
	}

	public String getRightImage() {
		return rightImage;
	}

	public void setRightImage(String rightImage) {
		this.rightImage = rightImage;
	}

}