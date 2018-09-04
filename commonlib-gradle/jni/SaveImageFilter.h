#ifndef _SAVE_IMAGE_FILTER_H_
#define _SAVE_IMAGE_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class SaveImageFilter : public BaseOpenCvFilter {

public:
  SaveImageFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
  static cv::Mat memory;
};

#endif
