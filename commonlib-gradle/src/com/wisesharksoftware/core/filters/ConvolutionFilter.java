package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;

import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class ConvolutionFilter extends Filter
{
    private static final long serialVersionUID = 1L;
    
    private String kernel = "1,1,1, 1,1,1, 1,1,1";
    private boolean normalize = false;
    private final static String KERNEL = "kernel";
    private int size = 0;
    
    public ConvolutionFilter() {
    	filterName = FilterFactory.CONVOLUTION_FILTER;
    }
    
    public void setKernel(int v1, int v2, int v3, int v4, int v5, int v6, int v7, int v8, int v9) {
    	kernel = v1 + "," + v2 + "," + v3 + "," + v4 + "," + v5 + "," + v6 + "," + v7 + "," + v8 + "," + v9;
    }
    
    public void setKernel(double v1, double v2, double v3, double v4, double v5, double v6, double v7, double v8, double v9) {
    	kernel = v1 + "," + v2 + "," + v3 + "," + v4 + "," + v5 + "," + v6 + "," + v7 + "," + v8 + "," + v9;
    }
    
    public void setNormalize(boolean normalize) {
    	this.normalize = normalize;
    }
    
    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();
            
            if (name.equals( KERNEL ) )
            {
                kernel = value;
            }
            if (name.equals("normalize")) {
            	if (value.equals("true")) {
            		normalize = true;
            	}
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
    	
    	//param kernel
    	s += "{";
    	s += "\"name\":" + "\"" + KERNEL + "\",";
    	s += "\"value\":" + "\"" + kernel + "\"";
    	s += "},";
    	//param normalize
    	s += "{";
    	s += "\"name\":" + "\"" + "normalize" + "\",";
    	s += "\"value\":" + "\"" + normalize + "\"";
    	s += "}";
    	
    	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }
}
