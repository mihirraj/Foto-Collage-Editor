#include <string>
#include "FocusFilter.h"
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"
#include "logging.h"

FocusFilter::FocusFilter() :
		left(0.0), top(0.0), right(1.0), bottom(1.0) {

}

void FocusFilter::setParam(const std::string& name, const std::string& value) {
	std::stringstream ss(value);
	if (name == "left") {
		ss >> left;
	} else if (name == "top") {
		ss >> top;
	} else if (name == "right") {
		ss >> right;
	} else if (name == "bottom") {
		ss >> bottom;
	}
}

void FocusFilter::process(cv::Mat& image) {
//	if (left == 0.0 && top == 0.0 && right == 1.0 && bottom == 1.0) {
//		return;
//	}

	int x = image.cols * left;
	int y = image.rows * top;
	int width = image.cols * std::fabs(right - left);
	int height = image.rows * std::fabs(bottom - top);
	int radius = width / 2;
	int radius2 = radius * radius;

	int blurSize = 5;
	int blurSizeR = blurSize + blurSize + 1;
	cv::Mat bluredImage;
	int radiusRing = 50;
	int radiusRing2 = radiusRing * radiusRing;
	GaussianBlur(image, bluredImage, cv::Size(blurSizeR, blurSizeR), 0);

	for (int i = 0; i < bluredImage.cols; i++) {
		for (int j = 0; j < bluredImage.rows; j++) {
			int c_x = i - x - width / 2;
			int c_y = j - y - height / 2;
			float inner_radius2 = c_x * c_x + c_y * c_y;

			if (inner_radius2 > radius2) {
				continue;
			} else if (inner_radius2 > (radius - radiusRing) * (radius - radiusRing)) {
				cv::Vec3b source = image.at < cv::Vec3b > (j, i);
				cv::Vec3b blur = bluredImage.at < cv::Vec3b > (j, i);
				double interval = sqrt(inner_radius2) - (radius - radiusRing);
				double interval2 = interval * interval;
				double coeff = 1 - (interval / (radiusRing * 1.0));

				cv::Vec3b resultPixel;
				int tempr = (int) (source[0] * coeff + blur[0] * (1 - coeff));
				int tempg = (int) (source[1] * coeff + blur[1] * (1 - coeff));
				int tempb = (int) (source[2] * coeff + blur[2] * (1 - coeff));

				if (tempr < 0) {
					tempr = 0;
				}

				if (tempg < 0) {
					tempg = 0;
				}

				if (tempb < 0) {
					tempb = 0;
				}
				if (tempr > 255) {
					tempr = 255;
				}
				if (tempg > 255) {
					tempg = 255;
				}
				if (tempb > 255) {
					tempb = 255;
				}

				resultPixel[0] = tempr;
				resultPixel[1] = tempg;
				resultPixel[2] = tempb;

				bluredImage.at < cv::Vec3b > (j, i) = resultPixel;
			} else {
				cv::Vec3b source = image.at < cv::Vec3b > (j, i);
				cv::Vec3b blur = bluredImage.at < cv::Vec3b > (j, i);

				cv::Vec3b resultPixel;
				resultPixel[0] = source[0];
				resultPixel[1] = source[1];
				resultPixel[2] = source[2];
				bluredImage.at < cv::Vec3b > (j, i) = resultPixel;
			}
		}
	}

	LOGI("--- IMAGE left:%f top:%f right:%f bottom:%f x:%d y:%d width:%d height:%d", left, top, right, bottom, x, y, width, height);
	bluredImage.copyTo(image);
}
