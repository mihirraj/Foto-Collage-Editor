#ifndef _FISH_EYE_FILTER_H_
#define _FISH_EYE_FILTER_H_

#include "BaseOpenCvFilter.h"
#include <opencv2/core/core.hpp>


class FishEyeFilter : public BaseOpenCvFilter {
public:
  FishEyeFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);

  static void circularFilterOpenCV(cv::Mat & mat);
  static void circleFilter(cv::Mat & mat);
  static void circleFilter(cv::Mat & mat, double k);
  static void barrelFilter(cv::Mat & mat);
  static void barrelFilterNew(cv::Mat & mat);
  static void barrelFilterConvex(cv::Mat & mat);

private:
  int type_;
  int scale;
  double curvature;

  static const int TYPE_CIRCLE = 1;
  static const int TYPE_BARREL = 2;
  static const int TYPE_BARREL_CONVEX = 3;
  static const int TYPE_BARREL_NEW = 4;
};

#endif
