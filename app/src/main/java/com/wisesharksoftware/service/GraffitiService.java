package com.wisesharksoftware.service;

import java.util.ArrayList;
import java.util.List;

import android.graphics.RectF;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.BlendFilter;
import com.wisesharksoftware.core.filters.SaveImageFilter;
import com.wisesharksoftware.core.filters.ThresholdFilter;
import com.wisesharksoftware.core.filters.BlendFilter.Algorithm;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.ButtonPanel.OnLaunchPanelListener;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.panels.bars.BarTypes;
import com.wisesharksoftware.panels.fragment.grid.GridPagerPanel.OnGridItemClickListener;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.panels.okcancel.OKCancelBarsPanel;
import com.wisesharksoftware.panels.okcancel.OKCancelPanel;
import com.wisesharksoftware.panels.ButtonPanel;
import com.wisesharksoftware.panels.IPanel;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.service.base.IService;
import com.wisesharksoftware.service.base.ServicesManager;
import com.wisesharksoftware.sticker.DrawableHighlightView;

public class GraffitiService extends StickerService implements IService {
	private static final String TAG = GraffitiService.class.getSimpleName();
	private int categoryCount = 0;
	private int alpha = 255; 
	private BlendFilter filter;
	private ThresholdFilter filterThreshold;
	private BlendFilter filterBlend;
	private SaveImageFilter filterSaveImage;

	public GraffitiService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		super(a, m, action, actionGroup);
	}

	@Override
	public void addSticker(String path, RectF position) {
	}

	public DrawableHighlightView getHighlightView() {
		return hv;
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {
		ArrayList<Preset> PresetArray = new ArrayList<Preset>();
		if (filter != null) {
			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterSaveImage);
			FilterArray.add(filterThreshold);
			FilterArray.add(filter);
			FilterArray.add(filterBlend);

			Filter[] filters = new Filter[FilterArray.size()];
			FilterArray.toArray(filters);

			Preset preset = new Preset();
			preset.setFilters(filters);

			PresetArray.add(preset);
			return PresetArray;
		}
		if (stickerName != null && chooseProcessing != null && chooseProcessing.mBitmap != null) {			
			filter = new BlendFilter();
			filter.setBlendSrc(stickerName.replace(chooseProcessing
					.getExternalFilesDir(null).toString() + "/assets/sd/", ""));
			filter.setAlgorithm(Algorithm.multiply);
			filter.setAlpha(255 - alpha);

			filterThreshold = new ThresholdFilter();
			filterThreshold.setLowerThreshold(100);
			filterThreshold.setUpperThreshold(110);

			filterBlend = new BlendFilter();
			filterBlend.setAlgorithm(Algorithm.transparency_alpha);
			filterBlend.setBlend_with_image_memory(true);
			filterBlend.setAlpha(255 - alpha);

			filterSaveImage = new SaveImageFilter();

			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterSaveImage);
			FilterArray.add(filterThreshold);
			FilterArray.add(filter);
			FilterArray.add(filterBlend);

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
		chooseProcessing.processImage();
	}

	@Override
	public void setDrawableHighlightView(DrawableHighlightView view) {
		this.hv = view;

	}

	@Override
	public String getName() {
		return "Graffiti";
	}

	@Override
	public OnLaunchPanelListener getOnLaunchPanelListener() {
		return null;
	}

	public com.wisesharksoftware.panels.okcancel.IOkCancelListener getOkCancelListener() {
		return new IOkCancelListener() {

			@Override
			public void onRestore() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStop(Object... params) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onOK() {
				Utils.reportFlurryEvent("Action", "Sticker");
//				onApplyCurrent(stickerName);
//				chooseProcessing.processImage();
				if (categoryCount > 1) {
					panelManager.upLevel();
				}
				chooseProcessing.hideGrid();
			}

			@Override
			public void onLocked(boolean lock) {
				if (!lock || stickerProductIds == null
						|| stickerProductIds.size() == 0) {
					onOK();
					panelManager.upLevel();
				} else {
					boolean locked = true;
					if (stickerProductIds != null) {
						for (String p : stickerProductIds) {
							if (chooseProcessing.isItemPurchased(
									chooseProcessing, p)) {
								locked = false;
								break;
							}
						}
					}
					if (locked) {
						chooseProcessing.showBuyDialog(stickerProductIds);
					} else {
						onOK();
						panelManager.upLevel();
					}
				}

			}

			@Override
			public void onChange(Object... params) {
				try {
					BarTypes barType = (BarTypes) params[1];
					int change = (Integer) params[2];
					if (barType == BarTypes.OPACITY) {
						alpha = change;
					}
					onApplyCurrent(stickerName);
					} catch (ClassCastException e) {

				}
			}

			@Override
			public void onCancel() {				
				panelManager.upLevel();
				stickerName = null;
				onApplyCurrent(stickerName);
				chooseProcessing.processImage();				
			}

			@Override
			public void onShow() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onChangeFromUser(Object... params) {
				// TODO Auto-generated method stub
				
			}
		};
	};

	
	OnStateListener onStateListener = new OnStateListener() {

		@Override
		public void onShow(List<LauncherItemView> items) {
			try {
				chooseProcessing.showGrid(
						(ButtonPanel) panelManager.getPanel("GraffitiPack"),
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

			stickerProductIds = item.getProductIds();
			Utils.reportFlurryEvent("StickerChoosed", buttonName);
			chooseProcessing.hideRemoveAdsButton();

			ServicesManager.instance().setCurrentService(GraffitiService.this);

			// addSticker(buttonName, null);
			stickerName = buttonName;
			onApplyCurrent(stickerName);

			chooseProcessing.hideGrid();
			if (!lock) {
				stickerProductIds = null;
			}
			IPanel panel = panelManager.getPanel("OKCancelBars");
			if (panel != null) {
				if (item.getItem().getColorable()) {
					((OKCancelBarsPanel) panel).showColorBar();
				} else {
					((OKCancelBarsPanel) panel).hideColorBar();
				}
				if (item.locked && lock) {
					((OKCancelPanel) panel).setLocked(true);
				} else {

					((OKCancelPanel) panel).setLocked(false);
				}

				if (((ButtonPanel) panelManager.getPanel("GraffitiPack"))
						.getItems().size() != 1) {
					panel.setRootPanel(panelManager.getCurrPanel());
					panelManager.ShowPanel("OKCancelBars",
							panelManager.getCurrPanel());

				} else {
					panel.setRootPanel(panelManager.getCurrPanel());
					panelManager.ShowPanel("OKCancelBars",
							panelManager.getCurrPanel());
				}
				categoryCount = ((ButtonPanel) panelManager.getPanel("GraffitiPack")).getItems().size();
			}
		}
	};

	@Override
	public OnItemListener getOnItemListener() {
		return null;
	}
}
