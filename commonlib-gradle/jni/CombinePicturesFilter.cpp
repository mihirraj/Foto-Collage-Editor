#include "CombinePicturesFilter.h"
#include <string>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "logging.h"

#include "utils.h"

void CombinePicturesFilter::process(cv::Mat& image) {
	cv::Mat blendImage1;
	cv::Mat blendImage2;
	cv::Mat blendImage3;
	cv::Mat blendImage4;

	blendImage1 = cv::imread(picture1, CV_LOAD_IMAGE_UNCHANGED);
	blendImage2 = cv::imread(picture2, CV_LOAD_IMAGE_UNCHANGED);
	blendImage3 = cv::imread(picture3, CV_LOAD_IMAGE_UNCHANGED);
	blendImage4 = cv::imread(picture4, CV_LOAD_IMAGE_UNCHANGED);
	int cols = blendImage1.cols;
	int rows = blendImage1.rows;

	cv::Mat tmp;
	cv::Size size = image.size();
//	size.width = 2 * size.height;
//	size.height = 2 * size.height;
	tmp.create(size, image.type());

	blendImage1.copyTo(tmp(cv::Rect(0, 0, cols, rows)));
	blendImage2.copyTo(tmp(cv::Rect(cols, 0, cols, rows)));
	blendImage3.copyTo(tmp(cv::Rect(0, rows, cols, rows)));
	blendImage4.copyTo(tmp(cv::Rect(cols, rows, cols, rows)));
	image = tmp;
}

void CombinePicturesFilter::setParam(const std::string& name,
		const std::string& value) {
	if (name == "picture1") {
		picture1 = value;
	} else if (name == "picture2") {
		picture2 = value;
	} else if (name == "picture3") {
		picture3 = value;
	} else if (name == "picture4") {
		picture4 = value;
	}
}
