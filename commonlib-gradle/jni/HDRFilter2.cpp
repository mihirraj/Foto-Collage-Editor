#include "HDRFilter2.h"
#include "GrayFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include "utils.h"
#include "logging.h"

HDRFilter2::HDRFilter2() {
	alpha = 0;
	blurSize = 1;
	black = 0;
	algorithm = ALGORITHM_OLD_HDR;
}

void HDRFilter2::setParam(const std::string &name, const std::string &value) {
	if (name == "alpha") {
		alpha = atoi(value.c_str());
	}
	if (name == "blurSize") {
		blurSize = atoi(value.c_str());
	}
	if (name == "algorithm") {
		if (value == "old_hdr") {
			algorithm = ALGORITHM_OLD_HDR;
		}
		if (value == "hdr") {
			algorithm = ALGORITHM_HDR;
		}
		if (value == "midtones") {
			algorithm = ALGORITHM_MIDTONES;
		}
	}
	if (name == "black") {
		black = atoi(value.c_str());
	}
}

unsigned char HDRFilter2::ChannelBlend_Overlay(unsigned char B,
		unsigned char A) {
	return ((unsigned char) (
			(B < 128) ?
					(2 * A * B / 255) : (255 - 2 * (255 - A) * (255 - B) / 255)));
}

unsigned char ChannelBlend_NormalOverlay(unsigned char A, unsigned char B,
		float alpha) {
	int result = A + B * alpha;
	if (result > 255) {
		result = 255;
	}
	return ((unsigned char) (result));
}

uchar blendTransparencyComp(uchar blendA, uchar sourceComp, uchar blendComp) {
	if (blendA == 0) {
		return sourceComp;
	}
	if (blendA == 255) {
		return blendComp;
	}
	double k = blendA / 255.0f;
	return sourceComp - (sourceComp - blendComp) * k;
}

uchar overlay(uchar B, uchar A) {
	return ((uchar) ((B < 128) ? (2 * A * B / 255) : (255 - 2 * (255 - A) * (255 - B) / 255)));
}

cv::Vec3b specialOverlay(cv::Vec3b base, cv::Vec3b overlay_image) {
	cv::Vec3b result;
	result[0] = overlay(base[0], overlay_image[0]);
	result[1] = overlay(base[1], overlay_image[1]);
	result[2] = overlay(base[2], overlay_image[2]);

	int gray = (overlay_image[0] + overlay_image[1] + overlay_image[2]) / 3;
	if (gray > 190) {
		result[0] = base[0];
		result[1] = base[1];
		result[2] = base[2];
	}
	if ((gray >= 170) && (gray <= 190)) {
		double k = (190 - gray) / 20;
		result[0] = base[0] - (base[0] - result[0]) * k;
		result[1] = base[1] - (base[1] - result[1]) * k;
		result[2] = base[2] - (base[2] - result[2]) * k;
	}
	return result;
}

void HDRFilter2::process(cv::Mat& image) {
	if (algorithm == ALGORITHM_OLD_HDR) {
		cv::Mat bluredImage;
		blur(image, bluredImage, cv::Size(10, 10));
		bluredImage = cv::Scalar::all(255) - bluredImage;
		cv::Mat grayImage;
		cvtColor(bluredImage, grayImage, CV_BGR2GRAY);

		//overlay image and grayImage

		for (int i = 0; i < image.cols; i++) {
			for (int j = 0; j < image.rows; j++) {
				cv::Vec3b sourcePixelA = image.at < cv::Vec3b > (j, i);
				unsigned char sourcePixelB = grayImage.at<unsigned char>(j, i);

				cv::Vec3b resultPixel;
				resultPixel[0] = ChannelBlend_Overlay(sourcePixelA[0],
						sourcePixelB);
				resultPixel[1] = ChannelBlend_Overlay(sourcePixelA[1],
						sourcePixelB);
				resultPixel[2] = ChannelBlend_Overlay(sourcePixelA[2],
						sourcePixelB);
				image.at < cv::Vec3b > (j, i) = resultPixel;
			}
		}
	}
	if (algorithm == ALGORITHM_HDR) {
		cv::Mat bluredImage;
		GaussianBlur(image, bluredImage, cv::Size(blurSize, blurSize), 0);
		bluredImage = cv::Scalar::all(255) - bluredImage;
		cv::Mat grayImage;
		cvtColor(bluredImage, grayImage, CV_BGR2GRAY);
		for (int i = 0; i < image.cols; i++) {
			for (int j = 0; j < image.rows; j++) {
				cv::Vec3b sourcePixelA = image.at < cv::Vec3b > (j, i);
				unsigned char sourcePixelB = grayImage.at<unsigned char>(j, i);

				cv::Vec3b resultPixel;
				for (int ch = 0; ch < 3; ch++) {
					resultPixel[ch] = ChannelBlend_Overlay(sourcePixelA[ch],
							sourcePixelB);
					resultPixel[ch] = blendTransparencyComp(alpha,
							sourcePixelA[ch], resultPixel[ch]);
					//			resultPixel[ch] = ChannelBlend_NormalOverlay(sourcePixelA[ch], resultPixel[ch], 0.5);
				}
				image.at < cv::Vec3b > (j, i) = resultPixel;
			}
		}
	}
	//midtones
	if (algorithm == ALGORITHM_MIDTONES) {
		cv::Mat bluredImage;
		GaussianBlur(image, bluredImage, cv::Size(blurSize, blurSize), 0);
		bluredImage = cv::Scalar::all(255) - bluredImage;
		//cv::Mat grayImage;
		//cvtColor(bluredImage, bluredImage, CV_BGR2GRAY);

		for (int i = 0; i < image.cols; i++) {
			for (int j = 0; j < image.rows; j++) {
				cv::Vec3b sourcePixelA = image.at < cv::Vec3b > (j, i);
				cv::Vec3b sourcePixelB = bluredImage.at < cv::Vec3b > (j, i);

				cv::Vec3b resultPixel;
				for (int ch = 0; ch < 3; ch++) {
					resultPixel[ch] = blendTransparencyComp(128,
							sourcePixelA[ch], sourcePixelB[ch]);
				}
				bluredImage.at < cv::Vec3b > (j, i) = resultPixel;
			}
		}

//		CurveFilter curveFilter;
//		curveFilter.setParam("green_curve", "0,0;78,35;186,233;255,255");
//		curveFilter.setParam("red_curve", "0,0;78,35;186,233;255,255");
//		curveFilter.setParam("blue_curve", "0,0;78,35;186,233;255,255");
//		curveFilter.process(bluredImage);

//		GrayFilter grayFilter;
//		grayFilter.setParam("use3channels", "true");
//		grayFilter.process(bluredImage);

		///////////////////////////////////////////
		if (black != 0) {
			GrayFilter grayFilter;
			grayFilter.setParam("use3channels", "true");
			grayFilter.process(bluredImage);

			std::stringstream ss;
			int inv_black = 255 - black;
			ss << "0,0;" << black << ",0;" << inv_black << ",255;255,255";
			CurveFilter curveFilter;
			curveFilter.setParam("green_curve", ss.str());
			curveFilter.setParam("red_curve", ss.str());
			curveFilter.setParam("blue_curve", ss.str());
			curveFilter.process(bluredImage);
		}

		///////////////////////////////////////////

		//image = bluredImage;

		for (int i = 0; i < image.cols; i++) {
			for (int j = 0; j < image.rows; j++) {
				cv::Vec3b sourcePixelA = image.at < cv::Vec3b > (j, i);
				cv::Vec3b sourcePixelB = bluredImage.at < cv::Vec3b > (j, i);

				cv::Vec3b resultPixel = specialOverlay(sourcePixelA, sourcePixelB);
				for (int ch = 0; ch < 3; ch++) {
					resultPixel[ch] = blendTransparencyComp(alpha,
							sourcePixelA[ch], resultPixel[ch]);
				}
				image.at < cv::Vec3b > (j, i) = resultPixel;
			}
		}

		//cvtColor(image, image, CV_BGR2GRAY);
	}
}
