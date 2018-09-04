#include <string>
#include "MirrorFilter.h"
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"
#include "logging.h"

MirrorFilter::MirrorFilter() {

}

void MirrorFilter::setParam(const std::string& name, const std::string& value) {

}

void MirrorFilter::process(cv::Mat& image) {
	cv::Mat tmp;
	cv::Size size = image.size();
	size.width = 2 * size.width;
	tmp.create(size, image.type());
	for (int i = 0; i < image.cols; i++) {
		for (int j = 0; j < image.rows; j++) {
			tmp.at < cv::Vec3b > (j, i) = image.at < cv::Vec3b > (j, i);
			tmp.at < cv::Vec3b > (j, 2 * image.cols - i - 1) = image.at < cv::Vec3b > (j, i);
		}
	}
	image = tmp;
}
