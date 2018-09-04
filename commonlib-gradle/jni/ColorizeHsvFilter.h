#ifndef _COLORIZE_HSV_FILTER_H_
#define _COLORIZE_HSV_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class ColorizeHsvFilter : public BaseOpenCvFilter {
public:
  ColorizeHsvFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
  static void colorizeHsvFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage,
                                      int hue, int saturation, int value);
  static void setSaturationFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage,
                                      int saturation);
  static cv::Vec3b colorizePixel(cv::Vec3b srcPixel, float hue, float saturation, float value);
private:
  int hue_;
  int saturation_;
  int value_;
};

#endif
