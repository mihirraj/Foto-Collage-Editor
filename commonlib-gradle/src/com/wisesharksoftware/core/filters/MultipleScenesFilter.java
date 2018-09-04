package com.wisesharksoftware.core.filters;

import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

import java.util.Map.Entry;

public class MultipleScenesFilter extends Filter
{    
    private static final long serialVersionUID = 1L;

    private final static String PICTURES_COUNT = "pictures_count";

    private Integer picturesCount;
    private int currentPicture;

    public MultipleScenesFilter( )
    {
    	filterName = FilterFactory.MULTIPLES_SCENES_FILTER;
    	picturesCount = 0;
    }
    
    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();
            
            if( name.equals( PICTURES_COUNT ) )
            {
                picturesCount = Integer.parseInt( value );
            }
        }
    }

    @Override
    public boolean hasNativeProcessing()
    {
        return false;
    }

    @Override
    public boolean hd()
    {
        return false;
    }
    
    @Override
    public String convertToJSON() {
    	String s = "{";
    	s += "\"type\":" + "\"" + filterName + "\",";
    	//start params array
    	s += "\"params\":" + "[";
    	//param PICTURES_COUNT
      	s += "{";
      	s += "\"name\":" + "\"" + PICTURES_COUNT + "\",";
      	s += "\"value\":" + "\"" + picturesCount + "\"";
      	s += "}";
        s += "]";
    	//end params array
    	s += "}";
    	return s;
    }
}
