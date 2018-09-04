#ifndef _CB_FILTER_H_
#define _CB_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class CBFilter : public BaseOpenCvFilter {
public:
  CBFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
  void setContrast(double contrast);
  void setBightness(double brightness);
private:
  double contrast_;
  double brightness_;
};

#endif
