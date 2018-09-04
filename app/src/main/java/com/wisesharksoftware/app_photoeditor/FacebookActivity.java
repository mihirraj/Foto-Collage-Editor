package com.wisesharksoftware.app_photoeditor;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.photostudio.photoeditior.R;


public class FacebookActivity extends Activity {
	String productId = "";
	
	@Override
	public void onCreate( Bundle savedInstanceState )
    {
		setResult(Activity.RESULT_CANCELED);
    	
        super.onCreate( savedInstanceState );
        Intent intent = getIntent();
        List<String> productIds = intent.getStringArrayListExtra("productIds");
        int id = 0;
        if (productIds == null) {
        	id = getResources().getIdentifier("buy_dialog1", "layout", getPackageName());
			setContentView(id);			
        } else {        	
        	for (int i = 0 ; i < productIds.size(); i++) {
        		if (productIds.get(i).equals("pack1")) {
        			id = getResources().getIdentifier("buy_dialog1", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("pack2")) {
        			id = getResources().getIdentifier("buy_dialog2", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("pack3")) {
        			id = getResources().getIdentifier("buy_dialog3", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("pack4")) {
        			id = getResources().getIdentifier("buy_dialog4", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("crop")) {
        			id = getResources().getIdentifier("buy_dialog_crop", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("lovepack")) {
        			id = getResources().getIdentifier("buy_dialog_lovepack", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("comicpack")) {
        			id = getResources().getIdentifier("buy_dialog_comicspack", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("framepack")) {
        			id = getResources().getIdentifier("buy_dialog_framepack", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("hipsterspack")) {
        			id = getResources().getIdentifier("buy_dialog_hipsterspack", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("linepack")) {
        			id = getResources().getIdentifier("buy_dialog_linepack", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("hairstylespack")) {
        			id = getResources().getIdentifier("buy_dialog_hairstylespack", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("glassespack")) {
        			id = getResources().getIdentifier("buy_dialog_glassespack", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("cartoonpack")) {
        			id = getResources().getIdentifier("buy_dialog_cartoonpack", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("effects_premium_pack")) {
        			id = getResources().getIdentifier("buy_dialog_effects_premium_pack", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}        		
        		if (productIds.get(i).equals("fisheye_curve")) {
        			id = getResources().getIdentifier("buy_dialog_fisheye", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}
        		if (productIds.get(i).equals("hdr_tool")) {
        			id = getResources().getIdentifier("buy_dialog_hdr_tool", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}        	
        		if (productIds.get(i).equals("eyespack")) {
        			id = getResources().getIdentifier("buy_dialog_eyespack", "layout", getPackageName());
        			setContentView(id);
        			productId = productIds.get(i);
        			break;
        		}        	
        	}
        	if (productId.equals("")) {
        		id = getResources().getIdentifier("buy_dialog1", "layout", getPackageName());
    			setContentView(id);  		
        	}
        }
        
        findViewById(R.id.imgPostToFacebook).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_OK);				
				finish();
			}
		});
        
//        findViewById(R.id.imgPostToFacebookCancel).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				finish();
//			}
//		});
        findViewById(R.id.imgBuy).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				data.putExtra("productId", productId);
				setResult(Activity.RESULT_FIRST_USER, data);
				finish();
			}
		});
    } 
}
