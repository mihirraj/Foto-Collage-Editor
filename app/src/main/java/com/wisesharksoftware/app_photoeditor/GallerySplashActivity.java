package com.wisesharksoftware.app_photoeditor;

import java.util.ArrayList;
import java.util.List;

import net.hockeyapp.android.CrashManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ViewFlipper;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.MarketingHelper;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.gallery.BaseGallerySplashScreen;

public class GallerySplashActivity extends BaseGallerySplashScreen {
	private static Uri outputFileUri = null;
	private List<Uri> selectedImages = new ArrayList<Uri>();
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private ViewFlipper mViewFlipper;
	private final GestureDetector detector = new GestureDetector(
			new SwipeGestureDetector());
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageButton btnSettings = (ImageButton) findViewById(R.id.btnSettings);
		mContext = this;

		btnSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GallerySplashActivity.this,
						PreviewSettingsActivity.class);
				startActivity(intent);
				finish();
			}
		});

		ImageButton btnLoadFromGallery = (ImageButton) findViewById(R.id.btnLoadFromGallery);
		btnLoadFromGallery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectPhoto();
			}
		});

		mViewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
		mViewFlipper.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(final View view, final MotionEvent event) {
				detector.onTouchEvent(event);
				return true;
			}
		});

	}

	@Override
	protected void onStart() {
		View view = findViewById(R.id.adView);
		if (view != null && IsAdsHidden()) {
			view.setVisibility(View.GONE);
		}
		super.onStart();
	}

	private void selectPhoto() {
		try {
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");

			startActivityForResult(photoPickerIntent, SELECT_PHOTO);
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "selectPhoto");
		}
	}

	@Override
	public void onResume() {
		//Utils.reportFlurryEvent("DeviceId",
		//		((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
		//				.getDeviceId());
		Utils.reportFlurryEvent("onResume", this.toString());
		super.onResume();
		try {
			String hockeyAppId = getString(R.string.hockeyAppId);
			if (hockeyAppId != null && hockeyAppId.length() > 0) {
				CrashManager.register(this, hockeyAppId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		Utils.reportFlurryEvent("onPause", this.toString());
		super.onPause();
	}

	@Override
	public void onTakePhotoClick(View v) {
		super.onTakePhotoClick(v);
		startActivity(new Intent(this, CameraPreviewActivity.class));
		finish();
	}
	
	public void onDownloadClick(View v) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		String downloadLink = getString(R.string.full_app_link);
		if (downloadLink != null && downloadLink.length() > 0) {
			intent.setData(Uri.parse(downloadLink)); 
			startActivity(intent);
		}
	}

	@Override
	public int getRootLayout() {
		//return R.layout.activity_gallery_splash_screen;
		return 0;
	}

	@Override
	public String getPath() {
		return Utils.getFolderPath(getApplicationContext().getString(
				R.string.photoFolder));
	}

	@Override
	public boolean isShowingAds() {
		return false;
	}

	@Override
	protected void startNextActivity() {
		Intent intent = null;
		if (!getString(R.string.workflow).equals("turboscan")) {
			intent = new Intent(getApplicationContext(),
					ChooseProcessingActivity.class);
		} else {
			intent = new Intent(getApplicationContext(),
					DocumentTouchActivity.class);
		}
		intent.putExtra(ChooseProcessingActivity.INTENT_PARAM_URIS,
				selectedImages.toArray(new Uri[selectedImages.size()]));
		startActivity(intent);
		selectedImages.clear();
		finish();
	}

	@Override
	protected void addSelectedImage(Uri uri) {
		selectedImages.add(uri);
	}

	@Override
	protected void showProgressBar() {
		findViewById(R.id.imgLockScreen).setVisibility(View.VISIBLE);
		findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
	}

	@Override
	protected void hideProgressBar() {
		findViewById(R.id.imgLockScreen).setVisibility(View.INVISIBLE);
		findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
	}

	@Override
	protected View getTakeFromGalleryButton() {
		return findViewById(R.id.BtnGallerySplash);
	}

	public boolean IsAdsHidden() {
		if (getResources().getBoolean(R.bool.show_ads) == false) {
			return true;
		}
		return isFullVersion()
				|| ChooseProcessingActivity.isItemPurchased(this,
						ChooseProcessingActivity.REMOVE_ADS_PUCHASE_ID)
				|| MarketingHelper.isTrialPeriod(this) || isPromoAppInstalled()
				|| isFacebookPosted() || (getProductIds().size() > 0);
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

	class SwipeGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					mViewFlipper.stopFlipping();

					int leftin = getResources().getIdentifier("leftin", "anim",
							getPackageName());
					int leftout = getResources().getIdentifier("leftout",
							"anim", getPackageName());
					if (leftin != 0) {
						mViewFlipper.setInAnimation(AnimationUtils
								.loadAnimation(mContext, leftin));
					}
					if (leftout != 0) {
						mViewFlipper.setOutAnimation(AnimationUtils
								.loadAnimation(mContext, leftout));
					}
					mViewFlipper.showNext();
					return true;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					mViewFlipper.stopFlipping();
					int rightin = getResources().getIdentifier("rightin",
							"anim", getPackageName());
					int rightout = getResources().getIdentifier("rightout",
							"anim", getPackageName());
					if (rightin != 0) {
						mViewFlipper.setInAnimation(AnimationUtils
								.loadAnimation(mContext, rightin));
					}
					if (rightout != 0) {
						mViewFlipper.setInAnimation(AnimationUtils
								.loadAnimation(mContext, rightout));
					}
					mViewFlipper.showPrevious();
					return true;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return false;
		}
	}
}
