package com.wisesharksoftware.category_panel;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

public class ItemView extends ImageButton{
	private static final String LOG_TAG = "ItemView";
	private boolean state = false;
	private int idOnResourceImage;
	private int idOffResourceImage;
	
	public ItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setState(false);
		// TODO Auto-generated constructor stub
	}

	public ItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ItemView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public ItemView(Context context, int idOnResourceImage, int idOffResourceImage){
		this(context);
		this.idOnResourceImage = idOnResourceImage;
		this.idOffResourceImage = idOffResourceImage;
		setState(false);
	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean state) {
		Log.d(LOG_TAG, "idOnResourceImage = " + idOnResourceImage + " idOffResourceImage = " + idOffResourceImage + "SET STATE");
		/*if (state){
			this.state = state;
		}*/
		this.state = state;
		if (this.state){
			this.setBackgroundResource(idOnResourceImage);
		} else{
			this.setBackgroundResource(idOffResourceImage);
		}
	}

	public int getIdOnResourceImage() {
		return idOnResourceImage;
	}

	public void setIdOnResourceImage(int idOnResourceImage) {
		this.idOnResourceImage = idOnResourceImage;
	}

	public int getIdOffResourceImage() {
		return idOffResourceImage;
	}

	public void setIdOffResourceImage(int idOffResourceImage) {
		this.idOffResourceImage = idOffResourceImage;
	}
	
}
