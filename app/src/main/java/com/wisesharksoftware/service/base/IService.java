package com.wisesharksoftware.service.base;

import java.util.ArrayList;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.ButtonPanel.OnLaunchPanelListener;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.sticker.DrawableHighlightView;

public interface IService {

	public DrawableHighlightView getHighlightView();

	public IOkCancelListener getOkCancelListener();

	public OnLaunchPanelListener getOnLaunchPanelListener();

	public OnItemListener getOnItemListener();

	public void setDrawableHighlightView(DrawableHighlightView view);

	public String getName();

	public ArrayList<Preset> getFilterPreset();

	public void clear();

	public String getActionGroup();

	public void setChooseProcessing(ChooseProcessingActivity a);

	public void setPanelManager(PanelManager m);

	public void setActionGroup(String actionGroup);

	public OnStateListener getOnStateListener();

	public String getAction();

	public int getPriority();
	
	public void setPriority(int p);

}
