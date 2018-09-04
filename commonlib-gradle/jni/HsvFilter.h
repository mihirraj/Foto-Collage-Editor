#ifndef _HSV_FILTER_H_
#define _HSV_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class HsvFilter  : public BaseOpenCvFilter {
public:
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);

    static void hsvFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage,
                                int hue, int saturation, int value);
    
    static cv::Vec3b modifyPixel(cv::Vec3b srcPixel, float hue, float saturation, float value);
private:
  int hue_;
  int saturation_;
  int value_;
};

#endif
