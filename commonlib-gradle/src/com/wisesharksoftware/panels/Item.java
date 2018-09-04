package com.wisesharksoftware.panels;

import java.util.List;

public class Item {
	private String name;
	private String imageOnResourceName;
	private String imageOffResourceName;
	private String imageLockResourceName;
	private List<String> productIds;
	private String launchPanel = "";
	private String lockedlaunchPanel = "";
	private boolean showAsLocked = false;
	private String backgroundColor;
	private boolean colorable;
	private int unlockedCount;

	public Item(String name_, String imageOnResourceName_,
			String imageOffResourceName_, String imageLockResourceName_,
			boolean showAsLocked_, List<String> productIds, String launchPanel,
			String lockedlaunchPanel) {
		name = name_;
		imageOnResourceName = imageOnResourceName_;
		imageOffResourceName = imageOffResourceName_;
		imageLockResourceName = imageLockResourceName_;
		showAsLocked = showAsLocked_;
		this.productIds = productIds;
		this.launchPanel = launchPanel;
		this.lockedlaunchPanel = lockedlaunchPanel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setColorable(boolean colorable) {
		this.colorable = colorable;
	}

	public boolean getColorable() {
		return colorable;

	}

	public String getImageOnResourceName() {
		return imageOnResourceName;
	}

	public String getImageOffResourceName() {
		return imageOffResourceName;
	}

	public String getImageLockResourceName() {
		return imageLockResourceName;
	}

	public String getLaunchPanel() {
		return launchPanel;
	}

	public void setLaunchPanel(String launchPanel) {
		this.launchPanel = launchPanel;
	}

	public void setImageOnResourceName(String imageResourceName) {
		this.imageOnResourceName = imageResourceName;
	}

	public void setImageOffResourceName(String imageResourceName) {
		this.imageOffResourceName = imageResourceName;
	}

	public boolean isShowAsLocked() {
		return showAsLocked;
	}

	public void setShowAsLocked(boolean showAsLocked) {
		this.showAsLocked = showAsLocked;
	}

	public List<String> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<String> productIds) {
		this.productIds = productIds;
	}

	public String getLockedlaunchPanel() {
		return lockedlaunchPanel;
	}

	public void setLockedlaunchPanel(String lockedlaunchPanel) {
		this.lockedlaunchPanel = lockedlaunchPanel;
	}

	public int getUnlockedCount() {
		return unlockedCount;
	}

	public void setUnlockedCount(int unlockedCount) {
		this.unlockedCount = unlockedCount;
	}
}