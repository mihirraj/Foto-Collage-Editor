#include "SaveImageFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include "utils.h"
#include "logging.h"
#include "config.h"
#include "BlendFilter.h"

cv::Mat SaveImageFilter::memory = cv::Mat::zeros(cv::Size(0, 0), CV_8UC3);

SaveImageFilter::SaveImageFilter() {
}

void SaveImageFilter::setParam(const std::string &name,
		const std::string &value) {
}

void SaveImageFilter::process(cv::Mat& image) {
	image.copyTo(SaveImageFilter::memory);
}
