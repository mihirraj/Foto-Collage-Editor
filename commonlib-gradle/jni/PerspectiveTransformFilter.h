#ifndef _PERSPECTIVE_TRANSFORM_FILTER_H_
#define _PERSPECTIVE_TRANSFORM_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class PerspectiveTransformFilter : public BaseOpenCvFilter {

public:
  PerspectiveTransformFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  std::vector< std::vector<double> > corners;
};

#endif
