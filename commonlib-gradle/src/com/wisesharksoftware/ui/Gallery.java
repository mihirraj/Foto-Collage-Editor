package com.wisesharksoftware.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class Gallery extends android.widget.Gallery
{
    private float speedRate;
    
    public Gallery( Context context )
    {
        super( context );
    }

    public Gallery( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        speedRate = attrs.getAttributeFloatValue( "http://schemas.android.com/apk/res/com.wisesharksoftware.ui", "speed_rate", 1F );
    }

    public Gallery( Context context, AttributeSet attrs, int defStyle )
    {
        super( context, attrs, defStyle );
    }

    @Override
    public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY )
    {
//        int kEvent;
//        if( isScrollingLeft( e1, e2 ) )
//        {
//            kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
//        }
//        else
//        {
//            kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
//        }
//        onKeyDown( kEvent, null );
//        return true;
        return super.onFling(e1, e2, velocityX / 2, velocityY);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int viewsOnScreen = getLastVisiblePosition() - getFirstVisiblePosition();
        if(viewsOnScreen <= 0)
            super.onLayout(changed, l, t, r, b);
    }

    private boolean isScrollingLeft( MotionEvent e1, MotionEvent e2 )
    {
        return e2.getX() > e1.getX();
    }
}
