#include <string>
#include "GrayFilter.h"
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>

GrayFilter::GrayFilter() {
	use3channels = false;
}

void GrayFilter::setParam(const std::string& name, const std::string& value) {
	if (name == "use3channels") {
		if (value == "true") {
			use3channels = true;
		}
	}
}

void GrayFilter::process(cv::Mat& image) {
	if (image.channels() == 3 || image.channels() == 4) {
		cv::cvtColor(image, image, CV_BGR2GRAY);
		if (use3channels) {
			cv::cvtColor(image, image, CV_GRAY2BGR);
		}
	}
}
