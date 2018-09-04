package com.wisesharksoftware.app_photoeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.MarketingHelper;
import com.smsbackupandroid.lib.SettingsHelper;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Image2;
import com.wisesharksoftware.core.ImageProcessing;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Presets;
import com.wisesharksoftware.core.ProcessingCallback;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.FlipRotateFilter;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.ui.BaseActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class EditPagesActivity extends BaseActivity {
	public static final int SELECT_PHOTO = 1;
	private static final String FILE_SCHEMA = "file://";

	public static final String INTENT_PARAM_DOCUMENT_PATH = "documentPath";
	public static String documentFolder;
	public static String documentPath;

	public String[] imageUrls;
	private ImageLoader imageLoader;
	private ImagePagerAdapter imagePagerAdapter;
	private TextView tvEditDocument;
	private TextView tvEditDocumentPages;
	private ImageView imgEditPagesLockScreen;
	private ProgressBar pbEditPagesProcessing;
	private AdView adView;
	private DisplayImageOptions options;

	ViewPager pager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initImageLoader(getApplicationContext());
		adView = (AdView) findViewById(R.id.adView);
		imageLoader = ImageLoader.getInstance();
		Intent intent = getIntent();
		if (intent != null) {
			documentPath = intent.getStringExtra(INTENT_PARAM_DOCUMENT_PATH);
			File file = new File(documentPath);
			documentFolder = file.getName();
		} else {
			finish();
		}
		if (!IsAdsHidden()) {
			adView.setVisibility(View.GONE);
			AdRequest adRequest = new AdRequest.Builder().build();
		    adView.loadAd(adRequest);
		}
		imageLoader.clearDiscCache();
		imageLoader.clearMemoryCache();
		imageUrls = formDocumentImages(documentPath);

		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.ic_error)
				.resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300)).build();

		pager = (ViewPager) findViewById(R.id.pagerEditDocument);
		imagePagerAdapter = new ImagePagerAdapter(imageUrls);
		pager.setAdapter(imagePagerAdapter);
		pager.setCurrentItem(imageUrls.length - 1);// set pager position to last
													// image
		pager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// Toast.makeText(getApplicationContext(), imageUrls[position],
				// Toast.LENGTH_LONG).show();
				updateDocumentInfo(documentFolder, pager.getCurrentItem(),
						imageUrls.length);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		tvEditDocument = (TextView) findViewById(R.id.tvEditDocument);
		tvEditDocumentPages = (TextView) findViewById(R.id.tvEditDocumentPages);

		ImageButton ivShareDocument = (ImageButton) findViewById(R.id.ivShareDocument);
		ivShareDocument.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				lockScreen();
				String pdfFileName = documentFolder + ".pdf";
				List<String> imageFileNames = new ArrayList<String>();
				for (int i = 0; i < imageUrls.length; i++) {
					imageFileNames.add(imageUrls[i].substring(FILE_SCHEMA
							.length()));
				}
				// ConvertUtils.generatePdf(imageFileNames, documentPath + "/" +
				// pdfFileName);
				PDFGeneratorTask PDFGenerator = new PDFGeneratorTask(
						imageFileNames, documentPath + "/" + pdfFileName);
				PDFGenerator.execute();
			}
		});

		ImageButton ivOpenCamera = (ImageButton) findViewById(R.id.ivOpenCamera);
		ivOpenCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditPagesActivity.this,
						CameraPreviewActivity.class);
				intent.putExtra(INTENT_PARAM_DOCUMENT_PATH, documentPath);
				startActivity(intent);
				finish();
			}
		});

		ImageButton ivOpenGallery = (ImageButton) findViewById(R.id.ivOpenGallery);
		ivOpenGallery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectPhoto();
			}
		});

		ImageButton ivRotate = (ImageButton) findViewById(R.id.ivRotate);
		ivRotate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				lockScreen();
				String imageFileName = imageUrls[pager.getCurrentItem()]
						.substring(FILE_SCHEMA.length());
				rotateImage(imageFileName);
			}
		});

		ImageButton ivEditDocument = (ImageButton) findViewById(R.id.ivEditDocument);
		ivEditDocument.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditPagesActivity.this,
						EditDocumentActivity.class);
				intent.putExtra(INTENT_PARAM_DOCUMENT_PATH, documentPath);
				startActivity(intent);
				finish();
			}
		});

		ImageButton ivOpenDocuments = (ImageButton) findViewById(R.id.ivOpenDocuments);
		ivOpenDocuments.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditPagesActivity.this,
						DocumentGalleryActivity.class);
				File file = new File(documentPath);

				intent.putExtra(INTENT_PARAM_DOCUMENT_PATH, documentPath);
				intent.putExtra("allDocumentsPath", file.getParent());

				startActivity(intent);
				finish();
			}
		});

		imgEditPagesLockScreen = (ImageView) findViewById(R.id.imgEditPagesLockScreen);
		pbEditPagesProcessing = (ProgressBar) findViewById(R.id.pbEditPagesProcessing);
		updateDocumentInfo(documentFolder, pager.getCurrentItem(),
				imageUrls.length);
	}

	@Override
	protected void onResume() {
		super.onResume();
		File file = new File(documentPath);
		if (!file.exists()) {
			Intent intent = new Intent(EditPagesActivity.this,
					CameraPreviewActivity.class);
			startActivity(intent);
			finish();
		}
	}

	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		ImageLoader.getInstance().init(config);
	}

	public String[] formDocumentImages(String folder) {
		File dir = new File(folder);
		if (!dir.exists()) {
			String[] res = new String[0];
			return res;
		}

	

		File[] files = dir.listFiles();
		
		Arrays.sort(files, new Comparator<File>() {

			@Override
			public int compare(File f1, File f2) {
				if (f1 != null && f2 != null) {

					String fullPath1 = f1.getAbsolutePath();
					String fullPath2 = f2.getAbsolutePath();
					if (fullPath1.equals(fullPath2)) {
						return 0;
					}

					return fullPath1.compareTo(fullPath2);

				}

				return 0;
			}
		});
		
		List<String> resList = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				int dot = files[i].getPath().lastIndexOf(".");
				String extension = files[i].getPath().substring(dot);
				if (extension.equals(".jpg") || extension.equals(".jpeg")
						|| extension.equals(".bmp") || extension.equals(".png")) {
					if (!files[i].getName().contains("temp")) {
						resList.add(FILE_SCHEMA + files[i].getPath());
					}
				}
			}
		}

		String[] res = new String[resList.size()];
		for (int i = 0; i < resList.size(); i++) {
			res[i] = resList.get(i);
		}
		return res;
	}

	private String getFileName(String path) {
		String fileName;
		if (path.startsWith(FILE_SCHEMA)) {
			fileName = path.substring(FILE_SCHEMA.length());
		} else {
			fileName = path;
		}
		File file = new File(fileName);
		fileName = file.getName();
		// int dot = fileName.lastIndexOf(".");
		// fileName = fileName.substring(1, dot);
		return fileName;
	}

	private void selectPhoto() {
		try {
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");

			startActivityForResult(photoPickerIntent, SELECT_PHOTO);
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "selectPhoto");
		}
	}

	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent returnedIntent) {
		super.onActivityResult(requestCode, resultCode, returnedIntent);
		switch (requestCode) {
		case SELECT_PHOTO:
			if (resultCode == RESULT_OK) {
				String selectedImage = null;
				if (returnedIntent != null && returnedIntent.getData() != null) {
					selectedImage = getRealPathFromURI(this,
							returnedIntent.getData());
					//Log.d("AAA", "selectedImage = " + selectedImage);
					int dot = selectedImage.lastIndexOf(".");
					String extension = selectedImage.substring(dot);
					String folderName = getString(R.string.photoFolder);
					String newFileName = Utils.getFolderPath(folderName) + "/"
							+ Utils.getDateFileName() + extension;
					EditPagesActivity.copyFile(selectedImage, newFileName);

					Intent intent = new Intent(EditPagesActivity.this,
							DocumentTouchActivity.class);
					List<Uri> a = new ArrayList<Uri>();
					a.add(Uri.parse(newFileName));
					intent.putExtra(ChooseProcessingActivity.INTENT_PARAM_URIS,
							a.toArray(new Uri[a.size()]));
					intent.putExtra(
							EditPagesActivity.INTENT_PARAM_DOCUMENT_PATH,
							documentPath);
					startActivity(intent);
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, CameraPreviewActivity.class);
		// intent.putExtra(INTENT_PARAM_DOCUMENT_PATH, documentPath);
		startActivity(intent);
		finish();
	}

	public static boolean copyFile(String filename, String toFileName) {
		boolean result = true;
		InputStream in = null;
		OutputStream out = null;
		try {
			File outFile = new File(toFileName);
			in = new FileInputStream(filename);
			out = new FileOutputStream(outFile);
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} catch (IOException e) {
			result = false;
			Utils.reportFlurryEvent("Failed to copy file", e.toString());
			Log.e("AssetsUtils", "Failed to copy file: " + filename, e);
		}
		return result;
	}

	private static void copyFile(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	private class ImagePagerAdapter extends PagerAdapter {

		private String[] images;
		private LayoutInflater inflater;

		ImagePagerAdapter(String[] images) {
			this.images = images;
			inflater = getLayoutInflater();
		}

		public void setImages(String[] images) {
			this.images = images;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return images.length;
		}

		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = inflater.inflate(R.layout.item_pager_image,
					view, false);
			assert imageLayout != null;
			ImageView imageView = (ImageView) imageLayout
					.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout
					.findViewById(R.id.loading);

			imageLoader.displayImage(images[position], imageView, options,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							spinner.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							String message = null;
							switch (failReason.getType()) {
							case IO_ERROR:
								message = "Input/Output error";
								break;
							case DECODING_ERROR:
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED:
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY:
								message = "Out Of Memory error";
								break;
							case UNKNOWN:
								message = "Unknown error";
								break;
							}
							Log.d("imageloader", "error = " + message);

							spinner.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							spinner.setVisibility(View.GONE);
						}
					});

			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

	private class PDFGeneratorTask extends AsyncTask<Void, Void, Void> {
		List<String> imageFileNames;
		String pdfFileName;

		public PDFGeneratorTask(List<String> imageFileNames, String pdfFileName) {
			this.imageFileNames = imageFileNames;
			this.pdfFileName = pdfFileName;
		}

		@Override
		protected Void doInBackground(Void... params) {
			ConvertUtils.generatePdf(imageFileNames, pdfFileName);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			unlockScreen();
			Intent share = new Intent(Intent.ACTION_SEND);
			share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			share.setType("application/pdf");
			//Log.d("AAA", "onPostExecute pdfFileName = " + pdfFileName);
			share.putExtra(Intent.EXTRA_STREAM,
					Uri.parse(FILE_SCHEMA + pdfFileName));
			startActivity(share);
		}
	}

	private String createRotateJSONPreset() {
		ArrayList<Filter> FilterArray = null;
		Filter[] filters = null;
		ArrayList<Preset> PresetArray;
		Preset[] presets;

		PresetArray = new ArrayList<Preset>();

		FlipRotateFilter filterFlipRotate = new FlipRotateFilter();
		filterFlipRotate.setAngle(90);
		filterFlipRotate.setFlipHorizontal(false);
		filterFlipRotate.setFlipVertical(false);

		FilterArray = new ArrayList<Filter>();
		FilterArray.add(filterFlipRotate);

		filters = new Filter[FilterArray.size()];
		FilterArray.toArray(filters);

		Preset preset = new Preset();
		preset.setFilters(filters);

		PresetArray.add(preset);

		presets = new Preset[PresetArray.size()];
		PresetArray.toArray(presets);

		Presets effectsPreset = new Presets(null, presets, null);

		String presetsJson = effectsPreset.convertToJSON();
		Log.d("processing", presetsJson);

		return presetsJson;
	}

	private void rotateImage(String fileName) {
		// Log.d("AAA", "rotate image with filename = " + fileName);
		// int dot = fileName.lastIndexOf(".");
		// String outFile = fileName.replace(fileName.substring(dot), "_90" +
		// fileName.substring(dot));
		//Log.d("AAA", "rotate image with filename = " + fileName);
		File file = new File(fileName);
		String outFile = file.getName();

		if (outFile.contains("_")) {
			outFile = file.getParentFile().getPath() + "/"
					+ outFile.replace("_", "");
		} else {
			outFile = file.getParentFile().getPath() + "/"
					+ outFile.replace(".jpg", "_.jpg");
		}
		//Log.d("AAA", "rotate image outFile = " + outFile);

		rotateImage(fileName, outFile);
	}

	private void rotateImage(final String inFile, String outFile) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(inFile, options);
		int width = options.outWidth;
		int height = options.outHeight;

		ImageProcessing processing = new ImageProcessing(
				getApplicationContext(), createRotateJSONPreset(), width,
				height, new ProcessingCallback() {

					@Override
					public void onSuccess(String outFileName) {
						Log.d("processImage", "onSuccess");
						File oldFile = new File(inFile);
						oldFile.delete();
						imageUrls = formDocumentImages(documentPath);
						imagePagerAdapter.setImages(imageUrls);
						imageLoader.clearDiscCache();
						imageLoader.clearMemoryCache();
						imagePagerAdapter.notifyDataSetChanged();
						updateDocumentInfo(documentFolder,
								pager.getCurrentItem(), imageUrls.length);
						unlockScreen();
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onFail(Throwable e) {
						unlockScreen();
					}

					@Override
					public void onBitmapCreatedOpenCV() {
					}

					@Override
					public void onBitmapCreated(Image2 bitMap) {
					}

					@Override
					public void onBitmapCreated(Bitmap bitMap) {
					}

					@Override
					public void onCancelled() {
						// TODO Auto-generated method stub
						
					}
				});
		List<String> inFiles = new ArrayList<String>();
		inFiles.add(inFile);
		processing.processPictureAsync(inFiles, outFile);
	}

	public boolean IsAdsHidden() {
		if (getResources().getBoolean(R.bool.show_ads) == false) {
			return true;
		}
		return isFullVersion()
				|| SettingsHelper.getBoolean(this, "remove_ads", false)
				|| MarketingHelper.isTrialPeriod(this);
	}

	private void updateDocumentInfo(String documentName, int currPage,
			int pageCount) {
		tvEditDocument.setText("      " + documentName + "      ");
		tvEditDocumentPages.setText((currPage + 1) + " of " + pageCount);
	}

	private void lockScreen() {
		imgEditPagesLockScreen.setVisibility(View.VISIBLE);
		pbEditPagesProcessing.setVisibility(View.VISIBLE);
	}

	private void unlockScreen() {
		imgEditPagesLockScreen.setVisibility(View.INVISIBLE);
		pbEditPagesProcessing.setVisibility(View.INVISIBLE);
	}

	@Override
	protected int getRootLayout() {
		return R.layout.edit_pages;
	}

	@Override
	protected int getPortraitLayout() {
		return R.layout.edit_pages;
	}

	@Override
	protected int getLandscapeLayout() {
		return R.layout.edit_pages;
	}

	@Override
	protected String getFlurryKey() {
		return getString(R.string.flurryApiKey);
	}
}