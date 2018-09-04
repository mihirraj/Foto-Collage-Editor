package com.wisesharksoftware.panels;

import java.util.ArrayList;
import java.util.List;

public class Structure {
	private List<PanelInfo> panels;
	private List<String> productIds = new ArrayList<String>();

	public Structure(List<PanelInfo> panels) {
		this.panels = panels;
	}

	public List<PanelInfo> getPanelsInfo() {
		return panels;
	}

	public void setPanelsInfo(List<PanelInfo> panels) {
		this.panels = panels;
	}

	public List<String> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<String> productIds) {
		this.productIds = productIds;
	}
	
	public void addProductIds(List<String> addProductIds) {
		for (int i = 0; i < addProductIds.size(); i++) {
			if (productIds.indexOf(addProductIds.get(i)) == -1) {
				productIds.add(addProductIds.get(i));
			}
		}
	}
	
	
}
