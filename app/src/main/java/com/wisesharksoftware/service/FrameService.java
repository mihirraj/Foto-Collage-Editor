package com.wisesharksoftware.service;

import java.util.List;

import android.util.Log;
import android.widget.Toast;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.panels.ButtonPanel;
import com.wisesharksoftware.panels.IPanel;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.panels.fragment.grid.GridPagerPanel.OnGridItemClickListener;
import com.wisesharksoftware.panels.okcancel.OKCancelBarsPanel;
import com.wisesharksoftware.panels.okcancel.OKCancelPanel;
import com.wisesharksoftware.service.base.ServicesManager;

public class FrameService extends ShapeService {

	public FrameService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		super(a, m, action, actionGroup);
		// TODO Auto-generated constructor stub
	}

	final OnGridItemClickListener onGridItemClickListener = new OnGridItemClickListener() {

		@Override
		public void onClick(String buttonName, LauncherItemView item,
				boolean lock) {

			// stickerName = buttonName;
			stickerProductIds = item.getProductIds();
			Utils.reportFlurryEvent("StickerChoosed", buttonName);
			chooseProcessing.hideRemoveAdsButton();

			ServicesManager.instance().setCurrentService(FrameService.this);

			addSticker(buttonName, null);

			chooseProcessing.hideGrid();
			if (!lock) {
				stickerProductIds = null;
			}
			IPanel panel = panelManager.getPanel("OKCancelBars");
			if (panel != null) {
				if (item.getItem().getColorable()) {
					((OKCancelBarsPanel) panel).showColorBar();
				} else {
					((OKCancelBarsPanel) panel).hideColorBar();
				}
				if (item.locked && lock) {
					((OKCancelPanel) panel).setLocked(true);
				} else {
					((OKCancelPanel) panel).setLocked(false);
				}

				if (((ButtonPanel) panelManager.getPanel("FramePack"))
						.getItems().size() != 1) {
					panel.setRootPanel(panelManager.getCurrPanel());
					panelManager.ShowPanel("OKCancelBars",
							panelManager.getCurrPanel());					
				} else {
					panel.setRootPanel(panelManager.getCurrPanel());
					panelManager.ShowPanel("OKCancelBars", panelManager.getCurrPanel());
				}

			}

			// addSticker("sd/" + stickerName +
			// ".png", null);
			// processImage();
		}
	};
	OnStateListener onStateListener = new OnStateListener() {

		@Override
		public void onShow(List<LauncherItemView> items) {
			try {
				chooseProcessing.showGrid(
						(ButtonPanel) panelManager.getPanel("FramePack"),
						getActionGroup(), items, onGridItemClickListener);
			} catch (ClassCastException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onHide() {
			chooseProcessing.hideGrid();
		}

	};

	@Override
	public OnStateListener getOnStateListener() {
		return onStateListener;
	};
}
