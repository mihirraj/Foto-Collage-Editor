package com.wisesharksoftware.app_photoeditor;

import com.batch.android.*;
import com.google.android.gms.ads.InterstitialAd;
import com.startapp.android.publish.StartAppAd;
import com.wisesharksoftware.photoeditor.ApplicationClass;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aviary.android.feather.library.services.drag.DragView;
import com.aviary.android.feather.library.utils.BitmapUtils;
//import com.chartboost.sdk.Chartboost;
//import com.facebook.FacebookRequestError;
//import com.facebook.HttpMethod;
//import com.facebook.Request;
//import com.facebook.Response;
//import com.facebook.Session;
//import com.facebook.SessionState;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.MarketingHelper;
import com.wisesharksoftware.ad.Banner;
import com.wisesharksoftware.billing.BillingActivity;
import com.wisesharksoftware.camera.AppSettings;
import com.wisesharksoftware.core.ActionCallback;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Image2;
import com.wisesharksoftware.core.ImageProcessing;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Presets;
import com.wisesharksoftware.core.ProcessingCallback;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.BlendFilter;
import com.wisesharksoftware.core.filters.CropFilter;
import com.wisesharksoftware.core.filters.FlipRotateFilter;
import com.wisesharksoftware.core.filters.FocusFilter;
import com.wisesharksoftware.core.filters.PerspectiveTransformFilter;
import com.wisesharksoftware.core.filters.BlendFilter.Algorithm;
import com.wisesharksoftware.panels.ButtonPanel;
import com.wisesharksoftware.panels.ButtonPanel.OnLaunchPanelListener;
import com.wisesharksoftware.panels.IPanel;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.LauncherPanel;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.SliderPanel;
import com.wisesharksoftware.panels.TurboScanPanel;
import com.wisesharksoftware.panels.TurboScanPanel.OnActionListener;
import com.wisesharksoftware.panels.fragment.grid.GridPagerPanel;
import com.wisesharksoftware.panels.fragment.grid.GridPagerPanel.OnGridItemClickListener;
import com.wisesharksoftware.panels.okcancel.IOkCancel;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.panels.okcancel.OKCancelPanel;
import com.wisesharksoftware.panels.okcancel.SlidersBarsPanel;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.promo.PromoLoader;
//import com.wisesharksoftware.realtime.RealTimeCameraPreview;
import com.wisesharksoftware.service.CombinePhotosService;
import com.wisesharksoftware.service.base.IService;
import com.wisesharksoftware.service.base.ServicesManager;
import com.wisesharksoftware.sticker.CropImageView;
import com.wisesharksoftware.sticker.DragControllerService.DragSource;
import com.wisesharksoftware.sticker.DrawableHighlightView;
import com.wisesharksoftware.sticker.DropTarget.DropTargetListener;
import com.wisesharksoftware.sticker.FocusImageView;
import com.wisesharksoftware.sticker.ImageViewDrawableOverlay;
import com.wisesharksoftware.util.TypefaceManager;

public class ChooseProcessingActivity extends BillingActivity implements DropTargetListener {
    public static final int BUY_DIALOG_CODE = 1;
    public static Class returnToCameraClass = null;
    private static final String PACK_ALL_ID = "pack_all";

    //private Chartboost cb = null;

    private static final Integer[] procIds = null;

    private Preset cameraPreset;
    public Preset[] processingPresets;

    private List<String> originalFileNames;
    private List<Boolean> originalFileTypes;
    private String resultFileName;
    // private String oldProcessedFile;
    private String lastHDFileName;
    private ActionCallback<String> lastOnSuccessCallback;

    private int currentSelection;
    private int lastSavedSelection;
    private String documentPath;
    // private android.widget.Gallery procImageGallery;

    private ProgressDialog progressBar;
    private ProgressDialog saveProgressBar;
    private ProgressTask progressTask;

    private PanelManager manager;
    private View topPanel;

    private boolean firstAdRecieved = false;

    private static MarketingHelper marketingHelper;

    public static final String INTENT_PARAM_URIS = "uris";
    public static final String INTENT_PARAM_START_FROM_CAMERA = "start_from_camera";
    public static final String INTENT_PARAM_ANGLE = "angle";
    public static final String INTENT_PARAM_POINTS = "points";
    private boolean returnToCameraActivity = false;

    private boolean isFirstTime = true;
    public static final String REMOVE_ADS_PUCHASE_ID = "remove_ads";
    private String tempFileName = "";
    private Banner banner;

    public boolean processPreview = false;

    private String choosedFilterPreset = "";
    public ProcessOrder processOrder = new ProcessOrder();

    private final boolean new_values = false;
    private boolean sliderLocked = false;
    private boolean showBuyDialogFromSavePhoto = false;

    private CropImageView mCropImageView;
    private FocusImageView mFocusImageView;
    private ImageView backgroundImage;
    private CropFilter cropFilter;
    private FocusFilter focusFilter;
    private ImageView imgLockScreen;
    private ImageView imgFullLockScreen;
    private ProgressBar progressBarProcessing;
    private ProgressBar progressBarHorProcessing;
    private LinearLayout shareMenu;
    private boolean progressAnimEnded = false;
    private boolean showProgressTail = false;
    // private boolean flipVertical = false;
    // private boolean flipHorizontal = false;
    // private int angle = 0;
    private boolean first_launch = true;

    private boolean restore_launch = false;
    private Long startTime;
    private Long endTime;
    private ImageView imgShowOriginal;
    private ImageView imgRevert;
    List<String> stickerProductIds = new ArrayList<String>();
    List<String> cropProductIds = new ArrayList<String>();
    List<String> processedPathes;
    private ImageViewTouch mImageView;
    public Bitmap mBitmap;
    private Bitmap mBlurBitmap;
    public Bitmap mPreview;
    public Canvas mCanvas;
    DrawableHighlightView hvRestoreOriginal = null;
    private static final String AD_UNIT_ID_INSERTIAL =
            "ca-app-pub-2166241959146947/7654305314";
    private static final String AD_UNIT_ID_INSERTIAL_BACKPRESS =
            "ca-app-pub-6538616456653763/2335618733";
    private InterstitialAd interstitial;
    private PerspectiveTransformFilter perspectiveFilter;

    private boolean toastVisible = false;

    private ImageProcessing currentProcessing = null;
    private ImageProcessing nextProcessing = null;
    private StartAppAd startAppAd = new StartAppAd(this);


    private String combineImagePath = "";

    @Override
    public void onResume() {
        //Utils.reportFlurryEvent("DeviceId",
        //		((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
        //				.getDeviceId());
        //Utils.reportFlurryEvent("onResume", this.toString());

        List<String> temp = new ArrayList<String>();
        for (String s : originalFileNames) {
            File file = new File(s);
            if (file.exists())
                temp.add(s);
        }
        originalFileNames = temp;

        if (originalFileNames.size() == 0) {
            FlurryAgent
                    .logEvent("onResume: Cancel process originalfile not exist");
            if (returnToCameraActivity) {
                Intent intent = new Intent(this, CameraPreviewActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, SplashActivity.getHomeScreenClass(getApplicationContext()));
                startActivity(intent);
            }
            finish();
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        //Utils.reportFlurryEvent("onPause", this.toString());
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // generate hash key for facebook
        // PackageInfo info;
        // try {
        // info =
        // getPackageManager().getPackageInfo("com.wisesharksoftware.animalfaces",
        // PackageManager.GET_SIGNATURES);
        // for (Signature signature : info.signatures) {
        // MessageDigest md;
        // md = MessageDigest.getInstance("SHA");
        // md.update(signature.toByteArray());
        // String something = new String(Base64.encode(md.digest(), 0));
        // Log.e("hash key", something);
        // }
        // } catch (NameNotFoundException e1) {
        // Log.e("name not found", e1.toString());
        // } catch (NoSuchAlgorithmException e) {
        // Log.e("no such an algorithm", e.toString());
        // } catch (Exception e) {
        // Log.e("exception", e.toString());
        // }

        // oldProcessedFile = null;
        lastSavedSelection = -1;
        lastHDFileName = null;
        saveProgressBar = null;
        Intent intent = getIntent();
        Parcelable[] imgUris = intent
                .getParcelableArrayExtra(INTENT_PARAM_URIS);
        // selectedProcessingIdx = getLastUsedProcessing();
        try {
            cameraPreset = Presets.getPresets(this).getCameraPresets()[0];
        } catch (Exception e) {

        }
        processingPresets = Presets.getPresets(this).getProcessingPresets();

        startAppAd.loadAd();
        File file = new File(getExternalFilesDir(null) + "/assets/"
                + "eyes_position.txt");
        file.delete();
        file = new File(getExternalFilesDir(null) + "/assets/"
                + "eyes_position_preview.txt");
        file.delete();

        originalFileNames = new ArrayList<String>();
        originalFileTypes = new ArrayList<Boolean>();
        if (imgUris != null) {
            String[] proj = {MediaStore.Images.Media.DATA};
            for (Parcelable uri : imgUris) {
                Cursor cursor = managedQuery((Uri) uri, proj, null, null, null);
                if (cursor != null) {
                    int column_index = cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    originalFileNames.add(cursor.getString(column_index));
                    originalFileTypes.add(false);
                } else {
                    originalFileNames.add(((Uri) uri).getPath());
                    originalFileTypes.add(true);
                }
            }
        }
        documentPath = intent
                .getStringExtra(EditPagesActivity.INTENT_PARAM_DOCUMENT_PATH);
        if (intent.getFloatExtra(INTENT_PARAM_ANGLE, -1) != -1) {
            float angle = getIntent().getFloatExtra(INTENT_PARAM_ANGLE, -1);
            int i = (int) angle / 90;
            for (int j = 0; j < i; j++) {

                processOrder.addRotateRight();
            }

            perspectiveFilter = new PerspectiveTransformFilter();
            float[] points = getIntent()
                    .getFloatArrayExtra(INTENT_PARAM_POINTS);
            perspectiveFilter.setCorners(points[0], points[1], points[2],
                    points[3], points[4], points[5], points[6], points[7]);
            perspectiveFilter.setOutFileName(originalFileNames.get(0));
        }
        returnToCameraActivity = intent.getBooleanExtra(
                INTENT_PARAM_START_FROM_CAMERA, false);

        imgRevert = (ImageView) findViewById(R.id.imgRevert);
        imgRevert.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ServicesManager.clearServices();

                cropFilter = null;
                focusFilter = null;
                processOrder = new ProcessOrder();
                ((ImageViewDrawableOverlay) mImageView).clearOverlays();
                for (int i = 0; i < manager.panels.size(); i++) {
                    IPanel panel = manager.panels.get(i);
                    panel.restoreOriginal();
                }

                if (manager.getCurrPanel().getPanelType()
                        .equals(PanelManager.OK_CANCEL_PANEL_TYPE)
                        && manager.getCurrPanel().getPanelName()
                        .equals("OKCancel")) {
                    manager.upLevel();
                }

                if (manager.getCurrPanel().getPanelType()
                        .equals(PanelManager.OK_CANCEL_PANEL_TYPE)
                        && manager.getCurrPanel().getPanelName()
                        .equals("LockedOKCancel")) {
                    manager.upLevel();
                }

                if (manager.getCurrPanel().getPanelType()
                        .equals(PanelManager.OK_CANCEL_PANEL_TYPE)
                        && manager.getCurrPanel().getPanelName()
                        .equals("OKCancelBars")) {
                    manager.upLevel();
                }

                if (manager.getCurrPanel().getPanelType()
                        .equals(PanelManager.SLIDER_PANEL_TYPE)
                        && manager.getCurrPanel().getPanelName()
                        .equals("Effect_Alpha")) {
                    manager.upLevel();
                }

                if (manager.getCurrPanel().getPanelType()
                        .equals(PanelManager.SLIDER_PANEL_TYPE)
                        && manager.getCurrPanel().getPanelName()
                        .equals("Modes_Alpha")) {
                    manager.upLevel();
                }

                processImage();
            }
        });

        imgShowOriginal = (ImageView) findViewById(R.id.imgShowOriginal);

        processedPathes = null;


        /*interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(AD_UNIT_ID_INSERTIAL);

        // Create ad request.
        com.google.android.gms.ads.AdRequest adRequestinsertial = new
                com.google.android.gms.ads.AdRequest.Builder()
                .build();

        // Begin loading your interstitial.
        interstitial.loadAd(adRequestinsertial);
        interstitial.setAdListener(new AdListener() {
            public void onAdLoaded() {
                if (interstitial.isLoaded()) {

                    interstitial.show();

                }

            }

        });*/
        imgShowOriginal.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    imgShowOriginal.setImageDrawable(getResources()
                            .getDrawable(R.drawable.original_on));
                    hvRestoreOriginal = ((ImageViewDrawableOverlay) mImageView)
                            .getHighlightViewAt(0);
                    if (hvRestoreOriginal != null) {
                        ((ImageViewDrawableOverlay) mImageView)
                                .removeAndReturnHightlightView(hvRestoreOriginal);

                    }
                    createAndConfigurePreview(originalFileNames.get(0), false);
                }
                if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                    imgShowOriginal.setImageDrawable(getResources()
                            .getDrawable(R.drawable.original_off));
                    createAndConfigurePreview(resultFileName, false);
                    if (hvRestoreOriginal != null) {
                        ((ImageViewDrawableOverlay) mImageView)
                                .addHighlightView(hvRestoreOriginal);
                    }
                }
                return true;
            }
        });

        topPanel = findViewById(R.id.proc_header);

        manager = (PanelManager) findViewById(R.id.categoryPanel);
        //Log.d("AAA", "check facebookPosted");
        if (isFacebookPosted()) {
            manager.unlockAll();
            Log.d("unlock", "facebook posted: unlockAll()");
        }
        if (MarketingHelper.isTrialPeriod(this)) {
            manager.unlockAll();
            Log.d("unlock", "bastion enabled: unlockAll()");
        }
        if (isPromoAppInstalled()) {
            manager.unlockAll();
            Log.d("unlock", "promoAppInstalled: unlockAll()");
        }
        if (isFullVersion()) {
            manager.unlockAll();
            Log.d("unlock", "full version: unlockAll()");
        }
        if (isSamsungStoreVersion()) {
            manager.unlockAll();
            Log.d("unlock", "samsung version: unlockAll()");
        }
        if (isAmazonStoreVersion()) {
            manager.unlockAll();
            Log.d("unlock", "amazon version: unlockAll()");
        }

        if (isItemPurchased(this, getRemoveAdsPurchaseId())) {
            int id = getResources().getIdentifier("unlock_pack_if_ads_removed", "string", getPackageName());
            String unlockedProduct = "pack1";
            if (id != 0) {
                unlockedProduct = getResources().getString(id);
            }
            manager.unlockByProductId(unlockedProduct);

            Log.d("unlock", "remove ads purchased: unlock " + unlockedProduct);
        }

        List<String> productIds = getProductIds();
        for (int i = 0; i < productIds.size(); i++) {
            manager.unlockByProductId(productIds.get(i));
        }

        mImageView = (ImageViewDrawableOverlay) findViewById(R.id.image_overlay);
        mImageView.setDoubleTapEnabled(false);

        mCropImageView = (CropImageView) findViewById(R.id.crop_image);
        mFocusImageView = (FocusImageView) findViewById(R.id.focus_image);
        backgroundImage = (ImageView) findViewById(R.id.backgroundImage);

        createAndConfigurePreview(originalFileNames.get(0), true);

        mCropImageView.setDoubleTapEnabled(false);
        mCropImageView.setScaleEnabled(false);
        mCropImageView.setScrollEnabled(false);
        mCropImageView.setDisplayType(DisplayType.FIT_TO_SCREEN);
        mFocusImageView.setDoubleTapEnabled(false);
        mFocusImageView.setScaleEnabled(false);
        mFocusImageView.setScrollEnabled(false);
        mFocusImageView.setDisplayType(DisplayType.FIT_TO_SCREEN);

        disableFocus();
        disableCrop();

        // setup the main imageview
        ((ImageViewDrawableOverlay) mImageView)
                .setDisplayType(DisplayType.FIT_TO_SCREEN);
        ((ImageViewDrawableOverlay) mImageView).setForceSingleSelection(false);
        ((ImageViewDrawableOverlay) mImageView).setDropTargetListener(this);
        ((ImageViewDrawableOverlay) mImageView).setScaleWithContent(true);

        for (int i = 0; i < manager.panels.size(); i++) {
            IPanel panel = manager.panels.get(i);
            final String panelName = panel.getPanelName();
            String panelType = panel.getPanelType();

            if (panelName.equals("launcherPanel")) {
                LauncherPanel bpLauncher = ((LauncherPanel) panel);
                bpLauncher
                        .setOnLaunchPanelListener(new LauncherPanel.OnLaunchPanelListener() {

                            @Override
                            public void onLaunchPanelSelected(
                                    String nameLaunchPanel) {
                                try {
                                    ButtonPanel panel = (ButtonPanel) manager
                                            .getPanel(nameLaunchPanel);
                                    if (panel.getPanelInfo().getWithFragment() != null
                                            && panel.getPanelInfo() != null
                                            && panel.getPanelInfo()
                                            .getWithFragment()
                                            .equals("GridPager")
                                            && panel.getItems().size() == 1) {

                                    } else {
                                        hideGrid();
                                    }
                                } catch (Exception e) {
                                    hideGrid();
                                }
                                showCustomToast(nameLaunchPanel);
                            }
                        });
                continue;

            }

            // ----------------------------------------------------------------------------------------------------------------------------
            Log.d("PANEL",
                    "ACTION=" + panel.getAction() + "   GROUP="
                            + panel.getActionGroup());
            if (panel.getAction().equals(OKCancelPanel.ACTION)) {

                IOkCancel okCancel = (IOkCancel) panel;
                okCancel.setListener(new IOkCancelListener() {

                    @Override
                    public void onStop(Object... params) {
                        ServicesManager
                                .instance()
                                .getCurrentService()
                                .setPriority(
                                        manager.getCurrPanel().getPriority());
                        ServicesManager.instance().getCurrentService()
                                .getOkCancelListener().onStop(params);

                    }

                    @Override
                    public void onOK() {
                        ServicesManager
                                .instance()
                                .getCurrentService()
                                .setPriority(
                                        manager.getCurrPanel().getPriority());
                        ServicesManager.instance().getCurrentService()
                                .getOkCancelListener().onOK();
                    }

                    @Override
                    public void onLocked(boolean lock) {
                        ServicesManager
                                .instance()
                                .getCurrentService()
                                .setPriority(
                                        manager.getCurrPanel().getPriority());
                        ServicesManager.instance().getCurrentService()
                                .getOkCancelListener().onLocked(lock);

                    }

                    @Override
                    public void onChange(Object... params) {
                        try {
                            ServicesManager
                                    .instance()
                                    .getCurrentService()
                                    .setPriority(
                                            manager.getCurrPanel().getPriority());
                            ServicesManager.instance().getCurrentService()
                                    .getOkCancelListener().onChange(params);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancel() {
                        ServicesManager
                                .instance()
                                .getCurrentService()
                                .setPriority(
                                        manager.getCurrPanel().getPriority());
                        ServicesManager.instance().getCurrentService()
                                .getOkCancelListener().onCancel();

                    }

                    @Override
                    public void onRestore() {
                        ServicesManager
                                .instance()
                                .getCurrentService()
                                .setPriority(
                                        manager.getCurrPanel().getPriority());
                        ServicesManager.instance().getCurrentService()
                                .getOkCancelListener().onRestore();

                    }

                    @Override
                    public void onShow() {
                        ServicesManager
                                .instance()
                                .getCurrentService()
                                .setPriority(
                                        manager.getCurrPanel().getPriority());
                        ServicesManager.instance().getCurrentService()
                                .getOkCancelListener().onShow();
                    }

                    @Override
                    public void onChangeFromUser(Object... params) {
                        ServicesManager
                                .instance()
                                .getCurrentService()
                                .setPriority(
                                        manager.getCurrPanel().getPriority());
                        ServicesManager.instance().getCurrentService()
                                .getOkCancelListener().onChangeFromUser(params);
                    }
                });
            } else if (panel.getAction().equals(SliderPanel.ACTION)) {

            } else if (!panel.getAction().equals("")) {
                try {
                    ButtonPanel buttonPanel = (ButtonPanel) panel;
                    IService s = ServicesManager.instance().getService(this,
                            manager, panel.getAction(), panel.getActionGroup());

                    if (s != null) {
                        Log.d("SERVICE", s.getClass().getSimpleName());
                        s.setPriority(panel.getPriority());
                        buttonPanel.setOnItemListener(s.getOnItemListener(),
                                false);
                        buttonPanel.setOnLaunchListener(s
                                .getOnLaunchPanelListener());
                        buttonPanel.setOnStateListener(s.getOnStateListener());
                    }
                } catch (ClassCastException e) {
                    try {
                        IOkCancel buttonPanel = (IOkCancel) panel;
                        IService s = ServicesManager.instance().getService(
                                this, manager, panel.getAction(),
                                panel.getActionGroup());
                        if (s != null) {
                            Log.d("SERVICE", s.getClass().getSimpleName());
                            s.setPriority(panel.getPriority());
                            buttonPanel.setListener(s.getOkCancelListener());
                            panel.setOnStateListener(s.getOnStateListener());
                        }
                    } catch (Exception e2) {

                    }
                }
            }

            // ----------------------------------------------------------------------------------------------------------------------------

            if (panelName.contains("start")) {
                manager.ShowPanel(panelName, null);
            }

            if (panelName.contains("turbo_panel_start")) {
                TurboScanPanel tPanel = ((TurboScanPanel) panel);
                tPanel.setOnActionListener(new OnActionListener() {

                    @Override
                    public void onAction(int a) {
                        switch (a) {
                            case TurboScanPanel.ACTION_SAVE:
                                saveCopyPhoto();
                                break;
                            case TurboScanPanel.ACTION_BACK:
                                finish();
                                break;
                            default:
                                break;
                        }

                    }
                });
            }

            if (panelName.contains("hdr_panel_start") ) {
                TurboScanPanel tPanel = ((TurboScanPanel) panel);
                tPanel.setOnLaunchListener(new OnLaunchPanelListener() {

                    @Override
                    public void onLaunchPanelSelected(LauncherItemView item,
                                                      String nameLaunchPanel, List<String> productIds) {
                        shareMenu.setVisibility(View.INVISIBLE);
                    }
                });
                tPanel.setOnActionListener(new OnActionListener() {

                    @Override
                    public void onAction(int a) {
                        switch (a) {
                            case TurboScanPanel.ACTION_SAVE:
                                if (shareMenu.getVisibility() == View.INVISIBLE) {
                                    shareMenu.setVisibility(View.VISIBLE);
                                } else {
                                    shareMenu.setVisibility(View.INVISIBLE);
                                }
                                break;
                            case TurboScanPanel.ACTION_BACK:
                                onBackPressed();
                                break;
                            default:
                                break;
                        }

                    }
                });
            }

            if (panelName.contains("Effects_start") ) {
                ButtonPanel tPanel = ((ButtonPanel) panel);
                /*tPanel.setOnLaunchListener(new OnLaunchPanelListener() {

                    @Override
                    public void onLaunchPanelSelected(LauncherItemView item,
                                                      String nameLaunchPanel, List<String> productIds) {
                        shareMenu.setVisibility(View.INVISIBLE);
                    }
                });*/
                tPanel.setOnActionListener(new OnActionListener() {

                    @Override
                    public void onAction(int a) {
                        switch (a) {
                            case TurboScanPanel.ACTION_SAVE:
                                if (shareMenu.getVisibility() == View.INVISIBLE) {
                                    shareMenu.setVisibility(View.VISIBLE);
                                } else {
                                    shareMenu.setVisibility(View.INVISIBLE);
                                }
                                break;
                            case TurboScanPanel.ACTION_BACK:
                                onBackPressed();
                                break;
                            default:
                                break;
                        }

                    }
                });
            }


            if (panelName.equals("ChoosePanel")
                    || panelName.equals("ChooseFiltersPanel")) {
                ButtonPanel bpLauncher = ((ButtonPanel) panel);
                bpLauncher.setOnLaunchListener(new OnLaunchPanelListener() {

                    @Override
                    public void onLaunchPanelSelected(LauncherItemView item,
                                                      String nameLaunchPanel, List<String> productIds) {
                        showCustomToast(nameLaunchPanel);
                    }
                });
            }

        }

        findViewById(R.id.processing_share).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View arg0) {
                        hideRemoveAdsButton();
                        // sliderLocked = (manager.getCurrPanel().getPanelType()
                        // .equals(PanelManager.SLIDER_PANEL_TYPE) &&
                        // ((SliderPanel) manager
                        // .getCurrPanel()).isLocked());
                        String currPanelType = manager.getCurrPanel()
                                .getPanelType();
                        if (currPanelType
                                .equals(PanelManager.SLIDER_PANEL_TYPE)) {
                            sliderLocked = ((SliderPanel) manager
                                    .getCurrPanel()).isLocked();
                        } else if (currPanelType
                                .equals(PanelManager.SLIDERS_BAR_PANEL_TYPE)) {
                            sliderLocked = ((SlidersBarsPanel) manager
                                    .getCurrPanel()).isLocked();
                        } else {
                            sliderLocked = false;
                        }
                        if (sliderLocked) {
                            showBuyDialogFromSavePhoto = true;
                            // showBuyDialog(((SliderPanel)
                            // manager.getCurrPanel()).getProductIds());
                            if (manager.getCurrPanel() instanceof SliderPanel) {
                                showBuyDialog(((SliderPanel) manager
                                        .getCurrPanel()).getProductIds());
                            }
                            if (manager.getCurrPanel() instanceof SlidersBarsPanel) {
                                showBuyDialog(((SlidersBarsPanel) manager
                                        .getCurrPanel()).getProductIds());
                            }
                            return;
                        }
                        share();
                        if (!tempFileName.equals(resultFileName)) {
                            tempFileName = resultFileName;
                        }
                    }
                });

        findViewById(R.id.btnSavePhoto).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View arg0) {
                        try {
                            hideRemoveAdsButton();

                            String currPanelType = manager.getCurrPanel()
                                    .getPanelType();
                            if (currPanelType
                                    .equals(PanelManager.SLIDER_PANEL_TYPE)) {
                                sliderLocked = ((SliderPanel) manager
                                        .getCurrPanel()).isLocked();
                            } else if (currPanelType
                                    .equals(PanelManager.SLIDERS_BAR_PANEL_TYPE)) {
                                sliderLocked = ((SlidersBarsPanel) manager
                                        .getCurrPanel()).isLocked();
                            } else {
                                sliderLocked = false;
                            }
                            if (sliderLocked) {
                                showBuyDialogFromSavePhoto = true;
                                if (manager.getCurrPanel() instanceof SliderPanel) {
                                    showBuyDialog(((SliderPanel) manager
                                            .getCurrPanel()).getProductIds());
                                }
                                if (manager.getCurrPanel() instanceof SlidersBarsPanel) {
                                    showBuyDialog(((SlidersBarsPanel) manager
                                            .getCurrPanel()).getProductIds());
                                }
                                return;
                            }
                            savePhoto();
                            if (resultFileName != null
                                    && !tempFileName.equals(resultFileName)) {
                                tempFileName = resultFileName;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            new ExceptionHandler(e, "SavePhoto");
                        }
                    }
                });

        View newButton = findViewById(R.id.processing_back);
        newButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Utils.reportFlurryEvent("EditPhotoActions", "New");
                onBackPressed();
            }
        });
        marketingHelper = new MarketingHelper(this, this);

        if (!IsAdsHidden()) {
            AdView adView = getAdView();
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {
                    if (!firstAdRecieved) {
                      //  showRemoveAdsButton();
                    }
                    firstAdRecieved = true;
                }
            });
            adView.loadAd(adRequest);
        }
        // Configure Chartboost
//		if (!IsAdsHidden()
//				&& (getResources().getBoolean(R.bool.use_chartboost_banner))) {
//			cb = Chartboost.sharedChartboost();
//			String ChartboostAppId = getResources().getString(
//					R.string.ChartboostAppId);
//			String ChartboostAppSignature = getResources().getString(
//					R.string.ChartboostAppSignature);
//			cb.onCreate(this, ChartboostAppId, ChartboostAppSignature, null);
//		}
        shareMenu = (LinearLayout) findViewById(R.id.shareMenu);
        imgLockScreen = (ImageView) findViewById(R.id.imgLockScreen);
        imgFullLockScreen = (ImageView) findViewById(R.id.imgFullLockScreen);
        progressBarProcessing = (ProgressBar) findViewById(R.id.progressBarProcessing);
        progressBarHorProcessing = (ProgressBar) findViewById(R.id.progressBarHorProcessing);
        ImageButton imgShareHdr = (ImageButton) findViewById(R.id.imgShareHdr);
        imgShareHdr.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    shareMenu.setVisibility(View.INVISIBLE);
                    hideRemoveAdsButton();
                    String currPanelType = manager.getCurrPanel().getPanelType();
                    if (currPanelType.equals(PanelManager.SLIDER_PANEL_TYPE)) {
                        sliderLocked = ((SliderPanel) manager.getCurrPanel()).isLocked();
                    } else if (currPanelType.equals(PanelManager.SLIDERS_BAR_PANEL_TYPE)) {
                        sliderLocked = ((SlidersBarsPanel) manager.getCurrPanel()).isLocked();
                    } else {
                        sliderLocked = false;
                    }
                    if (sliderLocked) {
                        showBuyDialogFromSavePhoto = true;
                        if (manager.getCurrPanel() instanceof SliderPanel) {
                            //showBuyDialog(((SliderPanel) manager.getCurrPanel()).getProductIds());
                        }
                        if (manager.getCurrPanel() instanceof SlidersBarsPanel) {
                            //showBuyDialog(((SlidersBarsPanel) manager.getCurrPanel()).getProductIds());
                        }
                        return;
                    }
                    share();
                    if (resultFileName != null
                            && !tempFileName.equals(resultFileName)) {
                        tempFileName = resultFileName;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    new ExceptionHandler(e, "SavePhoto");
                }
            }
        });
        ImageButton imgSaveHdr = (ImageButton) findViewById(R.id.imgSaveHdr);
        imgSaveHdr.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    shareMenu.setVisibility(View.INVISIBLE);
                   hideRemoveAdsButton();
                    String currPanelType = manager.getCurrPanel().getPanelType();
                    if (currPanelType.equals(PanelManager.SLIDER_PANEL_TYPE)) {
                        sliderLocked = ((SliderPanel) manager.getCurrPanel()).isLocked();
                    } else if (currPanelType.equals(PanelManager.SLIDERS_BAR_PANEL_TYPE)) {
                        sliderLocked = ((SlidersBarsPanel) manager.getCurrPanel()).isLocked();
                    } else {
                        sliderLocked = false;
                    }
                    if (sliderLocked) {
                        showBuyDialogFromSavePhoto = true;
                        if (manager.getCurrPanel() instanceof SliderPanel) {
                            //showBuyDialog(((SliderPanel) manager.getCurrPanel()).getProductIds());
                        }
                        if (manager.getCurrPanel() instanceof SlidersBarsPanel) {
                            //showBuyDialog(((SlidersBarsPanel) manager.getCurrPanel()).getProductIds());
                        }
                        return;
                    }
                    savePhoto();
                    if (resultFileName != null
                            && !tempFileName.equals(resultFileName)) {
                        tempFileName = resultFileName;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    new ExceptionHandler(e, "SavePhoto");
                }
            }
        });

        if (originalFileNames.size() > 1) {
            combineImages();
        } else if (originalFileNames.size() > 0) {
            processImage();
        }
        // showFacebookPost();

    }

    public void showGrid(ButtonPanel p, String folder,
                         List<LauncherItemView> items,
                         OnGridItemClickListener onGridItemClickListener) {
        FrameLayout screenContainer = (FrameLayout) findViewById(R.id.launcher_screen_container);

        if (screenContainer != null) {
            if (screenContainer.getTag() == null
                    || !((String) screenContainer.getTag()).equals(folder)) {
                if (screenContainer.getChildAt(0) != null) {
                    View v = screenContainer.getChildAt(0);
                    if (v instanceof GridPagerPanel) {
                        ((GridPagerPanel) v).clearCache();
                    }
                }
                screenContainer.removeAllViews();
                screenContainer.setTag(folder);

                GridPagerPanel gridPanel = new GridPagerPanel(
                        ChooseProcessingActivity.this);
                gridPanel.setPath(folder);
                gridPanel.setOnGridItemClickListener(onGridItemClickListener);
                p.setOnItemListener(gridPanel.getOnItemListener(), true);

                gridPanel.setItemsWithAssetsFolders(items,
                        screenContainer.getWidth(),
                        p.getHorizontalScrollView(), p.getRoot());
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                screenContainer.addView(gridPanel, lp);

            }
            screenContainer.setVisibility(View.VISIBLE);
        }
    }

    private AdView getAdView() {
        return (AdView) findViewById(R.id.adView);
    }

    @Override
    protected int getPortraitLayout() {
        return R.layout.choose_processing_portrait;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.choose_processing_portrait;
    }

    @Override
    public void onBackPressed() {

       /* // Create the interstitial.
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(AD_UNIT_ID_INSERTIAL_BACKPRESS);

        // Create ad request.
        com.google.android.gms.ads.AdRequest adRequestinsertial = new
                com.google.android.gms.ads.AdRequest.Builder()
                .build();

        // Begin loading your interstitial.
        interstitial.loadAd(adRequestinsertial);
        interstitial.setAdListener(new AdListener() {
            public void onAdLoaded() {
                if (interstitial.isLoaded()) {

                    interstitial.show();

                } else {

                }

            }

        });*/
        startAppAd.onBackPressed();


        if ((manager.getCurrPanel().getPanelName().equals("launcherPanel") || manager
                .getCurrPanel().getPanelName().contains("start"))
                && findViewById(R.id.launcher_screen_container).getVisibility() == View.VISIBLE) {

            hideGrid();
        } else {

            if (/*(cb != null) && cb.onBackPressed()
                    && */!manager.getCurrPanel().getPanelName().contains("start")) {
                return;
            } else {

                deleteOldProcessedFile();
                deleteOldProcessedFileNew();
                deleteOriginalFiles();

                if (getResources().getString(R.string.workflow).equals("powercam")) {
                    if (returnToCameraClass != null) {
                        if (returnToCameraActivity) {
                            Intent intent = new Intent(this, returnToCameraClass);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(this, returnToCameraClass);
                            startActivity(intent);
                        }
                    }
                } else if (getResources().getString(R.string.workflow).equals("realtime")) {
					/*if (returnToCameraActivity) {
						Intent intent = new Intent(this, RealTimeCameraPreview.class);
						startActivity(intent);
					} else {*/



                    Intent intent = new Intent(this, SplashActivity.getHomeScreenClass(getApplicationContext()));
                    startActivity(intent);
                    //}
                } else {
                    if (returnToCameraActivity) {
                        Intent intent = new Intent(this,
                                CameraPreviewActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, SplashActivity.getHomeScreenClass(getApplicationContext()));
                        startActivity(intent);
                    }
                }
                finish();
            }
        }
    }

    public void hideGrid() {
        FrameLayout screenContainer = (FrameLayout) findViewById(R.id.launcher_screen_container);
        if (screenContainer != null) {
            screenContainer.setVisibility(View.GONE);
        }
    }

    public boolean IsAdsHidden() {
        if (getResources().getBoolean(R.bool.show_ads) == false || ApplicationClass.AD_FREE_MODE) {
            return true;
        }
        return isFullVersion()
                //|| isItemPurchased(this, getRemoveAdsPurchaseId())
                || MarketingHelper.isTrialPeriod(this) || isFacebookPosted()
                || isPromoAppInstalled() || isOneofPacksPurchased();
    }

    public boolean isOneofPacksPurchased() {
        List<String> productIds = manager.getAllProductIds();
        if (productIds == null) {
            return false;
        }
        for (int i = 0; i < productIds.size(); i++) {
            if (isItemPurchased(this, productIds.get(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onStart() {

        super.onStart();

        if (IsAdsHidden()) {
        //    hideAds();
        } else if (firstAdRecieved) {
           // showRemoveAdsButton();
        }
        FlurryAgent.onStartSession(getApplicationContext(),
                getString(R.string.flurryApiKey));
        rate();

        // if (isFirstTime &&
        // getResources().getBoolean(R.bool.use_admob_banner)) {
        // banner = IsAdsHidden() ? null : new Banner(this, 1,
        // getString(R.string.adUnitIdInterstitial), true);
        // }
        //showPromo();
        Log.d("check", "onStart: showing ad ");

        startAppAd.showAd();

        Log.d("check", "onStart: showing ad bool : "+startAppAd.showAd());

//		if (cb != null) {
//			cb.onStart(this);
//			// Notify the beginning of a user session. Must not be dependent on
//			// user
//			// actions or any prior network requests.
//			cb.startSession();
//			// Show an interstitial
//			cb.showInterstitial();
//		}
    }

    private void showPromo() {
        Log.d("check", "showPromo: showing promo");
        int displays = MarketingHelper
                .getPromoDisplaysNumber(this, isFirstTime);

        if (isFirstTime && displays == 2) {
            Log.d("test", "show promo");
            PromoLoader loader = new PromoLoader(ChooseProcessingActivity.this,
                    "InstaEffects Camera Promo");
            if (!loader.showContent()) {
                displays--;
                MarketingHelper.setPromoDisplaysNumber(
                        ChooseProcessingActivity.this.getApplicationContext(),
                        displays);
                // show interstitial
                if (isFirstTime
                        && getResources().getBoolean(R.bool.use_admob_banner)) {
                    banner = IsAdsHidden() ? null : new Banner(this, 1,
                            getString(R.string.adUnitIdInterstitial), true);
                }
            }
        } else {
            // show interstitial
            if (isFirstTime
                    && getResources().getBoolean(R.bool.use_admob_banner)) {
                banner = IsAdsHidden() ? null : new Banner(this, 1,
                        getString(R.string.adUnitIdInterstitial), true);
            }
        }
        isFirstTime = false;
    }

    private void rate() {
        try {
            if (marketingHelper != null) {
                marketingHelper.updateRateCondition();
                if (marketingHelper.showRate(10)) {
                    showDialog(Dialogs.RATE);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
            new ExceptionHandler(th, "ShowRate");
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case Dialogs.RATE:
                return marketingHelper != null ? marketingHelper.createRateDialog(
                        getString(R.string.rateTitle), getPackageName()) : null;
            default:
        }
        return null;
    }

    private final class Dialogs {
        private static final int RATE = 1;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Batch.onNewIntent(this, intent);
        super.onNewIntent(intent);
    }


    @Override
    public void onStop() {
        super.onStop();
//		if (cb != null) {
//			cb.onStop(this);
//		}
        FlurryAgent.onEndSession(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        ServicesManager.clear();
        try {
            AdView adView = getAdView();
            if (adView != null) {
                adView.removeAllViews();
                adView.destroy();
            }

//			if (cb != null) {
//				cb.onDestroy(this);
//			}

            // galleryAdapter.onDestroy();

            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
                mBitmap = null;
            }
            if (mBlurBitmap != null && !mBlurBitmap.isRecycled()) {
                mBlurBitmap.recycle();
                mBlurBitmap = null;
            }
            if (mPreview != null && !mPreview.isRecycled()) {
                mPreview.recycle();
            }

            deleteOldProcessedFile();
            deleteOldProcessedFileNew();
            deleteOriginalFiles();
            if (progressBar != null) {
                progressBar.dismiss();
                progressBar = null;
            }
            if (saveProgressBar != null) {
                saveProgressBar.dismiss();
                saveProgressBar = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            new ExceptionHandler(e, "ChooseSticker::onDestroyError");
        }
        super.onDestroy();
    }

    private void saveHDImage(final ActionCallback<String> onSuccessCallback,
                             final boolean showProgress, final boolean blockUI) {


		/*
		 * Utils.reportFlurryEvent("SaveHDImage", "effectName = " + effectName +
		 * "; brightness = " + brightness + " contrast = " + contrast);
		 */
		/*
		 * Log.d("processing", "SaveHDImage effectName = " + effectName +
		 * "; brightness = " + brightness + " contrast = " + contrast);
		 */

        int w = AppSettings.getWidth(this);
        int h = AppSettings.getHeight(this);
       // Utils.reportFlurryEvent("SaveHDResolution", w + "x" + h);

        final ImageProcessing processing = new ImageProcessing(
                getApplicationContext(), createJSONPreset(true), w, h,
                new ProcessingCallback() {

                    @Override
                    public void onStart() {
                        // keep screen on and disable orientation change during
                        // the long running process
                        getWindow().addFlags(
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        lastSavedSelection = currentSelection;

						/*
						 * saveProgressBar = showProgress ? ProgressDialog
						 * .show(ChooseProcessingActivity.this, "",
						 * getString(R.string.savingProgress), true, false) :
						 * new ProgressDialog( ChooseProcessingActivity.this);
						 */
                        if (!showProgress) {
                            Toast.makeText(ChooseProcessingActivity.this,
                                    getString(R.string.saved_notification),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            if (getString(R.string.workflow)
                                    .equals("turboscan")) {
                                ProgressBar progressBarSavePhoto = (ProgressBar) findViewById(R.id.progressBarProcessing);
                                progressBarSavePhoto
                                        .setVisibility(View.VISIBLE);
                            } else {
                                ProgressBar progressBarSavePhoto = (ProgressBar) findViewById(R.id.progressBarSavePhoto);
                                progressBarSavePhoto
                                        .setVisibility(View.VISIBLE);
                            }
                            if (blockUI) {
                                imgFullLockScreen.setVisibility(View.VISIBLE);
                            }
                        }
                        // Map<String, String> params = new HashMap<String,
                        // String>();
                        // params.put("Camera", cameraPreset.getName());
                        // params.put("Filter", filter.getName());
                        FlurryAgent.onEvent("ProcessHDPhoto");
                    }

                    @Override
                    public void onFail(Throwable e) {
                        // reset keep screen on and disable orientation change
                        // flags
                        getWindow().clearFlags(
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        if (getString(R.string.workflow).equals("turboscan")) {
                            ProgressBar progressBarSavePhoto = (ProgressBar) findViewById(R.id.progressBarProcessing);
                            progressBarSavePhoto.setVisibility(View.INVISIBLE);
                        } else {
                            ProgressBar progressBarSavePhoto = (ProgressBar) findViewById(R.id.progressBarSavePhoto);
                            progressBarSavePhoto.setVisibility(View.INVISIBLE);
                        }
                        if (blockUI) {
                            imgFullLockScreen.setVisibility(View.INVISIBLE);
                        }
                        try {
                            if (saveProgressBar != null) {
                                saveProgressBar.dismiss();
                                saveProgressBar = null;
                            }
                            Toast.makeText(ChooseProcessingActivity.this,
                                    getString(R.string.failed_notification),
                                    Toast.LENGTH_SHORT).show();
                            //Utils.reportFlurryEvent("SaveHDResult", "Error");
                            Log.d("processing", "SaveHDResult Error");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            e.printStackTrace();
                            new ExceptionHandler(e, "PROCESSING_HD_FAILED");
                            Log.d("processing", "PROCESSING_HD_FAILED");
                        }
                        FlurryAgent.endTimedEvent("ProcessHDPhoto");
                    }

                    @Override
                    public void onSuccess(String resultFileName) {
                        // reset keep screen on and disable orientation change
                        // flags
                        getWindow().clearFlags(
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        ProgressBar progressBarSavePhoto = (ProgressBar) findViewById(R.id.progressBarSavePhoto);
                        progressBarSavePhoto.setVisibility(View.INVISIBLE);
                        if (blockUI) {
                            imgFullLockScreen.setVisibility(View.INVISIBLE);
                        }
                        try {
                            if (saveProgressBar != null) {
                                saveProgressBar.dismiss();
                                saveProgressBar = null;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            new ExceptionHandler(e, "ImageProcessing:onSuccess");
                        }
                        lastHDFileName = resultFileName;

                        startAppAd.showAd();
                        if (lastOnSuccessCallback != null) {
                            lastOnSuccessCallback.onAction(resultFileName);
                            lastOnSuccessCallback = null;
                        } else {
                            onSuccessCallback.onAction(resultFileName);
                        }


                        FlurryAgent.endTimedEvent("ProcessHDPhoto");
                       // Utils.reportFlurryEvent("SaveHDResult", "OK");
                        Log.d("processing", "SaveHDResult OK");
                    }

                    @Override
                    public void onBitmapCreated(Bitmap bitMap) {
                    }

                    @Override
                    public void onBitmapCreated(Image2 bitMap) {
                    }

                    @Override
                    public void onBitmapCreatedOpenCV() {
                    }

                    @Override
                    public void onCancelled() {
                    }

                });
        String resultHDFileName = getProcessedFileName();
        processing.processPictureAsync(originalFileNames, resultHDFileName);
    }

    private void stickerProccessOrder(ArrayList<Filter> FilterArray,
                                      Filter[] filters, ArrayList<Preset> PresetArray) {
        Log.d("sticker", "processOrder.size = " + processOrder.size());

        for (int i = 0; i < processOrder.size(); i++) {
            // add stickers
            List<Filter> stikerFilters = processOrder.order.get(i).filters;
            int imageAngle = processOrder.order.get(i).angle;
            boolean imageFlipVertical = processOrder.order.get(i).flipVertical;
            boolean imageFlipHorizontal = processOrder.order.get(i).flipHorizontal;

            if (stikerFilters != null && !stikerFilters.isEmpty()) {
                Preset stickerPreset = new Preset();
                stickerPreset.setName("allstickers");
                Filter[] f = new Filter[stikerFilters.size()];
                // stikerFilters.get(0).
                stickerPreset.setFilters(stikerFilters.toArray(f));
                PresetArray.add(stickerPreset);
            }

            if ((imageAngle != 0) || (imageFlipVertical)
                    || (imageFlipHorizontal)) {
                FlipRotateFilter filterFlipRotate = new FlipRotateFilter();
                filterFlipRotate.setAngle(imageAngle);
                filterFlipRotate.setFlipHorizontal(imageFlipHorizontal);
                filterFlipRotate.setFlipVertical(imageFlipVertical);

                FilterArray = new ArrayList<Filter>();
                FilterArray.add(filterFlipRotate);

                filters = new Filter[FilterArray.size()];
                FilterArray.toArray(filters);

                Preset preset = new Preset();
                preset.setFilters(filters);

                PresetArray.add(preset);
            }
        }

    }

    private String createJSONPreset() {
        return createJSONPreset(false);
    }

    private String createJSONPreset(boolean fromSaveShare) {
        ArrayList<Filter> FilterArray = null;
        Filter[] filters = null;
        ArrayList<Preset> PresetArray;
        Preset[] presets;
        Preset filterPreset;

        PresetArray = new ArrayList<Preset>();

        filterPreset = null;
        if (!choosedFilterPreset.equals("")) {
            int processingIndex = Presets.getProcessingIndex(
                    ChooseProcessingActivity.this, choosedFilterPreset);
            filterPreset = processingPresets[processingIndex];
        }
        if (filterPreset != null) {
            PresetArray.add(filterPreset);
        }

        String[] always_used_filters = getResources().getStringArray(R.array.always_used_filters);
        if ((first_launch || fromSaveShare) && (always_used_filters.length != 0)) {
            for (int i = 0; i < always_used_filters.length; i++) {
                int processingIndex = Presets.getProcessingIndex(
                        ChooseProcessingActivity.this, always_used_filters[i]);
                filterPreset = processingPresets[processingIndex];
                if (filterPreset != null) {
                    PresetArray.add(filterPreset);
                }
            }
        }

        for (IService s : ServicesManager.instance().getServicesQueueHighPriority()) {
            ArrayList<Preset> fpresets = s.getFilterPreset();
            if (fpresets != null) {
                PresetArray.addAll(fpresets);
            }
        }
        stickerProccessOrder(FilterArray, filters, PresetArray);
        for (IService s : ServicesManager.instance().getServicesQueueLowPriority()) {
            ArrayList<Preset> fpresets = s.getFilterPreset();
            if (fpresets != null) {
                PresetArray.addAll(fpresets);
            }
        }
//		for (IService s : ServicesManager.instance().getServicesQueue()) {
//			ArrayList<Preset> fpresets = s.getFilterPreset();
//			if (fpresets != null) {
//				PresetArray.addAll(fpresets);
//			}
//		}

        if (perspectiveFilter != null && !first_launch && !restore_launch) {
            FilterArray = new ArrayList<Filter>();
            FilterArray.add(perspectiveFilter);

            filters = new Filter[FilterArray.size()];
            FilterArray.toArray(filters);

            Preset preset = new Preset();
            preset.setFilters(filters);

            PresetArray.add(preset);
        }

        int id = getResources().getIdentifier("use_watermark", "bool", getPackageName());
        boolean use_watermark = false;
        if (id != 0) {
            use_watermark = getResources().getBoolean(id);
        }
        if (!first_launch && use_watermark && fromSaveShare) {
            FilterArray = new ArrayList<Filter>();

            BlendFilter filterOverlay = new BlendFilter();
            filterOverlay.setAlgorithm(Algorithm.overlay);
            filterOverlay.setBlendSrc("watermark.png");
            FilterArray.add(filterOverlay);

            filters = new Filter[FilterArray.size()];
            FilterArray.toArray(filters);

            Preset preset = new Preset();
            preset.setFilters(filters);

            PresetArray.add(preset);
        }

        if (cameraPreset != null) {
            Log.d("processing",
                    "cameraPreset = " + cameraPreset.convertToJSON());
            PresetArray.add(cameraPreset);
        }

        presets = new Preset[PresetArray.size()];
        PresetArray.toArray(presets);

        Presets effectsPreset = new Presets(null, presets, null);

        String presetsJson = effectsPreset.convertToJSON();
        Log.d("processing", presetsJson);

        return presetsJson;
    }

    public synchronized void processImage() {
        if (new_values) {
            return;
        }

        Log.d("processImage", "processImage");
        processPreview = true;
        // new_values = true;
        ImageProcessing processing = new ImageProcessing(
                getApplicationContext(), createJSONPreset(), 200, 150,
                new ProcessingCallback() {

                    @Override
                    public void onSuccess(String outFileName) {
                        Log.d("processImage", "onSuccess");
                        onClearCurrent(true);
                        List<String> paths = new ArrayList<String>();
                        paths.add(outFileName);
                        createAndConfigurePreview(outFileName, true);

                        hideCustomToast();
                        if (getString(R.string.workflow).equals("turboscan")) {

                        } else {
                            progressBarProcessing.setVisibility(View.INVISIBLE);
                        }
                        processPreview = false;
                        currentProcessing = null;
                        processImage2();
                    }

                    @Override
                    public void onStart() {
                        Log.d("processImage", "onStart");
                        // new_values = true;
                        FlurryAgent.onEvent("ProcessPhoto");

                        getWindow().addFlags(
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        // imgLockScreen.setVisibility(View.VISIBLE);
                        imgRevert.setEnabled(false);
                        // manager.disableViews();
//						if (!toastVisible) {
//							progressBarProcessing.setVisibility(View.VISIBLE);
//						}
                    }

                    @Override
                    public void onFail(Throwable e) {
                        Log.d("processImage", "onFail");
                        processPreview = false;
                        currentProcessing = null;
                        processImage2();
                    }

                    @Override
                    public void onBitmapCreatedOpenCV() {
                    }

                    @Override
                    public void onBitmapCreated(Image2 bitMap) {
                    }

                    @Override
                    public void onBitmapCreated(Bitmap bitMap) {
                    }

                    @Override
                    public void onCancelled() {
                        if (nextProcessing != null) {
                            List<String> files = getSmallTempProcessedFileName();
                            List<String> temp = new ArrayList<String>();
                            for (String s : files) {
                                File file = new File(s);
                                if (file.exists())
                                    temp.add(s);
                            }
                            files = temp;
                            if (files.size() != 0) {
                                nextProcessing.processPictureAsync(files,
                                        getSmallTempProcessedFileName2());
                            }
                            currentProcessing = nextProcessing;
                            nextProcessing = null;
                        }
                    }
                });

        // resultFileName = getProcessedFileName();

        if (first_launch || restore_launch) {
            List<String> temp = new ArrayList<String>();
            for (String s : originalFileNames) {
                File file = new File(s);
                if (file.exists())
                    temp.add(s);
            }
            originalFileNames = temp;

            if (originalFileNames.size() != 0) {
                processing.processPictureAsync(originalFileNames,
                        getSmallTempProcessedFileName().get(0));
            } else {
                FlurryAgent.logEvent("Cancel process: File not exist");
                if (getResources().getString(R.string.workflow).equals("powercam")) {

                } else {
                    if (returnToCameraActivity) {
                        Intent intent = new Intent(this,
                                CameraPreviewActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this,
                                SplashActivity.getHomeScreenClass(getApplicationContext()));
                        startActivity(intent);
                    }
                }
                finish();
            }
        } else {
            processing.setCancellable(true);
            List<String> files = getSmallTempProcessedFileName();
            List<String> temp = new ArrayList<String>();
            for (String s : files) {
                File file = new File(s);
                if (file.exists())
                    temp.add(s);
            }
            files = temp;
            if (files.size() != 0) {
                if (currentProcessing != null) {
                    currentProcessing.cancel();
                    nextProcessing = processing;
                } else {
                    processing.processPictureAsync(files,
                            getSmallTempProcessedFileName2());
                    currentProcessing = processing;
                }
            } else if (currentProcessing != null) {
                FlurryAgent.logEvent("File not exist: restore it");
                restore_launch = true;
                processImage();
            }
        }
    }

    private synchronized void processImage2() {
        // if (new_values) {
        // return;
        // }

        // new_values = true;
        ImageProcessing processing = new ImageProcessing(
                getApplicationContext(), createJSONPreset(),
                AppSettings.getPreviewWidth(this),
                AppSettings.getPreviewHeight(this), new ProcessingCallback() {

            @Override
            public void onSuccess(String outFileName) {
                Log.d("processImage", "onSuccess2");
                // reset keep screen on and disable orientation change
                // flags
                endTime = System.nanoTime();
                Log.d("TimeProcessing", "Elapsed time: "
                        + (endTime - startTime));
                getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                manager.enableViews();
                imgLockScreen.setVisibility(View.INVISIBLE);
                imgRevert.setEnabled(true);
                onClearCurrent(true);
                if (manager.getCurrPanel().getPanelType()
                        .equals(PanelManager.OK_CANCEL_PANEL_TYPE)
                        && manager.getCurrPanel().getPanelName()
                        .equals("OKCancelFocus")) {
                    if (manager.upLevel()) {
                        disableFocus();
                    }
                }

                if (manager.getCurrPanel().getPanelType()
                        .equals(PanelManager.OK_CANCEL_PANEL_TYPE)
                        && manager.getCurrPanel().getPanelName()
                        .equals("OKCancelCrop")) {
                    if (manager.upLevel()) {
                        disableCrop();
                    }
                }

                hideCustomToast();

                progressBarProcessing.setVisibility(View.INVISIBLE);
                hideOnePixelProgress(false);
                List<String> paths = new ArrayList<String>();
                paths.add(outFileName);
                createAndConfigurePreview(outFileName, true);
                resultFileName = outFileName;
                FlurryAgent.endTimedEvent("ProcessPhoto");
                Log.d("processing", "onSuccess processing");

                // new_values = false;
                if (first_launch) {
                    first_launch = false;

                    String choosedFilter = getApplicationContext()
                            .getResources().getString(
                                    R.string.choosed_filter);
                    if (choosedFilter.equals("")) {
                        choosedFilterPreset = getApplicationContext()
                                .getResources().getString(
                                        R.string.choosed_filter_preset);
                        if (!choosedFilterPreset.equals("")) {
                            processImage();
                        }
                    }


//							if (choosedFilter.startsWith("lens_")) {
//								((ButtonPanel) manager.getPanel("Lens"))
//										.callOnClickItem(choosedFilter);
//							} else {
//								((ButtonPanel) manager.getPanel("Effects"))
//										.callOnClickItem(choosedFilter);
//							}
                    for (int p = 0; p < manager.panels.size(); p++) {
                        IPanel panel = manager.panels.get(p);
                        String panelType = panel.getPanelType();

                        if (panelType.equals(PanelManager.BUTTON_PANEL_TYPE)) {
                            ((ButtonPanel) panel).callOnClickItem(choosedFilter);
                        }
                    }
                } else {
                    choosedFilterPreset = "";
                }
						/*
						 * ((ImageViewDrawableOverlay) mImageView)
						 * .removeHightlightView(currentService
						 * .getHighlightView());
						 */

                currentProcessing = null;
            }

            @Override
            public void onStart() {
                Log.d("processImage", "onStart2");
                // keep screen on and disable orientation change during
                // the long running process
                startTime = System.nanoTime();
                // new_values = true;
                FlurryAgent.onEvent("ProcessPhoto");

                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // imgLockScreen.setVisibility(View.VISIBLE);
                imgRevert.setEnabled(false);
                // manager.disableViews();
                if (!toastVisible) {
                    showOnePixelProgress();
                }
            }

            @Override
            public void onFail(Throwable e) {
                Log.d("processImage", "onFail2");
                // reset keep screen on and disable orientation change
                // flags
                endTime = System.nanoTime();
                Log.d("TimeProcessing", "Elapsed time: "
                        + (endTime - startTime));
                Log.d("processing", "onFail");
                getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                imgLockScreen.setVisibility(View.INVISIBLE);
                imgRevert.setEnabled(true);
                manager.enableViews();

                hideCustomToast();

                progressBarProcessing.setVisibility(View.INVISIBLE);
                hideOnePixelProgress(false);

                FlurryAgent.endTimedEvent("ProcessPhoto");
                // new_values = false;
                if (first_launch) {
                    first_launch = true;
                    processImage();
                }
						/*
						 * ((ImageViewDrawableOverlay) mImageView)
						 * .removeHightlightView(currentService
						 * .getHighlightView());
						 */
                currentProcessing = null;
            }

            @Override
            public void onBitmapCreatedOpenCV() {
            }

            @Override
            public void onBitmapCreated(Image2 bitMap) {
            }

            @Override
            public void onBitmapCreated(Bitmap bitMap) {
            }

            @Override
            public void onCancelled() {
                if (nextProcessing != null) {
                    List<String> files = getSmallTempProcessedFileName();
                    List<String> temp = new ArrayList<String>();
                    for (String s : files) {
                        File file = new File(s);
                        if (file.exists())
                            temp.add(s);
                    }
                    files = temp;
                    if (files.size() != 0) {
                        nextProcessing.processPictureAsync(files,
                                getSmallTempProcessedFileName2());
                    }
                    currentProcessing = nextProcessing;
                    nextProcessing = null;
                }
            }

        });

        // resultFileName = getProcessedFileName();

        if (first_launch || restore_launch) {
            List<String> temp = new ArrayList<String>();
            for (String s : originalFileNames) {
                File file = new File(s);
                if (file.exists())
                    temp.add(s);
            }
            originalFileNames = temp;

            if (originalFileNames.size() != 0) {
                processing.processPictureAsync(originalFileNames,
                        getTempProcessedFileName().get(0));
            } else {
                FlurryAgent.logEvent("Cancel process: File not exist");
                if (getResources().getString(R.string.workflow).equals("powercam")) {

                } else {
                    if (returnToCameraActivity) {
                        Intent intent = new Intent(this,
                                CameraPreviewActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this,
                                SplashActivity.getHomeScreenClass(getApplicationContext()));
                        startActivity(intent);
                    }
                }
                finish();
            }
        } else {
            processing.setCancellable(true);
            List<String> files = getTempProcessedFileName();
            List<String> temp = new ArrayList<String>();
            for (String s : files) {
                File file = new File(s);
                if (file.exists())
                    temp.add(s);
            }
            files = temp;
            if (files.size() != 0) {
                if (currentProcessing != null) {
                    currentProcessing.cancel();
                    nextProcessing = processing;
                } else {
                    processing.processPictureAsync(files,
                            getTempProcessedFileName2());
                    currentProcessing = processing;
                }
            } else if (currentProcessing != null) {
                FlurryAgent.logEvent("File not exist: restore it");
                restore_launch = true;
                processImage();
            }
        }
    }

    public int getProcImageViewHeight() {
        AdView adView = getAdView();
        int topPanelWidth = topPanel.getVisibility() == View.VISIBLE ? topPanel
                .getHeight() : 0;
        int bottomPanelWidth = manager.getVisibility() == View.VISIBLE ? manager
                .getHeight() : 0;
        int adViewHeight = adView != null
                && adView.getVisibility() == View.VISIBLE ? adView.getHeight()
                : 0;
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        return screenHeight - adViewHeight - topPanelWidth - bottomPanelWidth;
    }

    public int getProcImageViewWidth() {
        return getWindowManager().getDefaultDisplay().getWidth();
    }

    public String getProcessedFileName() {
        return Utils.getFullFileName(
                getApplicationContext().getString(R.string.photoFolder), "jpg");
    }

    public List<String> getTempProcessedFileName() {
        List<String> temp = new ArrayList<String>();
        temp.add(Utils.getFolderPath(getApplicationContext().getString(
                R.string.photoFolder))
                + "/temp.jpg");
        return temp;
    }

    public List<String> getSmallTempProcessedFileName() {
        List<String> temp = new ArrayList<String>();
        temp.add(Utils.getFolderPath(getApplicationContext().getString(
                R.string.photoFolder))
                + "/smalltemp.jpg");
        return temp;
    }

    public String getTempProcessedFileName2() {
        return Utils.getFolderPath(getApplicationContext().getString(
                R.string.photoFolder))
                + "/temp2.jpg";
    }

    public String getSmallTempProcessedFileName2() {
        return Utils.getFolderPath(getApplicationContext().getString(
                R.string.photoFolder))
                + "/smalltemp2.jpg";
    }

    private void deleteOriginalFiles() {
        List<String> temp = getTempProcessedFileName();
        for (int i = 0; i < temp.size(); ++i) {
            File file = new File(temp.get(i));
            if (file.exists()) {
                boolean b = file.delete();
                getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                Log.d("check", "deleteOriginalFiles: deleted:"+b);
                if(file.exists())
                {
                    deleteFile(file);
                }
            }
        }
        temp.clear();

        if (AppSettings.isSaveOriginal(this)) {
            return;
        }
        try {
            for (int i = 0; i < originalFileNames.size(); ++i) {
                if (i < originalFileTypes.size() && originalFileTypes.get(i)) {
                    File file = new File(originalFileNames.get(i));
                    if (file.exists()) {
                        boolean b = file.delete();
                        getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                        Log.d("check", "deleteOriginalFiles: delete "+b);
                        if(file.exists())
                        {
                            deleteFile(file);
                        }
                    }
                }

            }
            originalFileNames.clear();
            originalFileTypes.clear();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("check", "deleteOriginalFiles:error "+e);
            new ExceptionHandler(e, "DeleteOriginalFiles");
        }
    }

    private void deleteOldProcessedFile() {
		/*
		 * if (oldProcessedFile != null) { File file = new
		 * File(oldProcessedFile); file.delete(); oldProcessedFile = null; }
		 */
        if (getTempProcessedFileName2() != null) {
            File file = new File(getTempProcessedFileName2());
            if (file.exists()) {
                boolean b = file.delete();
                getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                Log.d("check", "deleteOldProcessedFile: temp2 delelted :"+b);
                if(file.exists())
                {
                    deleteFile(file);
                }

            }
        }
    }
//done by mihir raj on 12/27/2016
    private void deleteOldProcessedFileNew() {
		/*
		 * if (oldProcessedFile != null) { File file = new
		 * File(oldProcessedFile); file.delete(); oldProcessedFile = null; }
		 */
        if (getSmallTempProcessedFileName2() != null) {
            File file = new File(getSmallTempProcessedFileName2());
            if (file.exists()) {
                boolean b = file.delete();
                getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                Log.d("check", "deleteOldProcessedFileNew: Smalltemp2 delelted :"+b);
                if(file.exists())
                {
                    deleteFile(file);
                }

            }
        }

        List<String> temp = getSmallTempProcessedFileName();
        for (int i = 0; i < temp.size(); ++i) {
            File file = new File(temp.get(i));
            if (file.exists()) {
                boolean b = file.delete();
                getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                Log.d("check", "deleteSmalltempFiles: deleted:"+b);
                if(file.exists())
                {
                    deleteFile(file);
                }
            }
        }
        temp.clear();


    }

    private boolean usePreview() {
        int pW = AppSettings.getPreviewWidth(this);
        int pH = AppSettings.getPreviewHeight(this);
        int w = AppSettings.getWidth(this);
        int h = AppSettings.getHeight(this);
        return pW == w && pH == h;
    }

    private void share() {
        Log.d("processing", "share");
//        FlurryAgent.logEvent("EditPhotoShare");
//        Utils.reportFlurryEvent("EditPhotoActions", "Share");
//        try {
//            AppsFlyerLib.sendTrackingWithEvent(getApplicationContext(), getString(R.string.appsFlyerKey), "Sharing", "");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
		/*
		 * if (usePreview() && resultFileName != null) {
		 * Utils.reportFlurryEvent("SaveHDResolution", "UsePreview");
		 * Utils.addPhotoToGallery(this, resultFileName); share(resultFileName);
		 * //oldProcessedFile = null; return; }
		 */
        saveHDImage(new ActionCallback<String>() {

            @Override
            public void onAction(String fileName) {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(fileName))));
                share(fileName);
                ProgressBar progressBarSavePhoto = (ProgressBar) findViewById(R.id.progressBarSavePhoto);
                progressBarSavePhoto.setVisibility(View.INVISIBLE);
                // rate();
            }
        }, true, true);
    }

    private void share(String fileName) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fileName));
        startActivity(share);
    }

    private void saveCopyPhoto() {

        saveHDImage(new ActionCallback<String>() {
            @Override
            public void onAction(String fileName) {
                Calendar c = Calendar.getInstance();
                String[] strs = fileName.split("/");
                String appFolderName = strs[strs.length - 2];
                String name = strs[strs.length - 1];
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "yyyy-MM-dd-HH-mm-ss");

                String path = Environment.getExternalStorageDirectory()
                        + File.separator + appFolderName + File.separator
                        + sdf.format(c.getTime());
                if (documentPath != null) {
                    path = documentPath;

                    File directory = new File(documentPath);
                    File[] fList = directory.listFiles();

                    if (fList.length > 0) {
                        Log.d("1 file", fList[0].getPath());
                        Log.d("1 filename", fList[0].getName());
                        File from = new File(fList[0].getPath());
                        String pathFirst = fList[0].getPath().replace(
                                fList[0].getName(), "1.jpg");
                        File to = new File(pathFirst);
                        from.renameTo(to);
                    }
                    name = fList.length + 1 + ".jpg";
                }

                Log.d("NAME", name);

                try {
                    String out = Utils.copy(fileName, path, name);
                    File file = new File(fileName);
                    boolean b = file.delete();
                    getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                    Log.d("check", "onAction: copy pohoto delete"+b);
                    if(file.exists())
                    {
                        deleteFile(file);
                    }
                    Intent intent = new Intent(ChooseProcessingActivity.this,
                            EditPagesActivity.class);
                    intent.putExtra("documentPath", out);
                    startActivity(intent);
                    finish();

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("check", "onAction: error deleting "+e);

                }
            }
        }, false, false);

    }

    private void savePhoto() {
        Log.d("processing", "save");
        FlurryAgent.logEvent("EditPhotoSave");
        Log.d("check", "savePhoto: saving photo ");
       // Utils.reportFlurryEvent("EditPhotoActions", "Save");
        if (usePreview() && resultFileName != null) {
            Utils.reportFlurryEvent("SaveHDResolution", "UsePreview");
            Utils.addPhotoToGallery(this, resultFileName);
            Toast.makeText(ChooseProcessingActivity.this,
                    getString(R.string.saved_notification), Toast.LENGTH_SHORT)
                    .show();
            // oldProcessedFile = null;
            // return;
        }

        if (startAppAd.isReady()) {
            startAppAd.showAd();
        }
        saveHDImage(new ActionCallback<String>() {
            @Override
            public void onAction(String fileName) {
                // Toast.makeText(ChooseProcessingActivity.this,
                // getString(R.string.saved_notification),
                // Toast.LENGTH_SHORT).show();
                // rate();
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(fileName))));
            }
        }, false, false);// false
    }

    private void hideAds() {
        FlurryAgent.logEvent("Hide Ads");
        AdView adView = getAdView();
        if (adView != null) {
            adView.setVisibility(View.GONE);
        }
       hideRemoveAdsButton();
    }

    public void deleteFile(File imageFile)
    {

                // Set up the projection (we only need the ID)
                String[] projection = { MediaStore.Images.Media._ID };

                // Match on the file path
                String selection = MediaStore.Images.Media.DATA + " = ?";
                String[] selectionArgs = new String[] { imageFile.getAbsolutePath() };

                // Query for the ID of the media matching the file path
                Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = getApplicationContext().getContentResolver();
                Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
                if (c.moveToFirst()) {
                    // We found the ID. Deleting the item via the content provider will also remove the file
                    long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    contentResolver.delete(deleteUri, null, null);
                } else {
                    // File not found in media store DB
                }
                c.close();
            }



    private class ProcImageAdapter extends BaseAdapter {
        private List<String> paths;
        private Bitmap bitmap;
        private boolean pathWasChanged = false;
        private ImageView procImageView;

        private int width = 0;
        private int height = 0;

        public ProcImageAdapter(Context context) {
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
            pathWasChanged = true;
        }

        @Override
        public int getCount() {
            return paths.size() >= 1 ? 1 : 0;
        }

        @Override
        public Object getItem(int position) {
            return paths.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void onDestroy() {
            try {
                if (procImageView != null) {
                    BitmapDrawable bd = (BitmapDrawable) procImageView
                            .getDrawable();
                    if (bd != null) {
                        Bitmap bitmap = bd.getBitmap();
                        if (bitmap != null) {
                            bitmap.recycle();
                        }
                    }
                }
                if (procImageView != null) {
                    procImageView.setImageDrawable(null);
                    procImageView.setImageBitmap(null);
                    procImageView = null;
                }
                bitmap = null;
            } catch (Exception e) {
                e.printStackTrace();
                new ExceptionHandler(e, "getView::onDestroy");
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.proc_image_item, parent, false);
            }
            ViewGroup layout = (ViewGroup) convertView;

            procImageView = (ImageView) layout
                    .findViewById(R.id.proc_image_item);

            int realWidth = procImageView.getWidth();
            int realHeight = procImageView.getHeight();

            int width = realWidth > 0 ? realWidth : getProcImageViewWidth();
            int height = realHeight > 0 ? realHeight : getProcImageViewHeight();

            if (pathWasChanged || this.width != width || this.height != height) {
                try {
                    BitmapDrawable bd = (BitmapDrawable) procImageView
                            .getDrawable();
                    if (bd != null) {
                        Bitmap bitmap = bd.getBitmap();
                        if (bitmap != null) {
                            bitmap.recycle();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    new ExceptionHandler(e, "getView");
                }
                bitmap = Utils.getThumbnailFromPath((String) getItem(position),
                        width, height);
                Log.d("test", "bitmap addr = " + (String) getItem(position));
                procImageView.setImageBitmap(bitmap);
                pathWasChanged = false;
                this.width = width;
                this.height = height;
            } else {
                procImageView.setImageBitmap(bitmap);
            }

            return layout;
        }
    }

    @Override
    protected String getFlurryKey() {
        return getString(R.string.flurryApiKey);
    }

    @Override
    protected void onItemPurchased(String itemId, boolean isPurchased) {
        if (isPurchased) {
//            try {
//                AppsFlyerLib.sendTrackingWithEvent(getApplicationContext(), getString(R.string.appsFlyerKey), "Purchase", PACK_ALL_ID.equals(itemId) ? "2.99" : "0.99");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            addProductId(itemId);

            List<String> productIds = getProductIds();
            for (int i = 0; i < productIds.size(); i++) {
                manager.unlockByProductId(productIds.get(i));
                if (stickerProductIds != null) {
                    stickerProductIds.remove(productIds.get(i));
                }
                if (cropProductIds != null) {
                    cropProductIds.remove(productIds.get(i));
                }
            }

            if (REMOVE_ADS_PUCHASE_ID.equals(itemId)) {
                manager.unlockByProductId("pack1");
                if (stickerProductIds != null) {
                    stickerProductIds.remove("pack1");
                }
                if (cropProductIds != null) {
                    cropProductIds.remove("crop");
                }
            }
          //  hideAds();
        }
    }

    @Override
    protected String getItemPurchasedNotification(String itemId,
                                                  boolean isPurchased) {
        if (REMOVE_ADS_PUCHASE_ID.equals(itemId)) {
            return isPurchased ? getString(R.string.remove_ads_purchased_ok)
                    : getString(R.string.remove_ads_purchased_error);
        }
        return "";
    }

    @Override
    protected String getBillingUnavailableNotification() {
        return getString(R.string.remove_ads_purchased_error);
    }

    @Override
    protected String getBString() {
        return null;
    }

    @Override
    protected View getRemoveAdsButton() {
        if (isSamsungStoreVersion()) {
            return null;
        } else {
            return findViewById(R.id.remove_ads);
        }
    }

    @Override
    protected String getRemoveAdsPurchaseId() {
        return REMOVE_ADS_PUCHASE_ID;
    }

    @Override
    protected int getRootLayout() {
        return R.id.processing_root_view;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            // sliderLocked = (manager.getCurrPanel().getPanelType()
            // .equals(PanelManager.SLIDER_PANEL_TYPE) && ((SliderPanel) manager
            // .getCurrPanel()).isLocked());
            if (manager.getCurrPanel().getPanelName().contains("start")) {
                if (getString(R.string.workflow).equals("turboscan")) {
                    finish();
                    return false;
                } else {
                    //Toast.makeText(self(), "BACK", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            } else {
                String currPanelType = manager.getCurrPanel().getPanelType();
                if (currPanelType.equals(PanelManager.SLIDER_PANEL_TYPE)) {
                    sliderLocked = ((SliderPanel) manager.getCurrPanel())
                            .isLocked();
                } else if (currPanelType
                        .equals(PanelManager.SLIDERS_BAR_PANEL_TYPE)) {
                    sliderLocked = ((SlidersBarsPanel) manager.getCurrPanel())
                            .isLocked();
                } else {
                    sliderLocked = false;
                }

                if (sliderLocked) {
                    // showBuyDialog(((SliderPanel)
                    // manager.getCurrPanel()).getProductIds());
                    if (manager.getCurrPanel() instanceof SliderPanel) {
                        showBuyDialog(((SliderPanel) manager.getCurrPanel())
                                .getProductIds());
                    }
                    if (manager.getCurrPanel() instanceof SlidersBarsPanel) {
                        showBuyDialog(((SlidersBarsPanel) manager
                                .getCurrPanel()).getProductIds());
                    }
                    return false;
                }

                if ((manager.getCurrPanel().getPanelName().contains("holdstickers"))
                        && findViewById(R.id.launcher_screen_container).getVisibility() == View.VISIBLE) {
                    hideGrid();
                    return false;
                }

                if (manager.upLevel()) {
                    ((ImageViewDrawableOverlay) mImageView).clearOverlays();
                    disableCrop();
                    disableFocus();
                    return false;
                } else {
                    //Utils.reportFlurryEvent("BackPressed", this.toString());
                    Log.d("processing", "BackPressed");
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    // =====================other=================
    private void createAndConfigurePreview(String fileName, boolean removed) {
        try {

            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
            }
            if (mBlurBitmap != null && !mBlurBitmap.isRecycled()) {
                mBlurBitmap.recycle();
            }
            int realWidth = mImageView.getWidth();
            int realHeight = mImageView.getHeight();

            int width = realWidth > 0 ? realWidth : getProcImageViewWidth();
            int height = realHeight > 0 ? realHeight : getProcImageViewHeight();

            mBitmap = Utils.getThumbnailFromPath(fileName, width, height);

            if (mPreview != null && !mPreview.isRecycled()) {
                mPreview.recycle();
                mPreview = null;
            }

            // onClearCurrent(removed);

            if (mBitmap != null) {
                mImageView.setImageBitmap(mBitmap, null, -1, 1);
                mPreview = BitmapUtils.copy(mBitmap, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mPreview);
            }
            // mImageView.setImageBitmap( mBitmap, null, -1,
            // com.aviary.android.feather.library.utils.UIConfiguration.IMAGE_VIEW_MAX_ZOOM
            // );
        } catch (Exception e) {
            e.printStackTrace();
            FlurryAgent.logEvent("CreateAndConfigurePreviewError");
            new ExceptionHandler(e, "CreateAndConfigurePreviewError");
        }
    }

    /**
     * Recycle and free the preview bitmap.
     */
    protected void recyclePreview() {
        if (mPreview != null && !mPreview.isRecycled()
                && !mPreview.equals(mBitmap)) {
            // mLogger.warning( "[recycle] preview Bitmap: " + mPreview );
            mPreview.recycle();
        }
    }

    /**
     * On preview changed.
     *
     * @param bitmap the bitmap
     * @param reset  if true, then will ask to reset the imageview to its original
     *               matrix otherwise the old matrix will be applied
     * @param notify the notify
     */
    public void onPreviewChanged(Bitmap bitmap, boolean reset, boolean notify) {
        // setIsChanged( bitmap != null );

        if (bitmap == null || !bitmap.equals(mPreview)) {
            recyclePreview();
        }

        mPreview = bitmap;

        // if ( notify && isActive() ) {
        // Message msg = mListenerHandler.obtainMessage( PREVIEW_BITMAP_CHANGED
        // );
        // msg.obj = bitmap;
        // msg.arg1 = reset ? 1 : 0;
        // mListenerHandler.sendMessage( msg );
        // }

        // if ( mListener != null && notify && isActive() )
        // mListener.onPreviewChange(
        // bitmap );
    }

    /**
     * Remove the current sticker.
     *
     * @param removed - true if the current sticker is being removed, otherwise it
     *                was flattened
     */
    private void onClearCurrent(boolean removed) {
        // mLogger.info( "onClearCurrent. removed=" + removed );

        if (stickersOnScreen()) {
            final ImageViewDrawableOverlay image = (ImageViewDrawableOverlay) mImageView;
            final DrawableHighlightView hv = image.getHighlightViewAt(0);
            onClearCurrent(hv, removed);
        }
    }

    /**
     * Removes the current active sticker.
     *
     * @param hv      - the {@link DrawableHighlightView} of the active sticker
     * @param removed - current sticker is removed
     */
    private void onClearCurrent(DrawableHighlightView hv, boolean removed) {

        // mLogger.info( "onClearCurrent. hv=" + hv + ", removed=" + removed );

        // if ( mCurrentFilter != null ) {
        // mCurrentFilter = null;
        // }

        if ((removed) && (hv != null)) {
            hv.setOnDeleteClickListener(null);
            ((ImageViewDrawableOverlay) mImageView).removeHightlightView(hv);
            // ((ImageViewDrawableOverlay) mImageView).invalidate();
        }
    }

    /**
     * Return true if there's at least one active sticker on screen.
     *
     * @return true, if successful
     */
    private boolean stickersOnScreen() {
        final ImageViewDrawableOverlay image = (ImageViewDrawableOverlay) mImageView;
        return image.getHighlightCount() > 0;
    }

    // ==============================Drag and drop

    @Override
    public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
                              int yOffset, DragView dragView, Object dragInfo) {
        return source == this;
    }

    @Override
    public void onDrop(DragSource source, int x, int y, int xOffset,
                       int yOffset, DragView dragView, Object dragInfo) {
        if (dragInfo != null && dragInfo instanceof String) {
            String sticker = (String) dragInfo;
            // onApplyCurrent(stickerName);

            float scaleFactor = dragView.getScaleFactor();

            float w = dragView.getWidth();
            float h = dragView.getHeight();

            int width = (int) (w / scaleFactor);
            int height = (int) (h / scaleFactor);

            int targetX = (int) (x - xOffset);
            int targetY = (int) (y - yOffset);

            RectF rect = new RectF(targetX, targetY, targetX + width, targetY
                    + height);
            // stickerService.addSticker(sticker, rect);
        }
    }

    public void disableCrop() {
        mImageView.setVisibility(View.VISIBLE);
        mCropImageView.setVisibility(View.GONE);
    }

    public void disableFocus() {
	/*	Log.d("FOCUS", "DISABLE");
		mImageView.setVisibility(View.VISIBLE);
		mFocusImageView.setVisibility(View.GONE);
		backgroundImage.setVisibility(View.GONE);
		if (mBlurBitmap != null && !mBlurBitmap.isRecycled()) {
			
			mBlurBitmap.recycle();
			mBlurBitmap = null;
			backgroundImage.setImageBitmap(null);
		}
		mFocusImageView.setHighlightView(null);*/
    }

    public void showFacebookPost() {
        Intent intent = new Intent(this, FacebookActivity.class);
        startActivityForResult(intent, BUY_DIALOG_CODE);
    }

    public void showBuyDialog(List<String> productIds) {
        Intent intent = new Intent(this, FacebookActivity.class);
        intent.putStringArrayListExtra("productIds",
                (ArrayList<String>) productIds);
        startActivityForResult(intent, BUY_DIALOG_CODE);
    }

//	public void OpenSession() {
//		Log.d("facebook", "OpenSession");
//		Session.StatusCallback mCallback = new Session.StatusCallback() {
//
//			@Override
//			public void call(Session session, SessionState state,
//					Exception exception) {
//				// TODO Auto-generated method stub
//				if (exception != null) {
//					exception.printStackTrace();
//				}
//
//				if (session.isOpened()) {
//					Log.d("facebook", "Session opened");
//					sendRequests(session);
//				}
//			}
//		};
//
//		Session.OpenRequest request = new Session.OpenRequest(this);
//		request.setPermissions(Arrays.asList("publish_actions"));
//		request.setCallback(mCallback);
//
//		// get active session
//		Session mFacebookSession = Session.getActiveSession();
//		if (mFacebookSession == null || mFacebookSession.isClosed()) {
//			mFacebookSession = new Session(this);
//		}
//		mFacebookSession.openForPublish(request);
//		Session.setActiveSession(mFacebookSession);
//	}
//
//	private void sendRequests(Session session) {
//		Toast.makeText(getApplicationContext(),
//				getResources().getString(R.string.remove_ads_wait),
//				Toast.LENGTH_LONG).show();
//
//		Bundle parameters = new Bundle();
//
//		parameters.putString(
//				"message",
//				getResources().getString(R.string.facebook_message_to_post)
//						+ " "
//						+ getResources().getString(
//								R.string.facebook_link_to_post));
//
//		Request request = new Request(session, "me/feed", parameters,
//				HttpMethod.POST, new Request.Callback() {
//					public void onCompleted(Response response) {
//						FacebookRequestError error = response.getError();
//						if (error != null) {
//							Utils.reportFlurryEvent("facebook request error",
//									error.getErrorMessage());
//							Log.d("facebook",
//									"error = " + error.getErrorMessage());
//							Toast.makeText(
//									getApplicationContext(),
//									getResources()
//											.getString(
//													R.string.remove_ads_purchased_error),
//									Toast.LENGTH_LONG).show();
//						} else {
//							setFaceBookPosted();
//							hideAds();
//							Toast.makeText(
//									getApplicationContext(),
//									getResources().getString(
//											R.string.remove_ads_purchased_ok),
//									Toast.LENGTH_LONG).show();
//							manager.unlockAll();
//							stickerProductIds.clear();
//							cropProductIds.clear();
//							FlurryAgent.logEvent("facebook post to wall");
//							Log.d("facebook", "complete successful");
//						}
//					}
//				});
//
//		new Request(session, "/me", null, HttpMethod.GET,
//				new Request.Callback() {
//					public void onCompleted(Response response) {
//						if (response.getError() == null) {
//							String username = (String) response
//									.getGraphObject().getProperty("username");
//							Utils.reportFlurryEvent("facebook username",
//									username);
//							Log.d("facebook", "username = " + username);
//						}
//					}
//				}).executeAsync();
//		// Log.d("facebook", request.getGraphPath());
//		Request.executeBatchAsync(request);
//	}

    public boolean isPromoAppInstalled() {
        if (isPromoAppAlreadyChecked()) {
            return true;
        }
        int id = getResources().getIdentifier("promo_app_packagename",
                "string", getPackageName());
        if (id == 0) {
            return false;
        } else {
            String packageName = getResources().getString(id);
            // String packageName = "com.onemanwithcameralomo";
            if ((packageName == null) || (packageName.equals(""))) {
                return false;
            }
            Intent intent = getPackageManager().getLaunchIntentForPackage(
                    packageName);
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

    public boolean isFacebookPosted() {
        SharedPreferences sPref;
        sPref = getSharedPreferences("facebook", MODE_PRIVATE);
        boolean posted = sPref.getBoolean("facebook_posted", false);
        Log.d("facebook", "facebook_posted = " + posted);
        return posted;
    }

    public void setFaceBookPosted() {
        Log.d("facebook", "set facebook_posted to true");
        SharedPreferences sPref;
        sPref = getSharedPreferences("facebook", MODE_PRIVATE);
        Editor ed = sPref.edit();
        ed.putBoolean("facebook_posted", true);
        ed.commit();
    }

    public boolean isSamsungStoreVersion() {
        int id = getResources().getIdentifier("samsung_store",
                "bool", getPackageName());
        if (id == 0) {
            return false;
        } else {
            return getResources().getBoolean(id);
        }
    }

    public boolean isAmazonStoreVersion() {
        int id = getResources().getIdentifier("amazon_store",
                "bool", getPackageName());
        if (id == 0) {
            return false;
        } else {
            return getResources().getBoolean(id);
        }
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

    public void setProductIds(List<String> productIds) {
        SharedPreferences sPref;
        sPref = getSharedPreferences("productIds", MODE_PRIVATE);
        Editor ed = sPref.edit();
        ed.putInt("ProductIdsCount", productIds.size());
        for (int i = 0; i < productIds.size(); i++) {
            String productIdKey = "productId" + i;
            ed.putString(productIdKey, productIds.get(i));
        }
        ed.commit();
    }

    public void addProductId(String productId) {
        List<String> productIds = getProductIds();
        productIds.add(productId);
        setProductIds(productIds);
    }

    public void showCustomToast(String text) {
        int toastTextId = getResources().getIdentifier("filtername_" + text,
                "string", getPackageName());
        //Log.d("AAA", "identifier = " + "filtername_" + text);

        if (toastTextId == 0) {
            return;
        }

        // LayoutInflater inflater = getLayoutInflater();
        // View layout = inflater.inflate(R.layout.custom_toast,
        // (ViewGroup) findViewById(R.id.custom_toast_layout_id));
        // TextView tvText = (TextView) layout.findViewById(R.id.tvText);
        // tvText.setText(toastTextId);
        // Typeface myTypeface = Typeface.createFromAsset(getAssets(),
        // "fonts/toast_font.ttf");
        // tvText.setTypeface(myTypeface);
        // Toast toast = new Toast(getApplicationContext());
        // toast.setDuration(Toast.LENGTH_LONG);
        // toast.setView(layout);
        // toast.show();
        final TextView tvText = (TextView) findViewById(R.id.tvToast);
        tvText.setText(toastTextId);
        Typeface myTypeface = TypefaceManager.createFromAsset(getAssets(),
                "fonts/BebasNeue.otf");
        tvText.setTypeface(myTypeface);
        tvText.setTextColor(Color.rgb(255, 255, 255));

        AlphaAnimation fadeIn = new AlphaAnimation(0.1f, 1.0f);
        final AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);

        fadeIn.setDuration(800);
        fadeIn.setFillAfter(true);
        fadeOut.setDuration(800);
        fadeOut.setFillAfter(true);
        fadeOut.setStartOffset(1500 + fadeIn.getStartOffset());
        fadeIn.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvText.setAnimation(fadeOut);
            }
        });
        tvText.clearAnimation();
        tvText.setAnimation(fadeIn);
    }

    public void showCustomToast(String text, boolean stayOnTop, boolean hideProgress) {
        showCustomToast(text, 800, true, true, stayOnTop, hideProgress);
    }

    public void showCustomToast(String text, boolean stayOnTop) {
        showCustomToast(text, 800, true, true, stayOnTop, true);
    }

    public void showProgressToast(String text) {
        final TextView tvText = (TextView) findViewById(R.id.tvProgressToast);
        tvText.setText(text);

        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/BebasNeue.otf");
        tvText.setTypeface(myTypeface);
        tvText.setTextColor(Color.rgb(255, 255, 255));
        tvText.setVisibility(View.VISIBLE);
    }

    public void hideProgressToast() {
        final TextView tvText = (TextView) findViewById(R.id.tvProgressToast);
        tvText.setVisibility(View.INVISIBLE);
    }

    public void showCustomToast(String text, int duration, boolean useidentifier, boolean fillafter, boolean stayOnTop, boolean hideProgress) {
        if (hideProgress) {
            hideOnePixelProgress(true);
        }
        if (stayOnTop) {

            int toastTextId = 0;
            final TextView tvText = (TextView) findViewById(R.id.tvToast);
            if (useidentifier) {
                toastTextId = getResources().getIdentifier(
                        "filtername_" + text, "string", getPackageName());
                //Log.d("AAA", "identifier = " + "filtername_" + text);

                if (toastTextId == 0) {
                    return;
                } else {
                    toastVisible = true;
                }
                tvText.setText(toastTextId);
            } else {
                tvText.setText(text);
            }

            Typeface myTypeface = TypefaceManager.createFromAsset(getAssets(),
                    "fonts/BebasNeue.otf");
            tvText.setTypeface(myTypeface);
            tvText.setTextColor(Color.rgb(255, 255, 255));

            AlphaAnimation fadeIn = new AlphaAnimation(0.1f, 1.0f);

            fadeIn.setDuration(duration);
            fadeIn.setFillAfter(fillafter);
            tvText.clearAnimation();
            tvText.setAnimation(fadeIn);
        } else {
            showCustomToast(text);
        }
    }

    public void hideCustomToast() {
        if (toastVisible) {
            final TextView tvText = (TextView) findViewById(R.id.tvToast);
            final AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);

            fadeOut.setDuration(800);
            fadeOut.setFillAfter(true);
            tvText.clearAnimation();
            tvText.setAnimation(fadeOut);
            toastVisible = false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BUY_DIALOG_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Log.d("ads", "hideAds on ActivityResult");
                  //  hideAds();
                    //OpenSession();
                } catch (Exception e) {
                    Utils.reportFlurryEvent("facebook session exception",
                            e.toString());
                    e.printStackTrace();
                }
            }
            if (resultCode == Activity.RESULT_FIRST_USER) {
                try {
                    String productId = data.getStringExtra("productId");
                    // makePurchase(getRemoveAdsPurchaseId(), false);
                    makePurchase(productId, false);
                    FlurryAgent.logEvent("unlock app by purchase");
                } catch (Exception e) {
                    Utils.reportFlurryEvent("facebook session exception",
                            e.toString());
                    e.printStackTrace();
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                if ((sliderLocked) && (!showBuyDialogFromSavePhoto)) {
                    // ((SliderPanel)
                    // manager.getCurrPanel()).restoreOriginal(true);
                    manager.getCurrPanel().restoreOriginal(true);
                    manager.upLevel();
                }
            }
            showBuyDialogFromSavePhoto = false;
        } else {
//			try {
//				Session.getActiveSession().onActivityResult(this, requestCode,
//						resultCode, data);
//			} catch (Exception e) {
//				FlurryAgent.logEvent("facebook session does not restore");
//				e.printStackTrace();
//			}
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showOnePixelProgress() {
        if (!first_launch) {
            if (progressTask == null) {
                progressTask = new ProgressTask();
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    progressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    progressTask.execute();
                }
            } else {
                if (!progressTask.isFinished()) {
                    progressTask.cancel(true);
                }
                progressTask = new ProgressTask();
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    progressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    progressTask.execute();
                }
            }
        }
    }

    private void hideOnePixelProgress(boolean cancelled) {
        Log.d("onepixelprogressbar", "hideOnePixelProgress()");
        if (progressTask != null) {
            if (progressTask.isFinished()) {
                progressBarHorProcessing.setProgress(0);
            } else {
                if (cancelled) {
                    Log.d("onepixelprogressbar", "progressTask.cancel(true);");
                    progressTask.cancel(true);
                } else {
                    progressTask.animateTail();
                }
            }
        }
    }

    public class ProgressTask extends AsyncTask<Void, Void, Void> {
        int max_value = 500;
        int stop_point = max_value / 2;
        int duration_first_interval = 750;
        int duration_second_interval = 30000;
        int duration_tail = 500;

        int progress_duration = duration_first_interval / stop_point;
        int slow_progress_duration = duration_second_interval / (max_value - stop_point);
        int quick_progress_duration = duration_tail / max_value;

        boolean cancelled = false;
        private boolean finished = false;
        private boolean animateTail = false;

        public void animateTail() {
            animateTail = true;
        }

        public boolean isFinished() {
            return finished;
        }

        protected void onPreExecute() {
            Log.d("progressBarHorProcessing", "onPreExecute");
            progressBarHorProcessing.setProgress(0);
            progressBarHorProcessing.setMax(max_value);
        }

        ;

        @Override
        protected Void doInBackground(Void... params) {
            while (progressBarHorProcessing.getProgress() < max_value) {
                try {
                    if (isCancelled()) {
                        cancelled = true;
                        publishProgress();
                        return null;
                    }
                    publishProgress();

                    if (progressBarHorProcessing.getProgress() < stop_point) {
                        if (animateTail) {
                            Thread.sleep(quick_progress_duration);
                        } else {
                            Thread.sleep(progress_duration);
                        }
                    } else {
                        if (animateTail) {
                            Thread.sleep(quick_progress_duration);
                        } else {
                            Thread.sleep(slow_progress_duration);
                        }
                    }
                } catch (InterruptedException e) {
                    //silently close
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (cancelled) {
                progressBarHorProcessing.setProgress(0);
            } else {
                progressBarHorProcessing.incrementProgressBy(1);
                int percent = (progressBarHorProcessing.getProgress() * 100) / max_value;
                if (percent == 20) {
                    showCustomToast("step1", 0, true, true, true, false);
                }
                if (percent == 40) {
                    showCustomToast("step2", 0, true, true, true, false);
                }
                if (percent == 60) {
                    showCustomToast("step3", 0, true, true, true, false);
                }
                if (percent == 80) {
                    showCustomToast("step4", 0, true, true, true, false);
                }
                if (percent == 90) {
                    showCustomToast("step5", 0, true, true, true, false);
                }
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            finished = true;
            Log.d("progressBarHorProcessing", "onPostExecute");
            if (animateTail || cancelled) {
                progressBarHorProcessing.setProgress(0);
            } else {
                progressBarHorProcessing.setProgress(max_value);
            }
            hideCustomToast();
        }

        ;

        @Override
        protected void onCancelled() {
            progressBarHorProcessing.setProgress(0);
            hideCustomToast();
            super.onCancelled();
        }
    }


    public Activity self() {
        return this;
    }

    private static boolean copyFile(String inFileName, String outFileName) {
        boolean result = true;
        try {
            InputStream in = new FileInputStream(new File(inFileName));
            OutputStream out = new FileOutputStream(new File(outFileName));
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (IOException e) {
            result = false;
            Utils.reportFlurryEvent("Failed to copy file", e.toString());
            Log.e("AssetsUtils", "Failed to copy file: " + inFileName, e);
        }
        return result;
    }

    private static void copyFile(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public void combineImages() {
        if (getResources().getString(R.string.workflow).equals("hdr")) {
            combineImages(CombinePhotosService.ALGORITHM_GETFIRST, 0);
        } else {
            combineImages(CombinePhotosService.ALGORITHM_SCREEN, 0);
        }
    }

    public void combineImages(int algorithm, int alpha) {
        CombinePhotosTask task = new CombinePhotosTask();
        String origTempFile = Utils.getFolderPath(getApplicationContext().getString(R.string.photoFolder)) + "/temp_orig.jpg";
        if (combineImagePath.equals("")) {
            copyFile(originalFileNames.get(0), origTempFile);
            combineImagePath = originalFileNames.get(1);
        }
        task.execute(origTempFile, combineImagePath, originalFileNames.get(0), algorithm, alpha);
    }

    class CombinePhotosTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("combinephotos", "CombinePhotosTask : onPreExecute()");
        }

        @Override
        protected Void doInBackground(Object... param) {
            String inFileName1 = (String) param[0];
            String inFileName2 = (String) param[1];
            String outFileName = (String) param[2];
            int algorithm = (Integer) param[3];
            int alpha = (Integer) param[4];
            ImageProcessing.combinePhotos(inFileName1, inFileName2, outFileName, algorithm, alpha);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (originalFileNames.size() > 1) {
                originalFileNames.remove(1);
            }
            first_launch = true;
            processImage();
        }
    }
}