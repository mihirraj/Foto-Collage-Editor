package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;

import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class FaceDetectionFilter extends Filter {
	private static final long serialVersionUID = 1L;
	private double whitenEyeMaxCoef = 0;
	private double whitenTeethMaxCoef = 0;
	private int balanseFaceColorAlpha = 0;
	private int smoothSkinAlpha = 0;
	private int unsharpEyeAlpha = 0;
	private int brightness = 0;
	private int temperature = 0;
	private boolean preview = false;
		
	public int getUnsharpEyeAlpha() {
		return unsharpEyeAlpha;
	}

	public void setUnsharpEyeAlpha(int unsharpEyeAlpha) {
		this.unsharpEyeAlpha = unsharpEyeAlpha;
	}
	
	public int getBalanseFaceColor() {
		return balanseFaceColorAlpha;
	}

	public void setBalanseFaceColorAlpha(int balanseFaceColorAlpha) {
		this.balanseFaceColorAlpha = balanseFaceColorAlpha;
	}

	public double getWhitenEyeMaxCoef() {
		return whitenEyeMaxCoef;
	}

	public void setWhitenEyeMaxCoef(double max_coef) {
		this.whitenEyeMaxCoef = max_coef;
	}

	public double getWhitenTeethMaxCoef() {
		return whitenTeethMaxCoef;
	}

	public void setWhitenTeethMaxCoef(double max_coef) {
		this.whitenTeethMaxCoef = max_coef;
	}

	public int getSmoothSkinAlpha() {
		return smoothSkinAlpha;
	}

	public void setSmoothSkinAlpha(int smoothSkinAlpha) {
		this.smoothSkinAlpha = smoothSkinAlpha;
	}

	public int getBrightness() {
		return brightness;
	}

	public void setBrightness(int brightness) {
		this.brightness = brightness;
	}
	
	public int getTemperature() {
		return temperature;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}
	
	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean preview) {
		this.preview = preview;
	}
	
	public FaceDetectionFilter() {
		filterName = FilterFactory.FACE_DETECTION_FILTER;
	}

	@Override
	protected void onSetParams() {
		for (Entry<String, String> paramsEntry : params.entrySet()) {
			String name = paramsEntry.getKey();
			String value = paramsEntry.getValue();

			if (name.equals("whiten_eye_max_coef")) {
				whitenEyeMaxCoef = Double.parseDouble(value);
			}			
			if (name.equals("smooth_skin_alpha")) {
				smoothSkinAlpha = Integer.parseInt(value);
			}						
			if (name.equals("balanse_face_color_alpha")) {
				balanseFaceColorAlpha = Integer.parseInt(value);
			}
			if (name.equals("unsharp_eye_alpha")) {
				unsharpEyeAlpha = Integer.parseInt(value); 
			}
			if (name.equals("brightness")) {
				brightness = Integer.parseInt(value);
			}
			if (name.equals("temperature")) {
				temperature = Integer.parseInt(value);
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
		// param whiten_eye_max_coef
		s += "{";
		s += "\"name\":" + "\"" + "whiten_eye_max_coef" + "\",";
		s += "\"value\":" + "\"" + whitenEyeMaxCoef + "\"";
		s += "},";
		// param whiten_teeth_max_coef
		s += "{";
		s += "\"name\":" + "\"" + "whiten_teeth_max_coef" + "\",";
		s += "\"value\":" + "\"" + whitenTeethMaxCoef + "\"";
		s += "},";
		// param smooth_skin_alpha
		s += "{";
		s += "\"name\":" + "\"" + "smooth_skin_alpha" + "\",";
		s += "\"value\":" + "\"" + smoothSkinAlpha + "\"";
		s += "},";
		// param balans_face_color_alpha
		s += "{";
		s += "\"name\":" + "\"" + "balanse_face_color_alpha" + "\",";
		s += "\"value\":" + "\"" + balanseFaceColorAlpha + "\"";
		s += "},";
		// param unsharp_eye_alpha
		s += "{";
		s += "\"name\":" + "\"" + "unsharp_eye_alpha" + "\",";
		s += "\"value\":" + "\"" + unsharpEyeAlpha + "\"";
		s += "},";						
		// param brightness
		s += "{";
		s += "\"name\":" + "\"" + "brightness" + "\",";
		s += "\"value\":" + "\"" + brightness + "\"";
		s += "},";
		// param temperature
		s += "{";
		s += "\"name\":" + "\"" + "temperature" + "\",";
		s += "\"value\":" + "\"" + temperature + "\"";
		s += "},";
		// param temperature
		s += "{";
		s += "\"name\":" + "\"" + "preview" + "\",";
		s += "\"value\":" + "\"" + isPreview() + "\"";
		s += "}";
		s += "]";
		// end params array
		s += "}";
		return s;
	}

}