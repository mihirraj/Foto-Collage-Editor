#include <string>
#include "CropFilter.h"
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"
#include "logging.h"

CropFilter::CropFilter():left(0.0), top(0.0), right(1.0), bottom(1.0) {

}

void CropFilter::setParam(const std::string& name, const std::string& value) {
	std::stringstream ss(value);
	if (name == "left") {
		ss >> left;
	} else if (name == "top") {
		ss >> top;
	} else if (name == "right") {
		ss >> right;
	} else if (name == "bottom") {
		ss >> bottom;
	}
}

void CropFilter::process(cv::Mat& image) {
	if (left == 0.0 && top == 0.0 && right == 1.0 && bottom == 1.0) {
		return;
	}
	int x = image.cols * left;
	int y = image.rows * top;
	int width = image.cols * std::fabs(right - left);
	int height = image.rows * std::fabs(bottom - top);
	if (x + width > image.cols) {
		width = image.cols - x;
	}
	if (y + height > image.rows) {
		height = image.rows - y;
	}
	cv::Rect cropRect(x, y, width, height);
	LOGI("--- IMAGE left:%f top:%f right:%f bottom:%f x:%d y:%d width:%d height:%d", left, top, right, bottom, x, y, width, height);
	cv::Mat(image, cropRect).copyTo(image);
}
