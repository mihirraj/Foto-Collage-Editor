#include "SharpenFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"
#include <math.h>
#include "logging.h"

SharpenFilter::SharpenFilter():useMask_(false)
{

}

void SharpenFilter::setParam(const std::string& name, const std::string& value)
{
	if (name == "mask")
	{
		std::vector<std::string> vals;
		split(value, ',', vals);
		if (vals.size() > 4) {
			useMask_ = true;
		}
		int size = std::sqrt((double)vals.size());
		mask_ = cv::Mat(size,  size, CV_32SC1);
		for (int i = 0; i < mask_.rows; ++i) {
			for (int j = 0; j < mask_.cols; ++j) {
				mask_.at<int>(i, j) = atoi(vals[i * mask_.rows + j].c_str());
			}
		}
	}
}

void SharpenFilter::process(cv::Mat& image)
{
	if (!useMask_)
	{
		return;
	}
	for (int i = 0; i < mask_.rows; ++i) {
		for (int j = 0; j < mask_.cols; ++j) {
			int val = mask_.at<int>(i, j);
			LOGI("i = %d j = %d val=%d", i, j, val);
		}
	}
	filter2D(image, image, image.depth(), mask_);
}

