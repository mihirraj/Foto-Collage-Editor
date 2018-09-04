package com.wisesharksoftware.category_panel;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StructureParser {
	private static final String CATEGORIES = "categories";
	private static final String CATEGORY_NAME = "name";
	private static final String CATEGORY_IMG_ON = "imageOnResourceName";
	private static final String CATEGORY_IMG_OFF = "imageOffResourceName";
	private static final String ITEMS = "items";
	private static final String ITEM_NAME = "name";
	private static final String ITEM_IMG_ON = "imageOnResourceName";
	private static final String ITEM_IMG_OFF = "imageOffResourceName";

	public Structure parse(String jsonData) throws JSONException {
		JSONObject structureJSON = new JSONObject(jsonData);
		JSONArray categoriesJSON = structureJSON.getJSONArray(CATEGORIES);
		List<Category> categories = new ArrayList<Category>();
		for (int i = 0; i < categoriesJSON.length(); ++i) {
			// parse category
			JSONObject categoryJSON = categoriesJSON.getJSONObject(i);
			String catName = categoryJSON.getString(CATEGORY_NAME);
			String catImageOnResourceName = categoryJSON.getString(CATEGORY_IMG_ON);
			String catImageOffResourceName = categoryJSON.getString(CATEGORY_IMG_OFF);
			// parse items in category
			JSONArray itemsJSON = categoryJSON.getJSONArray(ITEMS);
			List<Item> items = new ArrayList<Item>();
			for (int j = 0; j < itemsJSON.length(); ++j) {
				JSONObject itemJSON = itemsJSON.getJSONObject(j);
				String itemName = itemJSON.getString(ITEM_NAME);
				String itemImageOnResourceName = itemJSON.getString(ITEM_IMG_ON);
				String itemImageOffResourceName = itemJSON.getString(ITEM_IMG_OFF);
				Item item = new Item(itemName, itemImageOnResourceName, itemImageOffResourceName);
				items.add(item);
			}
			// add to categories list
			Category category = new Category(catName, catImageOnResourceName,catImageOffResourceName, items);
			categories.add(category);
		}
		// add to structure
		Structure structure = new Structure(categories);
		return structure;
	}
}
