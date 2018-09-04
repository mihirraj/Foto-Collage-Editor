package com.wisesharksoftware.app_photoeditor;

import java.util.ArrayList;
import com.wisesharksoftware.core.Filter;

public class ItemOrder {
	public ArrayList<Filter> filters = new ArrayList<Filter>();
	public String type;
	public int angle = 0;
	public boolean flipVertical = false;
	public boolean flipHorizontal = false;

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
