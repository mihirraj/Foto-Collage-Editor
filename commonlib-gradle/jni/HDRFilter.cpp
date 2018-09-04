#include "HDRFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include "utils.h"
#include "logging.h"

HDRFilter::HDRFilter(): useShadowsCurve_(false), useHighlightsCurve_(false), useMidtoneCurve_(false)
{
}

void HDRFilter::setParam(const std::string &name, const std::string &value)
{
	if (name == "shadows_curve") {
		shadowsCurveStr_ = value;
		useShadowsCurve_ = true;
	} else if (name == "highlights_curve") {
		highlightsCurveStr_ = value;
		useHighlightsCurve_ = true;
	} else if (name == "midtone_curve") {
		midtoneCurveStr_ = value;
		useMidtoneCurve_ = true;
	}
}

void HDRFilter::equalizeIntensity(cv::Mat & inputImage)
{
    if(inputImage.channels() >= 3)
    {
        cv::Mat ycrcb;

        cv::cvtColor(inputImage, ycrcb, CV_BGR2YCrCb);

        std::vector<cv::Mat> channels;

        split(ycrcb, channels);

        equalizeHist(channels[0], channels[0]);

        merge(channels, ycrcb);

        cv::cvtColor(ycrcb, inputImage, CV_YCrCb2BGR);
    }
}

void HDRFilter::process(cv::Mat& image) {
	//LOGI("CurveFilter::process 1");
	int shadowsCurve[256];
	int highlightsCurve[256];
	int midtoneCurve[256];
	
	if (useShadowsCurve_) {
		fillCurve(shadowsCurve, shadowsCurveStr_);
	}
	if (useHighlightsCurve_) {
		fillCurve(highlightsCurve, highlightsCurveStr_);
	}
	if (useMidtoneCurve_) {
		fillCurve(midtoneCurve, midtoneCurveStr_);
	}

	//equalizeIntensity(image);

	for (int i = 0; i < image.cols; i++) {
		for (int j = 0; j < image.rows; j++) {
			cv::Vec3b resultPixel = image.at<cv::Vec3b>(j, i);
			if (useShadowsCurve_) {
				for (int k = 0; k < 3; ++k) {
					resultPixel[k] = applyShadowsCurve(resultPixel[k], shadowsCurve);
				}
			}
			if (useHighlightsCurve_) {
				for (int k = 0; k < 3; ++k) {
					resultPixel[k] = applyHighlightsCurve(resultPixel[k], highlightsCurve);
				}
			}
			if (useMidtoneCurve_) {
				for (int k = 0; k < 3; ++k) {
					resultPixel[k] = applyShadowsCurve(resultPixel[k], midtoneCurve);
				}
			}
			image.at<cv::Vec3b>(j, i) = resultPixel;
		}
	}
}

uchar HDRFilter::applyShadowsCurve(uchar comp, int* curve) {
	if (!curve) {
		return comp;
	}
	double k = (255.0 - comp)/255.0;
	double result =  curve[comp] * k + comp * (1 - k);
	return (uchar)result;
}

uchar HDRFilter::applyHighlightsCurve(uchar comp, int* curve) {
	if (!curve) {
		return comp;
	}
	double k = comp/255.0;
	double result =  curve[comp] * k + comp * (1 - k);
	return (uchar)result;
}
