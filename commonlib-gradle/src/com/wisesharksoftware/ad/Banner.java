package com.wisesharksoftware.ad;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import com.smsbackupandroid.lib.SettingsHelper;
import com.smsbackupandroid.lib.Utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class Banner extends AdListener{
	public interface OnBannerListener {
		public void onShow();
	}
	
	private static final String BANNER_COUNTER = "banner counter";
	private int maxCounter;
	private Context context;
	private InterstitialAd interstitialAd;
	private String LOG_TAG = "Banner"; 
	private boolean autoshow; 
	private OnBannerListener onBannerListener;
	
	public void setOnBannerListener(OnBannerListener onBannerListener) {
		this.onBannerListener = onBannerListener;
	}
	
	public Banner(Context context, int maxCounter, String id, boolean autoshow){
		this.autoshow = autoshow;
		Log.d(LOG_TAG, "banner created");
		this.context = context;
		this.maxCounter = maxCounter;
	    // Create an ad.
	    interstitialAd = new InterstitialAd(context);
	    interstitialAd.setAdUnitId(id);	   
	    interstitialAd.setAdListener(this);
	    // Load the interstitial ad.	    
	    AdRequest adRequest = new AdRequest.Builder().build();
	    interstitialAd.loadAd(adRequest);
	    //interstitialAd.loadAd(new AdRequest());
	}
	
	private void upCounter(String bannerCounter) {
		int counter = SettingsHelper.getInt(context, bannerCounter, 0);
		counter++;
		Log.d(LOG_TAG , "count = " + counter);
		SettingsHelper.setInt(context, bannerCounter, counter);
	}
	
	public void show() {
		upCounter(BANNER_COUNTER);
		int counter = SettingsHelper.getInt(context, BANNER_COUNTER, 0);
		if (counter >= maxCounter) {
			SettingsHelper.setInt(context, BANNER_COUNTER, 0);
			if (interstitialAd.isLoaded()) {
		        interstitialAd.show();

			} else {
				autoshow = true;
			}
		}
	}

	public void show(String counterId, int maxCounter) {
		upCounter(counterId);
		int counter = SettingsHelper.getInt(context, counterId, 0);
		if (counter >= maxCounter) {
			SettingsHelper.setInt(context, counterId, 0);
			if (interstitialAd.isLoaded()) {
				interstitialAd.show();

			} else {
				autoshow = true;
			}
	    }
	}

	@Override
	public void onAdLoaded() {
		Log.d(LOG_TAG, "banner received");
		if (autoshow){
			if (onBannerListener != null) {
				onBannerListener.onShow();
			}
			interstitialAd.show();

		}
	}
}
