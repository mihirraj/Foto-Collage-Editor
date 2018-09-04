package com.wisesharksoftware.billing;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Toast;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.smsbackupandroid.lib.R;
import com.smsbackupandroid.lib.SettingsHelper;
import com.smsbackupandroid.lib.Utils;
import com.wisesharksoftware.billing.Consts.ResponseCode;
import com.wisesharksoftware.billing.BillingService;
import com.wisesharksoftware.billing.IPurchaseObserverListener;
import com.wisesharksoftware.billing.ResponseHandler;
import com.wisesharksoftware.billing.SimplePurchaseObserver;
import com.wisesharksoftware.ui.BaseActivity;

public abstract class BillingActivity extends BaseActivity implements IPurchaseObserverListener
{
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
        initBilling();
        View removeAdsButton = getRemoveAdsButton();
        if (removeAdsButton != null) {
        	removeAdsButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					hideRemoveAdsButton();
					makePurchase(getRemoveAdsPurchaseId(), false);
				}
			});
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        View removeAdsButton = getRemoveAdsButton();
        if (removeAdsButton != null) {
        	removeAdsButton.setVisibility(View.INVISIBLE);
        }
        restorePurchases();
        showRemoveAdsButton = !isFullVersion() && isBillingSupported() && !isItemPurchased(this, getRemoveAdsPurchaseId());
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
		   if (mBillingService != null) {
			   mBillingService.unbind();
		   }
		   ResponseHandler.unregister(mPurchaseObserver);
		   stopService(new Intent(this, BillingService.class));
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
    	return billingSupported;
    }
    
	protected void makePurchase(String productId, boolean showProgressDialog) {
		makePurchase(true, productId, showProgressDialog);
	}


	private void makePurchase(boolean isRequestPurchase, String productId, boolean showProgressDialog) {
		try {
			if (mBillingService != null && billingSupported) {
				startBillingTransaction(true, showProgressDialog);
				Log.d("Billing", "Product id="+productId+" isRequest="+isRequestPurchase);
				Utils.reportFlurryEvent("Purchase", productId);
				billingRequest = isRequestPurchase;
				if (billingRequest) {
					lastProductId = productId;
					this.showProgressDialog = showProgressDialog;
					mBillingService.restoreTransactions();
				} else {
					mBillingService.requestPurchase(productId, developerPayload);
				}
			} else {
				Utils.reportFlurryEvent("Purchase", "NotSupported");
				Toast.makeText(this, getBillingUnavailableNotification(), Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			stopBillingTransaction();
			e.printStackTrace();
			new ExceptionHandler(e, "MakePurchase");
		}
	}
    
	@Override
	public void onBillingSupported(boolean supported) {
		billingSupported = supported;
		Utils.reportFlurryEvent("OnBillingSupported", Boolean.toString(supported));
	}

	@Override
	public void onPurchased(String itemId) {
		Log.d(BILLING_LOG_TAG, "onPurchased: " + itemId);
		Utils.reportFlurryEvent("PurchaseStateChanged_"+itemId, "Purchased");
		onPurcahseStateChanged(itemId, true);
	}

	@Override
	public void onRefunded(String itemId) {
		Log.d(BILLING_LOG_TAG, "onRefunded: " + itemId);
		Utils.reportFlurryEvent("PurchaseStateChanged_"+itemId, "Refunded");
		onPurcahseStateChanged(itemId, false);
	}

	@Override
	public void onCanceled(String itemId) {
		Log.d(BILLING_LOG_TAG, "onCanceled: " + itemId);
		Utils.reportFlurryEvent("PurchaseStateChanged_"+itemId, "Canceled");
		onPurcahseStateChanged(itemId, false);
	}

	@Override
	public void onNone(String itemId) {
		Log.d(BILLING_LOG_TAG, "onNone: " + itemId);
		if (itemId != null) {
			Utils.reportFlurryEvent("PurchaseStateChanged_"+itemId, "None");
		}
		onPurcahseStateChanged(itemId, false);
	}

	@Override
	public void onRequestPurchaseResponseOk() {
		Log.d(BILLING_LOG_TAG, "onRequestPurchaseResponseOk");
		Utils.reportFlurryEvent("RequestPurchase", "OK");
		stopBillingTransaction();
	}

	@Override
	public void onRequestPurchaseResponseError(ResponseCode responseCode) {
		Log.d(BILLING_LOG_TAG, "onRequestPurchaseResponseError: " + responseCode);
		Utils.reportFlurryEvent("RequestPurchase", responseCode.toString());
		stopBillingTransaction();
		Toast.makeText(this, getBillingUnavailableNotification(), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onRestoreTransactionsResponseOk() {
		SettingsHelper.setBoolean(this, OPEN_AFTER_INSTALL, true);
		Log.d(BILLING_LOG_TAG, "onRestoreTransactionsResponseOk");
		Utils.reportFlurryEvent("RestoreTransactions", "OK");
		stopBillingTransaction();
		isFirstRestoreTransaction = false;
	}

	@Override
	public void onRestoreTransactionsResponseError(ResponseCode responseCode) {
		Log.d(BILLING_LOG_TAG, "onRestorePurchaseResponseError: " + responseCode);
		Utils.reportFlurryEvent("RestoreTransactions", responseCode.toString());
		stopBillingTransaction();
		if (!isFirstRestoreTransaction && billingRequest && lastProductId != null) {
			makePurchase(false, lastProductId, showProgressDialog);
		}
		isFirstRestoreTransaction = false;
	}
	
	private void initBilling() {
		try {
			mPurchaseObserver = new SimplePurchaseObserver(this, mHandler, this);
		       
			mBillingService = new BillingService(getBString());
		    mBillingService.setContext(this);
		     
		    ResponseHandler.register(mPurchaseObserver);
		    billingSupported = mBillingService.checkBillingSupported();
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "BillingInitError");
			billingSupported = false;
		}
	}

    private void onPurcahseStateChanged(String purchaseId, boolean isPurchased) {
    	if (isPurchased && purchaseId != null) {
			setItemPurchased(purchaseId, isPurchased);
    		onItemPurchased(purchaseId, isPurchased);
    		if (purchaseId == lastProductId) {
    	    	Log.d("Billing", "make 2 productId=" + lastProductId + " null");
    			lastProductId = null;
    		}
    		if (!billingRequest) {
    			Toast.makeText(this, getItemPurchasedNotification(purchaseId, isPurchased), Toast.LENGTH_LONG).show();
    		} 
    	} else {
    		if (billingRequest && lastProductId != null) {
    			makePurchase(false, lastProductId, showProgressDialog);
    	    	Log.d("Billing", "make 1 productId=" + lastProductId + " null");
    			lastProductId = null;
    		} else if (purchaseId != null) {
    			setItemPurchased(purchaseId, isPurchased);
    			onItemPurchased(purchaseId, isPurchased);
    			Toast.makeText(this, getItemPurchasedNotification(purchaseId, isPurchased), Toast.LENGTH_LONG).show();
    		}
    	}
    	//lastProductId = null;
    	showProgressDialog = false;
    }

	private void restorePurchases() {
		try {
			boolean restoredPurchases = SettingsHelper.getBoolean(this,	OPEN_AFTER_INSTALL, false);
			if (!restoredPurchases) {
				isFirstRestoreTransaction = true;
				startBillingTransaction(false, false);
				mBillingService.restoreTransactions();
			}
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "BillingActivity::restorePurchases");
		}
	}

	
	private void startBillingTransaction(boolean request, boolean showProgressDialog) {
		billingRequest = request;
		if (progressDialog != null || !showProgressDialog) {
			return;
		}
		progressDialog = ProgressDialog.show(
                this,
                "",
                getString(R.string.please_wait),
                true,
                true);
	}
	
	private void stopBillingTransaction() {
		try {
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "stopBillingTransaction");
		}
	}
	
	public static boolean isItemPurchased(Context context, String itemId) {
		return SettingsHelper.getBoolean(context, itemId, false);
	}

	private void setItemPurchased(String itemId, boolean isPurchased) {
		SettingsHelper.setBoolean(this, itemId, isPurchased);
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

	
	private Handler mHandler = new Handler();
	private SimplePurchaseObserver mPurchaseObserver;
	private BillingService mBillingService;
	private boolean billingSupported = false;
	private String developerPayload = null;
	private boolean billingRequest = false;
	private ProgressDialog progressDialog;
	private boolean isFirstRestoreTransaction = false;
	private String lastProductId = null;
	private boolean showProgressDialog = false;
	private boolean showRemoveAdsButton = false;
	
	private final static String BILLING_LOG_TAG = "WisesharkBilling";
	private static final String OPEN_AFTER_INSTALL = "OPEN_AFTER_INSTALL";
}