package com.wisesharksoftware.service;

import java.util.ArrayList;

import android.util.Log;
import android.widget.SeekBar;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.ColorizeHsvFilter;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;

public class SaturationService extends BaseService {
	private int saturation = 50;

	public SaturationService(ChooseProcessingActivity a, PanelManager m,
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
				// brightness = seekBar.getProgress() - 50;

				int oldvalue = saturation;
				saturation = (int) Math.round(seekBar.getProgress() * 5);
				if (oldvalue != saturation) {
					chooseProcessing.showCustomToast(panelManager
							.getCurrPanel().getPanelName(), true);
					Utils.reportFlurryEvent("Saturation changed",
							"saturation = " + saturation);
					Utils.reportFlurryEvent("Action", "Saturation");
					Log.d("processing", "Saturation changed " + saturation);
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
			}

			@Override
			public void onOK() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRestore() {
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
	}

	@Override
	public void clear() {
		saturation = 50;
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {

		if (saturation != 50) {
			ArrayList<Preset> PresetArray = new ArrayList<Preset>();

			ColorizeHsvFilter filterSaturation = new ColorizeHsvFilter();
			//Log.d("AAA", "add saturation filter with alpa = " + saturation);
			filterSaturation.setSaturation(saturation);

			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterSaturation);

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
