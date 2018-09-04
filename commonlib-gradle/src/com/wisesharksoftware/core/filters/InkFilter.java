package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

public class InkFilter extends Filter
{
    private static final long serialVersionUID = 1L;
    
    public InkFilter() {
    	filterName = FilterFactory.INK_FILTER;
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
     
    @Deprecated
    private static native void nativeProcessing( Bitmap bitmap, int[] redSpline, int[] greenSpline, int[] blueSpline );
      
    @Override
    public String convertToJSON() {
    	String s = "{";
    	s += "\"type\":" + "\"" + filterName + "\",";
    	//start params array
    	s += "\"params\":" + "[";
    	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }

}
