package com.wisesharksoftware.core;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wisesharksoftware.core.Preset.Contrast;
import com.wisesharksoftware.core.Preset.Scratches;
import com.wisesharksoftware.core.Preset.Vignette;


public class PresetParser
{
    private static final String CAMERAS = "cameras";
    private static final String PROCESSINGS = "processings";
    private static final String PRESET_NAME = "name";
    private static final String IMAGE_RESOURCE_NAME = "imageResourceName";
    private static final String NAME_RESOURCE_NAME = "nameResourceName";
    private static final String DESCRIPTION_IMAGE_RESOURCE_NAME = "descriptionImageResourceName";
    private static final String DESCRIPTION_TEXT_RESOURCE_NAME = "descriptionTextResourceName";
    private static final String CONTRAST_RESOURCE_NAME = "contrastResourceName";
    private static final String SCRATCHES_RESOURCE_NAME = "scratchesResourceName";
    private static final String VIGNETTE_RESOURCE_NAME = "vignetteResourceName";
    private static final String HEADER_IMAGE_RESOURCE_NAME_PORTRAIT = "headerImageResourceNamePortrait";
    private static final String HEADER_IMAGE_RESOURCE_NAME_LANDSCAPE = "headerImageResourceNameLandscape";
    private static final String FOOTER_BACKGROUND_COLOR = "footerBackgroundColor";
    private static final String PRESET_Z_INDEX = "zIndex";
    private static final String PRESET_SQUARE = "square";
    private static final String FILTERS = "filters";
    private static final String FILTER_TYPE = "type";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_VALUE = "value";
    private static final String PARAMS = "params";
    private static final String WATERMARK = "watermark";
    private static final String PRODUCT_ID = "product_id";

    FilterFactory filterFactory;

    public PresetParser( FilterFactory filterFactory )
    {
        this.filterFactory = filterFactory;
    }

    public Presets parse( String jsonData ) throws Exception
    {
        JSONObject presetsJSON = new JSONObject( jsonData );
        JSONArray cameras = presetsJSON.getJSONArray( CAMERAS );
        JSONArray processings = presetsJSON.getJSONArray( PROCESSINGS );
        Preset watermarkPreset = !presetsJSON.isNull(WATERMARK) ? parsePreset(presetsJSON.getJSONObject(WATERMARK)) : null;

        Preset[] cameraPresets = new Preset[ cameras.length() ];
        for( int i = 0; i < cameras.length(); ++i )
        {
            JSONObject presetJSON = cameras.getJSONObject( i );
            String name = presetJSON.getString( PRESET_NAME );
            String imageResourceName = presetJSON.getString( IMAGE_RESOURCE_NAME );
            String nameResourceName = presetJSON.getString( NAME_RESOURCE_NAME );
            String descriptionImage = getString(presetJSON, DESCRIPTION_IMAGE_RESOURCE_NAME);
            String descriptionText = getString(presetJSON, DESCRIPTION_TEXT_RESOURCE_NAME);
            String productId = getString(presetJSON, PRODUCT_ID);
            Contrast contrast = Contrast.valueOf( presetJSON.getString( CONTRAST_RESOURCE_NAME ) );
            Scratches scratches = Scratches.valueOf( presetJSON.getString( SCRATCHES_RESOURCE_NAME ) );
            Vignette vignette = Vignette.valueOf( presetJSON.getString( VIGNETTE_RESOURCE_NAME ) );
            
            String headerImageResourceNamePortrait = !presetJSON.isNull( HEADER_IMAGE_RESOURCE_NAME_PORTRAIT ) ? 
            		presetJSON.getString( HEADER_IMAGE_RESOURCE_NAME_PORTRAIT ) : null;
            
            String headerImageResourceNameLandscape = !presetJSON.isNull( HEADER_IMAGE_RESOURCE_NAME_LANDSCAPE ) ?
            		presetJSON.getString( HEADER_IMAGE_RESOURCE_NAME_LANDSCAPE ) : null;
            
            int footerBackgroundColor = !presetJSON.isNull(FOOTER_BACKGROUND_COLOR ) ? 
            		presetJSON.getInt( FOOTER_BACKGROUND_COLOR ) : 0;
            int zIndex = 1;
            if( !presetJSON.isNull( PRESET_Z_INDEX ) )
            {
                zIndex = presetJSON.getInt( PRESET_Z_INDEX );
            }
            Boolean square = false;
            if( !presetJSON.isNull( PRESET_SQUARE ) )
            {
                square = presetJSON.getBoolean( PRESET_SQUARE );
            }
            JSONArray filtersJSON = presetJSON.getJSONArray( FILTERS );
            Filter[] filters = new Filter[ filtersJSON.length() ];
            for( int j = 0; j < filtersJSON.length(); ++j )
            {
                JSONObject filterJSON = filtersJSON.getJSONObject( j );
                String type = filterJSON.getString( FILTER_TYPE );
                Filter filter = filterFactory.getFilter( type );
                Map<String, String> params = new HashMap<String, String>();
                if (!filterJSON.isNull( PARAMS )) {
                    JSONArray paramsJSON = filterJSON.getJSONArray( PARAMS );
                    for( int k = 0; k < paramsJSON.length(); ++k )
                    {
                        JSONObject paramJSON = paramsJSON.getJSONObject( k );
                        params.put( paramJSON.getString( PARAM_NAME ), paramJSON.getString( PARAM_VALUE ) );
                    }
                }
                filter.setParams( params );
                filters[ j ] = filter;
            }
            Preset preset = new Preset()
                    .setName( name )
                    .setImageResourceName( imageResourceName )
                    .setNameResourceName( nameResourceName )
                    .setDescriptionImgResourceName(descriptionImage)
                    .setDescriptionTextResourceName(descriptionText)
                    .setContrastResourceName( contrast )
                    .setScratchesResourceName( scratches )
                    .setVignetteResourceName( vignette )
                    .setHeaderImageResourceNamePortrait( headerImageResourceNamePortrait )
                    .setHeaderImageResourceNameLandscape( headerImageResourceNameLandscape )
                    .setFooterBackgroundColor( footerBackgroundColor )
                    .setzIndex( zIndex )
                    .setSquare( square )
                    .setProductId(productId)
                    .setFilters( filters );
            cameraPresets[i] = preset;
        }

        Preset[] processingPresets = new Preset[ processings.length() ];
        for( int i = 0; i < processings.length(); ++i )
        {
        	processingPresets[i] = parsePreset(processings.getJSONObject( i ));
        }

        return new Presets( cameraPresets, processingPresets, watermarkPreset );
    }
    
    private Preset parsePreset(JSONObject presetJSON) throws Exception {
    	if (presetJSON == null) {
    		return null;
    	}
        String name = presetJSON.getString( PRESET_NAME );
        String imageResourceName = presetJSON.getString( IMAGE_RESOURCE_NAME );
        int zIndex = 1;
        if( !presetJSON.isNull( PRESET_Z_INDEX ) )
        {
            zIndex = presetJSON.getInt( PRESET_Z_INDEX );
        }
        Boolean square = false;
        if( !presetJSON.isNull( PRESET_SQUARE ) )
        {
            square = presetJSON.getBoolean( PRESET_SQUARE );
        }
        JSONArray filtersJSON = presetJSON.getJSONArray( FILTERS );
        Filter[] filters = new Filter[ filtersJSON.length() ];
        for( int j = 0; j < filtersJSON.length(); ++j )
        {
            JSONObject filterJSON = filtersJSON.getJSONObject( j );
            String type = filterJSON.getString( FILTER_TYPE );
            Filter filter = filterFactory.getFilter( type );
            Map<String, String> params = new HashMap<String, String>();
            if (!filterJSON.isNull( PARAMS )) {
                JSONArray paramsJSON = filterJSON.getJSONArray( PARAMS );
                for( int k = 0; k < paramsJSON.length(); ++k )
                {
                    JSONObject paramJSON = paramsJSON.getJSONObject( k );
                    params.put( paramJSON.getString( PARAM_NAME ), paramJSON.getString( PARAM_VALUE ) );
                }
            }
            filter.setParams( params );
            filters[ j ] = filter;
        }
        return new Preset()
                .setName( name )
                .setImageResourceName( imageResourceName )
                .setzIndex( zIndex )
                .setSquare( square )
                .setFilters( filters );
    }
    
    private String getString(JSONObject json, String name) throws JSONException {
    	return !json.isNull( name ) ? json.getString( name ) : null;
    }
}
