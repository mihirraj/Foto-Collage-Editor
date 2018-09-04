#ifndef _THRESHOLD_FILTER_H_
#define _THRESHOLD_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class ThresholdFilter : public BaseOpenCvFilter {
public:
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
  
private:
  int lowerThreshold_;
  int upperThreshold_;
  int black_;
  int white_;
};

#endif
