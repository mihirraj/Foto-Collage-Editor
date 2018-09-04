package com.wisesharksoftware.core.filters;

import java.util.Map.Entry;

import android.content.Context;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Image2;

/**
 * A filter which performs a threshold operation on an image.
 */
public class ThresholdFilter extends Filter {


  private static final long serialVersionUID = 1L;

  private int lowerThreshold;
  private int upperThreshold;
  private int white = 0xffffff;
  private int black = 0x000000;
  
  private static final String LOWER_THRESHOLD = "l_t";
  private static final String UPPER_THRESHOLD = "u_t";
  private static final String WHITE = "white";
  private static final String BLACK = "black";
  
  /**
     * Construct a ThresholdFilter.
     */
    public ThresholdFilter() {
    this(127);
  }

  /**
     * Construct a ThresholdFilter.
     * @param t the threshold value
     */
  public ThresholdFilter(int t) {
	filterName = FilterFactory.THRESHOLD_FILTER;
    setLowerThreshold(t);
    setUpperThreshold(t);
  }

  /**
     * Set the lower threshold value.
     * @param lowerThreshold the threshold value
     * @see #getLowerThreshold
     */
  public void setLowerThreshold(int lowerThreshold) {
    this.lowerThreshold = lowerThreshold;
  }
  
  /**
     * Get the lower threshold value.
     * @return the threshold value
     * @see #setLowerThreshold
     */
  public int getLowerThreshold() {
    return lowerThreshold;
  }
  
  /**
     * Set the upper threshold value.
     * @param upperThreshold the threshold value
     * @see #getUpperThreshold
     */
  public void setUpperThreshold(int upperThreshold) {
    this.upperThreshold = upperThreshold;
  }

  /**
     * Get the upper threshold value.
     * @return the threshold value
     * @see #setUpperThreshold
     */
  public int getUpperThreshold() {
    return upperThreshold;
  }

  /**
     * Set the color to be used for pixels above the upper threshold.
     * @param white the color
     * @see #getWhite
     */
  public void setWhite(int white) {
    this.white = white;
  }

  /**
     * Get the color to be used for pixels above the upper threshold.
     * @return the color
     * @see #setWhite
     */
  public int getWhite() {
    return white;
  }

  /**
     * Set the color to be used for pixels below the lower threshold.
     * @param black the color
     * @see #getBlack
     */
  public void setBlack(int black) {
    this.black = black;
  }

  /**
     * Set the color to be used for pixels below the lower threshold.
     * @return the color
     * @see #setBlack
     */
  public int getBlack() {
    return black;
  }

  @Override
  public void processImage(Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
  {
      try
      {
          for( int i = 0; i < image.height; i++ )
          {
              for( int j = 0; j < image.width; j++ )
              {
                  int color = image.data[ i ][ j ];
                  int v = PixelUtils.brightness( color );
                  float f = ImageMath.smoothStep( lowerThreshold, upperThreshold, v );
                  image.data[ i ][ j ] = (color & 0xff000000) | (ImageMath.mixColors( f, black, white ) & 0xffffff);
              }
          }
      }
      catch( Exception e )
      {
          e.printStackTrace();
          new ExceptionHandler( e, "ThresholdFilter" );
      }
  }

  @Override
  protected void onSetParams()
  {
      for( Entry<String, String> paramsEntry : params.entrySet() )
      {
          String name = paramsEntry.getKey();
          String value = paramsEntry.getValue();
          
          if(name.equals(LOWER_THRESHOLD))
          {
              lowerThreshold = Integer.parseInt(value);
          }
          if( name.equals( UPPER_THRESHOLD ) )
          {
              upperThreshold = Integer.parseInt(value);
          }
          if( name.equals( BLACK ) )
          {
              black = Integer.parseInt(value, 16);
          }
          if( name.equals( WHITE ) )
          {
              white = Integer.parseInt(value, 16);
          }
      }
  }
  @Override
  public boolean hasNativeProcessing() {
    return false;
  }
  
  @Override
  public String convertToJSON() {
  	String s = "{";
  	s += "\"type\":" + "\"" + filterName + "\",";
  	//start params array
  	s += "\"params\":" + "[";
  	//param lower threshold
  	s += "{";
  	s += "\"name\":" + "\"" + LOWER_THRESHOLD + "\",";
  	s += "\"value\":" + "\"" + lowerThreshold + "\"";
  	s += "},";
	//param upper threshold
  	s += "{";
  	s += "\"name\":" + "\"" + UPPER_THRESHOLD + "\",";
  	s += "\"value\":" + "\"" + upperThreshold + "\"";
  	s += "},";
	//param color black
  	s += "{";
  	s += "\"name\":" + "\"" + BLACK + "\",";
  	s += "\"value\":" + "\"" + String.format("#%X", black) + "\"";
  	s += "},";
  	//param color white
  	s += "{";
  	s += "\"name\":" + "\"" + WHITE + "\",";
  	s += "\"value\":" + "\"" + String.format("#%X", white) + "\"";
  	s += "}";
  	s += "]";
  	//end params array
  	s += "}";
  	return s;
  }
}
