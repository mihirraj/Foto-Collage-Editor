package com.wisesharksoftware.core.filters;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;

import android.content.Context;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.Utils;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;

public class NormalFilter extends Filter {

	private static final long serialVersionUID = 1L;
	
	public NormalFilter() {
		filterName = FilterFactory.NORMAL_FILTER;
	}
	
	@Override
	protected void onSetParams() {
	}

	@Override
	public boolean hasNativeProcessing() {
		return true;
	}

	@Override
	public boolean processOpenCV(Context context, String srcPath, String outPath) {
		if (!srcPath.equals(outPath)) {
			try {
				Utils.copyFile(new File(srcPath), new File(outPath));
			} catch (IOException e) {
				e.printStackTrace();
				new ExceptionHandler(e, "NormalFilterOpenCV");
			}
		}
		return true;
	}

	@Override
	public String convertToJSON() {
		String s = "{";
    	s += "\"type\":" + "\"" + filterName + "\",";
    	s += "\"params\":" + "[";
    	s += "]";
    	s += "}";
    	return s;
    	/*
		JSONObject json = new JSONObject();
		try {
			json.put("type", filterName);
			JSONArray jsonParams = new JSONArray();
			json.put("params", jsonParams);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
		*/
	}

}
