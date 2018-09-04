package com.wisesharksoftware.service;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.BlendFilter;
import com.wisesharksoftware.core.filters.BlendFilter.Algorithm;
import com.wisesharksoftware.core.filters.HDRFilter;
import com.wisesharksoftware.core.filters.SaveImageFilter;
import com.wisesharksoftware.core.filters.SquareBorderFilter;
import com.wisesharksoftware.panels.IPanel;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.SliderPanel;
import com.wisesharksoftware.panels.ButtonPanel.OnLaunchPanelListener;
import com.wisesharksoftware.panels.bars.BarTypes;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.IService;
import com.wisesharksoftware.service.base.ServicesManager;

public class NoCropFramesService extends BaseService {
	private int frameColor = 0;

	public NoCropFramesService(ChooseProcessingActivity a, PanelManager m,
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
//				SeekBar seekBar = (SeekBar) params[0];
//
//				int oldvalue = shadowsAlpha;
//				shadowsAlpha = 255 - seekBar.getProgress();
//				if (oldvalue != shadowsAlpha) {
//					chooseProcessing.showCustomToast(panelManager
//							.getCurrPanel().getPanelName(), true);
//					Utils.reportFlurryEvent("shadowsAlpha changed",
//							"shadowsAlpha = " + shadowsAlpha);
//					Utils.reportFlurryEvent("Action", "Shadows");
//					Log.d("processing", "shadowsAlpha changed " + shadowsAlpha);
//					ServicesManager.instance().addToQueue(self());
//					chooseProcessing.processImage();
//				}
				
				Toast.makeText(chooseProcessing, "OnStop", Toast.LENGTH_LONG).show();
				
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
				//Toast.makeText(chooseProcessing, "OK", Toast.LENGTH_LONG).show();
				ServicesManager.instance().addToQueue(self());
				chooseProcessing.processImage();
				panelManager.upLevel();
			}

			@Override
			public void onChange(Object... params) {
				// TODO Auto-generated method stub
				try {
					BarTypes barType = (BarTypes) params[1];
					int change = (Integer) params[2];
					if (barType == BarTypes.COLOR) {
						frameColor = change;
						//hv.setColor(change);
						//Log.d("PANEL", "color = " + change);
					}
				} catch (ClassCastException e) {

				}
			}

			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				panelManager.upLevel();
			}

			@Override
			public void onShow() {
				// TODO Auto-generated method stub	
				for (int i = 0; i < panelManager.panels.size(); i++) {				
					IPanel panel = panelManager.panels.get(i);
					if (!getActionGroup().equals("")) {
						if (panel.getActionGroup().equals(getActionGroup())) {
							panel.restoreOriginal();
						}
					}
				}
			}

			@Override
			public void onChangeFromUser(Object... params) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	@Override
	public OnLaunchPanelListener getOnLaunchPanelListener() {
		return new OnLaunchPanelListener() {

			@Override
			public void onLaunchPanelSelected(LauncherItemView item,
					String nameLaunchPanel, List<String> productIds) {
//				ServicesManager.instance().setCurrentService(
//						NoCropFramesService.this);
//				ServicesManager.instance().addToQueue(self());
				
			}
		};
	}

	@Override
	public void clear() {
		frameColor = 0;
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {
		if (frameColor != 0) {
			ArrayList<Preset> PresetArray = new ArrayList<Preset>();
			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			SquareBorderFilter filterSquareBorder = new SquareBorderFilter();
			filterSquareBorder.setReflect(false);
			filterSquareBorder.setBackground(0);			
			filterSquareBorder.setUsecolorbackground(true);			
			filterSquareBorder.setRed(Color.red(frameColor));
			filterSquareBorder.setGreen(Color.green(frameColor));
			filterSquareBorder.setBlue(Color.blue(frameColor));
			FilterArray.add(filterSquareBorder);
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
