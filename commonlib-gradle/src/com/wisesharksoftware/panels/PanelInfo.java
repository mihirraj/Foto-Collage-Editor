package com.wisesharksoftware.panels;

import java.util.List;

public class PanelInfo {
	private String name;
	private String type;
	private String launcher;
	private Boolean generateThumbnail = false;
	private String thumbnailSrc;
	private String thumbnailBlendOn;
	private String thumbnailBlendOff;
	private String thumbnailSize;
	private String imageOnResourceName;
	private String imageOffResourceName;
	private String imageLockResourceName;
	private List<String> productIds;
	private int targetItem;
	private int lockedCount = 5;
	private List<Category> categories;
	private List<Item> items;
	private String withFragment;
	private String action, actionGroup;
	private int priority;

	public PanelInfo(String name, String type, String launcher,
			boolean generateThumbnail, String thumbnailSrc,
			String thumbnailBlendOn, String thumbnailBlendOff,
			String thumbnailSize, String imageOnResourceName,
			String imageOffResourceName, String imageLockResourceName,
			List<String> productIds, List<Category> categories, List<Item> items) {
		this.setName(name);
		this.setType(type);
		this.setLauncher(launcher);
		this.generateThumbnail = generateThumbnail;
		this.thumbnailSrc = thumbnailSrc;
		this.thumbnailBlendOn = thumbnailBlendOn;
		this.thumbnailBlendOff = thumbnailBlendOff;
		this.thumbnailSize = thumbnailSize;
		this.setImageOnResourceName(imageOnResourceName);
		this.setImageOffResourceName(imageOffResourceName);
		this.setImageLockResourceName(imageLockResourceName);
		this.productIds = productIds;		
		this.setCategories(categories);
		this.setItems(items);
	}
	
	public void setTargetItem(int targetItem) {
		this.targetItem = targetItem;
	}

	public int getTargetItem() {
		return targetItem;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	};

	public void setAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return action;
	}

	public void setActionGroup(String actionGroup) {
		this.actionGroup = actionGroup;
	}

	public String getActionGroup() {
		return actionGroup;
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

	public String getImageLockResourceName() {
		return imageLockResourceName;
	}

	public void setImageLockResourceName(String imageLockResourceName) {
		this.imageLockResourceName = imageLockResourceName;
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

	public void setWithFragment(String withFragment) {
		this.withFragment = withFragment;
	}

	public String getWithFragment() {
		return withFragment;
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

	public List<String> getProductIds() {
		return productIds;
	}

	public int getLockedCount() {
		return lockedCount;
	}

	public void setProductIds(List<String> productIds) {
		this.productIds = productIds;
	}

	public Boolean getGenerateThumbnail() {
		return generateThumbnail;
	}

	public void setGenerateThumbnail(Boolean generateThumbnail) {
		this.generateThumbnail = generateThumbnail;
	}

	public String getThumbnailSrc() {
		return thumbnailSrc;
	}

	public void setThumbnailSrc(String thumbnailSrc) {
		this.thumbnailSrc = thumbnailSrc;
	}

	public String getThumbnailBlendOn() {
		return thumbnailBlendOn;
	}

	public void setThumbnailBlendOn(String thumbnailBlendOn) {
		this.thumbnailBlendOn = thumbnailBlendOn;
	}

	public String getThumbnailBlendOff() {
		return thumbnailBlendOff;
	}

	public void setThumbnailBlendOff(String thumbnailBlendOff) {
		this.thumbnailBlendOff = thumbnailBlendOff;
	}

	public String getThumbnailSize() {
		return thumbnailSize;
	}

	public void setThumbnailSize(String thumbnailSize) {
		this.thumbnailSize = thumbnailSize;
	}

}