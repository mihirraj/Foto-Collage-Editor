package com.wisesharksoftware.service;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.ContrastBrightnessFilter;
import com.wisesharksoftware.core.filters.FaceDetectionFilter;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.ButtonPanel.OnLaunchPanelListener;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;

public class FaceService extends BaseService {
	private int faceTemperature = 0;
	private int smoothSkinAlpha = 0;
	private int faceBrightness = 0;
	private double whitenEyes = 0;
	private int balanseFaceColorAlpha = 0;
	private int unsharpEyeAlpha = 0;
	private int whitenTeeth = 0;

	public FaceService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		setChooseProcessing(a);
		setPanelManager(m);
		setAction(action);
		setActionGroup(actionGroup);
	}

	@Override
	public OnLaunchPanelListener getOnLaunchPanelListener() {
		return new OnLaunchPanelListener() {

			@Override
			public void onLaunchPanelSelected(LauncherItemView item,
					String nameLaunchPanel, List<String> productIds) {
				ServicesManager.instance().setCurrentService(self());

			}
		};
	}

	IOkCancelListener okCancelListener = new IOkCancelListener() {

		@Override
		public void onStop(Object... params) {

			SeekBar seekBar = (SeekBar) params[0];
			String panelName = (String) params[1];

			if (panelName.equals("FaceTemperature")) {
				/*
				 * if (new_values) { seekBar.setProgress(faceTemperature + 10);
				 * Log.d("processing", "FaceTemperature cancel " +
				 * faceTemperature); return; }
				 */
				int oldvalue = faceTemperature;
				faceTemperature = (int) Math.round(seekBar.getProgress()) - 10;
				if (oldvalue != faceTemperature) {
					chooseProcessing.showCustomToast(panelName, true);
					Utils.reportFlurryEvent("FaceTemperature changed",
							"faceTemperature = " + faceTemperature);
					Utils.reportFlurryEvent("Action", "FaceTemperature");
					Log.d("processing", "FaceTemperature changed "
							+ faceTemperature);
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
			} else if (panelName.equals("SkinSmooth")) {

				// brightness = seekBar.getProgress() - 50;
				/*
				 * if (new_values) { // seekBar.setProgress(sharpness + 5);
				 * Log.d("processing", "SmoothSkin cancel " + smoothSkinAlpha);
				 * return; }
				 */
				double oldvalue = smoothSkinAlpha;
				smoothSkinAlpha = seekBar.getProgress();
				if (Math.abs(oldvalue - smoothSkinAlpha) > 0) {
					chooseProcessing.showCustomToast(panelName, true);
					Utils.reportFlurryEvent("smoothSkinAlpha changed",
							"smoothSkinAlpha = " + smoothSkinAlpha);
					Utils.reportFlurryEvent("Action", "SkinSmooth");
					Log.d("processing", "smoothSkinAlpha changed "
							+ smoothSkinAlpha);
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
			} else if (panelName.equals("FaceBrightness")) {
				/*
				 * if (new_values) { // seekBar.setProgress(sharpness + 5);
				 * Log.d("processing", "FaceBrightness cancel " +
				 * faceBrightness); return; }
				 */
				int oldvalue = faceBrightness;
				faceBrightness = seekBar.getProgress();
				if (Math.abs(oldvalue - faceBrightness) > 0) {
					chooseProcessing.showCustomToast(panelName, true);
					Utils.reportFlurryEvent("faceBrightness changed",
							"faceBrightness = " + faceBrightness);
					Utils.reportFlurryEvent("Action", "FaceBrightness");
					Log.d("processing", "faceBrightness changed "
							+ faceBrightness);
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
			} else if (panelName.equals("WhitenTeeth")) {
				// brightness = seekBar.getProgress() - 50;
				/*
				 * if (new_values) { // seekBar.setProgress(sharpness + 5);
				 * Log.d("processing", "WhiteTeeth cancel " + whitenTeeth);
				 * return; }
				 */
				double oldvalue = whitenTeeth;
				whitenTeeth = seekBar.getProgress();
				if (Math.abs(oldvalue - whitenTeeth) > 0.00001) {
					chooseProcessing.showCustomToast(panelName, true);
					Utils.reportFlurryEvent("WhitenTeeth changed",
							"WhitenTeeth = " + whitenTeeth);
					Utils.reportFlurryEvent("Action", "WhitenTeeth");
					Log.d("processing", "WhitenTeeth changed " + whitenTeeth);
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
			} else if (panelName.equals("WhitenEyes")) {
				/*
				 * if (new_values) { // seekBar.setProgress(sharpness + 5);
				 * Log.d("processing", "Whiteness eyes cancel " + whitenEyes);
				 * return; }
				 */
				double oldvalue = whitenEyes;
				whitenEyes = (seekBar.getProgress() / 10.0) * 2;
				if (Math.abs(oldvalue - whitenEyes) > 0.00001) {
					chooseProcessing.showCustomToast(panelName, true);
					Utils.reportFlurryEvent("WhitenEyes changed",
							"WhitenEys = " + whitenEyes);
					Utils.reportFlurryEvent("Action", "WhitenEyes");
					Log.d("processing", "WhitenEyes changed " + whitenEyes);
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
			} else if (panelName.equals("UnsharpEye")) {
				/*
				 * // brightness = seekBar.getProgress() - 50; if (new_values) {
				 * // seekBar.setProgress(sharpness + 5); Log.d("processing",
				 * "unsharpEyeAlpha cancel " + unsharpEyeAlpha); return; }
				 */
				double oldvalue = unsharpEyeAlpha;
				unsharpEyeAlpha = seekBar.getProgress();
				if (Math.abs(oldvalue - unsharpEyeAlpha) > 0) {
					chooseProcessing.showCustomToast(panelName, true);
					Utils.reportFlurryEvent("unsharpEyeAlpha changed",
							"unsharpEyeAlpha = " + unsharpEyeAlpha);
					Utils.reportFlurryEvent("Action", "UnsharpEye");
					Log.d("processing", "unsharpEyeAlpha changed "
							+ unsharpEyeAlpha);
					ServicesManager.instance().addToQueue(self());
					chooseProcessing.processImage();
				}
			} else if (panelName.equals("SmoothCurve")) {
				/*
				 * if (new_values) { // seekBar.setProgress(sharpness + 5);
				 * Log.d("processing", "smoothCorrectCurveAlpha cancel " +
				 * smoothSkinAlpha); return; }
				 */
				double oldvalue = balanseFaceColorAlpha;
				balanseFaceColorAlpha = seekBar.getProgress();
				if (Math.abs(oldvalue - balanseFaceColorAlpha) > 0) {
					chooseProcessing.showCustomToast(panelName, true);
					Utils.reportFlurryEvent("smoothCorrectCurveAlpha changed",
							"smoothCorrectCurveAlpha = "
									+ balanseFaceColorAlpha);
					Utils.reportFlurryEvent("Action", "SmoothCurve");
					Log.d("processing", "smoothCorrectCurveAlpha changed "
							+ balanseFaceColorAlpha);
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
		public void onRestore() {
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

	@Override
	public IOkCancelListener getOkCancelListener() {
		return okCancelListener;
	}

	@Override
	public void clear() {
		faceTemperature = 0;
		smoothSkinAlpha = 0;
		faceBrightness = 0;
		whitenEyes = 0;
		balanseFaceColorAlpha = 0;
		unsharpEyeAlpha = 0;
		whitenTeeth = 0;

	}

	@Override
	public ArrayList<Preset> getFilterPreset() {
		if ((Math.abs(whitenEyes) > 0.001) || (Math.abs(whitenTeeth) > 0.001)
				|| (smoothSkinAlpha != 0) || (faceBrightness != 0)
				|| (balanseFaceColorAlpha != 0) || (unsharpEyeAlpha != 0)
				|| (faceTemperature != 0)) {
			FaceDetectionFilter filterWhitenEyes = new FaceDetectionFilter();
			// filterContrast.setContrast(1);
			//Log.d("AAA", "add whitenEyes filter with max_coef = " + whitenEyes
			//		+ " and smoothSkinAlpha = " + smoothSkinAlpha);
			filterWhitenEyes.setWhitenEyeMaxCoef(whitenEyes);
			filterWhitenEyes.setWhitenTeethMaxCoef(whitenTeeth);
			filterWhitenEyes.setSmoothSkinAlpha(smoothSkinAlpha);
			filterWhitenEyes.setUnsharpEyeAlpha(unsharpEyeAlpha);
			filterWhitenEyes.setBalanseFaceColorAlpha(balanseFaceColorAlpha);
			filterWhitenEyes.setBrightness(faceBrightness);
			filterWhitenEyes.setTemperature(faceTemperature);
			filterWhitenEyes.setPreview(chooseProcessing.processPreview);
			ArrayList<Preset> PresetArray = new ArrayList<Preset>();
			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterWhitenEyes);

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
