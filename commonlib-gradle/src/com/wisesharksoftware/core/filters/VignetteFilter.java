package com.wisesharksoftware.core.filters;

import android.content.Context;

import java.util.Map.Entry;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Image2;

public class VignetteFilter extends Filter
{
    private static final long serialVersionUID = 1L;

    private final static String INNER_RADIUS = "inner_radius";
    private final static String OUTER_RADIUS = "outer_radius";

    private double innerRadius;
    private double outerRadius;

    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();
            
            if( name.equals( INNER_RADIUS ) )
            {
                innerRadius = Double.parseDouble( value );
            }
            if( name.equals( OUTER_RADIUS ) )
            {
                outerRadius = Double.parseDouble( value );
            }
        }
    }
    
    @Override
    public void processImage( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
    {
        try
        {
            int xCenter = image.width / 2;
            int yCenter = image.height / 2;
            double radius = Math.sqrt( xCenter * xCenter + yCenter * yCenter );

            for( int x = 0; x < image.width; ++x )
            {
                for( int y = 0; y < image.height; ++y )
                {
                    int color = image.data[ y ][ x ];
                    int red = ( color >> 16 ) & 0xFF;
                    int green = ( color >> 8 ) & 0xFF;
                    int blue = color & 0xFF;
                    double k = getKoeff( x, y, xCenter, yCenter, radius, innerRadius, outerRadius );
                    int newRed = ( int )( red * k );
                    int newGreen = ( int )( green * k );
                    int newBlue = ( int )( blue * k );
                    int newColor = ( 0xFF << 24 ) | ( newRed << 16 ) | ( newGreen << 8 ) | newBlue;
                    image.data[ y ][ x ] = newColor;
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "VignetteFilter" );
        }
    }

    private static double getKoeff( int x, int y, int xCenter, int yCenter, double radius, double innerRadius, double outerRadius )
    {
        double dist = Math.sqrt( Math.pow( xCenter - x, 2 ) + Math.pow( yCenter - y, 2 ) ) / radius;
        if( dist < innerRadius )
        {
            return 1;
        }
        if( dist > outerRadius )
        {
            return 0;
        }
        return ( outerRadius - dist ) / ( outerRadius - innerRadius );
    }

    @Override
    public boolean hasNativeProcessing()
    {
        return false;
    }
}
