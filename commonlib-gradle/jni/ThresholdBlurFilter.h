#ifndef _THRESHOLD_BLUR_FILTER_H_
#define _THRESHOLD_BLUR_FILTER_H_

#include <opencv2/core/core.hpp>
#include "CurveFilter.h"

class ThresholdBlurFilter : public CurveFilter {

public:
  ThresholdBlurFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  int threshold;
  int kernel_size;
  int alpha;
};

#endif
