#include "InkFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "PosterizeFilter.h"
#include "EdgeFilter.h"
#include "ThresholdFilter.h"
#include "CurveFilter.h"
#include "utils.h"
#include "logging.h"

InkFilter::InkFilter() {
}

void InkFilter::setParam(const std::string &name, const std::string &value) {
}

uchar blendTransparencyCompA(uchar blendA, uchar sourceComp, uchar blendComp) {
	if (blendA == 0) {
		return sourceComp;
	}
	if (blendA == 255) {
		return blendComp;
	}
	double k = blendA / 255.0f;
	return sourceComp - (sourceComp - blendComp) * k;
}

void blendFilterOpenCVAlphaA(const cv::Mat & sourceImage,
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
				resultPixel[k] = blendTransparencyCompA(alpha, sourcePixel[k],
						blendPixel[k]);
			}
			resultImage.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}
}

void oilPainting(cv::Mat& image, int D) {
	cv::Mat oilMat;
	image.copyTo(oilMat);

	int window = D / 2;
	int cols = image.cols;
	int rows = image.rows;

	int intensities[256];
	int red[256];
	int green[256];
	int blue[256];
	for (int i = window; i < cols - window - 1; i++) {
		for (int j = window; j < rows - window - 1; j++) {
			for (int t = 0; t < 256; t++) {
				intensities[t] = 0; //clear
				red[t] = 0; //clear
				green[t] = 0; //clear
				blue[t] = 0; //clear
			}
			for (int k = i - window; k <= i + window; k++) {
				for (int r = j - window; r <= j + window; r++) {
					cv::Vec3b source = image.at < cv::Vec3b > (r, k);
					int intensity = (21 * source[2] + 72 * source[1]
							+ 7 * source[0]) / 100;
					if (intensity < 0) {
						intensity = 0;
					}
					if (intensity > 255) {
						intensity = 255;
					}
					intensities[intensity]++;
					red[intensity] += source[2];
					green[intensity] += source[1];
					blue[intensity] += source[0];
				}
			}

			// get most frequent intesity
			int maxIntensity = 0;
			int value = 0;

			for (int t = 0; t < 256; t++) {
				if (intensities[t] > value) {
					maxIntensity = t;
					value = intensities[t];
				}
			}

			cv::Vec3b result;
			result[2] = (red[maxIntensity] / intensities[maxIntensity]);
			result[1] = (green[maxIntensity] / intensities[maxIntensity]);
			result[0] = (blue[maxIntensity] / intensities[maxIntensity]);
			image.at < cv::Vec3b > (j, i) = result;
		}
	}
	image = oilMat;
}

void threshold(cv::Mat& image, int low_th, int high_th) {
	int cols = image.cols;
	int rows = image.rows;
	cv::Vec3b whitePixel;
	whitePixel[0] = 255;
	whitePixel[1] = 255;
	whitePixel[2] = 255;
	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			cv::Vec3b source = image.at < cv::Vec3b > (j, i);
			int intensity = (21 * source[2] + 72 * source[1] + 7 * source[0]) / 100;
			if (intensity > high_th) {
				image.at < cv::Vec3b > (j, i) = whitePixel;
			} if (intensity < low_th) {
				image.at < cv::Vec3b > (j, i) = whitePixel;
			}
			else {
				image.at < cv::Vec3b > (j, i) = source;
			}
		}
	}
}

void clear(cv::Mat& image) {
	int cols = image.cols;
	int rows = image.rows;
	cv::Vec3b whitePixel;
	whitePixel[0] = 255;
	whitePixel[1] = 255;
	whitePixel[2] = 255;
	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			image.at < cv::Vec3b > (j, i) = whitePixel;
		}
	}
}

void merge(cv::Mat& image, cv::Mat& addedMat) {
	int cols = image.cols;
	int rows = image.rows;
	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			cv::Vec3b source = addedMat.at < cv::Vec3b > (j, i);
			if ((source[0] != 255) && (source[1] != 255) && (source[2] != 255)) {
				image.at < cv::Vec3b > (j, i) = source;
			}
		}
	}
}

void posterize(cv::Mat& image) {
	pyrMeanShiftFiltering(image, image, 10, 20);
}

void diffuseMap(cv::Mat& image, int D, int blurSize) {
	srand (time(NULL));
	int cols = image.cols;
	int rows = image.rows;
	int R = D / 2;
	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			cv::Vec3b source = image.at < cv::Vec3b > (j, i);
			int dj = (rand() % D) - R;
			int di = (rand() % D) - R;
			int rj = j + dj;
			int ri = i + di;
			if (rj < 0) {
				rj = 0;
			}
			if (rj > rows - 1) {
				rj = rows - 1;
			}
			if (ri < 0) {
				ri = 0;
			}
			if (ri > cols - 1) {
				ri = cols - 1;
			}

			image.at < cv::Vec3b > (rj, ri) = source;
		}
	}
	if (blurSize != 0) {
		medianBlur(image, image, blurSize);
	}
}

void setEdges(cv::Mat& image, float weight) {
	cv::Mat edges;

	cv::Mat grad_x, grad_y;
	cv::Mat abs_grad_x, abs_grad_y;
	int ddepth = CV_16S;
	cv::Scharr(image, grad_x, ddepth, 1, 0);
	cv::convertScaleAbs(grad_x, abs_grad_x);
	cv::Scharr(image, grad_y, ddepth, 0, 1);
	cv::convertScaleAbs(grad_y, abs_grad_y);
	addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, edges);

	cv::bitwise_not(edges, edges);

	int alpha = (int) (weight * 255);
	blendFilterOpenCVAlphaA(image, edges, image, alpha);
	edges.release();
}

void InkFilter::process(cv::Mat& image) {
	cv::Mat diffuseMat;
	cv::Mat medianMat;
	cv::Mat reference;

	image.copyTo(reference);
	clear(image);
	CurveFilter curveFilter;
	int count = 1;
	for (int uplevel = 250; uplevel > 0; uplevel -= 30) {
		int lowlevel = uplevel - 30;
		reference.copyTo(diffuseMat);
		if (count == 0) {
			threshold(diffuseMat, lowlevel, 255);
		} else {
			if (lowlevel > 0) {
				threshold(diffuseMat, lowlevel, uplevel);
			} else {
				threshold(diffuseMat, 0, uplevel);
			}
		}
		medianBlur(diffuseMat, diffuseMat, 11);
		//posterize(diffuseMat);
		diffuseMap(diffuseMat, 21, 5); //D, blurSize
		if ((count % 2) == 0) {
			curveFilter.setParam("green_curve", "0,0;107,117;255,255");
			curveFilter.setParam("red_curve", "0,0;107,117;255,255");
			curveFilter.setParam("blue_curve", "0,0;107,117;255,255");
		}
		curveFilter.process(diffuseMat);
		merge(image, diffuseMat);
		count++;
	}
	medianBlur(image, image, 5);
	setEdges(image, 0.08);//weight
}

