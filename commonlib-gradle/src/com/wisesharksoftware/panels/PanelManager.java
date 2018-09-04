package com.wisesharksoftware.panels;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.smsbackupandroid.lib.R;
import com.wisesharksoftware.panels.okcancel.OKCancelBarsPanel;
import com.wisesharksoftware.panels.okcancel.OKCancelPanel;
import com.wisesharksoftware.panels.okcancel.SlidersBarsPanel;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class PanelManager extends FrameLayout {
    public static final String LAUNCHER_PANEL_TYPE = "LauncherPanel";
    public static final String EFFECTS_PANEL_TYPE = "effects_panel";
    public static final String BUTTON_PANEL_TYPE = "button_panel";
    public static final String SLIDER_PANEL_TYPE = "slider_panel";
    public static final String SLIDERS_PANEL_TYPE = "sliders_panel";
    public static final String ROTATE_MIRROR_PANEL_TYPE = "rotate_mirror_panel";
    public static final String OK_CANCEL_PANEL_TYPE = "ok_cancel_panel";
    public static final String OK_CANCEL_BAR_PANEL_TYPE = "ok_cancel_bars_panel";
    public static final String SLIDERS_BAR_PANEL_TYPE = "sliders_bars_panel";
    private static final String LOG_TAG = "PanelManager";

    private Context context;
    private AttributeSet attrs;
    private Structure structure;
    private LauncherPanel launcherPanel;
    public List<IPanel> panels = new ArrayList<IPanel>();
    private IPanel currPanel;

    public PanelManager(Context context) {
        super(context);
        this.context = context;
        structure = parseStructure();
        if (structure != null) {
            addViews();
        }
    }

    public PanelManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        this.context = context;
        structure = parseStructure();
        if (structure != null) {
            addViews();
        }
    }

    public PanelManager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.attrs = attrs;
        this.context = context;
        structure = parseStructure();
        if (structure != null) {
            addViews();
        }
    }

    private void addViews() {
        loadStandartViews();
        loadStructureViews();
    }

    private void loadStandartViews() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                (int) LayoutParams.MATCH_PARENT,
                (int) LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        this.setLayoutParams(params);
    }

    private String getStringParam(int paramId) {
        String value = null;
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CategoryPanel);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == paramId) {
                value = a.getString(attr);
            }
        }
        a.recycle();
        return value;
    }

    private Structure parseStructure() {
        Structure structure = null;
        String s;
        String value = getStringParam(R.styleable.CategoryPanel_structure);
        if (value == null) {
            return null;
        }
        int id = context.getResources().getIdentifier(value, "raw",
                context.getPackageName());
        InputStream inputStream = null;
        try {
            inputStream = getResources().openRawResource(id);
            byte[] reader = new byte[inputStream.available()];
            while (inputStream.read(reader) != -1) {
            }
            s = new String(reader);
            StructureParser parser = new StructureParser();
            try {
                structure = parser.parse(s);
                Log.d(LOG_TAG, "structure" + structure.toString());
            } catch (JSONException e) {
                Log.d(LOG_TAG, "parser structure error");
                e.printStackTrace();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }
        return structure;
    }

    private void loadStructureViews() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                (int) LayoutParams.MATCH_PARENT,
                (int) LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        launcherPanel = new LauncherPanel(context, attrs);
        launcherPanel.setPanelName("launcherPanel");
        launcherPanel.setPanelManager(this);
        launcherPanel.setRootPanel(null);
        launcherPanel.setStructure(structure);

        launcherPanel.setLayoutParams(params);
        addView(launcherPanel);
        panels.add(launcherPanel);
        setCurrPanel(launcherPanel);

        for (int i = structure.getPanelsInfo().size() - 1; i >= 0; i--) {
            PanelInfo panelInfo = structure.getPanelsInfo().get(i);
            /*
			 * if (panelInfo.getType().equals(EFFECTS_PANEL_TYPE)) {
			 * effectsPanel = new EffectsPanel(context, attrs);
			 * effectsPanel.setPanelName(panelInfo.getName());
			 * effectsPanel.setPanelManager(this);
			 * effectsPanel.setRootPanel(launcherPanel);
			 * effectsPanel.setStructure(structure);
			 * effectsPanel.setLayoutParams(params);
			 * effectsPanel.setVisibility(View.GONE); for (int j =
			 * panelInfo.getCategories().size() - 1; j >= 0; j--) { Category cat
			 * = panelInfo.getCategories().get(j); for (int k =
			 * structure.getPanelsInfo().size() - 1; k >= 0; k--) { PanelInfo
			 * panelCat = structure.getPanelsInfo().get(k); if
			 * (cat.getName().equals(panelCat.getName())) { if
			 * (panelCat.getType().equals(BUTTON_PANEL_TYPE)) { ButtonPanel
			 * btnPanel = new ButtonPanel(context, attrs);
			 * btnPanel.setPanelName(panelCat.getName());
			 * btnPanel.setPanelManager(this);
			 * btnPanel.setRootPanel(effectsPanel);
			 * btnPanel.setStructure(structure);
			 * btnPanel.setLayoutParams(params);
			 * btnPanel.setVisibility(View.GONE); addView(btnPanel);
			 * panels.add(btnPanel); } } } } addView(effectsPanel);
			 * panels.add(effectsPanel); }
			 */
            IPanel panel = null;

            if (panelInfo.getType().equals(BUTTON_PANEL_TYPE)) {
                panel = new ButtonPanel(context, attrs);
            }
            if (panelInfo.getType().equals(SLIDER_PANEL_TYPE)) {
                panel = new SliderPanel(context, attrs);
            }
            if (panelInfo.getType().equals(SLIDERS_PANEL_TYPE)) {
                panel = new SlidersPanel(context, attrs);
            }
			/*
			 * if (panelInfo.getType().equals(ROTATE_MIRROR_PANEL_TYPE)) { panel
			 * = new RotateMirrorPanel(context, attrs); }
			 */
            if (panelInfo.getType().equals(OK_CANCEL_PANEL_TYPE)) {
                panel = new OKCancelPanel(context, attrs);
            }
            if (panelInfo.getType().equals(OK_CANCEL_BAR_PANEL_TYPE)) {
                panel = new OKCancelBarsPanel(context, attrs);
            }
            if (panelInfo.getType().equals(SLIDERS_BAR_PANEL_TYPE)) {
                panel = new SlidersBarsPanel(context, attrs);
            }
            if (panelInfo.getName().equals("turbo_panel_start")) {
                panel = new TurboScanPanel(context, attrs);
            }
            if (panelInfo.getName().equals("hdr_panel_start")) {
                panel = new TurboScanPanel(context, attrs);
            }
            if (panelInfo.getName().equals("Effects_start")) {
                panel = new TurboScanPanel(context, attrs);
            }

            if (panel != null) {
                panel.setPanelInfo(panelInfo);
                panel.setPanelName(panelInfo.getName());
                panel.setPanelManager(this);
                panel.setRootPanel(launcherPanel);
                panel.setStructure(structure);
                panel.setTargetItem(panelInfo.getTargetItem());

                ((View) panel).setLayoutParams(params);
                ((View) panel).setVisibility(View.GONE);
                addView((RelativeLayout) panel);
                panels.add(panel);
            }
        }
    }

    public void ShowLauncherPanel(final IPanel hidePanel) {
        if (launcherPanel != null) {
            Log.d("AAA", "----->");
            launcherPanel.showPanel(hidePanel);
            setCurrPanel(launcherPanel);
        }
    }

    public void ShowPanel(String name, final IPanel hidePanel) {
        for (int i = panels.size() - 1; i >= 0; i--) {
            if (panels.get(i).getPanelName().equals(name)) {
                Log.d("AAA", "----->");
                panels.get(i).showPanel(hidePanel);
                setCurrPanel(panels.get(i));
                break;
            }
        }
    }

    public IPanel getPanel(String name) {
        IPanel panel = null;
        for (int i = panels.size() - 1; i >= 0; i--) {
            if (panels.get(i).getPanelName().equals(name)) {
                panel = panels.get(i);
                break;
            }
        }
        return panel;
    }

    public boolean upLevel() {
        if (getCurrPanel().getRootPanel() != null) {
            getCurrPanel().getRootPanel().showPanel(getCurrPanel());
            return true;
        }
        return false;
    }

    public IPanel getCurrPanel() {
        return currPanel;
    }

    public void setCurrPanel(IPanel currPanel) {
        this.currPanel = currPanel;
    }

    public void enableViews() {
        for (IPanel panel : panels) {
            panel.enableViews();
        }
    }

    public void disableViews() {
        for (IPanel panel : panels) {
            panel.disableViews();
        }
    }

    public void unlockAll() {
        for (IPanel panel : panels) {
            panel.unlockAll();
        }
    }

    public void unlockByProductId(String productId) {
        for (IPanel panel : panels) {
            panel.unlockByProductId(productId);
        }
    }

    public List<String> getAllProductIds() {
        if (structure == null) {
            return null;
        }
        return structure.getProductIds();
    }
}