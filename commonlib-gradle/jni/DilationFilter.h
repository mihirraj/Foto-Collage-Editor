#ifndef _DILATION_FILTER_H_
#define _DILATION_FILTER_H_

#include <opencv2/core/core.hpp>
#include "CurveFilter.h"

class DilationFilter : public CurveFilter {

public:
  DilationFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  int dilation_size;
};

#endif
