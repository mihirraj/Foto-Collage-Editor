package com.wisesharksoftware.service.base;

import java.util.ArrayList;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.ButtonPanel.OnLaunchPanelListener;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.sticker.DrawableHighlightView;

public class BaseService implements IService {

	protected PanelManager panelManager;
	protected ChooseProcessingActivity chooseProcessing;
	protected String actionGroup;
	protected String action;
	protected int priority;

	public void setAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return action;
	}

	@Override
	public OnItemListener getOnItemListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DrawableHighlightView getHighlightView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OnLaunchPanelListener getOnLaunchPanelListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDrawableHighlightView(DrawableHighlightView view) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getActionGroup() {
		return actionGroup;
	}

	@Override
	public void setChooseProcessing(ChooseProcessingActivity a) {
		chooseProcessing = a;

	}

	@Override
	public void setPanelManager(PanelManager m) {
		panelManager = m;

	}

	@Override
	public void setActionGroup(String actionGroup) {
		this.actionGroup = actionGroup;

	}

	@Override
	public IOkCancelListener getOkCancelListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OnStateListener getOnStateListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	public BaseService self() {
		return this;
	}

	@Override
	public void setPriority(int p) {
		priority = p;
	}
}
