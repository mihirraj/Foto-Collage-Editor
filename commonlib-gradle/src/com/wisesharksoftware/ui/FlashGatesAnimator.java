package com.wisesharksoftware.ui;

import android.app.Activity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class FlashGatesAnimator {

	public final static int STATE_CLOSED = 0;
	public final static int STATE_OPEN = 1;

	private Activity parentActivity;
	private ImageView flashGate;
	private int duration;
	private int state;
	
	public FlashGatesAnimator(Activity parentActivity, int flashGateId, int duration) {
		this.parentActivity = parentActivity;
		flashGate = (ImageView) this.parentActivity.findViewById(flashGateId);
		if (flashGate != null) {
			flashGate.setVisibility(View.INVISIBLE);
		}
		this.duration = duration;
		state = STATE_OPEN;
	}

	public void close() {
		state = STATE_CLOSED;
		AlphaAnimation animateAlpha = null;
		animateAlpha = new AlphaAnimation(0f, 1.0f);
		startShowAnimation(animateAlpha, flashGate);		
	}

	public void open() {
		state = STATE_OPEN;
	}

	public void hide() {
		if (flashGate != null) {
			flashGate.setVisibility(View.INVISIBLE);
		}
		state = STATE_OPEN;
	}

	public void show() {
		if (flashGate != null) {
			flashGate.setVisibility(View.VISIBLE);
		}
		state = STATE_CLOSED;
	}

	public boolean isGatesClosed() {
		return state == STATE_CLOSED;
	}

	private void startShowAnimation(final AlphaAnimation animation,
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
				view.setVisibility(View.INVISIBLE);
			}
		});
		view.startAnimation(animation);
	}
}
