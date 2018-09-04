package com.wisesharksoftware.app_photoeditor;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.SettingsHelper;
import com.smsbackupandroid.lib.Utils;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.core.CopyAssetsAsyncTask;
import com.wisesharksoftware.core.opencv.OpenCVLoader;
import com.wisesharksoftware.panels.ThumbnailGenerator;
import com.wisesharksoftware.panels.ThumbnailGenerator.OnThumbnailGenerated;

public class SplashActivity extends com.wisesharksoftware.ui.BaseSplashActivity{
    private Long startTime;
    private Long endTime;

    static {
        if (!OpenCVLoader.initDebug()) {
            Utils.reportFlurryEvent("OpenCVLoader", "ERROR");
        } else {
            Utils.reportFlurryEvent("OpenCVLoader", "OK");
        }
        try {
            System.loadLibrary("processing");
        } catch (Error e) {
            e.printStackTrace();
            new ExceptionHandler(e, "LoadLibrary");
        }
    }

    @Override
    public void onResume() {
//		Utils.reportFlurryEvent("DeviceId",
//				((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
//						.getDeviceId());
        Utils.reportFlurryEvent("onResume", this.toString());
        super.onResume();
    }

    @Override
    public void onPause() {
        Utils.reportFlurryEvent("onPause", this.toString());
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    try {
//                        boolean result = TapjoyConnect.requestTapjoyConnect(getApplicationContext(),
//                                getString(R.string.tapjoyAppId),
//                                getString(R.string.tapjoySecretKey));
//                        Utils.reportFlurryEvent("TapjoyConnect", Boolean.toString(result));
//                        Log.d("TapJoyConnect", "connect result: " + result);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        new ExceptionHandler(e, "TapJoyError");
//                       // FlurryAgent.logEvent("TapJoyError");
//                    }
                }
            }).start();
        } catch (Throwable th) {
            th.printStackTrace();
        }


        File f = getApplicationContext().getExternalFilesDir(null);
        if (f == null) {
           // FlurryAgent.logEvent("ExternalFilesDirNotExist");
        }

        if (isNewVersion() || (getCopyFilesResult() == false)) {
            new CopyAssetsAsyncTask(getApplicationContext()) {
                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                    Log.d("AssetsUtils", "postexecute result = " + result);
                   // FlurryAgent.endTimedEvent("Splash");
//                    Utils.reportFlurryEvent("CopyAssetsResult",
//                            result.toString());
                    setCopyFilesResult(result);
                    if (result) {
                        startTime = System.nanoTime();
                        ThumbnailGenerator tg = new ThumbnailGenerator(
                                getApplicationContext());
                        tg.setOnThumbnailGenerated(new OnThumbnailGenerated() {

                            @Override
                            public void onThumbnailGenerated() {
                                endTime = System.nanoTime();
                                Log.d("TimeProcessing",
                                        "Thumbnail Generator Elapsed time: "
                                                + (endTime - startTime));
                                startMainActivity();
                                finish();
                            }
                        });
                        tg.generate();
                    } else {
                        Toast.makeText(getApplicationContext(), "free disk space, please!", Toast.LENGTH_LONG).show();
                        startMainActivity();
                        finish();
                    }
                }

                ;
            }.execute();
        } else {
           // FlurryAgent.endTimedEvent("Splash");
           // FlurryAgent.logEvent("NoNeedCopyAssets");
            startMainActivity();
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

//        FlurryAgent.onStartSession(getApplicationContext(),
//                getString(R.string.flurryApiKey));
//        MarketingHelper.reportRetantion(this, null);
//		PromoLoader loader = new PromoLoader(SplashActivity.this,
//				"InstaEffects Camera Promo");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
       // FlurryAgent.onEndSession(getApplicationContext());
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.splash_screen;
    }

    @Override
    protected int[] getSplashResources() {
        return splashResources;
    }

    @Override
    protected int getSplashId() {
        return R.id.splash;
    }

    private static final int[] splashResources = {R.drawable.splash2};

    @Override
    protected void startMainActivity() {
        Intent intent = new Intent(SplashActivity.this, getHomeScreenClass(getApplicationContext()));
        startActivity(intent);
    }

    public boolean isNewVersion() {
        String lastVersion = SettingsHelper.getString(this, "last_version", "");
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;
            if (lastVersion.equals(versionName)) {
                return false;
            } else {
                SettingsHelper.setString(this, "last_version", versionName);
                return true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean getCopyFilesResult() {
        SharedPreferences sPref;
        sPref = getSharedPreferences("copy_files", MODE_PRIVATE);
        boolean res = sPref.getBoolean("copy_files_result", true);
        return res;
    }

    public void setCopyFilesResult(boolean result) {
        SharedPreferences sPref;
        sPref = getSharedPreferences("copy_files", MODE_PRIVATE);
        Editor ed = sPref.edit();
        ed.putBoolean("copy_files_result", result);
        ed.commit();
    }

    @Override
    protected int getSplashPromoAppInstalled() {
        return getResources().getIdentifier("splash_promo_app_installed", "drawable", getPackageName());
    }

    @Override
    protected boolean isPromoAppInstalled() {
        if (isPromoAppAlreadyChecked()) {
            return true;
        }
        int id = getResources().getIdentifier("promo_app_packagename", "string", getPackageName());
        if (id == 0) {
            return false;
        } else {
            String packageName = getResources().getString(id);
            if ((packageName == null) || (packageName.equals(""))) {
                return false;
            }
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            boolean res = (intent != null);
            if (res) {
                SharedPreferences sPref;
                sPref = getSharedPreferences("promo_app", MODE_PRIVATE);
                Editor ed = sPref.edit();
                ed.putBoolean("promo_app_installed", true);
                ed.commit();
            }
            return res;
        }
    }

    @Override
    protected boolean isPromoAppAlreadyChecked() {
        SharedPreferences sPref;
        sPref = getSharedPreferences("promo_app", MODE_PRIVATE);
        boolean checked = sPref.getBoolean("promo_app_installed", false);
        return checked;
    }

    public static Class getHomeScreenClass(Context context) {
        String homescreenType = "hdr";
        homescreenType = context.getResources().getString(R.string.homescreen_type);
        if (homescreenType.equals("hdr")) {
            return HomeScreenActivity.class;
        } else {
            return GallerySplashActivity.class;
        }
    }

}
