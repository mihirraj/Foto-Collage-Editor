package com.wisesharksoftware.core.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.GlobalSettings;
import com.wisesharksoftware.core.Image2;

import java.util.Map.Entry;

public class TextOverlayFilter extends Filter
{
    private static final long serialVersionUID = 1L;

    private static final String TEXT = "text";
    private static final String TEXT_SIZE = "text_size";
    private static final String Y = "y";
    private static final String TEXT_COLOR = "text_color";
    private static final String FONT = "font";

    private String text;
    private int textColor;
    private int textSize;
    private String fontPath;

    @Override
    public void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();

            if( name.equals( TEXT ) )
            {
                text = value;
            }
            else if( name.equals( TEXT_COLOR ) )
            {
                textColor = ( int )Long.parseLong( value, 16 );
            }
            else if( name.equals( TEXT_SIZE ) )
            {
                textSize = Integer.parseInt( value );
            }
            else if( name.equals( FONT ) )
            {
                fontPath = value;
            }
        }
    }
   
    public void processImage( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
    {
        try
        {
            System.gc();
            Bitmap bitmap = Bitmap.createBitmap(
                    isPortraitPhoto ? GlobalSettings.getImageHeight( square, false ) : GlobalSettings.getImageWidth( square, false ),
                    isPortraitPhoto ? GlobalSettings.getImageWidth( square, false ) : GlobalSettings.getImageHeight( square, false ),
                    Config.ARGB_8888 );
            Canvas canvas = new Canvas( bitmap );
            Paint paint = new Paint();
            paint.setColor( textColor );
            paint.setTextSize( textSize );
            Typeface font = null;
            if( fontPath.contains( ".ttf" ) || fontPath.contains( ".otf" ) )
            {
                font = Typeface.createFromAsset( context.getAssets(), fontPath );
            }
            else
            {
                font = Typeface.create( fontPath, Typeface.NORMAL );
            }
            paint.setTypeface( font );
            paint.setAntiAlias( true );
            paint.setTextAlign( Paint.Align.CENTER );
            canvas.drawText( text, isPortraitPhoto ? GlobalSettings.getImageHeight( square, false ) / 2 : GlobalSettings.getImageWidth( square, false ) / 2, isPortraitPhoto ? GlobalSettings.getImageWidth( square, false ) - 60 : GlobalSettings.getImageHeight( square, false ) - 60, paint );
            // 2. remove canvas
            // 3. convert bitmap to our Image
            Image2 blendImage = Image2.fromBitmap( bitmap );
            // 4. Use usual overlay-transparency filter to apply this overlay.
            // TODO (roman) : that is copy-paste of existing filter... it sucks
            int loc = 0;
            for( int i = 0; i < image.height; i++ )
            {
                for( int j = 0; j < image.width; j++ )
                {
                    int blendColor = blendImage.data[ i ][ j ];
                    int blendA = blendColor >>> 24;

                    if( blendA == 0 )
                    {
                        loc++;
                        continue;
                    }
                    if( blendA == 255 )
                    {
                        image.data[ i ][ j ] = blendColor;
                        continue;
                    }

                    int srcRed = ( image.data[ i ][ j ] >> 16 ) & 0xFF;
                    int srcGreen = ( image.data[ i ][ j ] >> 8 ) & 0xFF;
                    int srcBlue = image.data[ i ][ j ] & 0xFF;

                    int blendRed = ( blendColor >> 16 ) & 0xFF;
                    int blendGreen = ( blendColor >> 8 ) & 0xFF;
                    int blendBlue = blendColor & 0xFF;
                    double k = blendA / 255.0;
                    int resRed = ( int )( srcRed - ( srcRed - blendRed ) * k );
                    int resGreen = ( int )( srcGreen - ( srcGreen - blendGreen ) * k );
                    int resBlue = ( int )( srcBlue - ( srcBlue - blendBlue ) * k );
                    image.data[ i ][ j ] = ( 0xFF << 24 ) | ( resRed << 16 ) | ( resGreen << 8 ) | resBlue;
                }
            }
            if( bitmap != null )
            {
                bitmap.recycle();
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "TextOverlayFilter" );
        }
    }

    @Override
    public boolean hasNativeProcessing()
    {
        return false;
    }
}
