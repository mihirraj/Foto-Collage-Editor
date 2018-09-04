package com.wisesharksoftware.camera;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;

import com.flurry.android.FlurryAgent;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.Utils;
import com.wisesharksoftware.core.ActionCallback;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {

	public Preview(BaseCameraPreviewActivity context,
			ActionCallback<byte[]> callback,
			ActionCallback<byte[]> shutterCallback,
			ActionCallback<byte[]> postPhotoCallback, int cameraId,
			boolean multiPictures, int wantedWidth, int wantedHeight,
			boolean shootOnTouch) {
		super(context);
		cameraId_ = cameraId;
		callback_ = callback;
		shutterCallback_ = shutterCallback;
		postPhotoCallback_ = postPhotoCallback;
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mContext = context;
		mIsTakingPhoto = false;
		wantedWidth_ = wantedWidth;
		wantedHeight_ = wantedHeight;
		shootOnTouch_ = shootOnTouch;

		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		wantedWidth_ = display.getWidth();
		wantedHeight_ = display.getHeight();
		Log.v("DISPLAY",
				" w= " + display.getWidth() + " h = " + display.getHeight());

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (shootOnTouch_) {
				autoFocusAndShot();
			}
		}
		return true;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	};

	public String getFlashMode() {
		if (mCamera != null) {
			return mCamera.getParameters().getFlashMode();
		}
		return null;
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

	public void setShutterSound(boolean shutterEnabled) {
		shutterEnabled_ = shutterEnabled;
	}

	public void releaseCamera() {
		try {
			synchronized (this) {
				if (mCamera != null) {
					mCamera.stopPreview();
					mCamera.release();
					mCamera = null;
				}
				mIsTakingPhoto = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "ReleaseCamera");
		}
	}

	/**
	 * Set camera using reflection for if API more than 9.
	 */
	private void createCamera() {
		if (cameraId_ == -1) {
			mCamera = Camera.open();
			return;
		}
		try {
			Class<Camera> cameraClass = Camera.class;
			Class<?> partypes[] = new Class[1];
			partypes[0] = Integer.TYPE;
			Method method = cameraClass.getMethod("open", partypes);
			mCamera = (Camera) method.invoke(null, cameraId_); // since it is
																// static - pass
																// null
		} catch (Exception exception) {
			// it is cool, just open it normally.
			exception.printStackTrace();
			mCamera = Camera.open();
		}
	}

	@SuppressLint("NewApi")
	public void resetCameraResolution() {
		if (mCamera == null) {
			return;
		}
		Camera.Parameters params = mCamera.getParameters();
		// boolean isHd = AppSettings.getHdEnabled(getContext());
		// int wantedWidth = AppSettings.getOriginalWidth(getContext(), isHd);
		// int wantedHeight = AppSettings.getOriginalHeight(getContext(), isHd);
		int wantedWidth = AppSettings.getWidth(getContext());
		int wantedHeight = AppSettings.getHeight(getContext());

		Size bestSize = getBestSize(wantedWidth, wantedHeight);
		pictureSize_ = bestSize;
		Log.v("MODEL", " model= " + android.os.Build.MODEL);
		if (android.os.Build.MODEL.contains("Nexus 4") || android.os.Build.MODEL.contains("Nexus 7")) {
			switch (AppSettings.getResolution(getContext())) {
			case AppSettings.RES_HIGH:
				pictureSize_.width = 3264;
				pictureSize_.height = 1836;
				break;
			case AppSettings.RES_NORMAL:
				pictureSize_.width = 1920;
				pictureSize_.height = 1080;
				break;
			case AppSettings.RES_LOW:
				pictureSize_.width = 1280;
				pictureSize_.height = 720;
				break;
			default:
				break;
			}

		}
		params.setPictureSize(pictureSize_.width, pictureSize_.height);
		params.setJpegQuality(100);
		if (params.isZoomSupported()) {
			params.setZoom(0);
		}
		try {
			mCamera.setParameters(params);
		} catch (RuntimeException e) {
			if (android.os.Build.MODEL.contains("Nexus 4") || android.os.Build.MODEL.contains("Nexus 7")) {
				switch (AppSettings.getResolution(getContext())) {
				case AppSettings.RES_HIGH:
					pictureSize_.width = 1920;
					pictureSize_.height = 1080;
					break;
				case AppSettings.RES_NORMAL:
					pictureSize_.width = 1280;
					pictureSize_.height = 720;
					break;
				case AppSettings.RES_LOW:
					pictureSize_.width = 1280;
					pictureSize_.height = 720;
					break;
				default:
					break;
				}

			}
			params.setPictureSize(pictureSize_.width, pictureSize_.height);
			mCamera.setParameters(params);
		}
		Log.v("PictSizr", " w= " + pictureSize_.width + " h = "
				+ pictureSize_.height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			if (mCamera != null) {
				mCamera.release();
			}
			createCamera();
			resetCameraResolution();
			// notify parent that camera is ready.
			readyCallback_.onAction(null);
			mCamera.setPreviewDisplay(holder);
		} catch (Exception exception) {
			exception.printStackTrace();
			new ExceptionHandler(exception, "SurfaceCreated");
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
	}

	public boolean isResolutionSupported(int width, int height) {
		Size bestSize = getBestSize(width, height);
		return bestSize.width >= width && bestSize.height >= height;
	}

	@SuppressLint("NewApi")
	public Size getBestSize(int wantedWidth, int wantedHeight) {
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

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		synchronized (this) {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
				mCamera = null;
			}
		}
	}

	public void initializeCamera(Camera camera) {
		try {
			camera.setPreviewDisplay(this.getHolder());
			/*
			 * Parameters param = camera.getParameters();
			 * param.setPreviewSize(previewSize_.width, previewSize_.height);
			 * camera.setParameters(param);
			 */
			startPreview(camera);
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "Preview:initializeCamera");
		}
	}

	public void takeShot() {
		AudioManager mgr = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		if (!shutterEnabled_) {
			volume_ = mgr.getStreamVolume(AudioManager.STREAM_SYSTEM);
			mgr.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
		}

		try {
			if (mCamera != null) {
				initializeCamera(mCamera);
				FlurryAgent.onEvent("TakePhoto:Photo");
				// mCamera.stopPreview();
				mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			new ExceptionHandler(ex, "Preview:takeShot");
			mgr.setStreamVolume(AudioManager.STREAM_SYSTEM, volume_, 0);
		}
	}

	public void initializeCamera() {
		try {
			if (mCamera == null) {
				createCamera();
				resetCameraResolution();
			}
			initializeCamera(mCamera);
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "InitializeCamera");
		}
	}

	@SuppressLint("NewApi")
	public void autoFocusAndShot() {
		try {
			synchronized (this) {
				if (mIsTakingPhoto || mCamera == null) {
					return;
				}
				mIsTakingPhoto = true;
				String focusMode = mCamera.getParameters().getFocusMode();
				if (Camera.Parameters.FOCUS_MODE_AUTO.equals(focusMode)
						|| Camera.Parameters.FOCUS_MODE_MACRO.equals(focusMode)) {
					mCamera.autoFocus(new Camera.AutoFocusCallback() {
						@Override
						public void onAutoFocus(boolean success, Camera camera) {
							takeShot();
						}
					});
				} else {
					takeShot();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			new ExceptionHandler(ex, "Preview:autoFocusAndShot");
		}
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			// Log.d("CameraPreview", "shutterCallback");
			if (shutterCallback_ != null) {
				shutterCallback_.onAction(null);
			}
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
			// Log.d("CameraPreview", "rawCallback");
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(final byte[] data, Camera _) {
			/*
			 * if (volume_ != -1) { AudioManager mgr =
			 * (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
			 * mgr.setStreamVolume(AudioManager.STREAM_SYSTEM, volume_, 0); }
			 */
			if (!shutterEnabled_) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(2000);
							AudioManager mgr = (AudioManager) mContext
									.getSystemService(Context.AUDIO_SERVICE);
							mgr.setStreamVolume(AudioManager.STREAM_SYSTEM,
									volume_, 0);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}).start();
			}
			if (takePhotoTask != null) {
				takePhotoTask.cancel(true);
			}
			takePhotoTask = null;
			synchronized (this) {
				takePhotoTask = new TakePhotoTask(postPhotoCallback_, data);
				takePhotoTask.execute(callback_);
			}

			// new Thread(new Runnable(){
			//
			// @Override
			// public void run() {
			// // TODO Auto-generated method stub
			// callback_.onAction(data);
			//
			// }
			// }).start();
		}
	};

	private class TakePhotoTask extends
			AsyncTask<ActionCallback<byte[]>, Integer, Integer> {
		ActionCallback<byte[]> postCallback;
		byte[] data;

		public TakePhotoTask(ActionCallback<byte[]> postCallback, byte[] data) {
			this.postCallback = postCallback;
			this.data = data;
		}

		@Override
		protected Integer doInBackground(ActionCallback<byte[]>... params) {
			Log.d(LOG_TAG, "doInBackground");
			try {
				params[0].onAction(data);
			} catch (Throwable th) {
			}
			return 0;
		}

		@Override
		protected void onPreExecute() {
			Log.d(LOG_TAG, "onPreExecute");
		}

		@Override
		protected void onPostExecute(Integer result) {
			Log.d(LOG_TAG, "onPostExecute");
			if (postCallback != null) {
				postCallback.onAction(null);
			}
			// if( th != null )
			// {
			// callback.onFail( th );
			// }
			// else
			// {
			// callback.onSuccess();
			// }
		}
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
		Log.v("FIRST OPTIMAL SIZE", optimalSize + "");
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
		/*
		 * final double ASPECT_TOLERANCE = 0.05; double targetRatio = (double)
		 * w/h;
		 * 
		 * if (sizes==null) return null;
		 * 
		 * Size optimalSize = null;
		 * 
		 * double minDiff = Double.MAX_VALUE;
		 * 
		 * int targetHeight = h;
		 * 
		 * // Find size for (Size size : sizes) { double ratio = (double)
		 * size.width / size.height; if (Math.abs(ratio - targetRatio) >
		 * ASPECT_TOLERANCE) continue; if (Math.abs(size.height - targetHeight)
		 * < minDiff) { optimalSize = size; minDiff = Math.abs(size.height -
		 * targetHeight); } }
		 * 
		 * if (optimalSize == null) { minDiff = Double.MAX_VALUE; for (Size size
		 * : sizes) { if (Math.abs(size.height - targetHeight) < minDiff) {
		 * optimalSize = size; minDiff = Math.abs(size.height - targetHeight); }
		 * } } return optimalSize;
		 */
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		// Log.d("GET SIZE", " Width = " + w + "  Height = " + h);
	}

	@SuppressLint("NewApi")
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		if (changedCallback_ != null) {
			changedCallback_.onAction(null);
		}
		try {
			if (mCamera == null) {
				return;
			}
			// from android documentation
			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}
			// stop preview before making changes
			try {
				mCamera.stopPreview();
			} catch (Exception e) {
				// ignore: tried to stop a non-existent preview
			}

			Camera.Parameters parameters = mCamera.getParameters();
			if (parameters.getSupportedFocusModes().contains(
					Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
				parameters
						.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			}
			List<Camera.Size> sizes = parameters.getSupportedPreviewSizes(); // Doesn't
																				// exist
																				// in
			wantedWidth_ = w;
			wantedHeight_ = h;// API

			previewSize_ = wantedWidth_ > wantedHeight_ ? getOptimalPreviewSize(
					sizes, wantedWidth_, wantedHeight_)
					: getOptimalPreviewSize(sizes, wantedHeight_, wantedWidth_);

			if (previewSize_.width > wantedWidth_
					|| previewSize_.height > wantedHeight_) {
				for (int i = 0; i < sizes.size(); i++) {
					Size s = sizes.get(i);
					if (s.width == previewSize_.width
							&& s.height == previewSize_.height) {
						if (i + 1 < sizes.size()) {
							previewSize_ = sizes.get(i + 1);
						}
						break;

					}
				}
			}

			Log.d("photography", "holder Width = " + wantedWidth_
					+ " holder Height = " + wantedHeight_);

			// parameters.set("orientation", "portrait");
			// parameters.setRotation(90);

			float scale = (float) wantedHeight_ / (float) previewSize_.height;
			// Log.d("SCALE", scale + "");
			parameters.setPreviewSize(previewSize_.width, previewSize_.height);
			int newWidth = (int) (previewSize_.width * scale);
			for (Camera.Size s : sizes) { // 1.6
				if (s.width == newWidth) {
					Log.d("equals", "holder Width = " + s.width
							+ " holder Height = " + s.height);
					previewSize_.width = s.width;
					previewSize_.height = s.height;
					// parameters.setPreviewSize(s.width, s.height);
					scale = (float) wantedHeight_ / (float) s.height;
					newWidth = (int) (s.width * scale);
				}
			}
			if (newWidth > wantedWidth_) {
				if (previewSize_.width < wantedWidth_) {
					newWidth = wantedWidth_;
				} else {
					// newWidth = previewSize_.width;
				}

			}
			Log.d("photography", "new Width = " + newWidth);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
					newWidth, wantedHeight_);
			Log.d("photography", "preview Width = " + previewSize_.width
					+ " preview Height = " + previewSize_.height);

			parameters.setPreviewSize(previewSize_.width, previewSize_.height);
			params.setMargins((wantedWidth_ / 2) - (newWidth / 2), 0, 0, 0);
			// Log.d("SET SIZE", " Width = "
			// + ((int) (previewSize_.width * scale)) + "  Height = "
			// + wantedHeight_);
			setLayoutParams(params);
			mCamera.setParameters(parameters);
			mCamera.setPreviewDisplay(mHolder);
			startPreview(mCamera);

		} catch (Exception ex) {
			ex.printStackTrace();
			new ExceptionHandler(ex, "Preview:surfaceChanged");
		}
	}

	@Override
	protected void onLayout(boolean flag, int i, int j, int k, int l) {
		// TODO Auto-generated method stub
	}

	// Sets callback to be called when camera is ready.
	public void setOnReadyCallback(ActionCallback<Void> readyCallback) {
		readyCallback_ = readyCallback;
	}

	// Sets callback that will be called when state of Preview View is changed.
	public void setOnPreviewChanged(ActionCallback<Void> changedCallback) {
		changedCallback_ = changedCallback;
	}

	// returns current size of preview.
	public Size getPreviewSize() {
		return previewSize_;
	}

	public Size getPictureSize() {
		return pictureSize_;
	}

	// @SuppressLint("NewApi")
	@SuppressLint("NewApi")
	public static void setCameraDisplayOrientation(Activity activity,
			int cameraId, android.hardware.Camera camera) {
		// android.hardware.Camera.CameraInfo info = new
		// android.hardware.Camera.CameraInfo();
		// android.hardware.Camera.getCameraInfo(cameraId, info);
		int result = 0;

		Class<?> cameraInfoClass;
		int cameraFacing = 0;
		int cameraOrientation = 0;
		try {
			cameraInfoClass = Class
					.forName("android.hardware.Camera$CameraInfo");
			Field faceCameraField = cameraInfoClass.getField("facing");
			Field orientationCameraField = cameraInfoClass
					.getField("orientation");
			Class<Camera> cameraClass = Camera.class;
			Class<?> camInfoParams[] = new Class[2];
			camInfoParams[0] = Integer.TYPE;
			camInfoParams[1] = cameraInfoClass;

			Method getCameraInfoMethod = cameraClass.getMethod("getCameraInfo",
					camInfoParams);
			Object cameraInfo = cameraInfoClass.newInstance();
			getCameraInfoMethod.invoke(null, cameraId, cameraInfo);
			cameraFacing = faceCameraField.getInt(cameraInfo);

			cameraOrientation = orientationCameraField.getInt(cameraInfo);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}
		if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (cameraFacing + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				degrees = 90;
			}

			result = (degrees - cameraOrientation + 360) % 360;
		}
		String info = "F=" + cameraFacing + "-O=" + cameraOrientation + "-R="
				+ result;
		Utils.reportFlurryEvent("PreviewInfo", info);
		setDisplayOrientation(camera, 90);
	}

	protected static void setDisplayOrientation(Camera camera, int angle) {
		Method downPolymorphic;
		try {
			downPolymorphic = camera.getClass().getMethod(
					"setDisplayOrientation", new Class[] { int.class });
			if (downPolymorphic != null)
				downPolymorphic.invoke(camera, new Object[] { angle });
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "SetDisplayOrientation");
		}
	}

	public void startPreview(Camera camera) {
		if (camera != null) {
			// setCameraDisplayOrientation(mContext, cameraId_, camera);
			camera.startPreview();
		}
	}

	SurfaceHolder mHolder;
	public Camera mCamera;
	BaseCameraPreviewActivity mContext;
	String processedFileName;
	Boolean mIsTakingPhoto;
	ActionCallback<byte[]> callback_;
	ActionCallback<byte[]> shutterCallback_;
	ActionCallback<byte[]> postPhotoCallback_;
	int cameraId_ = -1;
	ActionCallback<Void> readyCallback_;
	ActionCallback<Void> changedCallback_;
	Size previewSize_;
	Size pictureSize_;
	boolean shutterEnabled_ = true;
	boolean shootOnTouch_ = true;
	int volume_ = -1;
	TakePhotoTask takePhotoTask;
	int wantedWidth_;
	int wantedHeight_;
	private static final String LOG_TAG = "Preview";
}
