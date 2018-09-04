package com.wisesharksoftware.core.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.camera.AppSettings;
import com.wisesharksoftware.core.AssetsUtils;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Image2;
import com.wisesharksoftware.core.Utils;

public class BlendFilter extends Filter
{
    private enum Position
    {
        mozaic,
        stretch,
        absolute
    }

    public enum Algorithm
    {
        screen,
        multiply,
        transparency,
        transparency_alpha,
        colorDodge,
        overlay,
        hue
    }

    private enum XPosition
    {
        left,
        right
    }

    private enum YPosition
    {
        top,
        bottom
    }
    
    private static final long serialVersionUID = 1L;
    
    private Position position = Position.absolute;
    private XPosition xPosition = XPosition.left;
    private YPosition yPosition = YPosition.top;
    private Algorithm algorithm = Algorithm.multiply;
    private int x = 0;
    private int y = 0;
    private String blendSrc;
    private String blendSrcPortrait;
    private Bitmap bitmap;
    private Image2 blendImage;
    private boolean blend_with_image_memory = false;
    private int alpha = 0;
    
    private static final String PARAM_ALGORITHM = "algorithm";
    private static final String PARAM_X = "x";
    private static final String PARAM_Y = "y";
    private static final String PARAM_IMAGE = "image";
    private static final String PARAM_IMAGE_PORTRAIT = "image_portrait";
    private static final String PARAM_POSITION = "position";
    private static final String PARAM_POSITION_LEFT = "left";
    private static final String PARAM_POSITION_RIGHT = "right";
    private static final String PARAM_BLEND_WITH_IMAGE_MEMORY = "blend_with_image_memory";
    private static final String PARAM_ALPHA = "alpha";

    public BlendFilter() {
    	filterName = FilterFactory.BLEND_FILTER;
    }
    
    public void setBlendSrc(String value) {
      blendSrc = value;
    }
    
    public void setAlgorithm(Algorithm value) {
      algorithm = value;
    }
    
	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public boolean isBlend_with_image_memory() {
		return blend_with_image_memory;
	}

	public void setBlend_with_image_memory(boolean blend_with_image_memory) {
		this.blend_with_image_memory = blend_with_image_memory;
	}
	
    protected void onSetParams()
    {
        for( Entry<String, String> paramEntry : params.entrySet() )
        {
            String name = paramEntry.getKey();
            String value = paramEntry.getValue();
            
            if( name.equals( PARAM_X ) )
            {
            	try {
            		x = Integer.parseInt( value );
            	} catch (NumberFormatException e) {
            		xPosition = XPosition.valueOf(value);
            	}
            }
            else if( name.equals( PARAM_Y ) )
            {
            	try {
            		y = Integer.parseInt( value );
            	} catch (NumberFormatException e) {
            		yPosition = YPosition.valueOf(value);
            	}
            	
            }
            else if( name.equals( PARAM_IMAGE ) )
            {
                blendSrc = value;
            }
            else if( name.equals( PARAM_IMAGE_PORTRAIT ) )
            {
                blendSrcPortrait = value;
            }
            else if( name.equals( PARAM_POSITION ) )
            {
                position = Position.valueOf( value );
            }
            else if( name.equals( PARAM_ALGORITHM ) )
            {
                algorithm = Algorithm.valueOf( value );
            }
            else if( name.equals( PARAM_BLEND_WITH_IMAGE_MEMORY ) )
            {
            	if (value.equals("true")) {
            		blend_with_image_memory = true;
            	}
            }
            else if( name.equals( PARAM_ALPHA ) )
            {
            	setAlpha(Integer.parseInt(value));            	
            }            
        }
    }

    // bridge to C++
    private static native boolean blendFilterOpenCV(String inFileName, String outFileName, byte[] blendJpeg, int algorithm, int blendWidth, int blendHeight);

    @Deprecated
    private static native void nativeProcessing( Bitmap srcBitmap, Bitmap blendBitmap, int x, int y, int position, int algoritm);
    
    /**
     * Temporary solution, just for testing.
     * @param context
     * @param srcPath
     * @param blendAsset
     * @param algorithm
     */
    public boolean processOpenCV(Context context, String srcPath, String outPath, String blendAsset, Algorithm algorithm) {
      byte[] blendJpeg = null;
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(srcPath, options);

      int height = options.outHeight;
      int width = options.outWidth;

      boolean isHd = AppSettings.isHdImage(context, width, height);

      try {        
        String prefix = isHd ? "hd" : "sd";
        blendJpeg = AssetsUtils.readFile(context, prefix + "/square/" + blendSrc);
      } catch (IOException e) {
        e.printStackTrace();
      }

      return blendFilterOpenCV(srcPath, outPath, blendJpeg,
                               algorithm.ordinal(),
                               AppSettings.getWidth(context, isHd),
                               AppSettings.getHeight(context, isHd));
    }
    
    @Override
    public boolean processOpenCV(Context context, String srcPath, String outPath) {
      return processOpenCV(context, srcPath, outPath, this.blendSrc, this.algorithm);
    }
    
    @Override
    public void processBitmap( Bitmap srcBitmap, Context context, boolean square, boolean isPortraitPhoto )
    {
        Bitmap blendBitmap = null;
        try
        {
            String actualBlendSrc = !square && blendSrcPortrait != null ? blendSrcPortrait : blendSrc;
            if( actualBlendSrc.contains( "/sdcard" ) )
            { // for tests only
                blendBitmap = Utils.loadBitmap( new File( actualBlendSrc ), false, square, false );
            }
            else
            {
                if( square )
                {
                    try
                    {
                        blendBitmap = Utils.getBitmapAsset( context, "square/" + actualBlendSrc, true );
                    }
                    catch( Exception e )
                    {
                    	e.printStackTrace();
                    	new ExceptionHandler(e, "BlendFilter: getBitmapAsset");
                    }
                }
                if( blendBitmap == null )
                {
                    Log.d( "BlendFilter", "Blend image: " + actualBlendSrc );
                    blendBitmap =  Utils.getBitmapAsset( context, actualBlendSrc, true );
                    if( isPortraitPhoto )
                    {
                        Bitmap tmpBitmap = blendBitmap;
                        blendBitmap = Utils.rotateBitmap( blendBitmap, 90 );
                        tmpBitmap.recycle();
                    }
                }
            }

            nativeProcessing( srcBitmap, blendBitmap, x, y, position.ordinal(), algorithm.ordinal() );
        }
        catch( IOException e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "BlendFilter" );
        }
        finally
        {
            if( blendBitmap != null )
            {
                blendBitmap.recycle();
                blendBitmap = null;
            }
        }
    }

    @Override
    public void processImage( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
    {
        Bitmap bitmap = null;
        Bitmap scaledBitmap = null;
        try
        {
            String actualBlendSrc = !square && isPortraitPhoto && blendSrcPortrait != null ? blendSrcPortrait : blendSrc;
            if( actualBlendSrc.contains( "/sdcard" ) )
            { // for tests only
                bitmap = Utils.loadBitmap( new File( actualBlendSrc ), false, square, false );
            }
            else
            {
                if( square )
                {
                    try
                    {
                        bitmap = Utils.getBitmapAsset( context, "square/" + actualBlendSrc, hd );
                    }
                    catch( Exception e )
                    { 
                    	e.printStackTrace();
                    	new ExceptionHandler(e, "BlendFilter: getBitmapAsset");
                    }
                }
                if( bitmap == null )
                {
                    bitmap = Utils.getBitmapAsset( context, actualBlendSrc, hd );
                    if( isPortraitPhoto && blendSrcPortrait == null )
                    {
                        bitmap = Utils.rotateBitmap( bitmap, 90 );
                    }
                }
            }
            	
            Image2 blendImage = null;
            if (image.width < bitmap.getWidth() && image.height < bitmap.getHeight()) {
            	scaledBitmap = Bitmap.createScaledBitmap(bitmap, image.width, image.height, true);
            	bitmap.recycle();
            	bitmap = null;
            	blendImage = Image2.fromBitmap( scaledBitmap );
            } else {
                blendImage = Image2.fromBitmap( bitmap );
            }
            int minX = 0;
            int minY = 0;
            int maxX = image.width;
            int maxY = image.height;
            if( position == Position.absolute )
            {
            	if (xPosition == XPosition.left) {
            		x = 0;
            	} else if (xPosition == XPosition.right) {
            		x = image.width - blendImage.width;
            	}
            	if (yPosition == YPosition.top) {
            		y = 0;
            	} else if (yPosition == YPosition.bottom) {
            		y = image.height - blendImage.height;
            	}
                minX = constrain( x, 0, image.width );
                minY = constrain( y, 0, image.height );
                maxX = constrain( x + blendImage.width, 0, image.width );
                maxY = constrain( y + blendImage.height, 0, image.height );
            }
            switch( algorithm )
            {
                case screen:
                    screenAlgorithm( minX, minY, maxX, maxY, image.width, blendImage.width, blendImage.height, image.data, blendImage.data );
                    break;
                case transparency:
                    transparencyAlgorithm( minX, minY, maxX, maxY, image.width, blendImage.width, blendImage.height, image.data, blendImage.data );
                    break;
                case colorDodge:
                    colorDodgeAlgorithm( minX, minY, maxX, maxY, image.width, blendImage.width, blendImage.height, image.data, blendImage.data );
                    break;
                default:
                    multiplyAlgorithm( minX, minY, maxX, maxY, image.width, blendImage.width, blendImage.height, image.data, blendImage.data );
                    break;
            }
            blendImage = null;
        }
        catch( IOException e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "BlendFilter" );
        }
        finally
        {
            if( bitmap != null && !bitmap.isRecycled() )
            {
                bitmap.recycle();
                bitmap = null;
            }
            if( scaledBitmap != null && !scaledBitmap.isRecycled() )
            {
                scaledBitmap.recycle();
                scaledBitmap = null;
            }
        }
    }

    private void screenAlgorithm( int minX, int minY, int maxX, int maxY,
                                  int imageWidth, int blendImageWidth, int blendImageHeight, int[][] imageData, int[][] blendImageData )
    {
        for( int x = minX; x < maxX; ++x )
        {
            for( int y = minY; y < maxY; ++y )
            {
                int srcColor = imageData[ y ][ x ];
                int blendColor = blendImageData[ position == Position.mozaic ? y % blendImageHeight : position == Position.absolute ? y - this.y : 0 ][ position == Position.mozaic ? x % blendImageWidth : position == Position.absolute ? x - this.x : 0 ];

                int srcRed = ( srcColor >> 16 ) & 0xFF;
                int srcGreen = ( srcColor >> 8 ) & 0xFF;
                int srcBlue = srcColor & 0xFF;
                int blendRed = ( blendColor >> 16 ) & 0xFF;
                int blendGreen = ( blendColor >> 8 ) & 0xFF;
                int blendBlue = blendColor & 0xFF;

                int resRed = srcRed + blendRed - ( srcRed * blendRed ) / 255;
                int resGreen = srcGreen + blendGreen - ( srcGreen * blendGreen ) / 255;
                int resBlue = srcBlue + blendBlue - ( srcBlue * blendBlue ) / 255;
                int res = ( 0xFF << 24 ) | ( resRed << 16 ) | ( resGreen << 8 ) | resBlue; //Color.rgb
                imageData[ y ][ x ] = res;
            }
        }
    }

    private void transparencyAlgorithm( int minX, int minY, int maxX, int maxY,
                                        int imageWidth, int blendImageWidth, int blendImageHeight, int[][] imageData, int[][] blendImageData )
    {
        for( int x = minX; x < maxX; ++x )
        {
            for( int y = minY; y < maxY; ++y )
            {
                int srcColor = imageData[ y ][ x ];
                int blendColor = blendImageData[ position == Position.mozaic ? y % blendImageHeight : position == Position.absolute ? y - this.y : 0 ][ position == Position.mozaic ? x % blendImageWidth : position == Position.absolute ? x - this.x : 0 ];
                int blendA = blendColor >>> 24; //Color.alpha(blendColor);

                if( blendA == 0 )
                {
                    continue;
                }
                if( blendA == 255 )
                {
                    imageData[ y ][ x ] = blendColor;
                    continue;
                }

                int srcRed = ( srcColor >> 16 ) & 0xFF;
                int srcGreen = ( srcColor >> 8 ) & 0xFF;
                int srcBlue = srcColor & 0xFF;
                int blendRed = ( blendColor >> 16 ) & 0xFF;
                int blendGreen = ( blendColor >> 8 ) & 0xFF;
                int blendBlue = blendColor & 0xFF;

                // top layer algorithm
                double k = blendA / 255.0;
                int resRed = ( int )( srcRed - ( srcRed - blendRed ) * k );
                int resGreen = ( int )( srcGreen - ( srcGreen - blendGreen ) * k );
                int resBlue = ( int )( srcBlue - ( srcBlue - blendBlue ) * k );
                int res = ( 0xFF << 24 ) | ( resRed << 16 ) | ( resGreen << 8 ) | resBlue; //Color.rgb
                imageData[ y ][ x ] = res;
            }
        }
    }

    private void multiplyAlgorithm( int minX, int minY, int maxX, int maxY,
                                    int imageWidth, int blendImageWidth, int blendImageHeight, int[][] imageData, int[][] blendImageData )
    {
        for( int x = minX; x < maxX; ++x )
        {
            for( int y = minY; y < maxY; ++y )
            {
                int srcColor = imageData[ y ][ x ];
                int blendColor = blendImageData[ position == Position.mozaic ? y % blendImageHeight : position == Position.absolute ? y - this.y : 0 ][ position == Position.mozaic ? x % blendImageWidth : position == Position.absolute ? x - this.x : 0 ];

                int srcRed = ( srcColor >> 16 ) & 0xFF;
                int srcGreen = ( srcColor >> 8 ) & 0xFF;
                int srcBlue = srcColor & 0xFF;
                int blendRed = ( blendColor >> 16 ) & 0xFF;
                int blendGreen = ( blendColor >> 8 ) & 0xFF;
                int blendBlue = blendColor & 0xFF;

                int resRed = ( srcRed * blendRed ) / 255;
                int resGreen = ( srcGreen * blendGreen ) / 255;
                int resBlue = ( srcBlue * blendBlue ) / 255;
                int res = ( 0xFF << 24 ) | ( resRed << 16 ) | ( resGreen << 8 ) | resBlue; //Color.rgb
                imageData[ y ][ x ] = res;
            }
        }
    }

  private void colorDodgeAlgorithm(int minX, int minY, int maxX, int maxY,
      int imageWidth, int blendImageWidth, int blendImageHeight,
      int[][] imageData, int[][] blendImageData) {
    for (int x = minX; x < maxX; ++x) {
      for (int y = minY; y < maxY; ++y) {
        int srcColor = imageData[y][x];
        int blendColor = blendImageData[position == Position.mozaic ? y
            % blendImageHeight : position == Position.absolute ? y - this.y : 0][position == Position.mozaic ? x
            % blendImageWidth
            : position == Position.absolute ? x - this.x : 0];

        int srcRed = (srcColor >> 16) & 0xFF;
        int srcGreen = (srcColor >> 8) & 0xFF;
        int srcBlue = srcColor & 0xFF;
        int blendRed = (blendColor >> 16) & 0xFF;
        int blendGreen = (blendColor >> 8) & 0xFF;
        int blendBlue = blendColor & 0xFF;

        int resRed = (blendRed == 255) ? blendRed:Math.min(255, ((srcRed << 8 ) / (255 - blendRed)));
        int resGreen = (blendGreen == 255) ? blendGreen:Math.min(255, ((srcGreen << 8 ) / (255 - blendGreen)));
        int resBlue = (blendBlue == 255) ? blendBlue:Math.min(255, ((srcBlue << 8 ) / (255 - blendBlue)));
        int res = (0xFF << 24) | (resRed << 16) | (resGreen << 8) | resBlue; // Color.rgb
        imageData[y][x] = res;
      }
    }
  }
    @Override
    public boolean hasNativeProcessing()
    {
        return false;
    }

    @Override
    public void clean()
    {
        super.clean();
        bitmap.recycle();
        bitmap = null;
        blendImage = null;
    }
    
    @Override
    public String convertToJSON() {
    	String s = "{";
    	s += "\"type\":" + "\"" + filterName + "\",";
    	//start params array
    	s += "\"params\":" + "[";
    	//param algorithm
    	s += "{";
    	s += "\"name\":" + "\"" + PARAM_ALGORITHM + "\",";
    	s += "\"value\":" + "\"" + algorithm.toString() + "\"";
    	s += "},";
    	//param image
    	s += "{";
    	s += "\"name\":" + "\"" + PARAM_IMAGE + "\",";
    	s += "\"value\":" + "\"" + blendSrc + "\"";
    	s += "},";
    	//PARAM_X
    	if (xPosition != null) {
    	s += "{";
    	s += "\"name\":" + "\"" + PARAM_X + "\",";
    	s += "\"value\":" + "\"" + xPosition.toString() + "\"";
    	s += "},";
    	}
    	if (yPosition != null) {
    	//PARAM_Y
    	s += "{";
    	s += "\"name\":" + "\"" + PARAM_Y + "\",";
    	s += "\"value\":" + "\"" + yPosition.toString() + "\"";
    	s += "},";
    	}
    	//param position
    	s += "{";
    	s += "\"name\":" + "\"" + PARAM_POSITION + "\",";
    	s += "\"value\":" + "\"" + position.toString() + "\"";
    	s += "},";
    	//param blend_with_image_position
    	s += "{";
    	s += "\"name\":" + "\"" + PARAM_BLEND_WITH_IMAGE_MEMORY + "\",";
    	s += "\"value\":" + "\"" + blend_with_image_memory + "\"";
    	s += "},";
    	//param alpha
    	s += "{";
    	s += "\"name\":" + "\"" + PARAM_ALPHA + "\",";
    	s += "\"value\":" + "\"" + getAlpha() + "\"";
    	s += "}";
    	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }

}
