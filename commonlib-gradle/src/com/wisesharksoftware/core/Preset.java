package com.wisesharksoftware.core;

import android.content.Context;
import android.graphics.Bitmap;

import com.flurry.android.FlurryAgent;
import com.smsbackupandroid.lib.ExceptionHandler;

import java.io.Serializable;
import java.util.ArrayList;

public class Preset implements Serializable
{
    public enum Contrast
    {
        contrast_high,
        contrast_low,
        contrast_normal
    }

    public enum Scratches
    {
        scratches_no,
        scratches_alot
    }

    public enum Vignette
    {
        vignette_yes,
        vignette_no
    }

    private static final long serialVersionUID = 1L;

    private static final String PICTURES_COUNT = "pictures_count";
    private static final String COMPOSE_TYPE = "compose_type";
    private static final String COMPOSE_TYPE_VERTICAL = "vertical";

    private String name;
    private String imageResourceName;
    private String nameResourceName;
    private String descriptionImgResourceName;
    private String descriptionTextResourceName;
    private Contrast contrastResourceName;
    private Scratches scratchesResourceName;
    private Vignette vignetteResourceName;
    private String headerImageResourceNamePortrait;
    private String headerImageResourceNameLandscape;
    private int footerBackgroundColor;
    private int zIndex = 1;
    private boolean square = false;
    private Filter[] filters;
    private String productId;

    public Preset setName( String name )
    {
        this.name = name;
        return this;
    }

    public Preset setImageResourceName( String imageResourceName )
    {
        this.imageResourceName = imageResourceName;
        return this;
    }

    public Preset setNameResourceName( String nameResourceName )
    {
        this.nameResourceName = nameResourceName;
        return this;
    }

    public Preset setContrastResourceName( Contrast contrastResourceName )
    {
        this.contrastResourceName = contrastResourceName;
        return this;
    }

    public Preset setScratchesResourceName( Scratches scratchesResourceName )
    {
        this.scratchesResourceName = scratchesResourceName;
        return this;
    }

    public Preset setVignetteResourceName( Vignette vignetteResourceName )
    {
        this.vignetteResourceName = vignetteResourceName;
        return this;
    }

    public Preset setHeaderImageResourceNamePortrait( String headerImageResourceNamePortrait )
    {
        this.headerImageResourceNamePortrait = headerImageResourceNamePortrait;
        return this;
    }

    public Preset setHeaderImageResourceNameLandscape( String headerImageResourceNameLandscape )
    {
        this.headerImageResourceNameLandscape = headerImageResourceNameLandscape;
        return this;
    }

    public Preset setFooterBackgroundColor( int footerBackgroundColor )
    {
        this.footerBackgroundColor = footerBackgroundColor;
        return this;
    }

    public Preset setzIndex( int zIndex )
    {
        this.zIndex = zIndex;
        return this;
    }

    public Preset setSquare( boolean square )
    {
        this.square = square;
        return this;
    }

    public Preset setFilters( Filter[] filters )
    {
        this.filters = filters;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public String getImageResourceName()
    {
        return imageResourceName;
    }

    public String getNameResourceName()
    {
        return nameResourceName;
    }

    public Contrast getContrastResourceName()
    {
        return contrastResourceName;
    }

    public Scratches getScratchesResourceName()
    {
        return scratchesResourceName;
    }

    public Vignette getVignetteResourceName()
    {
        return vignetteResourceName;
    }

    public String getHeaderImageResourceNamePortrait()
    {
        return headerImageResourceNamePortrait;
    }

    public String getHeaderImageResourceNameLandscape()
    {
        return headerImageResourceNameLandscape;
    }

    public int getFooterBackgroundColor()
    {
        return footerBackgroundColor;
    }

    public int getzIndex()
    {
        return zIndex;
    }

    public boolean isSquare()
    {
        return square;
    }

    public Filter[] getFilters()
    {
        return filters;
    }

    public Filter getFilter(String type)
    {
    	for (int i = 0; i < filters.length; ++i) {
    		if (filters[i].getFilterName().equals(type)) {
    			return filters[i];
    		}
    	}
        return null;
    }

    public int getPicturesCount()
    {
        for( Filter filter : filters )
        {
            String cnt = filter.getParams().get( PICTURES_COUNT );
            if( cnt != null )
            {
                return Integer.parseInt( cnt );
            }
        }
        return 1;
    }

    public boolean useNativeProcessing()
    {
        for( Filter filter : filters )
        {
            if( !filter.hasNativeProcessing() )
            {
                return false;
            }
        }
        return true;
    }
    
    public void processOpenCV(Context context, String srcPath, String outPath) {
      String inFile = srcPath;
      Boolean isFirst = true;
      for( Filter filter : filters )
      {
        filter.processOpenCV(context, inFile, outPath);
        if (isFirst) {
          isFirst = false;
          inFile = outPath;
        }
      }
    }
    
    public void process( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd, String outFileName, Bitmap bitmap )
    {
        FlurryAgent.onEvent( "ProcessPhoto:" + name );
        try
        {
            if( bitmap != null && useNativeProcessing() )
            {
                for( Filter filter : filters )
                {
                    filter.setOutFileName( outFileName );
                    filter.processBitmap( bitmap, context, square, isPortraitPhoto );
                }
            }
            else
            {
                for( Filter filter : filters )
                {
                    filter.setOutFileName( outFileName );
                    filter.init( context );
                    filter.processImage( image, context, square, isPortraitPhoto, hd );
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "ProcessPhoto:" + name + "_Error" );
        }
    }

	public String getDescriptionImgResourceName() {
		return descriptionImgResourceName;
	}

	public Preset setDescriptionImgResourceName(String descriptionImgResourceName) {
		this.descriptionImgResourceName = descriptionImgResourceName;
		return this;
	}

	public String getDescriptionTextResourceName() {
		return descriptionTextResourceName;
	}

	public Preset setDescriptionTextResourceName(
			String descriptionTextResourceName) {
		this.descriptionTextResourceName = descriptionTextResourceName;
		return this;
	}

	public String getProductId() {
		return productId;
	}

	public Preset setProductId(String productId) {
		this.productId = productId;
		return this;
	}
	
	public boolean isVertical() {
        for( Filter filter : filters )
        {
            String type = filter.getParams().get( COMPOSE_TYPE );
            if( type != null )
            {
                return type.equals(COMPOSE_TYPE_VERTICAL);
            }
        }
        return false;
	  }

	public String convertToJSON() {
		String s = "{";
		s += "\"name\":" + "\"" + name + "\",";
		s += "\"imageResourceName\":" + "\"" + imageResourceName + "\",";
		s += "\"zIndex\":" + "\"" + zIndex + "\",";
		s += "\"square\":" + "\"" + square + "\",";
		// start filters array
		s += "\"filters\":" + "[";
		for (int i = 0; i < filters.length; i++) {
			s += filters[i].convertToJSON();
			if (i != filters.length - 1) {
				s += ",";
			}
		}
		s += "]";
		// end filters array
		s += "}";
		return s;
	}
}