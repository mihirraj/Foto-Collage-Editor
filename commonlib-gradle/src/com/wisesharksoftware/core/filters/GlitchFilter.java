package com.wisesharksoftware.core.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Image2;

import java.util.Map.Entry;

public class GlitchFilter extends Filter
{
    private static final long serialVersionUID = 1L;

    int hue;
    int saturation;
    int value;

    static final float rwgt = 0.3086f;
    static final float gwgt = 0.6094f;
    static final float bwgt = 0.0820f;

    private final static String HUE = "h";
    private final static String SATURATION = "s";
    private final static String VALUE = "v";

    public GlitchFilter() {
    	filterName = FilterFactory.GLITCH_FILTER;
    }
    
    public void setHue(int val) {
      hue = val;
    }
    public void setValue(int val) {
      value = val;
    }
    
    public void setSaturation(int val) {
      saturation = val;
    }
    
    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();
            
            if( name.equals( HUE ) )
            {
                hue = constrain( Integer.parseInt( value ), -180, +180 );
            }
            else if( name.equals( SATURATION ) )
            {
                saturation = constrain( Integer.parseInt( value ), -100, +100 );
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
                    hsv[ 0 ] = ( hsv[ 0 ] + hue ) % 360;
                    hsv[ 0 ] += hsv[ 0 ] < 0 ? 360 : 0;
                    hsv[ 2 ] += value >= 0 ? ( 1 - hsv[ 2 ] ) * value : hsv[ 2 ] * value;

                    int new_col = Color.HSVToColor( hsv );

                    int red = ( new_col >> 16 ) & 0xFF;
                    int green = ( new_col >> 8 ) & 0xFF;
                    int blue = new_col & 0xFF;

                    int gray = ( int )( red * rwgt + green * gwgt + blue * bwgt );

                    red = ( int )( red + ( red - gray ) * saturation );
                    green = ( int )( green + ( green - gray ) * saturation );
                    blue = ( int )( blue + ( blue - gray ) * saturation );
                    // saturation
                    int new_red = red > 255 ? 255 : red < 0 ? 0 : red;
                    int new_green = green > 255 ? 255 : green < 0 ? 0 : green;
                    int new_blue = blue > 255 ? 255 : blue < 0 ? 0 : blue;
                    image.data[ i ][ j ] = ( 0xFF << 24 ) | ( new_red << 16 ) | ( new_green << 8 ) | new_blue;
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "HsvFilter" );
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
      return glitchFilterOpenCV(srcPath, outPath, hue, saturation, value);
    }

    private static native boolean glitchFilterOpenCV(String inFileName, String outFileName, int hue, int saturation, int value);

    
    @Deprecated
    private static native void nativeProcessing( Bitmap bitmap, int h, int s, int v );
    
    @Override
    public String convertToJSON() {
    	String s = "{";
    	s += "\"type\":" + "\"" + filterName + "\",";
    	//start params array
    	s += "\"params\":" + "[";
    	//param hue
    	s += "{";
    	s += "\"name\":" + "\"" + HUE + "\",";
    	s += "\"value\":" + "\"" + hue + "\"";
    	s += "},";
    	//param saturation
    	s += "{";
    	s += "\"name\":" + "\"" + SATURATION + "\",";
    	s += "\"value\":" + "\"" + saturation + "\"";
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
