#ifndef _MIRROR_FILTER_H_
#define _MIRROR_FILTER_H_

#include <opencv2/core/core.hpp>
#include <string>
#include "BaseOpenCvFilter.h"

class MirrorFilter : public BaseOpenCvFilter{
public:
  MirrorFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
};

#endif
