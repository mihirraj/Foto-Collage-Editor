package com.wisesharksoftware.panels.fragment.grid;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.smsbackupandroid.lib.R;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.fragment.grid.GridPagerPanel.OnGridItemClickListener;
import com.wisesharksoftware.util.MemoryCache;

public class PhotoEditorGridView extends GridView {

	private static final String TAG = PhotoEditorGridView.class.getSimpleName();
	private PhotoEditorGridAdapter adapter;
	private Context context;
	private static int width;
	private Options opt;
	private LayoutInflater inflater;
	private OnGridItemClickListener onGridItemClickListener;
	private ProgressBar bar;
	private LauncherItemView item;
	private String path;

	public PhotoEditorGridView(Context context) {
		this(context, null, 0, null);
		// TODO Auto-generated constructor stub
	}

	public PhotoEditorGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0, null);
		// TODO Auto-generated constructor stub
	}

	public PhotoEditorGridView(Context context, AttributeSet attrs,
			int defStyle, ArrayList<View> pathAssetsImages) {
		super(context, attrs, defStyle);
		this.context = context;

		initParams();
		if (pathAssetsImages != null) {
			// adapter = new PhotoEditorGridAdapter(context,
			// pathAssetsImages,resIdLockImage,width,ids);
			// setAdapter(adapter);
		}
	}

	public void setBar(ProgressBar bar) {
		this.bar = bar;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@SuppressLint("NewApi")
	private void initParams() {
		opt = new Options();
		opt.inDensity = 320;
		opt.inTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		setLayoutParams(lp);
		setNumColumns(3);

		setVerticalSpacing(5);
		setHorizontalSpacing(5);
	}

	public void update() {

		adapter.notifyDataSetChanged();
	}

	@SuppressLint("NewApi")
	public void setPathsFromAssetsFolder(LauncherItemView item,
			MemoryCache memoryCache) {
		this.item = item;
		new StartAdapter(new InfoForAdapter(memoryCache)).execute();

	}

	class InfoForAdapter {

		public MemoryCache memoryCache;

		public InfoForAdapter(MemoryCache memoryCache) {

			this.memoryCache = memoryCache;
		}
	}

	class StartAdapter extends AsyncTask<Void, Void, ArrayList<String>> {
		private InfoForAdapter info;

		public StartAdapter(InfoForAdapter info) {
			this.info = info;
		}

		@TargetApi(Build.VERSION_CODES.FROYO)
		@Override
		protected ArrayList<String> doInBackground(Void... params) {

			AssetManager m = context.getAssets();
			String[] files = null;
			try {
				files = m.list("sd/" + path + "/" + (String) item.getTag());

			} catch (IOException e) {
				Log.d(TAG, "Folder not exist");
			}
			if ((files != null) && (getContext() != null) && (getContext().getExternalFilesDir(null) != null)) {
				ArrayList<String> paths = new ArrayList<String>();

				for (String name : files) {

					StringBuilder sb = new StringBuilder(getContext()
							.getExternalFilesDir(null).toString())
							.append("/assets/sd/").append(path).append("/")
							.append((String) item.getTag())
							.append(File.separator).append(name);
					paths.add(sb.toString());

				}
				return paths;
			}
			return null;

		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			if (result != null) {
				adapter = new PhotoEditorGridAdapter(context, result, width,
						item.getProductIds(), info.memoryCache);

				setAdapter(adapter);
				adapter.setOnGridItemClickListener(onGridItemClickListener);
			}
			super.onPostExecute(result);
		}
	}

	public static void setWidth(int width) {
		PhotoEditorGridView.width = width;
	}

	public void setOnGridItemClickListener(
			OnGridItemClickListener onGridItemClickListener) {
		this.onGridItemClickListener = onGridItemClickListener;
		if (adapter != null) {
			adapter.setOnGridItemClickListener(onGridItemClickListener);
		}
	}

	class PhotoEditorGridAdapter extends BaseAdapter {

		private ArrayList<String> pathImages;
		private List<String> arrayIds;
		private Context context;
		private LayoutInflater inflater;
		private int width;

		private OnGridItemClickListener onGridItemClickListener;
		private Options opt;
		private MemoryCache memoryCache;
		private ImageLoader imageLoader;

		public PhotoEditorGridAdapter(Context context,
				ArrayList<String> pathAssetsImages, int width,
				List<String> arrayIds, MemoryCache memoryCache) {
			this.pathImages = pathAssetsImages;

			this.width = width;
			this.arrayIds = arrayIds;
			this.memoryCache = memoryCache;
			this.imageLoader = new ImageLoader(context);

			init(context);
		}

		public void setOnGridItemClickListener(
				OnGridItemClickListener onGridItemClickListener) {
			this.onGridItemClickListener = onGridItemClickListener;
		}

		@TargetApi(Build.VERSION_CODES.DONUT)
		private void init(Context context) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.context = context;
			opt = new Options();
			opt.inDensity = 320;
			opt.inTargetDensity = context.getResources().getDisplayMetrics().densityDpi;

		}

		@Override
		public int getCount() {
			return pathImages.size();
		}

		@Override
		public Object getItem(int arg0) {
			return pathImages.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if (convertView == null || convertView.getTag() == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.grid_view_image_item,
						null, false);
				holder.image = (ImageView) convertView
						.findViewById(R.id.grid_view_image_item_image);
				holder.bar = (ProgressBar) convertView
						.findViewById(R.id.grid_view_image_item_bar);
				holder.lockImage = (ImageView) convertView
						.findViewById(R.id.grid_view_image_item_image_lock);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.lockImage.setImageResource(item.getIdLockResourceImage());

			if (!item.locked) {
				holder.lockImage.setVisibility(View.GONE);
			} else {
				if (pos < item.getUnlockedCount()) {
					holder.lockImage.setVisibility(View.GONE);
				} else {
					holder.lockImage.setVisibility(View.VISIBLE);
				}
			}

			convertView.setMinimumHeight(width / 3);
			convertView.setMinimumWidth(width / 3);
			holder.image.setTag(pathImages.get(pos) + ";" + pos);

			holder.image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String path = (String) v.getTag();
					String strs = path.split(";")[0];
					int index = Integer.parseInt(path.split(";")[1]);
					if (onGridItemClickListener != null) {

						if (strs != null) {
							String name = strs;
							if (index < item.getUnlockedCount()) {
								onGridItemClickListener.onClick(name, item,
										false);
							} else {
								onGridItemClickListener.onClick(name, item,
										item.locked);
							}
						}
					}

				}
			});
			imageLoader.setWidth(width / 3);
			imageLoader.loadImage(pathImages.get(pos), holder.image);

			return convertView;
		}

		class ViewHolder {
			ImageView image;
			ProgressBar bar;
			ImageView lockImage;
		}

		public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
			if (getBitmapFromMemCache(key) == null) {
				memoryCache.put(key, bitmap);
			}
		}

		public Bitmap getBitmapFromMemCache(String key) {
			return memoryCache.get(key);
		}

		private Bitmap scaleBitmap(int width, String path) {

			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, width, width,
					true);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeFile(path, options);

			// return bitmap;
		}

		public int calculateInSampleSize(BitmapFactory.Options options,
				int reqWidth, int reqHeight, boolean strict) {
			// Raw height and width of image
			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;

			if (height > reqHeight || width > reqWidth) {

				final int halfHeight = height / 2;
				final int halfWidth = width / 2;

				while ((halfHeight / inSampleSize) > reqHeight
						&& (halfWidth / inSampleSize) > reqWidth) {
					inSampleSize *= 2;
				}

				long totalPixels = width * height / inSampleSize;

				final long totalReqPixelsCap = reqWidth * reqHeight * 2;

				while (totalPixels > totalReqPixelsCap) {
					inSampleSize *= 2;
					totalPixels /= 2;
				}
			}

			if (strict && inSampleSize >= 3) {
				inSampleSize = 4;
			}
			if (!strict && inSampleSize > 8) {
				inSampleSize = 8;
			}

			return inSampleSize;
		}

		class ImageLoader {

			private Map<ImageView, String> imageViews = Collections
					.synchronizedMap(new WeakHashMap<ImageView, String>());
			private ExecutorService executorService;
			private int width = 0;

			public ImageLoader(Context context) {
				executorService = Executors.newFixedThreadPool(5);
			}

			public void setWidth(int width) {
				this.width = width;
			}

			public void loadImage(String url, ImageView imageView) {
				imageViews.put(imageView, url);
				imageView.setLayoutParams(new RelativeLayout.LayoutParams(
						width, width));
				Bitmap bitmap = memoryCache.get(url);
				if (bitmap != null) {
					if (bar != null) {
						bar.setVisibility(GONE);
						bar = null;
					}
					imageView.setImageBitmap(bitmap);
				} else {
					imageView.setImageDrawable(null);
					ImageToLoad imageToLoad = new ImageToLoad(url, imageView);
					executorService.submit(new PhotosLoader(imageToLoad));
				}
			}

			private class ImageToLoad {
				public String url;
				public ImageView imageView;

				public ImageToLoad(String u, ImageView i) {
					url = u;
					imageView = i;
				}
			}

			boolean imageViewReused(ImageToLoad photoToLoad) {
				String tag = imageViews.get(photoToLoad.imageView);
				if (tag == null || !tag.equals(photoToLoad.url))
					return true;
				return false;
			}

			class PhotosLoader implements Runnable {
				ImageToLoad photoToLoad;

				PhotosLoader(ImageToLoad photoToLoad) {
					this.photoToLoad = photoToLoad;
				}

				@Override
				public void run() {
					if (imageViewReused(photoToLoad))
						return;

					Bitmap bmp = scaleBitmap(width, photoToLoad.url);
					memoryCache.put(photoToLoad.url, bmp);
					if (imageViewReused(photoToLoad))
						return;
					BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
					Activity a = (Activity) photoToLoad.imageView.getContext();
					a.runOnUiThread(bd);
				}
			}

			class BitmapDisplayer implements Runnable {
				Bitmap bitmap;
				ImageToLoad photoToLoad;

				public BitmapDisplayer(Bitmap b, ImageToLoad p) {
					bitmap = b;
					photoToLoad = p;
				}

				public void run() {
					if (imageViewReused(photoToLoad))
						return;
					if (bitmap != null) {
						if (bar != null) {
							bar.setVisibility(GONE);
							bar = null;
						}
						photoToLoad.imageView.setImageBitmap(bitmap);
					} else
						photoToLoad.imageView
								.setImageResource(android.R.drawable.ic_delete);
				}
			}

		}

	}
}
