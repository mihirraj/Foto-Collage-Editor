#ifndef _BIT_FILTER_H_
#define _BIT_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class BitFilter : public BaseOpenCvFilter{
public:
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
  static void bitFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage, uchar pixelSize, uchar colorsPerChannel, int borderColor, uchar borderSize);
private:
  uchar pixelSize_;
  uchar colorsPerChannel_;
  int borderColor_;
  uchar borderSize_;
};

#endif
