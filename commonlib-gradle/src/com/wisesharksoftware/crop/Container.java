package com.wisesharksoftware.crop;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class Container extends FrameLayout{
	private OnContainerSizeChanged onContainerSizeChanged;
	
	public Container(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public Container(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public Container(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void setOnContainerSizeChanged(OnContainerSizeChanged arg){
		onContainerSizeChanged = arg;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (onContainerSizeChanged != null){
			onContainerSizeChanged.onContainerSizeChanged();
		}
	}



	public interface OnContainerSizeChanged{
		public void onContainerSizeChanged();
	}

}
