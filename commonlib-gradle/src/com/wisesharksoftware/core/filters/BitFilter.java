package com.wisesharksoftware.core.filters;

import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Color;

import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Image2;

public class BitFilter extends Filter {

  private static final long serialVersionUID = 1L;

  private final static String PIXEL_SIZE = "pixel_size";
  private final static String COLORS_PER_CHANNEL = "colors_per_channel";
  private final static String BORDER_SIZE = "border_size";
  private final static String BORDER_COLOR = "border_color";
  

  private int pixelSize_  = 4;
  private int colorsPerChannel_ = 8;
  private int borderSize_ = 1;
  private int borderColor_ = 0xFF333333;
  
  public BitFilter() {
  	filterName = FilterFactory.BIT_FILTER;
  }
  
  @Override
  public void processImage(Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd)
  {
    int sqSize = pixelSize_ * pixelSize_;
    int pixelAndBorder = pixelSize_ + borderSize_;
    int colorDiv = 256 / colorsPerChannel_;
    int maxX = image.width - 1;
    int maxY = image.height - 1;
    for (int y = 0; y < image.height; y += pixelAndBorder) {
      for (int x = 0; x < image.width; x += pixelAndBorder) {
        int avgR = 0;
        int avgG = 0;
        int avgB = 0;
        for (int i = 0; i < pixelSize_; ++i) {
          int cY = constrain(y + i, 0, maxY);
          for (int j = 0; j < pixelSize_; j++) {
            int cX = constrain(x + j, 0, maxX);
            int rgb = image.data[cY][cX];
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;
            avgR += r;
            avgG += g;
            avgB += b;
          }
        }

        avgR /= sqSize;
        avgG /= sqSize;
        avgB /= sqSize;
        
        // palette adjustments
        avgR = (avgR / colorDiv) * colorDiv;
        avgG = (avgG / colorDiv) * colorDiv;
        avgB = (avgB / colorDiv) * colorDiv;
        
        int avgColor = ( 0xFF << 24 ) | ( avgR << 16 ) | ( avgG << 8 ) | avgB;
        for (int i = 0; i < pixelAndBorder && y + i <= maxY; ++i) {
          int cY = y + i;
          for (int j = 0; j < pixelAndBorder && x + j <= maxX; j++) {
            int cX = x + j;
            if (i < pixelSize_ && j < pixelSize_)
              image.data[cY][cX] = avgColor;
            else
              image.data[cY][cX] = borderColor_;
          }
        }
      }
    }
    
  }
  @Override
  public boolean hasNativeProcessing() {
    return false;
  }
  @Override
  protected void onSetParams()
  {
      for( Entry<String, String> paramsEntry : params.entrySet() )
      {
          String name = paramsEntry.getKey();
          String value = paramsEntry.getValue();
          
          if(name.equals(PIXEL_SIZE))
          {
            pixelSize_ = Integer.parseInt(value);
          }
          else if(name.equals(COLORS_PER_CHANNEL))
          {
            colorsPerChannel_ = Integer.parseInt(value);
          }
          else if(name.equals(BORDER_SIZE))
          {
            borderSize_ = Integer.parseInt(value);
          }
          else if(name.equals(BORDER_COLOR))
          {
            borderColor_ = Color.parseColor(value);
          }
      }
  }
  
  @Override
  public String convertToJSON() {
  	String s = "{";
  	s += "\"type\":" + "\"" + filterName + "\",";
  	//start params array
  	s += "\"params\":" + "[";
  	//param pixel size
  	s += "{";
  	s += "\"name\":" + "\"" + PIXEL_SIZE + "\",";
  	s += "\"value\":" + "\"" + pixelSize_ + "\"";
  	s += "},";
  	//param colors per channel
  	s += "{";
  	s += "\"name\":" + "\"" + COLORS_PER_CHANNEL + "\",";
  	s += "\"value\":" + "\"" + colorsPerChannel_ + "\"";
  	s += "},";
  	//param border size
  	s += "{";
  	s += "\"name\":" + "\"" + BORDER_SIZE + "\",";
  	s += "\"value\":" + "\"" + borderSize_ + "\"";
  	s += "},";
  	//param border color
  	s += "{";
  	s += "\"name\":" + "\"" + BORDER_COLOR + "\",";
  	s += "\"value\":" + "\"" + String.format("#%X", borderColor_) + "\"";
  	s += "}";
  	s += "]";
  	//end params array
  	s += "}";
  	return s;
  }
}
