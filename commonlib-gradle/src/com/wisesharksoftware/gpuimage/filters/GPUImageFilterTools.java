package com.wisesharksoftware.gpuimage.filters;

import android.content.Context;

public class GPUImageFilterTools {  

	public static GPUImageFilter createFilterForType(final Context context, final FilterType type) {
        switch (type) {
        	case NO_FILTER:
        		return new GPUImageFilter();
            case SPHERE_REFRACTION:
                return new GPUImageSphereRefractionFilter();
            case SPHERE_REFRACTION2:
                return new GPUImageSphereRefractionFilter2();
            case PINCH_DISTORTION:
            	return new GPUImagePinchDistortionFilter();
            case PINCH_DISTORTION2:
            	return new GPUImagePinchDistortionFilter2();
           default:
                throw new IllegalStateException("No filter of that type!");
        }

    }

    public interface OnGpuImageFilterChosenListener {
        void onGpuImageFilterChosenListener(GPUImageFilter filter);
    }

    public enum FilterType {
        NO_FILTER, SPHERE_REFRACTION, SPHERE_REFRACTION2, PINCH_DISTORTION, PINCH_DISTORTION2
    }

}
