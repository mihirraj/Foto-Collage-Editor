package com.wisesharksoftware.core.filters;

import java.util.Map.Entry;

import android.content.Context;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Image2;

/**
 * Converts an image to gray scale
 * 
 * @author Roman
 *
 */
public class GrayFilter extends Filter {
  private static final long serialVersionUID = 1L;
  private boolean use3channels = false;
  
  public GrayFilter() {
  	filterName = FilterFactory.GRAY_FILTER;
  }

  protected void onSetParams() {
	  for( Entry<String, String> paramsEntry : params.entrySet() )
      {
          String name = paramsEntry.getKey();
          String value = paramsEntry.getValue();
          
          if( name.equals( "use3channels" ) )
          {
              if (value.equals("true")) {
            	  use3channels = true;  
              }        	  
          }
      }
  }

  @Override
  public void processImage( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
  {
    int height = image.height;
    int width = image.width;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int color = image.data[y][x];
        int srcRed = (color >> 16) & 0xFF;
        int srcGreen = (color >> 8) & 0xFF;
        int srcBlue = color & 0xFF;
        int gray = (srcRed + srcGreen + srcBlue) / 3;
        image.data[y][x] = ((0xFF << 24) | (gray << 16) | (gray << 8) | gray);
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
  	s += "\"type\":" + "\"" + getFilterName() + "\",";
  	//start params array
  	s += "\"params\":" + "[";
	//param use3channels
  	s += "{";
  	s += "\"name\":" + "\"" + "use3channels" + "\",";
  	s += "\"value\":" + "\"" + use3channels + "\"";
  	s += "}";
  	s += "]";
  	//end params array
  	s += "}";
  	return s;
  }

}
