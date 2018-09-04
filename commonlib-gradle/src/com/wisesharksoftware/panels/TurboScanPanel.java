package com.wisesharksoftware.panels;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.smsbackupandroid.lib.R;

public class TurboScanPanel extends ButtonPanel {

	public static final int ACTION_BACK = 0;
	public static final int ACTION_SAVE = 1;

	private ImageView back;
	private ImageView save;
	private LinearLayout buttonContainer;
	private OnActionListener onActionListener;
	protected int resBackground = 0;
	protected int resSelectorBack = 0;
	protected int resSelectorSave = 0;

	public interface OnActionListener {
		public void onAction(int a);
	}

	public void setOnActionListener(OnActionListener onActionListener) {
		this.onActionListener = onActionListener;
	}

	public TurboScanPanel(Context context) {
		this(context, null, 0);
	}

	public TurboScanPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TurboScanPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		resBackground = R.drawable.bottom_panel_red;
		resSelectorBack = R.drawable.selector_back;
		resSelectorSave = R.drawable.selector_save;
		setBackgroundResource(resBackground);
	}

	@Override
	protected void addViews() {
		loadStandartViews();
		loadStructureViews();
	}

	private void loadStandartViews() {
		back = new ImageView(context);
		back.setBackgroundResource(resSelectorBack);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (onActionListener != null) {
					onActionListener.onAction(ACTION_BACK);
				}

			}
		});
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(CENTER_VERTICAL);
		params.addRule(ALIGN_PARENT_LEFT);
		// params.leftMargin = 7;
		back.setLayoutParams(params);
		back.setId(1);
		addView(back);

		save = new ImageView(context);
		save.setBackgroundResource(resSelectorSave);
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (onActionListener != null) {
					onActionListener.onAction(ACTION_SAVE);
				}

			}
		});
		params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(CENTER_VERTICAL);
		params.addRule(ALIGN_PARENT_RIGHT);
		//params.rightMargin = 7;
		save.setLayoutParams(params);
		save.setId(2);
		addView(save);

		buttonContainer = new LinearLayout(context);
		params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		buttonContainer.setGravity(Gravity.CENTER);
		params.addRule(CENTER_VERTICAL);
		params.addRule(LEFT_OF, 2);
		params.addRule(RIGHT_OF, 1);
		buttonContainer.setLayoutParams(params);
		addView(buttonContainer);

	}

	private void loadStructureViews() {

		for (int i = structure.getPanelsInfo().size() - 1; i >= 0; i--) {
			PanelInfo panel = structure.getPanelsInfo().get(i);

			if (panel.getName().equals(panelName)) {
				for (int j = panel.getItems().size() - 1; j >= 0; j--) {
					Item item = panel.getItems().get(j);
					addItemView(item, j, buttonContainer, false);
				}
			}
		}
	}
}
