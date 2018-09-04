package com.wisesharksoftware.core;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Map;

public abstract class Filter implements Serializable
{
    private static final long serialVersionUID = 1L;
    protected Map<String, String> params;
    protected String outFileName;
    protected String filterName = "ABSTRACT_FILTER";
    
    public void setOutFileName( String outFileName )
    {
        this.outFileName = outFileName;
    }

    public final void setParams( Map<String, String> params )
    {
        this.params = params;
        onSetParams();
    }

    public final void addParam( String name, String value )
    {
        params.put( name, value );
        onSetParams();
    }

    public Map<String, String> getParams()
    {
        return params;
    }

    protected static int constrain( int value, int min, int max )
    {
        if( value < min )
        {
            return min;
        }
        if( value > max )
        {
            return max;
        }
        return value;
    }

    protected static float constrain( float value, float min, float max )
    {
        if( value < min )
        {
            return min;
        }
        if( value > max )
        {
            return max;
        }
        return value;
    }

    protected abstract void onSetParams();

    public boolean init( Context context )
    {
      return false;
    }

    public void clean()
    {
    }
    
    public boolean processOpenCV( Context context, String srcPath, String outPath) {
      return true;
    }
    
    public void processBitmap( Bitmap bitmap, Context context, boolean square, boolean isPortraitPhoto )
    {
    }

    public void processImage( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
    {
    }

    public abstract boolean hasNativeProcessing();
    
    public boolean hd()
    {
//        return true;
        return false;
    }

	public String getFilterName() {
		return filterName;
	}
	
	public String convertToJSON() {
		return null;
	}
}
