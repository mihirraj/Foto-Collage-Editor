package com.smsbackupandroid.lib;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Sharing {
	public static void ShowShareDialog(Context context, String filePath) {
		Intent share = new Intent(Intent.ACTION_SEND);
		share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		share.setType("image/jpeg");
		share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
		context.startActivity(share);
	}
}
