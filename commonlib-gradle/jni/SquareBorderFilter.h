#ifndef _SQUARE_BORDER_FILTER_H_
#define _SQUARE_BORDER_FILTER_H_

#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class SquareBorderFilter : public BaseOpenCvFilter{
public:
	SquareBorderFilter();
	virtual void process(cv::Mat& image);
	virtual void setParam(const std::string& name, const std::string& value);
private:
	int background;
	bool reflect;
	bool usecolorbackground;
	int red;
	int green;
	int blue;
};

#endif
