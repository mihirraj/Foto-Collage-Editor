#include "ColorTemperatureFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include "utils.h"
#include "logging.h"

ColorTemperatureFilter::ColorTemperatureFilter() {
	temperature = 0;
}

void ColorTemperatureFilter::setParam(const std::string &name,
		const std::string &value) {
	if (name == "temperature") {
		temperature = atoi(value.c_str());
	}
}

void ColorTemperatureFilter::process(cv::Mat& image) {
	if (temperature == 0) return;
	int cols = image.cols;
	int rows = image.rows;
	int temp;

	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			cv::Vec3b source = image.at < cv::Vec3b > (j, i);

			cv::Vec3b resultPixel;
			temp = source[0] - (2.55 * temperature);
			if (temp < 0) {
				resultPixel[0] = 0;
			} else if (temp > 255) {
				resultPixel[0] = 255;
			} else {
				resultPixel[0] = temp;
			}
			resultPixel[1] = source[1];
			temp = source[2] + (2.55 * temperature);
			if (temp < 0) {
				resultPixel[2] = 0;
			} else if (temp > 255) {
				resultPixel[2] = 255;
			} else {
				resultPixel[2] = temp;
			}
			image.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}
}
