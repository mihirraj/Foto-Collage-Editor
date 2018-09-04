#include "ImageProcessing.h"
#include "CurveFilter.h"
#include "AnimalEyesFilter.h"
#include "BlendFilter.h"
#include "BillboardFilter.h"
#include "ComicFilter.h"
#include "EdgeFilter.h"
#include "GrayFilter.h"
#include "BaseOpenCvFilter.h"
#include "HsvFilter.h"
#include "GlitchFilter.h"
#include "ThresholdFilter.h"
#include "PosterizeFilter.h"
#include "BitFilter.h"
#include "ColorizeHsvFilter.h"
#include "CropRectangleFilter.h"
#include "MultiPicturesFilter.h"
#include "CombinePicturesFilter.h"
#include "MultipleScenesFilter.h"
#include "TiltShiftFilter.h"
#include "FishEyeFilter.h"
#include "SquareFilter.h"
#include "SquareBorderFilter.h"
#include "CBFilter.h"
#include "HDRFilter.h"
#include "HDRFilter2.h"
#include "CoarseEdgesFilter.h"
#include "DilationFilter.h"
#include "FlipRotateFilter.h"
#include "ThresholdBlurFilter.h"
#include "EdgeClosingFilter.h"
#include "ClosingFilter.h"
#include "SharpenFilter.h"
#include "StickerFilter.h"
#include "ColorTemperatureFilter.h"
#include "CropFilter.h"
#include "MirrorFilter.h"
#include "FocusFilter.h"
#include "ConvolutionFilter.h"
#include "SharpnessFilter.h"
#include "FaceDetectionFilter.h"
#include "SheetDetectionFilter.h"
#include "PerspectiveTransformFilter.h"
#include "SaveImageFilter.h"
#include "InkFilter.h"
#include "logging.h"
#include "FilterException.h"
#include <algorithm>

#define Z_INDEX "zIndex"

ImageProcessing::ImageProcessing(const jsonxx::Object& config) {
  config_ = &config;
  cancelled_ = false;
}

struct Less {
  Less(ImageProcessing * p) : _proc(p) {}
  bool operator() (const std::string& o1, const std::string& o2) {
    return _proc->presetsSortFunc(o1, o2);
  }
  ImageProcessing * _proc;
};

bool ImageProcessing::presetsSortFunc(std::string preset1, std::string preset2) {
  // sort presets based on zIndex
  const jsonxx::Object* preset1Config = getConfig(preset1);
  const jsonxx::Object* preset2Config = getConfig(preset2);
  int zIndex1 = 1;
  int zIndex2 = 1;
  if (preset1Config->has<jsonxx::Number>(Z_INDEX)) {
    zIndex1 = preset1Config->get<jsonxx::Number>(Z_INDEX);
  }
  if (preset2Config->has<jsonxx::Number>(Z_INDEX)) {
    zIndex2 = preset2Config->get<jsonxx::Number>(Z_INDEX);
  }
  return zIndex2 > zIndex1;
}

void ImageProcessing::process(std::vector<cv::Mat*> images, std::string presetName) {
  for (int i = 0; i < images.size(); i++) {
    process(*images[i], presetName);
  }
}

void ImageProcessing::process(const std::vector<cv::Mat*> & images, const std::vector<std::string> & presetNames, cv::Mat& image) {
//  LOGI("before sort");
//  for (int i = 0; i < presetNames.size(); i++) {
//    LOGI("%d, %s", i, presetNames[i].c_str());
//  }
//  std::sort(presetNames.begin(), presetNames.end(), Less(this));
//  LOGI("after sort");
//  for (int i = 0; i < presetNames.size(); i++) {
//    LOGI("$%d, %s", i, presetNames[i].c_str());
//  }
	//LOGI("--- process multi pictures");

	const jsonxx::Object* cameraConfig = getConfig(presetNames[0]);
	const jsonxx::Array& filters = cameraConfig->get<jsonxx::Array>("filters");
    const jsonxx::Object& filterConfig = filters.get<jsonxx::Object>(0);
    const jsonxx::String& filterName = filterConfig.get<jsonxx::String>("type");
    if (filterName != "multi_pictures") {
    	return;
    }
    if (cameraConfig->has<jsonxx::Boolean>("square")) {
      if (cameraConfig->get<jsonxx::Boolean>("square")) {
        SquareFilter squareFilter;
        squareFilter.process(image);
		for(std::vector<cv::Mat*>::const_iterator it = images.begin(); it != images.end(); ++it) {
			squareFilter.process(*(*it));
		}
      }
    }
    MultiPicturesFilter * multiPicturesFilter = new MultiPicturesFilter();
    setFilterParams((BaseOpenCvFilter*)multiPicturesFilter, filterConfig);
    int imagesCount = multiPicturesFilter->preProcess(images, image);
	//LOGI("--- pre process done");
    if (imagesCount > 1) {
    	//LOGI("--- process all pictures");
    	for (int i = 0; i < images.size(); ++i) {
    		process(*images[i], presetNames[1]);
    	}
    } else {
    	//LOGI("--- process one pictures");
    	process(image, presetNames[1]);
    }
	//LOGI("--- process done");
    multiPicturesFilter->postProcess(images, image);
	//LOGI("--- post process done");
}

void ImageProcessing::process(cv::Mat& image, std::string presetName){
  // 1 - find the filter config
	std::string errorCause = presetName;
	std::string errorPrefix = presetName + ":";
	try {
		if (isCancelled()) {
			return;
		}
  const jsonxx::Object* presetConfig = getConfig(presetName);
  const jsonxx::Array& filters = presetConfig->get<jsonxx::Array>("filters");
  //LOGI("--- got filters");
  
  
  if (presetConfig->has<jsonxx::Boolean>("square")) {
    if (presetConfig->get<jsonxx::Boolean>("square")) {
      SquareFilter squareFilter;
      squareFilter.process(image);
    }
  }
  
  for (int i = 0; i < filters.size(); i++) {
		if (isCancelled()) {
		    LOGI("--- skip processing 20");
			return;
		}
    const jsonxx::Object& filterConfig = filters.get<jsonxx::Object>(i);
    // temporary solution - because now everything is static
    const jsonxx::String& filterName = filterConfig.get<jsonxx::String>("type");
    errorCause = filterName;
    LOGI("--- doing filter %s", filterName.c_str());
    if (filterName == "normal") {
      //LOGI("--- skipping, since it is normal");
      continue;
    }
    
    BaseOpenCvFilter* filter = createFilter(filterName);
    setFilterParams(filter, filterConfig);
    filter->process(image);
    delete filter;
  }
	} catch (...) {
		throw FilterException(presetName + errorCause);
	}
}

const jsonxx::Object* ImageProcessing::getConfig(std::string presetName) {
  const jsonxx::Array& cameras = config_->get<jsonxx::Array>("cameras");

  const jsonxx::Array& processings = config_->get<jsonxx::Array>("processings");
  for (int i = 0; i < cameras.size(); i++) {
    const jsonxx::Object& obj = cameras.get<jsonxx::Object>(i);
    if (obj.get<jsonxx::String>("name") == presetName) {
      //LOGI("--- get camera: '%s'", presetName.c_str());
      return &obj;
    }
  }
  for (int i = 0; i < processings.size(); i++) {
    const jsonxx::Object& obj = processings.get<jsonxx::Object>(i);
    if (obj.get<jsonxx::String>("name") == presetName) {
      //LOGI("--- get processing: '%s'", presetName.c_str());
      return &obj;
    }
  }
  return NULL;
}

void ImageProcessing::process(cv::Mat& image){
	std::string errorCause = "";
	try {
		if (isCancelled()) {
		    LOGI("--- skip processing 2");
			return;
		}
		const jsonxx::Array& processings = config_->get<jsonxx::Array>("processings");
		for (int i = 0; i < processings.size(); i++) {
			if (isCancelled()) {
			    LOGI("--- skip processing 3");
				return;
			}
			const jsonxx::Object& presetConfig = processings.get<jsonxx::Object>(i);
			const jsonxx::Array& filters = presetConfig.get<jsonxx::Array>("filters");
			if (presetConfig.has<jsonxx::Boolean>("square")) {
				if (presetConfig.get<jsonxx::Boolean>("square")) {
					SquareFilter squareFilter;
					squareFilter.process(image);
				}
			}
			for (int i = 0; i < filters.size(); i++) {
				if (isCancelled()) {
				    LOGI("--- skip processing 4");
					return;
				}
				const jsonxx::Object& filterConfig = filters.get<jsonxx::Object>(i);
			    // temporary solution - because now everything is static
				const jsonxx::String& filterName = filterConfig.get<jsonxx::String>("type");
			    errorCause = filterName;
			    LOGI("--- doing filter 2 %s", filterName.c_str());
			    if (filterName == "normal") {
			      //LOGI("--- skipping, since it is normal");
			      continue;
			    }

			    BaseOpenCvFilter* filter = createFilter(filterName);
			    setFilterParams(filter, filterConfig);
			    filter->process(image);
			    delete filter;
			}
		}
	} catch (...) {
		throw FilterException(errorCause);
	}
}


BaseOpenCvFilter* ImageProcessing::createFilter(std::string filterName) {
  if (filterName == "edge") {
    return new EdgeFilter();
  } else if (filterName == "blend") {
    return new BlendFilter();
  } else if (filterName == "billboard") {
    return new BillboardFilter();
  } else if (filterName == "animal_eyes") {
	return new AnimalEyesFilter();
  } else if (filterName == "comic")  {
    return new ComicFilter();
  } else if (filterName == "curves") {
    return new CurveFilter();
  } else if (filterName == "gray") {
    return new GrayFilter();
  } else if (filterName == "hsv") {
    return new HsvFilter();
  } else if (filterName == "glitch") {
    return new GlitchFilter();
  } else if (filterName == "threshold") {
    return new ThresholdFilter();
  } else if (filterName == "posterize") {
    return new PosterizeFilter();
  } else if (filterName == "8bit") {
    return new BitFilter();
  } else if (filterName == "colorize") {
    return new ColorizeHsvFilter();
  } else if (filterName == "crop_rect") {
    return new CropRectangleFilter();
  } else if (filterName == "square") {
    return new SquareFilter();
  } else if (filterName == "square_border") {
    return new SquareBorderFilter();
  } else if (filterName == "multi_pictures") {
    return new MultiPicturesFilter();
  } else if (filterName == "multiple_scenes") {
	  return new MultipleScenesFilter();
  } else if (filterName == "combine_pictures") {
	  return new CombinePicturesFilter();
  } else if (filterName == "tilt_shift") {
	return new TiltShiftFilter();
  } else if (filterName == "fish_eye") {
 	return new FishEyeFilter();
  } else if (filterName == "contrast_brightness") {
  	return new CBFilter();
  } else if (filterName == "hdr") {
   	return new HDRFilter();
  } else if (filterName == "hdr2") {
   	return new HDRFilter2();
  } else if (filterName == "coarse_edges") {
   	return new CoarseEdgesFilter();
  } else if (filterName == "dilation") {
	  return new DilationFilter();
  } else if (filterName == "flip_rotate") {
	  return new FlipRotateFilter();
  } else if (filterName == "threshold_blur") {
	  return new ThresholdBlurFilter();
  } else if (filterName == "edge_closing") {
	  return new EdgeClosingFilter();
  } else if (filterName == "closing") {
	  return new ClosingFilter();
  } else if (filterName == "sharpen") {
	  return new SharpenFilter();
  } else if (filterName == "sticker") {
	  return new StickerFilter();
  } else if (filterName == "color_temperature") {
	  return new ColorTemperatureFilter();
  } else if (filterName == "crop") {
	  return new CropFilter();
  } else if (filterName == "mirror") {
	  return new MirrorFilter();
  } else if (filterName == "focus") {
	  return new FocusFilter();
  } else if (filterName == "convolution") {
	  return new ConvolutionFilter();
  } else if (filterName == "sharpness") {
	  return new SharpnessFilter();
  } else if (filterName == "face_detection") {
  	  return new FaceDetectionFilter();
  } else if (filterName == "sheet_detection") {
  	  return new SheetDetectionFilter();
  } else if (filterName == "perspective_transform") {
  	  return new PerspectiveTransformFilter();
  } else if (filterName == "save_image") {
 	  return new SaveImageFilter();
  } else if (filterName == "ink") {
	  return new InkFilter();
  }
}

void ImageProcessing::setFilterParams(BaseOpenCvFilter * filter, const jsonxx::Object & filterConfig) {
    if (filterConfig.has<jsonxx::Array>("params")) {
      const jsonxx::Array& params = filterConfig.get<jsonxx::Array>("params");

      for (int i = 0; i < params.size(); i++) {
        const jsonxx::Object& param = params.get<jsonxx::Object>(i);
        const jsonxx::String& paramName = param.get<jsonxx::String>("name");
        const jsonxx::String& paramValue = param.get<jsonxx::String>("value");
        filter->setParam(paramName, paramValue);
      }
    }
}

void ImageProcessing::cancelProcessing() {
	LOGI("--- ImageProcessing::cancelProcessing %p", this);
	cancelled_ = true;
}

bool ImageProcessing::isCancelled() const {
	return cancelled_;
}

