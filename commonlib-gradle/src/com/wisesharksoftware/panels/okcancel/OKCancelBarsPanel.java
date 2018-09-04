package com.wisesharksoftware.panels.okcancel;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.smsbackupandroid.lib.R;
import com.wisesharksoftware.panels.IPanel;
import com.wisesharksoftware.panels.Structure;
import com.wisesharksoftware.panels.bars.BarTypes;
import com.wisesharksoftware.panels.bars.ColorPickerBar;
import com.wisesharksoftware.panels.bars.ColorPickerBar.OnColorChangeListener;

public class OKCancelBarsPanel extends OKCancelPanel {



	private LinearLayout barsContainer;
	private ColorPickerBar colorBar;

	private int bars_count;
	private int[] progress;
	private int[] progressSavedValues;
	private int[] max;
	private int[] types;
	private String[] captions;
	private SeekBar[] seekbars;
	

	public OKCancelBarsPanel(Context context) {
		this(context, null, 0);

	}

	public OKCancelBarsPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public OKCancelBarsPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}


	@Override
	protected void findViews() {
		super.findViews();
		barsContainer = (LinearLayout) findViewById(R.id.ok_cancel_bars_Container);

	}

	public void hideBar(int id) {
		View view = barsContainer.getChildAt(id);
		if (view != null) {
			view.setVisibility(GONE);
		}
	}

	public void showBar(int id) {
		View view = barsContainer.getChildAt(id);
		if (view != null) {
			view.setVisibility(VISIBLE);
		}
	}

	public void hideColorBar() {
		if (colorBar != null) {
			colorBar.setVisibility(GONE);
		}
	}

	public void showColorBar() {
		if (colorBar != null) {
			colorBar.setVisibility(VISIBLE);
		}
	}

	@Override
	public void showPanel(IPanel hidePanel) {

		super.showPanel(hidePanel);
		for (int i = 0; i < barsContainer.getChildCount(); i++) {
			View v = barsContainer.getChildAt(i).findViewById(R.id.seekBar);
			if (v != null && v instanceof SeekBar) {
				((SeekBar) v).setProgress(((SeekBar) v).getMax());
			}
		}

		if (colorBar != null) {
			colorBar.hideCursor();
		}
		invalidate();
	}

	@Override
	public void hidePanel() {

		super.hidePanel();
	}

	public void setToDefaultValues() {
		for (int i = 0; i < bars_count; i++) {
			int type = types[i];
			switch (type) {
			case 0:
				if (seekbars[i] != null) {
					seekbars[i].setProgress(progress[i]);
				}
				break;
			case 1:
				break;
			default:
				break;
			}
		}
	}

	public void setToSavedValues() {
		for (int i = 0; i < bars_count; i++) {
			int type = types[i];
			switch (type) {
			case 0:
				if (seekbars[i] != null) {
					seekbars[i].setProgress(progressSavedValues[i]);
				}
				break;
			case 1:
				break;
			default:
				break;
			}
		}
	}

	public void saveValues() {
		for (int i = 0; i < bars_count; i++) {
			int type = types[i];
			switch (type) {
			case 0:
				if (seekbars[i] != null) {
					progressSavedValues[i] = seekbars[i].getProgress();
				}
				break;
			case 1:
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void setStructure(Structure structure) {
		this.structure = structure;

		if (structure != null) {
			LayoutInflater.from(context).inflate(R.layout.panel_ok_cancel_bars,
					this);
			findViews();

			bars_count = ((OkCancelBarsPanelInfo) getPanelInfo()).getBarCount();
			progress = ((OkCancelBarsPanelInfo) getPanelInfo())
					.getBarProgress();
			progressSavedValues = ((OkCancelBarsPanelInfo) getPanelInfo())
					.getBarProgress();
			max = ((OkCancelBarsPanelInfo) getPanelInfo()).getBarMax();

			types = ((OkCancelBarsPanelInfo) getPanelInfo()).getBarTypes();
			captions = ((OkCancelBarsPanelInfo) getPanelInfo())
					.getBarCaptions();
			seekbars = new SeekBar[bars_count];
			if (bars_count != 0) {
				if (progress != null && progress.length == bars_count
						&& max != null && max.length == bars_count) {
					for (int i = 0; i < bars_count; i++) {
						int type = types[i];
						switch (type) {
						case 0:
							RelativeLayout main = (RelativeLayout) View
									.inflate(context, R.layout.panel_slider,
											null);
							TextView name = (TextView) main
									.findViewById(R.id.tvLabel);
							if (captions == null) {
								name.setText("Opacity");
							} else {
								name.setText(captions[i]);
							}
							SeekBar seekBar = (SeekBar) main
									.findViewById(R.id.seekBar);
							seekBar.setMax(max[i]);
							seekBar.setProgress(progress[i]);
							seekBar.setTag(i);
							seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

								@Override
								public void onStopTrackingTouch(SeekBar seekBar) {
									if (listener != null) {
										/*listener.onStopChange(
												(Integer) seekBar.getTag(),
												BarTypes.OPACITY,
												seekBar.getProgress());*/
									}
								}

								@Override
								public void onStartTrackingTouch(SeekBar seekBar) {

								}

								@Override
								public void onProgressChanged(SeekBar seekBar,
										int progress, boolean fromUser) {
									if (listener != null) {
										listener.onChange(
												(Integer) seekBar.getTag(),
												BarTypes.OPACITY, progress);
									}

								}
							});
							seekbars[i] = seekBar;
							barsContainer.addView(main);
							break;
						case 1:
							colorBar = (ColorPickerBar) findViewById(R.id.color_picker);
							colorBar.setVisibility(View.VISIBLE);

							colorBar.setOnColorChangeListener(new OnColorChangeListener() {

								@Override
								public void onChange(int color) {
									if (listener != null) {
										listener.onChange(1,
												BarTypes.COLOR, color);
									}

								}
							});
							break;
						default:
							break;
						}

					}
				}
			}

		}
	}
}
