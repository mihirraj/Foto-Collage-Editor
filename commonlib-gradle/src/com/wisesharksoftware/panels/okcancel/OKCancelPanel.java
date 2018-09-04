package com.wisesharksoftware.panels.okcancel;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.smsbackupandroid.lib.R;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.panels.IPanel;
import com.wisesharksoftware.panels.PanelInfo;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.Structure;

public class OKCancelPanel extends RelativeLayout implements IPanel, IOkCancel {
	public static final String ACTION = "OkCancel";
	protected Context context;
	protected Structure structure;
	protected PanelManager manager;
	protected IPanel RootPanel;
	protected String panelName;
	ImageButton btnOK;
	ImageButton btnCancel;
	protected boolean enableViews = true;
	protected boolean locked = false;
	protected OnLockedButtonListener onLockedButtonListener;
	protected OnStateListener onStateListener;

	private PanelInfo panelInfo;
	protected IOkCancelListener listener;

	@Override
	public String getAction() {
		return panelInfo.getAction();
	}

	@Override
	public String getActionGroup() {
		return panelInfo.getActionGroup();
	}

	@Override
	public void setPanelInfo(PanelInfo pi) {
		panelInfo = pi;

	}

	@Override
	public int getPriority() {
		return panelInfo.getPriority();
	}

	// private static List<String> productIds = new ArrayList<String>();

	public void setListener(IOkCancelListener listener) {
		this.listener = listener;
	}

	public interface OnLockedButtonListener {
		public void onClick(boolean locked);
	}

	public OKCancelPanel(Context context) {
		this(context, null, 0);
		// LayoutInflater.from(context).inflate(R.layout.panel_ok_cancel, this);
		// findViews();
	}

	public OKCancelPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// LayoutInflater.from(context).inflate(R.layout.panel_ok_cancel, this);
		// findViews();
	}

	public OKCancelPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		// LayoutInflater.from(context).inflate(R.layout.panel_ok_cancel, this);
		// findViews();
	}

	public PanelInfo getPanelInfo() {
		for (int i = structure.getPanelsInfo().size() - 1; i >= 0; i--) {
			PanelInfo panel = structure.getPanelsInfo().get(i);
			if (panel.getName().equals(panelName)) {
				return panel;
			}
		}
		return null;
	}

	protected void findViews() {
		btnOK = (ImageButton) findViewById(R.id.btnOK);
		btnCancel = (ImageButton) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (listener != null) {
					listener.onCancel();
				}
			}
		});
		btnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onLocked(locked);
				}
				if (onLockedButtonListener != null) {
					onLockedButtonListener.onClick(locked);
				}
			}
		});
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void inflateDefault() {
		LayoutInflater.from(context).inflate(R.layout.panel_ok_cancel, this);
		findViews();
	}

	public void inflateForFiltergram() {
		LayoutInflater.from(context).inflate(
				R.layout.panel_ok_cancel_filtergram, this);
		findViews();
	}

	public void setOnBtnOKLockedButtonListener(
			OnLockedButtonListener onLockedButtonListener_) {
		onLockedButtonListener = onLockedButtonListener_;
	}

	public void setOnBtnCancelClickListener(OnClickListener onClickListener) {
		if (btnCancel != null) {
			// btnCancel.setOnClickListener(onClickListener);
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (listener != null) {
						listener.onCancel();
					}
				}
			});
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
			String res = ((OKCancelPanelInfo) getPanelInfo()).getResName();
			if (res.equals("filtergram")) {
				inflateForFiltergram();
			} else {
				inflateDefault();
			}
			locked = ((OKCancelPanelInfo) getPanelInfo()).isLocked();
			Log.d("AAA", "okcancelpanel locked = " + locked);
			// if (locked) {
			// int resourceLock = context.getResources().getIdentifier("lock",
			// "drawable", context.getPackageName());
			// btnOK.setImageResource(resourceLock);
			// }
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
		if (onStateListener != null)
			onStateListener.onShow(null);
		if (listener != null) {
			listener.onShow();
		}
		
		bringToFront();
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.showbar);

		getPanelManager().setCurrPanel(this);

		setVisibility(View.VISIBLE);
		startAnimation(anim);

		if (hidePanel != null) {
			hidePanel.hidePanel();
		}

	}

	@Override
	public void hidePanel() {

		Animation anim = AnimationUtils.loadAnimation(context, R.anim.hidebar);
		startAnimation(anim);
		setVisibility(View.GONE);
		if (onStateListener != null) {
			onStateListener.onHide();
		}
	}

	@Override
	public String getPanelType() {
		return PanelManager.OK_CANCEL_PANEL_TYPE;
	}

	@Override
	public void enableViews() {
		if (btnOK != null) {
			btnOK.setEnabled(true);
		}
		if (btnCancel != null) {
			btnCancel.setEnabled(true);
		}
		enableViews = true;
	}

	@Override
	public void disableViews() {
		if (btnOK != null) {
			btnOK.setEnabled(false);
		}
		if (btnCancel != null) {
			btnCancel.setEnabled(false);
			enableViews = false;
		}
	}

	@Override
	public boolean isViewsEnabled() {
		return enableViews;
	}

	@Override
	public void restoreOriginal() {
		// do nothing
	}

	@Override
	public void unlockAll() {
		// btnOK.setImageResource(0);
		locked = false;
	}

	@Override
	public void unlockByProductId(String productId) {
		// TODO Auto-generated method stub
		// productIds.add(productId);
	}

	@Override
	public void restoreOriginal(boolean sendChanges) {
		// do nothing

	}

	@Override
	public void setTargetItem(int item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnStateListener(OnStateListener onStatetListener) {
		this.onStateListener = onStatetListener;

	}

}