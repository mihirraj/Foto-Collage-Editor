#include "EdgeFilter.h"
#include "utils.h"
#include <string>
#include "BlendFilter.h"

void EdgeFilter::process(cv::Mat& image) {
  cv::Mat edges;

  
  // edge detection
  cv::Mat grad_x, grad_y;
  cv::Mat abs_grad_x, abs_grad_y;
  int ddepth = CV_16S;
  cv::Scharr(image, grad_x, ddepth, 1, 0);
  cv::convertScaleAbs( grad_x, abs_grad_x );
  cv::Scharr(image, grad_y, ddepth, 0, 1);
  cv::convertScaleAbs( grad_y, abs_grad_y );
  addWeighted( abs_grad_x, 0.5, abs_grad_y, 0.5, 0, edges );

  // invert
  cv::bitwise_not(edges, edges);
  
  // optional gray scale
  if (this->doGrayScale_) {
    cv::cvtColor(edges, edges, CV_BGR2GRAY);
  }
  
  if (this->multiplyWithOriginal_) {
  // multiply with original
    cv::Mat tmp;
    tmp.create(image.size(), image.type());
    BlendFilter::blendFilterOpenCV(image, edges, tmp, ALGORITHM_MULTIPLY);
    tmp.copyTo(image);
  } else {
    edges.copyTo(image);
    edges.release();
  }
  edges.release();
}

void EdgeFilter::setParam(const std::string& name, const std::string& value) {
  if (name == "gray") {
    doGrayScale_ = getBool(value);
  } else if (name == "multiply") {
    multiplyWithOriginal_ = getBool(value);
  }
}