package com.wisesharksoftware.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.smsbackupandroid.lib.ExceptionHandler;

public class Presets implements Serializable {
	private static final long serialVersionUID = 1L;

	private Preset[] cameraPresets;
	private Preset[] processingPresets;
	private Preset watermarkPreset;

	private static Presets presets;

	public Presets(Preset[] cameraPresets, Preset[] processingPresets,
			Preset watermakPreset) {
		this.cameraPresets = cameraPresets;
		this.processingPresets = processingPresets;
		this.watermarkPreset = watermakPreset;
	}

	public Preset[] getCameraPresets() {
		return cameraPresets;
	}

	public Preset[] getProcessingPresets() {
		return processingPresets;
	}

	public Preset getWatermarkPreset() {
		return watermarkPreset;
	}

	public static Presets getPresets(Context context) {
		return getPresets(context, false);
	}

//	@TargetApi(Build.VERSION_CODES.FROYO)
//	public static Presets getPresets(Context context, boolean needToReload) {
//		PresetParser parser = new PresetParser(FilterFactory.getInstance());
//		if ((presets == null) || (needToReload == true)) {
//			try {
//				presets = null;
//                //String presetJson = Utils.getStringAsset( context, "preset.json" );
//
//				File dir = new File(context.getExternalFilesDir(null)
//						+ "/assets/sd/presets/");
//				File[] list = dir.listFiles();
//				for (int i = 0; i < list.length; i++) {
//					if (!list[i].getName().toString().startsWith("preset")) {
//						continue;
//					}
//					Log.d("AAA", "load json with name " + list[i].toString());
//
//					FileInputStream fis;
//					fis = null;
//					try {
//						fis = new FileInputStream(list[i].toString());
//					} catch (FileNotFoundException e) {
//						Log.d("AAA", "File Not Found Exception");
//						new ExceptionHandler(e, "PresetNotFound");
//					}
//					int size = fis.available();
//					byte[] buffer = new byte[size];
//					fis.read(buffer);
//					fis.close();
//
//					String presetJson = new String(buffer);
//					if (presets == null) {
//						Log.d("AAA", "create presets");
//						presets = parser.parse(presetJson);
//					} else {
//						Log.d("AAA", "add presets");
//						presets.addPreset(parser.parse(presetJson));
//					}
//					Log.d("AAA",
//							"presets length = "
//									+ presets.getProcessingPresets().length);
//				}
//				return presets;
//			} catch (Exception e) {
//				Log.d("AAA", "preset parsing error!!!");
//				e.printStackTrace();
//				new ExceptionHandler(e, "PresetParsing");
//				return presets;
//			}
//		} else {
//			return presets;
//		}
//	}

	public static Presets getPresets(Context context, boolean needToReload) {
		PresetParser parser = new PresetParser(FilterFactory.getInstance());
		if ((presets == null) || (needToReload == true)) {
			try {
				presets = null;
                //String presetJson = Utils.getStringAsset( context, "preset.json" );
				AssetManager assetManager = context.getAssets();
				String[] files = assetManager.list("sd/presets");
				if (files == null || files.length <= 0) {
					FlurryAgent.logEvent("PresetsListEmpty");
					return presets;
				}
				for (int i = 0; i < files.length; i++) {
					if (!files[i].startsWith("preset")) {
						continue;
					}
					//Log.d("AAA", "load json with name " + files[i]);

					String presetJson = Utils.getStringAsset(context, "sd/presets/" + files[i]);
					if (presets == null) {
						//Log.d("AAA", "create presets");
						presets = parser.parse(presetJson);
					} else {
						//Log.d("AAA", "add presets");
						presets.addPreset(parser.parse(presetJson));
					}
//					Log.d("AAA",
//							"presets length = "
//									+ presets.getProcessingPresets().length);
				}
				return presets;
			} catch (Exception e) {
				Log.d("AAA", "preset parsing error!!!");
				e.printStackTrace();
				FlurryAgent.logEvent("PresetParsingError");
				new ExceptionHandler(e, "PresetParsing");
				return presets;
			}
		} else {
			return presets;
		}
	}

	public void addPreset(Presets presets) {
		if (presets == null) {
			//Log.d("AAA", "presets == null");
			return;
		}
//		Log.d("AAA",
//				"presets != null cameraPresetsCount = "
//						+ presets.getCameraPresets().length
//						+ " processingPresetsCount = "
//						+ presets.getProcessingPresets().length);
		ArrayList<Preset> cameraList = new ArrayList<Preset>();
		for (int i = 0; i < cameraPresets.length; i++) {
			cameraList.add(cameraPresets[i]);
		}
		for (int i = 0; i < presets.getCameraPresets().length; i++) {
			cameraList.add(presets.getCameraPresets()[i]);
		}
		cameraPresets = cameraList.toArray(new Preset[cameraList.size()]);

		ArrayList<Preset> processingList = new ArrayList<Preset>();
		for (int i = 0; i < processingPresets.length; i++) {
			//Log.d("AAA", "Resave Preset");
			processingList.add(processingPresets[i]);
		}
		for (int i = 0; i < presets.getProcessingPresets().length; i++) {
			//Log.d("AAA", "Add Preset");
			processingList.add(presets.getProcessingPresets()[i]);
		}
		processingPresets = processingList.toArray(new Preset[processingList
				.size()]);
	}

	public static int getProcessingIndex(Context context, String id) {
		Preset[] presets = getPresets(context).getProcessingPresets();
		int i = 0;
		for (Preset preset : presets) {
			if (preset.getName().equals(id)) {
				return i;
			}
			++i;
		}
		return 0;
	}

	public String convertToJSON() {
		String s = "{";
		// start cameraPresetArray

		// start processingPreset array
		if (cameraPresets != null) {
			s += "\"cameras\":" + "[";
			for (int i = 0; i < cameraPresets.length; i++) {
				s += cameraPresets[i].convertToJSON();
				if (i != cameraPresets.length - 1) {
					s += ",";
				}
			}
			s += "],";
		}

		// cameraPresets
		/*
		 * "cameras":[ { "name":"Normal", "imageResourceName":"cam_fish",
		 * "nameResourceName":"name_fish",
		 * "contrastResourceName":"contrast_normal",
		 * "scratchesResourceName":"scratches_no",
		 * "vignetteResourceName":"vignette_no",
		 * "headerImageResourceNamePortrait":"panel_top",
		 * "headerImageResourceNameLandscape":"panel_top",
		 * "footerBackgroundColor":0, "square":true, "filters":[ {
		 * "type":"normal" } ] } ],
		 */

		// start processingPreset array
		s += "\"processings\":" + "[";
		for (int i = 0; i < processingPresets.length; i++) {
			s += processingPresets[i].convertToJSON();
			if (i != processingPresets.length - 1) {
				s += ",";
			}
		}
		s += "]";
		// end presetProcessing array
		s += "}";
		return s;
	}
}
