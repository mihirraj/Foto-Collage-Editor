#include "PosterizeFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"

void PosterizeFilter::setParam(const std::string& name, const std::string& value) {
  if (name == "levels") {
    numLevels_ = atoi(value.c_str());
  }
}

void PosterizeFilter::process(cv::Mat& image) {
  int cols = image.cols;
  int rows = image.rows;
  int numChannels = image.channels();
  
  uchar levels[256];
  if (numLevels_ != 1)
    for (int i = 0; i < 256; i++)
      levels[i] = 255 * (numLevels_*i / 256) / (numLevels_-1);
  
  for (int i = 0; i < cols; i++) {
    for (int j = 0; j < rows; j++) {
      cv::Vec3b pixel = image.at<cv::Vec3b>(j, i);
      for(int k = 0; k < numChannels; k++) {
        pixel[k] = levels[pixel[k]];
      }
      image.at<cv::Vec3b>(j, i) = pixel;
    }
  }
}