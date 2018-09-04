package com.smsbackupandroid.lib;

import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SettingsHelper {
	public static int getInt(Context ctx, String key, int defaultValue) {
		return getSharedPreferences(ctx).getInt(key, defaultValue);	
	}
	
	public static void setInt(Context ctx, String key, int value) {
		Editor editor = getSharedPreferences(ctx).edit();
    editor.putInt(key, value);
    editor.commit();
	}
	
	public static long getLong(Context ctx, String key, long defaultValue) {
		return getSharedPreferences(ctx).getLong(key, defaultValue);	
	}
	
	public static void setLong(Context ctx, String key, long value) {
		Editor editor = getSharedPreferences(ctx).edit();
		editor.putLong(key, value);
		editor.commit();
	}
	
  public static void setBoolean(Context ctx, String key, Boolean value) {
      Editor editor = getSharedPreferences(ctx).edit();
      editor.putBoolean(key, value);
      editor.commit();
  }

  public static Boolean getBoolean(Context ctx, String key, Boolean defaultValue) {
      return getSharedPreferences(ctx).getBoolean(key, defaultValue);
  }

  public static String getString(Context ctx, String key, String defaultValue) {
    return getSharedPreferences(ctx).getString(key, defaultValue); 
  }
  
  public static void setString(Context ctx, String key, String value) {
    Editor editor = getSharedPreferences(ctx).edit();
    editor.putString(key, value);
    editor.commit();
  }
  
  private static SharedPreferences getSharedPreferences(Context ctx) {
      return PreferenceManager.getDefaultSharedPreferences(ctx);
  }
  
  public static Set<String> getStringSet(Context ctx, String key, Set<String> defaultValue){
	  return getSharedPreferences(ctx).getStringSet(key, defaultValue);  
  }
  
  public static void setStringSet(Context ctx, String key, Set<String> value) {
	    Editor editor = getSharedPreferences(ctx).edit();
	    editor.putStringSet(key, value);
	    editor.commit();
	  }
}
