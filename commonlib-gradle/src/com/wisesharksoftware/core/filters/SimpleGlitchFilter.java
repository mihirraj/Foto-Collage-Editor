package com.wisesharksoftware.core.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Image2;

import java.util.Map.Entry;
import java.util.Random;

public class SimpleGlitchFilter extends Filter
{
    private static final long serialVersionUID = 1L;

    int dispersion;
    int value;

    private static Random random = new Random();

    private final static String DISPERSION = "d";
    private final static String VALUE = "v";

    public SimpleGlitchFilter() {
    	filterName = FilterFactory.SIMPLE_GLITCH_FILTER;
    }
    
    public void setDispersion(int val) {
      dispersion = val;
    }
    public void setValue(int val) {
      value = val;
    }

    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();
            
            if( name.equals( DISPERSION ) )
            {
                dispersion = constrain( Integer.parseInt( value ), -100, +100 );
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
            Log.d("TestLog", "point1");
            float value = ( float )( this.value / 100.0 );
            float[] hsv = new float[ 3 ];
            for( int i = 0; i < image.height; i++ )
            {
                for( int j = 0; j < image.width; j++ )
                {
                    int color = image.data[ i ][ j ];
                    
                    //int red = (color >> 16) & 0xFF;
                    //int green = (color >> 8) & 0xFF;
                    //int blue = color & 0xFF;
                    hsv[ 0 ] = hsv[ 1 ] = hsv[ 2 ] = 0;
                    //Color.RGBToHSV(red, green, blue, hsv);
                    Color.colorToHSV( color, hsv );
                   // hsv[ 0 ] = ( hsv[ 0 ] + hue ) % 360;
                    hsv[ 0 ] += hsv[ 0 ] < 0 ? 360 : 0;
                    hsv[ 2 ] += value >= 0 ? ( 1 - hsv[ 2 ] ) * value : hsv[ 2 ] * value;

                    int new_col = Color.HSVToColor( hsv );

                    int red = ( new_col >> 16 ) & 0xFF;
                    int green = ( new_col >> 8 ) & 0xFF;
                    int blue = new_col & 0xFF;

                    // saturation
                    int new_red = red > 255 ? 255 : red < 0 ? 0 : red;
                    int new_green = green > 255 ? 255 : green < 0 ? 0 : green;
                    int new_blue = blue > 255 ? 255 : blue < 0 ? 0 : blue;
                    image.data[ i ][ j ] = ( 0xFF << 24 ) | ( random.nextInt(256) << 16 ) | ( random.nextInt(256) << 8 ) | random.nextInt(256);

                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "SimpleGlitchFilter" );
        }
    }

    @Override
    public void processBitmap( Bitmap bitmap, Context context, boolean square, boolean isPortraitPhoto )
    {
        nativeProcessing( bitmap, dispersion, value );
    }

    @Override
    public boolean hasNativeProcessing()
    {
        return false;
    }

    @Override
    public boolean processOpenCV(Context context, String srcPath, String outPath) {
        Log.d("TestLog", "point2");
      return simpleGlitchFilterOpenCV(srcPath, outPath, dispersion, value);
    }

    private static native boolean simpleGlitchFilterOpenCV(String inFileName, String outFileName, int dispersion, int value);

    
    @Deprecated
    private static native void nativeProcessing( Bitmap bitmap, int d, int v );
    
    @Override
    public String convertToJSON() {
    	String s = "{";
    	s += "\"type\":" + "\"" + filterName + "\",";
    	//start params array
    	s += "\"params\":" + "[";
    	//param hue
    	s += "{";
    	s += "\"name\":" + "\"" + DISPERSION + "\",";
    	s += "\"value\":" + "\"" + dispersion + "\"";
    	s += "},";
    	//param value
    	s += "{";
    	s += "\"name\":" + "\"" + VALUE + "\",";
    	s += "\"value\":" + "\"" + value + "\"";
    	s += "}";
    	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }
}
