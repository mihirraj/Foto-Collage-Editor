#include "ThresholdFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"

void ThresholdFilter::setParam(const std::string& name, const std::string& value) {
  if (name == "l_t") {
    lowerThreshold_ = atoi(value.c_str());
  } else if (name == "u_t") {
    upperThreshold_ = atoi(value.c_str());
  } else if (name == "black") {
    std::stringstream ss;
    ss << std::hex << value;
    ss >> black_;
  } else if (name == "white") {
    std::stringstream ss;
    ss << std::hex << value;
    ss >> white_;
  }
}

void ThresholdFilter::process(cv::Mat& image) {
  cv::cvtColor(image, image, CV_RGB2GRAY);
  int cols = image.cols;
  int rows = image.rows;
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      uchar srcValue = image.at<uchar>(j, i);
      if (srcValue < lowerThreshold_) {
        srcValue = 0;
      }
      else if (srcValue > upperThreshold_) {
        srcValue = 255;
      }
      else {
        float srcFloat = (float)srcValue;
        srcFloat = (srcFloat - lowerThreshold_) / (upperThreshold_ - lowerThreshold_);
        srcFloat = srcFloat*srcFloat * (3 - 2*srcFloat);
        srcValue = (uchar)(srcFloat * 255);
      }
      image.at<uchar>(j, i) = srcValue;
    }
  }
  cv::cvtColor(image, image, CV_GRAY2RGB);
}
