package com.wisesharksoftware.app_photoeditor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.MarketingHelper;
import com.smsbackupandroid.lib.SettingsHelper;
import com.wisesharksoftware.core.Utils;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.ui.BaseActivity;

public class DocumentGalleryActivity extends BaseActivity {
	public static final int SELECT_PHOTO = 1;
	private static final int NUM_COLUMNS = 2;
	private static final String FILE_SCHEMA = "file://";

	public static String documentPath;
	public static String allDocumentsPath;

	public String[] imageUrls;
	private ImageLoader imageLoader;
	private ImagePagerAdapter imagePagerAdapter;
	DisplayImageOptions options;
	private List<Integer> pageCount = new ArrayList<Integer>();
	private AdView adView;

	// ViewPager pager;
	GridView pager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initImageLoader(getApplicationContext());

		adView = (AdView) findViewById(R.id.adView);
		imageLoader = ImageLoader.getInstance();
		// imageLoader.init(configuration)
		Intent intent = getIntent();
		if (intent != null) {
			documentPath = intent.getStringExtra("documentPath");
			allDocumentsPath = intent.getStringExtra("allDocumentsPath");
		} else {
			finish();
		}
		if (!IsAdsHidden()) {
			adView.setVisibility(View.GONE);
			AdRequest adRequest = new AdRequest.Builder().build();
		    adView.loadAd(adRequest);
		}
		imageUrls = formDocumentImages(allDocumentsPath);
		Options decodingOptions = new Options();
		decodingOptions.inSampleSize = 4;
		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.ic_error)
				.resetViewBeforeLoading(true).decodingOptions(decodingOptions)
				.cacheInMemory(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
				.build();

		ImageButton ivOpenCamera = (ImageButton) findViewById(R.id.ivOpenCamera);
		ivOpenCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(DocumentGalleryActivity.this,
						CameraPreviewActivity.class);
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

		imagePagerAdapter = new ImagePagerAdapter();

		pager = (GridView) findViewById(R.id.gvDocumentGallery);
		((GridView) pager).setAdapter(imagePagerAdapter);
	}

	public boolean IsAdsHidden() {
		if (getResources().getBoolean(R.bool.show_ads) == false) {
			return true;
		}
		return isFullVersion()
				|| SettingsHelper.getBoolean(this, "remove_ads", false)
				|| MarketingHelper.isTrialPeriod(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		File fileall = new File(allDocumentsPath);
		File file = new File(documentPath);
		if (!fileall.exists() || !file.exists()) {
			Intent intent = new Intent(DocumentGalleryActivity.this,
					CameraPreviewActivity.class);
			startActivity(intent);
			finish();
		}
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

	@SuppressWarnings("unchecked")
	public String[] formDocumentImages(String folder) {
		File dir = new File(folder);
		if (!dir.exists()) {
			String[] res = new String[0];
			return res;
		}
		File[] dirs = dir.listFiles();
		Arrays.sort(dirs, new Comparator<File>() {
			public int compare(File o1, File o2) {

				return  -1*(new Long(o1.lastModified())
								.compareTo(new Long(o2.lastModified())));
			}

		});
		List<String> resList = new ArrayList<String>();
		pageCount.clear();
		for (int i = 0; i < dirs.length; i++) {
			if (dirs[i].isDirectory()) {
				File[] files = dirs[i].listFiles();

				boolean added = false;
				int pageNumber = 0;
				for (int j = 0; j < files.length; j++) {
					int dot = files[j].getPath().lastIndexOf(".");
					String extension = files[j].getPath().substring(dot);
					if (extension.equals(".jpg") || extension.equals(".jpeg")
							|| extension.equals(".bmp")
							|| extension.equals(".png")) {
						if (!files[j].getName().contains("temp")) {
							if (!added) {
								resList.add(FILE_SCHEMA + files[j].getPath());
								added = true;
							}
							pageNumber++;
						}
					}
				}
				pageCount.add(pageNumber);
			}
		}

		// Collections.sort(resList, new Comparator<String>() {
		// @Override
		// public int compare(String text1, String text2) {
		// String file1 = getFileName(text1);
		// if (file1.startsWith("_")) {
		// file1 = file1.substring(1);
		// }
		// String file2 = getFileName(text2);
		// if (file2.startsWith("_")) {
		// file2 = file2.substring(1);
		// }
		// return file1.compareToIgnoreCase(file2);
		// }
		// });

		String[] res = new String[resList.size()];
		for (int i = 0; i < resList.size(); i++) {
			res[i] = resList.get(i);
		}
		return res;
	}

	private String removeSchema(String path) {
		String fileName;
		if (path.startsWith(FILE_SCHEMA)) {
			fileName = path.substring(FILE_SCHEMA.length());
		} else {
			fileName = path;
		}
		return fileName;
	}

	private String getFolderFileName(String path) {
		String fileName = removeSchema(path);
		File file = new File(fileName);
		fileName = file.getParentFile().getName();
		return fileName;
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
	public void onBackPressed() {
		File file = new File(documentPath);
		if (file.exists()) {
			Intent intent = new Intent(DocumentGalleryActivity.this,
					EditPagesActivity.class);
			intent.putExtra("documentPath", documentPath);
			startActivity(intent);
			finish();
		} else {
			Intent intent = new Intent(DocumentGalleryActivity.this,
					CameraPreviewActivity.class);
			startActivity(intent);
			finish();
		}
	}

	public class ImagePagerAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return imageUrls.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final ViewHolder holder;
			View view = convertView;
			if (view == null) {
				view = getLayoutInflater().inflate(
						R.layout.item_grid_image_gallery, parent, false);
				holder = new ViewHolder();
				holder.imageView = (ImageView) view.findViewById(R.id.image);
				holder.progressBar = (ProgressBar) view
						.findViewById(R.id.progress);
				holder.tvPageNumber = (TextView) view
						.findViewById(R.id.tvPageNumber);
				holder.tvDocumentName = (TextView) view
						.findViewById(R.id.tvDocumentName);
				holder.ibDeletePage = (ImageButton) view
						.findViewById(R.id.ibDeletePage);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			if (pageCount.get(position) != 1) {
				holder.tvPageNumber.setText(pageCount.get(position) + " pages");
			} else {
				holder.tvPageNumber.setText(pageCount.get(position) + " page");
			}
			holder.tvDocumentName
					.setText(getFolderFileName(imageUrls[position]));
			holder.ibDeletePage.setTag(position);
			// holder.ibDeletePage.setOnClickListener(this);
			holder.ibDeletePage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					imageLoader.stop();
					File oldDir = new File(imageUrls[position]
							.substring(FILE_SCHEMA.length()));
					oldDir = oldDir.getParentFile();
					deleteDirectory(oldDir);

					imageUrls = formDocumentImages(allDocumentsPath);
					if (imageUrls.length != 0) {
						imagePagerAdapter.notifyDataSetChanged();
					} else {
						Intent intent = new Intent(
								DocumentGalleryActivity.this,
								CameraPreviewActivity.class);
						startActivity(intent);
						finish();
					}
				}
			});

			holder.imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(DocumentGalleryActivity.this,
							EditPagesActivity.class);
					File file = new File(removeSchema(imageUrls[position]));
					intent.putExtra("documentPath", file.getParent());
					startActivity(intent);
					finish();
				}
			});

			view.setMinimumHeight((int) (pager.getWidth() / NUM_COLUMNS * 4 / 3.0));

			imageLoader.displayImage(imageUrls[position], holder.imageView,
					options, new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							// holder.progressBar.setProgress(0);
							holder.progressBar.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							holder.progressBar.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							holder.progressBar.setVisibility(View.GONE);
						}
					}, new ImageLoadingProgressListener() {
						@Override
						public void onProgressUpdate(String imageUri,
								View view, int current, int total) {
						}
					});

			return view;
		}

		class ViewHolder {
			ImageView imageView;
			ProgressBar progressBar;
			TextView tvPageNumber;
			TextView tvDocumentName;
			ImageButton ibDeletePage;
		}

	}

	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
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
					int dot = selectedImage.lastIndexOf(".");
					String extension = selectedImage.substring(dot);
					String folderName = getString(R.string.photoFolder);
					String newFileName = Utils.getFolderPath(folderName) + "/"
							+ Utils.getDateFileName() + extension;
					EditPagesActivity.copyFile(selectedImage, newFileName);

					Intent intent = new Intent(DocumentGalleryActivity.this,
							DocumentTouchActivity.class);
					List<Uri> a = new ArrayList<Uri>();
					a.add(Uri.parse(newFileName));
					intent.putExtra(ChooseProcessingActivity.INTENT_PARAM_URIS,
							a.toArray(new Uri[a.size()]));

					// intent.putExtra(
					// EditPagesActivity.INTENT_PARAM_DOCUMENT_PATH,
					// documentPath);

					startActivity(intent);
				}
			}
		}
	}

	@Override
	protected int getRootLayout() {
		return R.layout.document_gallery;
	}

	@Override
	protected int getPortraitLayout() {
		return R.layout.document_gallery;
	}

	@Override
	protected int getLandscapeLayout() {
		return R.layout.document_gallery;
	}

	@Override
	protected String getFlurryKey() {
		return getString(R.string.flurryApiKey);
	}
}