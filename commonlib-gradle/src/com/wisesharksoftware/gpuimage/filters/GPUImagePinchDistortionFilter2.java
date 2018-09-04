package com.wisesharksoftware.gpuimage.filters;


import android.graphics.PointF;
import android.opengl.GLES20;

public class GPUImagePinchDistortionFilter2 extends GPUImageFilter {
	public static final String SPHERE_REFRACTION_SHADER = ""

			+ "varying highp vec2 textureCoordinate;\n"

			+ "uniform sampler2D inputImageTexture;\n"

			+ "uniform highp float aspectRatio;\n"
			+ "uniform highp vec2 center;\n"
			+ "uniform highp float radius;\n"
			+ "uniform highp float scale;\n"

			+ "void main()\n"
			+ "{\n"
			+ "highp vec2 textureCoordinateToUse = vec2(textureCoordinate.x, (textureCoordinate.y * aspectRatio + 0.5 - 0.5 * aspectRatio));\n"
			+ "highp float dist = distance(center, textureCoordinateToUse);\n"
			+ "textureCoordinateToUse = textureCoordinate;\n"

			+ "if (dist < radius)\n"
			+ "{\n"
			+ "textureCoordinateToUse -= center;\n"
			+ "highp float percent = 1.0 + ((0.5 - dist) / 0.5) * scale;\n"
			+ "textureCoordinateToUse = textureCoordinateToUse * percent;\n"
			+ "textureCoordinateToUse += center;\n"

			+ "gl_FragColor = texture2D(inputImageTexture, textureCoordinateToUse );\n"
			+ "}\n"
			+ "else\n"
			+ "{\n"
			+ "gl_FragColor = texture2D(inputImageTexture, textureCoordinate );\n"
			+ "}\n" + "}";

	private int mScaleLocation;
	private int mAspectRatioLocation;
	private int mRadiusLocation;
	private int mCenterLocation;
	private int mTextureCoordinate;
	
	private float mScale = -0.5f;
	private float mRadius = 1.5f;
	
	public GPUImagePinchDistortionFilter2() {
		super(NO_FILTER_VERTEX_SHADER, SPHERE_REFRACTION_SHADER);
	}

	public GPUImagePinchDistortionFilter2(float scale, float radius) {
		super(NO_FILTER_VERTEX_SHADER, SPHERE_REFRACTION_SHADER);
		this.mScale = scale;
		this.mRadius = radius;
	}
	
	public GPUImagePinchDistortionFilter2(final String fragmentShader) {
		super(SPHERE_REFRACTION_SHADER, fragmentShader);
	}

	@Override
	public void onInit() {
		super.onInit();
		mScaleLocation = GLES20.glGetUniformLocation(getProgram(),
				"scale");
		mRadiusLocation = GLES20.glGetUniformLocation(getProgram(), "radius");
		mCenterLocation = GLES20.glGetUniformLocation(getProgram(), "center");
		mAspectRatioLocation = GLES20.glGetUniformLocation(getProgram(),
				"aspectRatio");
		mTextureCoordinate = GLES20.glGetUniformLocation(getProgram(),
				"textureCoordinate");
	}

	@Override
	public void onInitialized() {
		super.onInitialized();
		//setScale(0.5f);
		//setRadius(1.5f);
		update();
	}

	@Override
	public void onOutputSizeChanged(final int width, final int height) {
		super.onOutputSizeChanged(width, height);
		update();
	}

	public void update() {
		/*
		для pinch             
		customFilter.radius = 1.5f;  // 1
        customFilter.scale = 0.5f;  // 0.5
        и
        customFilter.radius = 1.5f;  // 1
        customFilter.scale = -0.5f;  // 0.5
		*/
		//setFloat(mRadiusLocation, 1.5f);
		//setFloat(mScaleLocation, 0.5f);
		
		setFloat(mRadiusLocation, getRadius());
		setFloat(mScaleLocation, getScale());
		
		setPoint(mCenterLocation, new PointF(0.5f, 0.5f));
		setFloat(mAspectRatioLocation, 1.0f);
		setPoint(mTextureCoordinate, new PointF(0.0f, 0.0f));
	}

	public float getRadius() {
		return mRadius;
	}

	public void setRadius(float mRadius) {
		this.mRadius = mRadius;
		update();
	}
	
	public float getScale() {
		return mScale;
	}

	public void setScale(float mScale) {
		this.mScale = mScale;
		update();
	}

}
