package com.wisesharksoftware.category_panel;

import android.util.Log;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class ViewItemsStatus {
	private static final String LOG_TAG = "ViewItemsStatus";
	private int count;
	private int countOnScreen;
	private int lastPosX;
	private int screenWidth;
	
	public ViewItemsStatus(CategoryPanel panel){
		screenWidth = panel.getWidth();
		Log.d(LOG_TAG, "screenWidth = " + screenWidth);
		LinearLayout root = panel.getRoot();
		int totalItemsCount = root.getChildCount();
		Log.d(LOG_TAG, "total items count = " + totalItemsCount);
		// cycling in view items
		int totalWidth = 0;
		for (int i = 0; i < totalItemsCount; i++){
			// rigth item pos X
			int pos = root.getChildAt(i).getRight();
			if (pos <= screenWidth){
				lastPosX = pos;
				countOnScreen = i + 1;
			}
		}
		Log.d(LOG_TAG, "count on screen = " + countOnScreen + " lastPosX = " + lastPosX);
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCountOnScreen() {
		return countOnScreen;
	}	
	public int getScreenWidth(){
		return screenWidth;
	}
	
	public int getLastPosX(){
		return lastPosX;
	}
	
}
