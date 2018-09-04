package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;

import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class PerspectiveTransformFilter extends Filter {
	private static final long serialVersionUID = 1L;
	private String corners;
	
	public PerspectiveTransformFilter() {
		filterName = FilterFactory.PERSPECTIVE_TRANSFORM_FILTER;
	}

	@Override
	protected void onSetParams() {
		for (Entry<String, String> paramsEntry : params.entrySet()) {
			String name = paramsEntry.getKey();
			String value = paramsEntry.getValue();

			if (name.equals("corners")) {
				corners = value;
			}
		}
	}
	
	public void setCorners(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
		corners = x1 + "," + y1 + ";" + x2 + "," + y2 + ";" + x3 + "," + y3 + ";" + x4 + "," + y4;		
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
		// param corners
		s += "{";
		s += "\"name\":" + "\"" + "corners" + "\",";
		s += "\"value\":" + "\"" + corners + "\"";
		s += "}";
		s += "]";
		// end params array
		s += "}";
		return s;
	}

}