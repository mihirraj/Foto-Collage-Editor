package com.wisesharksoftware.gpuimage.filters;


import android.graphics.PointF;
import android.opengl.GLES20;

public class GPUImageSphereRefractionFilter2 extends GPUImageFilter {
	public static final String SPHERE_REFRACTION_SHADER = ""
			+ "varying highp vec2 textureCoordinate;\n"
			+ "uniform sampler2D inputImageTexture;\n"
			+ "uniform highp vec2 center;\n"
			+ "uniform highp float radius;\n"
			+ "uniform highp float aspectRatio;\n"
			+ "uniform highp float refractiveIndex;\n"
			+ "void main()\n"
			+ "{\n"
			+ "highp vec2 textureCoordinateToUse = vec2((textureCoordinate.x * aspectRatio + 0.5 - 0.5 * aspectRatio), textureCoordinate.y);\n"
			//+ "highp vec2 textureCoordinateToUse = vec2(textureCoordinate.x, (textureCoordinate.y * aspectRatio + 0.5 - 0.5 * aspectRatio));\n"
			+ "highp float distanceFromCenter = distance(center, textureCoordinateToUse);\n"
			+ "lowp float checkForPresenceWithinSphere = step(distanceFromCenter, radius);\n"
			+ "distanceFromCenter = distanceFromCenter / radius;\n"
			+ "highp float normalizedDepth = radius * sqrt(1.0 - distanceFromCenter * distanceFromCenter);\n"
			+ "highp vec3 sphereNormal = normalize(vec3(textureCoordinateToUse - center, normalizedDepth));\n"
			+ "highp vec3 refractedVector = refract(vec3(0.0, 0.0, -1.0), sphereNormal, refractiveIndex);\n"
			+ "refractedVector.xy = -refractedVector.xy;\n"
			+ "gl_FragColor = texture2D(inputImageTexture, (refractedVector.xy + 1.0) * 0.5) * checkForPresenceWithinSphere;\n"
			+ "}";

	private int mRefractiveIndexLocation;
	private float mAspectRatio = 1;
	private int mAspectRatioLocation;
	private int mRadiusLocation;
	private int mCenterLocation;
	private int mTextureCoordinate;
	private float mRefraction = 0.35f;
	 
	public GPUImageSphereRefractionFilter2() {
	      super(NO_FILTER_VERTEX_SHADER, SPHERE_REFRACTION_SHADER);
	}

	public GPUImageSphereRefractionFilter2(final String fragmentShader) {
		super(SPHERE_REFRACTION_SHADER, fragmentShader);
	}
	
	public void setAspectRatio(float ratio) {
		mAspectRatio = ratio;
		update();
	}

	@Override
	public void onInit() {
		super.onInit();
		mRefractiveIndexLocation = GLES20.glGetUniformLocation(getProgram(),
				"refractiveIndex");
		mRadiusLocation = GLES20.glGetUniformLocation(getProgram(), "radius");
		mCenterLocation = GLES20.glGetUniformLocation(getProgram(), "center");
		mAspectRatioLocation = GLES20.glGetUniformLocation(getProgram(),
				"aspectRatio");
		mTextureCoordinate = GLES20.glGetUniformLocation(getProgram(), "textureCoordinate");	
	}

	@Override
	public void onInitialized() {
		super.onInitialized();
		update();
	}

	@Override
	public void onOutputSizeChanged(final int width, final int height) {
		super.onOutputSizeChanged(width, height);
		update();
	}

	public void update() {
		setFloat(mRefractiveIndexLocation, mRefraction);
		setFloat(mRadiusLocation, 0.5f);//0.25f
		setPoint(mCenterLocation, new PointF(0.5f, 0.5f));
		setFloat(mAspectRatioLocation, mAspectRatio);
		setPoint(mTextureCoordinate, new PointF(0.0f, 0.0f));
	}
	
	public void setRefraction(float refraction) {
		mRefraction = refraction;
		update();
	}
}
