#ifndef _SHARPEN_FILTER_H_
#define _SHARPEN_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class SharpenFilter : public BaseOpenCvFilter {
public:
  SharpenFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  bool useMask_;
  cv::Mat mask_;
};

#endif
