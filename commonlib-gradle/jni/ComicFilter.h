#ifndef _COMIC_FILTER_H_
#define _COMIC_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class ComicFilter : public BaseOpenCvFilter {
  
public:
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
  ComicFilter() : colorDodge_(false), smallPattern_(true){}

private:
  void processSmallPattern(cv::Mat& image, bool colorDodge, int patternSize, const std::vector<std::string>& images, const int brightnessImageMap[]);
  void processBigPattern(cv::Mat& image, bool colorDodge, int patternSize, const std::vector<std::string>& images, const int brightnessImageMap[]);
  int patternSize_;
  bool colorDodge_;
  bool smallPattern_;
  std::string imagesConfig_;
};

#endif
