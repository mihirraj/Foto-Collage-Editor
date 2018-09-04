package com.wisesharksoftware.service;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.widget.SeekBar;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Presets;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.BlendFilter;
import com.wisesharksoftware.core.filters.SaveImageFilter;
import com.wisesharksoftware.core.filters.BlendFilter.Algorithm;
import com.wisesharksoftware.panels.ButtonPanel;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.ButtonPanel.OnLaunchPanelListener;
import com.wisesharksoftware.panels.IPanel;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.SliderPanel;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;

public class ModesService extends BaseService {
	private int modesAlpha = -1;
	private String modesName = "";

	public ModesService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		setChooseProcessing(a);
		setPanelManager(m);
		setAction(action);
		setActionGroup(actionGroup);

	}

	@Override
	public OnLaunchPanelListener getOnLaunchPanelListener() {
		return new OnLaunchPanelListener() {

			@Override
			public void onLaunchPanelSelected(LauncherItemView item,
					String nameLaunchPanel, List<String> productIds) {
				ServicesManager.instance().setCurrentService(self());
				// hideGrid();
				// Log.d("AAA", "nameLaunchPanel = " + nameLaunchPanel);
				/*
				 * if (new_values) { Log.d("processing", "Modes cancel " +
				 * modesName); return; }
				 */
				modesName = nameLaunchPanel;
				modesAlpha = 0;
				chooseProcessing.showCustomToast(nameLaunchPanel, true);
				((SliderPanel) panelManager.getPanel("Modes_Alpha"))
						.setProgress(255);

				ServicesManager.instance().addToQueue(self());
				chooseProcessing.processImage();
			}
		};
	}

	@Override
	public OnItemListener getOnItemListener() {
		return new OnItemListener() {

			@Override
			public void onLockedItemSelected(String buttonName,
					LauncherItemView item) {
				chooseProcessing.showBuyDialog(item.getProductIds());

			}

			@Override
			public boolean onItemSelected(LauncherItemView item,
					String buttonName, boolean state) {
				ServicesManager.instance().setCurrentService(self());
				/*
				 * if (new_values) { Log.d("processing", "Modes cancel " +
				 * modesName); return false; }
				 */
				String oldvalue = modesName;
				chooseProcessing.showCustomToast(buttonName, true);
				if (state) {
					modesName = buttonName;
				} else {
					modesName = "";
				}
				if (!oldvalue.equals(modesName)) {
					Utils.reportFlurryEvent("ModesNameChoosed", modesName);
					Log.d("processing", "ModeChoosed" + modesName);
					Utils.reportFlurryEvent("Action", "Modes");
					modesAlpha = -1;
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
				return true;
			}
		};
	}

	IOkCancelListener okCancelListener = new IOkCancelListener() {

		@Override
		public void onStop(Object... params) {

			SeekBar seekBar = (SeekBar) params[0];
			/*
			 * if (new_values) { seekBar.setProgress(255 - modesAlpha);
			 * Log.d("processing", "modesAlpha cancel " + modesAlpha); return; }
			 */
			int oldvalue = modesAlpha;
			modesAlpha = 255 - seekBar.getProgress();
			if (oldvalue != modesAlpha) {
				/*
				 * chooseProcessing .showCustomToast(panelManager.getCurrPanel()
				 * .getRootPanel().getPanelName(), true);
				 */
				Utils.reportFlurryEvent("modesAlpha changed", "modesAlpha = "
						+ modesAlpha);
				Utils.reportFlurryEvent("Action", "ModesAlpha");
				Log.d("processing", "modesAlpha changed " + modesAlpha);
				chooseProcessing.processImage();
			}

		}

		@Override
		public void onRestore() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onOK() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocked(boolean lock) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onChange(Object... params) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

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

	@Override
	public IOkCancelListener getOkCancelListener() {
		return okCancelListener;
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {
		ArrayList<Preset> PresetArray = new ArrayList<Preset>();
		if (modesAlpha != -1) {
			SaveImageFilter filterSave = new SaveImageFilter();
			// Log.d("AAA", "add whitenEyes filter with max_coef = " +
			// whitenEyes + " and smoothSkinAlpha = " + smoothSkinAlpha);
			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterSave);

			Filter[] filters = new Filter[FilterArray.size()];
			FilterArray.toArray(filters);

			Preset preset = new Preset();
			preset.setFilters(filters);

			PresetArray.add(preset);
		}

		if (!modesName.equals("")) {
			int processingIndex = Presets.getProcessingIndex(chooseProcessing,
					modesName);

			Preset filterPreset = chooseProcessing.processingPresets[processingIndex];
			if (filterPreset != null) {
				PresetArray.add(filterPreset);
			}
		}

		// blend with prepocessing saved image;
		if ((modesAlpha != -1) && (!modesName.equals(""))) {
			BlendFilter filterBlend = new BlendFilter();
			// Log.d("AAA", "add whitenEyes filter with max_coef = " +
			// whitenEyes + " and smoothSkinAlpha = " + smoothSkinAlpha);
			filterBlend.setAlgorithm(Algorithm.transparency_alpha);
			filterBlend.setBlend_with_image_memory(true);
			filterBlend.setAlpha(modesAlpha);
			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterBlend);

			Filter[] filters = new Filter[FilterArray.size()];
			FilterArray.toArray(filters);

			Preset preset = new Preset();
			preset.setFilters(filters);

			PresetArray.add(preset);
		}

		return PresetArray;

	}
}
