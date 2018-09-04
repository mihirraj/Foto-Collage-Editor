package com.smsbackupandroid.lib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class ImagesTutorialActivity extends Activity {
    
	private final static String resourcesKey = "images";
	
	private class ImageAdapter extends BaseAdapter {
	    private Context mContext;
	    private int[] mImageIds; 

	    public ImageAdapter(Context c, int[] images) {
	        mContext = c;
	        mImageIds = images;	        
	    }

	    public int getCount() {
	        return mImageIds.length;
	    }

	    public Object getItem(int position) {
	        return position;
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView imageView = new ImageView(mContext);
	        imageView.setImageResource(mImageIds[position]);
	        imageView.setLayoutParams(new Gallery.LayoutParams(
	        		Gallery.LayoutParams.FILL_PARENT, 
	        		Gallery.LayoutParams.FILL_PARENT));
	        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	        return imageView;
	    }
	}
	
	public static Intent getIntent(Context context, int[] imagesList) {
    	Intent intent = new Intent(context, ImagesTutorialActivity.class);
    	intent.putExtra(resourcesKey, imagesList);
    	return intent;
    }
	
	private int selectedIndex = -1;
	
	@Override
    public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.images_gallery);
        final Gallery gallery = (Gallery)findViewById(R.id.gallery);
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        int [] images = extra.getIntArray(resourcesKey);
        final ImageAdapter adapter = new ImageAdapter(this, images);
        gallery.setAdapter(adapter);
        final View leftButton = findViewById(R.id.left_button);
        final View rightButton = findViewById(R.id.right_button);
        gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				selectedIndex = position;
				if (selectedIndex <= 0) {
					leftButton.setVisibility(View.INVISIBLE);
				}
				else {
					leftButton.setVisibility(View.VISIBLE);
				}
				if (selectedIndex >= adapter.getCount() - 1) {
					rightButton.setVisibility(View.INVISIBLE);
				}
				else {
					rightButton.setVisibility(View.VISIBLE);
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				selectedIndex = -1;
				rightButton.setVisibility(View.INVISIBLE);
				leftButton.setVisibility(View.INVISIBLE);
			}
		});
        rightButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (selectedIndex < adapter.getCount() - 1) {
					gallery.setSelection(selectedIndex+1, true);
				}
			}
		});
        leftButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (selectedIndex > 0) {
					gallery.setSelection(selectedIndex-1, true);
				}
			}
		});
	}
}
