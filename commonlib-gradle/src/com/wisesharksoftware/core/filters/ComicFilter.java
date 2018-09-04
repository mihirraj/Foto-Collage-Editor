package com.wisesharksoftware.core.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Bitmap;

import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Image2;
import com.wisesharksoftware.core.Utils;

public class ComicFilter extends Filter {
  
  private static final long serialVersionUID = 1L;

  private ArrayList <String> imageSrcs_ = new ArrayList<String>();
  private ArrayList <Integer> brighnesses_ = new ArrayList<Integer>();
  private int[] brightnessImageMap_ = new int[256];
  private int patternSize_;
  private boolean smallPattern_ = true;
  private boolean colorDodge_ = false;
 
  private final static String IMAGES_MAP = "images";
  private final static String PATTERN_SIZE = "pattern_size";
  private final static String COLOR_DODGE = "color_dodge";
  private final static String SMALL_PATTERN = "small_pattern";
  
  public ComicFilter() {
	  	filterName = FilterFactory.COMIC_FILTER;
  }
  
  @Override
  protected void onSetParams() {
    for( Entry<String, String> paramsEntry : params.entrySet() )
    {
        String name = paramsEntry.getKey();
        String value = paramsEntry.getValue();
        
        if( name.equals( IMAGES_MAP ) )
        {
            String[] images = value.split(";");
            for (String image:images) {
              String[] item = image.split(":");
              int brightness = Integer.parseInt(item[0]);
              String imageSrc = item[1];
              imageSrcs_.add(imageSrc);
              brighnesses_.add(brightness);
            }
            int imageIndex = 0;
            int curBrighness = brighnesses_.get(imageIndex);
            for (int i = 0; i < 256; i++) {
              if (i < curBrighness) {
                brightnessImageMap_[i] = imageIndex;
              } else {
                imageIndex++;
                curBrighness = brighnesses_.get(imageIndex);
              }
            }
        } else if (name.equals ( PATTERN_SIZE ) ){
          patternSize_ = Integer.parseInt(value);
        } else if (name.equals ( COLOR_DODGE ) ){
          colorDodge_ = Boolean.parseBoolean(value);
        } else if (name.equals ( SMALL_PATTERN ) ){
          smallPattern_ = Boolean.parseBoolean(value);
        }
    }
  }
    
  @Override
  public void processImage(Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd)
  {
    if (smallPattern_) {
      Image2[] images = new Image2[imageSrcs_.size()];
      for (int i = 0 ; i < imageSrcs_.size(); i++) {
        try {
          Bitmap patternBitmap;
          patternBitmap = Utils.getBitmapAsset( context, "square/" + imageSrcs_.get(i), false );
          images[i] = Image2.fromBitmap(patternBitmap);
          patternBitmap.recycle();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      int maxX = image.width - 1;
      int maxY = image.height - 1;
      for (int y = 0; y < image.height; y += patternSize_) {
        for (int x = 0; x < image.width; x += patternSize_) {
          for (int i = 0; i < patternSize_; ++i) {
            int cY = constrain(y + i, 0, maxY);
            for (int j = 0; j < patternSize_; j++) {
              int cX = constrain(x + j, 0, maxX);
              int rgb = image.data[cY][cX];
              int brightness = PixelUtils.brightness(rgb);
              int imageIndex = brightnessImageMap_[brightness];
              int res = images[imageIndex].data[i][j];
              if (colorDodge_) {
                // copy paste from BlendFilter.java
                int srcRed = (rgb >> 16) & 0xFF;
                int srcGreen = (rgb >> 8) & 0xFF;
                int srcBlue = rgb & 0xFF;
  
                int blendRed = (res >> 16) & 0xFF;
                int blendGreen = (res >> 8) & 0xFF;
                int blendBlue = res & 0xFF;
                
                int resRed = (blendRed == 255) ? blendRed:Math.min(255, ((srcRed << 8 ) / (255 - blendRed)));
                int resGreen = (blendGreen == 255) ? blendGreen:Math.min(255, ((srcGreen << 8 ) / (255 - blendGreen)));
                int resBlue = (blendBlue == 255) ? blendBlue:Math.min(255, ((srcBlue << 8 ) / (255 - blendBlue)));
                res = (0xFF << 24) | (resRed << 16) | (resGreen << 8) | resBlue;
              }
              image.data[cY][cX] = res;
            }
          }
        }
      }
    } else {
      Image2 brightnessImg = new Image2();
      brightnessImg.data = new int[image.height][image.width];
      brightnessImg.width = image.width;
      brightnessImg.height = image.height;
      for (int y = 0; y < image.height; y ++) {
        for (int x = 0; x < image.width; x ++) {
          brightnessImg.data[y][x] = PixelUtils.brightness(image.data[y][x]);
        }
      }
      
      for (int i = 0 ; i < imageSrcs_.size(); i++) {
        Image2 pattern = null;
        try {
          Bitmap patternBitmap;
          patternBitmap = Utils.getBitmapAsset( context, "square/" + imageSrcs_.get(i), false );
          pattern = Image2.fromBitmap(patternBitmap);
          patternBitmap.recycle();
        } catch (IOException e) {
          e.printStackTrace();
        }
        for (int y = 0; y < image.height; y ++) {
          for (int x = 0; x < image.width; x ++) {
            int imageIndex = brightnessImageMap_[brightnessImg.data[y][x]];
            if (imageIndex != i) {  // only one brightness at a time
              continue;
            }
            int res = pattern.data[y][x];
            if (colorDodge_) {
              int rgb = image.data[y][x];
              // copy paste from BlendFilter.java
              int srcRed = (rgb >> 16) & 0xFF;
              int srcGreen = (rgb >> 8) & 0xFF;
              int srcBlue = rgb & 0xFF;

              int blendRed = (res >> 16) & 0xFF;
              int blendGreen = (res >> 8) & 0xFF;
              int blendBlue = res & 0xFF;
              
              int resRed = (blendRed == 255) ? blendRed:Math.min(255, ((srcRed << 8 ) / (255 - blendRed)));
              int resGreen = (blendGreen == 255) ? blendGreen:Math.min(255, ((srcGreen << 8 ) / (255 - blendGreen)));
              int resBlue = (blendBlue == 255) ? blendBlue:Math.min(255, ((srcBlue << 8 ) / (255 - blendBlue)));
              res = (0xFF << 24) | (resRed << 16) | (resGreen << 8) | resBlue;
            }
            image.data[y][x] = res;
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
  public String convertToJSON() {
	String s = "{";
  	s += "\"type\":" + "\"" + filterName + "\",";
  	//start params array
  	s += "\"params\":" + "[";
  	//param pattern size
  	s += "{";
  	s += "\"name\":" + "\"" + PATTERN_SIZE + "\",";
  	s += "\"value\":" + "\"" + patternSize_ + "\"";
  	s += "},";
  	//param images map
  	String map = "";
  	for (int i = 0; i < brighnesses_.size(); i++) {
  		map += brighnesses_.get(i) + ":" + imageSrcs_.get(i) + ";";
  	}
  	map = map.substring(0, map.length() - 1);
  	s += "{";
  	s += "\"name\":" + "\"" + IMAGES_MAP + "\",";
  	s += "\"value\":" + "\"" + map + "\"";
  	s += "},";
  	//param color Dodge
  	s += "{";
  	s += "\"name\":" + "\"" + COLOR_DODGE + "\",";
  	s += "\"value\":" + "\"" + colorDodge_ + "\"";
  	s += "},";
  	//param small Pattern
  	s += "{";
  	s += "\"name\":" + "\"" + SMALL_PATTERN + "\",";
  	s += "\"value\":" + "\"" + smallPattern_ + "\"";
  	s += "}";
  	s += "]";
  	//end params array
  	s += "}";
  	return s;
  }
  
}
