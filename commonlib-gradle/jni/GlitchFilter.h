#ifndef _GLITCH_FILTER_H_
#define _GLITCH_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class GlitchFilter  : public BaseOpenCvFilter {
public:
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);

    static void glitchFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage,
                                int hue, int saturation, int value);
    
    static cv::Vec3b modifyPixel(cv::Vec3b srcPixel, float hue, float saturation, float value);
    static void moveSlice(cv::Mat & sourceImage, int x, int y, int w, int h, int toX, int toY, int incH, int incS, int incV);
private:
  int hue_;
  int saturation_;
  int value_;
};

#endif
