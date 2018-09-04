#ifndef _FACE_DETECTION_FILTER_H_
#define _FACE_DETECTION_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"
#include "CurveFilter.h"
#include "SharpnessFilter.h"

class FaceDetectionFilter : public BaseOpenCvFilter {

public:
  FaceDetectionFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
  virtual void whitenEye(cv::Mat& image, cv::Rect& eye);
  virtual void whitenFace(cv::Mat& image, cv::Rect& face, cv::Rect& left_eye, cv::Rect& right_eye, cv::Rect& mouth);
  virtual void correctCurveFace(cv::Mat& image, cv::Rect& face);
  virtual void addBrightnessFace(cv::Mat& image, cv::Rect& face);
  virtual void addTemperatureFace(cv::Mat& image, cv::Rect& face);
  virtual void addWhitenTeeth(cv::Mat& image, cv::Rect& face, cv::Rect& mouth);
  virtual void unsharpEye(cv::Mat& image, cv::Rect& eye);
private:
 double whiten_eye_max_coef;
 double whiten_teeth_max_coef;
 int smooth_skin_alpha;
 int balanse_face_color_alpha;
 int unsharp_eye_alpha;
 int brightness;
 int temperature;
 bool isPreview;
};

#endif
