#include "PerspectiveTransformFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include "utils.h"
#include "logging.h"

PerspectiveTransformFilter::PerspectiveTransformFilter() {
}

void PerspectiveTransformFilter::setParam(const std::string &name,
		const std::string &value) {
	if (name == "corners") {
		std::vector < std::string > pairs;
		split(value.c_str(), ';', pairs);

		for (int i = 0; i < pairs.size(); ++i) {
			std::vector < std::string > vals;
			split(pairs[i], ',', vals);
			std::vector<double> item;
			item.push_back(atof(vals[0].c_str()));
			item.push_back(atof(vals[1].c_str()));
			corners.push_back(item);
		}
	}
}

void PerspectiveTransformFilter::process(cv::Mat& image) {
	cv::Point2f source_points[4];
	cv::Point2f dest_points[4];

	source_points[0] = cv::Point2f(corners[0][0] * image.cols,
			corners[0][1] * image.rows);
	source_points[1] = cv::Point2f(corners[1][0] * image.cols,
			corners[1][1] * image.rows);
	source_points[2] = cv::Point2f(corners[2][0] * image.cols,
			corners[2][1] * image.rows);
	source_points[3] = cv::Point2f(corners[3][0] * image.cols,
			corners[3][1] * image.rows);

	dest_points[0] = cv::Point2f(0, 0);
	dest_points[1] = cv::Point2f(image.cols, 0);
	dest_points[2] = cv::Point2f(image.cols, image.rows);
	dest_points[3] = cv::Point2f(0, image.rows);

	cv::Mat transform_matrix = cv::getPerspectiveTransform(source_points,
			dest_points);
	cv::warpPerspective(image, image, transform_matrix,
			cv::Size(image.cols, image.rows));
}
