package com.wisesharksoftware.billing;

import com.wisesharksoftware.billing.Consts.ResponseCode;

public interface IPurchaseObserverListener {

	void onBillingSupported(boolean supported);
	void onPurchased(String itemId);
	void onRefunded(String itemId);
	void onCanceled(String itemId);
	void onNone(String itemId);
	void onRequestPurchaseResponseOk();
	void onRequestPurchaseResponseError(ResponseCode responseCode);
	void onRestoreTransactionsResponseOk();
	void onRestoreTransactionsResponseError(ResponseCode responseCode);
}
