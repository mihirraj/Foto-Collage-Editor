package com.smsbackupandroid.lib;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {
	public static Toast buildToast(Context context, String message, int iconId, int backgroundId) {
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, 
												LayoutParams.WRAP_CONTENT));
		ImageView image = new ImageView(context);
		image.setImageResource(iconId);
		
		TextView textControl = new TextView(context);
		textControl.setGravity(Gravity.TOP);
		if (backgroundId != 0) {
			textControl.setTextColor(0xFFFFFFFF);
			textControl.setBackgroundResource(backgroundId);				
		}
		textControl.setText(message);
		textControl.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, 
				LayoutParams.WRAP_CONTENT));
	
		layout.addView(image);
		layout.addView(textControl);
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		return toast;
	}
	
	public static Toast buildToast(Context context, int stringId, int iconId, int backgroundId) {
		return buildToast(context, context.getString(stringId), iconId, backgroundId);
	}
}
