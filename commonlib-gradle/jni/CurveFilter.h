#ifndef _CURVE_FILTER_H_
#define _CURVE_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class CurveFilter : public BaseOpenCvFilter {

public:
  CurveFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
  void curveFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage, int* redCurve, int* greenCurve, int* blueCurve);

protected:
  void fillCurve(int* array, std::string curveString);

private:
  std::vector<double> secondDerivative( std::vector< std::vector<int> > points);
  void getSpline(const std::vector< std::vector<int> >& key_points, int* result);
  static uchar applyCurve(uchar sourceComp, int* curve);

private:
  std::string greenCurveStr_;
  std::string blueCurveStr_;
  std::string redCurveStr_;
  
  bool useBlueCurve_;
  bool useGreenCurve_;
  bool useRedCurve_;
};

#endif
