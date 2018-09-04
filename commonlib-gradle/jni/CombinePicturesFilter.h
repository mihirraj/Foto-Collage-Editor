#ifndef _COMBINE_PICTURES_FILTER_
#define _COMBINE_PICTURES_FILTER_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class CombinePicturesFilter : public BaseOpenCvFilter {
public:
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  std::string picture1;
  std::string picture2;
  std::string picture3;
  std::string picture4;
};

#endif
