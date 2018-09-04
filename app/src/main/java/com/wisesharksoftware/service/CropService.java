package com.wisesharksoftware.service;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import java.util.List;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Presets;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.CropFilter;
import com.wisesharksoftware.panels.ButtonPanel.OnLaunchPanelListener;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;
import com.wisesharksoftware.sticker.CropImageView;
import com.wisesharksoftware.sticker.ImageViewDrawableOverlay;
import com.photostudio.photoeditior.R;

public class CropService extends BaseService {
	private ImageViewTouch mImageView;
	private List<String> cropProductIds;
	private CropImageView mCropImageView;

	public CropService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		setChooseProcessing(a);
		setPanelManager(m);
		setAction(action);
		setActionGroup(actionGroup);
		mImageView = (ImageViewDrawableOverlay) a
				.findViewById(R.id.image_overlay);
		mCropImageView = (CropImageView) a.findViewById(R.id.crop_image);
	}

	@Override
	public OnLaunchPanelListener getOnLaunchPanelListener() {

		return new OnLaunchPanelListener() {

			@Override
			public void onLaunchPanelSelected(LauncherItemView item,
					String nameLaunchPanel, List<String> productIds) {

				ServicesManager.instance().setCurrentService(CropService.this);
				String cropName = nameLaunchPanel;
				cropProductIds = productIds;
				chooseProcessing.showCustomToast(nameLaunchPanel);

				Utils.reportFlurryEvent("CropChoosed", cropName);
				Utils.reportFlurryEvent("Action", "Crop");
				Log.d("processing", "CropChoosed" + cropName);
				int processingIndex = Presets.getProcessingIndex(
						chooseProcessing.getApplicationContext(), cropName);

				Preset filterPreset = chooseProcessing.processingPresets[processingIndex];
				Filter filter = filterPreset
						.getFilter(FilterFactory.CROP_FILTER);
				if (filter == null) {
					return;
				}
				enableCrop(((CropFilter) filter).getRatio(),
						((CropFilter) filter).isFixed());

			}
		};
	}

	IOkCancelListener okCancelListener = new IOkCancelListener() {

		@Override
		public void onStop(Object... params) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onOK() {
			applyCrop();

		}

		@Override
		public void onLocked(boolean lock) {
			applyCrop();

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
			disableCrop();
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

	@Override
	public IOkCancelListener getOkCancelListener() {
		return okCancelListener;
	}

	public void enableCrop(double ratio, boolean isFixed) {
		mImageView.setVisibility(View.GONE);
		mCropImageView.setImageBitmap(chooseProcessing.mBitmap, ratio, isFixed);
		mCropImageView.setVisibility(View.VISIBLE);
	}

	public void disableCrop() {
		mImageView.setVisibility(View.VISIBLE);
		mCropImageView.setVisibility(View.GONE);
	}

	public void applyCrop() {
		if ((chooseProcessing != null) && (chooseProcessing.mBitmap != null)) {
			final double w = chooseProcessing.mBitmap.getWidth();
			final double h = chooseProcessing.mBitmap.getHeight();
			Rect cropRect = mCropImageView.getCropRect();
			Filter cropFilter = new CropFilter(cropRect.top / h, cropRect.left / w,
					cropRect.bottom / h, cropRect.right / w);
			chooseProcessing.processOrder
					.addFilter(cropFilter, "CropFilter", false);
			ServicesManager.instance().addToQueue(self());
			chooseProcessing.processImage();
		}
	}
}
