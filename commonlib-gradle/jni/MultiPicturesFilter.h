#ifndef _MULTI_PICTURES_FILTER_
#define _MULTI_PICTURES_FILTER_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class MultiPicturesFilter : public BaseOpenCvFilter {
public:
  virtual void process(cv::Mat& image);
  virtual void process(std::vector<cv::Mat*>& images);
  int preProcess(const std::vector<cv::Mat*>& images, cv::Mat & image);
  void postProcess(const std::vector<cv::Mat*>& images, cv::Mat & image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  int picturesCount_;
  int composeType_;

private:
  static const int COMPOSE_SQUARE = 1;
  static const int COMPOSE_VERTICAL = 2;
  static const int COMPOSE_BLEND = 3;
};

#endif
