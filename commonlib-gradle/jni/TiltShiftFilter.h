#ifndef _TILTSHIFT_FILTER_H_
#define _TILTSHIFT_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class TiltShiftFilter : public BaseOpenCvFilter{
public:
  TiltShiftFilter(){};
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
};

#endif
