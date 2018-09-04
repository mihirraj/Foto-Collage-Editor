package com.wisesharksoftware.core.filters;

import java.util.Map.Entry;

import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Image2;

import android.content.Context;

/**
 * An edge-detection filter.
 */
public class EdgeFilter extends Filter {
	
  private boolean multiplyWithOriginal_ = true;
  private boolean doGrayScale_ = true;

  private final static String MULTIPLY_WITH_ORIGINAL = "multiply";
  private final static String GRAY_SCALE = "gray";
  
  private static final long serialVersionUID = 1L;

  public final static float R2 = (float)Math.sqrt(2);

	public final static float[] ROBERTS_V = {
		0,  0, -1,
		0,  1,  0,
		0,  0,  0,
	};
	public final static float[] ROBERTS_H = {
		-1,  0,  0,
		0,  1,  0,
		0,  0,  0,
	};
	public final static float[] PREWITT_V = {
		-1,  0,  1,
		-1,  0,  1,
		-1,  0,  1,
	};
	public final static float[] PREWITT_H = {
		-1, -1, -1,
		0,  0,  0,
		1,  1,  1,
	};
	public final static float[] SOBEL_V = {
		-1,  0,  1,
		-2,  0,  2,
		-1,  0,  1,
	};
	public static float[] SOBEL_H = {
		-1, -2, -1,
		0,  0,  0,
		1,  2,  1,
	};
 public final static float[] TEST_V = {
    -3,  0,  3,
    -10,  0,  10,
    -3,  0,  3,
  };
  public static float[] TEST_H = {
    -3, -10, -3,
    0,  0,  0,
    3,  10,  3,
  };
	public final static float[] FREI_CHEN_V = {
		-1,  0,  1,
		-R2,  0,  R2,
		-1,  0,  1,
	};
	public static float[] FREI_CHEN_H = {
		-1, -R2, -1,
		0,  0,  0,
		1,  R2,  1,
	};

	protected float[] vEdgeMatrix = TEST_V;
	protected float[] hEdgeMatrix = TEST_H;

	public EdgeFilter() {
	  	filterName = FilterFactory.EDGE_FILTER;
	}

	public void setVEdgeMatrix(float[] vEdgeMatrix) {
		this.vEdgeMatrix = vEdgeMatrix;
	}

	public float[] getVEdgeMatrix() {
		return vEdgeMatrix;
	}

	public void setHEdgeMatrix(float[] hEdgeMatrix) {
		this.hEdgeMatrix = hEdgeMatrix;
	}

	public float[] getHEdgeMatrix() {
		return hEdgeMatrix;
	}
  @Override
  public void processImage( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
  {
    int width = image.width;
    int height = image.height;
    int[][] outPixels = new int[image.height] [image.width];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int r = 0, g = 0, b = 0;
				int rh = 0, gh = 0, bh = 0;
				int rv = 0, gv = 0, bv = 0;
				int a = image.data[y][x] & 0xff000000;

				for (int row = -1; row <= 1; row++) {
					int iy = y+row;
					int ioffset;
					if (0 <= iy && iy < height)
						ioffset = iy;
					else
						ioffset = y;
					int moffset = 3*(row+1)+1;
					for (int col = -1; col <= 1; col++) {
						int ix = x+col;
						if (!(0 <= ix && ix < width))
							ix = x;
						int rgb = image.data[ioffset][ix];
						float h = hEdgeMatrix[moffset+col];
						float v = vEdgeMatrix[moffset+col];

						r = (rgb & 0xff0000) >> 16;
						g = (rgb & 0x00ff00) >> 8;
						b = rgb & 0x0000ff;
						rh += (int)(h * r);
						gh += (int)(h * g);
						bh += (int)(h * b);
						rv += (int)(v * r);
						gv += (int)(v * g);
						bv += (int)(v * b);
					}
				}
				r = (int)(Math.sqrt(rh*rh + rv*rv) / 1.8);
				g = (int)(Math.sqrt(gh*gh + gv*gv) / 1.8);
				b = (int)(Math.sqrt(bh*bh + bv*bv) / 1.8);
				r = PixelUtils.clamp(r);
				g = PixelUtils.clamp(g);
				b = PixelUtils.clamp(b);
				int res = a | (r << 16) | (g << 8) | b;
				// ======= invert result here (res -> invRes)
				int tmp = res & 0xff000000;
		    res = tmp | (~res & 0x00ffffff);

		    // ======== apply gray scale filter (invRes -> gs)
		    if (doGrayScale_) {
  		    int a1 = res & 0xff000000;
  		    int r1 = (res >> 16) & 0xff;
  		    int g1 = (res >> 8) & 0xff;
  		    int b1 = res & 0xff;
  		    int gs_tmp = (r1 * 77 + g1 * 151 + b1 * 28) >> 8; // NTSC luma
  		    res = a1 | (gs_tmp << 16) | (gs_tmp << 8) | gs_tmp;
		    }
		    // ======= multiply with the original picture (invRes * a -> resMult)
   		  if (multiplyWithOriginal_) {
  		    int orig = image.data[y][x];
          int srcRed = (orig >> 16) & 0xFF;
          int srcGreen = (orig >> 8) & 0xFF;
          int srcBlue = orig & 0xFF;
          int blendRed = (res >> 16) & 0xFF;
          int blendGreen = (res >> 8) & 0xFF;
          int blendBlue = res & 0xFF;
  
          int resRed = ( srcRed * blendRed ) / 255;
          int resGreen = ( srcGreen * blendGreen ) / 255;
          int resBlue = ( srcBlue * blendBlue ) / 255;
          res = ( 0xFF << 24 ) | ( resRed << 16 ) | ( resGreen << 8 ) | resBlue; //Color.rgb
				}
   		  outPixels[y][x] = res;
			}

		}
		image.data = outPixels;
	}

  @Override
  protected void onSetParams() {
    for( Entry<String, String> paramsEntry : params.entrySet() )
    {
        String name = paramsEntry.getKey();
        String value = paramsEntry.getValue();
        
        if(name.equals(MULTIPLY_WITH_ORIGINAL))
        {
          multiplyWithOriginal_ = Boolean.parseBoolean(value);
        }
        else if (name.equals(GRAY_SCALE))
        {
          doGrayScale_ = Boolean.parseBoolean(value);
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
  	s += "\"name\":" + "\"" + MULTIPLY_WITH_ORIGINAL + "\",";
  	s += "\"value\":" + "\"" + multiplyWithOriginal_ + "\"";
  	s += "},";
  	//param grayscale
  	s += "{";
  	s += "\"name\":" + "\"" + GRAY_SCALE + "\",";
  	s += "\"value\":" + "\"" + doGrayScale_ + "\"";
  	s += "}";
  	s += "]";
  	//end params array
  	s += "}";
  	return s;
  }
}
