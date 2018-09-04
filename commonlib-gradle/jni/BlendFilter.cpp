#include "BlendFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"
#include "logging.h"
#include "SaveImageFilter.h"

BlendFilter::BlendFilter() {
	blend_with_image_memory = false;
	alpha = 0;
}

void BlendFilter::setParam(const std::string& name, const std::string& value) {
	if (name == "algorithm") {
		if (value == "colorDodge") {
			algorithm_ = ALGORITHM_COLOR_DODGE;
		} else if (value == "screen") {
			algorithm_ = ALGORITHM_SCREEN;
		} else if (value == "multiply") {
			algorithm_ = ALGORITHM_MULTIPLY;
		} else if (value == "transparency") {
			algorithm_ = ALGORITHM_TRANSPARENCY;
		} else if (value == "transparency_alpha") {
			algorithm_ = ALGORITHM_TRANSPARENCY_ALPHA;
		} else if (value == "overlay") {
			algorithm_ = ALGORITHM_OVERLAY;
		} else if (value == "hue") {
			algorithm_ = ALGORITHM_HUE;
		}
	} else if (name == "image") {
		imagePath_ = value;
	} else if (name == "blend_with_image_memory") {
		if (value == "true") {
			blend_with_image_memory = true;
		}
	} else if (name == "position") {
		// nobody cares for now
	} else if (name == "alpha") {
		alpha = atoi(value.c_str());
	}
}

void blendFilterOpenCVAlpha(const cv::Mat & sourceImage,
		const cv::Mat & blendImage, cv::Mat & resultImage, int alpha) {
	int cols = std::min(sourceImage.cols, blendImage.cols);
	int rows = std::min(sourceImage.rows, blendImage.rows);
	int blendNumChannels = blendImage.channels();
	int srcNumChannels = sourceImage.channels();

	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			cv::Vec3b sourcePixel = sourceImage.at < cv::Vec3b > (j, i);
			cv::Vec3b blendPixel;
			uchar blendA = 255;
			if (blendNumChannels == 4) {
				cv::Vec4b blendPixel4 = blendImage.at < cv::Vec4b > (j, i);
				blendA = blendPixel4[3];
				blendPixel = cv::Vec3b(blendPixel4[0], blendPixel4[1],
						blendPixel4[2]);
			} else if (blendNumChannels == 1) {
				uchar gray = blendImage.at < uchar > (j, i);
				blendPixel = cv::Vec3b(gray, gray, gray);
			} else {
				blendPixel = blendImage.at < cv::Vec3b > (j, i);
			}
			cv::Vec3b resultPixel;
			for (int k = 0; k < srcNumChannels; k++) {
				resultPixel[k] = BlendFilter::blendTransparencyComp(alpha,
						sourcePixel[k], blendPixel[k]);
			}
			resultImage.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}
}

void BlendFilter::process(cv::Mat& image) {
	bool square = image.cols == image.rows;
	cv::Mat blendImage;
	if (blend_with_image_memory) {
		SaveImageFilter::memory.copyTo(blendImage);
	} else {
		blendImage = cv::imread(getFullAssetPath(imagePath_, image, square),
				CV_LOAD_IMAGE_UNCHANGED);
	}
	if (blendImage.empty()) {
		blendImage = cv::imread(getFullAssetPath(imagePath_, image, !square),
				CV_LOAD_IMAGE_UNCHANGED);
	}
	if (blendImage.empty()) {
			blendImage = cv::imread(imagePath_, CV_LOAD_IMAGE_UNCHANGED);
			if (blendImage.empty()) {
				return;
			}
		}
	int cols = blendImage.cols;
	int rows = blendImage.rows;
	if (cols != image.cols || rows != image.rows) {
		if (image.cols < image.rows && cols > rows) { //potrait photo - texture should be rotated
			int len = cv::min(rows, cols);
			cv::Point2f center(len / 2., len / 2.);
			warpAffine(blendImage, blendImage,
					cv::getRotationMatrix2D(center, -90, 1.0),
					cv::Size(rows, cols));
		}
		resize(blendImage, blendImage, cv::Size(image.cols, image.rows), 0, 0,
				cv::INTER_LINEAR);
	}
	if (algorithm_ == ALGORITHM_TRANSPARENCY_ALPHA) {
		blendFilterOpenCVAlpha(image, blendImage, image, alpha);
	} else {
		BlendFilter::blendFilterOpenCV(image, blendImage, image, algorithm_);
	}
	blendImage.release();
}

void BlendFilter::blendFilterOpenCV(const cv::Mat & sourceImage,
		const cv::Mat & blendImage, cv::Mat & resultImage, int algorithm) {
	int cols = std::min(sourceImage.cols, blendImage.cols);
	int rows = std::min(sourceImage.rows, blendImage.rows);
	int blendNumChannels = blendImage.channels();
	int srcNumChannels = sourceImage.channels();

	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			if (algorithm == ALGORITHM_HUE) {
            	cv::Vec3b sourcePixel = sourceImage.at < cv::Vec3b > (j, i);
                cv::Vec3b blendPixel = blendImage.at < cv::Vec3b > (j, i);
                cv::Vec3b resultPixel = sourceImage.at < cv::Vec3b > (j, i);
                resultPixel[0] = blendPixel[0];
				resultImage.at < cv::Vec3b > (j, i) = resultPixel;
            } else {
				cv::Vec3b sourcePixel = sourceImage.at < cv::Vec3b > (j, i);
				cv::Vec3b blendPixel;
				uchar blendA = 255;
				if (blendNumChannels == 4) {
					cv::Vec4b blendPixel4 = blendImage.at < cv::Vec4b > (j, i);
					blendA = blendPixel4[3];
					blendPixel = cv::Vec3b(blendPixel4[0], blendPixel4[1],
							blendPixel4[2]);
				} else if (blendNumChannels == 1) {
					uchar gray = blendImage.at < uchar > (j, i);
					blendPixel = cv::Vec3b(gray, gray, gray);
				} else {
					blendPixel = blendImage.at < cv::Vec3b > (j, i);
				}
				cv::Vec3b resultPixel;
				for (int k = 0; k < srcNumChannels; k++) {
					if (algorithm == ALGORITHM_SCREEN) {
						resultPixel[k] = BlendFilter::blendScreenComp(sourcePixel[k], blendPixel[k]);
						if (blendNumChannels == 4) {
							resultPixel[k] = BlendFilter::blendTransparencyComp(blendA,	sourcePixel[k], resultPixel[k]);
						}
					} else if (algorithm == ALGORITHM_MULTIPLY) {
						resultPixel[k] = BlendFilter::blendMultiplyComp(sourcePixel[k], blendPixel[k]);
						if (blendNumChannels == 4) {
							resultPixel[k] = BlendFilter::blendTransparencyComp(blendA,	sourcePixel[k], resultPixel[k]);
						}
					} else if (algorithm == ALGORITHM_OVERLAY) {
						resultPixel[k] = BlendFilter::blendOverlay(sourcePixel[k], blendPixel[k]);
						if (blendNumChannels == 4) {
							resultPixel[k] = BlendFilter::blendTransparencyComp(blendA,	sourcePixel[k], resultPixel[k]);
						}
					} else if (algorithm == ALGORITHM_TRANSPARENCY) {
						resultPixel[k] = BlendFilter::blendTransparencyComp(blendA, sourcePixel[k], blendPixel[k]);
					} else if (algorithm == ALGORITHM_COLOR_DODGE) {
						resultPixel[k] = blendColorDodgeComp(sourcePixel[k], blendPixel[k]);
						if (blendNumChannels == 4) {
							resultPixel[k] = BlendFilter::blendTransparencyComp(blendA,	sourcePixel[k], resultPixel[k]);
						}
					} else if (algorithm == ALGORITHM_GETFIRSRT) {
						resultPixel[k] = sourcePixel[k];
					}
				}
				resultImage.at < cv::Vec3b > (j, i) = resultPixel;
			}
		}
	}
}

void BlendFilter::blendFilterTransparencyOpenCV(const cv::Mat & sourceImage,
		const cv::Mat & blendImage, cv::Mat & resultImage, int alpha) {
	int cols = std::min(sourceImage.cols, blendImage.cols);
	int rows = std::min(sourceImage.rows, blendImage.rows);
	int srcNumChannels = sourceImage.channels();
	int blendNumChannels = blendImage.channels();

	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			cv::Vec3b sourcePixel = sourceImage.at < cv::Vec3b > (j, i);
			cv::Vec3b blendPixel;
			if (blendNumChannels == 4) {
				cv::Vec4b blendPixel4 = blendImage.at < cv::Vec4b > (j, i);
				blendPixel = cv::Vec3b(blendPixel4[0], blendPixel4[1],
						blendPixel4[2]);
			} else if (blendNumChannels == 1) {
				uchar gray = blendImage.at < uchar > (j, i);
				blendPixel = cv::Vec3b(gray, gray, gray);
			} else {
				blendPixel = blendImage.at < cv::Vec3b > (j, i);
			}
			cv::Vec3b resultPixel;
			for (int k = 0; k < srcNumChannels; k++) {
				resultPixel[k] = BlendFilter::blendTransparencyComp(alpha,
						sourcePixel[k], blendPixel[k]);
			}
			resultImage.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}
}

void BlendFilter::blendSticker(const cv::Mat & source, const cv::Mat & sticker,
		cv::Mat & result, double centerX, double centerY, int alpha,
		int color) {
	int x = centerX * source.cols;
	int y = centerY * source.rows;
	int offsetX = x - sticker.cols / 2;
	int offsetY = y - sticker.rows / 2;
	int x1 = std::max(offsetX, 0);
	int y1 = std::max(offsetY, 0);
//	offsetX = offesetX < 0 ? abs(offsetX) : 0;
//	offsetY = offesetY < 0 ? abs(offsetY) : 0;
	int x2 = std::min(x + sticker.cols / 2, source.cols);
	int y2 = std::min(y + sticker.rows / 2, source.rows);
	int stickerChannels = sticker.channels();
	int srcNumChannels = source.channels();
	for (int i = x1; i < x2; i++) {
		for (int j = y1; j < y2; j++) {
			cv::Vec3b sourcePixel = source.at < cv::Vec3b > (j, i);
			/*if (color != -1) {
			 sourcePixel.val[0] = (color & 0xFF000000) >> 24;
			 sourcePixel.val[1] = (color & 0x00FF0000) >> 16;
			 sourcePixel.val[2] = (color & 0x0000FF00) >> 8;

			 }*/

			cv::Vec3b blendPixel;

			double blendA = 0.0f;
			if (stickerChannels == 4) {
				cv::Vec4b blendPixel4 = sticker.at < cv::Vec4b
						> (j - offsetY, i - offsetX);
				if (alpha == 255) {
				blendA = blendPixel4[3] * (alpha / 255.0f);

				if (color != 0 && blendPixel4[3] != 0) {

					blendPixel = cv::Vec3b((color & 0x000000FF),
							(color & 0x0000FF00) >> 8,
							(color & 0x00FF0000) >> 16);
				} else {
					blendPixel = cv::Vec3b(blendPixel4[0], blendPixel4[1],
							blendPixel4[2]);
				}
				}
			}

			if (stickerChannels == 3) {
				cv::Vec3b blendPixel3 = sticker.at < cv::Vec3b
									> (j - offsetY, i - offsetX);
				blendPixel = cv::Vec3b(blendPixel3[0], blendPixel3[1],
										blendPixel3[2]);
			}

			cv::Vec3b resultPixel;
			for (int k = 0; k < srcNumChannels; k++) {
				if (stickerChannels == 4) {
				resultPixel[k] = BlendFilter::blendTransparencyComp(blendA,
						sourcePixel[k], blendPixel[k]);
				}
				if (stickerChannels == 3) {
					resultPixel[k] = BlendFilter::blendMultiplyComp(sourcePixel[k], blendPixel[k]);
				}
			}
			result.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}
}

uchar BlendFilter::blendScreenComp(uchar sourceComp, uchar blendComp) {
	return sourceComp + blendComp - (sourceComp * blendComp) / 255;
}

uchar BlendFilter::blendMultiplyComp(uchar sourceComp, uchar blendComp) {
	return (sourceComp * blendComp) / 255;
}

uchar BlendFilter::blendTransparencyComp(uchar blendA, uchar sourceComp,
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

uchar BlendFilter::blendOverlay(uchar B, uchar A) {
	return ((uchar)(
			(B < 128) ?
					(2 * A * B / 255) : (255 - 2 * (255 - A) * (255 - B) / 255)));
}

