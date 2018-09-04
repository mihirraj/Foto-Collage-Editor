package com.wisesharksoftware.core;

import com.wisesharksoftware.core.filters.*;

import java.util.HashMap;
import java.util.Map;

public class FilterFactory
{
    public static final String NORMAL_FILTER = "normal";
    public static final String VIGNETTE_FILTER = "vignette";
    public static final String CURVES_FILTER = "curves";
    public static final String BLEND_FILTER = "blend";
    public static final String BILLBOARD_FILTER = "billboard";
    public static final String ANIMAL_EYES_FILTER = "animal_eyes";
    public static final String HSV_FILTER = "hsv";
    public static final String GLITCH_FILTER = "glitch";
    public static final String SIMPLE_GLITCH_FILTER = "simple_glitch";
    public static final String COLORIZE_FILTER = "colorize";
    public static final String TEXT_OVERLAY_FILTER = "text_overlay";
    public static final String TILT_SHIFT_FILTER = "tilt_shift";
    public static final String FISH_EYE_FILTER = "fish_eye";
    public static final String MULTI_PICTURES_FILTER = "multi_pictures";
    public static final String COMBINE_PICTURES_FILTER = "combine_pictures";    
    public static final String MULTIPLES_SCENES_FILTER = "multiple_scenes";
    public static final String MEDIAN_FILTER = "median";
    public static final String THRESHOLD_FILTER = "threshold";
    public static final String EDGE_FILTER = "edge";
    public static final String POSTERIZE_FILTER = "posterize";
    public static final String BIT_FILTER = "8bit";
    public static final String COMIC_FILTER = "comic";
    public static final String GRAY_FILTER = "gray";
    public static final String CB_FILTER = "contrast_brightness";
    public static final String HDR_FILTER = "hdr";
    public static final String HDR_FILTER2 = "hdr2";
    public static final String COARSE_EDGES_FILTER = "coarse_edges";
    public static final String DILATION_FILTER = "dilation";
    public static final String FLIP_ROTATE_FILTER = "flip_rotate";
    public static final String THRESHOLD_BLUR_FILTER = "threshold_blur";
    public static final String EDGE_CLOSING_FILTER = "edge_closing";
    public static final String CLOSING_FILTER = "closing";
    public static final String SHARPEN_FILTER = "sharpen";
    public static final String SQUARE_FILTER = "square";
    public static final String SQUARE_BORDER_FILTER = "square_border";
    public static final String STICKER_FILTER = "sticker";
    public static final String CROP_FILTER = "crop";
    public static final String MIRROR_FILTER = "mirror";
    public static final String FOCUS_FILTER = "focus";
    public static final String COLOR_TEMPERATURE_FILTER = "color_temperature";
    public static final String FACE_DETECTION_FILTER = "face_detection";
    public static final String SHEET_DETECTION_FILTER = "sheet_detection";
    public static final String PERSPECTIVE_TRANSFORM_FILTER = "perspective_transform";
    public static final String CONVOLUTION_FILTER = "convolution";
    public static final String SHARPNESS_FILTER = "sharpness";
    public static final String SAVE_IMAGE_FILTER = "save_image";
    public static final String INK_FILTER = "ink";
    
    private static final FilterFactory filterFactory = new FilterFactory();

    static
    {
        try
        {
            filterFactory.registerFilter( NORMAL_FILTER, NormalFilter.class );
            filterFactory.registerFilter( VIGNETTE_FILTER, VignetteFilter.class );
            filterFactory.registerFilter( CURVES_FILTER, CurveFilter.class );
            filterFactory.registerFilter( ANIMAL_EYES_FILTER, AnimalEyesFilter.class );
            filterFactory.registerFilter( BLEND_FILTER, BlendFilter.class );
            filterFactory.registerFilter( BILLBOARD_FILTER, BillboardFilter.class );
            filterFactory.registerFilter( HSV_FILTER, HsvFilter.class );
            filterFactory.registerFilter( GLITCH_FILTER, GlitchFilter.class );
            filterFactory.registerFilter( SIMPLE_GLITCH_FILTER, SimpleGlitchFilter.class );
            filterFactory.registerFilter( COLORIZE_FILTER, ColorizeHsvFilter.class );
            filterFactory.registerFilter( TEXT_OVERLAY_FILTER, TextOverlayFilter.class );
            filterFactory.registerFilter( TILT_SHIFT_FILTER, TiltShiftFilter.class );
            filterFactory.registerFilter( FISH_EYE_FILTER, FishEyeFilter.class );
            filterFactory.registerFilter( MULTI_PICTURES_FILTER, MultiPicturesFilter.class );
            filterFactory.registerFilter( MULTIPLES_SCENES_FILTER, MultipleScenesFilter.class );
            filterFactory.registerFilter( COMBINE_PICTURES_FILTER, CombinePicturesFilter.class );            
            filterFactory.registerFilter( MEDIAN_FILTER , MedianFilter.class );
            filterFactory.registerFilter( THRESHOLD_FILTER , ThresholdFilter.class );
            filterFactory.registerFilter( EDGE_FILTER , EdgeFilter.class );
            filterFactory.registerFilter( POSTERIZE_FILTER , PosterizeFilter.class );
            filterFactory.registerFilter( BIT_FILTER , BitFilter.class );
            filterFactory.registerFilter( COMIC_FILTER , ComicFilter.class );
            filterFactory.registerFilter( GRAY_FILTER , GrayFilter.class );
            filterFactory.registerFilter( CB_FILTER, ContrastBrightnessFilter.class );
            filterFactory.registerFilter( HDR_FILTER, HDRFilter.class );
            filterFactory.registerFilter( HDR_FILTER2, HDRFilter2.class );
            filterFactory.registerFilter( COARSE_EDGES_FILTER, CoarseEdgesFilter.class );
            filterFactory.registerFilter( DILATION_FILTER, DilationFilter.class );
            filterFactory.registerFilter( FLIP_ROTATE_FILTER, FlipRotateFilter.class );
            filterFactory.registerFilter( THRESHOLD_BLUR_FILTER, ThresholdBlurFilter.class );
            filterFactory.registerFilter( EDGE_CLOSING_FILTER, EdgeClosingFilter.class );
            filterFactory.registerFilter( CLOSING_FILTER, ClosingFilter.class );
            filterFactory.registerFilter( SHARPEN_FILTER, SharpenFilter.class );
            filterFactory.registerFilter( SQUARE_FILTER, SquareFilter.class );
            filterFactory.registerFilter( SQUARE_BORDER_FILTER, SquareBorderFilter.class );
            filterFactory.registerFilter( STICKER_FILTER, StickerFilter.class );
            filterFactory.registerFilter( COLOR_TEMPERATURE_FILTER, ColorTemperatureFilter.class );
            filterFactory.registerFilter( CROP_FILTER, CropFilter.class );
            filterFactory.registerFilter( MIRROR_FILTER, MirrorFilter.class );            
            filterFactory.registerFilter( FOCUS_FILTER, FocusFilter.class );
            filterFactory.registerFilter( CONVOLUTION_FILTER, ConvolutionFilter.class );
            filterFactory.registerFilter( SHARPNESS_FILTER, SharpnessFilter.class );
            filterFactory.registerFilter( FACE_DETECTION_FILTER, FaceDetectionFilter.class );
            filterFactory.registerFilter( SHEET_DETECTION_FILTER, SheetDetectionFilter.class );
            filterFactory.registerFilter( PERSPECTIVE_TRANSFORM_FILTER, PerspectiveTransformFilter.class );            
            filterFactory.registerFilter( SAVE_IMAGE_FILTER, SaveImageFilter.class );
            filterFactory.registerFilter( INK_FILTER, InkFilter.class );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
        }
    }

    public static FilterFactory getInstance()
    {
        return filterFactory;
    }

    public Filter getFilter( String type ) throws Exception
    {
        return ( Filter )filters.get( type ).getConstructor().newInstance();
    }

    @SuppressWarnings( "rawtypes" )
    private Map<String, Class> filters = new HashMap<String, Class>();

    public void registerFilter( String type, @SuppressWarnings( "rawtypes" ) Class filter )
    {
        filters.put( type, filter );
    }
}
