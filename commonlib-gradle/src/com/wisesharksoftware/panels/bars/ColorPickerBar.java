package com.wisesharksoftware.panels.bars;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.smsbackupandroid.lib.R;

public class ColorPickerBar extends LinearLayout {

	public class Color {
		int color;
		View view;

		public Color(int color, View view) {
			this.color = color;
			this.view = view;
		};

		public void setView(View view) {
			this.view = view;
		}

		public View getView() {
			return view;
		}
	}

	public interface OnColorChangeListener {
		public void onChange(int color);
	}

	private LinearLayout colorsContainer;
	private ArrayList<Color> colors;
	private View colorCursor;
	private View colorCursorBackground;
	private int width, height;
	private Context context;
	private boolean init = false;
	private OnColorChangeListener onColorChangeListener;

	public ColorPickerBar(Context context) {
		this(context, null, 0);
		// TODO Auto-generated constructor stub
	}

	public ColorPickerBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public ColorPickerBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		this.context = context;
		float scale = context.getResources().getDisplayMetrics().density;
		height = (int) (context.getResources().getDimension(
				R.dimen.color_bar_height)
				* scale + 0.5f);
		setPadding(3, 3, 3, 10);
		setOrientation(VERTICAL);
		initColors();
	}

	public void setOnColorChangeListener(
			OnColorChangeListener onColorChangeListener) {
		this.onColorChangeListener = onColorChangeListener;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		super.onSizeChanged(w, h, oldw, oldh);

		if (w != 0 && !init) {
			this.width = w;
			init = true;
			post(new Runnable() {

				@Override
				public void run() {
					init(width);

				}
			});

		}
	}

	public void hideCursor() {
		if (colorCursor != null) {

			TranslateAnimation anim = new TranslateAnimation(-1000, -1000, 0, 0);
			anim.setFillAfter(true);
			anim.setDuration(0);
			colorCursor.startAnimation(anim);
			invalidate();
		}
	}

	public void init(int width) {

		this.width = width;

		initCursor();

		colorsContainer = new LinearLayout(context);
		LinearLayout.LayoutParams params = new LayoutParams(
				LayoutParams.WRAP_CONTENT, height);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		params.setMargins(10, 0, 10, 0);
		colorsContainer.setOrientation(LinearLayout.HORIZONTAL);
		addView(colorsContainer, params);
		int colorWidth = (width - 20) / colors.size();

		if (colors != null) {
			for (int i = 0; i < colors.size(); i++) {
				Color c = colors.get(i);
				View colorView = new View(context);
				colorView.setBackgroundColor(c.color);
				params = new LayoutParams(colorWidth, LayoutParams.MATCH_PARENT);
				colorsContainer.addView(colorView, params);
				c.setView(colorView);

			}
		}

		// setOnDragListener(onDragListener);
		setOnTouchListener(onTouchListener);

		invalidate();
	}

	private void initCursor() {
		colorCursor = View.inflate(context, R.layout.view_color_cursor, null);
		colorCursorBackground = colorCursor
				.findViewById(R.id.color_cursor_background);
		colorCursorBackground.setBackgroundColor(android.graphics.Color.WHITE);
		addView(colorCursor);
		colorCursor.setVisibility(INVISIBLE);

	}

	private void initColors() {
		colors = new ArrayList<ColorPickerBar.Color>();
		colors.add(new Color(android.graphics.Color.parseColor("#FFFFFFFF"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FFCCCCCC"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF999999"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF666666"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF333333"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF000000"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FFCCFF66"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FFFFD700"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FFDAA520"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FFB8860B"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF999900"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF666600"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FFADFF2F"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF00FA9A"),
				null));

		colors.add(new Color(android.graphics.Color.parseColor("#FF00FF7F"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF00FF00"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF32CD32"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF3CB371"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF99CCCC"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF66CCCC"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF339999"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF669999"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF006666"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FF336666"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FFFFCCCC"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FFFF9999"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FFFF6666"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FFFF3333"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FFFF0033"),
				null));
		colors.add(new Color(android.graphics.Color.parseColor("#FFCC0033"),
				null));

	}

	OnTouchListener onTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getX() < colorsContainer.getLeft()
					+ colors.get(colors.size() - 1).getView().getLeft()

					+ colors.get(colors.size() - 1).getView().getWidth()
					&& event.getX() > colorsContainer.getLeft()
							+ colors.get(0).getView().getLeft()) {
				/*
				 * colorCursor.setTranslationX(event.getX() -
				 * colorCursor.getWidth() / 2);
				 */
				TranslateAnimation anim = new TranslateAnimation(event.getX()
						- colorCursor.getWidth() / 2, event.getX()
						- colorCursor.getWidth() / 2, 0, 0);
				anim.setFillAfter(true);
				anim.setDuration(0);
				colorCursor.startAnimation(anim);
			}

			for (Color c : colors) {
				if (event.getX() > colorsContainer.getLeft()
						+ c.getView().getLeft()
						&& event.getX() < colorsContainer.getLeft()
								+ c.getView().getLeft()
								+ c.getView().getWidth()) {
					colorCursorBackground.setBackgroundColor(c.color);
					if (onColorChangeListener != null) {
						onColorChangeListener.onChange(c.color);
					}

				}
			}
			if (event.getAction() == 0) {
				colorCursor.setVisibility(VISIBLE);
			} else {
				colorCursor.setVisibility(INVISIBLE);
			}

			return true;
		}
	};
}
