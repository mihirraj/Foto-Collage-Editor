package com.wisesharksoftware.panels;

import java.util.ArrayList;
import java.util.List;

import com.smsbackupandroid.lib.R;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class LauncherPanel extends RelativeLayout implements IPanel {
	private HorizontalScrollView horizontalScrollView;
	private LinearLayout linearLayout;
	private Context context;
	private Structure structure;
	private LinearLayout root;
	private OnLaunchPanelListener onLaunchPanelListener;
	private PanelManager panelManager;
	private String panelName;
	private IPanel RootPanel;
	List<LauncherItemView> items = new ArrayList<LauncherItemView>();
	private boolean enableViews = true;
	private PanelInfo panelInfo;

	@Override
	public void setPanelInfo(PanelInfo pi) {
		panelInfo = pi;

	}
	@Override
	public String getAction() {
		return "launcher";
	}
	
	@Override
	public String getActionGroup() {
		return "";
	}
	
	@Override
	public int getPriority() {
		return 0;
	}

	public interface OnLaunchPanelListener {
		public void onLaunchPanelSelected(String nameLaunchPanel);
	}

	public LauncherPanel(Context context) {
		super(context);
		this.context = context;
	}

	public LauncherPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public LauncherPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public void setOnLaunchPanelListener(
			OnLaunchPanelListener onLaunchPanelListener_) {
		onLaunchPanelListener = onLaunchPanelListener_;
	}

	private void addViews() {
		loadStandartViews();
		loadStructureViews();
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void loadStandartViews() {
		// create linear layout root
		linearLayout = new LinearLayout(context);
		horizontalScrollView = new HorizontalScrollView(context);
		horizontalScrollView.setVerticalScrollBarEnabled(false);
		horizontalScrollView.setHorizontalScrollBarEnabled(false);
		horizontalScrollView.setFadingEdgeLength(0);
		horizontalScrollView.setHorizontalFadingEdgeEnabled(false);
		try {
			horizontalScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		} catch (NoSuchMethodError e) {
			e.printStackTrace();
		}

		linearLayout.addView(horizontalScrollView);

		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
				(int) LayoutParams.MATCH_PARENT,
				(int) LayoutParams.MATCH_PARENT);
		// params2.gravity = Gravity.CENTER;
		root = new LinearLayout(context);
		root.setOrientation(LinearLayout.HORIZONTAL);
		// root.setGravity(Gravity.CENTER);
		root.setLayoutParams(params2);
		horizontalScrollView.addView(root);

		params2.gravity = Gravity.BOTTOM;

		horizontalScrollView.setLayoutParams(params2);
		this.addView(linearLayout);
	}

	private void loadStructureViews() {
		int size = getStructure().getPanelsInfo().size();

		for (int i = size - 1; i >= 0; i--) {
			PanelInfo panel = getStructure().getPanelsInfo().get(i);
			if (panel.getLauncher().equals("true")) {

				addItemView(panel.getName(), panel.getImageOnResourceName(),
						panel.getImageOffResourceName(),
						panel.getImageLockResourceName(),
						panel.getProductIds(), panel.getLockedCount(), i);
			}
		}
		/*
		 * root.post(new Runnable() {
		 * 
		 * @Override public void run() { int size = items.size(); int margin =
		 * 0; if (size <= 5 && size > 2) {
		 * 
		 * int w = items.get(1).getWidth(); Log.d("WIDTH", "w=" + w);
		 * Log.d("RootWIDTH", "w=" + LauncherPanel.this.getWidth()); margin =
		 * ((LauncherPanel.this.getWidth() - (size * w)) / (size));
		 * Log.d("MARGIN", "m=" + margin); for (int i = 0; i < size - 1; i++) {
		 * View v = items.get(i); LinearLayout.LayoutParams params = new
		 * LinearLayout.LayoutParams( (int) LayoutParams.WRAP_CONTENT, (int)
		 * LayoutParams.WRAP_CONTENT); params.leftMargin = margin;
		 * v.setLayoutParams(params); }
		 * 
		 * } } });
		 */

	}

	private void addItemView(String name, String imageOn, String imageOff,
			String imageLock, List<String> productIds, int lockedCount, int id) {
		int resourceOff = context.getResources().getIdentifier(imageOff,
				"drawable", context.getPackageName());
		int resourceOn = context.getResources().getIdentifier(imageOn,
				"drawable", context.getPackageName());
		int resourceLock = context.getResources().getIdentifier(imageLock,
				"drawable", context.getPackageName());
		// LauncherItemView btn = new LauncherItemView(context, resourceOn,
		// resourceOff, 0, false, productIds, lockedCount);
		LauncherItemView btn = new LauncherItemView(context, resourceOn,
				resourceOff, resourceLock, true, productIds, lockedCount);

		btn.setId(id);
		btn.setTag(name);
		btn.setState(true);

		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (onLaunchPanelListener != null) {
					onLaunchPanelListener.onLaunchPanelSelected((String) v
							.getTag());
				}

				// getPanelManager().ShowPanel((String) v.getTag(),
				// LauncherPanel.this);

				String panelName = (String) v.getTag();
				IPanel panel = getPanelManager().getPanel(panelName);

				if (panel != null) {
					try {
					
						if (((ButtonPanel) panel).getPanelInfo()
								.getWithFragment() != null
								&& ((ButtonPanel) panel).getPanelInfo()
										.getWithFragment().equals("GridPager")
								&& ((ButtonPanel) panel).items.size() <= 1) {
							
							((ButtonPanel) panel).getOnStateFragmentListener()
									.onShow(((ButtonPanel) panel).items);
						} else {
							panel.setRootPanel(LauncherPanel.this);
							getPanelManager().ShowPanel(panelName,
									LauncherPanel.this);
						}
					} catch (Exception e) {
						panel.setRootPanel(LauncherPanel.this);
						getPanelManager().ShowPanel(panelName,
								LauncherPanel.this);
					}
				}
			}
		});

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				(int) LayoutParams.MATCH_PARENT,
				(int) LayoutParams.WRAP_CONTENT);

		if (id != 0) {
			int margin = (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 2, getResources()
							.getDisplayMetrics());
			params.leftMargin = margin;
		}

		items.add(btn);
		root.addView(btn, 0, params);
	}

	@Override
	public PanelManager getPanelManager() {
		return panelManager;
	}

	@Override
	public void setPanelManager(PanelManager manager) {
		panelManager = manager;
	}

	@Override
	public Structure getStructure() {
		return structure;
	}

	@Override
	public void setStructure(Structure structure) {
		this.structure = structure;
		if (structure != null) {
			addViews();
		}
	}

	@Override
	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}

	@Override
	public String getPanelName() {
		return panelName;
	}

	@Override
	public void setRootPanel(IPanel Root) {
		RootPanel = Root;
	}

	@Override
	public IPanel getRootPanel() {
		return RootPanel;
	}

	@Override
	public void showPanel(final IPanel hidePanel) {
		Log.d("AAA", "show LauncherPanel");

		Animation anim = AnimationUtils.loadAnimation(context, R.anim.showbar);
		bringToFront();

		getPanelManager().setCurrPanel(this);

		if (hidePanel != null) {
			hidePanel.hidePanel();
		}
		setVisibility(View.VISIBLE);
		startAnimation(anim);
	}

	@Override
	public void hidePanel() {
		Log.d("AAA", "hide LauncherPanel");
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.hidebar);
		startAnimation(anim);
		setVisibility(View.GONE);
	}

	@Override
	public String getPanelType() {
		return PanelManager.LAUNCHER_PANEL_TYPE;
	}

	@Override
	public void enableViews() {
		for (int i = 0; i < items.size(); i++) {
			items.get(i).setEnabled(true);
		}
		enableViews = true;
	}

	@Override
	public void disableViews() {
		for (int i = 0; i < items.size(); i++) {
			if (!items.get(i).isLauncher) {
				items.get(i).setEnabled(false);
			}
		}
		enableViews = false;
	}

	@Override
	public boolean isViewsEnabled() {
		return enableViews;
	}

	@Override
	public void restoreOriginal() {
		// TODO Auto-generated method stub
		boolean enable;
		for (int i = 0; i < items.size(); i++) {
			enable = items.get(i).isEnabled();
			items.get(i).setEnabled(false);
			items.get(i).setState(false);
			items.get(i).setEnabled(enable);
		}
	}

	@Override
	public void unlockAll() {
		for (int i = 0; i < items.size(); i++) {
			items.get(i).setUnlocked();
		}
	}

	@Override
	public void unlockByProductId(String productId) {
		Log.d("AAA", "laucherpanel unlock by productId = " + productId);
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getProductIds() != null) {
				Log.d("AAA", "items.get(i).getProductIds() = "
						+ items.get(i).getProductIds());
				for (int j = 0; j < items.get(i).getProductIds().size(); j++) {
					if (items.get(i).getProductIds().get(j).equals(productId)) {
						items.get(i).setUnlocked(productId);
					}
				}
			}
		}
	}

	@Override
	public void restoreOriginal(boolean sendChanges) {
		boolean enable;
		for (int i = 0; i < items.size(); i++) {
			enable = items.get(i).isEnabled();
			items.get(i).setEnabled(sendChanges);
			items.get(i).setState(false);
			items.get(i).setEnabled(enable);
		}

	}


	@Override
	public void setOnStateListener(OnStateListener onStatetListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTargetItem(int item) {
		// TODO Auto-generated method stub
		
	}
}