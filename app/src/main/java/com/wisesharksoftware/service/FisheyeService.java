package com.wisesharksoftware.service;

import java.util.ArrayList;

import android.util.Log;
import android.widget.SeekBar;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.FishEyeFilter;
import com.wisesharksoftware.core.filters.SquareFilter;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;

public class FisheyeService extends BaseService {
	int fisheyeCurvature = 0;

	public FisheyeService(ChooseProcessingActivity a, PanelManager m,
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
				ServicesManager.instance().setCurrentService(self());
				SeekBar seekBar = (SeekBar) params[0];
				/*
				 * if (new_values) { seekBar.setProgress(fisheyeCurvature);
				 * Log.d("processing", "fisheyeCurvature cancel " +
				 * fisheyeCurvature); return; }
				 */
				int oldvalue = fisheyeCurvature;
				fisheyeCurvature = seekBar.getProgress();
				if (oldvalue != fisheyeCurvature) {
					chooseProcessing.showCustomToast(panelManager
							.getCurrPanel().getPanelName(), true);
					Utils.reportFlurryEvent("fisheyeCurvature changed",
							"fisheyeCurvature = " + fisheyeCurvature);
					Utils.reportFlurryEvent("Action", "Fisheye");
					Log.d("processing", "fisheyeCurvature changed "
							+ fisheyeCurvature);
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

				SeekBar seekBar = (SeekBar) params[0];
				boolean fromUser = (Boolean) params[2];
				if (!fromUser) {
					/*
					 * if (new_values) { seekBar.setProgress(fisheyeCurvature);
					 * Log.d("processing", "fisheyeCurvature cancel " +
					 * fisheyeCurvature); return; }
					 */
					int oldvalue = fisheyeCurvature;
					fisheyeCurvature = seekBar.getProgress();
					if (oldvalue != fisheyeCurvature) {
						chooseProcessing.showCustomToast(panelManager
								.getCurrPanel().getPanelName(), true);
						Utils.reportFlurryEvent("fisheyeCurvature changed",
								"fisheyeCurvature = " + fisheyeCurvature);

						Log.d("processing", "fisheyeCurvature changed "
								+ fisheyeCurvature);

						ServicesManager.instance().addToQueue(self());
						chooseProcessing.processImage();
					}
				}
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
		fisheyeCurvature = 0;
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {
		if (fisheyeCurvature != 0) {
			ArrayList<Preset> PresetArray = new ArrayList<Preset>();
			FishEyeFilter filter = new FishEyeFilter();
			filter.setType(FishEyeFilter.TYPE_CIRCLE);
			filter.setCurvature(fisheyeCurvature);
			filter.setScale(2);

			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(new SquareFilter());
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
