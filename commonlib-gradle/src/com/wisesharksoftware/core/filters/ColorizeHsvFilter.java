package com.wisesharksoftware.core.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Map.Entry;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Image2;

public class ColorizeHsvFilter extends Filter
{
    private static final long serialVersionUID = 1L;

    private final static String HUE = "h";
    private final static String SATURATION = "s";
    private final static String VALUE = "v";
    
    private int hue = -1; // 0 - 360 // not assigned == -1
	private int saturation = 0;  // 0 - 100
    private int value = 0;  // -100 .. +100
    
	public int getHue() {
		return hue;
	}

	public int getSaturation() {
		return saturation;
	}

	public int getValue() {
		return value;
	}

	public void setHue(int val) {
		hue = val;
	}

	public void setSaturation(int val) {
		saturation = val;
	}

	public void setValue(int val) {
		value = val;
	}

	public ColorizeHsvFilter() {
    	filterName = FilterFactory.COLORIZE_FILTER;
    }
    
    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramEntry : params.entrySet() )
        {
            String name = paramEntry.getKey();
            String value = paramEntry.getValue();
            
            if( name.equals( HUE ) )
            {
                hue = constrain( Integer.parseInt( value ), 0, 360 );
            }
            else if( name.equals( SATURATION ) )
            {
                saturation = constrain( Integer.parseInt( value ), 0, 100 );
            }
            else if( name.equals( VALUE ) )
            {
                this.value = constrain( Integer.parseInt( value ), -100, +100 );
            }
        }
    }

    @Override
    public void processImage( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
    {
        try
        {
            float saturation = ( float )( this.saturation / 100.0 );
            float value = ( float )( this.value / 100.0 );
            float[] hsv = new float[ 3 ];

            if( value >= 0 )
            {
                for( int i = 0; i < image.height; i++ )
                {
                    for( int j = 0; j < image.width; j++ )
                    {
                        Color.colorToHSV( image.data[ i ][ j ], hsv );
                        hsv[ 0 ] = hue;
                        hsv[ 1 ] = saturation;
                        hsv[ 2 ] += ( 1 - hsv[ 2 ] ) * value; //Lerp(hsv[2], 1, value);         
                        image.data[ i ][ j ] = Color.HSVToColor( hsv );
                    }
                }
            }
            else
            {
                for( int i = 0; i < image.height; i++ )
                {
                    for( int j = 0; j < image.width; j++ )
                    {
                        Color.colorToHSV( image.data[ i ][ j ], hsv );
                        hsv[ 0 ] = hue;
                        hsv[ 1 ] = saturation;
                        hsv[ 2 ] += hsv[ 2 ] * value;  //Lerp(hsv[2], 0, -value);        
                        image.data[ i ][ j ] = Color.HSVToColor( hsv );
                    }
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "ColorizeHsvFilter" );
        }
    }
   
    @Override
    public void processBitmap( Bitmap bitmap, Context context, boolean square, boolean isPortraitPhoto )
    {
        nativeProcessing( bitmap, hue, saturation, value );
    }

    @Override
    public boolean hasNativeProcessing()
    {
        return false;
    }
    
    @Override
    public boolean processOpenCV(Context context, String srcPath, String outPath) {
        return colorizeHsvFilterOpenCV(srcPath, outPath, hue, saturation, value);
    }

    private static native boolean colorizeHsvFilterOpenCV(String inFileName, String outFileName, int hue, int saturation, int value);
    
    @Deprecated
    private static native void nativeProcessing( Bitmap bitmap, int h, int s, int v );
    
    @Override
    public String convertToJSON() {
    	String s = "{";
    	s += "\"type\":" + "\"" + filterName + "\",";
    	//start params array
    	s += "\"params\":" + "[";
    	//param hue
    	if (hue >= 0) {
        	s += "{";
        	s += "\"name\":" + "\"" + HUE + "\",";
        	s += "\"value\":" + "\"" + hue + "\"";
        	s += "},";
    	
        	//param value
        	s += "{";
        	s += "\"name\":" + "\"" + VALUE + "\",";
        	s += "\"value\":" + "\"" + value + "\"";
        	s += "},";
    	}
    	//param saturation
    	s += "{";
    	s += "\"name\":" + "\"" + SATURATION + "\",";
    	s += "\"value\":" + "\"" + saturation + "\"";
    	s += "}";
    	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }
}
