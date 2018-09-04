#include "BlackWhiteScanFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

#include "utils.h"
#include "logging.h"

void BlackWhiteScanFilter::setParam(const std::string &name, const std::string &value)
{}


void BlackWhiteScanFilter::process(cv::Mat& image) {
  // cv::threshold(image, image, 200, 255, CV_THRESH_BINARY);
  //cv::dilate(image, image, NULL);
  //cv::erode(image, image, NULL);
  // cv::adaptiveThreshold(image, image, 255.0, CV_THRESH_BINARY, CV_ADAPTIVE_THRESH_MEAN_C, 11, 3);
}