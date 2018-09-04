package com.wisesharksoftware.panels;

import java.util.List;

public class SlidersPanelInfo extends PanelInfo {
	private String name;
	private String type;
	private String launcher;
	private String imageOnResourceName;
	private String imageOffResourceName;
	private List<Category> categories;
	private List<Item> items;
	private int max = 20;
	private int progress = 10;
	private String caption;
	
	private int seekBar2Max = 20;
	private int seekBar2Progress = 10;
	private String seekBar2Caption;
	
	public SlidersPanelInfo(String name, String type, String caption, String caption2, String launcher, 
			boolean generateThumbnail,String thumbnailSrc, String thumbnailBlendOn , String thumbnailBlendOff, String thumbnailSize,
			String imageOnResourceName,	String imageOffResourceName, List<String> productIds, List<Category> categories, List<Item> items, int max, int progress, int max2, int progress2) {
		super(name, type, launcher, generateThumbnail, thumbnailSrc, thumbnailBlendOn, thumbnailBlendOff, thumbnailSize, imageOnResourceName,
				imageOffResourceName, "", productIds, categories, items);
		this.max = max;
		this.progress = progress;
		this.caption = caption;
		this.seekBar2Max = max2;
		this.seekBar2Progress = progress2;
		this.seekBar2Caption = caption2;
		
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

	public String getSeekBar2Caption() {
		return seekBar2Caption;
	}

	public void setSeekBarCaption(String caption) {
		this.seekBar2Caption = caption;
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

	public int getSeekBar2Max() {
		return seekBar2Max;
	}

	public void setSeekBar2Max(int max) {
		this.seekBar2Max = max;
	}

	public int getSeekBar2Progress() {
		return seekBar2Progress;
	}

	public void setSeekBar2Progress(int progress) {
		this.seekBar2Progress = progress;
	}
}