package com.wisesharksoftware.panels;

import java.util.List;

import com.smsbackupandroid.lib.R;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.panels.okcancel.IOkCancel;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SliderPanel extends RelativeLayout implements IPanel, IOkCancel {
    public static final String ACTION = "slider";
    private Context context;
    private Structure structure;
    private PanelManager manager;
    private IPanel RootPanel;
    private String panelName;
    private TextView tvLabel;
    private SeekBar seekBar;
    private ImageView sliderLeftImage;
    private ImageView sliderRightImage;
    private boolean enableViews = true;
    private int seekBarOriginalValue = 0;
    private boolean locked = false;
    private PanelInfo panelInfo;
    private IOkCancelListener listener;

    @Override
    public void setPanelInfo(PanelInfo pi) {
        panelInfo = pi;

    }

    @Override
    public String getAction() {
        return panelInfo.getAction();
    }

    @Override
    public int getPriority() {
        return panelInfo.getPriority();
    }

    @Override
    public String getActionGroup() {
        return panelInfo.getActionGroup();
    }

    public SliderPanel(Context context) {
        super(context);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.panel_slider, this);
        findViews();
    }


    public SliderPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.panel_slider, this);
        findViews();
    }

    public SliderPanel(Context context, AttributeSet attrs, boolean hdr) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.panel_slider_hdr, this);
        findViews();
    }

    public SliderPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.panel_slider, this);
        findViews();
    }

    public void findViews() {
        tvLabel = (TextView) findViewById(R.id.tvLabel);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                if (listener != null) {
                    if (panelInfo != null) {
                        listener.onStop(arg0, panelInfo.getName());
                    } else {
                        listener.onStop(arg0);
                    }
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                if (listener != null) {
                    if (panelInfo != null) {
                        listener.onChange(arg0, arg1, arg2, panelInfo.getName());
                    } else {
                        listener.onChange(arg0, arg1, arg2);
                    }
                }

            }
        });
        sliderLeftImage = (ImageView) findViewById(R.id.sliderLeftImage);
        sliderRightImage = (ImageView) findViewById(R.id.sliderRightImage);
    }

    public void setOnSeekBarChangeListener(
            OnSeekBarChangeListener onSeekBarChangeListener) {
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        }
    }

    public int getProgress() {
        if (seekBar != null) {
            return seekBar.getProgress();
        }
        return 0;
    }

    private void addViews() {
        loadStandartViews();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void loadStandartViews() {
        if (((SliderPanelInfo) getPanelInfo()).getCaption().equals("")) {
            tvLabel.setText(this.getPanelName());
        } else {
            tvLabel.setText(((SliderPanelInfo) getPanelInfo()).getCaption());
        }

        String fontPath = (String) tvLabel.getTag();
        if ((fontPath != null) && (fontPath != "")) {
            Typeface myTypeface = Typeface.createFromAsset(context.getAssets(),
                    fontPath);
            tvLabel.setTypeface(myTypeface);
        }

        seekBar.setFocusable(true);
        seekBar.setThumbOffset(8);

        seekBar.setMax(((SliderPanelInfo) getPanelInfo()).getMax());
        seekBarOriginalValue = ((SliderPanelInfo) getPanelInfo()).getProgress();
        seekBar.setProgress(seekBarOriginalValue);
        // seekBar.setPadding(24, 10, 24, 10);
        seekBar.setPadding(24, 0, 24, 0);

        String leftImageName = ((SliderPanelInfo) getPanelInfo())
                .getLeftImage();
        String rightImageName = ((SliderPanelInfo) getPanelInfo())
                .getRightImage();

        int resourceLeftImage = context.getResources().getIdentifier(
                leftImageName, "drawable", context.getPackageName());
        int resourceRightImage = context.getResources().getIdentifier(
                rightImageName, "drawable", context.getPackageName());
        if (sliderLeftImage != null)
            sliderLeftImage.setImageResource(resourceLeftImage);
        if (sliderRightImage != null)
            sliderRightImage.setImageResource(resourceRightImage);

        if ((getPanelInfo().getProductIds()).size() != 0) {
            locked = true;
        }
    }

    public PanelInfo getPanelInfo() {
        for (int i = structure.getPanelsInfo().size() - 1; i >= 0; i--) {
            PanelInfo panel = structure.getPanelsInfo().get(i);
            if (panel.getName().equals(panelName)) {
                return panel;
            }
        }
        return null;
    }

    @Override
    public void setPanelManager(PanelManager manager) {
        this.manager = manager;
    }

    @Override
    public void setRootPanel(IPanel Root) {
        RootPanel = Root;
    }

    @Override
    public Structure getStructure() {
        return structure;
    }

    @Override
    public void setStructure(Structure structure) {
        this.structure = structure;
        if (structure != null) {
            addViews();
        }
    }

    @Override
    public PanelManager getPanelManager() {
        return manager;
    }

    @Override
    public IPanel getRootPanel() {
        return RootPanel;
    }

    @Override
    public void setPanelName(String panelName) {
        this.panelName = panelName;
    }

    @Override
    public String getPanelName() {
        return panelName;
    }

    @Override
    public void showPanel(final IPanel hidePanel) {
        Log.d("AAA", "show SliderPanel");
        bringToFront();

        Animation anim = AnimationUtils.loadAnimation(context, R.anim.showbar);

        setVisibility(View.VISIBLE);
        startAnimation(anim);
        getPanelManager().setCurrPanel(this);

        if (hidePanel != null) {
            hidePanel.hidePanel();
        }
    }

    @Override
    public void hidePanel() {
        Log.d("AAA", "hide SilderPanel");
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.hidebar);
        startAnimation(anim);
        setVisibility(View.GONE);
    }

    @Override
    public String getPanelType() {
        return PanelManager.SLIDER_PANEL_TYPE;
    }

    @Override
    public void enableViews() {
        seekBar.setEnabled(true);
        enableViews = true;
    }

    @Override
    public void disableViews() {
        seekBar.setEnabled(false);
        enableViews = false;
    }

    @Override
    public boolean isViewsEnabled() {
        return enableViews;
    }

    @Override
    public void restoreOriginal() {
        boolean enable;
        enable = seekBar.isEnabled();
        seekBar.setEnabled(false);
        seekBar.setProgress(seekBarOriginalValue);
        seekBar.setEnabled(enable);
    }

    @Override
    public void restoreOriginal(boolean sendChanges) {
        boolean enable;
        enable = seekBar.isEnabled();
        seekBar.setEnabled(sendChanges);
        seekBar.setProgress(seekBarOriginalValue);
        seekBar.setEnabled(enable);
    }

    public void setProgress(int seekBarValue) {
        boolean enable;
        enable = seekBar.isEnabled();
        seekBar.setEnabled(false);
        seekBar.setProgress(seekBarValue);
        seekBar.setEnabled(enable);
    }

    @Override
    public void unlockAll() {
        locked = false;
    }

    @Override
    public void unlockByProductId(String productId) {
        List<String> productIds = (getPanelInfo()
                .getProductIds());

        for (int i = 0; i < productIds.size(); i++) {
            if (productIds.get(i).equals(productId)) {
                locked = false;
            }
        }
    }

    public List<String> getProductIds() {
        return (getPanelInfo().getProductIds());
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setListener(IOkCancelListener listener) {
        this.listener = listener;

    }

    @Override
    public void setOnStateListener(OnStateListener onStatetListener) {

    }

    @Override
    public void setTargetItem(int item) {
        // TODO Auto-generated method stub

    }

}