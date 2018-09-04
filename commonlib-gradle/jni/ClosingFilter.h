#ifndef _CLOSING_FILTER_H_
#define _CLOSING_FILTER_H_

#include <opencv2/core/core.hpp>
#include "CurveFilter.h"

class ClosingFilter : public CurveFilter {

public:
  ClosingFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  int kernel_size;
};

#endif
