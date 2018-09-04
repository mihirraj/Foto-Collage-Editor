package com.wisesharksoftware.core.filters;

import android.graphics.Bitmap;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class CombinePicturesFilter extends Filter
{      
    private static final long serialVersionUID = 1L;
    
    private String picture1;
    private String picture2;
    private String picture3;
    private String picture4;
    private Bitmap resultBitmap;

    public CombinePicturesFilter( )
    {
    	filterName = FilterFactory.COMBINE_PICTURES_FILTER;
    }
    
    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();
            
            if( name.equals( "picture1" ) )
            {
                setPicture1(value);
            }
            if( name.equals( "picture2" ) )
            {
                setPicture2(value);
            }
            if( name.equals( "picture3" ) )
            {
                setPicture3(value);
            }
            if( name.equals( "picture4" ) )
            {
                setPicture4(value);
            }            
        }
    }
    
    @Override
    public String convertToJSON() {
    	String s = "{";
    	s += "\"type\":" + "\"" + filterName + "\",";
    	//start params array
    	s += "\"params\":" + "[";
    	//param picture1
      	s += "{";
      	s += "\"name\":" + "\"" + "picture1" + "\",";
      	s += "\"value\":" + "\"" + getPicture1() + "\"";
      	s += "},";
    	//param picture2
      	s += "{";
      	s += "\"name\":" + "\"" + "picture2" + "\",";
      	s += "\"value\":" + "\"" + getPicture2() + "\"";
      	s += "},";
    	//param picture3
      	s += "{";
      	s += "\"name\":" + "\"" + "picture3" + "\",";
      	s += "\"value\":" + "\"" + getPicture3() + "\"";
      	s += "},";
    	//param picture4
      	s += "{";
      	s += "\"name\":" + "\"" + "picture4" + "\",";
      	s += "\"value\":" + "\"" + getPicture4() + "\"";
      	s += "}";
        s += "]";
    	//end params array
    	s += "}";
    	return s;
    }

	@Override
	public boolean hasNativeProcessing() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setPictures(String picture1, String picture2, String picture3, String picture4 ) {
		this.picture1 = picture1;
		this.picture2 = picture2;
		this.picture3 = picture3;
		this.picture4 = picture4;
	}
	
	public String getPicture1() {
		return picture1;
	}

	public void setPicture1(String picture1) {
		this.picture1 = picture1;
	}
	
	public String getPicture2() {
		return picture2;
	}

	public void setPicture2(String picture2) {
		this.picture2 = picture2;
	}

	public String getPicture3() {
		return picture3;
	}

	public void setPicture3(String picture3) {
		this.picture3 = picture3;
	}

	public String getPicture4() {
		return picture4;
	}

	public void setPicture4(String picture4) {
		this.picture4 = picture4;
	}	
}
