package com.wisesharksoftware.camera;

import android.content.Context;

import com.smsbackupandroid.lib.R;
import com.smsbackupandroid.lib.SettingsHelper;

public class AppSettings {
  public static Boolean getHdEnabled(Context context) {
    return context.getResources().getBoolean(R.bool.hd_enabled);
  }
  public static Boolean getPreviewEnabled(Context context) {
    return context.getResources().getBoolean(R.bool.preview_enabled);
  }
  public static Boolean getOpenCvEnabled(Context context) {
    return context.getResources().getBoolean(R.bool.use_open_cv);
  }

  public static boolean isHdImage(Context context, int width, int height) {
    return height >= getHdHeight(context) && width >= getHdWidth(context);
  }
  
  public static int getOriginalWidth(Context context, Boolean isHd) {
    if (isHd) {
      return getOriginalHdWidth(context);
    } else {
      return getOriginalWidth(context);
    }
  }
  
  public static int getOriginalHeight(Context context, Boolean isHd) {
    if (isHd) {
      return getOriginalHdHeight(context);
    } else {
      return getOriginalHeight(context);
    }
  }

  public static int getWidth(Context context, Boolean isHd) {
    if (isHd) {
      return getHdWidth(context);
    } else {
      return getSdWidth(context);
    }
  }
  
  public static int getHeight(Context context, Boolean isHd) {
    if (isHd) {
      return getHdHeight(context);
    } else {
      return getSdHeight(context);
    }
  }
  
  public static int getWidth(Context context) {
	  int res = getResolution(context);
	  return getWidth(context, res);
  }

  public static int getWidth(Context context, int resolutionType) {
	  switch (resolutionType) {
	  	case RES_LOW: return getLowWidth(context);
	  	case RES_NORMAL: return getNormalWidth(context);
	  	case RES_HIGH: return getHighWidth(context);
	  	default: return getNormalWidth(context);
	  }
  }

  public static int getHeight(Context context) {
	  int res = getResolution(context);
	  return getHeight(context, res);
  }

  public static int getHeight(Context context, int resolutionType) {
	  switch (resolutionType) {
	  	case RES_LOW: return getLowHeight(context);
	  	case RES_NORMAL: return getNormalHeight(context);
	  	case RES_HIGH: return getHighHeight(context);
	  	default: return getNormalHeight(context);
	  }
  }
  
  public static int getResolution(Context context) {
	  int res = SettingsHelper.getInt(context, RESOLUTION_SETTINGS_TYPE, RES_NOTSET);
	  if (res == RES_NOTSET) {
		  setResolution(context, RES_LOW);
		  res = RES_LOW;
	  }
	  return res;
  }
  
  public static boolean isSaveOriginal(Context context) {
	  return SettingsHelper.getBoolean(context, SAVE_ORIGINAL_SETTINGS_KEY, true);
  }

  public static void setSaveOriginal(Context context, boolean saveOriginal) {
	  SettingsHelper.setBoolean(context, SAVE_ORIGINAL_SETTINGS_KEY, saveOriginal);
  }

  //
  public static boolean isDoSquarePhoto(Context context) {
	  boolean defValue = context.getResources().getBoolean(R.bool.settings_do_square_photo_default);
	  return SettingsHelper.getBoolean(context, DO_SQUARE_PHOTO_KEY, defValue);
  }

  public static void setDoSquarePhoto(Context context, boolean doSquarePhoto) {
	  SettingsHelper.setBoolean(context, DO_SQUARE_PHOTO_KEY, doSquarePhoto);
  }
  
  public static void setResolution(Context context, int resoltion) {
	  SettingsHelper.setInt(context, RESOLUTION_SETTINGS_TYPE, resoltion);
  }

  public static void setResolutionKey(Context context, String resoltion) {
	  SettingsHelper.setString(context, RESOLUTION_SETTINGS_KEY, resoltion);
  }

  public static int getPreviewWidth(Context context) {
    return context.getResources().getInteger(R.integer.preview_width);
  }
	  
  public static int getPreviewHeight(Context context) {
    return context.getResources().getInteger(R.integer.preview_height);
  }
  
  private static int getHdWidth(Context context) {
    return context.getResources().getInteger(R.integer.image_width_hd);
  }

  private static int getSdWidth(Context context) {
    return context.getResources().getInteger(R.integer.image_width);
  }
  
  private static int getHdHeight(Context context) {
    return context.getResources().getInteger(R.integer.image_height_hd);
  }

  private static int getSdHeight(Context context) {
    return context.getResources().getInteger(R.integer.image_height);
  }

  private static int getOriginalHdWidth(Context context) {
    return context.getResources().getInteger(R.integer.image_width_hd_original);
  }

  private static int getOriginalHdHeight(Context context) {
    return context.getResources().getInteger(R.integer.image_height_hd_original);
  }

  private static int getOriginalWidth(Context context) {
    return context.getResources().getInteger(R.integer.image_width_original);
  }

  private static int getOriginalHeight(Context context) {
    return context.getResources().getInteger(R.integer.image_height_original);
  }

  private static int getLowWidth(Context context) {
	  return context.getResources().getInteger(R.integer.image_width_low);
  }
	  
  private static int getLowHeight(Context context) {
	  return context.getResources().getInteger(R.integer.image_height_low);
  }

  private static int getNormalWidth(Context context) {
	  return context.getResources().getInteger(R.integer.image_width_normal);
  }
	  
  private static int getNormalHeight(Context context) {
	  return context.getResources().getInteger(R.integer.image_height_normal);
  }

  private static int getHighWidth(Context context) {
	  return context.getResources().getInteger(R.integer.image_width_high);
  }
	  
  private static int getHighHeight(Context context) {
	  return context.getResources().getInteger(R.integer.image_height_high);
  }

  public final static String RESOLUTION_SETTINGS_TYPE = "resolution_settings_type";
  public final static String RESOLUTION_SETTINGS_KEY = "resolution_settings_key";
  public final static String SAVE_ORIGINAL_SETTINGS_KEY = "save_original_settings_key";
  public final static String DO_SQUARE_PHOTO_KEY = "do_square_photo_key";
  
  private final static int RES_NOTSET = -1;
  public final static int RES_LOW = 0;
  public final static int RES_NORMAL = 1;
  public final static int RES_HIGH = 2;
}
