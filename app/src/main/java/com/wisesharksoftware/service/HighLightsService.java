package com.wisesharksoftware.service;

import java.util.ArrayList;

import android.util.Log;
import android.widget.SeekBar;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.BlendFilter;
import com.wisesharksoftware.core.filters.HDRFilter;
import com.wisesharksoftware.core.filters.SaveImageFilter;
import com.wisesharksoftware.core.filters.BlendFilter.Algorithm;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;

public class HighLightsService extends BaseService {
	private int highlightsAlpha = 255;

	public HighLightsService(ChooseProcessingActivity a, PanelManager m,
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

				int oldvalue = highlightsAlpha;
				highlightsAlpha = seekBar.getProgress();
				if (oldvalue != highlightsAlpha) {
					chooseProcessing.showCustomToast(panelManager
							.getCurrPanel().getPanelName(), true);
					Utils.reportFlurryEvent("highlightsAlpha changed",
							"highlightsAlpha = " + highlightsAlpha);
					Utils.reportFlurryEvent("Action", "Highlights");
					Log.d("processing", "highlightsAlpha changed "
							+ highlightsAlpha);

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
			public void onRestore() {
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
		highlightsAlpha = 255;
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {
		if (highlightsAlpha != 255) {
			ArrayList<Preset> PresetArray = new ArrayList<Preset>();
			ArrayList<Filter> FilterArray = new ArrayList<Filter>();

			SaveImageFilter filterSave = new SaveImageFilter();
			FilterArray.add(filterSave);

			HDRFilter filterHDR = new HDRFilter();
			filterHDR.setHighlightsCurve(0, 0, 75, 0, 255, 179);
			FilterArray.add(filterHDR);

			BlendFilter filterBlend = new BlendFilter();
			filterBlend.setAlgorithm(Algorithm.transparency_alpha);
			filterBlend.setBlend_with_image_memory(true);
			filterBlend.setAlpha(highlightsAlpha);
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
