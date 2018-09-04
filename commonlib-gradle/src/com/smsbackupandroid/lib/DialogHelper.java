package com.smsbackupandroid.lib;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.TypedValue;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DialogHelper {
    public static Dialog createTextDialog(Context context, String content) {
        Builder builder = new AlertDialog.Builder(context);
        builder.setCustomTitle(null);
        builder.setPositiveButton(android.R.string.ok, null);
        WebView webView = new WebView(context);
        webView.loadData(content, "text/html", "utf-8");
        FrameLayout wrapper = new FrameLayout(context);
        wrapper.addView(webView);
        wrapper.setPadding(5, 10, 5, 10);
        builder.setView(wrapper);
        return builder.create();
    }
    
    public static void showFirstTimeHelpDialog(final Context context, 
    											   int contentId, 
    											   final String key) {
    	if (SettingsHelper.getBoolean(context, key, false)) 
    		return;
    	Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.help_title);
        builder.setPositiveButton(android.R.string.ok, null);
        String content = context.getString(contentId);
        final CheckBox checkbox = new CheckBox(context);        
        checkbox.setText(R.string.help_not_show_checkbox);
        LinearLayout wrapper = new LinearLayout(context);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        textView.setText(content);
        wrapper.addView(textView);
        wrapper.addView(checkbox);
        wrapper.setPadding(10, 10, 10, 10);
        builder.setView(wrapper);
        AlertDialog dialog = builder.create();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE,
   	   			context.getString(R.string.help_button_ok), 
   	   			new OnClickListener() {
   	   				public void onClick(DialogInterface dialog, int which) {
	   	   				if (checkbox.isChecked()) {
	   						SettingsHelper.setBoolean(context, key, true);
	   					}
   	   					dialog.dismiss();
   	   				}
   	   	});
        dialog.show();
    }
}
