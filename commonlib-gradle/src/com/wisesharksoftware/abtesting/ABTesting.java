package com.wisesharksoftware.abtesting;

import com.smsbackupandroid.lib.MarketingHelper;
import com.smsbackupandroid.lib.SettingsHelper;
import com.smsbackupandroid.lib.Utils;

import android.content.Context;

public class ABTesting {
	
	public static void selectWokflow(Context context, ABTest test) {
		setWorkflow(context);
		if (isWorkflowSet(context)) {
			test.onWorkflowSelected(getWorkflow(context));
		}
	}

	public static void setWorkflow(Context context) {
		if (!MarketingHelper.isNewUser(context)) {
			return;
		}
		if (isWorkflowSet(context)) {
			return;
		}
		int type = (int)Math.floor(Math.random() * getWorkflowNumber());
		if (type >= getWorkflowNumber()) {
			type = getWorkflowNumber() - 1;
		}
		SettingsHelper.setInt(context, WORKFLOW_TYPE, type);
		Utils.reportFlurryEvent(WORKFLOW_TYPE, Integer.toString(type));
	}
	
	public static int getWorkflow(Context context) {
		return SettingsHelper.getInt(context, WORKFLOW_TYPE, -1);
	}

	public static boolean isWorkflowSet(Context context) {
		return getWorkflow(context) != -1;
	}
	
	public static int getWorkflowNumber() {
		return workflowNumber;
	}

	public static void setWorkflowNumber(int workflowNumber) {
		ABTesting.workflowNumber = workflowNumber;
	}

	private static int workflowNumber = 1;

	private static final String WORKFLOW_TYPE = "workflow_type";
}
