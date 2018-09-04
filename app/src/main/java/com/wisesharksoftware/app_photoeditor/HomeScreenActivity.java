package com.wisesharksoftware.app_photoeditor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.batch.android.*;
import com.jude.rollviewpager.RollPagerView;
import com.jude.rollviewpager.adapter.LoopPagerAdapter;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;
import com.wisesharksoftware.photoeditor.ApplicationClass;

import net.hockeyapp.android.CrashManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.nineoldandroids.animation.ObjectAnimator;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.MarketingHelper;
import com.wisesharksoftware.camera.AppSettings;
import com.wisesharksoftware.core.Utils;
import com.photostudio.photoeditior.R;
//import com.wisesharksoftware.realtime.RealTimeCameraPreview;
import com.wisesharksoftware.util.TypefaceManager;
import com.wisesharksoftware.gallery.BaseGallerySplashScreen;

public class HomeScreenActivity extends BaseGallerySplashScreen implements BatchUnlockListener {
    private static Uri outputFileUri = null;
    private List<Uri> selectedImages = new ArrayList<Uri>();
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int touchPos = 5;

    private ViewFlipper mViewFlipper;
    private ViewPager moreAppsPanel;
    private RelativeLayout full_screen;
    private final GestureDetector detector = new GestureDetector(new SwipeGestureDetector());
    private Context mContext;

    RollPagerView mRollViewPager;
    private ImageButton BtnTakePhoto;
    private ImageButton BtnShareApp;
    private ImageButton BtnTakePhotonew;
    private ImageButton BtnRateUs;
    private ImageButton BtnMoreApps;
    private ImageButton btnLoadFromGallery;
    private ImageButton btnLoadFromGallerynew;
    private ImageButton btnApp;
    private ImageButton btnMore;
    private ImageButton btnSettings;
    private CheckBox btnSettingsHd;
    private CheckBox btnSettingsSave;
    private CheckBox btnSettingsSquare;
    private boolean moreAppsOpenned = false;
    private boolean settingsOpenned = false;
    private int settingsDiv = 0;
    private View hidden_panel;
    private View settingsPanel;
    private ImageView imgTouchScreen;
    private ImagePagerAdapter appsAdapter;
    private StartAppAd startAppAd = new StartAppAd(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartAppSDK.init(this, "110725497", "210296089");
        full_screen = (RelativeLayout) findViewById(R.id.full_screen);
        hidden_panel = findViewById(R.id.hidden_panel);
        settingsPanel = findViewById(R.id.settingsPanel);
        BtnTakePhoto = (ImageButton) findViewById(R.id.BtnTakePhoto);
        BtnTakePhotonew = (ImageButton) findViewById(R.id.new_shoot);
        BtnShareApp = (ImageButton) findViewById(R.id.btn_share_app);
        BtnRateUs = (ImageButton) findViewById(R.id.btn_rateus);
        btnLoadFromGallery = (ImageButton) findViewById(R.id.btnLoadFromGallery);
        btnLoadFromGallerynew = (ImageButton) findViewById(R.id.new_gallery);
        btnApp = (ImageButton) findViewById(R.id.btnApp);
        BtnMoreApps = (ImageButton) findViewById(R.id.btnMore_apps);
        btnMore = (ImageButton) findViewById(R.id.btnMore);
        btnSettings = (ImageButton) findViewById(R.id.btnSettings);
        btnSettingsHd = (CheckBox) findViewById(R.id.btnSettingsHd);
        btnSettingsSave = (CheckBox) findViewById(R.id.btnSettingsSave);
        btnSettingsSquare = (CheckBox) findViewById(R.id.btnSettingsSquare);
        imgTouchScreen = (ImageView) findViewById(R.id.imgTouchScreen);
        mRollViewPager = (RollPagerView) findViewById(R.id.viewpager_new);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        settingsDiv = 3 * metrics.widthPixels / 4;
        startAppAd.showAd();
        startAppAd.loadAd();
        btnApp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String homescreen_app_link = getResources().getString(R.string.homescreen_app_link);
                intent.setData(Uri.parse(homescreen_app_link));
                startActivity(intent);
            }
        });
        BtnShareApp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                shareApp();
            }
        });
        BtnRateUs.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });

        mRollViewPager.setAdapter(new TestLoopAdapter(mRollViewPager));
        //appear animation
        Animation anim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, 0,
                TranslateAnimation.RELATIVE_TO_PARENT, 0,
                TranslateAnimation.RELATIVE_TO_PARENT, 1.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
        anim.setDuration(500);
        anim.setFillAfter(true);
        Animation anim2 = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, 0,
                TranslateAnimation.RELATIVE_TO_PARENT, 0,
                TranslateAnimation.RELATIVE_TO_PARENT, 1.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
        anim2.setDuration(500);
        anim2.setStartOffset(500);
        anim2.setFillAfter(true);
        Animation anim3 = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, 0,
                TranslateAnimation.RELATIVE_TO_PARENT, 0,
                TranslateAnimation.RELATIVE_TO_PARENT, 1.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
        anim3.setDuration(500);
        anim3.setStartOffset(1000);
        anim3.setFillAfter(true);

        Animation anim4 = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, 0,
                TranslateAnimation.RELATIVE_TO_PARENT, 0,
                TranslateAnimation.RELATIVE_TO_SELF, -2.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
        anim4.setDuration(300);
        anim4.setStartOffset(1500);
        anim4.setFillAfter(true);

        BtnTakePhotonew.startAnimation(anim);
        btnLoadFromGallerynew.startAnimation(anim2);
        BtnShareApp.startAnimation(anim3);
        BtnRateUs.startAnimation(anim3);
        btnMore.startAnimation(anim3);
        btnApp.startAnimation(anim3);
        BtnMoreApps.startAnimation(anim3);
        btnSettings.startAnimation(anim4);

        btnMore.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (moreAppsOpenned) {
                    ObjectAnimator obj = ObjectAnimator.ofFloat(full_screen, "translationY", 0);
                    obj.setInterpolator(new OvershootInterpolator(0.9f));
                    obj.start();
                    moreAppsOpenned = false;
                    imgTouchScreen.setVisibility(View.INVISIBLE);
                } else {
                    ObjectAnimator obj = ObjectAnimator.ofFloat(full_screen, "translationY", -moreAppsPanel.getHeight());
                    obj.setInterpolator(new OvershootInterpolator(0.9f));
                    obj.start();
                    imgTouchScreen.setVisibility(View.VISIBLE);
                    hidden_panel.setVisibility(View.VISIBLE);
                    moreAppsOpenned = true;
                }
            }
        });

        imgTouchScreen.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                moreAppsPanel = (ViewPager) findViewById(R.id.moreAppsPanel);
                try {
                    if (moreAppsOpenned) {
                        int dh = moreAppsPanel.getHeight();

                        int x1 = BtnTakePhoto.getLeft();
                        int y1 = BtnTakePhoto.getTop();
                        int x2 = BtnTakePhoto.getLeft() + BtnTakePhoto.getWidth();
                        int y2 = BtnTakePhoto.getTop() + BtnTakePhoto.getHeight();

                        boolean clicked = false;

                        if ((!clicked) && (event.getX() > x1) && (event.getY() + dh > y1) && (event.getX() < x2) && (event.getY() + dh < y2)) {
                            event.setLocation(touchPos, touchPos);
                            BtnTakePhoto.onTouchEvent(event);
                            clicked = true;
                        }

                        x1 = btnLoadFromGallery.getLeft();
                        y1 = btnLoadFromGallery.getTop();
                        x2 = btnLoadFromGallery.getLeft() + btnLoadFromGallery.getWidth();
                        y2 = btnLoadFromGallery.getTop() + btnLoadFromGallery.getHeight();

                        if ((!clicked) && (event.getX() > x1) && (event.getY() + dh > y1) && (event.getX() < x2) && (event.getY() + dh < y2)) {
                            event.setLocation(touchPos, touchPos);
                            btnLoadFromGallery.onTouchEvent(event);
                            clicked = true;
                        }

                        View appMoreGroup = findViewById(R.id.appMoreGroup);
                        x1 = appMoreGroup.getLeft() + btnApp.getLeft();
                        y1 = appMoreGroup.getTop() + btnApp.getTop();
                        x2 = appMoreGroup.getLeft() + btnApp.getLeft() + btnApp.getWidth();
                        y2 = appMoreGroup.getTop() + btnApp.getTop() + btnApp.getHeight();

                        if ((!clicked) && (event.getX() > x1) && (event.getY() + dh > y1) && (event.getX() < x2) && (event.getY() + dh < y2)) {
                            event.setLocation(touchPos, touchPos);
                            btnApp.onTouchEvent(event);
                            clicked = true;
                        }

                        x1 = appMoreGroup.getLeft() + btnMore.getLeft();
                        y1 = appMoreGroup.getTop() + btnMore.getTop();
                        x2 = appMoreGroup.getLeft() + btnMore.getLeft() + btnMore.getWidth();
                        y2 = appMoreGroup.getTop() + btnMore.getTop() + btnMore.getHeight();

                        if ((!clicked) && (event.getX() > x1) && (event.getY() + dh > y1) && (event.getX() < x2) && (event.getY() + dh < y2)) {
                            event.setLocation(touchPos, touchPos);
                            btnMore.onTouchEvent(event);
                            clicked = true;
                        }

                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);

                        if ((!clicked) && (event.getY() > metrics.heightPixels - dh) && (event.getY() < metrics.heightPixels)) {
                            event.setLocation(event.getX(), event.getY() - moreAppsPanel.getTop());
                            moreAppsPanel.dispatchTouchEvent(event);
                            clicked = true;
                        }
                    } else if (settingsOpenned) {

                        int x1 = btnSettings.getLeft();
                        int y1 = btnSettings.getTop();
                        int x2 = btnSettings.getLeft() + btnSettings.getWidth();
                        int y2 = btnSettings.getTop() + btnSettings.getHeight();

                        boolean clicked = false;

                        if ((!clicked) && (event.getX() + settingsDiv > x1) && (event.getY() > y1) && (event.getX() + settingsDiv < x2) && (event.getY() < y2)) {
                            event.setLocation(touchPos, touchPos);
                            btnSettings.onTouchEvent(event);
                            clicked = true;
                        }

                        if (!clicked) {
                            settingsPanel.dispatchTouchEvent(event);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return false;
            }
        });

        mContext = this;

        btnSettings.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (settingsOpenned) {
                    ObjectAnimator obj = ObjectAnimator.ofFloat(full_screen, "translationX", 0);
                    obj.setInterpolator(new OvershootInterpolator(0.9f));
                    obj.start();

                    if (btnSettingsHd.isChecked()) {
                        AppSettings.setResolution(getApplicationContext(), AppSettings.RES_HIGH);
                    } else {
                        AppSettings.setResolution(getApplicationContext(), AppSettings.RES_NORMAL);
                    }
                    AppSettings.setSaveOriginal(getApplicationContext(), btnSettingsSave.isChecked());
                    AppSettings.setDoSquarePhoto(getApplicationContext(), btnSettingsSquare.isChecked());

                    settingsOpenned = false;
                    imgTouchScreen.setVisibility(View.INVISIBLE);
                } else {
                    hidden_panel.setVisibility(View.INVISIBLE);
                    imgTouchScreen.setVisibility(View.VISIBLE);

                    if ((AppSettings.getResolution(getApplicationContext()) == AppSettings.RES_LOW) ||
                            (AppSettings.getResolution(getApplicationContext()) == AppSettings.RES_NORMAL)) {
                        btnSettingsHd.setChecked(false);
                    } else {
                        btnSettingsHd.setChecked(true);
                    }
                    btnSettingsSave.setChecked(AppSettings.isSaveOriginal(getApplicationContext()));
                    btnSettingsSquare.setChecked(AppSettings.isDoSquarePhoto(getApplicationContext()));

                    ObjectAnimator obj = ObjectAnimator.ofFloat(full_screen, "translationX", -settingsDiv);
                    obj.setInterpolator(new OvershootInterpolator(0.9f));
                    obj.start();
                    settingsOpenned = true;
                }
            }
        });

        ImageButton btnLoadFromGallery = (ImageButton) findViewById(R.id.btnLoadFromGallery);
        ImageButton btnLoadFromGallerynew = (ImageButton) findViewById(R.id.new_gallery);
        btnLoadFromGallerynew.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });

        mViewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        mViewFlipper.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                detector.onTouchEvent(event);
                return true;
            }
        });
        moreAppsPanel = (ViewPager) findViewById(R.id.moreAppsPanel);
        appsAdapter = new ImagePagerAdapter();
        moreAppsPanel.setAdapter(appsAdapter);
    }

    @Override
    public void onRedeemAutomaticOffer(Offer offer) {
        Log.d("Batch", "onRedeemAutomaticOffer");
        for (Feature feature : offer.getFeatures()) {
            String featureRef = feature.getReference();
            String value = feature.getValue();
            if (featureRef.equals("AD_FREE_MODE")) {
                hideAds();
                showAppGratisSplash(3000);
            }
            Log.d("Batch", featureRef + " :: " + value);
        }
    }

    public void showAppGratisSplash(long time) {
        try {
            int id = getResources().getIdentifier("appGratisSplash", "id", getPackageName());
            final ImageView appGratisSlpash = (ImageView) findViewById(id);
            appGratisSlpash.setVisibility(View.VISIBLE);
            appGratisSlpash.postDelayed(new Runnable() {
                @Override
                public void run() {
                    appGratisSlpash.setVisibility(View.GONE);
                }
            }, time);
        } catch (Exception e) {
            //nop
        }
    }

    @Override
    protected void onStart() {
        ApplicationClass.AD_FREE_MODE = false;
        if (ApplicationClass.BATCH_ENABLED) {
            Log.d("Batch", "onStart");
            Batch.onStart(this);
            Batch.Unlock.setUnlockListener(this);
            Batch.Unlock.restore(new BatchRestoreListener() {

                @Override
                public void onRestoreSucceed(List<Feature> features) {
                    for (int i = 0; i < features.size(); i++) {
                        Log.d("Batch", "feature: " + features.get(i).getReference());
                        if (features.get(i).getReference().equals("AD_FREE_MODE")) {
                            ApplicationClass.AD_FREE_MODE = true;
                            //showAppGratisSplash(3000);
                        }
                    }
                }

                @Override
                public void onRestoreFailed(FailReason reason) {
                    // Hide the wait UI
                    // Show a message error to the user using the reason
                }
            });
        }
        View view = findViewById(R.id.adView);
        if (view != null && IsAdsHidden()) {
            view.setVisibility(View.GONE);
        }
        super.onStart();
    }

    private void selectPhoto() {
        try {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");

            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
        } catch (Exception e) {
            e.printStackTrace();
            new ExceptionHandler(e, "selectPhoto");
        }
    }

    @Override
    public void onResume() {
        //Utils.reportFlurryEvent("DeviceId", ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId());
        //Utils.reportFlurryEvent("onResume", this.toString());

        super.onResume();
        mRollViewPager.setAdapter(new TestLoopAdapter(mRollViewPager));
        try {
            String hockeyAppId = getString(R.string.hockeyAppId);
            if (hockeyAppId != null && hockeyAppId.length() > 0) {
                CrashManager.register(this, hockeyAppId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        //Utils.reportFlurryEvent("onPause", this.toString());
        super.onPause();
    }

    @Override
    public void onTakePhotoClick(View v) {
        super.onTakePhotoClick(v);
        //if (getString(R.string.workflow).contains("realtime")) {
        //startActivity(new Intent(this, RealTimeCameraPreview.class));
        //} else {
        startActivity(new Intent(this, CameraPreviewActivity.class));
        //}
        finish();
    }

    public void onDownloadClick(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String downloadLink = getString(R.string.full_app_link);
        if (downloadLink != null && downloadLink.length() > 0) {
            intent.setData(Uri.parse(downloadLink));
            startActivity(intent);
        }
    }

    @Override
    public int getRootLayout() {
        return R.layout.activity_home_screen;
    }

    @Override
    public String getPath() {
        return Utils.getFolderPath(getApplicationContext().getString(
                R.string.photoFolder));
    }

    @Override
    public boolean isShowingAds() {
        return false;
    }

    @Override
    protected void startNextActivity() {
        if (getResources().getString(R.string.workflow).equals("animaleyes")) {
            Intent intent = new Intent(getApplicationContext(), CropActivity.class);
            intent.putExtra(ChooseProcessingActivity.INTENT_PARAM_URIS, selectedImages.toArray(new Uri[selectedImages.size()]));
            startActivity(intent);
            selectedImages.clear();
        } else {
            Intent intent = new Intent(getApplicationContext(), ChooseProcessingActivity.class);
            intent.putExtra(ChooseProcessingActivity.INTENT_PARAM_URIS, selectedImages.toArray(new Uri[selectedImages.size()]));
            startActivity(intent);
            selectedImages.clear();
        }
        if (getResources().getString(R.string.workflow).equals("powercam")) {

        } else {
            finish();
        }
    }

    @Override
    protected void addSelectedImage(Uri uri) {
        selectedImages.add(uri);
    }

    @Override
    protected void showProgressBar() {
        findViewById(R.id.imgLockScreen).setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideProgressBar() {
        findViewById(R.id.imgLockScreen).setVisibility(View.INVISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
    }

    @Override
    protected View getTakeFromGalleryButton() {
        View v = findViewById(R.id.BtnGallerySplash);
        if (v.getVisibility() == View.GONE) {
            return null;
        } else {
            return v;
        }
    }

    public boolean IsAdsHidden() {
        if (getResources().getBoolean(R.bool.show_ads) == false) {
            return true;
        }
        return isFullVersion()
                || ChooseProcessingActivity.isItemPurchased(this,
                ChooseProcessingActivity.REMOVE_ADS_PUCHASE_ID)
                || MarketingHelper.isTrialPeriod(this) || isPromoAppInstalled()
                || isFacebookPosted() || (getProductIds().size() > 0);
    }

    protected boolean isFullVersion() {
        return getPackageName().contains("full");
    }

    public boolean isFacebookPosted() {
        SharedPreferences sPref;
        sPref = getSharedPreferences("facebook", MODE_PRIVATE);
        boolean posted = sPref.getBoolean("facebook_posted", false);
        Log.d("facebook", "facebook_posted = " + posted);
        return posted;
    }

    public List<String> getProductIds() {
        SharedPreferences sPref;
        sPref = getSharedPreferences("productIds", MODE_PRIVATE);
        List<String> productIds = new ArrayList<String>();
        int count = sPref.getInt("ProductIdsCount", 0);
        for (int i = 0; i < count; i++) {
            String productId = sPref.getString("productId" + i, "");
            productIds.add(productId);
        }
        return productIds;
    }

    public boolean isPromoAppInstalled() {
        if (isPromoAppAlreadyChecked()) {
            return true;
        }
        int id = getResources().getIdentifier("promo_app_packagename", "string", getPackageName());
        if (id == 0) {
            return false;
        } else {
            String packageName = getResources().getString(id);
            //String packageName = "com.onemanwithcameralomo";
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

    public boolean isPromoAppAlreadyChecked() {
        SharedPreferences sPref;
        sPref = getSharedPreferences("promo_app", MODE_PRIVATE);
        boolean checked = sPref.getBoolean("promo_app_installed", false);
        return checked;
    }

    class SwipeGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    mViewFlipper.stopFlipping();

                    int leftin = getResources().getIdentifier("leftin", "anim", getPackageName());
                    int leftout = getResources().getIdentifier("leftout", "anim", getPackageName());
                    if (leftin != 0) {
                        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, leftin));
                    }
                    if (leftout != 0) {
                        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext, leftout));
                    }
                    mViewFlipper.showNext();
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    mViewFlipper.stopFlipping();
                    int rightin = getResources().getIdentifier("rightin", "anim", getPackageName());
                    int rightout = getResources().getIdentifier("rightout", "anim", getPackageName());
                    if (rightin != 0) {
                        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, rightin));
                    }
                    if (rightout != 0) {
                        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, rightout));
                    }
                    mViewFlipper.showPrevious();
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }
    }

    @Override
    protected void onStop() {
        if (ApplicationClass.BATCH_ENABLED) {
            Batch.onStop(this);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (ApplicationClass.BATCH_ENABLED) {
            Batch.onDestroy(this);
        }
        super.onDestroy();
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private int[] images;
        private String[] captions;
        public String[] links;
        private LayoutInflater inflater;

        ImagePagerAdapter() {
            inflater = getLayoutInflater();
            String[] homescreenAppImages = getResources().getStringArray(R.array.homescreen_app_images);
            images = new int[homescreenAppImages.length];
            for (int i = 0; i < homescreenAppImages.length; i++) {
                int id = getResources().getIdentifier(homescreenAppImages[i], "drawable", getPackageName());
                images[i] = id;
            }
            captions = getResources().getStringArray(R.array.homescreen_app_captions);
            links = getResources().getStringArray(R.array.homescreen_app_links);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public Object instantiateItem(ViewGroup view, final int position) {
            View imageLayout = inflater.inflate(R.layout.item_more_apps_pager_image, view, false);
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
            TextView textView = (TextView) imageLayout.findViewById(R.id.text);
            textView.setText(captions[position]);
            Typeface myTypeface = TypefaceManager.createFromAsset(getAssets(),
                    "fonts/intro_black_caps.ttf");
            textView.setTypeface(myTypeface);
            imageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Utils.reportFlurryEvent("moreAppsClick", links[position]);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(links[position]));
                    startActivity(intent);
                }
            });

            imageView.setImageResource(images[position]);
            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }
    }
    @Override
    public void onBackPressed() {

       startAppAd.onBackPressed();

        super.onBackPressed();
    }
    private class TestLoopAdapter extends LoopPagerAdapter {
        private int[] imgs = {
                R.drawable.img1,
                R.drawable.img2,
                R.drawable.img3,
        };

        public TestLoopAdapter(RollPagerView viewPager) {
            super(viewPager);
        }

        @Override
        public View getView(ViewGroup container, int position) {
            ImageView view = new ImageView(container.getContext());
            view.setImageResource(imgs[position]);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return view;
        }

        @Override
        public int getRealCount() {
            return imgs.length;
        }
    }
    private void shareApp() {
        Intent share = new Intent(Intent.ACTION_SEND);
        final String appPackageName = getPackageName();
        // If you want to share a png image only, you can do:
        // setType("image/png"); OR for jpeg: setType("image/jpeg");
        share.setType("text/plain");

        // Make sure you put example png image named myImage.png in your
        // directory

        share.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + appPackageName);

        startActivity(Intent.createChooser(share, "Share App!"));
    }

}
