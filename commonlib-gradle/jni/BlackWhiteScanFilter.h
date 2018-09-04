#ifndef _BLACK_WHITE_SCAN_FILTER_H_
#define _BLACK_WHITE_SCAN_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class BlackWhiteScanFilter : public BaseOpenCvFilter {
  
public:
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
};

#endif
