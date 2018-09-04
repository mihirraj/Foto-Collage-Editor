#ifndef _BILLBOARD_FILTER_H_
#define _BILLBOARD_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

#define ALGORITHM_SCREEN 0
#define ALGORITHM_MULTIPLY 1
#define ALGORITHM_TRANSPARENCY 2
#define ALGORITHM_TRANSPARENCY_ALPHA 3
#define ALGORITHM_COLOR_DODGE 4
#define ALGORITHM_OVERLAY 5
#define ALGORITHM_GETFIRSRT 6
#define ALGORITHM_HUE 7

class BillboardFilter : public BaseOpenCvFilter {
public:
  BillboardFilter();
  virtual void process(cv::Mat& image);
  bool inBounds(int i, int j);
  virtual void setParam(const std::string& name, const std::string& value);
  //static void blendFilterOpenCV(const cv::Mat & sourceImage, const cv::Mat & blendImage, cv::Mat & resultImage, int algorithm);
  //static void blendSticker(const cv::Mat & source, const cv::Mat & sticker, cv::Mat & result, double centerX, double centerY,int alpha,int color);
  //static void blendFilterTransparencyOpenCV(const cv::Mat & sourceImage, const cv::Mat & blendImage, cv::Mat & resultImage, int alpha);
  //static uchar blendTransparencyComp(uchar blendA, uchar sourceComp, uchar blendComp);
  //static uchar blendScreenComp(uchar sourceComp, uchar blendComp);
  //static uchar blendMultiplyComp(uchar sourceComp, uchar blendComp);
  //static uchar blendOverlay(uchar B, uchar A);
private:
  int algorithm_;
  int x0,y0;
  int x1,y1;
  int x2,y2;
  int x3,y3;
  std::string imagePath_;
  bool blend_with_image_memory;
  int alpha;
};

#endif