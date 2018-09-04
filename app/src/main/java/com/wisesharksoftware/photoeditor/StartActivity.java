package com.wisesharksoftware.photoeditor;

import com.wisesharksoftware.app_photoeditor.SplashActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//
//		Batch.setConfig(new Config(getString(R.string.batch_api_id)));

		Intent intent = new Intent(this, SplashActivity.class);
		startActivity(intent);
		finish();
	}

}
