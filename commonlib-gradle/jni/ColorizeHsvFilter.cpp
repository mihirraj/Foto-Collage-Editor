#include "ColorizeHsvFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

ColorizeHsvFilter::ColorizeHsvFilter() {
	hue_ = -1;
	saturation_ = -1;
	value_ = 0;
}

void ColorizeHsvFilter::setParam(const std::string& name, const std::string& value) {
  if (name == "h") {
    hue_ = atoi(value.c_str());
  } else if (name == "s") {
    saturation_ = atoi(value.c_str());
  } else if (name == "v") {
    value_ = atoi(value.c_str());
  }
}

void ColorizeHsvFilter::process(cv::Mat & image) {
  if ((hue_ >= 0) && (saturation_ >= 0)) {
	ColorizeHsvFilter::colorizeHsvFilterOpenCV(image, image, hue_, saturation_, value_);
  } else if (saturation_ >= 0) {
	setSaturationFilterOpenCV(image, image, saturation_);
  }
}


void ColorizeHsvFilter::setSaturationFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage,
                                                int saturation) {
    cv::cvtColor(sourceImage, sourceImage, CV_BGR2HSV);

    for (int i = 0; i < sourceImage.cols; i++) {
        for (int j = 0; j < sourceImage.rows; j++) {
            cv::Vec3b pixel = sourceImage.at<cv::Vec3b>(j, i);
            int s = (pixel[1] / 100.0f) * (saturation * 2);
            if (s > 255) {
            	pixel[1] = 255;
            } else {
            	pixel[1] = s;
            }
            resultImage.at<cv::Vec3b>(j, i) = pixel;
        }
    }
    cv::cvtColor(resultImage, resultImage, CV_HSV2BGR);
}

void ColorizeHsvFilter::colorizeHsvFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage,
                                                int hue, int saturation, int value) {

    // convert hue, saturation, value to values in openCV range
    // hue  (0..360) -> (0..179)
    // saturation (0..100) -> (0..255)
    
    float m_h = ((hue / 360.0f) * 180.0f);
    float m_s = ((saturation / 100.0f) * 255.0f);
    float m_v = value / 100.0f;  // becomes in range (-1..1)
    
    cv::cvtColor(sourceImage, sourceImage, CV_BGR2HSV);

    for (int i = 0; i < sourceImage.cols; i++) {
        for (int j = 0; j < sourceImage.rows; j++) {
            cv::Vec3b pixel = sourceImage.at<cv::Vec3b>(j, i);
            resultImage.at<cv::Vec3b>(j, i) = colorizePixel(pixel, m_h, m_s, m_v);
        }
    }
    cv::cvtColor(resultImage, resultImage, CV_HSV2BGR);
}

cv::Vec3b ColorizeHsvFilter::colorizePixel(cv::Vec3b pixel, float hue, float saturation, float vdiff_percent) {
    pixel[0] = hue;
    pixel[1] = saturation;
    uchar currentValue = pixel[2];
    // here vdiff_percent is in range -1..+1
    if (vdiff_percent < 0) {
        currentValue += currentValue * vdiff_percent; // so if vdiff_percent is -1 - it will go to 0.
    } else {
        currentValue += (255 - currentValue) * vdiff_percent;  // the value will be between v and
    }
    pixel[2] = currentValue;
    return pixel;
}
