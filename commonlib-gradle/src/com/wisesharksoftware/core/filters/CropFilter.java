package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class CropFilter extends Filter
{
    private static final long serialVersionUID = 1L;
    private double top = 0.0;
	private double left = 0.0;
    private double bottom = 1.0;
	private double right = 1.0;
	private double ratio = 1.0;
    private boolean fixed = true;
    
	public CropFilter() {
    	filterName = FilterFactory.CROP_FILTER;
    }

	public CropFilter(double top, double left, double bottom, double right) {
    	this();
    	this.top = top;
    	this.left = left;
    	this.bottom = bottom;
    	this.right = right;
    }
    
	public boolean isFixed() {
		return fixed;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}
	
    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();
            
            if( name.equals( "top" ) )
            {
                top = Double.parseDouble(value);
            }
            if( name.equals( "left" ) )
            {
                left = Double.parseDouble(value);
            }
            if( name.equals( "right" ) )
            {
                right = Double.parseDouble(value);
            } 
            if( name.equals( "bottom" ) )
            {
                bottom = Double.parseDouble(value);
            }
            if( name.equals( "fixed" ) )
            {            	
            	fixed =  Boolean.parseBoolean(value);
            }        
            if( name.equals( "ratio" ) )
            {
            	try {
                    String[] vals = value.split(":");
                    if (vals.length > 1) {
                 	   ratio = Double.parseDouble(vals[0]) / Double.parseDouble(vals[1]);
                    } else {
                 	   ratio = Double.parseDouble(value);
                    }
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            } 
        }
    }
    
    public void setRatio(double ratio) {
    	this.ratio = ratio;
    }
    
    public double getRatio() {
    	return ratio;
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
    	//param top
      	s += "{";
      	s += "\"name\":" + "\"" + "top" + "\",";
      	s += "\"value\":" + "\"" + top + "\"";
      	s += "},";
        //param left
      	s += "{";
      	s += "\"name\":" + "\"" + "left" + "\",";
      	s += "\"value\":" + "\"" + left + "\"";
      	s += "},";
        //param right
      	s += "{";
      	s += "\"name\":" + "\"" + "right" + "\",";
      	s += "\"value\":" + "\"" + right + "\"";
      	s += "},";
        //param bottom
      	s += "{";
      	s += "\"name\":" + "\"" + "bottom" + "\",";
      	s += "\"value\":" + "\"" + bottom + "\"";
      	s += "}";
    	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }
}
