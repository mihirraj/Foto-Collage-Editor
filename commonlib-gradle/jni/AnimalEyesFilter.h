#ifndef _ANIMALEYES_FILTER_H_
#define _ANIMALEYES_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class AnimalEyesFilter : public BaseOpenCvFilter {
public:
  AnimalEyesFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
  virtual uchar blendEyesOverlay(uchar B, uchar A);
  virtual uchar blendEyesTransparencyComp(uchar blendA, uchar sourceComp, uchar blendComp);
private:
  std::string imagePath_;
};

#endif
