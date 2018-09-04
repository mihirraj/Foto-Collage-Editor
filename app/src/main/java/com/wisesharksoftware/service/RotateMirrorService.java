package com.wisesharksoftware.service;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.service.base.BaseService;

public class RotateMirrorService extends BaseService {

	public RotateMirrorService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		setChooseProcessing(a);
		setPanelManager(m);
		setAction(action);
		setActionGroup(actionGroup);
	}

	@Override
	public OnItemListener getOnItemListener() {
		return new OnItemListener() {

			@Override
			public void onLockedItemSelected(String buttonName,
					LauncherItemView item) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onItemSelected(LauncherItemView item,
					String buttonName, boolean state) {
				if (buttonName.equals("rotate_left")) {
					int angle = chooseProcessing.processOrder.addRotateLeft();
					chooseProcessing.showCustomToast("rotate_left", true);

					Utils.reportFlurryEvent("Angle ", "" + angle);
					Utils.reportFlurryEvent("Action", "Rotate");
					chooseProcessing.processImage();
				} else if (buttonName.equals("rotate_right")) {
					int angle = chooseProcessing.processOrder.addRotateRight();
					chooseProcessing.showCustomToast("rotate_right", true);

					Utils.reportFlurryEvent("Angle ", "" + angle);
					Utils.reportFlurryEvent("Action", "Rotate");
					chooseProcessing.processImage();
				} else if (buttonName.equals("flip_vertical")) {
					boolean flipHorizontal = chooseProcessing.processOrder
							.addFlipHorizontal();
					chooseProcessing.showCustomToast("flip_horizontal", true);

					Utils.reportFlurryEvent("FlipHorizontal ", ""
							+ flipHorizontal);
					chooseProcessing.processImage();
				} else if (buttonName.equals("flip_horizontal")) {
					boolean flipVertical = chooseProcessing.processOrder
							.addFlipVertical();
					chooseProcessing.showCustomToast("flip_vertical", true);

					Utils.reportFlurryEvent("FlipVertical ", "" + flipVertical);
					Utils.reportFlurryEvent("Action", "Mirror");
					chooseProcessing.processImage();
				}
				return true;
			}
		};
	}
}
