package com.wisesharksoftware.service;

import java.util.ArrayList;

import android.util.Log;
import android.widget.SeekBar;

import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.ThresholdBlurFilter;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;

public class BlendService extends BaseService {

	private int blend;
	IOkCancelListener okCancelListener = new IOkCancelListener() {

		@Override
		public void onStop(Object... params) {
			// TODO Auto-generated method stub

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
			ServicesManager.instance().setCurrentService(self());
			SeekBar seekBar = (SeekBar) params[0];
			/*
			 * if (new_values) { seekBar.setProgress(blend); Log.d("processing",
			 * "Brightness cancel " + brightness); return; }
			 */
			int oldvalue = blend;
			blend = (int) Math.round(seekBar.getProgress() * 25); // 0..255
			if (oldvalue != blend) {
				chooseProcessing.showCustomToast(panelManager.getCurrPanel()
						.getPanelName(), true);
				Utils.reportFlurryEvent("Blend changed", "blend = " + blend);
				Utils.reportFlurryEvent("Action", "Blend");
				Log.d("processing", "Blend changed " + blend);
				ServicesManager.instance().addToQueue(self());
				chooseProcessing.processImage();
			}

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

	public IOkCancelListener getOkCancelListener() {
		return okCancelListener;
	};

	@Override
	public ArrayList<Preset> getFilterPreset() {
		if (blend != 0) {
			ArrayList<Preset> PresetArray = new ArrayList<Preset>();
			ThresholdBlurFilter filterBlend = new ThresholdBlurFilter();
			// filterContrast.setContrast(1);
			//Log.d("AAA", "add blend filter with alpa = " + blend);
			filterBlend.setAlpha(blend);

			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterBlend);

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
