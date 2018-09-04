package com.wisesharksoftware.service;

import java.util.ArrayList;

import android.util.Log;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Presets;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;

public class LensService extends BaseService {

	private String lensName = "";

	public LensService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		setChooseProcessing(a);
		setPanelManager(m);
		setAction(action);
		setActionGroup(actionGroup);
	}

	@Override
	public OnItemListener getOnItemListener() {

		return new OnItemListener() {
			@Override
			public boolean onItemSelected(LauncherItemView item,
					String buttonName, boolean state) {
				/*
				 * if (new_values) { Log.d("processing", "Lens cancel " +
				 * lensName); return false; }
				 */
				ServicesManager.instance().setCurrentService(self());
				String oldvalue = lensName;
				if (state) {
					lensName = buttonName;
				} else {
					lensName = "";
				}
				if (!oldvalue.equals(lensName)) {
					chooseProcessing.showCustomToast(buttonName, true);
					Utils.reportFlurryEvent("LensChoosed", lensName);
					Utils.reportFlurryEvent("Action", "Lens");
					Log.d("processing", "LensChoosed" + lensName);
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
				return true;
			}

			@Override
			public void onLockedItemSelected(String buttonName,
					LauncherItemView item) {
				chooseProcessing.showFacebookPost();

			}
		};
	}

	@Override
	public void clear() {
		lensName = "";
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {
		if (!lensName.equals("")) {
			int processingIndex = Presets.getProcessingIndex(chooseProcessing,
					lensName);

			Preset filterPreset = chooseProcessing.processingPresets[processingIndex];

			ArrayList<Preset> PresetArray = new ArrayList<Preset>();

			PresetArray.add(filterPreset);
			return PresetArray;
		} else {
			return null;
		}
	}
}
