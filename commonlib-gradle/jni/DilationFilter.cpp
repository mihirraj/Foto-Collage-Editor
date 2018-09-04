#include "DilationFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include "utils.h"
#include "logging.h"

DilationFilter::DilationFilter() {
	dilation_size = 5;
}

void DilationFilter::setParam(const std::string &name,
		const std::string &value) {
	if (name == "size") {
		dilation_size = atoi(value.c_str());
	}
}

void DilationFilter::process(cv::Mat& image) {
	int dilation_type = cv::MORPH_RECT;
	cv::Mat element = cv::getStructuringElement(dilation_type,
			cv::Size(2 * dilation_size + 1, 2 * dilation_size + 1),
			cv::Point(dilation_size, dilation_size));
	cv::dilate(image, image, element);
}
