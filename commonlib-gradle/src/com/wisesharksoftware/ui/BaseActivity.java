package com.wisesharksoftware.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Surface;
import android.view.WindowManager;
import com.flurry.android.FlurryAgent;

public abstract class BaseActivity extends FragmentActivity
{
    protected int orientation;
    protected int layoutID;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView();
        System.gc();
    }

    @Override
    protected void onPause() 
    {
        super.onPause();
        System.gc();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FlurryAgent.onStartSession( this, getFlurryKey() );
    }

    @Override
    public void onStop()
    {
        super.onStop();
        FlurryAgent.onEndSession( this );
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }

    private void setContentView()
    {
        WindowManager winMan = ( WindowManager )getSystemService( Context.WINDOW_SERVICE );
        if( winMan != null )
        {
            orientation = winMan.getDefaultDisplay().getOrientation();
            switch( orientation )
            {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    layoutID = getPortraitLayout();
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    layoutID = getLandscapeLayout();
                    break;
                default:
                    layoutID = getPortraitLayout();
            }
        }
        setContentView( layoutID );
    }

    protected boolean isFullVersion() {
    	return getPackageName().contains("full");
    }

    abstract protected int getRootLayout();

    abstract protected int getPortraitLayout();
    
    abstract protected int getLandscapeLayout();

    abstract protected String getFlurryKey();
}