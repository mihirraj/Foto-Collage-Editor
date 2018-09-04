package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class HDRFilter2 extends Filter {
	private static final long serialVersionUID = 1L;
	public static final int ALGORITHM_OLD_HDR = 0;
	public static final int ALGORITHM_HDR = 1;
	public static final int ALGORITHM_MIDTONES = 2;

	private int alpha = 0;
	private int blurSize = 1;
	private int black = 0;
	
	private int algorithm = ALGORITHM_OLD_HDR;

	public HDRFilter2() {
		filterName = FilterFactory.HDR_FILTER2;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getBlurSize() {
		return blurSize;
	}

	public void setBlurSize(int blurSize) {
		this.blurSize = blurSize;
	}
	
	public int getBlack() {
		return black;
	}

	public void setBlack(int black) {
		this.black = black;
	}

	public int getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(int algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	protected void onSetParams() {
		for (Entry<String, String> paramsEntry : params.entrySet()) {
			String name = paramsEntry.getKey();
			String value = paramsEntry.getValue();

			if (name.equals("alpha")) {
				setAlpha(Integer.parseInt(value));
			}
			if (name.equals("blur_size")) {
				setBlurSize(Integer.parseInt(value));
			}
			if (name.equals("black")) {
				setBlack(Integer.parseInt(value));
			}
			if (name.equals("algorithm")) {
				if (value.equals("old_hdr")) {
					setAlgorithm(ALGORITHM_OLD_HDR);
				}
				if (value.equals("hdr")) {
					setAlgorithm(ALGORITHM_HDR);
				}
				if (value.equals("midtones")) {
					setAlgorithm(ALGORITHM_MIDTONES);
				}
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
		// param alpha
		s += "{";
		s += "\"name\":" + "\"" + "alpha" + "\",";
		s += "\"value\":" + "\"" + getAlpha() + "\"";
		s += "},";
		// param blurSize
		s += "{";
		s += "\"name\":" + "\"" + "blurSize" + "\",";
		s += "\"value\":" + "\"" + blurSize + "\"";
		s += "},";
		// param black
		s += "{";
		s += "\"name\":" + "\"" + "black" + "\",";
		s += "\"value\":" + "\"" + black + "\"";
		s += "},";
		// param newAlgorithm
		s += "{";
		s += "\"name\":" + "\"" + "algorithm" + "\",";
		switch (algorithm) {
		case ALGORITHM_OLD_HDR: {
			s += "\"value\":" + "\"" + "old_hdr" + "\"";
			break;
		}
		case ALGORITHM_HDR: {
			s += "\"value\":" + "\"" + "hdr" + "\"";
			break;
		}
		case ALGORITHM_MIDTONES: {
			s += "\"value\":" + "\"" + "midtones" + "\"";
			break;
		}
		}
		s += "}";
		s += "]";
		// end params array
		s += "}";
		return s;
	}
}
