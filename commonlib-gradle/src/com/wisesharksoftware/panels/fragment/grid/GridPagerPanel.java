package com.wisesharksoftware.panels.fragment.grid;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.LauncherItemView.OnUnlockItemListener;
import com.wisesharksoftware.panels.fragment.IFragmentPanels;
import com.wisesharksoftware.util.MemoryCache;

public class GridPagerPanel extends RelativeLayout implements IFragmentPanels {

	private Context context;
	private GridPagerAdapter gridsPagerAdapter;
	private ViewPager gridsPager;
	private ArrayList<String> itemNames;
	private OnGridItemClickListener onGridItemClickListener;
	private HorizontalScrollView scrollView;
	private LinearLayout itemsRoot;
	private MemoryCache memoryCache;
	private List<LauncherItemView> items;
	private int currentItem = 0;
	private ProgressBar bar;
	private String path = "stickers";

	public interface OnGridItemClickListener {
		public void onClick(String buttonName, LauncherItemView item,
				boolean lock);
	}

	public GridPagerPanel(Context context) {
		this(context, null, 0);

	}

	public GridPagerPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public void clearCache() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				memoryCache.clear();
			}
		}).start();

	}

	public void setPath(String path) {
		this.path = path;
	}

	public GridPagerPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		initBar();
		itemNames = new ArrayList<String>();
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		int cacheSize = maxMemory / 8;
		memoryCache = new MemoryCache();
	}

	private void initBar() {
		bar = new ProgressBar(context);
		RelativeLayout.LayoutParams params = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(CENTER_IN_PARENT);
		bar.setVisibility(GONE);
		addView(bar, params);
	}

	public void setItemsWithAssetsFolders(List<LauncherItemView> lItems,
			int width, HorizontalScrollView scrollView, LinearLayout itemsRoot) {
		bar.setVisibility(VISIBLE);
		this.scrollView = scrollView;
		this.itemsRoot = itemsRoot;
		List<View> grids = new ArrayList<View>();
		this.items = new ArrayList<LauncherItemView>();

		for (int i = lItems.size() - 1; i >= 0; i--) {
			LauncherItemView item = lItems.get(i);
			item.setImageResource(0);
			PhotoEditorGridView g = new PhotoEditorGridView(context);
			g.setPath(path);
			PhotoEditorGridView.setWidth(width);
			if (grids.size() == 0) {
				g.setBar(bar);
			}
			int c = Color.argb(150, 255, 255, 255);
			try {
				c = Color.parseColor(item.getBackgroundColor());
			} catch (Exception e) {

			}
			g.setBackgroundColor(c);
			itemNames.add((String) item.getTag());
			/*
			 * g.setPathsFromAssetsFolder((String) item.getTag(),
			 * item.getProductIds(), item.getIdLockResourceImage(), item.locked,
			 * memoryCache);
			 */
			g.setPathsFromAssetsFolder(item, memoryCache);
			g.setOnGridItemClickListener(onGridItemClickListener);

			item.setOnUnlockItemListener(new OnUnlockItemListener() {

				@Override
				public void unlock(LauncherItemView item) {
					int index = items.indexOf(item);

					((PhotoEditorGridView) gridsPagerAdapter.getPages().get(
							index)).update();
				}
			});
			grids.add(g);
			item.setOff();
			this.items.add(item);
		}
		this.items.get(0).setOn();
		initGrids(grids);
	}

	private void initGrids(List<View> grids) {

		gridsPagerAdapter = new GridPagerAdapter(grids);
		gridsPager = new ViewPager(context);
		gridsPager.setAdapter(gridsPagerAdapter);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		gridsPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				((PhotoEditorGridView) gridsPagerAdapter.getPages().get(arg0))
						.invalidateViews();
				if (items != null && items.size() > arg0) {
					items.get(arg0).setOn();
					items.get(currentItem).setOff();

					if (items.get(arg0).getLeft() + items.get(arg0).getWidth() > getWidth()) {
						scrollView.scrollBy(items.get(arg0).getWidth() + 5, 0);
					} else if (arg0
							+ (getWidth() / (items.get(arg0).getWidth() + 22)) < items
							.size()) {
						scrollView.scrollBy(-(items.get(arg0).getWidth() + 5),
								0);
					}
					currentItem = arg0;
				}

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});
		addView(gridsPager, lp);
	}

	public void setOnGridItemClickListener(
			OnGridItemClickListener onGridItemClickListener) {
		this.onGridItemClickListener = onGridItemClickListener;
	}

	@Override
	public OnItemListener getOnItemListener() {

		return new OnItemListener() {

			@Override
			public void onLockedItemSelected(String buttonName,
					LauncherItemView item) {
				int i = itemNames.indexOf(buttonName);
				gridsPager.setCurrentItem(i);

			}

			@Override
			public boolean onItemSelected(LauncherItemView item,
					String buttonName, boolean state) {
				int i = itemNames.indexOf(buttonName);
				gridsPager.setCurrentItem(i);
				return false;
			}
		};
	}

}

class GridPagerAdapter extends PagerAdapter {

	List<View> pages = null;

	public GridPagerAdapter(List<View> pages) {
		this.pages = pages;
	}

	public List<View> getPages() {
		return pages;
	}

	@Override
	public Object instantiateItem(View collection, int position) {
		View v = pages.get(position);
		((ViewPager) collection).addView(v, 0);
		return v;
	}

	@Override
	public void destroyItem(View collection, int position, Object view) {
		((ViewPager) collection).removeView((View) view);
	}

	@Override
	public int getCount() {
		return pages.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}
}
