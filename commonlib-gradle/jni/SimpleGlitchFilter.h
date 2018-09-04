#ifndef _SIMPLE_GLITCH_FILTER_H_
#define _SIMPLE_GLITCH_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class SimpleGlitchFilter  : public BaseOpenCvFilter {
public:
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);

    static void simpleGlitchFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage,
                                int dispersion, int value);
    
    static cv::Vec3b modifyPixel(cv::Vec3b srcPixel, float dispersion, float value);
private:
  int dispersion_;
  int value_;
};

#endif
