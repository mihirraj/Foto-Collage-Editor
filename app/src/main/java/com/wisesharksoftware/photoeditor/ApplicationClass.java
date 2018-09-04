package com.wisesharksoftware.photoeditor;

import android.app.Application;
import android.util.Log;
import com.batch.android.Batch;
import com.batch.android.Config;

public class ApplicationClass extends Application {
    public static boolean BATCH_ENABLED = false;
    public static boolean AD_FREE_MODE = false;

    @Override
    public void onCreate() {
        super.onCreate();
        AD_FREE_MODE = false;
        try {
            Batch.setConfig(new Config(getString(getResources().getIdentifier("batch_api_id", "string", getPackageName()))));
            BATCH_ENABLED = true;
            Log.d("Batch", "enabled");
        } catch (Exception e) {
           // e.printStackTrace();
            BATCH_ENABLED = false;
            Log.d("Batch", "disabled");
        }
    }
}
