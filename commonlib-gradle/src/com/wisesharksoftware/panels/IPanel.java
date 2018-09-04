package com.wisesharksoftware.panels;

import java.util.List;

import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;

public interface IPanel {
	public void setPanelName(String panelName);

	public String getPanelName();

	public void setPanelManager(PanelManager manager);

	public void setRootPanel(IPanel Root);

	public IPanel getRootPanel();

	public PanelManager getPanelManager();

	public Structure getStructure();

	public void setStructure(Structure structure);

	public void showPanel(final IPanel hidePanel);

	public void hidePanel();

	public String getPanelType();

	public void enableViews();

	public void disableViews();

	public boolean isViewsEnabled();

	public void restoreOriginal();

	public void restoreOriginal(boolean sendChanges);

	public void unlockAll();

	public void unlockByProductId(String productId);

	public void setTargetItem(int item);

	public void setPanelInfo(PanelInfo pi);

	public String getAction();

	public String getActionGroup();

	public int getPriority();

	public void setOnStateListener(OnStateListener onStatetListener);
}
