package com.wisesharksoftware.billing;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.billing.Consts.ResponseCode;
import com.wisesharksoftware.billing.IPurchaseObserverListener;
import com.wisesharksoftware.ui.BaseActivity;

public abstract class AmazonActivity extends BaseActivity implements IPurchaseObserverListener
{
	private ProgressDialog progressDialog;
	private boolean showRemoveAdsButton = false;

    abstract protected void onItemPurchased(String itemId, boolean isPurchased);
    
    abstract protected String getItemPurchasedNotification(String itemId, boolean isPurchased);

    abstract protected String getBillingUnavailableNotification();

    abstract protected String getBString();
    
    abstract protected View getRemoveAdsButton();
    
    abstract protected String getRemoveAdsPurchaseId();

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );        
    }

    @Override
    public void onStart()
    {
        super.onStart();
        View removeAdsButton = getRemoveAdsButton();
        if (removeAdsButton != null) {
        	removeAdsButton.setVisibility(View.INVISIBLE);
        }
        showRemoveAdsButton = false;
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	try {
            if (progressDialog != null) {
    			progressDialog.dismiss();
    			progressDialog = null;
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }


    @Override
    public void onStop()
    {
        super.onStop();
    	try {
            if (progressDialog != null) {
    			progressDialog.dismiss();
    			progressDialog = null;
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    		new ExceptionHandler(e, "ResponseHandlerUnregister");
    	}
    }
    
	@Override
	protected void onDestroy() {
	   super.onDestroy();
	   try {
           if (progressDialog != null) {
   				progressDialog.dismiss();
   				progressDialog = null;
           }
	   } catch (Exception e) {
		   e.printStackTrace();
		   new ExceptionHandler(e, "BillingServiceUnbind");
	   }
	 }
    
    protected boolean isBillingSupported() {
    	return false;
    }
    
	protected void makePurchase(String productId, boolean showProgressDialog) {
		
	}
    
	@Override
	public void onBillingSupported(boolean supported) {
	}

	@Override
	public void onPurchased(String itemId) {
	}

	@Override
	public void onRefunded(String itemId) {
	}

	@Override
	public void onCanceled(String itemId) {
	}

	@Override
	public void onNone(String itemId) {
	}

	@Override
	public void onRequestPurchaseResponseOk() {
	}

	@Override
	public void onRequestPurchaseResponseError(ResponseCode responseCode) {
	}

	@Override
	public void onRestoreTransactionsResponseOk() {
	}

	@Override
	public void onRestoreTransactionsResponseError(ResponseCode responseCode) {
	}
	
	public static boolean isItemPurchased(Context context, String itemId) {
		return false;
	}

	public void hideRemoveAdsButton() {
		final View view = getRemoveAdsButton();
		if (view == null) {
			return;
		}
		if (view.getVisibility() != View.VISIBLE) {
			return;
		}
		TranslateAnimation animate = new TranslateAnimation(0,-view.getWidth(),0,0);
		animate.setDuration(500);
		animate.setFillAfter(false);
		animate.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.INVISIBLE);
			}
		});
		view.startAnimation(animate);
	}

	protected void showRemoveAdsButton() {
		final View view = getRemoveAdsButton();
		if (view == null) {
			return;
		}
		if (view.getVisibility() == View.VISIBLE) {
			return;
		}
		if (!showRemoveAdsButton || isItemPurchased(this, getRemoveAdsPurchaseId())) {
			return;
		}
		TranslateAnimation animate = new TranslateAnimation(-view.getWidth(),0,0,0);
		animate.setDuration(500);
		animate.setFillAfter(false);
		animate.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				view.setVisibility(View.VISIBLE);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}	
			@Override
			public void onAnimationEnd(Animation animation) {}
		});
		view.startAnimation(animate);
	}
	
	protected void setShowRemoveAdsButton(boolean showRemoveAdsButton) {
		this.showRemoveAdsButton = showRemoveAdsButton;
	}	
}