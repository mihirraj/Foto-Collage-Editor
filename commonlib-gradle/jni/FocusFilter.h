#ifndef _FOCUS_FILTER_H_
#define _FOCUS_FILTER_H_

#include <opencv2/core/core.hpp>
#include <string>
#include "BaseOpenCvFilter.h"

class FocusFilter : public BaseOpenCvFilter{
public:
  FocusFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);

private:
  double top;
  double left;
  double right;
  double bottom;
};

#endif
