package com.wisesharksoftware.ui;

import com.smsbackupandroid.lib.SettingsHelper;
import com.smsbackupandroid.lib.Utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public abstract class BasePromoActivity extends Activity {

	protected abstract int getLayoutId();

	protected abstract int getPromoImageViewId();

	protected abstract int getCloseImageViewId();

	protected abstract int getDownloadImageViewId();

	protected abstract int getPromoResourceId();

	protected abstract String getPromoEventId();

	protected abstract String getPromoClickedPropertyName();
	
	protected abstract String getPromoLink();

	@Override
	public void onCreate( Bundle savedInstanceState )
    {
    	
        super.onCreate( savedInstanceState );
        setContentView( getLayoutId() );
        
        ImageView view = ( ImageView )this.findViewById( getPromoImageViewId() );
        view.setImageResource(getPromoResourceId());
        
        findViewById(getCloseImageViewId()).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
            	Utils.reportFlurryEvent(getPromoEventId(), "Close");
                finish();
			}
		});

        findViewById(getDownloadImageViewId()).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                SettingsHelper.setBoolean(BasePromoActivity.this, getPromoClickedPropertyName() , true);
    			Utils.reportFlurryEvent(getPromoEventId(), "Download");

                Uri uri = Uri.parse( getPromoLink() );
                Intent viewIntent = new Intent( "android.intent.action.VIEW", uri );
                finish();
                startActivity( viewIntent );
			}
		});

    } 

	@Override
    public void onDestroy() {
    	super.onDestroy();
    	ImageView view = ( ImageView )this.findViewById( getPromoImageViewId() );
    	if (view != null) {
    		view.setImageResource(0);
    	}
    }

}
