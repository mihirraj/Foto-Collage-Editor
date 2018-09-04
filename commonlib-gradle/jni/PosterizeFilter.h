#ifndef _POSTERIZE_FILTER_H_
#define _POSTERIZE_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class PosterizeFilter : public BaseOpenCvFilter {
public:
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  int numLevels_;
};

#endif
