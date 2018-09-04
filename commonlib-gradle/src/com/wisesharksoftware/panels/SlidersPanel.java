package com.wisesharksoftware.panels;

import java.util.List;

import com.smsbackupandroid.lib.R;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.panels.okcancel.IOkCancel;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SlidersPanel extends RelativeLayout implements IPanel {
	private Context context;
	private Structure structure;
	private PanelManager manager;
	private IPanel RootPanel;
	private String panelName;
	private TextView tvLabel;
	private SeekBar seekBar;
	private TextView tvLabel2;
	private SeekBar seekBar2;
	private boolean enableViews = true;
	private int seekBarOriginalValue = 0;
	private int seekBar2OriginalValue = 0;
	private PanelInfo panelInfo;

	@Override
	public void setPanelInfo(PanelInfo pi) {
		panelInfo = pi;

	}

	@Override
	public int getPriority() {
		return panelInfo.getPriority();
	}

	@Override
	public String getAction() {
		return panelInfo.getAction();
	}

	@Override
	public String getActionGroup() {
		return getActionGroup();
	}

	public SlidersPanel(Context context) {
		super(context);
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.panel_sliders, this);
		findViews();
	}

	public SlidersPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.panel_sliders, this);
		findViews();
	}

	public SlidersPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.panel_sliders, this);
		findViews();
	}

	public void findViews() {
		tvLabel = (TextView) findViewById(R.id.tvLabel);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		tvLabel2 = (TextView) findViewById(R.id.tvLabel2);
		seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
	}

	public void setOnSeekBar1ChangeListener(
			OnSeekBarChangeListener onSeekBarChangeListener) {
		if (seekBar != null) {
			seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		}
	}

	public void setOnSeekBar2ChangeListener(
			OnSeekBarChangeListener onSeekBarChangeListener) {
		if (seekBar2 != null) {
			seekBar2.setOnSeekBarChangeListener(onSeekBarChangeListener);
		}
	}

	public int getSeekBar1Progress() {
		if (seekBar != null) {
			return seekBar.getProgress();
		}
		return 0;
	}

	public int getSeekBar2Progress() {
		if (seekBar2 != null) {
			return seekBar2.getProgress();
		}
		return 0;
	}

	private void addViews() {
		loadStandartViews();
	}

	private void loadStandartViews() {
		if (((SlidersPanelInfo) getPanelInfo()).getCaption().equals("")) {
			tvLabel.setText(this.getPanelName());
		} else {
			tvLabel.setText(((SlidersPanelInfo) getPanelInfo()).getCaption());
		}
		seekBar.setFocusable(true);
		seekBar.setThumbOffset(8);

		seekBar.setMax(((SlidersPanelInfo) getPanelInfo()).getMax());
		seekBarOriginalValue = ((SlidersPanelInfo) getPanelInfo())
				.getProgress();
		seekBar.setProgress(seekBarOriginalValue);
		// seekBar.setPadding(24, 10, 24, 10);
		seekBar.setPadding(24, 0, 24, 0);

		if (((SlidersPanelInfo) getPanelInfo()).getSeekBar2Caption().equals("")) {
			tvLabel2.setText(this.getPanelName());
		} else {
			tvLabel2.setText(((SlidersPanelInfo) getPanelInfo())
					.getSeekBar2Caption());
		}
		seekBar2.setFocusable(true);
		seekBar2.setThumbOffset(8);

		seekBar2.setMax(((SlidersPanelInfo) getPanelInfo()).getSeekBar2Max());
		seekBar2OriginalValue = ((SlidersPanelInfo) getPanelInfo())
				.getSeekBar2Progress();
		seekBar2.setProgress(seekBar2OriginalValue);

		// seekBar.setPadding(24, 10, 24, 10);
		seekBar2.setPadding(24, 0, 24, 0);
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
		Log.d("AAA", "show SliderPanel");
		bringToFront();

		Animation anim = AnimationUtils.loadAnimation(context, R.anim.showbar);

		setVisibility(View.VISIBLE);
		startAnimation(anim);
		getPanelManager().setCurrPanel(this);

		if (hidePanel != null) {
			hidePanel.hidePanel();
		}
	}

	@Override
	public void hidePanel() {
		Log.d("AAA", "hide SilderPanel");
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.hidebar);
		startAnimation(anim);
		setVisibility(View.GONE);
	}

	@Override
	public String getPanelType() {
		return PanelManager.SLIDER_PANEL_TYPE;
	}

	@Override
	public void enableViews() {
		seekBar.setEnabled(true);
		seekBar2.setEnabled(true);
		enableViews = true;
	}

	@Override
	public void disableViews() {
		seekBar.setEnabled(false);
		seekBar2.setEnabled(false);
		enableViews = false;
	}

	@Override
	public boolean isViewsEnabled() {
		return enableViews;
	}

	@Override
	public void restoreOriginal() {
		boolean enable;
		boolean enable2;
		enable = seekBar.isEnabled();
		seekBar.setEnabled(false);
		seekBar.setProgress(seekBarOriginalValue);
		seekBar.setEnabled(enable);
		enable2 = seekBar2.isEnabled();
		seekBar2.setEnabled(false);
		seekBar2.setProgress(seekBar2OriginalValue);
		seekBar2.setEnabled(enable2);
	}

	@Override
	public void unlockAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unlockByProductId(String productId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreOriginal(boolean sendChanges) {
		boolean enable;
		boolean enable2;
		enable = seekBar.isEnabled();
		seekBar.setEnabled(sendChanges);
		seekBar.setProgress(seekBarOriginalValue);
		seekBar.setEnabled(enable);
		enable2 = seekBar2.isEnabled();
		seekBar2.setEnabled(sendChanges);
		seekBar2.setProgress(seekBar2OriginalValue);
		seekBar2.setEnabled(enable2);
	}

	@Override
	public void setTargetItem(int item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnStateListener(OnStateListener onStatetListener) {
		// TODO Auto-generated method stub

	}
}