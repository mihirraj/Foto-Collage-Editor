#ifndef _INK_FILTER_H_
#define _INK_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class InkFilter : public BaseOpenCvFilter {

public:
  InkFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
};

#endif
