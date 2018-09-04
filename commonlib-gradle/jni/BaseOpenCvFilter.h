#ifndef _BASE_OPENCV_FILTER_H_
#define _BASE_OPENCV_FILTER_H_

#include <opencv2/core/core.hpp>

class BaseOpenCvFilter {
public:
  virtual void process(cv::Mat& image) {
  }
  virtual void process(std::vector<cv::Mat*>& images) {
    // default implementation
    for (int i = 0; i < images.size(); i++) {
      process(*images[i]);
    }
  }
  virtual void setParam(const std::string& name, const std::string& value) = 0;
  virtual ~BaseOpenCvFilter(void) {}
};

#endif
