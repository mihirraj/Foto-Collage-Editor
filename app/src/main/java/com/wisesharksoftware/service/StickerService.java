package com.wisesharksoftware.service;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.aviary.android.feather.headless.utils.IOUtils;
import com.aviary.android.feather.library.graphics.drawable.FeatherDrawable;
import com.aviary.android.feather.library.graphics.drawable.StickerDrawable;
import com.aviary.android.feather.library.services.PluginService;
import com.aviary.android.feather.library.services.PluginService.StickerType;
import com.aviary.android.feather.library.tracking.Tracker;
import com.aviary.android.feather.library.utils.MatrixUtils;
import com.flurry.android.FlurryAgent;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.Utils;
import com.wisesharksoftware.core.filters.StickerFilter;
import com.wisesharksoftware.panels.ButtonPanel;
import com.wisesharksoftware.panels.ButtonPanel.OnItemListener;
import com.wisesharksoftware.panels.ButtonPanel.OnLaunchPanelListener;
import com.wisesharksoftware.panels.ButtonPanel.OnStateListener;
import com.wisesharksoftware.panels.IPanel;
import com.wisesharksoftware.panels.LauncherItemView;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.panels.bars.BarTypes;
import com.wisesharksoftware.panels.fragment.grid.GridPagerPanel.OnGridItemClickListener;
import com.wisesharksoftware.panels.okcancel.IOkCancelListener;
import com.wisesharksoftware.panels.okcancel.OKCancelBarsPanel;
import com.wisesharksoftware.panels.okcancel.OKCancelPanel;
import com.wisesharksoftware.service.base.BaseService;
import com.wisesharksoftware.service.base.ServicesManager;
import com.wisesharksoftware.sticker.DrawableHighlightView;
import com.wisesharksoftware.sticker.DrawableHighlightView.OnDeleteClickListener;
import com.wisesharksoftware.sticker.ImageViewDrawableOverlay;
import com.photostudio.photoeditior.R;

public class StickerService extends BaseService {

	private int categoryCount = 0;
	protected Context context;
	protected ImageViewTouch mImageView;
	protected DrawableHighlightView.OnDeleteClickListener onDeleClickListener;
	protected PackageManager packageManager;
	protected WeakReference<Resources> resources;
	protected String packageName;
	protected WeakReference<ApplicationInfo> applicationInfo;
	protected DrawableHighlightView hv;
	protected String stickerName;
	protected List<String> stickerProductIds;	

	public StickerService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		setChooseProcessing(a);
		setPanelManager(m);
		setActionGroup(actionGroup);
		setAction(action);
		context = a;
		this.mImageView = (ImageViewDrawableOverlay) a
				.findViewById(R.id.image_overlay);

		packageName = context.getPackageName();
		packageManager = context.getPackageManager();

	}

	/**
	 * Add a new sticker to the canvas.
	 * 
	 * @param drawable
	 *            - the drawable name
	 */
	protected void addSticker(String drawable, RectF position) {
		stickerName = drawable;
		InputStream stream = null;
		Log.d("Draw", drawable);
		try {
			stream = openStickerAssetStream(drawable, StickerType.Small);
		} catch (Exception e) {
			e.printStackTrace();
			// onGenericError( "Failed to load the selected sticker",
			// android.R.string.ok, null );
			return;
		}

		if (stream != null) {
			StickerDrawable d = new StickerDrawable(context.getResources(),
					stream, drawable, "custom sticker");
			d.setAntiAlias(true);
			d.setDropShadow(false);
			IOUtils.closeSilently(stream);

			Tracker.recordTag(drawable + ": Selected");

			addSticker(d, position);

		}
	}

	public DrawableHighlightView getHighlightView() {
		return hv;
	}

	/**
	 * Adds the sticker.
	 * 
	 * @param drawable
	 *            - the drawable
	 * @param rotateAndResize
	 *            - allow rotate and resize
	 */
	protected void addSticker(FeatherDrawable drawable, RectF positionRect) {

		// mLogger.info( "addSticker: " + drawable + ", position: " +
		// positionRect );

		// setIsChanged( true );

		// DrawableHighlightView hv = new DrawableHighlightView( mImageView,
		// ( (ImageViewDrawableOverlay) mImageView ).getOverlayStyleId(),
		// drawable );
		hv = new DrawableHighlightView(mImageView,
				R.style.AviaryGraphics_StickerHighlightView, drawable);

		hv.setOnDeleteClickListener(new OnDeleteClickListener() {

			@Override
			public void onDeleteClick() {
				if (stickersOnScreen()) {

					hv.setOnDeleteClickListener(null);
					((ImageViewDrawableOverlay) mImageView)
							.removeHightlightView(hv);
					((ImageViewDrawableOverlay) mImageView).invalidate();
				}

				stickerName = "";
				panelManager.upLevel();

			}
		});

		Matrix mImageMatrix = mImageView.getImageViewMatrix();

		int cropWidth, cropHeight;
		int x, y;

		final int width = mImageView.getWidth();
		final int height = mImageView.getHeight();

		// width/height of the sticker
		if (positionRect != null) {
			cropWidth = (int) positionRect.width();
			cropHeight = (int) positionRect.height();
		} else {
			cropWidth = (int) drawable.getCurrentWidth();
			cropHeight = (int) drawable.getCurrentHeight();
		}

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

		RectF cropRect = new RectF(pts[0], pts[1], pts[2], pts[3]);
		Rect imageRect = new Rect(0, 0, width, height);

		// hv.setRotateAndScale( rotateAndResize );
		hv.setup(context, mImageMatrix, imageRect, cropRect, false);

		mImageView.setDoubleTapEnabled(true);
		mImageView.setScrollEnabled(true);
		mImageView.setScaleEnabled(true);
		mImageView.setDoubleTapEnabled(true);
		((ImageViewDrawableOverlay) mImageView).addHighlightView(hv);
		((ImageViewDrawableOverlay) mImageView).setSelectedHighlightView(hv);
	}

	protected InputStream openStickerAssetStream(String name,
			PluginService.StickerType type) throws IOException {
		if ((type == PluginService.StickerType.Small)
				|| (type == PluginService.StickerType.Preview))
			;
		try {
			// return openAsset("stickers" + File.separator + "small" +
			// File.separator +
			// name);

			return openAsset(name);
		} catch (FileNotFoundException localFileNotFoundException1) {
			localFileNotFoundException1.printStackTrace();
			if (type == PluginService.StickerType.Large)
				try {
					return openAsset(name);
				} catch (FileNotFoundException localFileNotFoundException2) {
					return openAsset(name);
				}
		}
		return null;
	}

	protected final InputStream openAsset(String name) throws IOException {
		Resources res = getResourcesSticker();
		if (res != null)
			return new FileInputStream(name);
		return null;
	}

	public final Resources getResourcesSticker() {
		if ((this.resources == null) || (this.resources.get() == null)) {

			ApplicationInfo info = getApplicationInfoSticker();

			if (info != null) {
				Resources res;
				try {
					res = packageManager.getResourcesForApplication(info);
				} catch (PackageManager.NameNotFoundException e) {
					return null;
				}
				this.resources = new WeakReference<Resources>(res);
			} else {
				return null;
			}
		}
		return ((Resources) this.resources.get());
	}

	public final ApplicationInfo getApplicationInfoSticker() {
		if ((this.applicationInfo == null)
				|| (this.applicationInfo.get() == null)) {
			try {
				this.applicationInfo = new WeakReference<ApplicationInfo>(
						this.packageManager.getApplicationInfo(
								this.packageName, 0));
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
		return ((ApplicationInfo) this.applicationInfo.get());
	}

	@Override
	public void setDrawableHighlightView(DrawableHighlightView view) {
		this.hv = view;

	}

	public Filter getFilter(Bitmap mBitmap, String path, Canvas mCanvas) {

		Log.d("sticker", "hv != null");

		hv.setSelected(false);
		final StickerDrawable stickerDrawable = ((StickerDrawable) hv
				.getContent());

		RectF cropRect = hv.getCropRectF();
		Rect rect = new Rect((int) cropRect.left, (int) cropRect.top,
				(int) cropRect.right, (int) cropRect.bottom);

		Matrix rotateMatrix = hv.getCropRotationMatrix();
		Matrix matrix = new Matrix(mImageView.getImageMatrix());
		if (!matrix.invert(matrix)) {
		}

		int saveCount = 0;
		if (mCanvas != null) {
			saveCount = mCanvas.save(Canvas.MATRIX_SAVE_FLAG);
			mCanvas.concat(rotateMatrix);
		}

		stickerDrawable.setDropShadow(false);
		hv.getContent().setBounds(rect);
		if (mCanvas != null) {
			hv.getContent().draw(mCanvas);
			mCanvas.restoreToCount(saveCount);
		}

		mImageView.invalidate();

		final int w = mBitmap.getWidth();
		final int h = mBitmap.getHeight();
		double left = cropRect.left / w;
		double top = cropRect.top / h;
		double right = cropRect.right / w;
		double bottom = cropRect.bottom / h;
		int dw = stickerDrawable.getBitmapWidth();
		int dh = stickerDrawable.getBitmapHeight();
		double scalew = cropRect.width() / dw;
		double scaleh = cropRect.height() / dh;

		double stickerX = (left + right) / 2.0;
		double stickerY = (top + bottom) / 2.0;
		double stickerAngle = Math.toRadians(hv.getRotation());
		double stickerScaleW = scalew / w;
		double stickerScaleH = scaleh / h;

		Log.d("sticker", "Cleft=" + cropRect.left);
		Log.d("sticker", "Ctop=" + cropRect.top);
		Log.d("sticker", "Cright=" + cropRect.right);
		Log.d("sticker", "Cbottom=" + cropRect.bottom);

		Log.d("sticker", "addStickerFilter with name = " + path);
		int alpha = hv.getAlpha();
		int color = hv.getColor();

		StickerFilter newFilter = new StickerFilter();
		newFilter.setPath(path.replace(context.getExternalFilesDir(null)
				.toString() + "/assets/sd/", ""));
		newFilter.setAngle(stickerAngle);
		newFilter.setScaleH(stickerScaleW);
		newFilter.setScaleW(stickerScaleH);
		newFilter.setX(stickerX);
		newFilter.setY(stickerY);
		newFilter.setAlpha(alpha);
		newFilter.setColor(color);
		return newFilter;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Sticker";
	}

	@Override
	public OnLaunchPanelListener getOnLaunchPanelListener() {

		return null;
	}

	@Override
	public OnItemListener getOnItemListener() {
		// TODO Auto-generated method stub
		return null;
	}

	final OnGridItemClickListener onGridItemClickListener = new OnGridItemClickListener() {

		@Override
		public void onClick(String buttonName, LauncherItemView item,
				boolean lock) {

			// stickerName = buttonName;
			stickerProductIds = item.getProductIds();
			Utils.reportFlurryEvent("StickerChoosed", buttonName);
			chooseProcessing.hideRemoveAdsButton();

			ServicesManager.instance().setCurrentService(StickerService.this);

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

				if (((ButtonPanel) panelManager.getPanel("Stickers"))
						.getItems().size() != 1) {
					panel.setRootPanel(panelManager.getCurrPanel());
					panelManager.ShowPanel("OKCancelBars",
							panelManager.getCurrPanel());

				} else {
					panel.setRootPanel(panelManager.getPanel("launcherPanel"));
					panelManager.ShowPanel("OKCancelBars",
							panelManager.getPanel("launcherPanel"));
				}
				
				categoryCount = ((ButtonPanel) panelManager.getPanel("Stickers")).getItems().size();
			}

			// addSticker("sd/" + stickerName +
			// ".png", null);
			// processImage();
		}
	};

	public com.wisesharksoftware.panels.okcancel.IOkCancelListener getOkCancelListener() {
		return new IOkCancelListener() {

			@Override
			public void onRestore() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStop(Object... params) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onOK() {
				Utils.reportFlurryEvent("Action", "Sticker");
				onApplyCurrent(stickerName);
				/*
				 * processOrder.addFilter(stickerService .getFilter(mBitmap,
				 * stickerName, mCanvas));
				 */
				chooseProcessing.processImage();
				/*
				 * ((ImageViewDrawableOverlay) mImageView)
				 * .removeHightlightView(currentService .getHighlightView());
				 */
				
				if (categoryCount > 1) {
					panelManager.upLevel();
				}
				chooseProcessing.hideGrid();
			}

			@Override
			public void onLocked(boolean lock) {
				//Toast.makeText(context, lock + "", Toast.LENGTH_SHORT).show();
				if (!lock || stickerProductIds == null
						|| stickerProductIds.size() == 0) {
					onOK();
					panelManager.upLevel();
				} else {
					boolean locked = true;
					if (stickerProductIds != null) {
						for (String p : stickerProductIds) {
//							Toast.makeText(context, p + "", Toast.LENGTH_SHORT)
//									.show();
							if (chooseProcessing.isItemPurchased(
									chooseProcessing, p)) {
								locked = false;
								break;
							}
						}
					}
					if (locked) {
						chooseProcessing.showBuyDialog(stickerProductIds);
					} else {
						onOK();
						panelManager.upLevel();
					}
				}

			}

			@Override
			public void onChange(Object... params) {
				try {
					BarTypes barType = (BarTypes) params[1];
					int change = (Integer) params[2];
					if (barType == BarTypes.OPACITY) {
						if (hv != null) {
							hv.setAlpha(change);

						}

						mImageView.invalidate();
					} else if (barType == BarTypes.COLOR) {
						if (hv != null) {
							hv.setColor(change);
						}

						mImageView.invalidate();
					}
				} catch (ClassCastException e) {

				}
			}

			@Override
			public void onCancel() {
				((ImageViewDrawableOverlay) mImageView).clearOverlays();
				panelManager.upLevel();
			}

			@Override
			public void onShow() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onChangeFromUser(Object... params) {
				// TODO Auto-generated method stub
				
			}
		};
	};

	OnStateListener onStateListener = new OnStateListener() {

		@Override
		public void onShow(List<LauncherItemView> items) {
			Log.d("PANEL", "SHOW " + panelManager.getCurrPanel().getPanelName());
			try {
				chooseProcessing.showGrid(
						(ButtonPanel) panelManager.getCurrPanel(),
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

	public OnStateListener getOnStateListener() {
		return onStateListener;
	};

	/**
	 * Flatten the current sticker within the preview bitmap. No more changes
	 * will be possible on this sticker.
	 */
	public void onApplyCurrent(String path) {

		// mLogger.info( "onApplyCurrent" );

		try {
			Log.d("sticker", "apply current");
			if (!stickersOnScreen())
				return;
			Log.d("sticker", "sticker on screen");

			final DrawableHighlightView hv = ((ImageViewDrawableOverlay) mImageView)
					.getHighlightViewAt(0);

			if (hv != null) {
				Log.d("sticker", "hv != null");

				hv.setSelected(false);
				final StickerDrawable stickerDrawable = ((StickerDrawable) hv
						.getContent());

				RectF cropRect = hv.getCropRectF();
				Rect rect = new Rect((int) cropRect.left, (int) cropRect.top,
						(int) cropRect.right, (int) cropRect.bottom);

				Matrix rotateMatrix = hv.getCropRotationMatrix();
				Matrix matrix = new Matrix(mImageView.getImageMatrix());
				if (!matrix.invert(matrix)) {
				}

				int saveCount = 0;
				if (chooseProcessing.mCanvas != null) {
					saveCount = chooseProcessing.mCanvas
							.save(Canvas.MATRIX_SAVE_FLAG);
					chooseProcessing.mCanvas.concat(rotateMatrix);
				}

				stickerDrawable.setDropShadow(false);
				hv.getContent().setBounds(rect);
				if (chooseProcessing.mCanvas != null) {
					hv.getContent().draw(chooseProcessing.mCanvas);
					chooseProcessing.mCanvas.restoreToCount(saveCount);
				}

				mImageView.invalidate();

				final int w = chooseProcessing.mBitmap.getWidth();
				final int h = chooseProcessing.mBitmap.getHeight();
				double left = cropRect.left / w;
				double top = cropRect.top / h;
				double right = cropRect.right / w;
				double bottom = cropRect.bottom / h;
				int dw = stickerDrawable.getBitmapWidth();
				int dh = stickerDrawable.getBitmapHeight();
				double scalew = cropRect.width() / dw;
				double scaleh = cropRect.height() / dh;

				double stickerX = (left + right) / 2.0;
				double stickerY = (top + bottom) / 2.0;
				double stickerAngle = Math.toRadians(hv.getRotation());
				double stickerScaleW = scalew / w;
				double stickerScaleH = scaleh / h;

				Log.d("sticker", "Cleft=" + cropRect.left);
				Log.d("sticker", "Ctop=" + cropRect.top);
				Log.d("sticker", "Cright=" + cropRect.right);
				Log.d("sticker", "Cbottom=" + cropRect.bottom);

				Log.d("sticker", "addStickerFilter with name = " + path);
				int alpha = hv.getAlpha();
				int color = hv.getColor();

				addStickerFilter(
						path.replace(chooseProcessing.getExternalFilesDir(null)
								.toString() + "/assets/sd/", ""), stickerX,
						stickerY, stickerAngle, stickerScaleH, stickerScaleW,
						alpha, color);
			}

			// onClearCurrent( false );

			chooseProcessing.onPreviewChanged(chooseProcessing.mPreview, false,
					false);
		} catch (Exception e) {
			e.printStackTrace();
			new ExceptionHandler(e, "ApplyCurrentStickerError");
			FlurryAgent.logEvent("ApplyCurrentStickerError");
		}
	}

	/**
	 * Return true if there's at least one active sticker on screen.
	 * 
	 * @return true, if successful
	 */
	protected boolean stickersOnScreen() {
		final ImageViewDrawableOverlay image = (ImageViewDrawableOverlay) mImageView;
		return image.getHighlightCount() > 0;
	}

	private void addStickerFilter(String stickerName, double x, double y,
			double angle, double scaleH, double scaleW, int alpha, int color) {
		if (stickerName == null || stickerName.length() <= 0) {
			return;
		}

		StickerFilter newFilter = new StickerFilter();
		newFilter.setPath(stickerName);
		newFilter.setAngle(angle);
		newFilter.setScaleH(scaleH);
		newFilter.setScaleW(scaleW);
		newFilter.setX(x);
		newFilter.setY(y);
		newFilter.setAlpha(alpha);
		newFilter.setColor(color);
		Log.d("sticker", "add newFilter in addStickerFilter");
		chooseProcessing.processOrder.addFilter(newFilter, "Sticker", false);
		// stikerFilters.add(newFilter);
	}

}
