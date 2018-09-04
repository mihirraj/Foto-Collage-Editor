package com.wisesharksoftware.core;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class Image2
{
    public int width;
    public int height;
    public int[][] data;

    public static Image2 fromBitmap( Bitmap bitmap )
    {
        System.gc();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Image2 image = new Image2();
        image.width = width;
        image.height = height;
        image.data = new int[ height ][ width ];
        for( int i = 0; i < image.height; i++ )
        {
            bitmap.getPixels( image.data[ i ], 0, width, 0, i, width, 1 );
        }
        return image;
    }

    public Bitmap toBitmap()
    {
        System.gc();
        Bitmap bitmap = Bitmap.createBitmap( width, height, Config.ARGB_8888 );
        for( int i = 0; i < height; ++i )
        {
            bitmap.setPixels( data[ i ], 0, width, 0, i, width, 1 );
        }
        return bitmap;
    }
}
