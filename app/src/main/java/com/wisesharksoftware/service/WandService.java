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

public class WandService extends BaseService {
	private String wandName="";

	public WandService(ChooseProcessingActivity a, PanelManager m,
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

				String oldvalue = wandName;
				if (state) {
					wandName = buttonName;
				} else {
					wandName = "";
				}
				if (!oldvalue.equals(wandName)) {
					chooseProcessing.showCustomToast(buttonName, true);
					Utils.reportFlurryEvent("WandChoosed", wandName);
					Utils.reportFlurryEvent("Action", "Wand");
					Log.d("processing", "WandChoosed" + wandName);
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
	public ArrayList<Preset> getFilterPreset() {
		if (!wandName.equals("")) {
			int processingIndex = Presets.getProcessingIndex(chooseProcessing,
					wandName);

			Preset filterPreset = chooseProcessing.processingPresets[processingIndex];

			ArrayList<Preset> PresetArray = new ArrayList<Preset>();

			PresetArray.add(filterPreset);
			return PresetArray;
		} else {
			return null;
		}
	}
}
