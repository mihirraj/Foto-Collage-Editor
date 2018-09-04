#ifndef _EDGE_FILTER_H_
#define _EDGE_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class EdgeFilter : public BaseOpenCvFilter{
public:
  EdgeFilter() : multiplyWithOriginal_(true), doGrayScale_(true){};
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  bool multiplyWithOriginal_;
  bool doGrayScale_;
};

#endif
