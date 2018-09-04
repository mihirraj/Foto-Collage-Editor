package com.wisesharksoftware.core;

import java.util.Map.Entry;

import android.content.Context;

import com.smsbackupandroid.lib.ExceptionHandler;

/**
 * A filter to posterize an image.
 */
public class PosterizeFilter extends Filter {

  private static final long serialVersionUID = 1L;

  private int numLevels;
	private int[] levels;
  private boolean initialized = false;

  private static final String NUM_LEVELS = "levels";

	public PosterizeFilter() {
		setNumLevels(6);
		filterName = FilterFactory.POSTERIZE_FILTER;
	}
	
	/**
     * Set the number of levels in the output image.
     * @param numLevels the number of levels
     * @see #getNumLevels
     */
    public void setNumLevels(int numLevels) {
		this.numLevels = numLevels;
		initialized = false;
	}

	/**
     * Get the number of levels in the output image.
     * @return the number of levels
     * @see #setNumLevels
     */
	public int getNumLevels() {
		return numLevels;
	}

	/**
     * Initialize the filter.
     */
  protected void initialize() {
		levels = new int[256];
		if (numLevels != 1)
			for (int i = 0; i < 256; i++)
				levels[i] = 255 * (numLevels*i / 256) / (numLevels-1);
	}
	
  public void processImage(Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
  {
    if (!initialized) {
      initialized = true;
      initialize();
    }
      try
      {
          for( int i = 0; i < image.height; i++ )
          {
              for( int j = 0; j < image.width; j++ )
              {
                  int rgb = image.data[ i ][ j ];
                  int a = rgb & 0xff000000;
                  int r = (rgb >> 16) & 0xff;
                  int g = (rgb >> 8) & 0xff;
                  int b = rgb & 0xff;
                  r = levels[r];
                  g = levels[g];
                  b = levels[b];
                  image.data[ i ][ j ] = a | (r << 16) | (g << 8) | b;
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
          
          if(name.equals(NUM_LEVELS))
          {
              numLevels = Integer.parseInt(value);
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
  	//param multiply
  	s += "{";
  	s += "\"name\":" + "\"" + NUM_LEVELS + "\",";
  	s += "\"value\":" + "\"" + numLevels + "\"";
  	s += "}";
  	s += "]";
  	//end params array
  	s += "}";
  	return s;
  }
}

