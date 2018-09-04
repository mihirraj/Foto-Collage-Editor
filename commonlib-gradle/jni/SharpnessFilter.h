#ifndef _SHARPNESS_FILTER_H_
#define _SHARPNESS_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class SharpnessFilter : public BaseOpenCvFilter {
public:
  SharpnessFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  int size;
};

#endif
