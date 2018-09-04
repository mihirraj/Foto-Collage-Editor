package com.wisesharksoftware.category_panel;

import java.util.List;

public class Structure {
	private List<Category> categories;
	public Structure(List<Category> categories) {
		this.categories = categories;
	}
	
	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	/*public String toString(){
		String result = null;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < categories.size(); i++){
			Category cat = categories.get(i);
			String catName = cat.getName();
			//String catResource = cat.getImageResourceName();
			sb.append("cat name = " + catName + " cat img res = " + catResource);
			List<Item> items = cat.getItems();
			for (int j = 0; j < items.size(); j++){
				Item item = items.get(j);
				String itemName = item.getName();
				String itemResource = item.getImageResourceName();
				sb.append(" item name = " + itemName + " item res = " + itemResource);
			}
			sb.append(" || ");
		}
		result = sb.toString();
		return result;
	}*/
}
