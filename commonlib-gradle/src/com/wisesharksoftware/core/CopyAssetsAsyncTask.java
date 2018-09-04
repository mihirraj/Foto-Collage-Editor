package com.wisesharksoftware.core;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class CopyAssetsAsyncTask extends AsyncTask<Void, Integer, Boolean> {
	Context context_;
	boolean result = true;

	public CopyAssetsAsyncTask(Context context) {
		context_ = context;
	}

	protected Boolean doInBackground(Void... params) {
		return AssetsUtils.copyAssets(context_);		
	}

	protected void onProgressUpdate(Integer... progress) {
		// don't care
	}

	protected void onPostExecute(Boolean result) {
		Log.i("CopyAssetsAsyncTask", "onPostExecute");		
	}
}
