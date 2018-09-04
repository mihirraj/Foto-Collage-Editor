package com.wisesharksoftware.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class GatesAnimator {

	public GatesAnimator(Activity parentActivity, int upGateId, int downGateId,
			int visibility, int duration) {
		this.parentActivity = parentActivity;
		orientation = parentActivity.getResources().getConfiguration().orientation;
		upGate = (ImageView) this.parentActivity.findViewById(upGateId);
		downGate = (ImageView) this.parentActivity.findViewById(downGateId);
		if (upGate != null) {
			upGate.setVisibility(visibility);
		}
		if (downGate != null) {
			downGate.setVisibility(visibility);
		}
		this.duration = duration;
		state = visibility == View.VISIBLE ? STATE_CLOSED : STATE_OPEN;
	}

	public void close() {
		state = STATE_CLOSED;
		TranslateAnimation animateUp = null;
		TranslateAnimation animateDown = null;

		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			int upWidth = upGate != null ? -upGate.getWidth() : 0;
			animateUp = new TranslateAnimation(upWidth, 0, 0, 0);
			int downWidth = downGate != null ? downGate.getWidth() : 0;
			animateDown = new TranslateAnimation(downWidth, 0, 0, 0);
		} else {
			int upHeight = upGate != null ? -upGate.getHeight() : 0;
			animateUp = new TranslateAnimation(0, 0, upHeight, 0);
			int downHeight = downGate != null ? downGate.getHeight() : 0;
			animateDown = new TranslateAnimation(0, 0, downHeight, 0);
		}

		startShowAnimation(animateUp, upGate);
		startShowAnimation(animateDown, downGate);
	}

	public void open() {
		state = STATE_OPEN;
		TranslateAnimation animateUp = null;
		TranslateAnimation animateDown = null;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			animateUp = new TranslateAnimation(0, -upGate.getWidth(), 0, 0);
			animateDown = new TranslateAnimation(0, downGate.getWidth(), 0, 0);

		} else {
			animateUp = new TranslateAnimation(0, 0, 0, -upGate.getHeight());
			animateDown = new TranslateAnimation(0, 0, 0, downGate.getHeight());
		}
		startHideAnimation(animateUp, upGate);
		startHideAnimation(animateDown, downGate);
	}

	public void hide() {
		if (upGate != null) {
			upGate.setVisibility(View.INVISIBLE);
		}
		if (downGate != null) {
			downGate.setVisibility(View.INVISIBLE);
		}
		state = STATE_OPEN;
	}

	public void show() {
		if (upGate != null) {
			upGate.setVisibility(View.VISIBLE);
		}
		if (downGate != null) {
			downGate.setVisibility(View.VISIBLE);
		}
		state = STATE_CLOSED;
	}

	public boolean isGatesClosed() {
		return state == STATE_CLOSED;
	}

	private void startShowAnimation(final TranslateAnimation animation,
			final View view) {
		animation.setDuration(duration);
		animation.setFillAfter(false);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});
		view.startAnimation(animation);
	}

	private void startHideAnimation(final TranslateAnimation animation,
			final View view) {
		animation.setDuration(duration);
		animation.setFillAfter(false);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.INVISIBLE);
			}
		});
		view.startAnimation(animation);
	}

	private Activity parentActivity;
	private ImageView upGate;
	private ImageView downGate;
	private int orientation;
	private int duration;
	private int state;

	public final static int STATE_CLOSED = 0;
	public final static int STATE_OPEN = 1;
}
