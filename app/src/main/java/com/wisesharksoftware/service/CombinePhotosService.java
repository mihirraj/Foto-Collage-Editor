package com.wisesharksoftware.service;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.widget.SeekBar;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.BlendFilter;
import com.wisesharksoftware.core.filters.BlendFilter.Algorithm;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.ButtonPanel.OnLaunchPanelListener;
import com.wisesharksoftware.panels.IPanel;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.SliderPanel;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;
import com.wisesharksoftware.sticker.DrawableHighlightView;

public class CombinePhotosService extends BaseService {

	private String effectName = "";
	private LauncherItemView effectItem = null;
	private int alpha = -1;
	
	public static int ALGORITHM_SCREEN = 0;
	public static int ALGORITHM_MULTIPLY = 1;
	public static int ALGORITHM_TRANSPARENCY_ALPHA = 3;
	public static int ALGORITHM_COLOR_DODGE = 4;
	public static int ALGORITHM_OVERLAY = 5;
	public static int ALGORITHM_GETFIRST = 6;

	public CombinePhotosService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		setChooseProcessing(a);
		setPanelManager(m);
		setAction(action);
		setActionGroup(actionGroup);
	}

	public String getEffectName() {
		return effectName;
	}

	public LauncherItemView getEffectItem() {
		return effectItem;
	}

	public void setEffectItem(LauncherItemView effectItem) {
		if (this.effectItem != null) {
			this.effectItem.setState(false,false);
		}
		effectItem.setState(true,false);
		this.effectItem = effectItem;
	}

	public void setEffectName(String effectName) {
		this.effectName = effectName;
	}

	public boolean hasEffect() {
		if (effectName == null || effectName.equals("")) {
			return false;
		}
		return true;
	}

	@Override
	public DrawableHighlightView getHighlightView() {
		return null;
	}

	@Override
	public void setDrawableHighlightView(DrawableHighlightView view) {

	}

	@Override
	public String getName() {
		return "EffectsService";
	}

	@Override
	public IOkCancelListener getOkCancelListener() {
		return new IOkCancelListener() {

			@Override
			public void onStop(Object... params) {
				/*
				 * if (new_values) { seekBar.setProgress(255 - effectsAlpha);
				 * Log.d("processing", "effectsAlpha cancel " + effectsAlpha);
				 * return; }
				 */
				SeekBar seekBar = (SeekBar) params[0];
				int oldvalue = alpha;
				alpha = 255 - seekBar.getProgress();
				if (oldvalue != alpha) {
					chooseProcessing.showCustomToast(effectName, true);
					Utils.reportFlurryEvent("effectsAlpha changed",
							"effectsAlpha = " + alpha);
					Utils.reportFlurryEvent("Action", "EffectAlpha");
					Log.d("processing", "effectsAlpha changed " + alpha);
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.combineImages(getAlgorithm(), getAlpha());
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
				panelManager.upLevel();

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
	public OnLaunchPanelListener getOnLaunchPanelListener() {
		return new OnLaunchPanelListener() {

			@Override
			public void onLaunchPanelSelected(LauncherItemView item,
					String nameLaunchPanel, List<String> productIds) {
				ServicesManager.instance().setCurrentService(
						CombinePhotosService.this);
				chooseProcessing.hideGrid();
				// Log.d("AAA", "nameLaunchPanel = " + nameLaunchPanel);
				/*
				 * if (new_values) { Log.d("processing", "Effects cancel " +
				 * effectName); return; }
				 */
				effectName = nameLaunchPanel;
				Log.d("ITEM",item+"");
				if (item != null) {
					setEffectName((String) item.getTag());
					setEffectItem(item);
					alpha = -1;
				} else {
					setEffectName(nameLaunchPanel);
				}
				chooseProcessing.showCustomToast(nameLaunchPanel, true);
				for (int i = 0; i < panelManager.panels.size(); i++) {
					IPanel panel = panelManager.panels.get(i);
					String panelName = panel.getPanelName();
					String panelType = panel.getPanelType();
					if (panelType.equals(PanelManager.SLIDER_PANEL_TYPE)
							&& panelName.equals("Effect_Alpha")) {
						((SliderPanel) panel).setProgress(255);

					}
				}
				ServicesManager.instance().addToQueue(self());
				chooseProcessing.combineImages(getAlgorithm(), getAlpha());
			}
		};
	}


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
						chooseProcessing.combineImages(getAlgorithm(), getAlpha());
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

	private int getAlgorithm() {
		if (effectName.equals("ALGORITHM_SCREEN")) {
			return ALGORITHM_SCREEN;
		}
		if (effectName.equals("ALGORITHM_MULTIPLY")) {
			return ALGORITHM_MULTIPLY;
		}
		if (effectName.equals("ALGORITHM_OVERLAY")) {
			return ALGORITHM_OVERLAY;
		}
		if (effectName.equals("ALGORITHM_COLOR_DODGE")) {
			return ALGORITHM_COLOR_DODGE;
		}		
		if (effectName.startsWith("ALGORITHM_TRANSPARENCY_ALPHA")) {
			return ALGORITHM_TRANSPARENCY_ALPHA;
		}
		if (effectName.equals("ALGORITHM_GETFIRST")) {
			return ALGORITHM_GETFIRST;
		}		
		
		return ALGORITHM_SCREEN;
	}
	
	private int getAlpha() {
		if (effectName.equals("ALGORITHM_TRANSPARENCY_ALPHA20")) {
			return 20;
		}
		if (effectName.equals("ALGORITHM_TRANSPARENCY_ALPHA50")) {
			return 50;
		}
		if (effectName.equals("ALGORITHM_TRANSPARENCY_ALPHA80")) {
			return 80;
		}
		
		return 0;
	}
	
	@Override
	public ArrayList<Preset> getFilterPreset() {

		ArrayList<Preset> PresetArray = new ArrayList<Preset>();
//		if (alpha != -1) {
//			SaveImageFilter filterSave = new SaveImageFilter();
//			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
//			FilterArray.add(filterSave);
//
//			Filter[] filters = new Filter[FilterArray.size()];
//			FilterArray.toArray(filters);
//
//			Preset preset = new Preset();
//			preset.setFilters(filters);
//
//			PresetArray.add(preset);
//		}
//		Preset filterPreset = chooseProcessing.processingPresets[Presets
//				.getProcessingIndex(chooseProcessing, getEffectName())];
//		if (filterPreset != null) {
//			PresetArray.add(filterPreset);
//		}
		String origTempFile = Utils.getFolderPath(chooseProcessing.getString(R.string.photoFolder)) + "/temp_orig.jpg";
		
		if (alpha != -1 && hasEffect()) {			
			BlendFilter filterBlend = new BlendFilter();
			filterBlend.setAlgorithm(Algorithm.transparency_alpha);
			//filterBlend.setBlend_with_image_memory(true);
			filterBlend.setAlpha(alpha);
			filterBlend.setBlendSrc(origTempFile);
			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterBlend);

			Filter[] filters = new Filter[FilterArray.size()];
			FilterArray.toArray(filters);

			Preset preset = new Preset();
			preset.setFilters(filters);

			PresetArray.add(preset);
		}

		return PresetArray;
	}
	
	@Override
	public void clear() {
		effectName = "";
		effectItem = null;
		alpha = -1;
	}
}
