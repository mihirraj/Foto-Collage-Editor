#ifndef _GRAY_FILTER_H_
#define _GRAY_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class GrayFilter : public BaseOpenCvFilter{
public:
  GrayFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  bool use3channels;
};

#endif
