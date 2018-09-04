#include "ConvolutionFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"
#include <math.h>
#include "logging.h"

ConvolutionFilter::ConvolutionFilter() {
	normalize = true;
	size = 3;
	mask = cv::Mat(size, size, CV_32F);
	for (int i = 0; i < mask.rows; ++i) {
		for (int j = 0; j < mask.cols; ++j) {
			mask.at<float>(i, j) = 1;
		}
	}
}

void ConvolutionFilter::setParam(const std::string& name,
		const std::string& value) {
	if (name == "mask") {
		std::vector < std::string > vals;
		split(value, ',', vals);
		if (vals.size() > 4) {
			size = std::sqrt((double) vals.size());
			mask = cv::Mat(size, size, CV_32F);
			for (int i = 0; i < mask.rows; ++i) {
				for (int j = 0; j < mask.cols; ++j) {
					mask.at<float>(i, j) = atof(
							vals[i * mask.rows + j].c_str());
				}
			}
		}
	}
	if (name == "normalize") {
		if (value == "true") {
			normalize = true;
		}
	}
}

void ConvolutionFilter::process(cv::Mat& image) {
	if (normalize) {
		cv::Mat tempMask = cv::Mat(size, size, CV_32F);
		for (int i = 0; i < mask.rows; ++i) {
			for (int j = 0; j < mask.cols; ++j) {
				tempMask.at<float>(i, j) = mask.at<float>(i, j) / (size * size);
			}
		}
		mask = tempMask;
	}
	filter2D(image, image, image.depth(), mask);
}
