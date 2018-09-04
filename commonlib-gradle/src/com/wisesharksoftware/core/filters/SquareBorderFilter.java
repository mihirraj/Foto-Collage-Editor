package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class SquareBorderFilter extends Filter
{
    private static final long serialVersionUID = 1L;
    private int background = 0;
    public int getBackground() {
		return background;
	}

	public void setBackground(int background) {
		this.background = background;
	}

	public boolean isReflect() {
		return reflect;
	}

	public void setReflect(boolean reflect) {
		this.reflect = reflect;
	}

	public boolean isUsecolorbackground() {
		return usecolorbackground;
	}

	public void setUsecolorbackground(boolean usecolorbackground) {
		this.usecolorbackground = usecolorbackground;
	}

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	private boolean reflect = false;
    private boolean usecolorbackground = false;
	private int red = 0;
	private int green = 0;
	private int blue = 0;
	
    public SquareBorderFilter() {
    	filterName = FilterFactory.SQUARE_BORDER_FILTER;
    }
    
    @Override
    protected void onSetParams()
    {
    	  for( Entry<String, String> paramsEntry : params.entrySet() )
          {
              String name = paramsEntry.getKey();
              String value = paramsEntry.getValue();
              
              if( name.equals( "background" ) )
              {
                  background = Integer.parseInt(value);
              }
              if( name.equals( "reflect" ) )
              {
            	  if (value.equals("true")) {
            		  reflect = true;  
            	  }                  
              }
              if( name.equals( "usecolorbackground" ) )
              {
            	  if (value.equals("true")) {
            		  usecolorbackground = true;  
            	  }                  
              }
              if( name.equals( "red" ) )
              {
                  red = Integer.parseInt(value);
              }
              if( name.equals( "green" ) )
              {
                  green = Integer.parseInt(value);
              }
              if( name.equals( "blue" ) )
              {
                  blue = Integer.parseInt(value);
              }
          }
    }

    @Override
    public boolean hasNativeProcessing()
    {
        return false;
    }
/*
    @Override
    public boolean processOpenCV(Context context, String srcPath, String outPath) {
    	return false;
    	return hsvFilterOpenCV(srcPath, outPath, hue, saturation, value);
    }

    private static native boolean hsvFilterOpenCV(String inFileName, String outFileName, int hue, int saturation, int value);
*/
    
    @Deprecated
    private static native void nativeProcessing( Bitmap bitmap, int h, int s, int v );
    
    @Override
    public String convertToJSON() {
    	String s = "{";
    	s += "\"type\":" + "\"" + filterName + "\",";
    	//start params array
    	s += "\"params\":" + "[";
    	//param kernel size
      	s += "{";
      	s += "\"name\":" + "\"" + "background" + "\",";
      	s += "\"value\":" + "\"" + background + "\"";
      	s += "},";
      	//param reflect      	
      	s += "{";
      	s += "\"name\":" + "\"" + "reflect" + "\",";
      	s += "\"value\":" + "\"" + reflect + "\"";
      	s += "},";
      	//param usecolorbackground      	
      	s += "{";
      	s += "\"name\":" + "\"" + "usecolorbackground" + "\",";
      	s += "\"value\":" + "\"" + usecolorbackground + "\"";
      	s += "},";
      	//param red      	
      	s += "{";
      	s += "\"name\":" + "\"" + "red" + "\",";
      	s += "\"value\":" + "\"" + red + "\"";
      	s += "},";
      	//param green      	
      	s += "{";
      	s += "\"name\":" + "\"" + "green" + "\",";
      	s += "\"value\":" + "\"" + green + "\"";
      	s += "},";
      	//param blue
      	s += "{";
      	s += "\"name\":" + "\"" + "blue" + "\",";
      	s += "\"value\":" + "\"" + blue + "\"";
      	s += "}";      	
    	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }
}
