package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class StickerFilter extends Filter {
	private static final long serialVersionUID = 1L;
	private String path = "";
	private double scaleW = 1.0;
	private double scaleH = 1.0;
	private double angle = 0.0;
	private double x = 0;
	private double y = 0;
	private int color = -1;
	private int alpha = 255;

	public double getScaleW() {
		return scaleW;
	}

	public void setScaleW(double scaleW) {
		this.scaleW = scaleW;
	}

	public double getScaleH() {
		return scaleH;
	}

	public void setScaleH(double scaleH) {
		this.scaleH = scaleH;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	public StickerFilter() {
		filterName = FilterFactory.STICKER_FILTER;
	}

	@Override
	protected void onSetParams() {
		for (Entry<String, String> paramsEntry : params.entrySet()) {
			String name = paramsEntry.getKey();
			String value = paramsEntry.getValue();

			if (name.equals("path")) {
				path = value;
			}

			if (name.equals("scale_w")) {
				scaleW = Double.parseDouble(value);
			}
			if (name.equals("scale_h")) {
				scaleH = Double.parseDouble(value);
			}
			if (name.equals("angle")) {
				angle = Double.parseDouble(value);
			}

			if (name.equals("x")) {
				x = Double.parseDouble(value);
			}
			if (name.equals("y")) {
				y = Double.parseDouble(value);
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
		// param path
		s += "{";
		s += "\"name\":" + "\"" + "path" + "\",";
		s += "\"value\":" + "\"" + path + "\"";
		s += "},";
		// param scale_w
		s += "{";
		s += "\"name\":" + "\"" + "scale_w" + "\",";
		s += "\"value\":" + "\"" + scaleW + "\"";
		s += "},";
		// param scale_h
		s += "{";
		s += "\"name\":" + "\"" + "scale_h" + "\",";
		s += "\"value\":" + "\"" + scaleH + "\"";
		s += "},";
		// param color
		s += "{";
		s += "\"name\":" + "\"" + "color" + "\",";
		s += "\"value\":" + "\"" + color + "\"";
		s += "},";
		// param alpha
		s += "{";
		s += "\"name\":" + "\"" + "alpha" + "\",";
		s += "\"value\":" + "\"" + alpha + "\"";
		s += "},";
		// param angle
		s += "{";
		s += "\"name\":" + "\"" + "angle" + "\",";
		s += "\"value\":" + "\"" + angle + "\"";
		s += "},";
		// param x
		s += "{";
		s += "\"name\":" + "\"" + "x" + "\",";
		s += "\"value\":" + "\"" + x + "\"";
		s += "},";
		// param y
		s += "{";
		s += "\"name\":" + "\"" + "y" + "\",";
		s += "\"value\":" + "\"" + y + "\"";
		s += "}";
		s += "]";
		// end params array
		s += "}";
		return s;
	}
}
