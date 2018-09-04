#ifndef _COARSE_EDGES_FILTER_H_
#define _COARSE_EDGES_FILTER_H_

#include <opencv2/core/core.hpp>
#include "CurveFilter.h"

class CoarseEdgesFilter : public CurveFilter {

public:
	CoarseEdgesFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  bool multiplyWithOriginal;
};

#endif
