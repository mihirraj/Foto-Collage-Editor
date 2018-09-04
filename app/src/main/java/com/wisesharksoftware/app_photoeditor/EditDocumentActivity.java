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

import net.pocketmagic.android.ccdyngridview.DragController;
import net.pocketmagic.android.ccdyngridview.DynGridView;
import net.pocketmagic.android.ccdyngridview.IDynamicAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
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
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.smsbackupandroid.lib.MarketingHelper;
import com.smsbackupandroid.lib.SettingsHelper;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.ui.BaseActivity;

public class EditDocumentActivity extends BaseActivity {
	private static final int NUM_COLUMNS = 3;
	private static final String FILE_SCHEMA = "file://";

	public static String documentFolder;
	public static String documentPath;

	public String[] imageUrls;
	private ImageLoader imageLoader;
	private ImagePagerAdapter imagePagerAdapter;
	public EditText edDocumentName;
	private DisplayImageOptions options;
	private AdView adView;

	// ViewPager pager;
	DynGridView pager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initImageLoader(getApplicationContext());
		adView = (AdView) findViewById(R.id.adView);
		imageLoader = ImageLoader.getInstance();
		// imageLoader.init(configuration)
		Intent intent = getIntent();
		if (intent != null) {
			documentPath = intent.getStringExtra("documentPath");
			File file = new File(documentPath);
			documentFolder = file.getName();
		} else {
			finish();
		}
		if (!IsAdsHidden()) {
			adView.setVisibility(View.GONE);
			AdRequest adRequest = new AdRequest.Builder().build();
		    //adView.loadAd(adRequest);
		}
		imageLoader.clearDiscCache();
		imageLoader.clearMemoryCache();
		imageUrls = formDocumentImages(documentPath);
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

		imagePagerAdapter = new ImagePagerAdapter();

		pager = (DynGridView) findViewById(R.id.gridview);
		((DynGridView) pager).setAdapter(imagePagerAdapter);
		DragController dragController = new DragController(this);

		pager.setDragController(dragController);
		edDocumentName = (EditText) findViewById(R.id.edDocumentName);
		edDocumentName.setText(documentFolder);
		InputFilter filter = new InputFilter() {

			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (!Character.isLetterOrDigit(source.charAt(i))
							&& !(source.charAt(i) == '_')
							&& !(source.charAt(i) == ' ')) {
						return "";
					}
				}
				return null;
			}
		};
		edDocumentName.setFilters(new InputFilter[] { filter });

		ImageButton ivDone = (ImageButton) findViewById(R.id.ivDone);
		ivDone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO rename folder
				documentPath = renameDocument(edDocumentName.getText()
						.toString());
				Intent intent = new Intent(EditDocumentActivity.this,
						EditPagesActivity.class);
				intent.putExtra("documentPath", documentPath);
				startActivity(intent);
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		File file = new File(documentPath);
		if (!file.exists()) {
			Intent intent = new Intent(EditDocumentActivity.this,
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
			int dot = files[i].getPath().lastIndexOf(".");
			String extension = files[i].getPath().substring(dot);
			if (extension.equals(".jpg") || extension.equals(".jpeg")
					|| extension.equals(".bmp") || extension.equals(".png")) {
				if (!files[i].getName().contains("temp")) {
					resList.add(FILE_SCHEMA + files[i].getPath());
				}
			}
		}
		// Collections.sort(resList);
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

	public String renameDocument(String newDocumentName) {
		if (newDocumentName.equals("")) {
			return documentPath;
		}
		File file = new File(documentPath);
		String parent = file.getParent();
		File file2 = new File(parent + "/" + newDocumentName);
		file.renameTo(file2);
		return file2.getPath();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(EditDocumentActivity.this,
				EditPagesActivity.class);
		documentPath = renameDocument(edDocumentName.getText().toString());
		intent.putExtra("documentPath", documentPath);
		startActivity(intent);
		finish();
	}

	public class ImagePagerAdapter extends BaseAdapter implements
			IDynamicAdapter {
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
				view = getLayoutInflater().inflate(R.layout.item_grid_image,
						parent, false);
				holder = new ViewHolder();
				holder.imageView = (ImageView) view.findViewById(R.id.image);
				holder.progressBar = (ProgressBar) view
						.findViewById(R.id.progress);
				holder.tvPageNumber = (TextView) view
						.findViewById(R.id.tvPageNumber);
				holder.ibDeletePage = (ImageButton) view
						.findViewById(R.id.ibDeletePage);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.tvPageNumber.setText("   " + (position + 1) + "   ");
			holder.ibDeletePage.setTag(position);
			// holder.ibDeletePage.setOnClickListener(this);
			holder.ibDeletePage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					imageLoader.stop();
					File oldFile = new File(imageUrls[position]
							.substring(FILE_SCHEMA.length()));
					oldFile.delete();
					imageUrls = formDocumentImages(documentPath);
					if (imageUrls.length != 0) {
						imagePagerAdapter.notifyDataSetChanged();
					} else {
						Intent intent = new Intent(EditDocumentActivity.this,
								CameraPreviewActivity.class);
						startActivity(intent);
						finish();
					}
				}
			});
			view.setOnLongClickListener((OnLongClickListener) parent);
			view.setOnTouchListener((OnTouchListener) parent);
			view.setMinimumHeight(pager.getWidth() / NUM_COLUMNS);

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
					});

			return view;
		}

		class ViewHolder {
			ImageView imageView;
			ProgressBar progressBar;
			TextView tvPageNumber;
			ImageButton ibDeletePage;
		}

		@Override
		public void set(int position, Object item) {
			// TODO Auto-generated method stub

		}

		@Override
		public void remove(int position) {
			// TODO Auto-generated method stub

		}

		@Override
		public void remove(Object item) {
			// TODO Auto-generated method stub

		}

		@Override
		public void swapItems(int positionOne, int positionTwo) {
			Log.d("SWAP", imageUrls[positionOne] + "  ->  "
					+ imageUrls[positionTwo]);

			File from = new File(imageUrls[positionTwo].replace("file://", ""));
			File to = new File(imageUrls[positionTwo].replace("file://", "")
					+ "copy");
			from.renameTo(to);

			/*
			 * try { copy(from, to); } catch (IOException e) {
			 * e.printStackTrace(); }
			 */

			from = new File(imageUrls[positionOne].replace("file://", ""));
			to = new File(imageUrls[positionTwo].replace("file://", ""));
			from.renameTo(to);

			from = new File(imageUrls[positionTwo].replace("file://", "")
					+ "copy");
			to = new File(imageUrls[positionOne].replace("file://", ""));
			from.renameTo(to);

			String tempImage = imageUrls[positionOne];
			// int tempPage = pageCount.get(positionOne);
			imageUrls[positionOne] = imageUrls[positionTwo];
			// pageCount.add(positionOne, pageCount.get(positionTwo));
			imageUrls[positionTwo] = tempImage;
			// pageCount.add(positionTwo, tempPage);

			notifyDataSetChanged();

		}
	}

	public void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
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
	protected int getRootLayout() {
		return R.layout.edit_document;
	}

	@Override
	protected int getPortraitLayout() {
		return R.layout.edit_document;
	}

	@Override
	protected int getLandscapeLayout() {
		return R.layout.edit_document;
	}

	@Override
	protected String getFlurryKey() {
		return getString(R.string.flurryApiKey);
	}

	// private class ImagePagerAdapter extends PagerAdapter {
	//
	// private String[] images;
	// private LayoutInflater inflater;
	//
	// ImagePagerAdapter(String[] images) {
	// this.images = images;
	// inflater = getLayoutInflater();
	// }
	//
	// public void setImages(String[] images) {
	// this.images = images;
	// }
	//
	// @Override
	// public void destroyItem(ViewGroup container, int position, Object object)
	// {
	// container.removeView((View) object);
	// }
	//
	// @Override
	// public int getCount() {
	// return images.length;
	// }
	//
	// @Override
	// public Object instantiateItem(ViewGroup view, int position) {
	// View imageLayout = inflater.inflate(R.layout.item_pager_image, view,
	// false);
	// assert imageLayout != null;
	// ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
	// final ProgressBar spinner = (ProgressBar)
	// imageLayout.findViewById(R.id.loading);
	//
	// imageLoader.displayImage(images[position], imageView, options, new
	// SimpleImageLoadingListener() {
	// @Override
	// public void onLoadingStarted(String imageUri, View view) {
	// spinner.setVisibility(View.VISIBLE);
	// }
	//
	// @Override
	// public void onLoadingFailed(String imageUri, View view, FailReason
	// failReason) {
	// String message = null;
	// switch (failReason.getType()) {
	// case IO_ERROR:
	// message = "Input/Output error";
	// break;
	// case DECODING_ERROR:
	// message = "Image can't be decoded";
	// break;
	// case NETWORK_DENIED:
	// message = "Downloads are denied";
	// break;
	// case OUT_OF_MEMORY:
	// message = "Out Of Memory error";
	// break;
	// case UNKNOWN:
	// message = "Unknown error";
	// break;
	// }
	// Log.d("imageloader", "error = " + message);
	//
	// spinner.setVisibility(View.GONE);
	// }
	//
	// @Override
	// public void onLoadingComplete(String imageUri, View view, Bitmap
	// loadedImage) {
	// spinner.setVisibility(View.GONE);
	// }
	// });
	//
	// view.addView(imageLayout, 0);
	// return imageLayout;
	// }
	//
	// @Override
	// public boolean isViewFromObject(View view, Object object) {
	// return view.equals(object);
	// }
	//
	// @Override
	// public void restoreState(Parcelable state, ClassLoader loader) {
	// }
	//
	// @Override
	// public Parcelable saveState() {
	// return null;
	// }
	//
	// @Override
	// public int getItemPosition(Object object) {
	// return POSITION_NONE;
	// }
	// }
}