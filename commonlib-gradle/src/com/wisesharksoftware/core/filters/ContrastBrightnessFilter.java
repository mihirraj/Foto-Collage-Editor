package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;

import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class ContrastBrightnessFilter extends Filter
{
    private static final long serialVersionUID = 1L;
    
    double contrast = 1.0f;
    int brightness = 0;
    
    private final static String CONTRAST = "contrast";
    private final static String BRIGHTNESS = "brightness";
    
    public ContrastBrightnessFilter() {
    	filterName = FilterFactory.CB_FILTER;
    }
    
    public void setContrast(double value) {
    	contrast = value;
    }
    
    public void setBrightness(int value) {
    	brightness = value;
    }
    
    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();
            
            if( name.equals( CONTRAST ) )
            {
                contrast = Double.parseDouble(value);
            }
            else if( name.equals( BRIGHTNESS ) )
            {
                brightness = Integer.parseInt(value);
            }
        }
    }

    @Override
    public boolean hasNativeProcessing()
    {
        return false;
    }
/*
    @Override
    public boolean processOpenCV(Context context, String srcPath, String outPath) {
    	return false;
    	return hsvFilterOpenCV(srcPath, outPath, hue, saturation, value);
    }

    private static native boolean hsvFilterOpenCV(String inFileName, String outFileName, int hue, int saturation, int value);
*/
    
    @Deprecated
    private static native void nativeProcessing( Bitmap bitmap, int h, int s, int v );
    
    @Override
    public String convertToJSON() {
    	String s = "{";
    	s += "\"type\":" + "\"" + filterName + "\",";
    	//start params array
    	s += "\"params\":" + "[";
    	//param shadows_curve
    	s += "{";
    	s += "\"name\":" + "\"" + CONTRAST + "\",";
    	/*
    	String sContrast = (contrast + "").replace(".", ",");
    	if (sContrast.indexOf(',') != -1) {
    		sContrast = sContrast.substring(0, sContrast.indexOf(',') + 2);
    	}*/
    	s += "\"value\":" + "\"" + contrast + "\"";
    	s += "},";
    	//param highlights_curve
    	s += "{";
    	s += "\"name\":" + "\"" + BRIGHTNESS + "\",";
    	s += "\"value\":" + "\"" + (brightness + "").replace(".", ",") + "\"";
    	s += "}";
    	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }
}
