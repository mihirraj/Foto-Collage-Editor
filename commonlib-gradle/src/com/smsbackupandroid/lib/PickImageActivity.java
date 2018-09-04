package com.smsbackupandroid.lib;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PickImageActivity extends Activity {
	
	public static Intent getIntent(Context context, String folderPath) {
    	Intent intent = new Intent(context, PickImageActivity.class);
    	intent.setData(Uri.fromFile(new File(folderPath)));
    	return intent;
    }
	
	private float dpi;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    DisplayMetrics dm = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(dm);
	    dpi = dm.xdpi;
	    
	    
	    setContentView(R.layout.pick_image);
	    File file = null;
		try {
			file = new File(new URI(getIntent().getData().toString()));
		} catch (URISyntaxException e) {
			setResult(RESULT_CANCELED);
	    	finish();
	    	e.printStackTrace();
	    	return;
		}
	    if (file == null || !file.isDirectory() || !file.exists()) {
	    	setResult(RESULT_CANCELED);
	    	finish();
	    	return;
	    }
	    
	    final File[] files = file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".jpg") || filename.endsWith(".png");
			}
		});
	    
	    final GridView gridview = (GridView) findViewById(R.id.gridview);
	    gridview.setAdapter(new ImageAdapter(this, files));

	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        	Intent data = new Intent();
	        	data.setData(Uri.fromFile((File)gridview.getAdapter().getItem(position)));
	        	setResult(RESULT_OK, data);
	        	finish();
	        }
	    });
	}
	
	public class ImageAdapter extends BaseAdapter {
	    private Context mContext;
	    private File[] mFiles;

	    public ImageAdapter(Context c, File[] files) {
	        mContext = c;
	        mFiles = files;
	    }

	    public int getCount() {
	        return mFiles.length;
	    }
	    
	    public Object getItem(int position) {
	    	return mFiles[position];
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView imageView;
	        Bitmap bitmap = getImageBitmap(mFiles[position]);
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	            imageView = new ImageView(mContext);
	            imageView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.WRAP_CONTENT, 
	            		GridView.LayoutParams.WRAP_CONTENT));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	            imageView.setPadding(8, 8, 8, 8);
	        } else {
	            imageView = (ImageView) convertView;
	        }
	        imageView.setImageBitmap(bitmap);
	        return imageView;
	    }
	    
	    private Bitmap getImageBitmap(File file) { 
            Bitmap bm = null; 
            try { 
            	bm = Utils.loadBitmap(file, true);
            	float thumbInPixels = 120 * (dpi / 160);
            	float k = thumbInPixels / bm.getWidth();
            	bm = Bitmap.createScaledBitmap(bm, 
                		(int)(bm.getWidth() * k), 
                		(int)(bm.getHeight() * k), 
                		true);
           } catch (IOException e) { 
               e.printStackTrace();
           } 
           return bm; 
        } 
	}
}
