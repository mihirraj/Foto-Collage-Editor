package com.wisesharksoftware.billing;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.wisesharksoftware.billing.BillingService.RequestPurchase;
import com.wisesharksoftware.billing.BillingService.RestoreTransactions;
import com.wisesharksoftware.billing.Consts.PurchaseState;
import com.wisesharksoftware.billing.Consts.ResponseCode;

public class SimplePurchaseObserver extends PurchaseObserver {

	public SimplePurchaseObserver(Activity activity, Handler handler, IPurchaseObserverListener purchaseListener) {
		super(activity, handler);
		this.purchaseListener = purchaseListener;
	}

	@Override
	public void onBillingSupported(boolean supported) {
		purchaseListener.onBillingSupported(supported);
	}

	@Override
	public void onPurchaseStateChange(PurchaseState purchaseState,
			String itemId, int quantity, long purchaseTime,
			String developerPayload) {
		
        if (purchaseState == PurchaseState.PURCHASED) {
        	
            Log.i("SimplePurchaseObserver", "Successfully purchased");
            purchaseListener.onPurchased(itemId);
            
        } else if (purchaseState == PurchaseState.CANCELED) {
        	
            Log.i("SimplePurchaseObserver", "Purchase canceled");
            purchaseListener.onCanceled(itemId);
            
        } else if (purchaseState == PurchaseState.REFUNDED) {
        	
        	Log.i("SimplePurchaseObserver", "Purchase refunded");
        	purchaseListener.onRefunded(itemId);
        	
        } else if (purchaseState == PurchaseState.NONE) {
        	
        	Log.i("SimplePurchaseObserver", "Don't purchased yet");
        	purchaseListener.onNone(itemId);
        	
        }
	}

	@Override
	public void onRequestPurchaseResponse(RequestPurchase request,
			ResponseCode responseCode) {

		Log.i("SimplePurchaseObserver", "onRequestPurchaseResponse: " + responseCode.toString());
		
		if (responseCode == ResponseCode.RESULT_OK) {
			purchaseListener.onRequestPurchaseResponseOk();
        } else {
			purchaseListener.onRequestPurchaseResponseError(responseCode);
        }	
	}

	@Override
	public void onRestoreTransactionsResponse(RestoreTransactions request,
			ResponseCode responseCode) {

		Log.i("SimplePurchaseObserver", "onRestoreTransactionsResponse: " + responseCode.toString());
		
		if (responseCode == ResponseCode.RESULT_OK) {
			purchaseListener.onRestoreTransactionsResponseOk();
		} else {
			purchaseListener.onRestoreTransactionsResponseError(responseCode);
		}
	}
	
	private IPurchaseObserverListener purchaseListener;
}
