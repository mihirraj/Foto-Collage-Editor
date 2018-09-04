package com.wisesharksoftware.panels;

import java.util.ArrayList;
import java.util.List;

import android.widget.ImageView;
import com.smsbackupandroid.lib.R;
import com.wisesharksoftware.category_panel.ItemView;
import com.wisesharksoftware.panels.LauncherPanel.OnLaunchPanelListener;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ButtonPanel extends RelativeLayout implements IPanel {
    private HorizontalScrollView horizontalScrollView;
    private LinearLayout linearLayout;
    protected Context context;
    protected Structure structure;
    private LinearLayout root;
    private OnItemListener onItemListener;
    public OnLaunchPanelListener onLaunchPanelListener;
    private PanelManager manager;
    private IPanel RootPanel;
    protected String panelName;
    List<LauncherItemView> items = new ArrayList<LauncherItemView>();
    private boolean enableViews = true;
    public OnStateListener onStateListener;
    private int targetItem;
    private PanelInfo panelInfo;

    public static final int ACTION_BACK = 0;
    public static final int ACTION_SAVE = 1;

    private ImageView back;
    private ImageView save;
    private TurboScanPanel.OnActionListener onActionListener;
    private LinearLayout buttonContainer;
    protected int resBackground = 0;
    protected int resSelectorBack = 0;
    protected int resSelectorSave = 0;

    public interface OnStateListener {
        public void onShow(List<LauncherItemView> items);

        public void onHide();
    }

    public void setOnActionListener(TurboScanPanel.OnActionListener onActionListener) {
        this.onActionListener = onActionListener;
        Log.d("sdglksdfgjklf","6");
    }

    @Override
    public int getPriority() {
        return panelInfo.getPriority();
    }

    public void setPanelInfo(PanelInfo panelInfo) {
        this.panelInfo = panelInfo;
    }

    public String getAction() {
        return panelInfo.getAction();
    }

    public String getActionGroup() {
        return panelInfo.getActionGroup();
    }

    public interface OnLaunchPanelListener {
        public void onLaunchPanelSelected(LauncherItemView item,
                                          String nameLaunchPanel, List<String> productIds);

    }

    public interface OnItemListener {
        public boolean onItemSelected(LauncherItemView item, String buttonName,
                                      boolean state);

        public void onLockedItemSelected(String buttonName,
                                         LauncherItemView item);
    }

    public HorizontalScrollView getHorizontalScrollView() {
        return horizontalScrollView;
    }

    public void setOnStateListener(OnStateListener onShowFragmentListener) {
        this.onStateListener = onShowFragmentListener;
    }

    public OnStateListener getOnStateFragmentListener() {
        return onStateListener;
    }

    public ButtonPanel(Context context) {
        this(context, null, 0);
        this.context = context;
    }

    public ButtonPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public ButtonPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

    }

    public void setOnItemListener(OnItemListener onItemListener_,
                                  boolean isSelecting) {
        onItemListener = onItemListener_;

        if (isSelecting) {
            for (final LauncherItemView btn : items) {
                btn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*
						 * boolean old_state = btn.getState(); boolean new_state
						 * = !old_state; btn.setState(new_state); for (int i =
						 * 0; i < items.size(); i++) { if (btn != items.get(i))
						 * { items.get(i).setState(false); } }
						 */
                        boolean new_state = true;
                        boolean changeState = true;
                        if (onItemListener != null) {
                            if (btn.isLocked()) {
                                onItemListener.onLockedItemSelected(
                                        (String) v.getTag(), btn);
                            } else {
                                changeState = onItemListener.onItemSelected(
                                        btn, (String) v.getTag(), new_state);
                            }
                        }

                        if (changeState) {
                            btn.setState(new_state);
                            for (int i = 0; i < items.size(); i++) {
                                if (btn != items.get(i)) {
                                    items.get(i).setState(false);
                                }
                            }
                        }

                    }
                });

            }
        }
    }

    public void setOnLaunchListener(OnLaunchPanelListener onLaunchPanelListener_) {
        onLaunchPanelListener = onLaunchPanelListener_;
    }

    public LinearLayout getRoot() {
        return root;
    }

    public List<LauncherItemView> getItems() {
        return items;
    }

    protected void addViews() {
        loadStandartViews();
        loadStructureViews();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void loadStandartViews() {

		buttonContainer = new LinearLayout(context);

		if(getPanelName().equals("Effects_start")) {


			Log.d("sdglksdfgjklf", "1");
			back = new ImageView(context);
			back.setBackgroundResource(resSelectorBack);
			back.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (onActionListener != null) {
						onActionListener.onAction(ACTION_BACK);
					}

				}
			});
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.addRule(CENTER_VERTICAL);
			params.addRule(ALIGN_PARENT_LEFT);
			// params.leftMargin = 7;
			back.setLayoutParams(params);
			back.setId(1);
			addView(back);

			save = new ImageView(context);
			save.setBackgroundResource(resSelectorSave);
			save.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Log.d("sdglksdfgjklf", "2");
					if (onActionListener != null) {
						Log.d("sdglksdfgjklf", "3");
						onActionListener.onAction(ACTION_SAVE);
					}

				}
			});
			params = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.addRule(CENTER_VERTICAL);
			params.addRule(ALIGN_PARENT_RIGHT);
			//params.rightMargin = 7;
			save.setLayoutParams(params);
			save.setId(2);
			addView(save);

			params = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			buttonContainer.setGravity(Gravity.CENTER);
			params.addRule(CENTER_VERTICAL);
			params.addRule(LEFT_OF, 2);
			params.addRule(RIGHT_OF, 1);
			buttonContainer.setLayoutParams(params);
			//addView(buttonContainer);

			Log.d("sdglksdfgjklf", "4");

			//-----------------------------------
		}

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

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                (int) LayoutParams.WRAP_CONTENT,
                (int) LayoutParams.MATCH_PARENT);
        buttonContainer.addView(horizontalScrollView);
        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);

        horizontalScrollView.addView(root);

        params2.gravity = Gravity.BOTTOM;

        horizontalScrollView.setLayoutParams(params2);
        this.addView(buttonContainer);

        Log.d("sdglksdfgjklf","5");


/*
        back = new ImageView(context);
        back.setBackgroundResource(resSelectorBack);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (onActionListener != null) {
                    onActionListener.onAction(ACTION_BACK);
                }

            }
        });
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_VERTICAL);
        params.addRule(ALIGN_PARENT_LEFT);
        // params.leftMargin = 7;
        back.setLayoutParams(params);
        back.setId(1);

        addView(back);

        save = new ImageView(context);
        save.setBackgroundResource(resSelectorSave);
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (onActionListener != null) {
                    onActionListener.onAction(ACTION_SAVE);
                }

            }
        });
        params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_VERTICAL);
        params.addRule(ALIGN_PARENT_RIGHT);
        //params.rightMargin = 7;
        save.setLayoutParams(params);
        save.setId(2);
        addView(save);

        linearLayout = new LinearLayout(context);
        params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        linearLayout.setGravity(Gravity.CENTER);
        params.addRule(CENTER_VERTICAL);
        params.addRule(LEFT_OF, 2);
        params.addRule(RIGHT_OF, 1);
        linearLayout.setLayoutParams(params);
        //addView(buttonContainer);
        //////////////////////////

        // create linear layout root
        //linearLayout = new LinearLayout(context);
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

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                (int) LayoutParams.WRAP_CONTENT,
                (int) LayoutParams.MATCH_PARENT);
        linearLayout.addView(horizontalScrollView);
        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);

        horizontalScrollView.addView(root);

        params2.gravity = Gravity.BOTTOM;

        horizontalScrollView.setLayoutParams(params2);
        this.addView(linearLayout);
  */  }

    private void loadStructureViews() {
        for (int i = structure.getPanelsInfo().size() - 1; i >= 0; i--) {
            PanelInfo panel = structure.getPanelsInfo().get(i);
            if (panel.getName().equals(panelName)) {
                for (int j = panel.getItems().size() - 1; j >= 0; j--) {
                    Item item = panel.getItems().get(j);
					/*
					 * addItemView(item.getName(),
					 * item.getImageOnResourceName(),
					 * item.getImageOffResourceName(),
					 * item.getImageLockResourceName(), item.getProductIds(),
					 * item.isShowAsLocked(), item.getLaunchPanel(),
					 * item.getLockedlaunchPanel(),
					 * j,item.getBackgroundColor());
					 */
                    addItemView(item, j, root, true);
                }
            }
        }
    }

    protected void addItemView(Item item, int id, ViewGroup container,
                               boolean marg) {
        int resourceOff = context.getResources().getIdentifier(
                item.getImageOffResourceName(), "drawable",
                context.getPackageName());
        int resourceOn = context.getResources().getIdentifier(
                item.getImageOnResourceName(), "drawable",
                context.getPackageName());
        int resourceLock = context.getResources().getIdentifier(
                item.getImageLockResourceName(), "drawable",
                context.getPackageName());
        boolean isLauncherItem = !item.getLaunchPanel().equals("");
        final LauncherItemView btn = new LauncherItemView(context, resourceOn,
                resourceOff, resourceLock, item.isShowAsLocked(),
                item.getProductIds(), item.getUnlockedCount(),
                item.getLaunchPanel(), item.getLockedlaunchPanel());
        btn.setBackgroundColor(item.getBackgroundColor());
        btn.setId(id);
        btn.setItem(item);
        btn.setTag(item.getName());

        if (!isLauncherItem) {
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
					/*
					 * boolean old_state = btn.getState(); boolean new_state =
					 * !old_state; btn.setState(new_state); for (int i = 0; i <
					 * items.size(); i++) { if (btn != items.get(i)) {
					 * items.get(i).setState(false); } }
					 */
                    boolean new_state = true;
                    boolean changeState = true;
                    if (onItemListener != null) {
                        if (btn.isLocked()) {
                            onItemListener.onLockedItemSelected(
                                    (String) v.getTag(), btn);
                        } else {
                            changeState = onItemListener.onItemSelected(btn,
                                    (String) v.getTag(), new_state);
                        }
                    }

                    if (changeState) {
                        btn.setState(new_state);
                        for (int i = 0; i < items.size(); i++) {
                            if (btn != items.get(i)) {
                                items.get(i).setState(false);
                            }
                        }
                    }
                }
            });
        } else {
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (btn.isLocked()) {
                        if (onItemListener != null) {
                            onItemListener.onLockedItemSelected(
                                    (String) v.getTag(), btn);
                        }
                        return;
                    }
                    if (onLaunchPanelListener != null) {
                        LauncherItemView targetItemView = null;
                        try {
                            String panelName = ((LauncherItemView) v)
                                    .getLauncherPanel();
                            ButtonPanel panel = (ButtonPanel) getPanelManager()
                                    .getPanel(panelName);
                            if (panel.targetItem != 0) {
                                targetItemView = panel.items
                                        .get(panel.targetItem - 1);
                            }
                        } catch (Exception e) {
                        }
                        onLaunchPanelListener.onLaunchPanelSelected(
                                ((LauncherItemView) v), (String) v.getTag(),
                                btn.getProductIds());
                    }

                    try {
                        if (!getPanelInfo().getWithFragment().equals(
                                "GridPager")
                                && items.size() != 1) {
                            String panelName = ((LauncherItemView) v)
                                    .getLauncherPanel();
                            String lockedPanelName = ((LauncherItemView) v)
                                    .getLockedLauncherPanel();
                            Log.d("AAA", "lockedPanelName = " + lockedPanelName);
                            if ((btn.isShowAsLocked())
                                    && (!lockedPanelName.equals(""))) {
                                Log.d("AAA", "<-->");
                                panelName = lockedPanelName;
                            }
                            Log.d("AAA", "LAUNCH panel with name = "
                                    + panelName);
                            IPanel panel = getPanelManager()
                                    .getPanel(panelName);
                            if (panel != null) {
                                panel.setRootPanel(ButtonPanel.this);
                                getPanelManager().ShowPanel(panelName,
                                        ButtonPanel.this);
                            }
                        }
                    } catch (Exception e) {
                        String panelName = ((LauncherItemView) v)
                                .getLauncherPanel();
                        String lockedPanelName = ((LauncherItemView) v)
                                .getLockedLauncherPanel();
                        Log.d("AAA", "lockedPanelName = " + lockedPanelName);
                        if ((btn.isShowAsLocked())
                                && (!lockedPanelName.equals(""))) {
                            Log.d("AAA", "<-->");
                            panelName = lockedPanelName;
                        }
                        Log.d("AAA", "LAUNCH panel with name = " + panelName);
                        IPanel panel = getPanelManager().getPanel(panelName);
                        if (panel != null) {
//							panel.setRootPanel(ButtonPanel.this);
//							getPanelManager().ShowPanel(panelName,
//									ButtonPanel.this);

                            if (panel != null) {
                                try {
                                    if (((ButtonPanel) panel).getPanelInfo().getWithFragment() != null
                                            && ((ButtonPanel) panel).getPanelInfo().getWithFragment().equals("GridPager")
                                            && ((ButtonPanel) panel).items.size() <= 1) {
                                        ((ButtonPanel) panel).getOnStateFragmentListener()
                                                .onShow(((ButtonPanel) panel).items);
                                    } else {
                                        panel.setRootPanel(ButtonPanel.this);
                                        getPanelManager().ShowPanel(panelName,
                                                ButtonPanel.this);
                                    }
                                } catch (Exception e2) {
//									e.printStackTrace();
                                    panel.setRootPanel(ButtonPanel.this);
                                    getPanelManager().ShowPanel(panelName,
                                            ButtonPanel.this);
                                }
                            }
                        }
                    }
                }
            });
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) LayoutParams.WRAP_CONTENT,
                (int) LayoutParams.WRAP_CONTENT);

		/*
		 * if (id != 0) { float margin = TypedValue.applyDimension(
		 * TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
		 * params.leftMargin = (int) margin; }
		 */
        if (marg) {
            int marginValue = ((ButtonPanelInfo) getPanelInfo()).getMargin();
            float margin = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, marginValue, getResources()
                            .getDisplayMetrics());
            params.leftMargin = (int) margin;
        }
        items.add(btn);
        container.addView(btn, 0, params);
    }

    public PanelInfo getPanelInfo() {
        return panelInfo;
    }

    public void callOnClickItem(String itemId) {
        boolean new_state = true;
        boolean changeState = true;
        LauncherItemView btn = null;

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getTag().equals(itemId)) {
                btn = items.get(i);
                break;
            }
        }
        if (btn != null) {
            if (onItemListener != null) {
                if (btn.isLocked()) {
                    onItemListener.onLockedItemSelected((String) btn.getTag(),
                            btn);
                } else {
                    changeState = onItemListener.onItemSelected(btn,
                            (String) btn.getTag(), new_state);
                }
            }
            if (changeState) {
                for (int i = 0; i < items.size(); i++) {
                    if (btn != items.get(i)) {
                        items.get(i).setState(false);
                    }
                }
            }
        }
    }

    @Override
    public void setPanelManager(PanelManager manager) {
        this.manager = manager;
    }

    @Override
    public void setRootPanel(IPanel Root) {
        RootPanel = Root;
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
    public PanelManager getPanelManager() {
        return manager;
    }

    @Override
    public IPanel getRootPanel() {
        return RootPanel;
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
    public void showPanel(final IPanel hidePanel) {
        Log.d("AAA", "show ButtonPanel");
        bringToFront();
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.showbar);

        getPanelManager().setCurrPanel(this);

        setVisibility(View.VISIBLE);
        startAnimation(anim);

        if (hidePanel != null) {
            hidePanel.hidePanel();

        }

        if (onStateListener != null)
            onStateListener.onShow(getItems());
    }

    @Override
    public void hidePanel() {
        Log.d("AAA", "hide ButtonPanel");
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.hidebar);
        startAnimation(anim);
        setVisibility(View.GONE);
        if (onStateListener != null)
            onStateListener.onHide();
    }

    @Override
    public String getPanelType() {
        return PanelManager.BUTTON_PANEL_TYPE;
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
        boolean enable;
        for (int i = 0; i < items.size(); i++) {
            enable = items.get(i).isEnabled();
            items.get(i).setEnabled(false);
            items.get(i).setState(false);
            items.get(i).setEnabled(enable);
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
    public void unlockAll() {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setUnlocked();
        }
    }

    @Override
    public void unlockByProductId(String productId) {
        Log.d("AAA", "buttonpanel unlock by productId = " + productId);
        for (int i = 0; i < items.size(); i++) {
            Log.d("AAA", "items.get(i).getProductIds() = "
                    + items.get(i).getProductIds());
            for (int j = 0; j < items.get(i).getProductIds().size(); j++) {
                if (items.get(i).getProductIds().get(j).equals(productId)) {
                    items.get(i).setUnlocked(productId);
                }
            }
        }
    }

    @Override
    public void setTargetItem(int item) {
        targetItem = item;

    }
}