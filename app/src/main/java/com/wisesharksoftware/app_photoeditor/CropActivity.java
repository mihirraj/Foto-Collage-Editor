package com.wisesharksoftware.app_photoeditor;

import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.MarketingHelper;
import com.smsbackupandroid.lib.SettingsHelper;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Image2;
import com.wisesharksoftware.core.ImageProcessing;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Presets;
import com.wisesharksoftware.core.ProcessingCallback;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.CropFilter;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.sticker.CropImageView;
import com.wisesharksoftware.ui.BaseActivity;

public class CropActivity extends BaseActivity {
	public static final String INTENT_PARAM_URIS = "uris";
	private ImageView back;
	private ImageView next;
	private ArrayList<String> originalFileNames = new ArrayList<String>();
	private ProgressBar bar;
	private AdView adView;
	private CropImageView mCropImageView;
	public Bitmap mBitmap;
	private CropFilter cropFilter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		initData(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		initData(intent);
		
		super.onNewIntent(intent);
	}

	private void initViews() {
		back = (ImageView) findViewById(R.id.activity_document_touch_back);
		next = (ImageView) findViewById(R.id.activity_document_touch_next);
		bar = (ProgressBar) findViewById(R.id.activity_document_touch_bar);
		mCropImageView = (CropImageView) findViewById(R.id.crop_image);
		mCropImageView.setDoubleTapEnabled(false);
		mCropImageView.setScaleEnabled(false);
		mCropImageView.setScrollEnabled(false);
		mCropImageView.setDisplayType(DisplayType.FIT_TO_SCREEN);
		mCropImageView.setShowAnimalEyes(true);
		mCropImageView.setDragByEdge(false);
		cropFilter = new CropFilter();
		cropFilter.setRatio(4);
		cropFilter.setFixed(true);
		
		adView = (AdView) findViewById(R.id.adView);

		if (!IsAdsHidden()) {
			adView.setVisibility(View.GONE);
			AdRequest adRequest = new AdRequest.Builder().build();
		    adView.loadAd(adRequest);
		}
		
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(self(), CameraPreviewActivity.class);
				startActivity(intent);
				finish();
			}
		});

		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				cropImage(originalFileNames.get(0), originalFileNames.get(0));				
			}
		});
	}

	private Context self() {
		return this;
	}	
	
	public void enableCrop(double ratio, boolean isFixed) {
		//mImageView.setVisibility(View.GONE);
		mCropImageView.setImageBitmap(mBitmap, ratio, isFixed);
		mCropImageView.setVisibility(View.VISIBLE);
	}
	
	private void initData(Intent intent) {
		originalFileNames.clear();

		Parcelable[] imgUris = intent
				.getParcelableArrayExtra(INTENT_PARAM_URIS);
		String[] proj = { MediaStore.Images.Media.DATA };
		for (Parcelable uri : imgUris) {
			Cursor cursor = managedQuery((Uri) uri, proj, null, null, null);
			if (cursor != null) {
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				originalFileNames.add(cursor.getString(column_index));

			} else {
				originalFileNames.add(((Uri) uri).getPath());

			}
		}

		createAndConfigurePreview(originalFileNames.get(0));
		enableCrop(cropFilter.getRatio(), cropFilter.isFixed());	
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, CameraPreviewActivity.class);
		startActivity(intent);
		finish();
	}

	public boolean IsAdsHidden() {
		if (getResources().getBoolean(R.bool.show_ads) == false) {
			return true;
		}
		return isFullVersion()
				|| SettingsHelper.getBoolean(this, "remove_ads", false)
				|| MarketingHelper.isTrialPeriod(this);
	}

	protected boolean isFullVersion() {
		return getPackageName().contains("full");
	}
	
	public int getProcImageViewHeight() {
		return getWindowManager().getDefaultDisplay().getHeight();
	}

	public int getProcImageViewWidth() {
		return getWindowManager().getDefaultDisplay().getWidth();
	}
	
	private void createAndConfigurePreview(String fileName) {
		try {

			if (mBitmap != null && !mBitmap.isRecycled()) {
				mBitmap.recycle();
			}
			int realWidth = mCropImageView.getWidth();
			int realHeight = mCropImageView.getHeight();

			int width = realWidth > 0 ? realWidth : getProcImageViewWidth();
			int height = realHeight > 0 ? realHeight : getProcImageViewHeight();			

			mBitmap = Utils.getThumbnailFromPath(fileName, width, height);
		} catch (Exception e) {
			e.printStackTrace();
			FlurryAgent.logEvent("CreateAndConfigurePreviewError");
			new ExceptionHandler(e, "CreateAndConfigurePreviewError");
		}
	}

	private String createCropJSONPreset() {
		ArrayList<Filter> FilterArray = null;
		Filter[] filters = null;
		ArrayList<Preset> PresetArray;
		Preset[] presets;

		PresetArray = new ArrayList<Preset>();

		final double w = mBitmap.getWidth();
		final double h = mBitmap.getHeight();
		Rect cropRect = mCropImageView.getCropRect();
		Filter cropFilter = new CropFilter(cropRect.top / h, cropRect.left / w,
				cropRect.bottom / h, cropRect.right / w);
	
		FilterArray = new ArrayList<Filter>();
		FilterArray.add(cropFilter);

		filters = new Filter[FilterArray.size()];
		FilterArray.toArray(filters);

		Preset preset = new Preset();
		preset.setFilters(filters);

		PresetArray.add(preset);

		presets = new Preset[PresetArray.size()];
		PresetArray.toArray(presets);

		Presets effectsPreset = new Presets(null, presets, null);

		String presetsJson = effectsPreset.convertToJSON();
		Log.d("processing", presetsJson);

		return presetsJson;
	}
	
	private void cropImage(final String inFile, final String outFile) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(inFile, options);
		int width = options.outWidth;
		int height = options.outHeight;

		ImageProcessing processing = new ImageProcessing(
				getApplicationContext(), createCropJSONPreset(), width,
				height, new ProcessingCallback() {

					@Override
					public void onSuccess(String outFileName) {
						Log.d("processImage", "onSuccess");
//						File oldFile = new File(inFile);
//						oldFile.delete();
						unlockScreen();
						Intent intent = new Intent(self(), ChooseProcessingActivity.class);
						List<Uri> a = new ArrayList<Uri>();
						a.add(Uri.parse(outFile));
						intent.putExtra(ChooseProcessingActivity.INTENT_PARAM_URIS, a.toArray(new Uri[1]));
						startActivity(intent);
						finish();
					}

					@Override
					public void onStart() {
						lockScreen();
					}

					@Override
					public void onFail(Throwable e) {
						unlockScreen();
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
		List<String> inFiles = new ArrayList<String>();
		inFiles.add(inFile);
		processing.processPictureAsync(inFiles, outFile);
	}

	private void lockScreen() {
		bar.setVisibility(View.VISIBLE);
	}

	private void unlockScreen() {
		bar.setVisibility(View.INVISIBLE);
	}
	
	public void onDestroy() {
		try {
			if (mBitmap != null && !mBitmap.isRecycled()) {
				mBitmap.recycle();
				mBitmap = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "ChooseSticker::onDestroyError");
		}
		super.onDestroy();
	}

	@Override
	protected int getRootLayout() {
		return R.layout.activity_crop;
	}

	@Override
	protected int getPortraitLayout() {
		return R.layout.activity_crop;
	}

	@Override
	protected int getLandscapeLayout() {
		return R.layout.activity_crop;
	}

	@Override
	protected String getFlurryKey() {
		return getString(R.string.flurryApiKey);
	}
}
