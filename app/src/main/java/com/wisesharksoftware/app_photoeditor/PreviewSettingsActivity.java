package com.wisesharksoftware.app_photoeditor;

import android.content.Intent;

import com.wisesharksoftware.core.Utils;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.ui.SettingsActivity;

public class PreviewSettingsActivity extends SettingsActivity {
	
	@Override
	public void onResume() {
		//Utils.reportFlurryEvent("DeviceId", ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId());
		Utils.reportFlurryEvent("onResume", this.toString());
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Utils.reportFlurryEvent("onPause", this.toString());
		super.onPause();
	}	

	@Override
	public void onBackPressed() {
		super.onBackPressed();		
		if (getResources().getString(R.string.workflow).equals("powercam")) {
			
		} else {
			Intent intent = new Intent(this, SplashActivity.getHomeScreenClass(getApplicationContext()));
			startActivity(intent);
			finish();
		}
	}
}
