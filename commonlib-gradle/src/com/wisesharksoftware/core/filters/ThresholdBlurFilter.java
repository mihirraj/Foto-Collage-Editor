package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;

import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class ThresholdBlurFilter extends Filter {
	private static final long serialVersionUID = 1L;
	private int threshold = 40;
	private int kernel_size = 9;
	private int alpha = -1;
	
	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public ThresholdBlurFilter() {
		filterName = FilterFactory.THRESHOLD_BLUR_FILTER;
	}

	@Override
	protected void onSetParams() {
		for (Entry<String, String> paramsEntry : params.entrySet()) {
			String name = paramsEntry.getKey();
			String value = paramsEntry.getValue();

			if (name.equals("threshold")) {
				threshold = Integer.parseInt(value);
			}
			if (name.equals("kernel_size")) {
				kernel_size = Integer.parseInt(value);
			}
			if (name.equals("alpha")) {
				alpha = Integer.parseInt(value);	
			}
		}
	}

	@Override
	public boolean hasNativeProcessing() {
		return false;
	}

	/*
	 * @Override public boolean processOpenCV(Context context, String srcPath,
	 * String outPath) { return false; return hsvFilterOpenCV(srcPath, outPath,
	 * hue, saturation, value); }
	 * 
	 * private static native boolean hsvFilterOpenCV(String inFileName, String
	 * outFileName, int hue, int saturation, int value);
	 */

	@Deprecated
	private static native void nativeProcessing(Bitmap bitmap, int h, int s,
			int v);

	@Override
	public String convertToJSON() {
		String s = "{";
		s += "\"type\":" + "\"" + filterName + "\",";
		// start params array
		s += "\"params\":" + "[";
		// param threshold
		s += "{";
		s += "\"name\":" + "\"" + "threshold" + "\",";
		s += "\"value\":" + "\"" + threshold + "\"";
		s += "},";
		// param alpha
		if ((alpha >= 0) && (alpha <= 255)) {
		s += "{";
		s += "\"name\":" + "\"" + "alpha" + "\",";
		s += "\"value\":" + "\"" + alpha + "\"";
		s += "},";
		}
		// param kernel size
		s += "{";
		s += "\"name\":" + "\"" + "kernel_size" + "\",";
		s += "\"value\":" + "\"" + kernel_size + "\"";
		s += "},";
		s += "]";
		// end params array
		s += "}";
		return s;
	}
}