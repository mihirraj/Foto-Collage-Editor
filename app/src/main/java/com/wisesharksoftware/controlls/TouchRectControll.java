package com.wisesharksoftware.controlls;

import com.photostudio.photoeditior.R;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class TouchRectControll extends ImageViewTouch {

	private Context context;
	private PointF points[] = new PointF[4];
	private PointF firstPoints[] = new PointF[4];
	private PointF startPoints[] = new PointF[4];
	private PointF currentPoint = null;
	private int currentPointIndex = -1;
	private Drawable pointDraw;
	private int dWidth;
	private int dHeight;
	private Paint mPaintIntoLines;
	private Paint mPaintOutLines;
	private Paint mFillPaint;
	private Path mLinesPath = new Path();
	private Path poly = new Path();
	private RectF fillRect = new RectF();
	private boolean expand = false;
	private PointF oldPoints[];
	private float angle = 0;
	private Matrix angleMatrix;
	private int rotatedImageW, rotatedImageH, oldW, oldH;
	private boolean rotated = true;
	private double scalex, scaley;
	private ArrayList<PointF> triangle = new ArrayList<PointF>();
	private Bitmap bitmap;
	private OnSizeChanged onSizeChanged;

	public interface OnSizeChanged {
		public void changed(int w, int h);
	}

	public void setExpand(boolean expand) {
		this.expand = expand;
	}

	public void setOnSizeChanged(OnSizeChanged onSizeChanged) {
		this.onSizeChanged = onSizeChanged;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public TouchRectControll(Context context) {
		this(context, null, 0);
	}

	public TouchRectControll(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@Override
	public void clear() {
		bitmap = null;
		points = new PointF[4];
		invalidate();
		super.clear();
	}

	public TouchRectControll(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;

		pointDraw = context.getResources().getDrawable(R.drawable.red_circle);
		dWidth = pointDraw.getIntrinsicWidth() / 2;
		dHeight = pointDraw.getIntrinsicWidth() / 2;

		mGestureDetector = new GestureDetector(getContext(),
				new TouchRectGesture(), null, true);
		mGestureDetector.setIsLongpressEnabled(false);

		setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
				if (paramMotionEvent.getAction() == MotionEvent.ACTION_UP) {
					currentPoint = null;
				}
				return false;
			}
		});
		angleMatrix = new Matrix();

		mFillPaint = new Paint();
		mFillPaint.setColor(Color.argb(100, 0, 0, 0));
		mFillPaint.setStyle(Style.FILL);
		mPaintIntoLines = new Paint();
		mPaintIntoLines.setStrokeWidth(1);
		mPaintIntoLines.setColor(Color.RED);
		mPaintIntoLines.setPathEffect(new DashPathEffect(new float[] { 3, 3 },
				1));
		mPaintIntoLines.setAlpha(170);
		mPaintOutLines = new Paint();
		mPaintOutLines.setStrokeWidth(4);
		mPaintOutLines.setAlpha(170);
		mPaintOutLines.setColor(Color.RED);

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		super.onSizeChanged(w, h, oldw, oldh);
		if (bitmap != null
				&& (firstPoints[0] != null && firstPoints[0].x != 0 && firstPoints[0].y != 0)) {
			PointF[] ps = firstPoints.clone();

			for (PointF p : ps) {
				p.x *= bitmap.getWidth();
				p.y *= bitmap.getHeight();
				p.x += (getWidth() / 2 - bitmap.getWidth() / 2);
				p.y += (getHeight() / 2 - bitmap.getHeight() / 2);
			}
			this.points = ps;
			if (wedge(points[0], points[1], points[2]) < 0) {
				Log.d("asd", "Против часовой");
				currentRevers(true);
			} else {
				currentRevers(false);
			}

			for (int i = 0; i < this.points.length; i++) {
				PointF p = this.points[i];
				Log.d("Point", "x=" + p.x + " y=" + p.y);
				startPoints[i] = new PointF(p.x, p.y);
			}
			invalidate();
		}
	}

	public void setOldSize(int w, int h) {
		oldW = w;
		oldH = h;
	}

	public void setRotatedSize(int w, int h) {
		rotatedImageW = w;
		rotatedImageH = h;
	}

	@Override
	public void draw(Canvas canvas) {
		if (bitmap != null) {
			canvas.drawBitmap(bitmap, (getWidth() / 2)
					- (bitmap.getWidth() / 2),
					(getHeight() / 2) - (bitmap.getHeight() / 2), null);
		}
		if (pointDraw != null && points[0] != null) {

			int x0 = getWidth() / 2;
			int y0 = (getHeight() / 2);
			if (!rotated) {
				boolean backExpand = false;
				if (expand) {
					backExpand = true;
					expand();
				}
				rotated = true;
				for (int i = 0; i < points.length; i++) {

					points[i].x -= x0;
					points[i].y -= y0;
					points[i] = rotate(points[i], 90);
					points[i].x += x0;
					points[i].y += y0;

				}

				double scalex = (double) rotatedImageW / (double) oldH;
				double scaley = (double) rotatedImageH / (double) oldW;

				for (PointF p : points) {
					double x = p.x - x0;
					double y = p.y - y0;
					x *= scalex;
					y *= scaley;
					p.x = x0 + (float) x;
					p.y = y0 + (float) y;
				}

				oldW = rotatedImageW;
				oldH = rotatedImageH;

				if (backExpand) {
					expand();
				}

			}

			fillRect.set(0, 0, canvas.getWidth(), canvas.getHeight());
			poly.reset();
			poly.addRect(fillRect, Path.Direction.CCW);

			poly.moveTo(points[0].x, points[0].y);
			poly.lineTo(points[1].x, points[1].y);
			poly.lineTo(points[2].x, points[2].y);
			poly.lineTo(points[3].x, points[3].y);
			poly.lineTo(points[0].x, points[0].y);
			poly.close();

			canvas.drawPath(poly, mFillPaint);
			// ------------draw lines into rect
			// -----1 top
			float x = (points[0].x + points[1].x) / 2;
			float y = (points[0].y + points[1].y) / 2;
			float x2 = (points[2].x + points[3].x) / 2;
			float y2 = (points[2].y + points[3].y) / 2;

			// canvas.drawPath(mLinesPath, mPaintIntoLines);
			canvas.drawLine(x, y, x2, y2, mPaintIntoLines);
			// -----1 top

			// -----2 top

			x = (((points[0].x + points[1].x) / 2) + points[0].x) / 2;
			y = (((points[0].y + points[1].y) / 2) + points[0].y) / 2;
			x2 = (((points[2].x + points[3].x) / 2) + points[3].x) / 2;
			y2 = (((points[2].y + points[3].y) / 2) + points[3].y) / 2;
			canvas.drawLine(x, y, x2, y2, mPaintIntoLines);
			// -----2 top

			// -----3 top
			x = (((points[0].x + points[1].x) / 2) + points[1].x) / 2;
			y = (((points[0].y + points[1].y) / 2) + points[1].y) / 2;
			x2 = (((points[2].x + points[3].x) / 2) + points[2].x) / 2;
			y2 = (((points[2].y + points[3].y) / 2) + points[2].y) / 2;
			canvas.drawLine(x, y, x2, y2, mPaintIntoLines);
			// -----3 top

			// -----1 right
			x = (points[1].x + points[2].x) / 2;
			y = (points[1].y + points[2].y) / 2;
			x2 = (points[0].x + points[3].x) / 2;
			y2 = (points[0].y + points[3].y) / 2;
			canvas.drawLine(x, y, x2, y2, mPaintIntoLines);
			// -----1 right

			// -----2 right
			x = (((points[1].x + points[2].x) / 2) + points[1].x) / 2;
			y = (((points[1].y + points[2].y) / 2) + points[1].y) / 2;
			x2 = (((points[0].x + points[3].x) / 2) + points[0].x) / 2;
			y2 = (((points[0].y + points[3].y) / 2) + points[0].y) / 2;
			canvas.drawLine(x, y, x2, y2, mPaintIntoLines);
			// -----2 right

			// -----3 right
			x = (((points[1].x + points[2].x) / 2) + points[2].x) / 2;
			y = (((points[1].y + points[2].y) / 2) + points[2].y) / 2;
			x2 = (((points[0].x + points[3].x) / 2) + points[3].x) / 2;
			y2 = (((points[0].y + points[3].y) / 2) + points[3].y) / 2;
			canvas.drawLine(x, y, x2, y2, mPaintIntoLines);
			// -----3 right
			// ------------END draw lines into rect

			// -------------draw lines out rect

			x = points[0].x;
			y = points[0].y;
			x2 = points[1].x;
			y2 = points[1].y;

			canvas.drawLine(x, y, x2, y2, mPaintOutLines);
			x = points[1].x;
			y = points[1].y;
			x2 = points[2].x;
			y2 = points[2].y;

			canvas.drawLine(x, y, x2, y2, mPaintOutLines);

			x = points[2].x;
			y = points[2].y;
			x2 = points[3].x;
			y2 = points[3].y;

			canvas.drawLine(x, y, x2, y2, mPaintOutLines);

			x = points[3].x;
			y = points[3].y;
			x2 = points[0].x;
			y2 = points[0].y;

			canvas.drawLine(x, y, x2, y2, mPaintOutLines);
			// -------------END draw lines out rect

			// -------------draw circle
			pointDraw
					.setBounds((int) (points[0].x) - dWidth,
							(int) (points[0].y) - dHeight, (int) (points[0].x)
									+ dWidth, (int) (points[0].y) + dHeight);
			pointDraw.draw(canvas);
			pointDraw
					.setBounds((int) (points[1].x) - dWidth,
							(int) (points[1].y) - dHeight, (int) (points[1].x)
									+ dWidth, (int) (points[1].y) + dHeight);
			pointDraw.draw(canvas);
			pointDraw
					.setBounds((int) (points[2].x) - dWidth,
							(int) (points[2].y) - dHeight, (int) (points[2].x)
									+ dWidth, (int) (points[2].y) + dHeight);
			pointDraw.draw(canvas);
			pointDraw
					.setBounds((int) (points[3].x) - dWidth,
							(int) (points[3].y) - dHeight, (int) (points[3].x)
									+ dWidth, (int) (points[3].y) + dHeight);
			pointDraw.draw(canvas);
			// -------------END draw circle
			// canvas.restore();
		}
	}

	class TouchRectGesture extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			float x = e.getX();
			float y = e.getY();
			if (points[0] != null) {
				for (int i = 0; i < points.length; i++) {
					PointF p = points[i];

					if (x > p.x - dWidth * 3 && x < p.x + dWidth * 3
							&& y > p.y - dHeight * 3 && y < p.y + dHeight * 3) {
						currentPoint = p;
						currentPointIndex = i;
						Log.d("Current", "i=" + currentPointIndex);
						break;
					}
				}
			}
			return super.onDown(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (currentPoint != null) {
				int nextIndex = currentPointIndex + 1;
				if (nextIndex > points.length - 1)
					nextIndex = 0;
				int prevIndex = currentPointIndex - 1;
				if (prevIndex < 0) {
					prevIndex = points.length - 1;
				}

				float y = ((e2.getX() - points[prevIndex].x) / (points[nextIndex].x - points[prevIndex].x))
						* (points[nextIndex].y - points[prevIndex].y)
						+ points[prevIndex].y;

				scroll(distanceX, distanceY);

				/*
				 * if (currentPointIndex == 3 || currentPointIndex == 2) { if (y
				 * <= e2.getY()) {
				 * 
				 * scroll(distanceX, distanceY);
				 * 
				 * } } else { if (y >= e2.getY()) { scroll(distanceX,
				 * distanceY); } }
				 */
			}

			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		private void scroll(float x, float y) {

			float newx = currentPoint.x - x;
			float newy = currentPoint.y - y;
			int xOffset = (getWidth() / 2) - (rotatedImageW / 2);
			if (newx > xOffset && newx < getWidth() - xOffset
					&& newy > getHeight() / 2 - rotatedImageH / 2
					&& newy < getHeight() / 2 + rotatedImageH / 2) {
				triangle.clear();
				for (int i = 0; i < points.length; i++) {
					if (i != currentPointIndex) {
						triangle.add(points[i]);
					}
				}
				PointF p1 = triangle.get(0);
				PointF p2 = triangle.get(1);
				PointF p3 = triangle.get(2);
				float a = (p1.x - newx) * (p2.y - p1.y) - (p2.x - p1.x)
						* (p1.y - newy);
				float b = (p2.x - newx) * (p3.y - p2.y) - (p3.x - p2.x)
						* (p2.y - newy);
				float c = (p3.x - newx) * (p1.y - p3.y) - (p1.x - p3.x)
						* (p3.y - newy);

				if ((a >= 0 && b >= 0 && c >= 0)
						|| (a <= 0 && b <= 0 && c <= 0)) {
					// Принадлежит треугольнику
				} else {
					currentPoint.x = newx;
					currentPoint.y = newy;
					invalidate();
				}

			}
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			currentPoint = null;
			Log.d("Touch", "Up");
			return super.onSingleTapUp(e);
		}

	}

	PointF rotate(PointF point, float angle) {
		double rad = Math.toRadians(angle);
		float x = (float) (point.x * Math.cos(rad) - point.y * Math.sin(rad));
		float y = (float) (point.x * Math.sin(rad) + point.y * Math.cos(rad));
		point.x = x;
		point.y = y;
		return point;
	}

	public void setAngle(float angle, int w, int h) {

		this.rotatedImageH = h;
		this.rotatedImageW = w;

		if (angle >= 360) {
			angle = 0;
		}
		this.angle = angle;
		rotated = false;
		invalidate();
	}

	public void setPoints(PointF[] points) {

		for (PointF p : points) {
			Log.d("Point", "x=" + p.x + " y=" + p.y);
		}
		if (points[0].x == -1) {
			if (bitmap != null) {
				int xOffset = (getWidth() / 2) - (rotatedImageW / 2);
				this.points[0] = new PointF(xOffset + rotatedImageW / 5,
						getHeight() / 2 - rotatedImageH / 2 + rotatedImageH / 5);
				this.points[1] = new PointF(getWidth() - xOffset
						- rotatedImageW / 5, getHeight() / 2 - rotatedImageH
						/ 2 + rotatedImageH / 5);
				this.points[2] = new PointF(getWidth() - xOffset
						- rotatedImageW / 5, getHeight() / 2 + rotatedImageH
						/ 2 - rotatedImageH / 5);
				this.points[3] = new PointF(xOffset + rotatedImageW / 5,
						getHeight() / 2 + rotatedImageH / 2 - rotatedImageH / 5);
			}
			firstPoints[0] = new PointF(0f, 0f);
			firstPoints[1] = new PointF(1f, 0f);
			firstPoints[2] = new PointF(1f, 1f);
			firstPoints[3] = new PointF(0f, 1f);
		} else {
			for (int i = 0; i < points.length; i++) {
				PointF p = points[i];
				Log.d("Point", "x=" + p.x + " y=" + p.y);
				firstPoints[i] = new PointF(p.x, p.y);
			}

			for (PointF p : points) {
				p.x *= oldW;
				p.y *= oldH;
				p.x += (getWidth() / 2 - oldW / 2);
				p.y += (getHeight() / 2 - oldH / 2);
			}
			this.points = points;
			if (wedge(points[0], points[1], points[2]) < 0) {
				Log.d("asd", "Против часовой");
				currentRevers(true);
			} else {
				currentRevers(false);
			}

		}
		for (int i = 0; i < this.points.length; i++) {
			PointF p = this.points[i];
			Log.d("Point", "x=" + p.x + " y=" + p.y);
			startPoints[i] = new PointF(p.x, p.y);
		}
		invalidate();
	}

	private void currentRevers(boolean inverse) {

		PointF newPoints[] = null;
		if (inverse) {
			newPoints = new PointF[4];
			newPoints[0] = points[3];
			newPoints[1] = points[2];
			newPoints[2] = points[1];
			newPoints[3] = points[0];
			points = newPoints;
		}
		int leftTopIndex = findLeftTop(points);
		newPoints = new PointF[4];
		newPoints[0] = points[leftTopIndex];
		int i = 1;
		while (i < 4) {
			int index = leftTopIndex + i;
			if (index >= points.length) {
				index -= points.length;
			}
			newPoints[i] = points[index];
			i++;
		}
		points = newPoints;

	}

	public static double wedge(PointF a, PointF b, PointF c) {
		return (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x);
	}

	private int findLeftTop(PointF[] points) {
		PointF curP = points[0];
		int left1Index = 0;
		for (int i = 1; i < points.length; i++) {
			PointF p = points[i];
			if (p.x < curP.x) {
				curP = p;
				left1Index = i;
			}
		}
		int index = 0;
		if (left1Index == 0) {
			index += 1;
		}
		curP = points[index];
		int left2Index = index;
		for (int i = 0; i < points.length; i++) {
			PointF p = points[i];
			if (i != left1Index && p.x < curP.x) {
				curP = p;
				left2Index = i;
			}
		}
		if (points[left1Index].y < points[left2Index].y) {
			return left1Index;
		}
		return left2Index;

	}

	public void expand() {
		if (!expand) {
			expand = true;
			oldPoints = points.clone();
			int xOffset = (getWidth() / 2) - (rotatedImageW / 2);
			points[0] = new PointF(xOffset, getHeight() / 2 - rotatedImageH / 2);
			points[1] = new PointF(getWidth() - xOffset, getHeight() / 2
					- rotatedImageH / 2);
			points[2] = new PointF(getWidth() - xOffset, getHeight() / 2
					+ rotatedImageH / 2);
			points[3] = new PointF(xOffset, getHeight() / 2 + rotatedImageH / 2);
		} else {
			expand = false;
			points = oldPoints;
		}
		invalidate();

	}

	public boolean getExpand() {
		return expand;
	}

	public boolean hasPoints() {
		if (points[0] == null) {
			return false;
		}
		return true;
	}

	public float[] getRealPoints(/* int imageW, int imageH */) {
		// float scaleX = (float) imageW / (float) getWidth();
		// float scaleY = (float) imageH / (float) getHeight();

		float[] ps = new float[8];
		int offsetX = getWidth() / 2 - rotatedImageW / 2;
		int offsetY = getHeight() / 2 - rotatedImageH / 2;
		ps[0] = (points[0].x - offsetX) / (float) rotatedImageW;
		ps[1] = (points[0].y - offsetY) / (float) rotatedImageH;
		ps[2] = (points[1].x - offsetX) / (float) rotatedImageW;
		ps[3] = (points[1].y - offsetY) / (float) rotatedImageH;
		ps[4] = (points[2].x - offsetX) / (float) rotatedImageW;
		ps[5] = (points[2].y - offsetY) / (float) rotatedImageH;
		ps[6] = (points[3].x - offsetX) / (float) rotatedImageW;
		ps[7] = (points[3].y - offsetY) / (float) rotatedImageH;

		return ps;

	}
}
