#include "SquareFilter.h"
#include <string>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "logging.h"

#include "utils.h"

void SquareFilter::process(cv::Mat& image) {
	// copy from processing.cpp
  
	//LOGI("SQUARE processs");
  	if (image.cols == image.rows) {
  		return;
  	}
	int side;
	if (image.rows > image.cols) {
		side = image.cols;
	} else {
		if (image.cols > image.rows) {
			side = image.rows;
		}
	}
	cv::Mat square;
	cv::Rect cropRect(image.cols / 2 - side / 2, image.rows / 2 - side / 2, side,
                    side);
  
	cv::Mat(image, cropRect).copyTo(image);
}

void SquareFilter::setParam(const std::string& name, const std::string& value) {
}
