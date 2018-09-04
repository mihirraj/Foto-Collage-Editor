#ifndef _HDR_FILTER2_H_
#define _HDR_FILTER2_H_

#include <opencv2/core/core.hpp>
#include "CurveFilter.h"

#define ALGORITHM_OLD_HDR 0
#define ALGORITHM_HDR 1
#define ALGORITHM_MIDTONES 2

class HDRFilter2 : public CurveFilter {

public:
  HDRFilter2();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  unsigned char ChannelBlend_Overlay(unsigned char A, unsigned char B);
  int alpha;
  int blurSize;
  int algorithm;
  int black;
};

#endif
