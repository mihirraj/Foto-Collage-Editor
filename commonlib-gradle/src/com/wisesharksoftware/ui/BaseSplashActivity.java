package com.wisesharksoftware.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public abstract class BaseSplashActivity extends Activity {

	@Override
	protected
	void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);    // Removes title bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);    // Removes notification bar
		
		setContentView(getLayoutResource());
		
		ImageView splash = (ImageView)findViewById(getSplashId());
		if (!isPromoAppAlreadyChecked() && isPromoAppInstalled() && (getSplashPromoAppInstalled() != 0)) {			
			splash.setImageResource(getSplashPromoAppInstalled());
		} else {
			splash.setImageResource(getSplashResource());
		}
		// Start timer and launch main activity
		IntentLauncher launcher = new IntentLauncher();
		launcher.start();
	}
	
	@Override
	protected
	void onDestroy() {
		super.onDestroy();
		findViewById(getSplashId()).setBackgroundResource(0);
		ImageView splash = (ImageView)findViewById(getSplashId());
		splash.setImageResource(0);
	}
	
	abstract protected boolean isPromoAppInstalled();
	abstract protected boolean isPromoAppAlreadyChecked();
	
	abstract protected int getSplashPromoAppInstalled();
	abstract protected int getSplashId();
	
	abstract protected int getLayoutResource();
	
	abstract protected int[] getSplashResources();
	
	abstract protected void startMainActivity();
	
	private int getSplashResource() {
		int length = getSplashResources().length;
		double rnd = Math.random();
		int i = (int)Math.floor(rnd * length);
		return getSplashResources()[i >= length ? length - 1 : i];
	}
	
	private class IntentLauncher extends Thread {
		  @Override
		  /**
		   * Sleep for some time and than start new activity.
		   */
		  public void run() {
		     try {
		        // Sleeping
		        Thread.sleep(1000);
		     } catch (Exception e) {
		        Log.e("SplashActivity", e.getMessage());
		     }

		     //startMainActivity();
		  }
	}
}
