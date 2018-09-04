package com.wisesharksoftware.app_photoeditor;

import com.wisesharksoftware.core.Utils;
import com.photostudio.photoeditior.R;
import com.wisesharksoftware.ui.BasePromoActivity;

public class PromoActivity extends BasePromoActivity {

	@Override
	public void onResume() {
		//Utils.reportFlurryEvent("DeviceId", ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId());
		Utils.reportFlurryEvent("onResume", this.toString());
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Utils.reportFlurryEvent("onPause", this.toString());
		super.onPause();
	}	
	
	public static String getClickedPropertyName() {
		return "INSTA_LOMO_HD_PROMO_CLICKED";
	}
	
	@Override
	protected int getLayoutId() {
		return R.layout.promo;
	}

	@Override
	protected int getPromoImageViewId() {
		return R.id.promo_image_view;
	}

	@Override
	protected String getPromoEventId() {
		return "InstaLomoHDPromo";
	}

	@Override
	protected String getPromoClickedPropertyName() {
		return getClickedPropertyName();
	}

	@Override
	protected String getPromoLink() {
		return getString(R.string.insta_lomo_hd_app_link);
	}

	@Override
	protected int getCloseImageViewId() {
		return R.id.close_image_view;
	}

	@Override
	protected int getDownloadImageViewId() {
		return R.id.download_image_view;
	}

	@Override
	protected int getPromoResourceId() {
		return R.drawable.promo;
	}

}
