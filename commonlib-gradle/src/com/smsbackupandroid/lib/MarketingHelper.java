package com.smsbackupandroid.lib;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.Intent;
import android.net.Uri;

import com.flurry.android.FlurryAgent;

public class MarketingHelper {
	
	public interface OnFacebookPost {
		public void onSuccessfulPost();
		public void onFailedPost();
	}

	public MarketingHelper(Context context, Activity activity, String rateConditionId) {
		this.context = context;
		this.activity = activity;
		this.rateConditionId = rateConditionId;
	}
	
	public MarketingHelper(Context context, Activity activity) {
		this(context, activity, PREF_RATE_CONDITION);
	}
	
	private AlertDialog createPurchaseButtons(final String purchiseLink)
	{
   		final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
   		alertDialog.setTitle(R.string.trialTitle);
   		alertDialog.setMessage(context.getString(R.string.trialText));
   		
   		FlurryAgent.onEvent("TrialShown");
   		
   		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
   	   			context.getString(R.string.trialYes), 
   	   			new OnClickListener() {
   	   				public void onClick(DialogInterface dialog, int which) {
   	   					SettingsHelper.setBoolean(context, PREF_RATED, true);
   	   					FlurryAgent.onEvent("TrialYes");
   	   					dialog.dismiss();
   	   					Intent intent = new Intent(Intent.ACTION_VIEW); 
   	   					intent.setData(Uri.parse(purchiseLink)); 
   	   					activity.startActivity(intent);
   	   				}
   	   	});
   		
   		

   		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
   				context.getString(R.string.trialNo), 
   	   			new OnClickListener() {
   	   				public void onClick(DialogInterface dialog, int which) {
   	   					SettingsHelper.setBoolean(context, PREF_RATED, true);
   	   					FlurryAgent.onEvent("TrialNo");
   	   					dialog.dismiss();
   	   				}
   	   	});
   		

   		return alertDialog;
		
	}
	 

	public AlertDialog createExpiredDialog(final String purchiseLink) {
		return createPurchaseButtons(purchiseLink);
	}
	
	public AlertDialog createRateDialog(String title, final String packageName) {
   		final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
   		alertDialog.setTitle(title);
   		alertDialog.setMessage(context.getString(R.string.rateText));
   		
   		FlurryAgent.onEvent("RateShown");
   		
   		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
   	   			context.getString(R.string.rateYes), 
   	   			new OnClickListener() {
   	   				public void onClick(DialogInterface dialog, int which) {
   	   					SettingsHelper.setBoolean(context, PREF_RATED, true);
   	   					Utils.reportFlurryEvent("Rate", "Yes");
   	   					dialog.dismiss();
   	   					Intent intent = new Intent(Intent.ACTION_VIEW); 
   	   					intent.setData(Uri.parse(context.getString(R.string.rateUrlPrefix) + packageName)); 
   	   					activity.startActivity(intent);
   	   				}
   	   	});

   		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
   				context.getString(R.string.rateLater), 
   	   			new OnClickListener() {
   	   				public void onClick(DialogInterface dialog, int which) {
   	   					SettingsHelper.setBoolean(context, PREF_RATED, false);
   	   					Utils.reportFlurryEvent("Rate", "Later");
   	   					resetRateCondition();
   	   					dialog.dismiss();
   	   				}
   	   	});

   		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
   				context.getString(R.string.rateNo), 
   	   			new OnClickListener() {
   	   				public void onClick(DialogInterface dialog, int which) {
   	   					SettingsHelper.setBoolean(context, PREF_RATED, true);
   	   					Utils.reportFlurryEvent("Rate", "No");
   	   					dialog.dismiss();
   	   				}
   	   	});
   		
   		alertDialog.setCanceledOnTouchOutside(true);
   		
   		alertDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
  				SettingsHelper.setBoolean(context, PREF_RATED, false);
   				Utils.reportFlurryEvent("Rate", "Cancel");
  				resetRateCondition();
			}
		});

   		return alertDialog;
   	}
    
	public boolean showRate() {
		return !SettingsHelper.getBoolean(context, PREF_RATED, false) && isRateCondition(5);
	}

	public boolean showRate(int rateCondition) {
		return !SettingsHelper.getBoolean(context, PREF_RATED, false) && isRateCondition(rateCondition);
	}
	
	public boolean isRateCondition(int rateCondition) {
		return SettingsHelper.getInt(context, PREF_RATE_CONDITION, 0) > rateCondition;
	}
	
	public void updateRateCondition() {
		int count = SettingsHelper.getInt(context, PREF_RATE_CONDITION, 0);
		SettingsHelper.setInt(context, PREF_RATE_CONDITION, count + 1);
	}
	
	public void resetRateCondition() {
		SettingsHelper.setInt(context, PREF_RATE_CONDITION, 0);
	}
	
	
	public static void setPromoDisplaysNumber(Context context, int displays){
		SettingsHelper.setInt(context, CAMERAS_DISPLAYED, displays);
	}
	
	public static int getPromoDisplaysNumber(Context context, boolean isFirstTime) {
		int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		int previousOpenDay = SettingsHelper.getInt(context, PREVIOUS_OPEN_DAY, -1);
		// day is changed
		if (previousOpenDay != today) {
			SettingsHelper.setInt(context, CAMERAS_DISPLAYED, 0);
			SettingsHelper.setInt(context, PREVIOUS_OPEN_DAY, today);
			previousOpenDay = SettingsHelper.getInt(context, PREVIOUS_OPEN_DAY, -1);
		}
		int installDay = SettingsHelper.getInt(context, APP_INSTALL_DAY, -1);
		if (installDay == -1) {
			SettingsHelper.setInt(context, APP_INSTALL_DAY, today);
			installDay = SettingsHelper.getInt(context, APP_INSTALL_DAY, -1);
		}
		int displays = SettingsHelper.getInt(context, CAMERAS_DISPLAYED, 0);
		if (isFirstTime) {
			SettingsHelper.setInt(context, CAMERAS_DISPLAYED, ++displays);
		}
		return displays;
	}
	
	public static void reportRetantion(Context context, String postfix) {
		try {
			if (SettingsHelper.getBoolean(context, RETANTION_REPORTED, false)) {
				return;
			}
			String versionName = SettingsHelper.getString(context, APP_INSTALL_VERSION, null);
			if (versionName == null) {
				if (SettingsHelper.getInt(context, RETANTION_APP_INSTALL_DAY, -1) != -1) {
					return;
				}
				PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				versionName = pInfo.versionName; 
				SettingsHelper.setString(context, APP_INSTALL_VERSION, versionName);
			}
			long currentTime = (new Date()).getTime();
			long installTime = SettingsHelper.getLong(context, RETANTION_APP_INSTALL_TIME, -1);
			String eventId = RETANTION_EVENT_ID + versionName + (postfix != null ? postfix : "");
			if (installTime == -1) {
				SettingsHelper.setLong(context, RETANTION_APP_INSTALL_TIME, currentTime);
				Utils.reportFlurryEvent(eventId, "install");
			} else {
				FlurryAgent.logEvent("old_user");
				if (!SettingsHelper.getBoolean(context, RETANTION_REPORTED, false)) {
					long hours = (currentTime - installTime) / (1000 * 60 * 60); 
					if (hours >= 24 && hours <= 48) {
						Utils.reportFlurryEvent(eventId, "1_day");
						SettingsHelper.setBoolean(context, RETANTION_REPORTED, true);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "ReportRetantion");
		}

	}
	
	public static boolean isNewUser(Context context) {
		return SettingsHelper.getInt(context, RETANTION_APP_INSTALL_DAY, -1) == -1 && SettingsHelper.getInt(context, APP_INSTALL_DAY, -1) == -1 &&
				SettingsHelper.getLong(context, RETANTION_APP_INSTALL_TIME, -1) == -1;
	}
	
	public static void setBastionTrial(Context context, boolean trial) {
		SettingsHelper.setBoolean(context, BASTION_TRIAL_ID, trial);
	}
/*	
	public static boolean isTrialPeriod(Context context) {
		if (SettingsHelper.getBoolean(context, BASTION_TRIAL_ID, false)) {
			return true;
		}
		if (!context.getResources().getBoolean(R.bool.trial_enabled) && !SettingsHelper.getBoolean(context, TRIAL_PERIOD_ENABLED, false)) {
			return false;
		}
		SettingsHelper.setBoolean(context, TRIAL_PERIOD_ENABLED, true);
		long trialPeriod = 24 * 5; // hours
		long currentTime = (new Date()).getTime();
		long installTime = SettingsHelper.getLong(context, TRIAL_APP_INSTALL_TIME, -1);
		if (installTime == -1) {
			SettingsHelper.setLong(context, TRIAL_APP_INSTALL_TIME, currentTime);
			installTime = currentTime;
		}
		return ((currentTime - installTime) / (1000 * 60 * 60)) < trialPeriod;
	}
*/
	public static boolean isTrialPeriod(Context context) {
		if (SettingsHelper.getBoolean(context, BASTION_TRIAL_ID, false)) {
			return true;
		}
		if (context.getResources().getBoolean(R.bool.trial_enabled)) {
			SettingsHelper.setBoolean(context, TRIAL_PERIOD_ENABLED, true);
			return true;
		} else {
			return SettingsHelper.getBoolean(context, TRIAL_PERIOD_ENABLED, false); 
		}
	}
    
	private Context context;
	private Activity activity;
	private String rateConditionId;

	private static final String PREF_RATED = "rated";
    private static final String PREF_RATE_CONDITION = "rate_condition";
    
    private static final String APP_INSTALL_DAY = "APP_INSTALL_DAY";
    private static final String RETANTION_APP_INSTALL_DAY = "RETANTION_APP_INSTALL_DAY";
    private static final String RETANTION_APP_INSTALL_TIME = "RETANTION_APP_INSTALL_TIME";
    private static final String TRIAL_APP_INSTALL_TIME = "TRIAL_APP_INSTALL_TIME";
    private static final String TRIAL_PERIOD_ENABLED = "TRIAL_PERIOD_ENBLED";
    private static final String APP_INSTALL_VERSION = "APP_INSTALL_VERSION";
    private static final String RETANTION_REPORTED = "RETANTION_REPORTED";
    private static final String RETANTION_EVENT_ID = "Retantion_";
    private static final String PREVIOUS_OPEN_DAY = "PREVIOUS_OPEN_DAY";
	private static final String CAMERAS_DISPLAYED = "CAMERAS_DISPLAYED_SECONDDDD";

	public static final String BASTION_TRIAL_ID = "BASTION_TRIAL_ID";
	public static final String BASTION_TRIAL = "TRIAL";
}
