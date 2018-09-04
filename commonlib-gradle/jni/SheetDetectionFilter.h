#ifndef _SHEET_DETECTION_FILTER_H_
#define _SHEET_DETECTION_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class SheetDetectionFilter : public BaseOpenCvFilter {

public:
  SheetDetectionFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
  static void findSquares(cv::Mat& image, std::vector<std::vector<cv::Point> >& squares);
  static void findMaxSquare(cv::Mat& image, std::vector<cv::Point>);
private:
  int temperature;
};

#endif
