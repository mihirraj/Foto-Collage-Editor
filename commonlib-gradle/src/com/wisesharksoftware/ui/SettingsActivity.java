package com.wisesharksoftware.ui;

import com.flurry.android.FlurryAgent;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.R;
import com.wisesharksoftware.camera.AppSettings;
import com.wisesharksoftware.core.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SettingsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
		AppSettings.setResolutionKey(ctx, Integer.toString(AppSettings.getResolution(ctx)));
		try {
			addPreferencesFromResource(R.xml.settings_activity);
		} catch (Exception e) {
			e.printStackTrace();
		}
        PreferenceManager prefMgr = getPreferenceManager();
        Preference pref = prefMgr.findPreference(AppSettings.RESOLUTION_SETTINGS_KEY);
        pref.setOnPreferenceChangeListener(this);

        pref = prefMgr.findPreference(AppSettings.SAVE_ORIGINAL_SETTINGS_KEY);
        pref.setOnPreferenceChangeListener(this);
        
        pref = prefMgr.findPreference(AppSettings.DO_SQUARE_PHOTO_KEY);
        pref.setOnPreferenceChangeListener(this);
        
        PreferenceScreen prefScreen = (PreferenceScreen)prefMgr.findPreference(DEVELOPERS_SETTINGS_KEY);
        prefScreen.setOnPreferenceChangeListener(this);
        prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				FlurryAgent.logEvent("SettingsDevelopers");
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.wisesharksoftware)));
				startActivity(browserIntent);
				return true;
			}
		});

        prefScreen = (PreferenceScreen)prefMgr.findPreference(MORE_APPS_SETTINGS_KEY);
        prefScreen.setOnPreferenceChangeListener(this);
        prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				FlurryAgent.logEvent("SettingsMoreApps");
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.wisesharksoftware_apps)));
				startActivity(browserIntent);
				return true;
			}
		});

        prefScreen = (PreferenceScreen)prefMgr.findPreference(RATE_SETTINGS_KEY);
        prefScreen.setOnPreferenceChangeListener(this);
        prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				try {
				FlurryAgent.logEvent("SettingsRate");
				Intent intent = new Intent(Intent.ACTION_VIEW); 
				intent.setData(Uri.parse(getString(R.string.rateUrlPrefix) + getPackageName())); 
				startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
					new ExceptionHandler(e, "SettingsRate");
				}
				return true;
			}
		});
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (AppSettings.SAVE_ORIGINAL_SETTINGS_KEY.equals(preference.getKey())) {
			AppSettings.setSaveOriginal(ctx, (Boolean)newValue);
			Utils.reportFlurryEvent("SettingsSaveOriginal", Boolean.toString((Boolean)newValue));
	    	FlurryAgent.logEvent("SettingsChanged");
			return true;
		}
		if (AppSettings.DO_SQUARE_PHOTO_KEY.equals(preference.getKey())) {
			AppSettings.setDoSquarePhoto(ctx, (Boolean)newValue);
			Utils.reportFlurryEvent("SettingsDoSquarePhoto", Boolean.toString((Boolean)newValue));
	    	FlurryAgent.logEvent("SettingsChanged");
			return true;
		}
		if (AppSettings.RESOLUTION_SETTINGS_KEY.equals(preference.getKey())) {
			AppSettings.setResolution(ctx, Integer.valueOf((String)newValue));
			Utils.reportFlurryEvent("SettingsResolution", (String)newValue);
	    	FlurryAgent.logEvent("SettingsChanged");
	    	updateResolutionSummary();
			return true;
		}
		return false;
	}
	
    @Override
    public void onStart() {
    	super.onStart();
    	FlurryAgent.onStartSession(getApplicationContext(), ctx.getString(R.string.flurryApiKey));
    	FlurryAgent.logEvent("SettingsShow");
    	updateResolutionSummary();
    }

    @Override
    public void onStop() {
    	super.onStop();
    	FlurryAgent.onEndSession(getApplicationContext());
    }

    @Override
    public void onResume() {
    	super.onResume();
    	updateResolutionSummary();
    }
    
    private void updateResolutionSummary() {
    	int res = AppSettings.getResolution(ctx);
    	String[] resValues = getResources().getStringArray(R.array.settings_resolution_types);
    	String summary = res < 0 || res >= resValues.length ? resValues[0] : resValues[res];
    	Preference pref = getPreferenceManager().findPreference(AppSettings.RESOLUTION_SETTINGS_KEY);
    	pref.setSummary(summary);
    }

	private Context ctx;
	
	private final static String DEVELOPERS_SETTINGS_KEY = "developers_settings_key";
	private final static String MORE_APPS_SETTINGS_KEY = "more_apps_settings_key";
	private final static String RATE_SETTINGS_KEY = "rate_settings_key";

}
