package com.wisesharksoftware.category_panel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.json.JSONException;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class CategoryPanel extends RelativeLayout {
	private ImageButton back;
	private static final String LOG_TAG = "CategoryPanel";
	private HorizontalScrollView horizontalScrollView;
	private LinearLayout linearLayout;
	private Context context;
	private AttributeSet attrs;
	private Structure structure;
	private int level = 0;
	private int currentCat = 0;
	private int currentItem = 0;
	private final int MAX_LEVEL = 1;
	private final int MIN_LEVEL = 0;
	private LinearLayout root;
	private OnItemListener onItemListener;
	private String resBack;
	private int backLeft;
	private int[] prePos = { -1, -1 };

	public LinearLayout getRoot() {
		return root;
	}

	public CategoryPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.attrs = attrs;
		this.context = context;
		structure = parseCategories();
		if (structure != null) {
			addViews();
		}
	}

	public void setOnItemListener(OnItemListener onItemListener_) {
		onItemListener = onItemListener_;
	}

	public CategoryPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CategoryPanel(Context context) {
		super(context);
	}

	private void addViews() {
		// load resource for button back
		resBack = getStringParam(R.styleable.CategoryPanel_res_image_back);
		Log.d(LOG_TAG, "resource name  = " + resBack);
		loadStandartViews();
		loadStructureViews();
	}

	@SuppressLint("NewApi")
	private void loadStandartViews() {
		// create linear layout root
		linearLayout = new LinearLayout(context);
		horizontalScrollView = new HorizontalScrollView(context);
		horizontalScrollView.setVerticalScrollBarEnabled(false);
		horizontalScrollView.setHorizontalScrollBarEnabled(false);
		horizontalScrollView.setFadingEdgeLength(0);
		horizontalScrollView.setHorizontalFadingEdgeEnabled(false);
		try {
			horizontalScrollView.setOverScrollMode(2);
		} catch (NoSuchMethodError e) {
			e.printStackTrace();
		}
		
		linearLayout.addView(horizontalScrollView);
		root = new LinearLayout(context);
		root.setOrientation(LinearLayout.HORIZONTAL);
		horizontalScrollView.addView(root);

		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
				(int) LayoutParams.WRAP_CONTENT,
				(int) LayoutParams.MATCH_PARENT);

		params2.gravity = Gravity.BOTTOM;

		horizontalScrollView.setLayoutParams(params2);
		this.addView(linearLayout);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				(int) LayoutParams.WRAP_CONTENT,
				(int) LayoutParams.MATCH_PARENT);
		
	//	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)linearLayout.getLayoutParams();

		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		//linearLayout.setLayoutParams(params);

	}

	private String getStringParam(int paramId) {
		String value = null;
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.CategoryPanel);
		final int N = a.getIndexCount();
		for (int i = 0; i < N; ++i) {
			int attr = a.getIndex(i);
			if (attr == paramId) {
				value = a.getString(attr);
			}
		}
		a.recycle();
		return value;
	}

	private Structure parseCategories() {
		Structure structure = null;
		String s;
		String value = getStringParam(R.styleable.CategoryPanel_structure);
		if (value == null) {
			return null;
		}
		int id = context.getResources().getIdentifier(value, "raw",
				context.getPackageName());
		InputStream inputStream = null;
		try {
			inputStream = getResources().openRawResource(id);
			byte[] reader = new byte[inputStream.available()];
			while (inputStream.read(reader) != -1) {
			}
			s = new String(reader);
			StructureParser parser = new StructureParser();
			try {
				structure = parser.parse(s);
				Log.d(LOG_TAG, "structure" + structure.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(LOG_TAG, e.getMessage());
				}
			}
		}
		return structure;
	}

	private void loadStructureViews() {
		Log.d(LOG_TAG, " level = " + level);
		switch (level) {
		// top level
		case 0:
			/*
			 * if (back != null){ back.setVisibility(View.GONE); }
			 */
			
			for (int i = structure.getCategories().size() - 1; i >= 0; i--) {
				Category category = structure.getCategories().get(i);
				addItemView(category.getName(),
						category.getImageOnResourceName(),
						category.getImageOffResourceName(), i);
			}
			break;
		case 1:
			final ViewItemsStatus status = new ViewItemsStatus(this);
			int leftMarginBack = status.getScreenWidth() - status.getLastPosX();
			int backIndex = root.getChildCount();
			createBackButton(leftMarginBack);
			animation(status.getScreenWidth(), backIndex);

			break;
		}
	}

	private void clearCategories(int backIndex) {
		try {
			if (root == null) {
				return;
			}
			for (int i = 0; i < backIndex; i++) {
				root.removeViewAt(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "clearCategories");
		}
	}

	private void animation(int x, int backIndex) {
		Run run = new Run(x, horizontalScrollView, backIndex);
		this.post(run);
	}

	public class Run implements Runnable {
		private int x;
		private HorizontalScrollView view;
		private int backIndex;

		public Run(int x, HorizontalScrollView view, int backIndex) {
			this.x = x;
			this.view = view;
			this.backIndex = backIndex;
		}

		public void run() {
			view.smoothScrollTo(x, 0);
			view.postDelayed(new Runnable() {

				@Override
				public void run() {
					clearCategories(backIndex);
					view.smoothScrollTo(-x, 0);
					root.removeView(back);
					((LinearLayout.LayoutParams) back.getLayoutParams()).leftMargin = 0;
					linearLayout.addView(back, 0);

					List<Item> items = structure.getCategories()
							.get(currentCat).getItems();
					for (int i = items.size() - 1; i >= 0; i--) {
						Item item = items.get(i);
						addItemView(item.getName(),
								item.getImageOnResourceName(),
								item.getImageOffResourceName(), i);
					}
				}
			}, 50);
		}
	}

	private void addItemView(String name, String imageOn, String imageOff,
			int id) {
		int resourceOff = context.getResources().getIdentifier(imageOff,
				"drawable", context.getPackageName());
		int resourceOn = context.getResources().getIdentifier(imageOn,
				"drawable", context.getPackageName());
		ItemView btn = new ItemView(context, resourceOn, resourceOff);
		btn.setId(id);
		btn.setTag(name);
		// btn.setBackgroundColor(0xFFFF0000);

		if (level != MIN_LEVEL && prePos[0] != -1 && prePos[1] != -1) {
			if (prePos[0] == currentCat && prePos[1] == id) {
				btn.setState(true);
			}
		}
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ItemView btn = ((ItemView) v);

				Log.d(LOG_TAG, "state = " + btn.getState());
				if (onItemListener != null) {

					if (level != MIN_LEVEL) {

						if (!btn.getState() && onItemListener.onItemSelected((String) v.getTag())) {
							
							btn.setState(true);
							if (prePos[0] == currentCat) {
								ItemView view = (ItemView) findViewById(prePos[1]);
								if (view != null) {
									view.setState(false);
								}
							}

							currentItem = v.getId();
							prePos[0] = currentCat;
							prePos[1] = currentItem;
						}

					} else {
						upLevel();
						currentCat = v.getId();
						loadStructureViews();
					}

				}
			}
		});

		// params.gravity = Gravity.CENTER_VERTICAL;
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
		  ((int) LayoutParams.MATCH_PARENT, (int)
		  LayoutParams.WRAP_CONTENT);
		float margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
		params.leftMargin = (int)margin;
		root.addView(btn, 0, params);
		
	}

	private void clearView() {
		// remove views
		// root.removeAllViews();
	}

	private void upLevel() {
		level++;
		if (level > MAX_LEVEL) {
			level = MAX_LEVEL;
		}
	}

	private void downLevel() {
		level--;
		if (level < MIN_LEVEL) {
			level = MIN_LEVEL;
		}
	}

	public interface OnItemListener {
		public boolean onItemSelected(String id);
	}

	public void createBackButton(int leftMargin) {
		if (back != null) {
			return;
		}
		// create button back
		back = new ImageButton(context);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
				downLevel();
				currentCat = 0;
				// clearView();
				// loadStructureViews();
				linearLayout.removeView(back);
				root.addView(back, 0);
				Log.d(LOG_TAG, "back clicked");
				loadStructureViews();
				backLeft = back.getLeft();
				back.setOnClickListener(null);
				CategoryPanel.this.post(new Runnable() {
					@Override
					public void run() {
						horizontalScrollView.scrollTo(back.getLeft(), 0);
						horizontalScrollView.smoothScrollTo(-back.getLeft(), 0);
						CategoryPanel.this.postDelayed(new Runnable() {

							@Override
							public void run() {
								int count = root.getChildCount();
								Log.d(LOG_TAG, "count = "  + count);
								// Find back index
								
								for (int j = 0; j < count; j++){
									if (root.getChildAt(j) == back){
										Log.d(LOG_TAG, "back index = "  + j);
										for (int i = j; i < count; i++) {
											root.removeViewAt(j);
											// backLeft = 0;
											back = null;
											// System.gc();
										}
										break;
									}
								}
								
							}
						}, 50);
					}
				});
				} catch (Exception e) {
					new ExceptionHandler(e, "CategoryPanelBack");
					e.printStackTrace();
					back = null;
				}
			}
		});

		if (resBack != null) {
			int resourceId = context.getResources().getIdentifier(resBack,
					"drawable", context.getPackageName());
			// back.setImageResource(resourceId);
			back.setBackgroundResource(resourceId);

		}
		root.addView(back);
		((LinearLayout.LayoutParams) back.getLayoutParams()).leftMargin = leftMargin;

	}
	
	public boolean backToCategories(){
		if (level == MAX_LEVEL){
			back.performClick();
			return true;
		} else{
			return false;
		}
	}
}
