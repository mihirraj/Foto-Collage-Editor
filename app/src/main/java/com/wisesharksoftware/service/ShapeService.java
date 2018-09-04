package com.wisesharksoftware.service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.aviary.android.feather.headless.utils.IOUtils;
import com.aviary.android.feather.library.graphics.drawable.FeatherDrawable;
import com.aviary.android.feather.library.graphics.drawable.StickerDrawable;
import com.aviary.android.feather.library.tracking.Tracker;
import com.aviary.android.feather.library.utils.MatrixUtils;
import com.flurry.android.FlurryAgent;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Preset;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.SaveImageFilter;
import com.wisesharksoftware.core.filters.StickerFilter;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.ButtonPanel.OnLaunchPanelListener;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.panels.fragment.grid.GridPagerPanel.OnGridItemClickListener;
import com.wisesharksoftware.panels.okcancel.OKCancelBarsPanel;
import com.wisesharksoftware.panels.okcancel.OKCancelPanel;
import com.wisesharksoftware.panels.ButtonPanel;
import com.wisesharksoftware.panels.IPanel;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.service.base.IService;
import com.wisesharksoftware.service.base.ServicesManager;
import com.wisesharksoftware.sticker.DrawableHighlightView;
import com.wisesharksoftware.sticker.ImageViewDrawableOverlay;

public class ShapeService extends StickerService implements IService {
	private static final String TAG = ShapeService.class.getSimpleName();
	private StickerFilter filter;

	public ShapeService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		super(a, m, action, actionGroup);
	}

	@Override
	public void addSticker(String path, RectF position) {
		stickerName = path;
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(path);
		} catch (Exception e) {
			Log.d(TAG, "Shape not found");
		}
		if (stream != null) {
			StickerDrawable d = new StickerDrawable(context.getResources(),
					stream, path, "custom sticker");
			d.setAntiAlias(true);
			d.setDropShadow(false);
			IOUtils.closeSilently(stream);

			Tracker.recordTag(path + ": Selected");

			addSticker(d);

		}
	}

	public DrawableHighlightView getHighlightView() {
		return hv;
	}

	private void addSticker(FeatherDrawable drawable) {
		RectF positionRect = null;
		hv = new DrawableHighlightView(null, 0, drawable);

		// hv.setOnDeleteClickListener(onDeleClickListener);

		Matrix mImageMatrix = mImageView.getImageViewMatrix();

		int cropWidth, cropHeight;
		int x, y;

		final int width = mImageView.getWidth();
		final int height = mImageView.getHeight();

		// width/height of the sticker

		cropWidth = (int) drawable.getCurrentWidth();
		cropHeight = (int) drawable.getCurrentHeight();

		final int cropSize = Math.max(cropWidth, cropHeight);
		final int screenSize = Math.min(mImageView.getWidth(),
				mImageView.getHeight());

		if (cropSize > screenSize) {
			float ratio;
			float widthRatio = (float) mImageView.getWidth() / cropWidth;
			float heightRatio = (float) mImageView.getHeight() / cropHeight;

			if (widthRatio < heightRatio) {
				ratio = widthRatio;
			} else {
				ratio = heightRatio;
			}

			cropWidth = (int) ((float) cropWidth * (ratio / 2));
			cropHeight = (int) ((float) cropHeight * (ratio / 2));

			if (positionRect == null) {
				int w = mImageView.getWidth();
				int h = mImageView.getHeight();
				positionRect = new RectF(w / 2 - cropWidth / 2, h / 2
						- cropHeight / 2, w / 2 + cropWidth / 2, h / 2
						+ cropHeight / 2);
			}

			positionRect.inset((positionRect.width() - cropWidth) / 2,
					(positionRect.height() - cropHeight) / 2);
		}

		if (positionRect != null) {
			x = (int) positionRect.left;
			y = (int) positionRect.top;
		} else {
			x = (width - cropWidth) / 2;
			y = (height - cropHeight) / 2;
		}

		Matrix matrix = new Matrix(mImageMatrix);
		matrix.invert(matrix);

		float[] pts = new float[] { x, y, x + cropWidth, y + cropHeight };
		MatrixUtils.mapPoints(matrix, pts);

		RectF cropRect = mImageView.getBitmapRect();
		Rect imageRect = new Rect(0, 0, width, height);

		// hv.setRotateAndScale( rotateAndResize );

		hv.setup(context, new Matrix(), imageRect, cropRect, false);

		// hv.setScaleEnabled(false);
		// hv.setMoveEnabled(false);
		// hv.setScaleEnabled(false);
		mImageView.setDoubleTapEnabled(false);
		mImageView.setScrollEnabled(false);
		mImageView.setScaleEnabled(false);

		((ImageViewDrawableOverlay) mImageView).addHighlightView(hv);
		((ImageViewDrawableOverlay) mImageView).setSelectedHighlightView(hv);
	}

	@Override
	public ArrayList<Preset> getFilterPreset() {
		ArrayList<Preset> PresetArray = new ArrayList<Preset>();
		// if (!stickersOnScreen())
		// return null;
		Log.d("HV", hv + "");
		if (filter != null) {

			ArrayList<Filter> FilterArray = new ArrayList<Filter>();
			FilterArray.add(filter);

			Filter[] filters = new Filter[FilterArray.size()];
			FilterArray.toArray(filters);

			Preset preset = new Preset();
			preset.setFilters(filters);

			PresetArray.add(preset);
			return PresetArray;
		}
		if (hv != null && chooseProcessing != null
				&& chooseProcessing.mBitmap != null) {
			hv.setSelected(false);
			final StickerDrawable stickerDrawable = ((StickerDrawable) hv
					.getContent());
			Log.d("SD", stickerDrawable + "");
			if (stickerDrawable != null) {
				RectF cropRect = hv.getCropRectF();
				Rect rect = new Rect((int) cropRect.left, (int) cropRect.top,
						(int) cropRect.right, (int) cropRect.bottom);

				Matrix rotateMatrix = hv.getCropRotationMatrix();
				Matrix matrix = new Matrix(mImageView.getImageMatrix());
				if (!matrix.invert(matrix)) {
				}

				int saveCount = 0;

				stickerDrawable.setDropShadow(false);
				hv.getContent().setBounds(rect);

				mImageView.invalidate();

				final int w = chooseProcessing.mBitmap.getWidth();
				final int h = chooseProcessing.mBitmap.getHeight();
				double left = 0 / w;
				double top = 0 / h;
				double right = 1;
				double bottom = 1;
				int dw = stickerDrawable.getBitmapWidth();
				int dh = stickerDrawable.getBitmapHeight();
				double scalew = (double) w / (double) dw;
				double scaleh = (double) h / (double) dh;

				double stickerX = (left + right) / 2.0;
				double stickerY = (top + bottom) / 2.0;
				double stickerScaleW = scalew / (double) w;
				double stickerScaleH = scaleh / (double) h;

				Log.d("sticker", "Cleft=" + cropRect.left);
				Log.d("sticker", "Ctop=" + cropRect.top);
				Log.d("sticker", "Cright=" + cropRect.right);
				Log.d("sticker", "Cbottom=" + cropRect.bottom);
				Log.d("sticker", "imageW=" + w);
				Log.d("sticker", "imageH=" + h);
				Log.d("sticker", "left=" + left);
				Log.d("sticker", "top=" + top);
				Log.d("sticker", "right=" + right);
				Log.d("sticker", "bottom=" + bottom);
				Log.d("sticker", "stickerW=" + dw);
				Log.d("sticker", "stickerH=" + dh);
				Log.d("sticker", "scaleW=" + scalew);
				Log.d("sticker", "scaleH=" + scaleh);
				Log.d("sticker", "stickerX=" + stickerX);
				Log.d("sticker", "stickerY=" + stickerY);
				Log.d("sticker", "stickerScaleW=" + stickerScaleW);
				Log.d("sticker", "stickerScaleH=" + stickerScaleH);

				filter = new StickerFilter();
				filter.setPath(stickerName.replace(chooseProcessing
						.getExternalFilesDir(null).toString() + "/assets/sd/",
						""));
				filter.setAngle(0);
				filter.setScaleH(stickerScaleH);
				filter.setScaleW(stickerScaleW);
				filter.setX(stickerX);
				filter.setY(stickerY);
				filter.setAlpha(hv.getAlpha());
				filter.setColor(hv.getColor());

				ArrayList<Filter> FilterArray = new ArrayList<Filter>();
				FilterArray.add(filter);

				Filter[] filters = new Filter[FilterArray.size()];
				FilterArray.toArray(filters);

				Preset preset = new Preset();
				preset.setFilters(filters);

				PresetArray.add(preset);
			}
		}
		return PresetArray;
	}

	@Override
	public void onApplyCurrent(String path) {
		filter = null;
		ServicesManager.instance().addToQueue(self());
		chooseProcessing.processImage();
		/*
		 * // mLogger.info( "onApplyCurrent" );
		 * 
		 * try { Log.d("sticker", "apply current"); if (!stickersOnScreen())
		 * return; hv.setSelected(false); final StickerDrawable stickerDrawable
		 * = ((StickerDrawable) hv .getContent()); if (stickerDrawable != null)
		 * { RectF cropRect = hv.getCropRectF(); Rect rect = new Rect((int)
		 * cropRect.left, (int) cropRect.top, (int) cropRect.right, (int)
		 * cropRect.bottom);
		 * 
		 * Matrix rotateMatrix = hv.getCropRotationMatrix(); Matrix matrix = new
		 * Matrix(mImageView.getImageMatrix()); if (!matrix.invert(matrix)) { }
		 * 
		 * int saveCount = 0;
		 * 
		 * stickerDrawable.setDropShadow(false);
		 * hv.getContent().setBounds(rect);
		 * 
		 * mImageView.invalidate();
		 * 
		 * final int w = chooseProcessing.mBitmap.getWidth(); final int h =
		 * chooseProcessing.mBitmap.getHeight(); double left = 0 / w; double top
		 * = 0 / h; double right = 1; double bottom = 1; int dw =
		 * stickerDrawable.getBitmapWidth(); int dh =
		 * stickerDrawable.getBitmapHeight(); double scalew = (double) w /
		 * (double) dw; double scaleh = (double) h / (double) dh;
		 * 
		 * double stickerX = (left + right) / 2.0; double stickerY = (top +
		 * bottom) / 2.0; double stickerScaleW = scalew / (double) w; double
		 * stickerScaleH = scaleh / (double) h;
		 * 
		 * Log.d("sticker", "Cleft=" + cropRect.left); Log.d("sticker", "Ctop="
		 * + cropRect.top); Log.d("sticker", "Cright=" + cropRect.right);
		 * Log.d("sticker", "Cbottom=" + cropRect.bottom); Log.d("sticker",
		 * "imageW=" + w); Log.d("sticker", "imageH=" + h); Log.d("sticker",
		 * "left=" + left); Log.d("sticker", "top=" + top); Log.d("sticker",
		 * "right=" + right); Log.d("sticker", "bottom=" + bottom);
		 * Log.d("sticker", "stickerW=" + dw); Log.d("sticker", "stickerH=" +
		 * dh); Log.d("sticker", "scaleW=" + scalew); Log.d("sticker", "scaleH="
		 * + scaleh); Log.d("sticker", "stickerX=" + stickerX); Log.d("sticker",
		 * "stickerY=" + stickerY); Log.d("sticker", "stickerScaleW=" +
		 * stickerScaleW); Log.d("sticker", "stickerScaleH=" + stickerScaleH);
		 * 
		 * StickerFilter newFilter = new StickerFilter();
		 * newFilter.setPath(path.replace(chooseProcessing
		 * .getExternalFilesDir(null).toString() + "/assets/sd/", ""));
		 * newFilter.setAngle(0); newFilter.setScaleH(stickerScaleW);
		 * newFilter.setScaleW(stickerScaleH); newFilter.setX(stickerX);
		 * newFilter.setY(stickerY); newFilter.setAlpha(hv.getAlpha());
		 * newFilter.setColor(hv.getColor()); Log.d("sticker",
		 * "add newFilter in addStickerFilter");
		 * chooseProcessing.processOrder.addFilter(newFilter, getActionGroup(),
		 * true); }
		 * 
		 * // onClearCurrent( false );
		 * 
		 * chooseProcessing.onPreviewChanged(chooseProcessing.mPreview, false,
		 * false); } catch (Exception e) { e.printStackTrace(); new
		 * ExceptionHandler(e, "ApplyCurrentStickerError");
		 * FlurryAgent.logEvent("ApplyCurrentStickerError"); }
		 */
	}

	@Override
	public void setDrawableHighlightView(DrawableHighlightView view) {
		this.hv = view;

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Shape";
	}

	@Override
	public OnLaunchPanelListener getOnLaunchPanelListener() {
		// TODO Auto-generated method stub
		return null;
	}

	OnStateListener onStateListener = new OnStateListener() {

		@Override
		public void onShow(List<LauncherItemView> items) {
			try {
				chooseProcessing.showGrid(
						(ButtonPanel) panelManager.getPanel("Shapes"),
						getActionGroup(), items, onGridItemClickListener);
			} catch (ClassCastException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onHide() {
			chooseProcessing.hideGrid();
		}

	};;

	@Override
	public OnStateListener getOnStateListener() {
		return onStateListener;
	};

	final OnGridItemClickListener onGridItemClickListener = new OnGridItemClickListener() {

		@Override
		public void onClick(String buttonName, LauncherItemView item,
				boolean lock) {

			// stickerName = buttonName;
			stickerProductIds = item.getProductIds();
			Utils.reportFlurryEvent("StickerChoosed", buttonName);
			chooseProcessing.hideRemoveAdsButton();

			ServicesManager.instance().setCurrentService(ShapeService.this);

			addSticker(buttonName, null);

			chooseProcessing.hideGrid();
			if (!lock) {
				stickerProductIds = null;
			}
			IPanel panel = panelManager.getPanel("OKCancelBars");
			if (panel != null) {
				if (item.getItem().getColorable()) {
					((OKCancelBarsPanel) panel).showColorBar();
				} else {
					((OKCancelBarsPanel) panel).hideColorBar();
				}
				if (item.locked && lock) {
					((OKCancelPanel) panel).setLocked(true);
				} else {

					((OKCancelPanel) panel).setLocked(false);
				}

				if (((ButtonPanel) panelManager.getPanel("Shapes")).getItems()
						.size() != 1) {
					panel.setRootPanel(panelManager.getCurrPanel());
					panelManager.ShowPanel("OKCancelBars",
							panelManager.getCurrPanel());

				} else {
					panel.setRootPanel(panelManager.getCurrPanel());
					panelManager.ShowPanel("OKCancelBars", panelManager.getCurrPanel());
					
//					panel.setRootPanel(panelManager.getPanel("launcherPanel"));
//					panelManager.ShowPanel("OKCancelBars",
//							panelManager.getPanel("launcherPanel"));
				}

			}

			// addSticker("sd/" + stickerName +
			// ".png", null);
			// processImage();
		}
	};

	@Override
	public OnItemListener getOnItemListener() {
		// TODO Auto-generated method stub
		return null;
	}
}
