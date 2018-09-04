package com.wisesharksoftware.panels.okcancel;

import java.util.List;

import com.wisesharksoftware.panels.Category;
import com.wisesharksoftware.panels.Item;
import com.wisesharksoftware.panels.PanelInfo;

public class OKCancelPanelInfo extends PanelInfo {
	protected String name;
	protected String type;
	protected String launcher;
	protected String imageOnResourceName;
	protected String imageOffResourceName;

	protected List<Category> categories;
	protected List<Item> items;
	protected String res = "";
	protected boolean locked = false;

	public OKCancelPanelInfo(String name, String type, String launcher, 
			boolean generateThumbnail, String thumbnailSrc, String thumbnailBlendOn, String thumbnailBlendOff, String thumbnailSize,			
			String imageOnResourceName, String imageOffResourceName, String imageLockResourceName, List<String> panelProductIds,
			boolean locked, List<Category> categories, List<Item> items,
			String res) {
		super(name, type, launcher, 
			generateThumbnail, thumbnailSrc, thumbnailBlendOn, thumbnailBlendOff, thumbnailSize, 
			imageOnResourceName, imageOffResourceName, imageLockResourceName,
			panelProductIds, categories, items);
		this.res = res;
		this.locked = locked;
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

	public String getResName() {
		return res;
	}

	public void setResName(String res) {
		this.res = res;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}