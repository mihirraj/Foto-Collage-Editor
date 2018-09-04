package com.wisesharksoftware.service;

import java.util.ArrayList;

import android.util.Log;
import android.widget.SeekBar;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.ColorTemperatureFilter;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;

public class TemperatureService extends BaseService {
	private int temperature = 0;

	public TemperatureService(ChooseProcessingActivity a, PanelManager m,
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

				int oldvalue = temperature;
				temperature = (int) Math.round(seekBar.getProgress()) - 10;
				if (oldvalue != temperature) {
					chooseProcessing.showCustomToast(panelManager
							.getCurrPanel().getPanelName(), true);
					Utils.reportFlurryEvent("Temperature changed",
							"temperature = " + temperature);
					Utils.reportFlurryEvent("Action", "Temperature");
					Log.d("processing", "Temperature changed " + temperature);
					ServicesManager.instance().addToQueue(self());
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
	}

	@Override
	public void clear() {
		temperature = 0;
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {

		if (temperature != 0) {
			ArrayList<Preset> PresetArray = new ArrayList<Preset>();

			ColorTemperatureFilter filterTemperature = new ColorTemperatureFilter();
			//Log.d("AAA", "add temperature filter with temp = " + temperature);
			filterTemperature.setTemperature(temperature);

			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterTemperature);

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
