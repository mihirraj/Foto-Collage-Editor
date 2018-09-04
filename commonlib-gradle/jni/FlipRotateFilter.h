#ifndef _FLIP_ROTATE_FILTER_H_
#define _FLIP_ROTATE_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class FlipRotateFilter : public BaseOpenCvFilter {

public:
  FlipRotateFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  int angle;
  bool flip_horizontal;
  bool flip_vertical;
};

#endif
