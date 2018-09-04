#include <string>
#include "StickerFilter.h"
#include "BlendFilter.h"
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"
#include "logging.h"

void StickerFilter::setParam(const std::string& name,
		const std::string& value) {
	std::stringstream ss(value);
	if (name == "angle") {
		ss >> angle;
	} else if (name == "path") {
		path = value;
	} else if (name == "scale_w") {
		ss >> scaleX;
	} else if (name == "scale_h") {
		ss >> scaleY;
	} else if (name == "x") {
		ss >> centerX;
	} else if (name == "y") {
		ss >> centerY;
	} else if (name == "color") {
		ss >> color;
	} else if (name == "alpha") {
		ss >> alpha;
	}
}

void StickerFilter::process(cv::Mat& image) {
	cv::Mat sticker = cv::imread(getFullAssetPath(path, image, false),
			CV_LOAD_IMAGE_UNCHANGED);
	if (sticker.empty()) {
		sticker = cv::imread(getFullAssetPath(path, image, true),
				CV_LOAD_IMAGE_UNCHANGED);
		//return;
	}
	LOGI( "--- STICKER SIZE %d %d ", image.cols, image.rows);
	cv::Mat rotatedSticker;
	rotateImage(sticker, rotatedSticker, angle, scaleX * image.cols,
			scaleY * image.rows);
	BlendFilter::blendSticker(image, rotatedSticker, image, centerX, centerY,
			alpha, color);

}

void StickerFilter::rotateImage(cv::Mat & image, cv::Mat & rotatedImage,
		double angle, double scaleW, double scaleH) {

	if (image.cols != image.rows) {
		int squareSize = image.cols;
		if (image.rows > image.cols) {
			squareSize = image.rows;
		}

		cv::Mat squareMat(cv::Size(squareSize, squareSize), image.type());
		squareMat.setTo(cv::Scalar(0, 0, 0, 0));
		int dx = (squareSize - image.cols) / 2;
		int dy = (squareSize - image.rows) / 2;
		for (int i = 0; i < image.cols; i++) {
			for (int j = 0; j < image.rows; j++) {
				squareMat.at < cv::Vec4b > (j + dy, i + dx) = image.at < cv::Vec4b > (j, i);
			}
		}
		image = squareMat;
	}
	resize(image, image,
			cv::Size((int) (image.cols * scaleW + 0.5),
					(int) (image.rows * scaleH + 0.5)), 0, 0, cv::INTER_LINEAR);
	double sinus = fabs(std::sin(angle));
	double cosinus = fabs(std::cos(angle));

	LOGI("--- IMAGE %f %f", sinus, cosinus);
	int newWidth = (int) (image.cols * cosinus + image.rows * sinus);
	int newHeight = (int) (image.cols * sinus + image.rows * cosinus);

	cv::Point center(newWidth / 2, newHeight / 2);
	cv::Size targetSize(newWidth, newHeight);

	cv::Mat targetMat(targetSize, image.type());
	targetMat.setTo(cv::Scalar(0, 0, 0, 0));
	int offsetX = (newWidth - image.cols) / 2;
	int offsetY = (newHeight - image.rows) / 2;

	LOGI(
			"--- IMAGE %d %d %d %d %d %d %f", image.cols, image.rows, newWidth, newHeight, offsetX, offsetY, angle);
	cv::Mat roi = targetMat(cv::Rect(offsetX, offsetY, image.cols, image.rows));
	image.copyTo(roi);
	cv::Mat rotImage = cv::getRotationMatrix2D(center, -(angle * 180.0 / CV_PI),
			1.0);
	warpAffine(targetMat, rotatedImage, rotImage, targetSize, cv::INTER_CUBIC,
			cv::BORDER_CONSTANT, cv::Scalar::all(0));
	//warpAffine(targetMat, rotatedImage, rotImage, targetSize);
}
