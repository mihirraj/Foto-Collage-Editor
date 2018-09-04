#ifndef _STICKER_FILTER_H_
#define _STICKER_FILTER_H_

#include <opencv2/core/core.hpp>
#include <string>
#include "BaseOpenCvFilter.h"

class StickerFilter : public BaseOpenCvFilter{
public:
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);

private:
  void rotateImage(cv::Mat & image, cv::Mat & rotatedImage, double angle, double scaleW, double scaleH);

private:
  double angle;
  double scaleX;
  double scaleY;
  double centerX;
  double centerY;
  int color;
  int alpha;
  std::string path;
};

#endif
