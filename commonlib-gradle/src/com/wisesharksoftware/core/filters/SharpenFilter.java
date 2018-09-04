package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;

import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class SharpenFilter extends Filter
{
    private static final long serialVersionUID = 1L;
    
    String mask = "";
    
    private final static String MASK = "mask";
    
    public SharpenFilter() {
    	filterName = FilterFactory.SHARPEN_FILTER;
    }
    
    public void setSharpen(int v1, int v2, int v3, int v4, int v5, int v6, int v7, int v8, int v9) {
    	mask = v1 + "," + v2 + "," + v3 + "," + v4 + "," + v5 + "," + v6 + "," + v7 + "," + v8 + "," + v9;
    }
    
    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();
            
            if( name.equals( MASK ) )
            {
                mask = value;
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
    	//param mask
    	s += "{";
    	s += "\"name\":" + "\"" + MASK + "\",";
    	s += "\"value\":" + "\"" + mask + "\"";
    	s += "}";
    	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }
}
