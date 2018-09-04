#include "EdgeClosingFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include "utils.h"
#include "logging.h"

EdgeClosingFilter::EdgeClosingFilter() {
	kernel_size = 5;
}

void EdgeClosingFilter::setParam(const std::string &name,
		const std::string &value) {
	if (name == "kernel_size") {
		kernel_size = atoi(value.c_str());
	}
}

void EdgeClosingFilter::process(cv::Mat& image) {
	cv::Mat edges;
	// edge detection

	cv::Mat grad_x, grad_y;
	cv::Mat abs_grad_x, abs_grad_y;
	int ddepth = CV_16S;
	cv::Scharr(image, grad_x, ddepth, 1, 0);
	cv::convertScaleAbs(grad_x, abs_grad_x);
	cv::Scharr(image, grad_y, ddepth, 0, 1);
	cv::convertScaleAbs(grad_y, abs_grad_y);
	addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, edges);

	int dilation_type = cv::MORPH_RECT;
	int dilation_size = kernel_size;
	cv::Mat element = cv::getStructuringElement(dilation_type,
			cv::Size(2 * dilation_size + 1, 2 * dilation_size + 1),
			cv::Point(dilation_size, dilation_size));

	//closing
	cv::dilate(edges, edges, element);
	cv::erode(edges, edges, element);

	cv::bitwise_not(edges, edges);

	image = edges;
}
