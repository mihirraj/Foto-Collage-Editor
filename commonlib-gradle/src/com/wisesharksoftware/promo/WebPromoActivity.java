package com.wisesharksoftware.promo;

import com.flurry.android.FlurryAgent;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.R;
import com.smsbackupandroid.lib.SettingsHelper;
import com.wisesharksoftware.core.Utils;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class WebPromoActivity extends Activity {
	private boolean start = true;
	private Bitmap bitmap;
	private ImageView imageview;
	private String filepath;
	private boolean portrait;
	private boolean orientationDetected;
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurryApiKey));
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_promo);
		String filepath = new StringBuilder()
		.append(WebPromoActivity.this.getCacheDir()
				.getAbsolutePath()).append("/")
		.append(PromoLoader.PROMO_FILE_NAME).toString();
		// detect image orientation
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Log.d("test", filepath);
		BitmapFactory.decodeFile(filepath, options);
		options.inJustDecodeBounds = false;
		int height = options.outHeight;
		int width = options.outWidth;
		Log.d("test ", height + "x" + width);
		if (height > width){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} 
		imageview = (ImageView) (findViewById(R.id.imageViewPromo));
		findViewById(R.id.btnClose).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String id = new GetterEventId(WebPromoActivity.this).getValue();
				if (id != null) {
					Utils.reportFlurryEvent(id, "Close");
				}
				finish();
			}
		});
		findViewById(R.id.btnDownload).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						String id = new GetterEventId(WebPromoActivity.this)
								.getValue();
						if (id != null) {
							Utils.reportFlurryEvent(id, "Download");
						}
						String link = SettingsHelper.getString(
								WebPromoActivity.this,
								PromoLoader.KEY_MARKET_URL, null);
						if (link != null) {
							Uri uri = Uri.parse(link);
							Intent viewIntent = new Intent(
									"android.intent.action.VIEW", uri);
							finish();
							startActivity(viewIntent);
						} else {
							finish();
						}
					}
				});
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (start) {
			start = false;
			int width = imageview.getWidth();
			int height = imageview.getHeight();
			String filepath = new StringBuilder()
					.append(WebPromoActivity.this.getCacheDir()
							.getAbsolutePath()).append("/")
					.append(PromoLoader.PROMO_FILE_NAME).toString();
			Log.d("test", "width = " + width);
			try {
				bitmap = Utils.getThumbnailFromPath(filepath, width, height);
				imageview.setImageBitmap(bitmap);
				imageview.setScaleType(ScaleType.FIT_XY);

			} catch (Exception e) {
				e.printStackTrace();
				finish();
			}
		}
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.web_promo, menu);
	// return true;
	// }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			if (imageview == null) {
				return;
			}
			BitmapDrawable bd = (BitmapDrawable)imageview.getDrawable();
			if (bd == null) {
				bitmap = null;
				imageview = null;
				return;
			}
			Bitmap bitmap = bd.getBitmap();
			if (bitmap == null || bitmap.isRecycled()) {
				this.bitmap = null;
				imageview = null;
				return;
			}
			bitmap.recycle();
			this.bitmap = null;
			imageview.setImageBitmap(null);
			imageview = null;
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "WebPromoActivity::onDestroy");
		}
	}

}
