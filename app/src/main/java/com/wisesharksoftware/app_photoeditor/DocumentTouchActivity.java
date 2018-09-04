package com.wisesharksoftware.app_photoeditor;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smsbackupandroid.lib.MarketingHelper;
import com.smsbackupandroid.lib.SettingsHelper;
import com.wisesharksoftware.controlls.TouchRectControll;
import com.wisesharksoftware.core.ImageProcessing;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.ui.BaseActivity;

public class DocumentTouchActivity extends BaseActivity {
	public static final String INTENT_PARAM_URIS = "uris";
	private ImageView rotate;
	private ImageView expand;
	private ImageView back;
	private ImageView next;
	private String documentPath;
	private TouchRectControll touchControl;
	private ArrayList<String> originalFileNames = new ArrayList<String>();
	private float angle = 0;
	private Bitmap image;
	private ProgressBar bar;
	private AdView adView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_document_touch);
		initViews();
		initData(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		initData(intent);
		touchControl.clear();
		super.onNewIntent(intent);
	}

	private void initViews() {

		touchControl = (TouchRectControll) findViewById(R.id.activity_document_touch_controller);
		expand = (ImageView) findViewById(R.id.activity_document_touch_expand);
		rotate = (ImageView) findViewById(R.id.activity_document_touch_rotate);
		back = (ImageView) findViewById(R.id.activity_document_touch_back);
		next = (ImageView) findViewById(R.id.activity_document_touch_next);
		bar = (ProgressBar) findViewById(R.id.activity_document_touch_bar);

		adView = (AdView) findViewById(R.id.adView);

		if (!IsAdsHidden()) {
			adView.setVisibility(View.GONE);
			AdRequest adRequest = new AdRequest.Builder().build();
		    adView.loadAd(adRequest);
		}
		expand.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (touchControl.getExpand()) {
					expand.setBackgroundResource(R.drawable.selector_expand);

				} else {
					expand.setBackgroundResource(R.drawable.selector_reexapand);
				}
				touchControl.expand();
			}
		});

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
				if (touchControl.hasPoints()) {
					if (touchControl.getExpand()) {
						expand.setBackgroundResource(R.drawable.selector_expand);
						touchControl.setExpand(false);
					}
					Intent intent = new Intent(self(),
							ChooseProcessingActivity.class);
					List<Uri> a = new ArrayList<Uri>();
					a.add(Uri.parse(originalFileNames.get(0)));
					intent.putExtra(ChooseProcessingActivity.INTENT_PARAM_URIS,
							a.toArray(new Uri[1]));
					intent.putExtra(
							ChooseProcessingActivity.INTENT_PARAM_ANGLE, angle);

					intent.putExtra(
							ChooseProcessingActivity.INTENT_PARAM_POINTS,
							touchControl.getRealPoints());
					intent.putExtra(
							EditPagesActivity.INTENT_PARAM_DOCUMENT_PATH,
							documentPath);

					startActivity(intent);
				}

			}
		});

		rotate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				angle += 90;
				if (angle >= 360) {
					angle = 0;
				}

				if (angle / 90 % 2 != 0) {
					touchControl.setOldSize(image.getWidth(), image.getHeight());
					Bitmap b = rotateImage(angle, image);
					resizeToSize(b, touchControl.getWidth(),
							touchControl.getHeight());

					touchControl.setBitmap(b);
					touchControl.setAngle(angle, b.getWidth(), b.getHeight());

				} else {

					Bitmap b = rotateImage(angle, image);
					resizeToSize(b, touchControl.getWidth(),
							touchControl.getHeight());
					touchControl.setBitmap(b);
					touchControl.setAngle(angle, b.getWidth(), b.getHeight());
				}

			}
		});
	}

	private Context self() {
		return this;
	}

	public Bitmap rotateImage(float angle, Bitmap bitmapSrc) {
		Matrix matrix = new Matrix();
		matrix.postRotate((float) angle, bitmapSrc.getWidth() / 2,

		bitmapSrc.getHeight() / 2);
		Bitmap b = Bitmap.createBitmap(bitmapSrc, 0, 0, bitmapSrc.getWidth(),
				bitmapSrc.getHeight(), matrix, true);
		return b;
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
		documentPath = intent
				.getStringExtra(EditPagesActivity.INTENT_PARAM_DOCUMENT_PATH);
		image = BitmapFactory.decodeFile(originalFileNames.get(0));
		new DetectDocumentTask().execute(originalFileNames.get(0));
		touchControl.post(new Runnable() {

			@Override
			public void run() {
				Bitmap b = null;
				Log.d("Image size",
						" w=" + image.getWidth() + " h=" + image.getHeight());
				image = resizeToSize(image, touchControl.getWidth(),
						touchControl.getHeight());
				touchControl.setOldSize(image.getWidth(), image.getHeight());
				touchControl.setRotatedSize(image.getWidth(), image.getHeight());
				b = image.copy(image.getConfig(), true);
				touchControl.setBitmap(b);
			}
		});

	}

	private static Bitmap resizeToSize(Bitmap bitmap, int w, int h) {
		Log.d("RESIZE SIZE",
				"imW=" + bitmap.getWidth() + " imH=" + bitmap.getHeight()
						+ " w=" + w + " h+" + h);
		if (bitmap.getWidth() < w && bitmap.getHeight() < h) {
			float scaley = (float) h / (float) bitmap.getHeight();
			float scalex = (float) w / (float) bitmap.getWidth();
			if (scalex < scaley) {
				bitmap = Bitmap.createScaledBitmap(bitmap,
						(int) (bitmap.getWidth() * scalex),
						(int) (bitmap.getHeight() * scalex), false);
			} else {
				bitmap = Bitmap.createScaledBitmap(bitmap,
						(int) (bitmap.getWidth() * scaley),
						(int) (bitmap.getHeight() * scaley), false);
			}
		} else {

			float scalex = (float) h / (float) bitmap.getHeight();
			float scaley = (float) w / (float) bitmap.getWidth();
			if (scalex < scaley) {
				bitmap = Bitmap.createScaledBitmap(bitmap,
						(int) (bitmap.getWidth() * scalex),
						(int) (bitmap.getHeight() * scalex), false);
			} else {
				bitmap = Bitmap.createScaledBitmap(bitmap,
						(int) (bitmap.getWidth() * scaley),
						(int) (bitmap.getHeight() * scaley), false);
			}

		}
		Log.d("Image size",
				" w=" + bitmap.getWidth() + " h=" + bitmap.getHeight());
		return bitmap;
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

	class DetectDocumentTask extends AsyncTask<Object, Void, double[]> {
		@Override
		protected void onPreExecute() {
			bar.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected double[] doInBackground(Object... param) {
			String fileName = (String) param[0];
			return ImageProcessing.detectSheetCorners(fileName);
		}

		@Override
		protected void onPostExecute(double[] result) {

			if (result.length == 8) {
				PointF points[] = {
						new PointF((float) result[0], (float) result[1]),
						new PointF((float) result[2], (float) result[3]),
						new PointF((float) result[4], (float) result[5]),
						new PointF((float) result[6], (float) result[7]) };
				touchControl.setPoints(points);
			} else {
				PointF points[] = { new PointF(-1, -1) };
				touchControl.setPoints(points);
			}
			bar.setVisibility(View.GONE);
			super.onPostExecute(result);
		}
	}

	@Override
	protected int getRootLayout() {
		return R.layout.activity_document_touch;
	}

	@Override
	protected int getPortraitLayout() {
		return R.layout.activity_document_touch;
	}

	@Override
	protected int getLandscapeLayout() {
		return R.layout.activity_document_touch;
	}

	@Override
	protected String getFlurryKey() {
		return getString(R.string.flurryApiKey);
	}
}
