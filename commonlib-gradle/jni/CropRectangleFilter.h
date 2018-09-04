#ifndef _CROP_RECTANGLE_FILTER_
#define _CROP_RECTANGLE_FILTER_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class CropRectangleFilter : public BaseOpenCvFilter {

public:
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
};

#endif
