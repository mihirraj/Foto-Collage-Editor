package com.wisesharksoftware.panels;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wisesharksoftware.panels.okcancel.OKCancelPanelInfo;
import com.wisesharksoftware.panels.okcancel.OkCancelBarsPanelInfo;

public class StructureParser {
	private static final String PANELS = "panels";
	private static final String PANEL_NAME = "name";
	private static final String PANEL_TYPE = "type";
	private static final String PANEL_LAUNCHER = "launcher";
	private static final String PANEL_GENERATE_THUMBNAIL = "generateThumbnail";
	private static final String PANEL_THUMBNAIL_SRC = "thumbnailSrc";
	private static final String PANEL_THUMBNAIL_BLEND_ON = "thumbnailBlendImageOn";
	private static final String PANEL_THUMBNAIL_BLEND_OFF = "thumbnailBlendImageOff";
	private static final String PANEL_THUMBNAIL_SIZE = "thumbnailSize";
	private static final String PANEL_SLIDER_MAX = "max";
	private static final String PANEL_ACTION = "action";
	private static final String PANEL_ACTION_GROUP = "action_group";
	private static final String PANEL_SLIDER_PROGRESS = "progress";
	private static final String PANEL_SLIDER_MAX2 = "max2";
	private static final String PANEL_SLIDER_PROGRESS2 = "progress2";
	private static final String PANEL_SLIDER_CAPTION = "caption";
	private static final String PANEL_SLIDER_CAPTION2 = "caption2";
	private static final String PANEL_SLIDER_LEFT_IMAGE = "left_image";
	private static final String PANEL_SLIDER_RIGHT_IMAGE = "right_image";
	private static final String SLIDER_TYPE = "barTypes";
	private static final String SLIDER_CAPTION = "captions";
	private static final String SLIDER_COLUMN_CAPTION = "columnCaptions";
	private static final String PANEL_BUTTON_MARGIN = "margin";
	private static final String PANEL_OK_CANCEL_BTN_OK_LOCK = "locked";
	private static final String PANEL_RES_NAME = "res_name";
	private static final String PANEL_PRIORITY = "priority";
	private static final String LAUNCH_PANEL = "launch_panel";
	private static final String LOCKED_LAUNCH_PANEL = "locked_launch_panel";
	private static final String CATEGORIES = "categories";
	private static final String CATEGORY_NAME = "name";
	private static final String PANEL_TARGET_ITEM = "target_item";
	private static final String PANEL_IMG_ON = "imageOnResourceName";
	private static final String PANEL_IMG_OFF = "imageOffResourceName";
	private static final String PANEL_IMG_LOCK = "imageLockResourceName";
	private static final String PRODUCT_IDS = "productIds";
	private static final String ITEMS = "items";
	private static final String ITEM_NAME = "name";

	private static final String ITEM_IMG_ON = "imageOnResourceName";
	private static final String ITEM_IMG_OFF = "imageOffResourceName";
	private static final String ITEM_IMG_LOCK = "imageLockResourceName";
	private static final String ITEM_IMG_SHOW_AS_LOCKED = "showAsLocked";
	private static final String ITEM_BACKGROUND_COLOR = "background_color";
	private static final String ITEM_UNLOCKED_COUNT = "unlocked_count";
	private static final String ITEM_COLORABLE = "colorable";
	private static final String PANEL_WITH_FRAGMENT = "with_screen";
	private static final String BAR_COUNT = "bars";
	private List<String> productIds;

	public Structure parse(String jsonData) throws JSONException {
		productIds = new ArrayList<String>();
		JSONObject structureJSON = new JSONObject(jsonData);

		JSONArray panelsJSON = structureJSON.getJSONArray(PANELS);
		List<PanelInfo> panels = new ArrayList<PanelInfo>();

		for (int i = 0; i < panelsJSON.length(); i++) {
			// parse panel
			JSONObject panelJSON = panelsJSON.getJSONObject(i);
			String panelName = panelJSON.getString(PANEL_NAME);
			String panelType = panelJSON.getString(PANEL_TYPE);
			String launcher = panelJSON.getString(PANEL_LAUNCHER);
			String action = "";
			String actionGroup = "";
			try {
				action = panelJSON.getString(PANEL_ACTION);
				actionGroup = panelJSON.getString(PANEL_ACTION_GROUP);
			} catch (Exception e) {

			}
			int priority = panelJSON.optInt(PANEL_PRIORITY);
			int panelTargetItem = panelJSON.optInt(PANEL_TARGET_ITEM);
			boolean generateThumbnail = panelJSON.optBoolean(
					PANEL_GENERATE_THUMBNAIL, false);
			String thumbnailSrc = panelJSON.optString(PANEL_THUMBNAIL_SRC, "");
			String thumbnailBlendOn = panelJSON.optString(
					PANEL_THUMBNAIL_BLEND_ON, "");
			String thumbnailBlendOff = panelJSON.optString(
					PANEL_THUMBNAIL_BLEND_OFF, "");
			String thumbnailSize = panelJSON
					.optString(PANEL_THUMBNAIL_SIZE, "");

			String withFragment = null;

			try {
				withFragment = panelJSON.getString(PANEL_WITH_FRAGMENT);

			} catch (Exception e) {

			}

			boolean OKCancelPanelLocked = false;
			if (panelType.equals(PanelManager.OK_CANCEL_PANEL_TYPE)) {
				String locked = panelJSON
						.optString(PANEL_OK_CANCEL_BTN_OK_LOCK);
				OKCancelPanelLocked = Boolean.parseBoolean(locked);
			}

			int sliderPanelMax = 20;
			int sliderPanelProgress = 10;
			int sliderSeekBar2PanelMax = 20;
			int sliderSeekBar2PanelProgress = 10;

			if (panelType.equals(PanelManager.SLIDER_PANEL_TYPE)
					|| panelType.equals(PanelManager.SLIDERS_PANEL_TYPE)) {
				String slider_max = panelJSON.optString(PANEL_SLIDER_MAX);
				try {
					sliderPanelMax = Integer.parseInt(slider_max);
				} catch (Exception e) {
					// nothing to do
				}
				String slider_progress = panelJSON
						.optString(PANEL_SLIDER_PROGRESS);
				try {
					sliderPanelProgress = Integer.parseInt(slider_progress);
				} catch (Exception e) {
					// nothing to do
				}
				String slider_max2 = panelJSON.optString(PANEL_SLIDER_MAX2);
				try {
					sliderSeekBar2PanelMax = Integer.parseInt(slider_max2);
				} catch (Exception e) {
					// nothing to do
				}
				String slider_progress1 = panelJSON
						.optString(PANEL_SLIDER_PROGRESS2);
				try {
					sliderSeekBar2PanelProgress = Integer
							.parseInt(slider_progress1);
				} catch (Exception e) {
					// nothing to do
				}

			}

			int barsCount = 0;
			int[] barsProgress = null;
			int[] barsMax = null;
			int[] barTypes = null;
			String[] barCaptions = null;
			String[] columnCaptions = null;
			if (panelType.equals(PanelManager.OK_CANCEL_BAR_PANEL_TYPE)
					|| panelType.equals(PanelManager.SLIDERS_BAR_PANEL_TYPE)) {
				try {
					barsCount = panelJSON.getInt(BAR_COUNT);

					if (barsCount > 0) {
						barsProgress = new int[barsCount];
						barsMax = new int[barsCount];
						barTypes = new int[barsCount];
						barCaptions = new String[barsCount];
						JSONArray arrayProgress = panelJSON
								.getJSONArray(PANEL_SLIDER_PROGRESS);
						if (arrayProgress != null
								&& arrayProgress.length() == barsCount) {
							for (int j = 0; j < barsCount; j++) {

								barsProgress[j] = arrayProgress.getInt(j);
							}
						}

						JSONArray arrayMax = panelJSON
								.getJSONArray(PANEL_SLIDER_MAX);
						if (arrayMax != null && arrayMax.length() == barsCount) {
							for (int j = 0; j < barsCount; j++) {

								barsMax[j] = arrayMax.getInt(j);
							}
						}

						JSONArray arrayType = panelJSON
								.getJSONArray(SLIDER_TYPE);
						if (arrayType != null
								&& arrayType.length() == barsCount) {
							for (int j = 0; j < barsCount; j++) {

								barTypes[j] = arrayType.getInt(j);
							}
						}

						if (!panelJSON.isNull(SLIDER_CAPTION)) {
							JSONArray arrayCaptions = panelJSON
									.getJSONArray(SLIDER_CAPTION);

							if (arrayCaptions.length() == barsCount) {

								for (int j = 0; j < barsCount; j++) {
									barCaptions[j] = arrayCaptions.getString(j);
								}
							}
						}

						if (!panelJSON.isNull(SLIDER_COLUMN_CAPTION)) {
							JSONArray arrayCaptions = panelJSON
									.getJSONArray(SLIDER_COLUMN_CAPTION);
							columnCaptions = new String[arrayCaptions.length()];
							for (int j = 0; j < arrayCaptions.length(); j++) {
								columnCaptions[j] = arrayCaptions.getString(j);
							}

						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			String sliderCaption = panelJSON.optString(PANEL_SLIDER_CAPTION);
			String sliderCaption2 = panelJSON.optString(PANEL_SLIDER_CAPTION2);
			String sliderLeftImage = panelJSON.optString(
					PANEL_SLIDER_LEFT_IMAGE, "");
			String sliderRightImage = panelJSON.optString(
					PANEL_SLIDER_RIGHT_IMAGE, "");

			int buttonPanelMargin = 15;
			if (panelType.equals(PanelManager.BUTTON_PANEL_TYPE)) {
				String button_margin = panelJSON.optString(PANEL_BUTTON_MARGIN);
				try {
					buttonPanelMargin = Integer.parseInt(button_margin);
				} catch (NumberFormatException e) {
					// nothing to do
				}
			}

			String ResourceName = panelJSON.optString(PANEL_RES_NAME, "");

			String panelImageOnResourceName = panelJSON.getString(PANEL_IMG_ON);
			String panelImageOffResourceName = panelJSON
					.getString(PANEL_IMG_OFF);
			String panelImageLockResourceName = panelJSON.optString(
					PANEL_IMG_LOCK, "");

			// parse productIds in panel
			List<String> panelProductIds = new ArrayList<String>();
			if (!panelJSON.isNull(PRODUCT_IDS)) {
				JSONArray productIdsJSON = panelJSON.getJSONArray(PRODUCT_IDS);

				for (int j = 0; j < productIdsJSON.length(); j++) {
					String id = productIdsJSON.get(j).toString();
					panelProductIds.add(id);
				}
			}

			// parse categories in panel
			List<Category> categories = new ArrayList<Category>();
			if (!panelJSON.isNull(CATEGORIES)) {
				JSONArray categoriesJSON = panelJSON.getJSONArray(CATEGORIES);

				for (int j = 0; j < categoriesJSON.length(); j++) {
					// parse category
					String catName = categoriesJSON.get(j).toString();
					Category category = new Category(catName);
					categories.add(category);
				}
			}
			List<Item> items = new ArrayList<Item>();

			if (!panelJSON.isNull(ITEMS)) {
				JSONArray itemsJSON = panelJSON.getJSONArray(ITEMS);

				for (int k = 0; k < itemsJSON.length(); k++) {
					JSONObject itemJSON = itemsJSON.getJSONObject(k);
					String itemName = itemJSON.getString(ITEM_NAME);
					String itemImageOnResourceName = itemJSON
							.getString(ITEM_IMG_ON);
					String itemImageOffResourceName = itemJSON
							.getString(ITEM_IMG_OFF);
					String itemImageLockResourceName = itemJSON
							.optString(ITEM_IMG_LOCK);
					boolean itemImageShowAsLocked = itemJSON.optBoolean(
							ITEM_IMG_SHOW_AS_LOCKED, false);
					String backgroundColor = itemJSON
							.optString(ITEM_BACKGROUND_COLOR);
					int unlockedCount = itemJSON.optInt(ITEM_UNLOCKED_COUNT, 5);
					boolean colorable = itemJSON.optBoolean(ITEM_COLORABLE);
					String launchPanel = itemJSON.optString(LAUNCH_PANEL);
					String lockedlaunchPanel = itemJSON
							.optString(LOCKED_LAUNCH_PANEL);

					// parse productIds in panel
					List<String> ProductIds = new ArrayList<String>();
					if (!itemJSON.isNull(PRODUCT_IDS)) {
						JSONArray productIdsJSON = itemJSON
								.getJSONArray(PRODUCT_IDS);
						for (int j = 0; j < productIdsJSON.length(); j++) {
							String id = productIdsJSON.get(j).toString();
							ProductIds.add(id);
						}
					}
					addProductIds(ProductIds);
					Item item = new Item(itemName, itemImageOnResourceName,
							itemImageOffResourceName,
							itemImageLockResourceName, itemImageShowAsLocked,
							ProductIds, launchPanel, lockedlaunchPanel);
					item.setBackgroundColor(backgroundColor);
					item.setColorable(colorable);
					item.setUnlockedCount(unlockedCount);
					items.add(item);
				}
			}
			// add to panels list
			PanelInfo panel = null;
			if (panelType.equals(PanelManager.SLIDER_PANEL_TYPE)) {
				panel = new SliderPanelInfo(panelName, panelType,
						sliderCaption, launcher, generateThumbnail,
						thumbnailSrc, thumbnailBlendOn, thumbnailBlendOff,
						thumbnailSize, panelImageOnResourceName,
						panelImageOffResourceName, panelProductIds, categories,
						items, sliderPanelMax, sliderPanelProgress);
				((SliderPanelInfo) panel).setLeftImage(sliderLeftImage);
				((SliderPanelInfo) panel).setRightImage(sliderRightImage);
			} else if (panelType.equals(PanelManager.SLIDERS_PANEL_TYPE)) {
				panel = new SlidersPanelInfo(panelName, panelType,
						sliderCaption, sliderCaption2, launcher,
						generateThumbnail, thumbnailSrc, thumbnailBlendOn,
						thumbnailBlendOff, thumbnailSize,
						panelImageOnResourceName, panelImageOffResourceName,
						panelProductIds, categories, items, sliderPanelMax,
						sliderPanelProgress, sliderSeekBar2PanelMax,
						sliderSeekBar2PanelProgress);
			} else if (panelType.equals(PanelManager.BUTTON_PANEL_TYPE)) {
				panel = new ButtonPanelInfo(panelName, panelType, launcher,
						generateThumbnail, thumbnailSrc, thumbnailBlendOn,
						thumbnailBlendOff, thumbnailSize,
						panelImageOnResourceName, panelImageOffResourceName,
						categories, items, buttonPanelMargin);
			} /*
			 * else if (panelType.equals(PanelManager.ROTATE_MIRROR_PANEL_TYPE))
			 * { panel = new RotateMirrorPanelInfo(panelName, panelType,
			 * launcher, generateThumbnail, thumbnailSrc, thumbnailBlendOn,
			 * thumbnailBlendOff, thumbnailSize, panelImageOnResourceName,
			 * panelImageOffResourceName, categories, items, ResourceName); }
			 */else if (panelType.equals(PanelManager.OK_CANCEL_PANEL_TYPE)) {
				panel = new OKCancelPanelInfo(panelName, panelType, launcher,
						generateThumbnail, thumbnailSrc, thumbnailBlendOn,
						thumbnailBlendOff, thumbnailSize,
						panelImageOnResourceName, panelImageOffResourceName,
						panelImageLockResourceName, panelProductIds,
						OKCancelPanelLocked, categories, items, ResourceName);
			} else if (panelType.equals(PanelManager.OK_CANCEL_BAR_PANEL_TYPE)
					|| panelType.equals(PanelManager.SLIDERS_BAR_PANEL_TYPE)) {
				panel = new OkCancelBarsPanelInfo(panelName, panelType,
						launcher, generateThumbnail, thumbnailSrc,
						thumbnailBlendOn, thumbnailBlendOff, thumbnailSize,
						panelImageOnResourceName, panelImageOffResourceName,
						panelImageLockResourceName, panelProductIds,
						OKCancelPanelLocked, categories, items, ResourceName,
						barsCount, barsProgress, barsMax, barTypes,
						barCaptions, columnCaptions);
			} else {
				panel = new PanelInfo(panelName, panelType, launcher,
						generateThumbnail, thumbnailSrc, thumbnailBlendOn,
						thumbnailBlendOff, thumbnailSize,
						panelImageOnResourceName, panelImageOffResourceName,
						panelImageLockResourceName, panelProductIds,
						categories, items);
			}
			panel.setTargetItem(panelTargetItem);
			panel.setWithFragment(withFragment);
			panel.setAction(action);
			panel.setActionGroup(actionGroup);
			panel.setPriority(priority);

			panels.add(panel);
		}

		// add to structure
		Structure structure = new Structure(panels);
		structure.setPanelsInfo(panels);
		structure.setProductIds(productIds);
		return structure;
	}

	public void addProductIds(List<String> addProductIds) {
		for (int i = 0; i < addProductIds.size(); i++) {
			if (productIds.indexOf(addProductIds.get(i)) == -1) {
				productIds.add(addProductIds.get(i));
			}
		}
	}
}
