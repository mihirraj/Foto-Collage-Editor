package com.smsbackupandroid.lib;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class FreeWatcher {
	private static int _maxUse;	
	private static boolean _resetIfVersionChanged;
	static String USED_TIMES_KEY = "USED_TIMES";
	private static boolean _enabled = false;
	
	public static void init(int maxUse, boolean resetIfVersionChanged, Context ctx) {
		_enabled = true;
		_maxUse = maxUse;
		_resetIfVersionChanged = resetIfVersionChanged;
	}
	
	public static void disable() {
		_enabled = false;
	}
	
	/**
	 * If MAX_USE <= current + 1 then returns false.
	 * otherwise increments usages cound and shows
	 * @return
	 */
	public static boolean tryUse(Context ctx) {
		if (!_enabled) {
			return true;
		}
		if (expired(ctx)) {
			return false;
		}
		else {
			setUseCount(ctx, getUseCount(ctx) + 1);
			return true;
		}
	}
	
	public static boolean expired(Context ctx) {
		if (!_enabled) {
			return false;
		}
		return _maxUse <= getUseCount(ctx);
	}
	
	private static int getUseCount(Context ctx) {
		return SettingsHelper.getInt(ctx, getKey(ctx), 0);
	}
	
	private static void setUseCount(Context ctx, int useCount) {
		SettingsHelper.setInt(ctx, getKey(ctx), useCount);
	}
	
    
    static void setDelayInterval(Context ctx, int delay) {
    	
    }
	
	private static String getKey(Context ctx) {
		String app_version = "";
		if (_resetIfVersionChanged) {
			try
			{
				app_version = ctx.getPackageManager().getPackageInfo(
			    		ctx.getPackageName(), 0).versionName;
			}
			catch (NameNotFoundException e)
			{}			
		}
		return USED_TIMES_KEY + app_version;
	}
}
