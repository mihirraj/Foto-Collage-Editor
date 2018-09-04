package com.wisesharksoftware.service;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.FocusFilter;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;
import com.wisesharksoftware.sticker.FocusImageView;
import com.wisesharksoftware.sticker.ImageViewDrawableOverlay;
import com.photostudio.photoeditior.R;

public class FocusService extends BaseService {
	private ImageViewTouch mImageView;
	private ImageView backgroundImage;
	private FocusImageView mFocusImageView;
	private Bitmap mBlurBitmap;
	private FocusFilter focusFilter;

	public FocusService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		setChooseProcessing(a);
		setPanelManager(m);
		setAction(action);
		setActionGroup(actionGroup);
		mImageView = (ImageViewDrawableOverlay) a
				.findViewById(R.id.image_overlay);
		backgroundImage = (ImageView) a.findViewById(R.id.backgroundImage);
		mFocusImageView = (FocusImageView) a.findViewById(R.id.focus_image);
	}

	@Override
	public IOkCancelListener getOkCancelListener() {
		return new IOkCancelListener() {

			@Override
			public void onStop(Object... params) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onOK() {
				applyFocus();

			}

			@Override
			public void onRestore() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onLocked(boolean lock) {
				applyFocus();

			}

			@Override
			public void onChange(Object... params) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onCancel() {
				disableFocus();
				panelManager.upLevel();

			}

			@Override
			public void onShow() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onChangeFromUser(Object... params) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	public OnStateListener getOnStateListener() {
		return new OnStateListener() {

			@Override
			public void onShow(List<LauncherItemView> items) {
				ServicesManager.instance().setCurrentService(FocusService.this);
				// String focusName = buttonName;
				// cropProductIds = productIds;
				chooseProcessing.showCustomToast("focus");
				//
				Utils.reportFlurryEvent("FocusChoosed", "");
				Utils.reportFlurryEvent("Action", "Focus");
				// Log.d("processing", "FocusChoosed" + focusName);

				enableFocus();
			}

			@Override
			public void onHide() {
				disableFocus();
			}
		};
	}

	public void enableFocus() {
		if (mBlurBitmap != null && !mBlurBitmap.isRecycled()) {
			backgroundImage.setImageBitmap(null);
			mBlurBitmap.recycle();
			mBlurBitmap = null;
		}

		mImageView.setVisibility(View.GONE);
		// mFocusImageView.setBackgroundDrawable(new BitmapDrawable(mBitmap));
		mBlurBitmap = fastblur(chooseProcessing.mBitmap, 5);
		backgroundImage.setImageBitmap(chooseProcessing.mBitmap);
		backgroundImage.setVisibility(View.VISIBLE);
		mFocusImageView.setBitmapForBlur(chooseProcessing.mBitmap);
		mFocusImageView.setImageBitmap(mBlurBitmap);
		mFocusImageView.setVisibility(View.VISIBLE);
		mFocusImageView.setBlurBitmap(mBlurBitmap);
		mFocusImageView.invalidate();

		/*
		 * Log.d("FOCUS", "ENABLE"); mImageView.setVisibility(View.GONE); //
		 * mFocusImageView.setBackgroundDrawable(new BitmapDrawable(mBitmap));
		 * mBlurBitmap = fastblur(mBitmap, 5);
		 * backgroundImage.setImageBitmap(mBitmap);
		 * backgroundImage.setVisibility(View.VISIBLE);
		 * mFocusImageView.setBitmapForBlur(mBitmap);
		 * mFocusImageView.setImageBitmap(mBlurBitmap);
		 * mFocusImageView.setVisibility(View.VISIBLE);
		 * mFocusImageView.setBlurBitmap(mBlurBitmap);
		 * mFocusImageView.invalidate();
		 */

	}

	public void disableFocus() {
		mImageView.setVisibility(View.VISIBLE);
		mFocusImageView.setVisibility(View.GONE);
		backgroundImage.setVisibility(View.GONE);

		mFocusImageView.setHighlightView(null);
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {

		ArrayList<Preset> PresetArray = new ArrayList<Preset>();
		if (focusFilter != null) {
			try {

				ArrayList<Filter> FilterArray = new ArrayList<Filter>();
				FilterArray.add(focusFilter);

				Filter[] filters = new Filter[FilterArray.size()];
				FilterArray.toArray(filters);

				Preset preset = new Preset();
				preset.setFilters(filters);

				PresetArray.add(preset);
			} catch (Exception e) {

			}
		}
		return PresetArray;
	}

	public void applyFocus() {
		if (mFocusImageView != null) {

			final double w = chooseProcessing.mBitmap.getWidth();
			final double h = chooseProcessing.mBitmap.getHeight();
			Rect cropRect = mFocusImageView.getCropRect();
			if (cropRect != null) {
				ServicesManager.instance().addToQueue(self());

				focusFilter = new FocusFilter(cropRect.top / h, cropRect.left
						/ w, cropRect.bottom / h, cropRect.right / w);
				/*
				 * chooseProcessing.processOrder.addFilter(focusFilter,
				 * "FocusFilter", false);
				 */
				disableFocus();
				chooseProcessing.processImage();
			} else {
				disableFocus();
			}

		}
	}

	public Bitmap fastblur(Bitmap sentBitmap, int radius) {

		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
		double max_coeff = -999;
		double min_coeff = 999;

		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				// int x_center = w / 2;
				// int y_center = h / 2;
				// int point_radius = 250;
				// int point_radius2 = point_radius * point_radius;
				// int radiusRing = 50;
				// int radiusRing2 = radiusRing * radiusRing;
				// int curr_radius2 = (x_center - x) * (x_center - x) +
				// (y_center - y) * (y_center - y);
				// //if (curr_radius2 > point_radius2) {
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
						| (dv[gsum] << 8) | dv[bsum];
				/*
				 * } else if (curr_radius2 > (point_radius - radiusRing) *
				 * (point_radius - radiusRing)) { double interval =
				 * Math.sqrt(curr_radius2) - (point_radius - radiusRing); double
				 * interval2 = interval * interval; double coeff = 1 - (interval
				 * / (radiusRing * 1.0)); if (coeff > max_coeff) { max_coeff =
				 * coeff; } if (coeff < min_coeff) { min_coeff = coeff; }
				 * 
				 * int tempr = (pix[yi] & 0xff0000) >> 16; int tempg = (pix[yi]
				 * & 0x00ff00) >> 8; int tempb = (pix[yi] & 0x0000ff);
				 * 
				 * pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | (
				 * dv[gsum] << 8 ) | dv[bsum]; int pixr = (pix[yi] & 0xff0000)
				 * >> 16; int pixg = (pix[yi] & 0x00ff00) >> 8; int pixb =
				 * (pix[yi] & 0x0000ff);
				 * 
				 * tempr = (int) (tempr * coeff + pixr * (1 - coeff)); tempg =
				 * (int) (tempg * coeff + pixg * (1 - coeff)); tempb = (int)
				 * (tempb * coeff + pixb * (1 - coeff));
				 * 
				 * if (tempr < 0) { tempr = 0; }
				 * 
				 * if (tempg < 0) { tempg = 0; }
				 * 
				 * if (tempb < 0) { tempb = 0; } if (tempr > 255) { tempr = 255;
				 * } if (tempg > 255) { tempg = 255; } if (tempb > 255) { tempb
				 * = 255; }
				 * 
				 * pix[yi] = ( 0xff000000 & pix[yi] ) | ( tempr << 16 ) | (
				 * tempg << 8 ) | tempb; }
				 */
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		// Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		//Log.d("AAA", "min_coeff = " + min_coeff + " max_coeff = " + max_coeff);
		return (bitmap);
	}

}
