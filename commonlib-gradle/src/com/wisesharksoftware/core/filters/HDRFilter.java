package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class HDRFilter extends Filter
{
    private static final long serialVersionUID = 1L;
    
    String shadows_curve;
    String highlights_curve;
    String midtone_curve;
    
    private final static String SHADOWS_CURVE = "shadows_curve";
    private final static String HIGHLIGHTS_CURVE = "highlights_curve";
    private final static String MIDTONE_CURVE = "midtone_curve";
    
    public HDRFilter() {
    	filterName = FilterFactory.HDR_FILTER;
    }
    
    public void setShadowsCurve(int v1, int v2, int v3, int v4, int v5, int v6) {
    	shadows_curve = v1 + "," + v2 + ";" + v3 + "," + v4 + ";" + v5 + "," + v6;
    }
    
    public void setHighlightsCurve(int v1, int v2, int v3, int v4, int v5, int v6) {
    	highlights_curve = v1 + "," + v2 + ";" + v3 + "," + v4 + ";" + v5 + "," + v6;
    }
    
    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();
            
            if( name.equals( SHADOWS_CURVE ) )
            {
                shadows_curve = value;
            }
            else if( name.equals( HIGHLIGHTS_CURVE ) )
            {
                highlights_curve = value;
            }
            else if( name.equals( MIDTONE_CURVE ) )
            {
                midtone_curve = value;
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
    	if (shadows_curve != null && shadows_curve.length() > 0) {
        	//param shadows_curve
        	s += "{";
        	s += "\"name\":" + "\"" + SHADOWS_CURVE + "\",";
        	s += "\"value\":" + "\"" + shadows_curve + "\"";
        	s += "}";
    	}
    	if (highlights_curve != null && highlights_curve.length() > 0) {
    		if (shadows_curve != null && shadows_curve.length() > 0) {
    			s += ",";
    		}
        	//param highlights_curve
        	s += "{";
        	s += "\"name\":" + "\"" + HIGHLIGHTS_CURVE + "\",";
        	s += "\"value\":" + "\"" + highlights_curve + "\"";
        	s += "}";
    	}
    	if (midtone_curve != null && midtone_curve.length() > 0) {
    		if ((shadows_curve != null && shadows_curve.length() > 0) ||
    			((highlights_curve != null && highlights_curve.length() > 0))) {
    			s += ",";
    		}
        	
        	//param midtone_curve
        	s += "{";
        	s += "\"name\":" + "\"" + MIDTONE_CURVE + "\",";
        	s += "\"value\":" + "\"" + midtone_curve + "\"";
        	s += "}";
    	}
    	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }
}
