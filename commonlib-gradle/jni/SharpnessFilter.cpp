#include "SharpnessFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"
#include <math.h>
#include "logging.h"

SharpnessFilter::SharpnessFilter() {
	size = 0;
}

void SharpnessFilter::setParam(const std::string& name,
		const std::string& value) {
		if (name == "size") {
		size = atoi(value.c_str());
	}
}

void SharpnessFilter::process(cv::Mat& image) {
	if (size == 0)
		return;
	if (size < 0) {
		if ((size % 2) == 0) {
			size -= 1;
		}
		GaussianBlur(image, image, cv::Size(-size, -size), 0);
	} else {
		if ((size % 2) == 0) {
			size += 1;
		}
		double threshold = 5, amount = 1;
		cv::Mat blurred;
		GaussianBlur(image, blurred, cv::Size(size, size), 0);
		cv::Mat lowContrastMask = abs(image - blurred) < threshold;
		cv::Mat sharpened = image * (1 + amount) + blurred * (-amount);
		image.copyTo(sharpened, lowContrastMask);
		image = sharpened;
	}
}

