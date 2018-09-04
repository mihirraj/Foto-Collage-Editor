package com.wisesharksoftware.service;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.widget.Toast;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Presets;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.HDRFilter2;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.panels.okcancel.SlidersBarsPanel;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;

public class HDRToolService extends BaseService {
	private int hdrAlpha = 153;
	private int hdrBlurSize = 89;
	private int midtonesAlpha = 153;
	private int midtonesBlurSize = 89;
	private int midtonesBlack = 0;

	private boolean valuesFromSliders = false; 
	
	//boolean HDRToolFirstShow = true;
	private String effectName = "";
	private String mode = "";//lastEffectName
	private LauncherItemView effectItem = null;
	private boolean firstShow = true;

	public HDRToolService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		setChooseProcessing(a);
		setPanelManager(m);
		setAction(action);
		setActionGroup(actionGroup);
	}

	public String getEffectName() {
		return effectName;
	}
	
	@Override
	public OnStateListener getOnStateListener() {
		return new OnStateListener() {

			@Override
			public void onShow(List<LauncherItemView> items) {
				//Toast.makeText(chooseProcessing, "SHOW",Toast.LENGTH_SHORT).show();
//				hdrAlpha = 255;
//				hdrBlurSize = 61;
//				midtonesAlpha = 255;
//				midtonesBlurSize = 61;
		//		HDRToolFirstShow = false;
				
//				if (panelManager.getCurrPanel().getPanelType().equals("sliders_bars_panel")) {
//					Toast.makeText(chooseProcessing, "sliders_bars_panel",Toast.LENGTH_SHORT).show();
//					SlidersBarsPanel sb = (SlidersBarsPanel) panelManager.getCurrPanel();
//					int[] progressValues = {hdrAlpha, hdrBlurSize, midtonesAlpha, midtonesBlurSize, midtonesBlack};					
//					sb.setToValues(progressValues);
//				} else {
//					Toast.makeText(chooseProcessing, "not sliders_bars_panel it\'s " + panelManager.getCurrPanel().getPanelType(),Toast.LENGTH_SHORT).show();
//				}
				ServicesManager.instance().setCurrentService(self());

			}

			@Override
			public void onHide() {

			}
		};
	}

	IOkCancelListener okCancelListener = new IOkCancelListener() {

		@Override
		public void onStop(Object... params) {
			valuesFromSliders = true;
			int barId = (Integer) params[0];
			int change = (Integer) params[2];
			setEffectItem(null);
			setEffectName("");			
			chooseProcessing.hideProgressToast();
			if (barId == 0) {
				/*
				 * if (new_values) { Log.d("processing", "HDRBlurSize cancel " +
				 * hdrBlurSize); return; }
				 */
				int oldvalue = hdrBlurSize;
				hdrBlurSize = change * 2 + 1;

				if (oldvalue != hdrBlurSize) {
					chooseProcessing.showCustomToast(panelManager
							.getCurrPanel().getPanelName(), true);
					Utils.reportFlurryEvent("HDRBlurSize changed",
							"hdrBlurSize = " + hdrBlurSize);
					Log.d("processing", "hdrBlurSize changed " + hdrBlurSize);
					Utils.reportFlurryEvent("Action", "HDRTool");
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
			}
			// HDR strength
			if (barId == 1) {
				/*
				 * if (new_values) { Log.d("processing", "HDRAlpha cancel " +
				 * hdrAlpha); return; }
				 */
				int oldvalue = hdrAlpha;
				hdrAlpha = change;
				if (oldvalue != hdrAlpha) {
					chooseProcessing.showCustomToast(panelManager
							.getCurrPanel().getPanelName(), true);
					Utils.reportFlurryEvent("HDRAlpha changed", "hdrAlpha = "
							+ hdrAlpha);
					Log.d("processing", "hdrAlpha changed " + hdrAlpha);
					Utils.reportFlurryEvent("Action", "HDRTool");
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
			}
			// MIDTONES radius
			if (barId == 2) {
				/*
				 * if (new_values) { Log.d("processing",
				 * "midtonesBlurSize cancel " + midtonesBlurSize); return; }
				 */
				int oldvalue = midtonesBlurSize;
				midtonesBlurSize = change * 2 + 1;
				if (oldvalue != midtonesBlurSize) {
					chooseProcessing.showCustomToast(panelManager
							.getCurrPanel().getPanelName(), true);
					Utils.reportFlurryEvent("midtonesBlurSize changed",
							"midtonesBlurSize = " + midtonesBlurSize);
					Log.d("processing", "midtonesBlurSize changed "
							+ midtonesBlurSize);
					Utils.reportFlurryEvent("Action", "HDRTool");
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}

			}
			// MIDTONES strength
			if (barId == 3) {
				/*
				 * if (new_values) { Log.d("processing", "midtonesAlpha cancel "
				 * + midtonesAlpha); return; }
				 */
				int oldvalue = midtonesAlpha;
				midtonesAlpha = change * 2 + 1;
				if (oldvalue != midtonesAlpha) {
					chooseProcessing.showCustomToast(panelManager
							.getCurrPanel().getPanelName(), true);
					Utils.reportFlurryEvent("midtonesAlpha changed",
							"midtonesAlpha = " + midtonesAlpha);
					Log.d("processing", "midtonesAlpha changed "
							+ midtonesAlpha);
					Utils.reportFlurryEvent("Action", "HDRTool");
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
			}
			
			// MIDTONES black
			if (barId == 4) {
				/*
				 * if (new_values) { Log.d("processing", "midtonesAlpha cancel "
				 * + midtonesAlpha); return; }
				 */
				int oldvalue = midtonesBlack;
				midtonesBlack = change;
				if (oldvalue != midtonesBlack) {
					chooseProcessing.showCustomToast(panelManager
							.getCurrPanel().getPanelName(), true);
					Utils.reportFlurryEvent("midtonesBlack changed",
							"midtonesBlack = " + midtonesBlack);
					Log.d("processing", "midtonesBlack changed "
							+ midtonesBlack);
					Utils.reportFlurryEvent("Action", "HDRTool");
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

		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRestore() {
			hdrAlpha = 0;
			hdrBlurSize = 1;
			midtonesAlpha = 0;
			midtonesBlack = 0;
			midtonesBlurSize = 1;
			ServicesManager.instance().addToQueue(self());
			chooseProcessing.processImage();
		}		
		
		@Override
		public void onShow() {
			// TODO Auto-generated method stub
			if (panelManager.getCurrPanel().getPanelType().equals("sliders_bars_panel")) {				
				SlidersBarsPanel sb = (SlidersBarsPanel) panelManager.getCurrPanel();
				if (!valuesFromSliders) {
					if (firstShow) {
						sb.setToDefaultValues();
						firstShow = false;
					} else {
						int[] progressValues = {hdrAlpha, hdrBlurSize, midtonesAlpha, midtonesBlurSize, midtonesBlack};					
						sb.setToValues(progressValues);
					}
				}
			}			
		}

		@Override
		public void onChangeFromUser(Object... params) {
			int barId = (Integer) params[0];
			int change = (Integer) params[2];
			if (barId == 0) {							
				chooseProcessing.showProgressToast("HDR RADIUS: " + (((change * 2 + 1) * 100) / 89));
			} else if (barId == 1){
				chooseProcessing.showProgressToast("HDR STRENGTH: " + (change * 100 / 255));
			} else if (barId == 2) {
				chooseProcessing.showProgressToast("MIDTONE RADIUS: " + ((change * 2 + 1) * 100 / 89));
			} else if (barId == 3) {
				chooseProcessing.showProgressToast("MIDTONE STRENGTH: " + (change * 100 / 255));
			} else if (barId == 4) {
				chooseProcessing.showProgressToast("MIDTONE BLACK: " + change);
			}			
		}
	};
	
	@Override
	public void clear() {
		hdrAlpha = 0;
		hdrBlurSize = 1;
		midtonesAlpha = 0;
		midtonesBlack = 0;
		midtonesBlurSize = 1;
		effectName = "";
		setEffectItem(null);
		mode = "";
	}

	public IOkCancelListener getOkCancelListener() {
		return okCancelListener;
	};
	
	@Override
	public OnItemListener getOnItemListener() {
		return new OnItemListener() {

			@Override
			public boolean onItemSelected(LauncherItemView item,
					String buttonName, boolean state) {
				/*
				 * if (new_values) { Log.d("processing", "Effects cancel " +
				 * effectName); return false; }
				 */
				valuesFromSliders = false;

				chooseProcessing.showCustomToast(buttonName, true);
				if (state) {
					if (!getEffectName().equals(buttonName)) {
						//setEffectItem(item);
						setEffectName(buttonName);
						Utils.reportFlurryEvent("EffectChoosed", effectName);
						Utils.reportFlurryEvent("Action", "Effects");
						Log.d("processing", "EffectChoosed" + getEffectName());
						// alpha = -1;
						ServicesManager.instance().addToQueue(self());
						chooseProcessing.processImage();
					}
				} else {
					setEffectItem(null);
					setEffectName("");
				}

				return true;
			}

			@Override
			public void onLockedItemSelected(String buttonName,
					LauncherItemView item) {
				chooseProcessing.showBuyDialog(item.getProductIds());
				// showFacebookPost();
			}
		};
	}
	
	public void setEffectItem(LauncherItemView effectItem) {
		if (this.effectItem != null) {
			this.effectItem.setState(false,false);
			effectItem.setState(true,false);
		}		
		this.effectItem = effectItem;
	}

	public void setEffectName(String effectName) {
		if (!effectName.equals("")) {
			mode = effectName;
		}
		this.effectName = effectName;
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {
		ArrayList<Preset> PresetArray = new ArrayList<Preset>();
		ArrayList<Filter> FilterArrayOtherFilters = new ArrayList<Filter>();		
		if (!mode.equals("")) {
			if (!effectName.equals("") && (valuesFromSliders == false)) {
				hdrAlpha = 0;
				hdrBlurSize = 1;
				midtonesAlpha = 0;
				midtonesBlack = 0;
				midtonesBlurSize = 1;
			}
			Preset filterPreset = chooseProcessing.processingPresets[Presets.getProcessingIndex(chooseProcessing, mode)];
			Filter[] filters = filterPreset.getFilters();
			for (int i = 0; i < filters.length; i++) {
				if (filters[i].getFilterName().equals(FilterFactory.HDR_FILTER2)) {
					if (!effectName.equals("") && (valuesFromSliders == false)) {
						HDRFilter2 filterHDR = (HDRFilter2) filters[i];			
						if (HDRFilter2.ALGORITHM_HDR == filterHDR.getAlgorithm()) {
							hdrAlpha = filterHDR.getAlpha();
							hdrBlurSize = filterHDR.getBlurSize();				
						}
						if (HDRFilter2.ALGORITHM_MIDTONES == filterHDR.getAlgorithm()) {
							midtonesAlpha = filterHDR.getAlpha();
							midtonesBlack = filterHDR.getBlack();
							midtonesBlurSize = filterHDR.getBlurSize();
						}
					}
				} else {
					FilterArrayOtherFilters.add(filters[i]);
				}
			}
		}
		if ((hdrAlpha != 0)) {
			HDRFilter2 filterHDR = new HDRFilter2();
			// filterContrast.setContrast(1);
			//Log.d("AAA", "add hdr filter with hdrAlpha = " + hdrAlpha);
			filterHDR.setAlpha(hdrAlpha);
			filterHDR.setBlurSize(hdrBlurSize);
			filterHDR.setAlgorithm(HDRFilter2.ALGORITHM_HDR);

			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterHDR);

			Filter[] filters = new Filter[FilterArray.size()];
			FilterArray.toArray(filters);

			Preset preset = new Preset();
			preset.setFilters(filters);

			PresetArray.add(preset);
		}

		if ((midtonesAlpha != 0)) {
			HDRFilter2 filterHDR = new HDRFilter2();
			// filterContrast.setContrast(1);
			//Log.d("AAA", "add midtones filter with midtonesAlpha = "
			//		+ midtonesAlpha);
			filterHDR.setAlpha(midtonesAlpha);
			filterHDR.setBlurSize(midtonesBlurSize);
			filterHDR.setBlack(midtonesBlack);
			filterHDR.setAlgorithm(HDRFilter2.ALGORITHM_MIDTONES);

			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterHDR);

			Filter[] filters = new Filter[FilterArray.size()];
			FilterArray.toArray(filters);

			Preset preset = new Preset();
			preset.setFilters(filters);

			PresetArray.add(preset);
		}		
		if (!mode.equals("")) {
			Filter[] filters = new Filter[FilterArrayOtherFilters.size()];
			FilterArrayOtherFilters.toArray(filters);

			Preset preset = new Preset();
			preset.setFilters(filters);

			PresetArray.add(preset);
		}
		return PresetArray;
	}
}
