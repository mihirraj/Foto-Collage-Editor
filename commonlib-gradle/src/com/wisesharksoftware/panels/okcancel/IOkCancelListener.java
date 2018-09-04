package com.wisesharksoftware.panels.okcancel;

import com.wisesharksoftware.panels.bars.BarTypes;

public interface IOkCancelListener {
	public void onShow();
	
	public void onOK();

	public void onCancel();

	public void onLocked(boolean lock);

	public void onChange(/*int barId, BarTypes barType, int change,*/Object... params);
	
	public void onChangeFromUser(Object... params);
	
	public void onStop(Object... params);
	
	public void onRestore();
}
