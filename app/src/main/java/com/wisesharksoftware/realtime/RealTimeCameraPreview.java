package com.wisesharksoftware.realtime;

import static android.os.Build.VERSION.SDK_INT;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.MarketingHelper;
import com.smsbackupandroid.lib.SettingsHelper;
import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.app_photoeditor.CropActivity;
import com.wisesharksoftware.app_photoeditor.DocumentTouchActivity;
import com.wisesharksoftware.app_photoeditor.EditPagesActivity;
import com.wisesharksoftware.camera.AppSettings;
import com.wisesharksoftware.camera.BaseCameraPreviewActivity;
//import com.wisesharksoftware.camera.SettingsConstants;
import com.wisesharksoftware.core.ActionCallback;
import com.wisesharksoftware.core.OrientationListener;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Presets;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.opencv.OpenCVLoader;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.realtime.utils.CameraHelper;
import com.wisesharksoftware.realtime.utils.CameraHelper.CameraInfo2;
import com.wisesharksoftware.ui.GatesAnimator;
import com.wisesharksoftware.ui.PagerContainer;
import com.wisesharksoftware.ui.PagerContainer.OnPageSelectedCallback;
import com.wisesharksoftware.util.TypefaceManager;
//import com.wisesharksoftware.ui.

import com.wisesharksoftware.gpuimage.GPUImage;
import com.wisesharksoftware.gpuimage.Rotation;
import com.wisesharksoftware.gpuimage.GPUImage.OnPictureSavedListener;
import com.wisesharksoftware.gpuimage.filters.GPUImageFilter;
import com.wisesharksoftware.gpuimage.filters.GPUImageFilterTools;
import com.wisesharksoftware.gpuimage.filters.GPUImageGrayScaleFilter;
import com.wisesharksoftware.gpuimage.filters.GPUImagePinchDistortionFilter;
import com.wisesharksoftware.gpuimage.filters.GPUImagePinchDistortionFilter2;
import com.wisesharksoftware.gpuimage.filters.GPUImageSphereRefractionFilter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

public class RealTimeCameraPreview extends Activity implements OnClickListener {

	private static final int DIALOG_CAMERA_ERROR = 1;
	
	private GPUImage mGPUImage;
	private CameraHelper mCameraHelper;
	private CameraLoader mCamera;
	private GPUImageFilter mFilter;
	// private FilterAdjuster mFilterAdjuster;

	private int cameraPosition = 0;
	private int processingPosition = 0;
	private static String folderName;
	private Preset cameraPreset;

//	private int backCameraNum_ = -1;
//	private int frontCameraNum_ = -1;
	private int defCamera = 0;
	private static final int SELECT_PHOTO = 1;
	public static final int CROP = 4;
	private static Uri outputFileUri = null;
	private String imageFilePath;
	private List<Uri> selectedImages = new ArrayList<Uri>();

	private final static int FLASH_AUTO = 0;
	private final static int FLASH_ON = 1;
	private final static int FLASH_OFF = 2;
	private int flash_mode = FLASH_AUTO;

	private final static int SOUND_OFF = 0;
	private final static int SOUND_ON = 1;
	private int sound_mode = SOUND_ON;
	ActionCallback<byte[]> shutterCallback_;

	boolean shutterEnabled_ = true;
	int volume_ = -1;
	GLSurfaceView gl;
	
	GPUImagePinchDistortionFilter2 fisheyeFilter;
	GPUImagePinchDistortionFilter punchFilter;
	GPUImageSphereRefractionFilter brutFilter;
	GPUImagePinchDistortionFilter2 shiawaseFilter;
	GPUImagePinchDistortionFilter ripcurlFilter;
	GPUImageSphereRefractionFilter voyagerFilter;
	GPUImagePinchDistortionFilter2 lomoFilter;
	GPUImageSphereRefractionFilter pythinFilter;
	GPUImageGrayScaleFilter grayscaleFilter;
	
	private ImageButton photoButton; 

	private ImageButton rotateButton; 
	
	private final static String CAMERA_VIEW = "com.wisesharksoftware.fisheye.camera_view";
	
	private final static String CAMERA_POSITION = "CAMERA_POSITION";
	
	private String documentPath;
	
	private PagerContainer filtersPagerContainer;
	private Preset[] cameraPresets;
	
	private OrientationListener orientationEventListener;
	
	private int currentCameraId;
	
	private Size bestPictureSize;

	@Override
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurryApiKey));
		boolean reported = SettingsHelper.getBoolean(this, "OPENGL_REPORETED", false);
		if (!reported && mGPUImage != null && !mGPUImage.supportsOpenGLES2(this)) {
			Log.e("Life", "GL20_Unsupported");
			FlurryAgent.logEvent("GL20_Unsupported");
			SettingsHelper.setBoolean(this, "OPENGL_REPORETED", true);
		}
		if (isAdsHidden()) {
			View adView = getAdView();
			if (adView != null) {
				adView.setVisibility(View.GONE);
			}
		} else {
			AdView adView =  getAdView();			
			if (adView != null) {
				AdRequest adRequest = new AdRequest.Builder().build();
				adView.loadAd(adRequest);
			}
		}
	}
	
	public boolean isAdsHidden() {
		if (getResources().getBoolean(R.bool.show_ads) == false) {
			return true;
		}
		return isFullVersion()
				|| ChooseProcessingActivity.isItemPurchased(this,
						ChooseProcessingActivity.REMOVE_ADS_PUCHASE_ID)
				|| MarketingHelper.isTrialPeriod(this)
				|| (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
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
	
	protected boolean isFullVersion() {
    	return getPackageName().contains("full");
    }
	
	private AdView getAdView() {
		return (AdView)findViewById(R.id.adView);
	}

	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		Log.e("Life", "onCreate");
		
		Intent intent = getIntent();
		documentPath = getIntent().getStringExtra(
				EditPagesActivity.INTENT_PARAM_DOCUMENT_PATH);
		//initCameras();
		//cameraPosition = CameraListActivity.getPosition(intent);
		// processingPosition = ProcessingListActivity.getPosition(intent);

		folderName = getString(R.string.photoFolder);
		File imagesFolder = new File(Environment.getExternalStorageDirectory()
				+ "/" + folderName);
		imagesFolder.mkdirs();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_preview_realtime);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//cameraPreset = CameraListActivity.getPresets(this).getCameraPresets()[cameraPosition];

		photoButton = (ImageButton)findViewById(R.id.btn_photo);
		rotateButton = (ImageButton)findViewById(R.id.btn_rotate_camera);

		photoButton.setOnClickListener(this);
		findViewById(R.id.btn_gallery).setOnClickListener(this);
		findViewById(R.id.btn_change_flash_settings).setOnClickListener(this);
		findViewById(R.id.btn_turn_on_sound).setOnClickListener(this);

		mGPUImage = new GPUImage(this);
		gl = (GLSurfaceView) findViewById(R.id.surfaceView);
		if (SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			try {
				gl.setPreserveEGLContextOnPause(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mGPUImage.setGLSurfaceView(gl);
		mCameraHelper = new CameraHelper(this);
		rotateButton.setOnClickListener(this);
		if (!mCameraHelper.hasFrontCamera() || !mCameraHelper.hasBackCamera()) {
			rotateButton.setVisibility(View.GONE);
		}
		
		//switchCameraFilter(cameraPosition);
		if (getString(R.string.workflow).contains("nightcamera")) {
			switchCameraFilter(9);
		}
		gatesAnimator = new GatesAnimator(this, R.id.gates_up, R.id.gates_down, View.VISIBLE, 200);
		
		createFilterList();
		
		orientationEventListener = new OrientationListener(this,
				SensorManager.SENSOR_DELAY_NORMAL);

		if (orientationEventListener.canDetectOrientation()) {
			orientationEventListener.enable();
		}

	}
	
	private GatesAnimator gatesAnimator;

	@Override
	protected void onResume() {
		super.onResume();
		gl.onResume();
		Log.e("Life", "onResume");
		//gl.setPreview(this);
		FlurryAgent.logEvent("RT_OnResume");
//		String bigRandom = SplashActivity.getBigRandom(this);
//		Log.w("Random", bigRandom);
//		Utils.reportFlurryEvent("UserId", bigRandom);

		try {
			createCamera();
		} catch (RuntimeException e) {
			FlurryAgent.logEvent("RuntimeException");
			releaseCamera();
			e.printStackTrace();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				e.printStackTrace();
			}
			FlurryAgent.logEvent("RT_OnSleep");
			try {
				createCamera();
			} catch(Exception ex) {
				releaseCamera();
				ex.printStackTrace();
				new ExceptionHandler(ex, "RT_CreateCameraError");
				FlurryAgent.logEvent("RT_CreateCameraError");
				showDialog(DIALOG_CAMERA_ERROR);
			}
		}
		try {
			swithFlashModeTo(flash_mode);
			switchSoundModeTo(sound_mode);

			
			switch (cameraPosition) {
			case 1:
				if (brutFilter != null) {
					brutFilter.setAspectRatio(getAspectRatio());
				}
				break;
			case 4:
				if (voyagerFilter != null) {
					voyagerFilter.setAspectRatio(getAspectRatio());
				}
				break;
			case 7:
				if (pythinFilter != null) {
					pythinFilter.setAspectRatio(getAspectRatio());
				}
				break;
			}
			photoButton.setEnabled(true);
			rotateButton.setEnabled(true);
		} catch (Exception e) {
			e.printStackTrace();
			FlurryAgent.logEvent("RT_OnResumeException");
			releaseCamera();
			finish();
		}
		//gl.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
		Log.e("Life", "onPause");
		FlurryAgent.logEvent("RT_OnPause");
		gl.onPause();
	}

	@Override
	protected void onDestroy() {
//		try {
//			AdView adView = getAdView();
//			if (adView != null) {
//				adView.destroy();
//			}
//		} catch (Throwable th) {
//			th.printStackTrace();
//			new ExceptionHandler(th, "RT_AdViewDestroyError");
//		}
		try {
			super.onDestroy();
			if (orientationEventListener != null) {
				orientationEventListener.disable();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.e("Life", "onDestroy");
		FlurryAgent.logEvent("RT_OnDestroy");
	}
	
	public void releaseCamera() {
		synchronized (this) {
			if (mCamera != null) {
				mCamera.onPause();
				mCamera = null;
				StaticCameraHolder.setCamera(null);
			}
		}
	}

	public void createCamera() {
		synchronized (this) {
			if (mCamera == null) {
				int cameraId = SettingsHelper.getInt(getApplicationContext(), CAMERA_VIEW, defCamera);
				mCamera = new CameraLoader();
				mCamera.onResume(cameraId);
			}
		}
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
		try {
	        if (keyCode == KeyEvent.KEYCODE_BACK) {
	        	FlurryAgent.logEvent("RT_BackPressed");
	        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
	        	FlurryAgent.logEvent("RT_HomePressed");
	        } else if (keyCode == KeyEvent.KEYCODE_POWER) {
	        	FlurryAgent.logEvent("RT_PowerPressed");
	        }
	        if (!photoButton.isEnabled()) {
	        	FlurryAgent.logEvent("RT_SkeepKeyDown");
	        	return false;
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}

        return super.onKeyDown(keyCode, event);
    }

	private void addSelectedImage(Uri selectedImage) {
		selectedImages.add(selectedImage);
	}

	private List<Uri> getSelectedImages() {
		return selectedImages;
	}

	@Override
	public void onClick(final View v) {
		try {
			int id = v.getId();
			if (id == R.id.btn_photo) {
				if (mCamera == null || mGPUImage == null || !mGPUImage.isPreviewStarted()) {
					return;
				}
				if (!photoButton.isEnabled()) {
					return;
				}
				photoButton.setEnabled(false);
				rotateButton.setEnabled(false);
				if (!isAutoFocusSupported()) {
					takePicture();
					return;
				}
				String focusMode = mCamera.mCameraInstance.getParameters().getFocusMode();
				if (focusMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) ||
					focusMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) ||
					focusMode.equals(Camera.Parameters.FOCUS_MODE_INFINITY)) {
					takePicture();
				} else {
					mCamera.mCameraInstance
							.autoFocus(new Camera.AutoFocusCallback() {

								@Override
								public void onAutoFocus(final boolean success,
										final Camera camera) {
									takePicture();
								}
							});
				}
			} else if (id == R.id.btn_gallery) {
				//selectedImages.clear();
				//selectPhoto();
				cameraPosition = ++cameraPosition % 4;
				switchCameraFilter(cameraPosition);
			} else if (id == R.id.btn_change_flash_settings) {
				drawFlashControl();
			} else if (id == R.id.btn_turn_on_sound) {
				drawSoundControl();
			} else if (id == R.id.btn_rotate_camera) {
				if (!photoButton.isEnabled()) {
					return;
				}
				photoButton.setEnabled(false);
				if (mCamera != null) {
					int prevCameraNum = SettingsHelper.getInt(this, CAMERA_VIEW, defCamera);
					int newCamera = (prevCameraNum + 1) % mCameraHelper.getNumberOfCameras();
					SettingsHelper.setInt(this, CAMERA_VIEW, newCamera);
					mCamera.switchCamera(newCamera);
					swithFlashModeTo(flash_mode);
				}
				photoButton.setEnabled(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			FlurryAgent.logEvent("RT_OnClickException");
			releaseCamera();
			finish();
		}
	}

	private void selectPhoto() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");

		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
	}
	
	private void takePicture() {
		try {
			FlurryAgent.logEvent("RT_Photo");
			if (mCamera == null || mCamera.mCameraInstance == null) {
				return;
			}
			if (gatesAnimator != null && !gatesAnimator.isGatesClosed()) {
				gatesAnimator.close();
			}

			int wantedWidth = AppSettings.getWidth(this);
			int wantedHeight = AppSettings.getHeight(this);

			if (!shutterEnabled_) {
				shutterCallback = null;
			}
			
			mCamera.mCameraInstance.takePicture(shutterCallback, null,
					new Camera.PictureCallback() {

						@Override
						public void onPictureTaken(byte[] data, final Camera camera) {
							if (data == null || mCamera == null) {
								releaseCamera();
								finish();
								return;
							}
							try {
							switch (cameraPosition) {
							case 1:
								brutFilter.setAspectRatio((bestPictureSize.width * 1.0f)
										/ bestPictureSize.height);
								break;
							case 4:
								voyagerFilter.setAspectRatio((bestPictureSize.width * 1.0f)
										/ bestPictureSize.height);
								break;
							case 7:
								pythinFilter.setAspectRatio((bestPictureSize.width * 1.0f)
										/ bestPictureSize.height);
								break;
							}

							Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
							data = null;
							final GLSurfaceView view = (GLSurfaceView) findViewById(R.id.surfaceView);
							view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

							//mGPUImage.setImage(bitmap);

							String timeStamp = new SimpleDateFormat(
									"yyyy-MM-dd-HH-mm-ss").format(new Date());
							String name = timeStamp;
							File f = new File(Environment
									.getExternalStorageDirectory()
									+ File.separator
									+ folderName + File.separator + name + ".jpg");

							Uri uri = Uri.fromFile(f);
							addSelectedImage(uri);

							Log.d("ASDF", "uri = " + uri);
								FlurryAgent.logEvent("RT_SaveToPictures");
								releaseCamera();	
								mGPUImage.saveToPictures(bitmap, Environment
									.getExternalStorageDirectory() + "/" + folderName, name
									+ ".jpg", new OnPictureSavedListener() {

								@Override
								public void onPictureSaved(String path) {
									// pictureFile.delete();
									// camera.startPreview();
									// view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
									FlurryAgent.logEvent("RT_OnPictureSaved");
									mGPUImage.deleteImage();
									
									try {
										rotatePhoto(path, currentCameraId);
									} catch (Throwable th) {
										th.printStackTrace();
									}
									
									startNextActivity();
									
									if (AppSettings.isSaveOriginal(getApplicationContext())) {
										Utils.addPhotoToGallery(getApplicationContext(), path);
									}
								}

								@Override
								public void onPictureSaveFailed(Throwable thr) {
									FlurryAgent.logEvent("RT_OnPictureSaveFailed");
									new ExceptionHandler(thr, "RT_OnPictureSaveFailed");
									releaseCamera();
									finish();
								}

								});
							} catch (UnsatisfiedLinkError e) {
								FlurryAgent.logEvent("RT_UnsatisfiedLinkError");
								e.printStackTrace();
						    	try {
						    		if (OpenCVLoader.initDebug()) {
						    			FlurryAgent.logEvent("RT_OpenCV_Reloaded");
						    		}
						    		System.loadLibrary("processing");
					    			FlurryAgent.logEvent("RT_Processing_Reloaded");
						    	} catch (Error error) {
						    		error.printStackTrace();
					    			FlurryAgent.logEvent("RT_Reloaded_Failed");
									throw e;
						    	}
								finish();
							}
							catch (Exception ex) {
								FlurryAgent.logEvent("RT_SaveToPicturesError");
								releaseCamera();
								ex.printStackTrace();
							}

						}
					});
		} catch (Exception e) {
			e.printStackTrace();
			FlurryAgent.logEvent("RT_TakePictureException");
			releaseCamera();
			finish();
		}
		//mCamera.mCameraInstance.stopPreview();
	}
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private void switchFilterTo(final GPUImageFilter filter) {
		if (mFilter == null
				|| (filter != null && !mFilter.getClass().equals(
						filter.getClass()))) {
			mFilter = filter;
			mGPUImage.setFilter(mFilter);
			// mFilterAdjuster = new FilterAdjuster(mFilter);
		}
	}

	private class CameraLoader {
		private Camera mCameraInstance;

		public void onResume(final int id) {
			setUpCamera(id);
			
		}

		public void onPause() {
			releaseCamera();
		}

		public void switchCamera(final int id) {
			FlurryAgent.logEvent("RT_SwitchCamera");
			releaseCamera();
			setUpCamera(id);
		}

		private void setUpCamera(final int id) {
			mCameraInstance = getCameraInstance(id);
			StaticCameraHolder.setCamera(mCameraInstance);
			try {
				FlurryAgent.logEvent("RT_CameraCreated");
				Parameters parameters = mCameraInstance.getParameters();

				int wantedWidth = AppSettings.getWidth(getApplicationContext());
				int wantedHeight = AppSettings.getHeight(getApplicationContext());

				bestPictureSize = getBestPictureSize(wantedWidth, wantedHeight);
				String model = android.os.Build.MODEL;
				if (android.os.Build.MODEL.contains("Nexus 4") || android.os.Build.MODEL.contains("Nexus 7")) {
					switch (AppSettings.getResolution(getApplicationContext())) {
					case AppSettings.RES_HIGH:
						bestPictureSize.width = 3264;
						bestPictureSize.height = 1836;
						break;
					case AppSettings.RES_NORMAL:
						bestPictureSize.width = 1920;
						bestPictureSize.height = 1080;
						break;
					case AppSettings.RES_LOW:
						bestPictureSize.width = 1280;
						bestPictureSize.height = 720;
						break;
					default:
						break;
					}

				}
				parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);

				int orientation = mCameraHelper.getCameraDisplayOrientation(
						RealTimeCameraPreview.this, id);
				
				//parameters.setRotation(orientation);
				
				Size previewSize;
				DisplayMetrics metrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metrics);

				int height = metrics.heightPixels;
				int width = metrics.widthPixels;
				Log.d("ASDF", "we want height = " + height + " and width = "
						+ width);

				List<Camera.Size> sizes = parameters.getSupportedPreviewSizes(); // Doesn't
																					// exist
																					// in
																					// API
																					// 1.6
																					// http://groups.google.com/group/android-developers/browse_thread/thread/82c3c9e1d3942c7f
				previewSize = width > height ? getOptimalPreviewSize(sizes, width,
						height) : getOptimalPreviewSize(sizes, height, width);
				Log.d("ASDF", "we get preview height = " + previewSize.height
						+ " and width = " + previewSize.width);
				parameters.setPreviewSize(previewSize.width, previewSize.height);

				if (parameters.getSupportedFocusModes().contains(
						Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
					parameters
							.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				} else if (parameters.getSupportedFocusModes().contains(
						Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				} else if (parameters.getSupportedFocusModes().contains(
						Camera.Parameters.FOCUS_MODE_INFINITY)) {
					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
				} else if (parameters.getSupportedFocusModes().contains(
						Camera.Parameters.FOCUS_MODE_AUTO)) {
					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				}
				try {
					mCameraInstance.setParameters(parameters);
				} catch (RuntimeException e) {
					if (android.os.Build.MODEL.contains("Nexus 4") || android.os.Build.MODEL.contains("Nexus 7")) {
						switch (AppSettings.getResolution(getApplicationContext())) {
						case AppSettings.RES_HIGH:
							bestPictureSize.width = 1920;
							bestPictureSize.height = 1080;
							break;
						case AppSettings.RES_NORMAL:
							bestPictureSize.width = 1280;
							bestPictureSize.height = 720;
							break;
						case AppSettings.RES_LOW:
							bestPictureSize.width = 1280;
							bestPictureSize.height = 720;
							break;
						default:
							break;
						}

					}
					parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);
					mCameraInstance.setParameters(parameters);
				}

				CameraInfo2 cameraInfo = new CameraInfo2();
				mCameraHelper.getCameraInfo(id, cameraInfo);

				boolean flipHorizontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT ? true
						: false;

				Log.d("ASDF", "orientation = " + orientation);
				Log.d("ASDF", "flipHorizontal = " + flipHorizontal);

				
				//Preview.setCameraDisplayOrientation(RealTimeCameraPreview.this, id, mCameraInstance);
				mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal, false, Rotation.ROTATION_90);
				currentCameraId = id;
				
			} catch (Exception e) {
				e.printStackTrace();
				FlurryAgent.logEvent("RT_SetupCameraException");
				releaseCamera();
			}
		}

		private Camera getCameraInstance(final int id) {
			Log.e("Life", "createCamera");
			FlurryAgent.logEvent("RT_GetCameraInstance");
		    if (id == -1) {
		        return Camera.open();
		    }
	    	return mCameraHelper.openCamera(id);
		}

		private void releaseCamera() {
			try {
				Log.e("Life", "releaseCamera");
				if (mGPUImage != null) {
					mGPUImage.releaseCamera();
				}
				if (mCameraInstance != null) {
					FlurryAgent.logEvent("RT_ReleaseCamera");
					mCameraInstance.stopPreview();
					mCameraInstance.setPreviewCallback(null);
					mCameraInstance.release();
					mCameraInstance = null;
				} else {
					FlurryAgent.logEvent("RT_ReleaseCameraNull");
				}
			} catch (Exception e) {
				e.printStackTrace();
				FlurryAgent.logEvent("RT_ReleaseCameraException");
				new ExceptionHandler(e, "RT_ReleaseCamera");
			}
		}

		public Parameters getParameters() {
			return mCameraInstance != null ? mCameraInstance.getParameters() : null;
		}

		public void setParameters(Parameters params) {
			if (mCameraInstance != null) {
				mCameraInstance.setParameters(params);
			}
		}

	}

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

				// boolean crop = cropping(selectedImage);

				// if (selectedImage == null) {
				// selectedImage = outputFileUri;
				// }

				// if (crop) {
				// FlurryAgent.logEvent("CropStartActivity");
				// startCropActivity();
				// } else {
				addSelectedImage(selectedImage);
				// if (selectedImages.size() >= getPicturesCount()) {
				startNextActivity();
				// } else {
				// selectPhoto();
				// }
				// }
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
			if (selectedImages.size() >= getPicturesCount()) {
				startNextActivity();
			} else {
				selectPhoto();
			}

		}
	}

	public boolean cropping(Uri fileName) {
		// User had pick an image.
		Cursor cursor = getContentResolver()
				.query(fileName,
						new String[] { android.provider.MediaStore.Images.ImageColumns.DATA },
						null, null, null);
		cursor.moveToFirst();
		// Link to the image
		imageFilePath = cursor.getString(0);
		cursor.close();
		// get ratio from file
		if (imageFilePath == null) {
			return false;
		}
		return true;
	}

	protected int getPicturesCount() {
		return 1;
	}

	public ImageButton getChangeFlashSettingsButton() {
		return (ImageButton) findViewById(R.id.btn_change_flash_settings);
	}

	public ImageButton getChangeSoundSettingsButton() {
		return (ImageButton) findViewById(R.id.btn_turn_on_sound);
	}

	public int getSoundImageResource(final int soundMode) {
		switch (soundMode) {
		case SOUND_OFF:
			return R.drawable.step3_sound_off;
		case SOUND_ON:
			return R.drawable.step3_sound_on;
		default:
			return R.drawable.step3_sound_on;
		}
	}

	public int getFlashImageResource(final int flashMode) {
		switch (flashMode) {
		case FLASH_OFF:
			return R.drawable.step3_flash_off;
		case FLASH_ON:
			return R.drawable.step3_flash_on;
		case FLASH_AUTO:
		default:
			return R.drawable.step3_flash_auto;
		}
	}

	// sets flash mode to camera.
	public void setFlashMode(String cameraFlashMode) {
		try {
			if (mCamera != null) {
				Camera.Parameters params = mCamera.getParameters();
				params.setFlashMode(cameraFlashMode);
				mCamera.setParameters(params);
			}
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "SetFlashModeError");
		}
	}

	private String getFlashMode(int id) {
		switch (id) {
		case FLASH_OFF:
			return Camera.Parameters.FLASH_MODE_OFF;
		case FLASH_ON:
			return Camera.Parameters.FLASH_MODE_ON;
		case FLASH_AUTO:
		default:
			return Camera.Parameters.FLASH_MODE_AUTO;
		}
	}

	private void swithFlashModeTo(int switch_flash_mode) {
		boolean flashSupported = false;
		if (mCamera != null && mCamera.getParameters() != null) {
			flashSupported = mCamera.getParameters().getFlashMode() != null;
		}
		final ImageButton changeFlashSettingsButton = getChangeFlashSettingsButton();
		if (flashSupported) {
			changeFlashSettingsButton
					.setImageResource(getFlashImageResource(switch_flash_mode));
			changeFlashSettingsButton.setVisibility(ImageButton.VISIBLE);
			setFlashMode(getFlashMode(switch_flash_mode));
		} else {
			changeFlashSettingsButton.setVisibility(ImageButton.GONE);
		}
	}

	private void switchSoundModeTo(int switch_sound_mode) {
		final ImageButton changeSoundSettingsButton = getChangeSoundSettingsButton();
		changeSoundSettingsButton
				.setImageResource(getSoundImageResource(switch_sound_mode));
		changeSoundSettingsButton.setVisibility(ImageButton.VISIBLE);
		switch (switch_sound_mode) {
		case SOUND_ON:
			setShutterSound(true);
			break;
		case SOUND_OFF:
			setShutterSound(false);
			break;
		}
	}

	private void drawFlashControl() {
		switch (flash_mode) {
		case FLASH_AUTO:
			flash_mode = FLASH_ON;
			break;
		case FLASH_ON:
			flash_mode = FLASH_OFF;
			break;
		case FLASH_OFF:
			flash_mode = FLASH_AUTO;
			break;
		}
		swithFlashModeTo(flash_mode);
	}

	private void drawSoundControl() {
		switch (sound_mode) {
		case SOUND_ON:
			sound_mode = SOUND_OFF;
			break;
		case SOUND_OFF:
			sound_mode = SOUND_ON;
			break;
		}
		switchSoundModeTo(sound_mode);
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			if (shutterCallback_ != null && shutterEnabled_) {
				shutterCallback_.onAction(null);
			}
		}
	};
	
	@Override
	public void onWindowFocusChanged (boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && gatesAnimator != null) {
			gatesAnimator.open();
		}
	}
	
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//    	super.onConfigurationChanged(newConfig);
//    }

	public void setShutterSound(boolean shutterEnabled) {
		shutterEnabled_ = shutterEnabled;
	}

	public Size getBestPictureSize(int wantedWidth, int wantedHeight) {
		Camera.Parameters params = mCamera.getParameters();
		List<Size> supportSizes = params.getSupportedPictureSizes();
		Size bestSize = supportSizes.get(0);
		// int minDiff = 100000; // kind of infinite
		int minDiff = bestSize.width * 3 != bestSize.height * 4 ? 100000 : Math
				.abs(bestSize.width + bestSize.height - wantedWidth
						- wantedHeight);
		boolean haveSomething = false;
		for (int i = 1; i < supportSizes.size(); ++i) {
			int width = supportSizes.get(i).width;
			int height = supportSizes.get(i).height;
			Log.d("PICTSIZES", "w = " + width + " h = " + height + " aspect="
					+ ((float) width / (float) height));
			if (width * 3 != height * 4) {
				continue;
			}

			if (/* haveSomething && */(width < wantedWidth || height < wantedHeight)) {
				continue;
			} else {
				haveSomething = true;
			}

			int diff = Math.abs(width + height - wantedWidth - wantedHeight);
			if (diff < minDiff) {
				// haveSomething = true;
				minDiff = diff;
				bestSize = supportSizes.get(i);
			}
		}
		if (haveSomething) {
			return bestSize;
		} else {
			bestSize = supportSizes.get(0);
			for (int i = 1; i < supportSizes.size(); ++i) {
				int width = supportSizes.get(i).width;
				int height = supportSizes.get(i).height;
				if (width * 3 != height * 4) {
					continue;
				}
				int diff = Math
						.abs(width + height - wantedWidth - wantedHeight);
				if (diff < minDiff) {
					minDiff = diff;
					bestSize = supportSizes.get(i);
				}
			}
		}
		return bestSize;
	}

	public Size getBestPreviewSize(int wantedWidth, int wantedHeight) {
		// TODO not true
		Camera.Parameters params = mCamera.getParameters();
		List<Size> supportSizes = params.getSupportedPreviewSizes();
		Size bestSize = supportSizes.get(0);
		int minDiff = bestSize.width * 3 != bestSize.height * 4 ? 100000 : Math
				.abs(bestSize.width + bestSize.height - wantedWidth
						- wantedHeight);
		boolean haveSomething = false;
		for (int i = 1; i < supportSizes.size(); ++i) {
			int width = supportSizes.get(i).width;
			int height = supportSizes.get(i).height;
			if (width * 3 != height * 4) {
				continue;
			}
			if (haveSomething
					&& (width < wantedHeight || height < wantedHeight)) {
				continue;
			}
			int diff = Math.abs(width + height - wantedWidth - wantedHeight);
			if (diff < minDiff) {
				haveSomething = true;
				minDiff = diff;
				bestSize = supportSizes.get(i);
			}
		}
		return bestSize;
	}

	private static Size getOptimalSize(List<Size> sizes, int w, int h,
			final double aspectTolerance) {
		double targetRatio = (double) w / h;
		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > aspectTolerance)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}
		return optimalSize;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		if (sizes == null)
			return null;

		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		Size optimalSize = getOptimalSize(sizes, w, h, ASPECT_TOLERANCE);

		if (optimalSize == null) {
			final double ASPECT_TOLERANCE_2 = 0.13;
			optimalSize = getOptimalSize(sizes, w, h, ASPECT_TOLERANCE_2);
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}
	
	private boolean isAutoFocusSupported() {
		try {
			return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		  try {
			  switch(id) {
			  	case DIALOG_CAMERA_ERROR:
			        AlertDialog.Builder builder = new AlertDialog.Builder(this);
			        builder.setMessage(R.string.cameraErrorMessage)
			        		.setTitle(R.string.cameraErrorTitle)
			                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			                   public void onClick(DialogInterface dialog, int id) {
			                	   finish();
			                   }
			               });
			        AlertDialog alertDialog = builder.create();
			        
			        alertDialog.setCanceledOnTouchOutside(true);
			   		
			   		alertDialog.setOnCancelListener(new OnCancelListener() {
						
						@Override
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					});
			   		
			        return alertDialog;
			  }
			  } catch (Exception e) {
				  return null;
			  }
		  return null;
	}
	
	public boolean isBannerEnabled() {
		return getResources().getBoolean(R.bool.use_admob_banner);
	}

	private void switchCameraFilter(int cameraPosition) {
		this.cameraPosition = cameraPosition;
		
		switch (cameraPosition) {
		case 0:
			switchFilterTo(GPUImageFilterTools.createFilterForType(this,
					GPUImageFilterTools.FilterType.NO_FILTER));
			break;
			
		case 3:
			fisheyeFilter = (GPUImagePinchDistortionFilter2) 
			GPUImageFilterTools.createFilterForType(this, GPUImageFilterTools.FilterType.PINCH_DISTORTION2);
			fisheyeFilter.setScale(-0.25f);
			fisheyeFilter.setRadius(1.7f);
			switchFilterTo(fisheyeFilter);
			break;
		case 2:
			punchFilter = (GPUImagePinchDistortionFilter) 
			GPUImageFilterTools.createFilterForType(this, GPUImageFilterTools.FilterType.PINCH_DISTORTION);
			punchFilter.setScale(0.25f);
			punchFilter.setRadius(1.5f);
			switchFilterTo(punchFilter);
			break;
		case 1:
			brutFilter = (GPUImageSphereRefractionFilter) 
			GPUImageFilterTools.createFilterForType(this, GPUImageFilterTools.FilterType.SPHERE_REFRACTION);
			brutFilter.setRefraction(0.25f);
			brutFilter.setAspectRatio(getAspectRatio());
			switchFilterTo(brutFilter);
			break;
		case 6:
			shiawaseFilter = (GPUImagePinchDistortionFilter2) 
			GPUImageFilterTools.createFilterForType(this, GPUImageFilterTools.FilterType.PINCH_DISTORTION2);
			shiawaseFilter.setScale(-0.5f);
			shiawaseFilter.setRadius(1.5f);
			switchFilterTo(shiawaseFilter);
			break;
		case 5:
			ripcurlFilter = (GPUImagePinchDistortionFilter) 
			GPUImageFilterTools.createFilterForType(this, GPUImageFilterTools.FilterType.PINCH_DISTORTION);
			ripcurlFilter.setScale(0.5f);
			ripcurlFilter.setRadius(1.5f);
			switchFilterTo(ripcurlFilter);
			break;
		case 4:
			voyagerFilter = (GPUImageSphereRefractionFilter) 
			GPUImageFilterTools.createFilterForType(this, GPUImageFilterTools.FilterType.SPHERE_REFRACTION);
			voyagerFilter.setRefraction(0.5f);
			voyagerFilter.setAspectRatio(getAspectRatio());
			switchFilterTo(voyagerFilter);
			break;
		case 8:
			lomoFilter = (GPUImagePinchDistortionFilter2) 
			GPUImageFilterTools.createFilterForType(this, GPUImageFilterTools.FilterType.PINCH_DISTORTION2);
			lomoFilter.setScale(0.7f);
			lomoFilter.setRadius(1.5f);
			switchFilterTo(lomoFilter);
			break;
		case 7:
			pythinFilter = (GPUImageSphereRefractionFilter) 
			GPUImageFilterTools.createFilterForType(this, GPUImageFilterTools.FilterType.SPHERE_REFRACTION);
			pythinFilter.setRefraction(0.75f);
			pythinFilter.setAspectRatio(getAspectRatio());
			switchFilterTo(pythinFilter);
			break;
		case 9:			
			grayscaleFilter = (GPUImageGrayScaleFilter) 
			GPUImageFilterTools.createFilterForType(this, GPUImageFilterTools.FilterType.GRAYSCALE);
			switchFilterTo(grayscaleFilter);
			break;
		default:
			switchFilterTo(GPUImageFilterTools.createFilterForType(this,
					GPUImageFilterTools.FilterType.NO_FILTER));
			this.cameraPosition = 0;
		}
		SettingsHelper.setInt(getApplicationContext(), CAMERA_POSITION, this.cameraPosition);
	}
	
	private float getAspectRatio() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		int height = metrics.heightPixels;
		int width = metrics.widthPixels;

		return (height * 1.0f) / width;
	}
	
	protected void savePhoto(String fileName, byte[] data) throws IOException {
//		boolean mirror = isFaceCamera();
//		int angle = getRotationDegrees();
//		int actualWidth = getPictureWidth();
//		int actualHeight = getPictureHeight();
//		savePhotoOpenCVWrapper(fileName, mirror, angle, data, actualWidth,
//				actualHeight, isSquare(), Float.parseFloat(getResources().getString(R.string.camera_ratio)));
	}
	
	private void createFilterList() {
		
		filtersPagerContainer = (PagerContainer)findViewById(R.id.pager_container);
		
		cameraPresets = Presets.getPresets(this).getCameraPresets();
		
        ViewPager pager = filtersPagerContainer.getViewPager();
        
        pager.setPageTransformer(false, new PageTransformer() {
			
			@SuppressLint("NewApi")
			@Override
			public void transformPage(View page, float paramFloat) {
				// TODO Auto-generated method stub
				try {
					final float normalizedposition = Math.abs(Math.abs(paramFloat) - 1);
					page.setScaleX(normalizedposition / 2 + 0.5f);
					page.setScaleY(normalizedposition / 2 + 0.5f);
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}
		});
		
        PagerAdapter adapter = new TextViewPagerAdapter();
        pager.setAdapter(adapter);
        //Necessary or the pager will only have one extra page to show
        // make this at least however many pages you can see
        pager.setOffscreenPageLimit(adapter.getCount());
        //A little space between pages
        //pager.setPageMargin(15);
        //If hardware acceleration is enabled, you should also remove
        // clipping on the pager for its children.
        pager.setClipChildren(false);
        cameraPosition = SettingsHelper.getInt(getApplicationContext(), CAMERA_POSITION, 1);
        pager.setCurrentItem(cameraPosition);
        switchCameraFilter(cameraPosition);
        
        filtersPagerContainer.setOnPageSelectedCallback(new OnPageSelectedCallback() {
			
			@Override
			public void onPageSelected(int position) {
				switchCameraFilter(position);
			}
		});
        
	}

    private class TextViewPagerAdapter extends PagerAdapter {
    	 
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TextView view = new TextView(RealTimeCameraPreview.this);
            view.setText(cameraPresets[position].getName());
            view.setGravity(Gravity.CENTER);
            view.setBackgroundColor(Color.argb(0, position * 50, position * 10, position * 50));
			Typeface myTypeface = TypefaceManager.createFromAsset(getAssets(), "fonts/MyriadPro-Cond.otf");
			view.setTypeface(myTypeface);
			view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28f);
            view.setTextColor(Color.rgb(255, 255, 255));
            view.setTag(position);
            container.addView(view);
            return view;
        }
 
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }
 
        @Override
        public int getCount() {
            return cameraPresets.length;
        }
 
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }
    }
    
    private boolean isFaceCamera(int id) {
		CameraInfo2 cameraInfo = new CameraInfo2();
		mCameraHelper.getCameraInfo(id, cameraInfo);
    	return cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT;
    }
    
    private boolean isSquare() {
    	return cameraPosition == 1 || cameraPosition == 4 || cameraPosition == 7;
    }
    
	private void rotatePhoto(String fileName, int cameraId) throws IOException {
		boolean mirror = isFaceCamera(cameraId);
		int angle = getRotationDegrees();
		BaseCameraPreviewActivity.rotatePhotoOpenCVWrapper(fileName, mirror, angle,
				isSquare(), Float.parseFloat(getResources().getString(R.string.camera_ratio)));
	}
    
	public int getRotationDegrees() {
		return orientationEventListener.getRotationDegrees();
	}

}
