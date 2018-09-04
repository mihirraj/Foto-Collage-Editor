package com.wisesharksoftware.core.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Image2;
import com.wisesharksoftware.core.Spline;
import com.wisesharksoftware.core.Utils;

import java.util.Map.Entry;

import org.json.JSONArray;


public class CurveFilter extends Filter
{
    private static final long serialVersionUID = 1L;
    
    private final static String RED_CURVE = "red_curve";
    private final static String GREEN_CURVE = "green_curve";
    private final static String BLUE_CURVE = "blue_curve";

    private int[] redSpline;
    private int[] greenSpline;
    private int[] blueSpline;

    private String sRedSpline;
    private String sGreenSpline;
    private String sBlueSpline;

    
    public CurveFilter() {
    	filterName = FilterFactory.CURVES_FILTER;
    }
    
    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();
            
            if( name.equals( RED_CURVE ) )
            {
            	sRedSpline = value;
                redSpline = getSpline( value );
            }
            if( name.equals( GREEN_CURVE ) )
            {
            	sGreenSpline = value;
                greenSpline = getSpline( value );
            }
            if( name.equals( BLUE_CURVE ) )
            {
            	sBlueSpline = value;
                blueSpline = getSpline( value );
            }
        }
    }

    public void setGreenSpline(String splineStr)
    {
    	sGreenSpline = splineStr;
    	greenSpline = getSpline(splineStr);
    }

    public void setBlueSpline(String splineStr)
    {
    	sBlueSpline = splineStr;
    	blueSpline = getSpline(splineStr);    	
    }

    public void setRedSpline(String splineStr)
    {
    	sRedSpline = splineStr;
    	redSpline = getSpline(splineStr);
    }
    
    public static int[] getSpline( String curve )
    {
        String[] pairs = curve.split( ";" );
        int[][] curveValues = new int[ pairs.length ][ 2 ];
        for( int i = 0; i < pairs.length; ++i )
        {
            String[] vals = pairs[ i ].split( "," );
            curveValues[ i ][ 0 ] = Integer.parseInt( vals[ 0 ] );
            curveValues[ i ][ 1 ] = Integer.parseInt( vals[ 1 ] );
        }
        return Spline.getSpline( curveValues );
    }

    @Override
    public void processImage( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
    {
        try
        {
            for( int i = 0; i < image.height; i++ )
            {
                for( int j = 0; j < image.width; j++ )
                {
                    int color = image.data[ i ][ j ];
                    int red = ( color >> 16 ) & 0xFF;
                    int green = ( color >> 8 ) & 0xFF;
                    int blue = color & 0xFF;
                    image.data[ i ][ j ] = ( 0xFF << 24 ) 
                    		| ( (redSpline != null ? redSpline[ red ] : red) << 16 ) 
                    		| ( (greenSpline != null ? greenSpline[ green ] : green) << 8 ) 
                    		| (blueSpline != null ? blueSpline[ blue ] : blue);
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "CurveFilter" );
        }
    }

    @Override
    public void processBitmap( Bitmap bitmap, Context context, boolean square, boolean isPortraitPhoto )
    {
        nativeProcessing( bitmap, redSpline, greenSpline, blueSpline );
    }

    @Override
    public boolean processOpenCV(Context context, String srcPath, String outPath) {
      return curveFilterOpenCV(srcPath, outPath, redSpline, greenSpline, blueSpline);
    }
    
    @Override
    public boolean hasNativeProcessing()
    {
        return false;
    }

    // bridge to C++
    private static native boolean curveFilterOpenCV(String inFileName, String outFileName, int[] redCurve, int[] greenCurve, int[] blueCurve);
    
    @Deprecated
    private static native void nativeProcessing( Bitmap bitmap, int[] redSpline, int[] greenSpline, int[] blueSpline );
      
    @Override
    public String convertToJSON() {
    	String s = "{";
    	s += "\"type\":" + "\"" + filterName + "\",";
    	//start params array
    	s += "\"params\":" + "[";
    	//param red_curve
    	s += "{";
    	s += "\"name\":" + "\"" + RED_CURVE + "\",";
    	s += "\"value\":" + "\"" + sRedSpline + "\"";
    	s += "},";
    	//param green_curve
    	s += "{";
    	s += "\"name\":" + "\"" + GREEN_CURVE + "\",";
    	s += "\"value\":" + "\"" + sGreenSpline + "\"";
    	s += "},";
    	//param blue_curve
    	s += "{";
    	s += "\"name\":" + "\"" + BLUE_CURVE + "\",";
    	s += "\"value\":" + "\"" + sBlueSpline + "\"";
    	s += "}";
    	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }

}
