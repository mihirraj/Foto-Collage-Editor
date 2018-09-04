package com.wisesharksoftware.core.filters;

import android.content.Context;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Image2;

public abstract class NearPixelFilter extends Filter
{
    private static final long serialVersionUID = 1L;

    private static int convolution( int x, int y, float[][] matrix, int matrixsize, Image2 img )
    {
        float rtotal = 0.0f;
        float gtotal = 0.0f;
        float btotal = 0.0f;
        int offset = matrixsize / 2;
        // Loop through convolution matrix
        for( int i = 0; i < matrixsize; ++i )
        {
            for( int j = 0; j < matrixsize; ++j )
            {
                // What pixel are we testing
                int xloc = x + i - offset;
                if( xloc < 0 )
                {
                    xloc = 0;
                }
                else if( xloc >= img.width )
                {
                    xloc = img.width - 1;
                }
                int yloc = y + j - offset;
                if( yloc < 0 )
                {
                    yloc = 0;
                }
                else if( yloc >= img.height )
                {
                    yloc = img.height - 1;
                }
//                int loc = xloc + img.width * yloc;
//                // Make sure we have not walked off the edge of the pixel array
//                //loc = loc;
//                loc = loc >= length ? length - 1 : loc;
//                loc = loc < 0 ? 0 : loc;

                // Calculate the convolution
                // We sum all the neighboring pixels multiplied by the values in the convolution matrix.
                float k = matrix[ i ][ j ];
                int pic = img.data[ yloc ][ xloc ];
                rtotal += ( ( pic >> 16 ) & 0xFF ) * k;
                gtotal += ( ( pic >> 8 ) & 0xFF ) * k;
                btotal += ( pic & 0xFF ) * k;
            }
        }
        // Make sure RGB is within range
        rtotal = constrain( ( int )rtotal, 0, 255 );
        gtotal = constrain( ( int )gtotal, 0, 255 );
        btotal = constrain( ( int )btotal, 0, 255 );
        // Return the resulting color
        return ( 0xFF << 24 ) | ( ( int )rtotal << 16 ) | ( ( int )gtotal << 8 ) | ( int )btotal;
    }

    @Override
    public void processImage( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
    {
        try
        {
            int pixels[][] = new int[ image.height ][ image.width ];
            for( int x = 0; x < image.width; ++x )
            {
                for( int y = 0; y < image.height; ++y )
                {
                    // Each pixel location (x,y) gets passed into a function called convolution() 
                    // which returns a new color value to be displayed.
                    float[][] matrix = getMatrix( x, y, image.width, image.height );
                    int c = convolution( x, y, matrix, matrix.length, image );
                    pixels[ y ][ x ] = c;
                }
            }
            image.data = pixels;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "NearPixelFilter" );
        }
    }

    protected abstract float[][] getMatrix( int x, int y, int imageWidth, int imageHeight );

    @Override
    protected void onSetParams()
    {
    }
}
