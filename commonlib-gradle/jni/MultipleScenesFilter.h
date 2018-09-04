#ifndef _MULTIPLE_SCENES_FILTER_
#define _MULTIPLE_SCENES_FILTER_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class MultipleScenesFilter : public BaseOpenCvFilter {
public:
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  int picturesCount_;
};

#endif
