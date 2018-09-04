#include "CoarseEdgesFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>
#include "BlendFilter.h"
#include "CurveFilter.h"
#include "ThresholdFilter.h"

#include "utils.h"
#include "logging.h"

CoarseEdgesFilter::CoarseEdgesFilter() {
	multiplyWithOriginal = false;
}

void CoarseEdgesFilter::setParam(const std::string &name,
		const std::string &value) {
	if (name == "multiply") {
		if (value == "true") {
			multiplyWithOriginal = true;
		}
	}
}

void CoarseEdgesFilter::process(cv::Mat& image) {
	//filter #1
	//EdgeDilationFilter
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

	// invert

	cv::cvtColor(edges, edges, CV_BGR2GRAY);
	cv::cvtColor(edges, edges, CV_GRAY2BGR);

	int dilation_type = cv::MORPH_RECT;
	int dilation_size = 1;
	cv::Mat element = cv::getStructuringElement(dilation_type,
			cv::Size(2 * dilation_size + 1, 2 * dilation_size + 1),
			cv::Point(dilation_size, dilation_size));

	//cv::dilate(edges, edges, cv::Mat(3,3,CV_8UC1, 1));
	//cv::dilate(edges, edges, cv::Mat());
	cv::erode(edges, edges, element);
	cv::dilate(edges, edges, element);

	cv::bitwise_not(edges, edges);

	//cv::cvtColor(edges, edges, CV_GRAY2BGR);

	ThresholdFilter *threshold = new ThresholdFilter;
	threshold->setParam("l_t", "128");
	threshold->setParam("u_t", "128");
	threshold->process(edges);

	if (multiplyWithOriginal) {
		blur(image, image, cv::Size(9, 9));

		cv::Mat tmp;
		tmp.create(image.size(), image.type());
		BlendFilter::blendFilterOpenCV(image, edges, tmp,
		ALGORITHM_MULTIPLY);
		tmp.copyTo(image);
	} else {
		image = edges;
	}

	/*if (filter == filter2) {
	 //filter #2
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

	 // invert

	 cv::cvtColor(edges, edges, CV_BGR2GRAY);
	 cv::cvtColor(edges, edges, CV_GRAY2BGR);

	 int dilation_type = cv::MORPH_RECT;
	 int dilation_size = 1;
	 cv::Mat element = cv::getStructuringElement(dilation_type,
	 cv::Size(2 * dilation_size + 1, 2 * dilation_size + 1),
	 cv::Point(dilation_size, dilation_size));

	 //cv::dilate(edges, edges, cv::Mat(3,3,CV_8UC1, 1));
	 //cv::dilate(edges, edges, cv::Mat());
	 cv::erode(edges, edges, element);
	 cv::dilate(edges, edges, element);

	 cv::bitwise_not(edges, edges);

	 //cv::cvtColor(edges, edges, CV_GRAY2BGR);

	 ThresholdFilter *threshold = new ThresholdFilter;
	 threshold->setParam("l_t", "128");
	 threshold->setParam("u_t", "128");
	 threshold->process(edges);

	 blur(image, image, cv::Size(9, 9));

	 ///////////////////////////////////
	 //if (this->multiplyWithOriginal_) {
	 cv::Mat tmp;
	 tmp.create(image.size(), image.type());
	 BlendFilter::blendFilterOpenCV(image, edges, tmp, ALGORITHM_MULTIPLY);
	 tmp.copyTo(image);
	 /*} else {
	 edges.copyTo(image);
	 edges.release();
	 }*/
	//edges.release();
	//image = edges;
}
