#ifndef _HDR_FILTER_H_
#define _HDR_FILTER_H_

#include <opencv2/core/core.hpp>
#include "CurveFilter.h"

class HDRFilter : public CurveFilter {

public:
  HDRFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  static uchar applyCurve(uchar sourceComp, int* curve);
  uchar applyShadowsCurve(uchar comp, int* curve);
  uchar applyHighlightsCurve(uchar comp, int* curve);
  void equalizeIntensity(cv::Mat & inputImage);

private:
  std::string shadowsCurveStr_;
  std::string highlightsCurveStr_;
  std::string midtoneCurveStr_;

  bool useShadowsCurve_;
  bool useHighlightsCurve_;
  bool useMidtoneCurve_;
};

#endif
