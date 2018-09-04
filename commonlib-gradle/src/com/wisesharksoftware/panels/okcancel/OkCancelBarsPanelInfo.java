package com.wisesharksoftware.panels.okcancel;

import java.util.List;

import android.util.Log;

import com.wisesharksoftware.panels.Category;
import com.wisesharksoftware.panels.Item;

public class OkCancelBarsPanelInfo extends OKCancelPanelInfo {
	private int barCount;
	private int[] barProgress;
	private int[] barMax;
	private int[] barTypes;
	private String[] barCaptions;
	private String[] columnCaptions;
	
	public OkCancelBarsPanelInfo(String name, String type, String launcher, 
			boolean generateThumbnail, String thumbnailSrc, String thumbnailBlendOn, String thumbnailBlendOff, String thumbnailSize, 
			String imageOnResourceName, String imageOffResourceName, String imageLockResourceName, List<String> panelProductIds,
			boolean locked, List<Category> categories, List<Item> items,
			String res, int barCount, int[] barProgress, int[] barMax,
			int[] types, String[] captions, String[] columnCaptions) {
		super(name, type, launcher, 
				generateThumbnail, thumbnailSrc, thumbnailBlendOn, thumbnailBlendOff, thumbnailSize, 
				imageOnResourceName, imageOffResourceName, imageLockResourceName, panelProductIds,
				locked, categories, items, res);
		setBarCount(barCount);
		setBarMax(barMax);
		setBarProgress(barProgress);
		setBarTypes(types);
		setBarCaptions(captions);
		setColumnCaptions(columnCaptions);
	}

	public void setBarCount(int barCount) {
		this.barCount = barCount;
	}

	public void setBarMax(int[] barMax) {
		this.barMax = barMax;
	}

	public void setBarProgress(int[] barProgress) {
		this.barProgress = barProgress;
	}

	public int getBarCount() {
		return barCount;
	}

	public int[] getBarMax() {
		return barMax;
	}

	public int[] getBarProgress() {
		return barProgress;
	}

	public void setBarTypes(int[] barTypes) {
		this.barTypes = barTypes;
	}

	public int[] getBarTypes() {
		return barTypes;
	}
	
	public void setBarCaptions(String[] barCaptions) {
		this.barCaptions = barCaptions;
	}

	public String[] getBarCaptions() {	
		return barCaptions;
	}

	public String[] getColumnCaptions() {
		return columnCaptions;
	}

	public void setColumnCaptions(String[] columnCaptions) {
		this.columnCaptions = columnCaptions;
	}
}
