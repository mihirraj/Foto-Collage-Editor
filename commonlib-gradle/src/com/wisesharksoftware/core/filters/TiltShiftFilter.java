package com.wisesharksoftware.core.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.smsbackupandroid.lib.ExceptionHandler;

public class TiltShiftFilter extends NearPixelFilter
{
    private static final long serialVersionUID = 1L;

    private static final int MAX_MATRIX_SIZE = 9;
    private static final int BLUR_GRAD_FACTOR = 10;
    private static final int MIN_Y = 200;
    private static final int MAX_Y = 400;

    private static final float[][] no_matrix = new float[][] {{1}};
    private Map<Integer, float[][]> map = new HashMap<Integer, float[][]>();

    @Override
    protected void onSetParams()
    {
    }

    @Override
    protected float[][] getMatrix( int x, int y, int width, int height )
    {
        int factor = getBlurFactorWithSafeCenter( x, y, width, height );
        if( factor <= 1 )
        {
            return no_matrix;
        }
        return getMatrixByFactor( Math.min( factor, MAX_MATRIX_SIZE ) );
    }

    private int getBlurFactorWithSafeCenter( int x, int y, int width, int height )
    {
        if( y > MAX_Y )
        {

            return ( y - MAX_Y ) / BLUR_GRAD_FACTOR;
        }
        if( y < MIN_Y )
        {
            return ( MIN_Y - y ) / BLUR_GRAD_FACTOR;
        }
        return 1;
    }

    private float[][] getMatrixByFactor( int radius )
    {
        if( map.containsKey( radius ) )
        {
            return map.get( radius );
        }
        float[][] matrix = new float[ radius ][ radius ];
        int total_cells = radius * radius;
        float each_value = 1.0f / total_cells;
        for( int i = 0; i < radius; ++i )
        {
            for( int j = 0; j < radius; ++j )
            {
                matrix[ i ][ j ] = each_value;
            }
        }
        map.put( radius, matrix );
        return matrix;
    }


    @Override
    public void processBitmap( Bitmap srcBitmap, Context context, boolean square, boolean isPortraitPhoto )
    {
        long startTime = System.currentTimeMillis();
        try
        {
            Log.d( "TiltShiftFilter", "starting native processing!" );
            nativeProcessing( srcBitmap );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "TiltShiftFilter" );
        }
        Log.d( "TiltShiftFilter", srcBitmap.getWidth() + ":" + srcBitmap.getHeight() );
        Log.d( "TiltShiftFilter", "finished native processing" );
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Log.d( "TiltShiftFilter", "Time " + elapsedTime );
    }

    @Override
    public boolean hasNativeProcessing()
    {
        return false;
    }

    private static native void nativeProcessing( Bitmap srcBitmap );
}
