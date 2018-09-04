#ifndef _CROP_FILTER_H_
#define _CROP_FILTER_H_

#include <opencv2/core/core.hpp>
#include <string>
#include "BaseOpenCvFilter.h"

class CropFilter : public BaseOpenCvFilter{
public:
  CropFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);

private:
  double top;
  double left;
  double right;
  double bottom;
};

#endif
