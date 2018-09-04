#include "MultipleScenesFilter.h"
#include <string>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "logging.h"

#include "utils.h"

void MultipleScenesFilter::process(cv::Mat& image) {
	// do nothing
}

void MultipleScenesFilter::setParam(const std::string& name,
		const std::string& value) {
	if (name == "pictures_count") {
		picturesCount_ = atoi(value.c_str());
	}
}
