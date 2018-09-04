package com.wisesharksoftware.panels;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

public class LauncherItemView extends ImageButton {
	private boolean state = false;
	private int idOnResourceImage = 0;
	private int idOffResourceImage = 0;
	private int idLockResourceImage = 0;
	private String launcherPanel = "";
	public boolean locked = false;
	boolean isLauncher = false;
	private boolean showAsLocked = false;
	private List<String> productIds = new ArrayList<String>();
	private int unlockedCount = 5;
	private String lockedLauncherPanel;
	private String name;
	private String backgroundColor;
	private OnUnlockItemListener onUnlockItemListener;
	private Item item;

	public interface OnUnlockItemListener {
		public void unlock(LauncherItemView item);
	}

	public void setOnUnlockItemListener(
			OnUnlockItemListener onUnlockItemListener) {
		this.onUnlockItemListener = onUnlockItemListener;
	}

	public LauncherItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setState(false);
	}

	public LauncherItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setState(false);
	}

	public LauncherItemView(Context context) {
		super(context);
		setState(false);
	}

	public int getIdLockResourceImage() {
		return idLockResourceImage;
	}

	public LauncherItemView(Context context, int idOnResourceImage,
			int idOffResourceImage, int idLockResourceImage,
			boolean showAsLocked, List<String> productIds, int lockedCount) {
		this(context);
		this.idOnResourceImage = idOnResourceImage;
		this.idOffResourceImage = idOffResourceImage;
		this.idLockResourceImage = idLockResourceImage;
		this.productIds = productIds;
		this.unlockedCount = lockedCount;
		this.isLauncher = true;
		this.showAsLocked = showAsLocked;
		if (idLockResourceImage != 0) {
			if (showAsLocked) {
				setAppearenceLocked();
			} else {
				setLocked();
			}
		}

		else {
			setUnlocked();
		}
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public Item getItem() {
		return item;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAppearenceLocked() {
		setImageResource(idLockResourceImage);
		setPadding(0, 0, 0, 0);
		if (idOffResourceImage != 0)
			setBackgroundResource(idOffResourceImage);
	}

	public void setLocked() {
		setImageResource(idLockResourceImage);
		setPadding(0, 0, 0, 0);
		if (idOffResourceImage != 0)
			setBackgroundResource(idOffResourceImage);
		locked = true;
	}

	public void setUnlocked(String productId) {
		for (int i = 0; i < productIds.size(); i++) {
			if (productIds.get(i).equals(productId)) {
				unlock();
				break;
			}
		}
	}

	public void setState(boolean state, boolean launcher) {
		Log.d("State", state+"");
		this.state = state;
		if (this.state) {
			this.setImageResource(idOnResourceImage);
		} else {
			this.setImageResource(idOffResourceImage);
		}

	}

	private void unlock() {
		StateListDrawable states = new StateListDrawable();
		if (idOnResourceImage != 0)
			states.addState(new int[] { android.R.attr.state_pressed },
					getResources().getDrawable(idOnResourceImage));
		if (idOffResourceImage != 0)
			states.addState(new int[] { android.R.attr.state_focused },

			getResources().getDrawable(idOffResourceImage));
		if (idOffResourceImage != 0)
			states.addState(new int[] {},
					getResources().getDrawable(idOffResourceImage));
		this.setPadding(0, 0, 0, 0);
		this.setBackgroundColor(Color.TRANSPARENT);
		setImageDrawable(states);
		locked = false;
		showAsLocked = false;
		if (onUnlockItemListener != null) {
			onUnlockItemListener.unlock(this);
		}
	}

	public void setUnlocked() {
		unlock();
	}

	public LauncherItemView(Context context, int idOnResourceImage,
			int idOffResourceImage, int idLockResourceImage,
			boolean showAsLocked, List<String> productIds, int lockedCount,
			String launcherPanel, String lockedLauncherPanel) {
		this(context);
		this.idOnResourceImage = idOnResourceImage;
		this.idOffResourceImage = idOffResourceImage;
		this.idLockResourceImage = idLockResourceImage;
		this.launcherPanel = launcherPanel;
		this.lockedLauncherPanel = lockedLauncherPanel;
		this.showAsLocked = showAsLocked;
		this.productIds = productIds;
		this.unlockedCount = lockedCount;
		this.isLauncher = (!launcherPanel.equals(""));

		if (this.isLauncher) {
			StateListDrawable states = new StateListDrawable();
			if (idOnResourceImage != 0)
				states.addState(new int[] { android.R.attr.state_pressed },
						getResources().getDrawable(idOnResourceImage));
			if (idOffResourceImage != 0)
				states.addState(new int[] { android.R.attr.state_focused },
						getResources().getDrawable(idOffResourceImage));
			if (idOnResourceImage != 0)
				states.addState(new int[] {},
						getResources().getDrawable(idOffResourceImage));
			this.setPadding(0, 0, 0, 0);
			this.setBackgroundColor(Color.TRANSPARENT);
			setImageDrawable(states);
		} else {
			setState(false);
		}

		if (idLockResourceImage != 0) {
			if (showAsLocked) {
				setAppearenceLocked();
			} else {
				setLocked();
			}
		}
	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean state) {
		if (idLockResourceImage != 0) {
			return;
		}
		if (!isLauncher) {
			this.state = state;
			if (this.state) {
				if (idOnResourceImage != 0)
					this.setBackgroundResource(idOnResourceImage);
			} else {
				if (idOffResourceImage != 0)
					this.setBackgroundResource(idOffResourceImage);
			}
		}
	}

	public void setOn() {
		this.setBackgroundResource(idOnResourceImage);
	}

	public void setOff() {
		this.setBackgroundResource(idOffResourceImage);
	}

	public boolean isLocked() {
		return locked;
	}

	public int getIdOnResourceImage() {
		return idOnResourceImage;
	}

	public void setIdOnResourceImage(int idOnResourceImage) {
		this.idOnResourceImage = idOnResourceImage;
	}

	public int getIdOffResourceImage() {
		return idOffResourceImage;
	}

	public void setIdOffResourceImage(int idOffResourceImage) {
		this.idOffResourceImage = idOffResourceImage;
	}

	public String getLauncherPanel() {
		return launcherPanel;
	}

	public List<String> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<String> productIds) {
		this.productIds = productIds;
	}

	public String getLockedLauncherPanel() {
		return lockedLauncherPanel;
	}

	public void setLockedLauncherPanel(String lockedLauncherPanel) {
		this.lockedLauncherPanel = lockedLauncherPanel;
	}

	public boolean isShowAsLocked() {
		return showAsLocked;
	}

	public int getUnlockedCount() {
		return unlockedCount;
	}

	public void setUnlockedCount(int unlockedCount) {
		this.unlockedCount = unlockedCount;
	}
}
