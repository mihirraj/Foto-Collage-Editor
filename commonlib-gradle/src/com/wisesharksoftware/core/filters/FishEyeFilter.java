package com.wisesharksoftware.core.filters;

import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Image2;

public class FishEyeFilter extends Filter
{
	public static final int TYPE_CIRCLE = 1;
    public static final int TYPE_BARREL = 2;
    public static final int TYPE_BARREL_CONVEX = 3;
    public static final int TYPE_BARREL_NEW = 4;
    
    private static final long serialVersionUID = 1L;
    private int scale = 1;
    private int type = TYPE_CIRCLE;
    private int curvature = 50; //0..100
   
	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCurvature() {
		return curvature;
	}

	public void setCurvature(int curvature) {
		this.curvature = curvature;
	}
    
    public FishEyeFilter() {
    	filterName = FilterFactory.FISH_EYE_FILTER;
    }
  
    @Override
    protected void onSetParams()
    {
    	 for( Entry<String, String> paramsEntry : params.entrySet() )
         {
             String name = paramsEntry.getKey();
             String value = paramsEntry.getValue();
             
             if( name.equals( "type" ) )
             {
            	 if (value.equals("circle")) {
            		 type = TYPE_CIRCLE;
            	 }
            	 if (value.equals("barrel")) {
            		 type = TYPE_BARREL;
            	 }
            	 if (value.equals("barrel_convex")) {
            		 type = TYPE_BARREL_CONVEX;
            	 }
            	 if (value.equals("barrel_new")) {
            		 type = TYPE_BARREL_NEW;
            	 }
             }
             if( name.equals( "scale" ) )
             {
            	 scale = Integer.parseInt(value);
             }
             if( name.equals( "curvature" ) )
             {
            	 curvature = Integer.parseInt(value);
             }             
         }
    }

    @Override
    public void processBitmap( Bitmap srcBitmap, Context context, boolean square, boolean isPortraitPhoto )
    {
        long startTime = System.currentTimeMillis();
        try
        {
            Log.d( "FishEyeFilter", "starting native processing!" );
            nativeProcessing( srcBitmap );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "FishEyeFilter" );
        }
        Log.d( "FishEyeFilter", srcBitmap.getWidth() + ":" + srcBitmap.getHeight() );
        Log.d( "FishEyeFilter", "finished native processing" );
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Log.d( "FishEyeFilter", "Time " + elapsedTime );
    }

    @Override
    public void processImage( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
    {
        //sphereFilter( image, true );
        circularFisheye(image, true);
    }

    @Override
    public boolean hasNativeProcessing()
    {
        return true;
    }
    
    private static native void nativeProcessing( Bitmap srcBitmap );

    public boolean sphereFilter( Image2 input, boolean bSmoothing )
    {
        int nWidth = input.width;
        int nHeight = input.height;

        int[][] colors = new int[ nWidth ][ nHeight ];
        Point mid = new Point();
        mid.x = nWidth / 2;
        mid.y = nHeight / 2;

        double radius;
        int newX, newY;
        double max = ( double )Math.max( mid.x, mid.y );
        for( int x = 0; x < nWidth; ++x )
        {
            for( int y = 0; y < nHeight; ++y )
            {
                int px = 0;
                int py = 0;
                int trueX = x - mid.x;
                int trueY = y - mid.y;
                radius = Math.sqrt( ( double )( trueX * trueX + trueY * trueY ) );

                newX = mid.x + ( int )( radius * trueX / max );

                if( newX > 0 && newX < nWidth )
                {
                    px = newX - 1;
                }
                else
                {
                    px = py = 0;
                }

                newY = mid.y + ( int )( radius * trueY / max );

                if( newY > 0 && newY < nHeight && newX > 0 && newX < nWidth )
                {
                    py = newY - 1;
                }
                else
                {
                    px = py = 0;
                }

                colors[ x ][ y ] = input.data[ py ][ px ];
            }
        }
        offsetFilterAbs( input, colors );
        return true;
    }

    private boolean offsetFilterAbs( Image2 b, int[][] colors )
    {
        int nWidth = b.width;
        int nHeight = b.height;

        int midX = b.width / 2;
        int midY = b.height / 2;

        for( int y = 0; y < nHeight; ++y )
        {
            for( int x = 0; x < nWidth; ++x )
            {
                int trueX = x - midX;
                int trueY = y - midY;

                double radius = Math.sqrt( ( double )( trueX * trueX + trueY * trueY ) );
                if( radius <= 50 )
                {
                    blurAround( x, y, colors, nWidth, nHeight, 5 - ( int )( radius / 10f ) );
                }

                b.data[ y ][ x ] = colors[ x ][ y ];
            }
        }
        return true;
    }

    private void blurAround( int x, int y, int[][] colors, int width, int height, int k )
    {
        if( x - k >= 0 && x + k <= width && y + k <= height && y - k >= 0 )
        {
            long sumR = 0;
            long sumB = 0;
            long sumG = 0;
            for( int i = x - k; i <= x + k; i++ )
            {
                for( int j = y - k; j <= y + k; j++ )
                {
                    sumR += ( colors[ i ][ j ] >> 16 ) & 0xFF;
                    sumG += ( colors[ i ][ j ] >> 8 ) & 0xFF;
                    sumB += colors[ i ][ j ] & 0xFF;
                }
            }
            int newK = k * 2 + 1;
            long kk = newK * newK;
            sumR = sumR / kk;
            sumB = sumB / kk;
            sumG = sumG / kk;

            colors[ x ][ y ] = ( int )( ( 0xFF << 24 ) | ( sumR << 16 ) | ( sumG << 8 ) | sumB );
        }
    }
    
    private final boolean circularFisheye(Image2 image, boolean square) {
	  	double w = image.width;
	  	double h = image.height;
	  	int[][] dst = new int[image.height] [image.width];
	    // for each row
	    for (int y=0;y<h;y++) {                                
	        // normalize y coordinate to -1 ... 1
	        double ny = ((2*y)/h)-1;                        
	        // pre calculate ny*ny
	        double ny2 = ny*ny;                                
	        // for each column
	        for (int x=0;x<w;x++) {                            
	            // normalize x coordinate to -1 ... 1
	            double nx = ((2*x)/w)-1;                    
	            // pre calculate nx*nx
	            double nx2 = nx*nx;
	            // calculate distance from center (0,0)
	            // this will include circle or ellipse shape portion
	            // of the image, depending on image dimensions
	            // you can experiment with images with different dimensions
	            double r = Math.sqrt(nx2+ny2);                
	            // discard pixels outside from circle!
	            if (0.0<=r&&r<=1.0) {                            
	                double nr = Math.sqrt(1.0-r*r);            
	                // new distance is between 0 ... 1
	                nr = (r + (1.0-nr)) / 2.0;
	                // discard radius greater than 1.0
	                if (nr<=1.0) {
	                    // calculate the angle for polar coordinates
	                    double theta = Math.atan2(ny,nx);         
	                    // calculate new x position with new distance in same angle
	                    double nxn = nr*Math.cos(theta);        
	                    // calculate new y position with new distance in same angle
	                    double nyn = nr*Math.sin(theta);        
	                    // map from -1 ... 1 to image coordinates
	                    int x2 = (int)(((nxn+1)*w)/2.0);        
	                    // map from -1 ... 1 to image coordinates
	                    int y2 = (int)(((nyn+1)*h)/2.0);        
	                    // find (x2,y2) position from source pixels
	                    int srcpos = (int)(y2*w+x2);            
	                    // make sure that position stays within arrays
	                    if (srcpos>=0 & srcpos < w*h) {
	                        // get new pixel (x2,y2) and put it to target array at (x,y)
	                        //dst[(int)(y*w+x)] = image.data[srcpos];    
	                        dst[y][x] = image.data[y2][x2];    
	                    }
	                }
	            }
	        }
	    }
	    //return result pixels
	    image.data = dst;
	    return true;
	}

	@Override
	public String convertToJSON() {
		String s = "{";
		s += "\"type\":" + "\"" + filterName + "\",";
		// start params array
		s += "\"params\":" + "[";
		// param kernel size
		s += "{";
		s += "\"name\":" + "\"" + "type" + "\",";
		switch (type) {
		case TYPE_CIRCLE:
			s += "\"value\":" + "\"" + "circle" + "\"";
			break;
		case TYPE_BARREL:
			s += "\"value\":" + "\"" + "barrel" + "\"";
			break;
		case TYPE_BARREL_CONVEX:
			s += "\"value\":" + "\"" + "barrel_convex" + "\"";
			break;
		case TYPE_BARREL_NEW:
			s += "\"value\":" + "\"" + "barrel_new" + "\"";
			break;
		}
		s += "},";
		// param scale
		s += "{";
		s += "\"name\":" + "\"" + "scale" + "\",";
		s += "\"value\":" + "\"" + scale + "\"";
		s += "},";
		// param scale
		s += "{";
		s += "\"name\":" + "\"" + "curvature" + "\",";
		s += "\"value\":" + "\"" + curvature + "\"";
		s += "}";		
		s += "]";
		// end params array
		s += "}";
		return s;
	}
}
