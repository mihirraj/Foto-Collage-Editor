package com.wisesharksoftware.core;

// Just some constants. need to be moved to settings someday.
public class GlobalSettings
{
    // returns width of the image. for now it is global parameter.
    public static int getImageWidth( boolean squared, boolean multiPictures )
    {
        if( squared )
        {
            return getImageHeight( squared, multiPictures );
        }
//        return hd() && !multiPictures ? 1600 : 800;
//        return 1600;
        return 800;
    }

    // returns height of the image. for now it is global parameter.
    public static int getImageHeight( boolean squared, boolean multiPictures )
    {
//        return hd() && !multiPictures ? 1200 : 600;
//        return 1200;
        return 600;
    }
    
    public static boolean hd()
    {
    	return false;
    }

}
