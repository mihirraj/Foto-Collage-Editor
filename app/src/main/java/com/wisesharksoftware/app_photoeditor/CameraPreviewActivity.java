package com.wisesharksoftware.app_photoeditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smsbackupandroid.lib.MarketingHelper;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.camera.AppSettings;
import com.wisesharksoftware.camera.BaseCameraPreviewActivity;
import com.wisesharksoftware.camera.SettingsPaneViewController.FlashState;
import com.wisesharksoftware.core.ActionCallback;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Presets;
import com.wisesharksoftware.core.Utils;

import com.wisesharksoftware.photoeditor.ApplicationClass;
import com.wisesharksoftware.ui.FlashGatesAnimator;
import com.wisesharksoftware.ui.GatesAnimator;

public class CameraPreviewActivity extends BaseCameraPreviewActivity {
	private Preset cameraPreset;
	private String documentPath;
	private boolean use_camera_gates = true;
	private TextView textCounter;

	private ScaleAnimation scale1 = new ScaleAnimation(0f, 1f, 0f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
	private ScaleAnimation scale2 = new ScaleAnimation(0f, 1f, 0f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
	private ScaleAnimation scale3 = new ScaleAnimation(0f, 1f, 0f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
	private ImageButton btnPhotoTimer; 

	@Override
	public void onResume() {
		//Utils.reportFlurryEvent("DeviceId",
		//		((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
		//				.getDeviceId());
		Utils.reportFlurryEvent("onResume", this.toString());
		super.onResume();
		if (square_ || (picturesCount_ > 1 && vertical_)) {
			mPreview.setOnPreviewChanged(new ActionCallback<Void>() {
				@Override
				public void onAction(Void v) {
					if (!squareOverlayViewController_.isBuild())
						squareOverlayViewController_.drawSquareFrame(mPreview);
					View view = findViewById(R.id.adView);
					View view2 = findViewById(R.id.button_wrapper);
					if (view != null) {
						view.bringToFront();
					}
					if (view2 != null) {
						view2.bringToFront();
					}
					// squareOverlayViewController_.invalidate();

				}
			});
		}
		if (btnPhotoTimer != null) {
			btnPhotoTimer.setEnabled(true);
		}
		if (getPhotoButton() != null) {
			getPhotoButton().setEnabled(true);
		}
	}

	@Override
	public void onPause() {
		Utils.reportFlurryEvent("onPause", this.toString());
		super.onPause();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onStart() {
		super.onStart();
		View view = findViewById(R.id.adView);
		if (view != null) {
			if (IsAdsHidden()) {
				view.setVisibility(View.GONE);
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
				}
				/*AdView adView = (AdView) view;
				AdRequest adRequest = new AdRequest.Builder().build();
			    adView.loadAd(adRequest);*/
			}
		}
	}

	public boolean IsAdsHidden() {
		if (getResources().getBoolean(R.bool.show_ads) == false || ApplicationClass.AD_FREE_MODE) {
			return true;
		}
		return isFullVersion()
				|| ChooseProcessingActivity.isItemPurchased(this,
						ChooseProcessingActivity.REMOVE_ADS_PUCHASE_ID)
				|| MarketingHelper.isTrialPeriod(this)
				|| (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
				|| isPromoAppInstalled() || isFacebookPosted()
				|| (getProductIds().size() > 0);

	}

	public List<String> getProductIds() {
		SharedPreferences sPref;
		sPref = getSharedPreferences("productIds", MODE_PRIVATE);
		List<String> productIds = new ArrayList<String>();
		int count = sPref.getInt("ProductIdsCount", 0);
		for (int i = 0; i < count; i++) {
			String productId = sPref.getString("productId" + i, "");
			productIds.add(productId);
		}
		return productIds;
	}

	public boolean isPromoAppInstalled() {
		if (isPromoAppAlreadyChecked()) {
			return true;
		}
		int id = getResources().getIdentifier("promo_app_packagename",
				"string", getPackageName());
		if (id == 0) {
			return false;
		} else {
			String packageName = getResources().getString(id);
			// String packageName = "com.onemanwithcameralomo";
			if ((packageName == null) || (packageName.equals(""))) {
				return false;
			}
			Intent intent = getPackageManager().getLaunchIntentForPackage(
					packageName);
			boolean res = (intent != null);
			if (res) {
				SharedPreferences sPref;
				sPref = getSharedPreferences("promo_app", MODE_PRIVATE);
				Editor ed = sPref.edit();
				ed.putBoolean("promo_app_installed", true);
				ed.commit();
			}
			return res;
		}
	}

	public boolean isPromoAppAlreadyChecked() {
		SharedPreferences sPref;
		sPref = getSharedPreferences("promo_app", MODE_PRIVATE);
		boolean checked = sPref.getBoolean("promo_app_installed", false);
		return checked;
	}

	protected boolean isFullVersion() {
		return getPackageName().contains("full");
	}

	public boolean isFacebookPosted() {
		SharedPreferences sPref;
		sPref = getSharedPreferences("facebook", MODE_PRIVATE);
		boolean posted = sPref.getBoolean("facebook_posted", false);
		Log.d("facebook", "facebook_posted = " + posted);
		return posted;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		documentPath = getIntent().getStringExtra(
				EditPagesActivity.INTENT_PARAM_DOCUMENT_PATH);
		Presets presets = Presets.getPresets(this, true);
		cameraPreset = presets.getCameraPresets()[0];
		super.onCreate(savedInstanceState);
		folderName = getString(R.string.photoFolder);
		shotsNumber = getPicturesCount();
		int id = getResources().getIdentifier("use_camera_gates", "bool", getPackageName());		
		if (id != 0) {
			use_camera_gates = getResources().getBoolean(id);
		}
		
		//if (!getString(R.string.gatesAnimator).equals("flash")) {
			gatesAnimator = new GatesAnimator(this, R.id.gates_up, R.id.gates_down,
					View.VISIBLE, 200);				
		//} else {
		//	gatesFlashAnimator = new FlashGatesAnimator(this, R.id.flashGate, 150);
		//}
		ImageButton btn_back_to_home = (ImageButton) findViewById(R.id.btn_back_to_home);
		btn_back_to_home.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		/*btnPhotoTimer = (ImageButton) findViewById(R.id.btn_phototimer);
		btnPhotoTimer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btnPhotoTimer.setEnabled(false);
				getPhotoButton().setEnabled(false);
				textCounter.clearAnimation();
				textCounter.setVisibility(View.VISIBLE);
				textCounter.startAnimation(scale1);
			}
		});*/
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && gatesAnimator != null) {
			gatesAnimator.open();
		}
		if (hasFocus && gatesFlashAnimator != null) {
			gatesFlashAnimator.open();
		}
		prepareScaleAnimations();
	}
	
	private void prepareScaleAnimations() {		
		/*textCounter = (TextView) findViewById(R.id.textCounter);
		scale1.setDuration(650);
	    scale1.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				textCounter.setText("3");				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				textCounter.startAnimation(scale2);				
			}
		});
	    scale2.setDuration(650);
	    scale2.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				textCounter.setText("2");				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				textCounter.startAnimation(scale3);				
			}
		});
	    
	    scale3.setDuration(650);
	    scale3.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				textCounter.setText("1");				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				textCounter.setVisibility(View.INVISIBLE);				
				if (btnPhotoTimer != null) {
					btnPhotoTimer.setEnabled(true);
				}
				if (getPhotoButton() != null) {
					getPhotoButton().setEnabled(true);
					getPhotoButton().performClick();
				}				
			}
		});*/
	}

	@Override
	protected void savePhoto(String fileName, byte[] data) throws IOException {
		boolean mirror = isFaceCamera();
		int angle = getRotationDegrees();
		try{if(!getResources().getBoolean(getResources().getIdentifier("autorotation_photo_enabled","bool", getPackageName()))) {
			angle = 90;
		}}catch (Resources.NotFoundException e) {}


		int actualWidth = getPictureWidth();
		int actualHeight = getPictureHeight();
		savePhotoOpenCVWrapper(fileName, mirror, angle, data, actualWidth,
				actualHeight, isSquare(), Float.parseFloat(getResources().getString(R.string.camera_ratio)));
	}

	@Override
	protected boolean isShootOnTouch() {
		return false;
	}

	@Override
	protected void startNextActivity() {
		Intent intent = null;
		if (getString(R.string.workflow).equals("animaleyes")) {
			intent = new Intent(getApplicationContext(),
					CropActivity.class);
		} else if (getString(R.string.workflow).equals("turboscan")) {
			intent = new Intent(getApplicationContext(),
					DocumentTouchActivity.class);
		} else {
			intent = new Intent(getApplicationContext(),
					ChooseProcessingActivity.class);
		}
		
//		if (getString(R.string.workflow).equals("blendcamera")) {
//			Uri[] selectedImagesUri = getSelectedImages().toArray(new Uri[getSelectedImages().size()]);
//			Uri[] choosedImagesUri = new Uri[1];
//			choosedImagesUri[0] = selectedImagesUri[0];
//			intent.putExtra(ChooseProcessingActivity.INTENT_PARAM_URIS,	choosedImagesUri);	
//		} else {
			intent.putExtra(ChooseProcessingActivity.INTENT_PARAM_URIS,
			getSelectedImages()
					.toArray(new Uri[getSelectedImages().size()]));			
//		}
		intent.putExtra(
				ChooseProcessingActivity.INTENT_PARAM_START_FROM_CAMERA, true);
		intent.putExtra(EditPagesActivity.INTENT_PARAM_DOCUMENT_PATH,
				documentPath);

		startActivity(intent);
		getSelectedImages().clear();
		finish();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(this, SplashActivity.getHomeScreenClass(getApplicationContext()));
		startActivity(intent);
		finish();
	}

	ActionCallback<byte[]> shutterCallback_ = new ActionCallback<byte[]>() {
		@Override
		public void onAction(byte[] t) {
			if (gatesAnimator != null && !gatesAnimator.isGatesClosed()) {
				if (use_camera_gates) {
					gatesAnimator.close();
				}
			}
			if (gatesFlashAnimator != null && !gatesFlashAnimator.isGatesClosed()) {
				if (use_camera_gates) {
					gatesFlashAnimator.close();
				}
			}
		}
	};

	@Override
	protected ActionCallback<byte[]> getShutterCallback() {
		return shutterCallback_;
	}

	@Override
	protected ActionCallback<byte[]> getPostPhotoCallback() {
		return null;
	}

	@Override
	protected boolean isSquare() {
		return AppSettings.isDoSquarePhoto(this)/* cameraPreset.isSquare() */;
	}

	@Override
	protected int getPicturesCount() {		
		return cameraPreset.getPicturesCount();
		//return 1;
	}

	@Override
	protected boolean isVertical() {
		return false;
	}

	@Override
	public ImageButton getPhotoButton() {
		return (ImageButton) findViewById(R.id.btn_photo);
	}

	@Override
	public ImageButton getGalleryButton() {
		return (ImageButton) findViewById(R.id.btn_gallery);
	}

	@Override
	public ImageButton getTurnOffSoundButton() {
		return (ImageButton) findViewById(R.id.btn_turn_off_sound);
	}

	@Override
	public ImageButton getTurnOnSoundButton() {
		return (ImageButton) findViewById(R.id.btn_turn_on_sound);
	}

	@Override
	public ImageButton getChangeFlashSettingsButton() {
		return (ImageButton) findViewById(R.id.btn_change_flash_settings);
	}

	@Override
	public Button getTurnOffFlashButton() {
		return (Button) findViewById(R.id.btn_turn_flash_off);
	}

	@Override
	public Button getTurnOnFlashButton() {
		return (Button) findViewById(R.id.btn_turn_flash_on);
	}

	@Override
	public Button getAutoFlashButton() {
		return (Button) findViewById(R.id.btn_turn_flash_auto);
	}

	@Override
	public LinearLayout getFlashSelectPanel() {
		return (LinearLayout) findViewById(R.id.flash_select_pane);
	}

	@Override
	public ImageButton getRotateCameraButton() {
		return (ImageButton) findViewById(R.id.btn_rotate_camera);
	}

	@Override
	public int getFlashImageResource(int flashMode) {
		switch (flashMode) {
		case FlashState.FLASH_OFF:
			return R.drawable.step3_flash_off_land;
		case FlashState.FLASH_ON:
			return R.drawable.step3_flash_on_land;
		case FlashState.FLASH_AUTO:
		default:
			return R.drawable.step3_flash_auto_land;
		}
	}

	@Override
	public int getLayoutLandscape() {
		return R.layout.camera_preview_landscape;
	}

	@Override
	public int getLayoutPortrait() {
		return R.layout.camera_preview_landscape;
	}

	@Override
	public int getTopControlsParent() {
		return R.id.top_controls_parent;
	}

	@Override
	public int getTopOverlayFrame() {
		return R.id.top_overlay_frame;
	}

	@Override
	protected int getWantedWidth() {
		return AppSettings.getWidth(this);
	}

	@Override
	protected int getWantedHeight() {
		return AppSettings.getHeight(this);
	}

	@Override
	protected ViewGroup getPreviewConatiner() {
		// TODO Auto-generated method stub
		return (ViewGroup) findViewById(R.id.preview_container);
	}

	@Override
	protected GatesAnimator getGatesAnimator() {
		return null;
	}

	// @Override
	// public boolean cropping(Uri fileName) {
	//
	// //User had pick an image.
	// Cursor cursor = getContentResolver().query(fileName, new String[] {
	// android.provider.MediaStore.Images.ImageColumns.DATA }, null, null,
	// null);
	// cursor.moveToFirst();
	// //Link to the image
	// imageFilePath = cursor.getString(0);
	// cursor.close();
	// // get ratio from file
	// BitmapSize fileSize = null;
	// if (imageFilePath != null){
	// fileSize = Utils.getBitmapSizeFromFile(imageFilePath);
	// }
	// if (fileSize == null){
	// return false;
	// }
	// float ratio = (float)fileSize.width / (float)fileSize.height;
	// Log.d(LOG_TAG, "width = " + fileSize.width + " height = " +
	// fileSize.height + " ratio = " + ratio);
	// // check different rations
	// return true;
	// }

	protected String getOriginalFileName() {
		return Utils.getFullFileName(folderName, Utils.getDateFileName(),
				"jpeg");
	}

	@Override
	protected int getShotsNumber() {
		return shotsNumber;
	}

	// @Override
	// public void startCropActivity() {
	// if (imageFilePath == null){
	// return;
	// }
	//
	// Intent intent = new Intent(this, RotateAndCropActivity.class);
	// intent.putExtra(RotateAndCropActivity.INPUT_FILE_NAME, imageFilePath);
	// String outputFileName = Utils.getFullFileName(folderName, "jpeg");
	// intent.putExtra(RotateAndCropActivity.OUTPUT_FILE_NAME, outputFileName);
	// intent.putExtra(RotateAndCropActivity.RATIO, rations[cameraPosition]);
	// if (cameraPosition == 5 || cameraPosition == 6 || cameraPosition == 7){
	// intent.putExtra(RotateAndCropActivity.MULTY, true);
	// } else{
	// intent.putExtra(RotateAndCropActivity.MULTY, false);
	// }
	// startActivityForResult(intent, CROP);
	// }

	private int shotsNumber = 0;

	private FlashGatesAnimator gatesFlashAnimator;
	private GatesAnimator gatesAnimator;

	private static String folderName;

	// private static final String LOG_TAG = "CameraPreviewActivity";

	// private String imageFilePath;

	@Override
	public boolean cropping(Uri fileName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startCropActivity() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getBotOverlayFrame() {
		return R.id.bot_overlay_frame;
	}

	@Override
	public boolean isAutoSnapshotUsed() {
		int id = getResources().getIdentifier("use_auto_snapshot",
				"bool", getPackageName());
		if (id == 0) {
			return true;
		} else {
			return getResources().getBoolean(id);
		}		
	}
}
