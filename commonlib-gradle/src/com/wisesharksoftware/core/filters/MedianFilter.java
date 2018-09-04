package com.wisesharksoftware.core.filters;

import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Image2;

import android.content.Context;

/**
 * A filter which performs a 3x3 median operation. Useful for removing dust and noise.
 */
public class MedianFilter extends Filter {

  private static final long serialVersionUID = 1L;

  public MedianFilter() {
	}

	private int median(int[] array) {
		int max, maxIndex;
		
		for (int i = 0; i < 4; i++) {
			max = 0;
			maxIndex = 0;
			for (int j = 0; j < 9; j++) {
				if (array[j] > max) {
					max = array[j];
					maxIndex = j;
				}
			}
			array[maxIndex] = 0;
		}
		max = 0;
		for (int i = 0; i < 9; i++) {
			if (array[i] > max)
				max = array[i];
		}
		return max;
	}

	private int rgbMedian(int[] r, int[] g, int[] b) {
		int sum, index = 0, min = Integer.MAX_VALUE;
		
		for (int i = 0; i < 9; i++) {
			sum = 0;
			for (int j = 0; j < 9; j++) {
				sum += Math.abs(r[i]-r[j]);
				sum += Math.abs(g[i]-g[j]);
				sum += Math.abs(b[i]-b[j]);
			}
			if (sum < min) {
				min = sum;
				index = i;
			}
		}
		return index;
	}

  public void processImage( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
  {
    int width = image.width;
    int height = image.height;
		int index = 0;
		int[] argb = new int[9];
		int[] r = new int[9];
		int[] g = new int[9];
		int[] b = new int[9];
		int[][] outPixels = new int[width][height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int k = 0;
				for (int dy = -1; dy <= 1; dy++) {
					int iy = y+dy;
					if (0 <= iy && iy < height) {
						for (int dx = -1; dx <= 1; dx++) {
							int ix = x+dx;
							if (0 <= ix && ix < width) {
								int rgb = image.data[ix][iy];
								argb[k] = rgb;
								r[k] = (rgb >> 16) & 0xff;
								g[k] = (rgb >> 8) & 0xff;
								b[k] = rgb & 0xff;
								k++;
							}
						}
					}
				}
				while (k < 9) {
					argb[k] = 0xff000000;
					r[k] = g[k] = b[k] = 0;
					k++;
				}
				outPixels[x][y] = argb[rgbMedian(r, g, b)];
			}
		}
	}

	public String toString() {
		return "Blur/Median";
	}

  @Override
  protected void onSetParams() {
  }

  @Override
  public boolean hasNativeProcessing() {
    return false;
  }

}

