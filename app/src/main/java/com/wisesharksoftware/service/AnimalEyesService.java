package com.wisesharksoftware.service;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.AnimalEyesFilter;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.ButtonPanel.OnLaunchPanelListener;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.panels.fragment.grid.GridPagerPanel.OnGridItemClickListener;
import com.wisesharksoftware.panels.ButtonPanel;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.service.base.IService;
import com.wisesharksoftware.service.base.ServicesManager;
import com.wisesharksoftware.sticker.DrawableHighlightView;

public class AnimalEyesService extends StickerService implements IService {
	private static final String TAG = AnimalEyesService.class.getSimpleName();
	private AnimalEyesFilter filter;

	public AnimalEyesService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		super(a, m, action, actionGroup);
	}

	public DrawableHighlightView getHighlightView() {
		return null;
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {
		ArrayList<Preset> PresetArray = new ArrayList<Preset>();
		if (filter != null) {

			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filter);

			Filter[] filters = new Filter[FilterArray.size()];
			FilterArray.toArray(filters);

			Preset preset = new Preset();
			preset.setFilters(filters);

			PresetArray.add(preset);
			return PresetArray;
		}
		if (chooseProcessing != null) {
			mImageView.invalidate();

			filter = new AnimalEyesFilter();
			filter.setBlendSrc(stickerName.replace(chooseProcessing.getExternalFilesDir(null).toString() + "/assets/sd/", ""));
				
			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filter);

			Filter[] filters = new Filter[FilterArray.size()];
			FilterArray.toArray(filters);

			Preset preset = new Preset();
			preset.setFilters(filters);

			PresetArray.add(preset);			
		}
		return PresetArray;
	}

	@Override
	public void onApplyCurrent(String path) {
		filter = null;
		ServicesManager.instance().addToQueue(self());
		for (int i = 0; i <	ServicesManager.getServices().size(); i++) {
			if (getActionGroup().equals(ServicesManager.getServices().get(i).getActionGroup())) {
				ServicesManager.getServices().get(i).clear();
			}
		}
		chooseProcessing.processImage();
	}

	@Override
	public void setDrawableHighlightView(DrawableHighlightView view) {
		this.hv = view;

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "AnimalEye";
	}

	@Override
	public OnLaunchPanelListener getOnLaunchPanelListener() {
		// TODO Auto-generated method stub
		return null;
	}

	OnStateListener onStateListener = new OnStateListener() {

		@Override
		public void onShow(List<LauncherItemView> items) {
			try {
				chooseProcessing.showGrid(
						(ButtonPanel) panelManager.getPanel("AnimalEyes"),
						getActionGroup(), items, onGridItemClickListener);
			} catch (ClassCastException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onHide() {
			chooseProcessing.hideGrid();
		}

	};;

	@Override
	public OnStateListener getOnStateListener() {
		return onStateListener;
	};

	final OnGridItemClickListener onGridItemClickListener = new OnGridItemClickListener() {

		@Override
		public void onClick(String buttonName, LauncherItemView item,
				boolean lock) {
			// stickerName = buttonName;
			if (!lock) {				
			stickerProductIds = item.getProductIds();
			Utils.reportFlurryEvent("StickerChoosed", buttonName);
			chooseProcessing.hideRemoveAdsButton();

			ServicesManager.instance().setCurrentService(AnimalEyesService.this);

			//addSticker(buttonName, null);

			chooseProcessing.hideGrid();
			if (!lock) {
				stickerProductIds = null;
			}
				stickerName = buttonName;
				onApplyCurrent(stickerName);
				//	if (categoryCount > 1) {
				panelManager.upLevel();
				//}
			} else {
				stickerProductIds = item.getProductIds();
				chooseProcessing.showBuyDialog(stickerProductIds);
			}

		}
	};

	@Override
	public OnItemListener getOnItemListener() {
		return null;
	}	
}
