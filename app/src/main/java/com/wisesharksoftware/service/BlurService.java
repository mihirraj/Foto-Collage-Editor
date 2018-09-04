package com.wisesharksoftware.service;

import java.util.ArrayList;

import android.util.Log;
import android.widget.SeekBar;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.ColorizeHsvFilter;
import com.wisesharksoftware.core.filters.SharpnessFilter;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.IService;
import com.wisesharksoftware.service.base.ServicesManager;

public class BlurService extends BaseService {
	private int sharpness = 0;

	public BlurService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		setChooseProcessing(a);
		setPanelManager(m);
		setAction(action);
		setActionGroup(actionGroup);
	}

	@Override
	public IOkCancelListener getOkCancelListener() {
		return new IOkCancelListener() {

			@Override
			public void onStop(Object... params) {
				SeekBar seekBar = (SeekBar) params[0];
				int oldvalue = sharpness;
				sharpness = (int) Math.round(seekBar.getProgress() - 50);
				if (oldvalue != sharpness) {
					chooseProcessing.showCustomToast(panelManager
							.getCurrPanel().getPanelName(), true);
					Utils.reportFlurryEvent("Sharpness changed", "sharpness = "
							+ sharpness);
					Utils.reportFlurryEvent("Action", "Sharpness");
					Log.d("processing", "Sharpness changed " + sharpness);
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
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
			public void onRestore() {
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
	}

	@Override
	public void clear() {
		sharpness = 0;
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {

		if (sharpness != 0) {
			ArrayList<Preset> PresetArray = new ArrayList<Preset>();

			SharpnessFilter filterSharpness = new SharpnessFilter();
			//Log.d("AAA", "add sharpness filter with sharp = " + sharpness);
			filterSharpness.setSize(sharpness);

			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterSharpness);

			Filter[] filters = new Filter[FilterArray.size()];
			FilterArray.toArray(filters);

			Preset preset = new Preset();
			preset.setFilters(filters);

			PresetArray.add(preset);
			return PresetArray;
		} else {
			return null;
		}

	}
}
