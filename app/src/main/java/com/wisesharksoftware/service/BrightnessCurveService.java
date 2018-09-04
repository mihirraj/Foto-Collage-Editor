package com.wisesharksoftware.service;

import java.util.ArrayList;

import android.util.Log;
import android.widget.SeekBar;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.ContrastBrightnessFilter;
import com.wisesharksoftware.core.filters.CurveFilter;
import com.wisesharksoftware.core.filters.SaveImageFilter;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;

public class BrightnessCurveService extends BaseService {
	private int brightness = 0;
	
	public BrightnessCurveService(ChooseProcessingActivity a, PanelManager m,
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
			public void onLockedItemSelected(String buttonName,
					LauncherItemView item) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onItemSelected(LauncherItemView item,
					String buttonName, boolean state) {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}

	@Override
	public IOkCancelListener getOkCancelListener() {
		return new IOkCancelListener() {

			@Override
			public void onStop(Object... params) {
				SeekBar seekBar = (SeekBar) params[0];
				String panelName = (String) params[1];
				if (panelName.equals("BrightnessCurve")) {
					int oldvalue = brightness;
					brightness = (seekBar.getProgress() * 5 - 50) * 3;// range:-150..150
					if (oldvalue != brightness) {
						chooseProcessing.showCustomToast(panelManager
								.getCurrPanel().getPanelName(), true);
						Utils.reportFlurryEvent("Brightness changed",
								"brightness = " + brightness);
						Utils.reportFlurryEvent("Action", "Brightness");
						Log.d("processing", "Brightness changed " + brightness);
						ServicesManager.instance().addToQueue(self());
						chooseProcessing.processImage();
					}
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
		brightness = 0;
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {

		if ((brightness != 0)) {			
			CurveFilter filter = new CurveFilter();
			String spline = "0,0;127," + (127 + brightness) + ";255,255";
			filter.setRedSpline(spline);
			filter.setBlueSpline(spline);
			filter.setGreenSpline(spline);			
			ArrayList<Preset> PresetArray = new ArrayList<Preset>();

			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filter);

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
