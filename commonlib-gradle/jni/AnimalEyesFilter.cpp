#include "AnimalEyesFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"
#include "logging.h"

AnimalEyesFilter::AnimalEyesFilter() {
}

void AnimalEyesFilter::setParam(const std::string& name, const std::string& value) {
	if (name == "image") {
		imagePath_ = value;
	}
}

void AnimalEyesFilter::process(cv::Mat& image) {
		cv::Mat blendImage;
		bool square = image.cols == image.rows;
		blendImage = cv::imread(getFullAssetPath(imagePath_, image, square), CV_LOAD_IMAGE_UNCHANGED);
		if (blendImage.empty()) {
			blendImage = cv::imread(getFullAssetPath(imagePath_, image, !square),
					CV_LOAD_IMAGE_UNCHANGED);
			if (blendImage.empty()) {
				return;
			}
		}
		int cols = blendImage.cols;
		int rows = blendImage.rows;
		if (cols != image.cols || rows != image.rows) {
			resize(blendImage, blendImage, cv::Size(image.cols, image.rows), 0, 0,
					cv::INTER_LINEAR);
		}
//		image = blendImage;

	//	image = cv::imread(getFullAssetPath(imagePath_, image, true), CV_LOAD_IMAGE_UNCHANGED);


		int blendNumChannels = blendImage.channels();
		int srcNumChannels = image.channels();


		cv::Mat tmp;
		cv::Size size = image.size();
		size.height = 4 * size.height;
		tmp.create(size, image.type());

		cv::Vec3b blendPixel;
		cv::Vec3b transp30Pixel;
		cv::Vec3b transp60Pixel;

		for (int i = 0; i < image.cols; i++) {
			for (int j = 0; j < image.rows; j++) {
				cv::Vec3b sourcePixel = image.at < cv::Vec3b > (j, i);

				uchar blendA = 255;
				if (blendNumChannels == 4) {
					cv::Vec4b blendPixel4 = blendImage.at < cv::Vec4b > (j, i);
					blendA = blendPixel4[3];
					blendPixel = cv::Vec3b(blendPixel4[0], blendPixel4[1], blendPixel4[2]);
				} else if (blendNumChannels == 1) {
					uchar gray = blendImage.at < uchar > (j, i);
					blendPixel = cv::Vec3b(gray, gray, gray);
				} else {
					blendPixel = blendImage.at < cv::Vec3b > (j, i);
				}

				for (int k = 0; k < srcNumChannels; k++) {
					transp30Pixel[k] = blendEyesTransparencyComp((30 * 255 / 100), sourcePixel[k], blendPixel[k]);//30%
					transp60Pixel[k] = blendEyesTransparencyComp((60 * 255 / 100), sourcePixel[k], blendPixel[k]);//60%
				}
				tmp.at < cv::Vec3b > (j, i) = sourcePixel;
				tmp.at < cv::Vec3b > (j + image.rows, i) = transp30Pixel;
				tmp.at < cv::Vec3b > (j + 2 * image.rows, i) = transp60Pixel;
				tmp.at < cv::Vec3b > (j + 3 * image.rows, i) = blendPixel;
			}
		}

		image = tmp;
		blendImage.release();
}

uchar AnimalEyesFilter::blendEyesOverlay(uchar B, uchar A) {
	return ((uchar)(
			(B < 128) ?
					(2 * A * B / 255) : (255 - 2 * (255 - A) * (255 - B) / 255)));
}

uchar AnimalEyesFilter::blendEyesTransparencyComp(uchar blendA, uchar sourceComp,
		uchar blendComp) {
	if (blendA == 0) {
		return sourceComp;
	}
	if (blendA == 255) {
		return blendComp;
	}
	double k = blendA / 255.0f;
	return sourceComp - (sourceComp - blendComp) * k;
}

