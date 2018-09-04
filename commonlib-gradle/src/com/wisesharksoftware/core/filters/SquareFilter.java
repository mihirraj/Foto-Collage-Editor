package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class SquareFilter extends Filter
{
    private static final long serialVersionUID = 1L;
    
    public SquareFilter() {
    	filterName = FilterFactory.SQUARE_FILTER;
    }
    
    @Override
    protected void onSetParams()
    {
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
    	//param kernel size
      	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }
}
