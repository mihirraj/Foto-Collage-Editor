package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

public class MirrorFilter extends Filter
{
    private static final long serialVersionUID = 1L;
    
	public MirrorFilter() {
    	filterName = FilterFactory.MIRROR_FILTER;
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
    private static native void nativeProcessing( Bitmap bitmap, int h, int s, int v );
    
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
