package com.wisesharksoftware.util;

import java.util.HashMap;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.Log;

public class TypefaceManager {

	private static HashMap<String, Typeface> typefaceContainer = new HashMap();
	
	public static Typeface createFromAsset(AssetManager assetManager, String path) {
		try {
			Typeface typeface = typefaceContainer.get(path);
			if (typeface != null) {
				return typeface;
			}
			typeface = Typeface.createFromAsset(assetManager, path);
			if (typeface != null) {
				typefaceContainer.put(path, typeface);
			} else {
				Log.e("TypefaceManager", "Error: font '" + path + "' not found!");
			}
			return typeface;
		} catch (Exception e) {
			Log.e("TypefaceManager", "Error: font '" + path + "' error!");
			e.printStackTrace();
			return null;
		}
	}
	
}
