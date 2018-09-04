#include "ClosingFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include "utils.h"
#include "logging.h"

ClosingFilter::ClosingFilter() {
	kernel_size = 5;
}

void ClosingFilter::setParam(const std::string &name,
		const std::string &value) {
	if (name == "kernel_size") {
		kernel_size = atoi(value.c_str());
	}
}

void ClosingFilter::process(cv::Mat& image) {
	int dilation_type = cv::MORPH_RECT;
	cv::Mat element = cv::getStructuringElement(dilation_type,
			cv::Size(2 * kernel_size + 1, 2 * kernel_size + 1),
			cv::Point(kernel_size, kernel_size));

	//closing
	cv::dilate(image, image, element);
	cv::erode(image, image, element);
}
