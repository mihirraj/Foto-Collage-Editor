package com.wisesharksoftware.core.filters;

import java.util.Map.Entry;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

public class AnimalEyesFilter extends Filter
{
    private static final long serialVersionUID = 1L;
    
    private String blendSrc;    
    private static final String PARAM_IMAGE = "image";
    
    public AnimalEyesFilter() {
    	filterName = FilterFactory.ANIMAL_EYES_FILTER;
    }
    
    public void setBlendSrc(String value) {
      blendSrc = value;
    }
    
    protected void onSetParams()
    {
        for( Entry<String, String> paramEntry : params.entrySet() )
        {
            String name = paramEntry.getKey();
            String value = paramEntry.getValue();
            
            if( name.equals( PARAM_IMAGE ) )
            {
                blendSrc = value;
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
    	//param image
    	s += "{";
    	s += "\"name\":" + "\"" + PARAM_IMAGE + "\",";
    	s += "\"value\":" + "\"" + blendSrc + "\"";
    	s += "}";
    	s += "]";
    	//end params array
    	s += "}";
    	return s;
    }
}
