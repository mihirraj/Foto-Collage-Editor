#include "ThresholdBlurFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include "utils.h"
#include "logging.h"
#include "BlendFilter.h"

ThresholdBlurFilter::ThresholdBlurFilter() {
	threshold = 40;
	kernel_size = 9;
	alpha = -1;
}

void ThresholdBlurFilter::setParam(const std::string &name,
		const std::string &value) {
	if (name == "threshold") {
		threshold = atoi(value.c_str());
	}
	if (name == "kernel_size") {
		kernel_size = atoi(value.c_str());
	}
	if (name == "alpha") {
		alpha = atoi(value.c_str());
	}
}

void ThresholdBlurFilter::process(cv::Mat& image) {
	//thresholdBlur
	int cols = image.cols;
	int rows = image.rows;
	int kh = kernel_size;
	int kw = kernel_size;
	int kh2 = kh / 2;
	int kw2 = kw / 2;

	cv::Mat orig;
	if ((alpha >= 0) && (alpha <= 255)) {
		orig.create(image.size(), image.type());
		image.copyTo(orig);
	}

	for (int i = kh2; i < cols - kh2; i++) {
		for (int j = kw2; j < rows - kw2; j++) {
			//cv::Vec3b sourcePixel = image.at < cv::Vec3b > (j, i);
			cv::Vec3b source = image.at < cv::Vec3b > (j, i);
			int sum0 = 0;
			int sum1 = 0;
			int sum2 = 0;
			int size = 0;
			for (int x = i - kh2; x <= i + kh2; x++) {
				for (int y = j - kw2; y <= j + kw2; y++) {
					cv::Vec3b data = image.at < cv::Vec3b > (y, x);
					if ((abs(data[0] - source[0]) < threshold)
							&& (abs(data[1] - source[1]) < threshold)
							&& (abs(data[1] - source[1]) < threshold)) {
						sum0 += data[0];
						sum1 += data[1];
						sum2 += data[2];
						size++;
					}
				}
			}

			int avg0 = sum0 / size;
			int avg1 = sum1 / size;
			int avg2 = sum2 / size;

			cv::Vec3b resultPixel;
			resultPixel[0] = avg0;
			resultPixel[1] = avg1;
			resultPixel[2] = avg2;
			image.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}

	if ((alpha >= 0) && (alpha <= 255)) {
		cv::Mat tmp;
		tmp.create(image.size(), image.type());
		BlendFilter::blendFilterTransparencyOpenCV(orig, image, tmp, alpha);
		tmp.copyTo(image);
	}
}
