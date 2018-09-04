package com.wisesharksoftware.panels;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.flurry.android.FlurryAgent;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Image2;
import com.wisesharksoftware.core.ImageProcessing;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Presets;
import com.wisesharksoftware.core.ProcessingCallback;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.BlendFilter;
import com.wisesharksoftware.core.filters.BlendFilter.Algorithm;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ThumbnailGenerator {
	private static final String LOG_TAG = "ThumbnailGenerator";
	private Context context;
	private Preset[] processingPresets;
	private List<ImageProcessing> processings = new ArrayList<ImageProcessing>();
	private List<String> inFiles = new ArrayList<String>();
	private List<String> outFiles = new ArrayList<String>();
	private OnThumbnailGenerated onThumbnailGenerated;

	public interface OnThumbnailGenerated {
		public void onThumbnailGenerated();
	}

	public void setOnThumbnailGenerated(OnThumbnailGenerated onThumnailGenerated) {
		this.onThumbnailGenerated = onThumnailGenerated;
	}

	public ThumbnailGenerator(Context context) {
		this.context = context;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	public void generate() {
		try {
			processingPresets = Presets.getPresets(context, true).getProcessingPresets();
			Structure structure = parseStructure();
			if (structure != null) {
				for (int i = structure.getPanelsInfo().size() - 1; i >= 0; i--) {
					PanelInfo panelInfo = structure.getPanelsInfo().get(i);
					if (panelInfo.getType().equals(PanelManager.BUTTON_PANEL_TYPE)) {
	
						if (panelInfo.getGenerateThumbnail()) {
	
							String inFile = context.getExternalFilesDir(null) + "/assets/" + "sd/batch/" + panelInfo.getThumbnailSrc();
							String size = panelInfo.getThumbnailSize();
							String[] sizes = size.split("x");
							int width = Integer.parseInt(sizes[0]);
							int height = Integer.parseInt(sizes[1]);
	
							List<Item> items = panelInfo.getItems();
							for (int j = 0; j < items.size(); j++) {
								String filterName = items.get(j).getName();
								{
									String outFile = context.getExternalFilesDir(null) + "/assets/sd/batch/" + items.get(j).getImageOnResourceName() + ".jpg";
									processings.add(createProcessing(filterName, panelInfo.getThumbnailBlendOn(), width, height));
									inFiles.add(inFile);
									outFiles.add(outFile);
								}
								{
									String outFile = context.getExternalFilesDir(null) + "/assets/sd/batch/" + items.get(j).getImageOffResourceName() + ".jpg";
									processings.add(createProcessing(filterName, panelInfo.getThumbnailBlendOff(), width, height));
									inFiles.add(inFile);
									outFiles.add(outFile);
								}
							}
						}
					}
				}
				startNextProcessing();
			}
		} 
		catch (Exception e) {
			//silently close
			FlurryAgent.logEvent("ThumbnailGenerator Error");			
		}
	}

	private Structure parseStructure() {
		Structure structure = null;
		String s;
		InputStream inputStream = null;
		try {
			int id = context.getResources().getIdentifier("structure_json", "raw", context.getPackageName());
			inputStream = context.getResources().openRawResource(id);
			byte[] reader = new byte[inputStream.available()];
			while (inputStream.read(reader) != -1) {
			}
			s = new String(reader);
			StructureParser parser = new StructureParser();
			try {
				structure = parser.parse(s);
				Log.d(LOG_TAG, "structure" + structure.toString());
			} catch (JSONException e) {
				Log.d(LOG_TAG, "parser structure error");
				e.printStackTrace();
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(LOG_TAG, e.getMessage());
				}
			}
		}
		return structure;
	}

	private String createJSONPreset(String filterName, String thumbnailBlend) {
		ArrayList<Filter> FilterArray;
		Filter[] filters;
		ArrayList<Preset> PresetArray;
		Preset[] presets;
		Preset filterPreset;

		PresetArray = new ArrayList<Preset>();
		// add filter
		filterPreset = null;
		if (!filterName.equals("")) {
			int processingIndex = Presets.getProcessingIndex(context, filterName);
			filterPreset = processingPresets[processingIndex];
		}
		if (filterPreset != null) {
			PresetArray.add(filterPreset);
		}

		// add Blend
		if (!thumbnailBlend.equals("")) {
			BlendFilter filterBlend = new BlendFilter();
			filterBlend.setAlgorithm(Algorithm.transparency);
			filterBlend.setBlendSrc(thumbnailBlend);
			FilterArray = new ArrayList<Filter>();
			FilterArray.add(filterBlend);

			filters = new Filter[FilterArray.size()];
			FilterArray.toArray(filters);

			Preset preset = new Preset();
			preset.setFilters(filters);

			PresetArray.add(preset);
		}

		presets = new Preset[PresetArray.size()];
		PresetArray.toArray(presets);

		Presets effectsPreset = new Presets(null, presets, null);

		String presetsJson = effectsPreset.convertToJSON();
		Log.d("processing", presetsJson);

		return presetsJson;
	}

	private void startNextProcessing() {
		boolean done = true;
		for (int i = 0; i < processings.size(); i++) {
			if (processings.get(i).tagDone == false) {
				done = false;
				processImage(inFiles.get(i), outFiles.get(i), processings.get(i));
				break;
			}
		}
		if ((done == true) && (onThumbnailGenerated != null)) {
			onThumbnailGenerated.onThumbnailGenerated();
		}
	}

	private ImageProcessing createProcessing(String filterName, String thumbnailBlend, int width, int height) {
		return new ImageProcessing(context, createJSONPreset(filterName, thumbnailBlend), width, height, new ProcessingCallback() {

			@Override
			public void onSuccess(String outFileName) {
				startNextProcessing();
			}

			@Override
			public void onStart() {

			}

			@Override
			public void onFail(Throwable e) {
			}

			@Override
			public void onBitmapCreatedOpenCV() {
			}

			@Override
			public void onBitmapCreated(Image2 bitMap) {
			}

			@Override
			public void onBitmapCreated(Bitmap bitMap) {
			}

			@Override
			public void onCancelled() {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private void processImage(String inFile, final String outFile, ImageProcessing processing) {
		List<String> files = new ArrayList<String>();
		files.add(inFile);
		processing.processPictureAsync(files, outFile);
	}

}
