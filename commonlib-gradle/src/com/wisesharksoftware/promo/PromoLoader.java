package com.wisesharksoftware.promo;

import java.io.File;

import org.json.JSONException;

import com.smsbackupandroid.lib.SettingsHelper;
import com.wisesharksoftware.promo.RequestTask.Response;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class PromoLoader implements Response {
	private static final String LOG_TAG = "PromoLoader";
	public static final String url = "http://wisesharksoftware.com/scripts/promo.php";
	public static final String KEY_IMG_URL = "key img url";
	public static final String KEY_MARKET_URL = "key market url";
	public static final String KEY_LOAD_COMPLETED = "load completed";
	public static final String PROMO_FILE_NAME = "promo_cache.jpg";
	public static final String EVENT_ID = "event id";
	private Context context;
	private String eventId;

	public boolean showContent(){
		if (isReady() && isFileExists()){
			context.startActivity(new Intent(context, WebPromoActivity.class));
			return true;
		}
		return false;
	}
	public boolean isReady(){
		return SettingsHelper.getBoolean(context, PromoLoader.KEY_LOAD_COMPLETED, false);
	}
	
	private boolean isNetworkAvailable(Context context) {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public PromoLoader(Context context, String eventId) {
		this.context = context;
		SettingsHelper.setString(context, EVENT_ID, eventId);
		if (isNetworkAvailable(context)) {
			load();
		}
	}
	
	public void load() {
		//SettingsHelper.setBoolean(context, KEY_LOAD_COMPLETED, false);
		RequestTask task = new RequestTask(this);
		String currentPackage = context.getPackageName();
		task.execute(url, currentPackage);
	}

	@Override
	public void onResponse(String result) {
		if (result == null || result.equals("null")){
			return;
		}
		Log.d(LOG_TAG, "response = " + result);
		// json parsing
		PromoParser parser = new PromoParser();
		PromoEntity entity = null;
		try {
			entity = parser.parse(result);
			Log.d(LOG_TAG, entity.package_ + entity.img_url + entity.market_url);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// check file exists
		try{
			update(entity);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	private void update(PromoEntity entity) throws Exception {
		if (entity == null){
			return;
		}
		String imageUrlStored = SettingsHelper.getString(context, KEY_IMG_URL,
				"");
		
		if (!entity.img_url.equals(imageUrlStored) || !isFileExists()) {
			//updating..
			Log.d(LOG_TAG, "updating..");
			SettingsHelper.setString(context, KEY_IMG_URL, entity.img_url);
			SettingsHelper
					.setString(context, KEY_MARKET_URL, entity.market_url);
			ImageLoader loader = new ImageLoader(context);
			loader.execute(entity.img_url);
		} else{
			Log.d(LOG_TAG, "file exists, img urls are equals");
			SettingsHelper.setBoolean(context, PromoLoader.KEY_LOAD_COMPLETED, true);
		}
		
	}
	
	private boolean isFileExists(){
		// check image exists
				String filepath = new StringBuilder()
						.append(context.getCacheDir().getAbsolutePath()).append("/")
						.append(PROMO_FILE_NAME).toString();
				Log.d(LOG_TAG, "filepath = " + filepath);
				File f = new File(filepath);
				boolean exists = f.exists();
			return exists;
	}
	
}
