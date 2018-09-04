package com.wisesharksoftware.gallery;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bastionsdk.android.Bastion;
import com.bastionsdk.android.BastionOfferListener;
import com.bastionsdk.android.Feature;
import com.bastionsdk.android.Offer;
import com.bastionsdk.android.Resource;
import com.flurry.android.FlurryAgent;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.startapp.android.publish.StartAppAd;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.gallery.ImageLoadTask.OnCompleteListener;
import com.photostudio.photoeditior.R;


public abstract class BaseGallerySplashScreen extends Activity implements OnCompleteListener, BastionOfferListener {
	private String eventId = "splash";
    protected static final int SELECT_PHOTO = 1;
    
    private boolean bastionEnabled = false;

	private StartAppAd startAppAd = new StartAppAd(this);
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurryApiKey));
		bastionEnabled = false;
		if (bastionEnabled) {
			Bastion.onStart(this, this);
		}
	}
	
	@Override
	protected void onStop() {
		if (bastionEnabled) {
			Bastion.onStop(this);
		}
		super.onStop();
		FlurryAgent.onEndSession(this);
		hideProgressBar();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getRootLayout());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		View view = getTakeFromGalleryButton();
		if (view != null) {
			view.setVisibility(isGalleryEmpty() ? View.GONE : View.VISIBLE);
		}
	}
	
	@Override
    protected void onDestroy() {
		if (bastionEnabled) {
			Bastion.onDestroy(this);
		}
        super.onDestroy();
    }	
	
	@Override
	protected void onNewIntent(Intent intent) {
		if (bastionEnabled) {
			Bastion.onNewIntent(this, intent);
		}
		super.onNewIntent(intent);
	}

	public abstract int getRootLayout();
	public abstract String getPath();
	public abstract boolean isShowingAds();
	protected abstract void startNextActivity(); 
	protected abstract void addSelectedImage(Uri uri); 
	protected abstract void showProgressBar(); 
	protected abstract void hideProgressBar(); 
	protected abstract View getTakeFromGalleryButton(); 
	
	public void onGalleryClick(View v){
		Utils.reportFlurryEvent(eventId, "onGalleryClick");
//		Intent intent = new Intent(this, ImageGridActivity.class);
//		intent.putExtra("path", getPath() + "/");
//		intent.putExtra("showAds", isShowingAds());
//		startActivity(intent);
	}
	public void onTakePhotoClick(View v){
		Utils.reportFlurryEvent(eventId, "onTakePhotoClick");
	}
	public void onSettingsClick(View v){
		Utils.reportFlurryEvent(eventId, "onSettingsClick");
	}
	
	protected boolean isGalleryEmpty() {
		try {
			String path = getPath();
			File folder = new File(path);
			if (!folder.exists()) {
				return true;
			}
			File[] listofFiles = folder.listFiles();
			if (listofFiles == null || listofFiles.length <= 0) {
				return true;
			}
			for (int i = 0; i < listofFiles.length; i++) {
				if (listofFiles[i].isFile()) {
					// not ideal solution
					if (!listofFiles[i].getName().equals("left.png")
							&& !listofFiles[i].getName().equals("right.png")
							&& !listofFiles[i].getName().equals(
									"align_left.png")
							&& !listofFiles[i].getName().equals(
									"align_right.png")) {
						if (!listofFiles[i].getName().contains("temp")) {
							return false;
						}
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "isalleryEmptyError");
			return true;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent returnedIntent) {
		super.onActivityResult(requestCode, resultCode, returnedIntent);
		switch (requestCode) {
		case SELECT_PHOTO:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = null;
				if (returnedIntent != null && returnedIntent.getData() != null) {
					selectedImage = returnedIntent.getData();
					ImageLoader imageLoader = new ImageLoader(getPath());
					imageLoader.load(this, selectedImage, this);
					Log.d("check", "onActivityResult: show ad");
					startAppAd.showAd();
				} else {
					onBitmapLoadError(null, false, "Returned inten is null");
				}
			}
		}
	}
	
	@Override
	public void onStartLoad() {
		showProgressBar();
		FlurryAgent.logEvent("LoadImageStart");
	}

	@Override
	public void onBitmapReady(String picturePath) {
		//hideProgressBar();
		FlurryAgent.logEvent("LoadImageSuccess");
		addSelectedImage((Uri.parse(picturePath)));
		startNextActivity();
	}

	@Override
	public void onBitmapLoadError(String picturePath, boolean outOfMemory, String errorMessage) {
		hideProgressBar();
		Utils.reportFlurryEvent("LoadImageFailed", errorMessage);
		Toast.makeText(this, /*getString(R.string.load_failed)*/"", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onRedeemOffer(Offer offer) {
		
		for(Feature feature : offer.getFeatures())
        {
            String featureRef = feature.getReference();
            String value = feature.getValue();
            FlurryAgent.logEvent(featureRef);
            Log.d("Bastion", "FeautureRef:'" + featureRef + "' value='"+value);
            // Provide the feature to the user
//            if (MarketingHelper.BASTION_TRIAL.equals(featureRef)) {
//            	MarketingHelper.setBastionTrial(this, true);
//            	hideAds();
//            }
        }

        for(Resource resource : offer.getResources() )
        {
            String resourceRef = resource.getReference();
            FlurryAgent.logEvent(resourceRef);
            int quantity = resource.getQuantity();
            Log.d("Bastion", "ResourceRef:'" + resourceRef + "' value='"+quantity);
            // Give the given quantity of the resource to the user
        }		
	}
	
	protected void hideAds() {
		
	}
}