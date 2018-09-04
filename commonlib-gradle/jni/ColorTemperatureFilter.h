#ifndef _COLOR_TEMPERATURE_FILTER_H_
#define _COLOR_TEMPERATURE_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class ColorTemperatureFilter : public BaseOpenCvFilter {

public:
  ColorTemperatureFilter();
  virtual void process(cv::Mat& image);
  virtual void setParam(const std::string& name, const std::string& value);
private:
  int temperature;
};

#endif
