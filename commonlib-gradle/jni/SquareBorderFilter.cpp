#include "SquareBorderFilter.h"
#include <string>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "logging.h"

#include "utils.h"

SquareBorderFilter::SquareBorderFilter() {
	background = 0;
	reflect = false;
	usecolorbackground = false;
	red = 0;
	green = 0;
	blue = 0;
}

void SquareBorderFilter::setParam(const std::string& name, const std::string& value) {
	if (name == "background") {
			background = atoi(value.c_str());
	}
	if (name == "red") {
		red = atoi(value.c_str());
	}
	if (name == "green") {
		green = atoi(value.c_str());
	}
	if (name == "blue") {
		blue = atoi(value.c_str());
	}
	if (name == "usecolorbackground") {
		if (value == "true") {
			usecolorbackground = true;
		}
	}
	if (name == "reflect") {
		if (value == "true") {
			reflect = true;
		}
	}
}

void SquareBorderFilter::process(cv::Mat& image) {
	if (image.cols == image.rows) {
  		return;
  	}
	int border;
	if (image.rows > image.cols) {
		border = (image.rows - image.cols) / 2;
		if (reflect) {
			cv::copyMakeBorder(image, image, 0, 0, border, border, cv::BORDER_REFLECT_101);
		} else {
			if (usecolorbackground) {
				cv::copyMakeBorder(image, image, 0, 0, border, border, cv::BORDER_CONSTANT, cv::Scalar(blue, green, red, 255));
			} else {
				cv::copyMakeBorder(image, image, 0, 0, border, border, cv::BORDER_CONSTANT, cv::Scalar::all(background));
			}
		}
	} else {
		border = (image.cols - image.rows) / 2;
		if (reflect) {
			cv::copyMakeBorder(image, image, border, border, 0, 0, cv::BORDER_REFLECT_101);
		} else {
			if (usecolorbackground) {
				cv::copyMakeBorder(image, image, border, border, 0, 0, cv::BORDER_CONSTANT, cv::Scalar(blue, green, red, 255));
			} else {
				cv::copyMakeBorder(image, image, border, border, 0, 0, cv::BORDER_CONSTANT, cv::Scalar::all(background));
			}
		}
	}
}
