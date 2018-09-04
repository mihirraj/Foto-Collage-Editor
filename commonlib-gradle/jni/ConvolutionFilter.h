#ifndef _CONVOLUTION_FILTER_H_
#define _CONVOLUTION_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class ConvolutionFilter : public BaseOpenCvFilter {
public:
  ConvolutionFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  cv::Mat mask;
  bool normalize;
  int size;
};

#endif
