#include "CBFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"

CBFilter::CBFilter():contrast_(1.0), brightness_(0.0)
{

}

void CBFilter::setParam(const std::string& name, const std::string& value)
{
	if (name == "contrast")
	{
		contrast_ = atof(value.c_str());
	}
	else if (name == "brightness")
	{
		brightness_ = (double)atoi(value.c_str());
	}
}

void CBFilter::process(cv::Mat& image)
{
	image.convertTo(image, -1, contrast_, brightness_);
}

void CBFilter::setContrast(double contrast)
{
	if (contrast < 0) {
		return;
	}
	contrast_ = contrast;
}

void CBFilter::setBightness(double brightness)
{
	brightness_ = brightness;
}
