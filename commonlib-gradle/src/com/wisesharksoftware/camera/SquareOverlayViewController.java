package com.wisesharksoftware.camera;
import com.smsbackupandroid.lib.R;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class SquareOverlayViewController {

	private LinearLayout rightOverlay;
	private LinearLayout leftOverlay;
	private FrameLayout layout;
	private LinearLayout botLayout;
	private RelativeLayout topLayout;
	private boolean isBuild = false;
	float ratio = -1;
	private int overlay_color;
	
	public SquareOverlayViewController(
			BaseCameraPreviewActivity parentActivity, int picturesCount) {
		parentActivity_ = parentActivity;
		picturesCount_ = picturesCount;
		String sratio = parentActivity.getResources().getString(R.string.camera_ratio);
		ratio = Float.parseFloat(sratio);
		overlay_color = parentActivity.getResources().getColor(R.color.square_overlay_color);
	}

	public boolean isBuild() {
		return isBuild;
	}

	// draw semitransparent stripes to make preview "square"
	public void drawSquareFrame(Preview preview) {
		int orienation = parentActivity_.getResources().getConfiguration().orientation;
		layout = (FrameLayout) parentActivity_.getPreviewConatiner();
		topLayout = (RelativeLayout) parentActivity_
				.findViewById(parentActivity_.getTopControlsParent());
		botLayout = (LinearLayout) parentActivity_.findViewById(parentActivity_
				.getBotOverlayFrame());
		// int width = preview.getPreviewSize().width;
		// int height = preview.getPreviewSize().height;
		int width = layout.getWidth();
		int height = layout.getHeight();
		
		int frameWidth = width / 2 - height / 2;
		
		//int frameWidth = (width - height / 4) / 2;
		if (ratio > 0) {
			frameWidth = Math.round(((width - height * ratio) / 2));
		}
		
		int frameHeight = FrameLayout.LayoutParams.MATCH_PARENT;

		if (picturesCount_ > 1) {
			frameWidth = (width - (width / picturesCount_)) / 2;
		}

		if (orienation != Configuration.ORIENTATION_LANDSCAPE) {
			frameHeight = frameWidth;
			frameWidth = FrameLayout.LayoutParams.MATCH_PARENT;
		}
		int gravity1 = orienation == Configuration.ORIENTATION_LANDSCAPE ? (Gravity.TOP | Gravity.LEFT)
				: Gravity.TOP;
		int gravity2 = orienation == Configuration.ORIENTATION_LANDSCAPE ? (Gravity.TOP | Gravity.RIGHT)
				: Gravity.BOTTOM;

		if (topLayout.findViewById(parentActivity_.getTopOverlayFrame()) != null) {
			topLayout.removeView(topLayout.findViewById(parentActivity_
					.getTopOverlayFrame()));
		}
		if (layout.findViewById(RIGHT_OVERLAY_ID) != null) {
			layout.removeView(layout.findViewById(RIGHT_OVERLAY_ID));
		}
		if (width > height) { // should be always so
			leftOverlay = new LinearLayout(parentActivity_);
			FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(
					frameWidth, frameHeight, gravity1);
			leftOverlay.setLayoutParams(params1);
			leftOverlay.setBackgroundColor(overlay_color);
			leftOverlay.setId(parentActivity_.getTopOverlayFrame());

			rightOverlay = new LinearLayout(parentActivity_);
			LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
					frameWidth, frameHeight, gravity2);
			rightOverlay.setBackgroundColor(overlay_color);
			rightOverlay.setId(RIGHT_OVERLAY_ID);

			topLayout.addView(leftOverlay, 0, params1);
			botLayout.addView(rightOverlay, 0, params2);
		}
		isBuild = true;
	}

	public void invalidate() {
		if (rightOverlay != null && leftOverlay != null && topLayout != null
				&& layout != null) {
			rightOverlay.invalidate();
			leftOverlay.invalidate();
			topLayout.invalidate();
			layout.invalidate();
			botLayout.invalidate();
		}
	}

	// private final static int LEFT_OVERLAY_ID = 64578;
	private final static int RIGHT_OVERLAY_ID = 78965;
	//private final static int OVERLAY_COLOR = 0x99000000;
	//private final static int OVERLAY_COLOR = 0xFF000000;
	private final BaseCameraPreviewActivity parentActivity_;
	private final int picturesCount_;

}
