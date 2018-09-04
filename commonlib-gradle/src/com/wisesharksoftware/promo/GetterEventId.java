package com.wisesharksoftware.promo;

import com.smsbackupandroid.lib.SettingsHelper;

import android.content.Context;

public class GetterEventId {
	private Context context;
	public GetterEventId(Context context){
		this.context = context;
	}
	public String getValue(){
		return  SettingsHelper.getString(context, PromoLoader.EVENT_ID, null);
	}
}
