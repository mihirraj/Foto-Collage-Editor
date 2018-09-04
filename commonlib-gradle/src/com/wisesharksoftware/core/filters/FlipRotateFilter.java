package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class FlipRotateFilter extends Filter {
	private static final long serialVersionUID = 1L;
	private int angle = 0;
	private boolean flipVertical = false;
	private boolean flipHorizontal = false;

	public FlipRotateFilter() {
		filterName = FilterFactory.FLIP_ROTATE_FILTER;
	}

	@Override
	protected void onSetParams() {
		for (Entry<String, String> paramsEntry : params.entrySet()) {
			String name = paramsEntry.getKey();
			String value = paramsEntry.getValue();

			if (name.equals("angle")) {
				angle = Integer.parseInt(value);
			}
			if (name.equals("flip_vertical")) {
				if (value.equals("true")) {
					flipVertical = true;
				}
			}
			if (name.equals("flip_horizontal")) {
				if (value.equals("true")) {
					flipHorizontal = true;
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
		// param angle
		s += "{";
		s += "\"name\":" + "\"" + "angle" + "\",";
		s += "\"value\":" + "\"" + angle + "\"";
		s += "},";
		s += "{";
		s += "\"name\":" + "\"" + "flip_vertical" + "\",";
		s += "\"value\":" + "\"" + flipVertical + "\"";
		s += "},";
		s += "{";
		s += "\"name\":" + "\"" + "flip_horizontal" + "\",";
		s += "\"value\":" + "\"" + flipHorizontal + "\"";
		s += "}";
		s += "]";
		// end params array
		s += "}";
		return s;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}
	
	public void setFlipVertical(boolean flipVertical) {
		this.flipVertical = flipVertical;
	}	
	
	public void setFlipHorizontal(boolean flipHorizontal) {
		this.flipHorizontal = flipHorizontal;
	}
}
