package com.wisesharksoftware.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.R;
import com.smsbackupandroid.lib.SettingsHelper;
import com.wisesharksoftware.core.ActionCallback;
import com.wisesharksoftware.core.OrientationListener;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.ui.GatesAnimator;

public abstract class BaseCameraPreviewActivity extends Activity {
	
	public abstract boolean isAutoSnapshotUsed();

	public abstract ImageButton getPhotoButton();

	public abstract ImageButton getGalleryButton();

	public abstract ImageButton getTurnOffSoundButton();

	public abstract ImageButton getTurnOnSoundButton();

	public abstract ImageButton getChangeFlashSettingsButton();

	public abstract Button getTurnOffFlashButton();

	public abstract Button getTurnOnFlashButton();

	public abstract Button getAutoFlashButton();

	public abstract LinearLayout getFlashSelectPanel();

	public abstract ImageButton getRotateCameraButton();

	public abstract int getFlashImageResource(int flashMode);

	public abstract int getLayoutLandscape();

	public abstract int getLayoutPortrait();

	public abstract int getTopControlsParent();

	public abstract int getBotOverlayFrame();

	public abstract int getTopOverlayFrame();

	// protected abstract ActionCallback<byte[]> getJpegCallback();

	protected abstract ActionCallback<byte[]> getShutterCallback();

	protected abstract ActionCallback<byte[]> getPostPhotoCallback();

	protected abstract int getWantedWidth();

	protected abstract int getWantedHeight();

	protected abstract ViewGroup getPreviewConatiner();

	protected abstract GatesAnimator getGatesAnimator();

	protected abstract boolean isSquare();

	protected abstract boolean isVertical();

	protected abstract int getPicturesCount();

	protected abstract void startNextActivity();
	
	protected abstract String getOriginalFileName();

	protected abstract int getShotsNumber();	
	
	protected int scaleAnimationCount = 0;
	
	private void createPreview() {
		if (mPreview != null) {
			getPreviewConatiner().removeView(mPreview);
			mPreview.releaseCamera();
		}

		initCameras();
		int cameraChoosen = SettingsHelper.getInt(getApplicationContext(),
				SettingsConstants.CAMERA_VIEW, defCamera);
		mPreview = new Preview(this, getJpegCallback(), getShutterCallback(),
				getPostPhotoCallback(), cameraChoosen, false, getWantedWidth(),
				getWantedHeight(), isShootOnTouch());

		mPreview.setOnReadyCallback(new ActionCallback<Void>() {
			@Override
			public void onAction(Void ignored) {
				BaseCameraPreviewActivity.this.initControls();
			}
		});

		getPreviewConatiner().addView(mPreview, 0);		
		getPhotoButton().setOnClickListener(new OnClickListener() {
			@TargetApi(Build.VERSION_CODES.ECLAIR)
			@Override
			public void onClick(View v) {
				try {
					scaleAnimationCount = 0;
				 getPhotoButton().setEnabled(false);
				Utils.reportFlurryEvent("ProcessPhoto", "From Camera");
				if (!mPreview.mCamera
						.getParameters()
						.getSupportedFocusModes()
						.contains(
								Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
					mPreview.takeShot();// mPreview.autoFocusAndShot();
				} else {
					mPreview.takeShot();
				}				

				if (gatesAnimator != null && !gatesAnimator.isGatesClosed()) {
					gatesAnimator.close();
				} else {
					if ((currentShotNumber < getShotsNumber() - 1) && !isAutoSnapshotUsed()) {
						Toast.makeText(getApplicationContext(), "Take another photo", Toast.LENGTH_LONG).show();
					}
				}
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		});

		getGalleryButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.reportFlurryEvent("ProcessPhoto", "From Gallery");
				selectedImages.clear();
				selectPhoto();				
			}
		});

		// ImageButton settingsButton = getSettingsButton();
		// if (settingsButton != null) {
		// settingsButton.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// FlurryAgent.logEvent("SettingsClick");
		// showDialog(SETTINGS_DIALOG);
		// }
		// });
		// if (!mPreview.isResolutionSupported(AppSettings.getWidth(this,
		// AppSettings.RES_NORMAL), AppSettings.getWidth(this,
		// AppSettings.RES_NORMAL))) {
		// AppSettings.setResolution(this, AppSettings.RES_LOW);
		// mPreview.resetCameraResolution();
		// settingsButton.setVisibility(View.GONE);
		// }
		// }
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			requestWindowFeature(Window.FEATURE_NO_TITLE);
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setContentView(getLayoutLandscape());
			} else {
				setContentView(getLayoutPortrait());
			}

			square_ = isSquare();
			vertical_ = isVertical();
			picturesCount_ = getPicturesCount();
			settingsPaneViewController_ = new SettingsPaneViewController(this);
			squareOverlayViewController_ = new SquareOverlayViewController(
					this, square_ ? 1 : picturesCount_);
			settingsPaneViewController_
					.setRedrawRequiredCallback(new ActionCallback<Void>() {
						@Override
						public void onAction(Void t) {
							createPreview();
						}
					});
			// createPreview();

			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);

			orientationEventListener = new OrientationListener(this,
					SensorManager.SENSOR_DELAY_NORMAL);

			if (orientationEventListener.canDetectOrientation()) {
				orientationEventListener.enable();
			}

			// gatesAnimator = new GatesAnimator(this, R.id.gates_up,
			// R.id.gates_down, View.VISIBLE, 200);
			gatesAnimator = getGatesAnimator();

		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "CameraPreviewActivity");
		}
	}

	private void initControls() {
		settingsPaneViewController_.init(mPreview, frontCameraNum_,
				backCameraNum_);
		settingsPaneViewController_.redraw();
	}

	protected boolean isShootOnTouch() {
		return true;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && gatesAnimator != null) {
			gatesAnimator.open();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mPreview != null) {
			mPreview.releaseCamera();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		createPreview();
		getPhotoButton().setEnabled(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mPreview != null) {
			mPreview.releaseCamera();
		}
		if (orientationEventListener != null) {
			orientationEventListener.disable();
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
				} else {
					selectedImage = outputFileUri;
					Intent mediaScanIntent = new Intent(
							Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					mediaScanIntent.setData(outputFileUri);
					this.sendBroadcast(mediaScanIntent);
				}

				boolean crop = cropping(selectedImage);

				// if (selectedImage == null) {
				// selectedImage = outputFileUri;
				// }

				if (crop) {
					FlurryAgent.logEvent("CropStartActivity");
					startCropActivity();
				} else {
					addSelectedImage(selectedImage);
					if (selectedImages.size() >= picturesCount_) {
						startNextActivity();
					} else {
						selectPhoto();
					}
				}
			}

			break;
		case CROP:
			Uri selectedImage = null;
			if (resultCode == RESULT_OK) {
				if (returnedIntent != null && returnedIntent.getData() != null) {
					selectedImage = returnedIntent.getData();
				}
			}
			if (selectedImage == null)
				return;
			addSelectedImage(selectedImage);
			if (selectedImages.size() >= picturesCount_) {
				startNextActivity();
			} else {
				selectPhoto();
			}

		}
	}

	// @Override
	// protected Dialog onCreateDialog(int id) {
	// switch(id) {
	// case SETTINGS_DIALOG:
	// return createSelectDialog();
	// }
	// return null;
	// }

	protected void addSelectedImage(Uri selectedImage) {
		selectedImages.add(selectedImage);
	}

	protected List<Uri> getSelectedImages() {
		return selectedImages;
	}

	private void selectPhoto() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");

		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
	}

	private void onResolutionSelected(int which) {
		if (which == AppSettings.getResolution(this)) {
			return;
		}
		AppSettings.setResolution(getApplicationContext(), which);
		mPreview.resetCameraResolution();
	}

	// private Dialog createSelectDialog() {
	// AlertDialog.Builder builder = new AlertDialog.Builder(this);
	//
	// builder.setTitle(R.string.selectResolutionDialogTitle);
	// boolean isHighSupported =
	// true;mPreview.isResolutionSupported(AppSettings.getWidth(this,
	// AppSettings.RES_HIGH),
	// AppSettings.getWidth(this, AppSettings.RES_HIGH));
	// builder.setSingleChoiceItems(isHighSupported ?
	// R.array.selectResolutionHighResource :
	// R.array.selectResolutionNormalResource,
	// AppSettings.getResolution(this), new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// onResolutionSelected(which);
	// dismissDialog(SETTINGS_DIALOG);
	// }
	// });
	// AlertDialog alertDialog = builder.create();
	// alertDialog.setCanceledOnTouchOutside(true);
	// return alertDialog;
	// }

	protected ActionCallback<byte[]> getJpegCallback() {
		return new ActionCallback<byte[]>() {			
			public void onAction(byte[] data) {
				Log.d("CameraPreview", "jpegCallback");
//				onAutoSnapshot(2000);													
				// String tempFileName = getTempFile();
				String tempFileName = getOriginalFileName();
				Log.d(LOG_TAG, "Orientation "
						+ getResources().getConfiguration().orientation);
				try {

					savePhoto(tempFileName, data);

					data = null;

					Uri fileUri = Uri.fromFile(new File(tempFileName));
					addSelectedImage(fileUri);
					if (AppSettings.isSaveOriginal(getApplicationContext())) {
						Utils.addPhotoToGallery(getApplicationContext(),
								fileUri);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (currentShotNumber < getShotsNumber() - 1) {
					currentShotNumber++;
					if (getPreview() != null) {
						getPreview().initializeCamera();
						if (isAutoSnapshotUsed()) {							
							try {
								Thread.sleep(2000); // Is it normal???
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							getPreview().takeShot();							
						} else {
							getPhotoButton().setEnabled(true);
						}
					}
					return;
				}
				// getPreview().releaseCamera();
				startNextActivity();
				currentShotNumber = 0;
				Log.d("CameraPreview", "onPictureTaken - jpeg");
			}
		};
	}

	protected void savePhoto(String fileName, byte[] data) throws IOException {
		FileOutputStream fileStream = new FileOutputStream(fileName);
		fileStream.write(data);
		fileStream.flush();		
		fileStream.close();		
		// boolean mirror = isFaceCamera();
		// int angle = orientationEventListener.getRotationDegrees();
		// int actualWidth = mPreview.getPictureSize().width;
		// int actualHeight = mPreview.getPictureSize().height;
		// savePhotoOpenCV(fileName, mirror, angle, data, actualWidth,
		// actualHeight);
	}

	public int getRotationDegrees() {
		return orientationEventListener.getRotationDegrees();
	}

	public int getPictureWidth() {
		return mPreview.getPictureSize().width;
	}

	public int getPictureHeight() {
		return mPreview.getPictureSize().height;
	}

	protected void savePhotoOpenCVWrapper(String origFileName, boolean mirror,
			int angle, byte[] jpegData, int width, int height, boolean square, float ratio) {
		savePhotoOpenCV(origFileName, mirror, angle, jpegData, width, height,
				square, ratio);		
	}

	public static void rotatePhotoOpenCVWrapper(String origFileName, boolean mirror,
			int angle, boolean square, float ratio) {
		rotatePhotoOpenCV(origFileName, mirror, angle, square, ratio);
	}

	private static native void savePhotoOpenCV(String origFileName,
			boolean mirror, int angle, byte[] jpegData, int width, int height,
			boolean square, float ratio);

	private static native void rotatePhotoOpenCV(String origFileName, boolean mirror, int angle,
			boolean square, float ratio);

	/*
	 * ActionCallback<byte[]> shutterCallback_ = new ActionCallback<byte[]>() {
	 * 
	 * @Override public void onAction(byte[] t) { //progressDialog =
	 * ProgressDialog.show( CameraPreviewActivity.this, "", getString(
	 * R.string.processingProgress ), true, false ); //gatesAnimator.close(); }
	 * 
	 * };
	 * 
	 * ActionCallback<byte[]> postPhotoCallback_ = new ActionCallback<byte[]>()
	 * {
	 * 
	 * @Override public void onAction(byte[] t) { //gatesAnimator.open(); }
	 * 
	 * };
	 * 
	 * ActionCallback<byte[]> jpegCallback_ = new ActionCallback<byte[]>() {
	 * 
	 * @SuppressLint("NewApi") public void onAction(byte[] data) {
	 * Log.d("CameraPreview", "jpegCallback");
	 * 
	 * // long startTime = System.currentTimeMillis();
	 * 
	 * byte[] newData = processCurrencyImage(data,
	 * mPreview.getPictureSize().width, mPreview.getPictureSize().height,
	 * GlobalSettings.getImageWidth(false, false),
	 * GlobalSettings.getImageHeight(false, false));
	 * 
	 * // long endTime = System.currentTimeMillis(); // long elapsedTime =
	 * endTime - startTime; // Log.d( "NativeProcessing", "Time: " + elapsedTime
	 * );
	 * 
	 * //String tempFileName = getTempFile(0); File file = new
	 * File(Utils.getFolderPath( getString(R.string.photoFolder) ),
	 * Utils.getDateFileName() + ".jpg"); Uri outputFileUri =
	 * Uri.fromFile(file); Log.d(LOG_TAG, "Orientation " +
	 * getResources().getConfiguration().orientation); try {
	 * BitmapFactory.Options options = new BitmapFactory.Options();
	 * options.inJustDecodeBounds = false; options.inPreferredConfig =
	 * Bitmap.Config.ARGB_8888; options.inDither = true; options.inScaled =
	 * false; options.inPurgeable = true; options.inSampleSize = 1;
	 * options.inInputShareable = true; //options.inPreferQualityOverSpeed =
	 * true; Bitmap bitmap = BitmapFactory.decodeByteArray(newData, 0,
	 * newData.length, options); Log.d("PHOTO", "w:" + bitmap.getWidth() + " h:"
	 * + bitmap.getHeight()); newData = null; Matrix matrix = new Matrix(); if
	 * (isFaceCamera()) { matrix.preScale(-1, 1); }
	 * 
	 * matrix.postRotate(orientationEventListener.getRotationDegrees());
	 * bitmap.setHasAlpha(true); // cropping int width = bitmap.getWidth(); int
	 * height = bitmap.getHeight(); // int reqWidth = 600; // int reqHeight =
	 * 600; // float reqRatio = (float) reqWidth / reqHeight; float reqRatio =
	 * 1.0f; float ratio = (float) width / height; float k = ratio / reqRatio;
	 * int widthCropped = width; int heightCropped = height; int left = 0; int
	 * top = 0; if (k > 1){ widthCropped = (int) (width / k); left = (width -
	 * widthCropped) / 2; } else { heightCropped = (int) (height * k); top =
	 * (height - heightCropped) / 2; }
	 * 
	 * Bitmap rotated = null; try { rotated = Bitmap.createBitmap(bitmap, left,
	 * top, widthCropped, heightCropped, matrix, true); bitmap.recycle();
	 * System.gc(); } catch (Error e) { new ExceptionHandler(e,
	 * "OnPictureTaken"); e.printStackTrace(); System.gc(); rotated =
	 * Bitmap.createBitmap(bitmap, left, top, widthCropped, heightCropped,
	 * matrix, true); bitmap.recycle(); System.gc(); } ByteArrayOutputStream
	 * stream = new ByteArrayOutputStream();
	 * rotated.compress(Bitmap.CompressFormat.JPEG, 100, stream); data =
	 * stream.toByteArray(); FileOutputStream fileStream = new
	 * FileOutputStream(file); fileStream.write(data); fileStream.flush();
	 * fileStream.close(); stream.close(); rotated.recycle(); data = null;
	 * System.gc(); } catch (Exception e) { e.printStackTrace(); new
	 * ExceptionHandler(e, "OnAction"); } //mPreview.releaseCamera(); Intent
	 * mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	 * mediaScanIntent.setData(outputFileUri); sendBroadcast(mediaScanIntent);
	 * Uri[] uris = new Uri[1]; uris[0] = outputFileUri; Intent intent = new
	 * Intent( getApplicationContext(), ChooseProcessingActivity.class );
	 * intent.putExtra( ChooseProcessingActivity.INTENT_PARAM_URIS, uris);
	 * intent.putExtra( ChooseProcessingActivity.INTENT_PARAM_CAMERA_PRESET_ID,
	 * 0 ); CameraPreviewActivity.this.startActivity( intent );
	 * 
	 * 
	 * // Intent intent = ApplyProcessingActivity.getIntent( //
	 * CameraPreviewActivity.this, // tempFileName); //
	 * CameraPreviewActivity.this.startActivity(intent); // if (progressDialog
	 * != null) { // progressDialog.dismiss(); // progressDialog = null; // }
	 * //gatesAnimator.open(); Log.d("CameraPreview", "onPictureTaken - jpeg");
	 * } };
	 */

	public boolean isFaceCamera() {
		int prevCameraNum = SettingsHelper.getInt(this,
				SettingsConstants.CAMERA_VIEW, defCamera);
		return prevCameraNum == frontCameraNum_;
	}

	private void initCameras() {
		try {
			Class<?> cameraInfoClass = Class
					.forName("android.hardware.Camera$CameraInfo");
			Field faceCameraField = cameraInfoClass.getField("facing");
			Class<Camera> cameraClass = Camera.class;
			Class<?> partypes[] = new Class[0];
			Method getNumCamerasMethod = cameraClass.getMethod(
					"getNumberOfCameras", partypes);
			int numCameras = (Integer) getNumCamerasMethod.invoke(null);
			Class<?> camInfoParams[] = new Class[2];
			camInfoParams[0] = Integer.TYPE;
			camInfoParams[1] = cameraInfoClass;

			Method getCameraInfoMethod = cameraClass.getMethod("getCameraInfo",
					camInfoParams);
			for (int i = 0; i < numCameras; ++i) {
				Object cameraInfo = cameraInfoClass.newInstance();
				getCameraInfoMethod.invoke(null, i, cameraInfo);
				int cameraFacing = faceCameraField.getInt(cameraInfo);
				if (cameraFacing == 0) {
					backCameraNum_ = i;
				} else if (cameraFacing == 1) {
					frontCameraNum_ = i;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (backCameraNum_ != -1) {
			defCamera = backCameraNum_;
		} else {
			if (frontCameraNum_ != -1) {
				defCamera = frontCameraNum_;
			}
		}
	}

	public Preview getPreview() {
		return mPreview;
	}

	private static final String LOG_TAG = "CameraPreviewActivity";
	protected Preview mPreview;

	private OrientationListener orientationEventListener;

	private int backCameraNum_ = -1;
	private int frontCameraNum_ = -1;
	private int defCamera = -1;
	protected boolean square_ = false;
	protected boolean vertical_ = false;
	protected int picturesCount_ = 1;
	private SettingsPaneViewController settingsPaneViewController_;
	protected SquareOverlayViewController squareOverlayViewController_;

	private static final int SELECT_PHOTO = 1;
	private static final int IMAGE_TITLE = 2;
	private static final int TAKE_PICTURE = 3;
	public static final int CROP = 4;

	private static final int SETTINGS_DIALOG = 2;

	private static int selectedCameraIdx = 0;
	private static Uri outputFileUri = null;
	private List<Uri> selectedImages = new ArrayList<Uri>();

	private int currentShotNumber = 0;

	public int getCurrentShotNumber() {
		return currentShotNumber;
	}

	private GatesAnimator gatesAnimator;

	abstract public boolean cropping(Uri fileName);

	abstract public void startCropActivity();
}
