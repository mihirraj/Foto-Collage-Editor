#include "HsvFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"


void HsvFilter::process(cv::Mat& image) {
  HsvFilter::hsvFilterOpenCV(image, image, hue_, saturation_, value_);
}

void HsvFilter::setParam(const std::string& name, const std::string& value) {
  if (name == "h") {
    hue_ = atoi(value.c_str());
  } else if (name == "v" ) {
    value_ = atoi(value.c_str());
  } else if (name == "s") {
    saturation_ = atoi(value.c_str());
  }
}

void HsvFilter::hsvFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage,
                                int hue, int saturation, int value) {
    float m_h = (hue / 360.0f) * 180.0f;
    float m_s = saturation / 100.0f;
    float m_v = value / 100.0f;
    
    cv::cvtColor(sourceImage, sourceImage, CV_BGR2HSV);
    
    for (int i = 0; i < sourceImage.cols; i++) {
        for (int j = 0; j < sourceImage.rows; j++) {
            cv::Vec3b pixel = sourceImage.at<cv::Vec3b>(j, i);
            resultImage.at<cv::Vec3b>(j, i) = modifyPixel(pixel, m_h, m_s, m_v);
        }
    }

    cv::cvtColor(resultImage, resultImage, CV_HSV2BGR);
}

cv::Vec3b HsvFilter::modifyPixel(cv::Vec3b pixel, float hue, float saturation_diff, float vdiff_percent) {
    pixel[0] = (pixel[0] + (int)hue + 180) % 180;
    
    uchar currentSaturation = pixel[1];
    // here vdiff_percent is in range -1..+1
    currentSaturation = constrain(currentSaturation + saturation_diff * currentSaturation, 0, 255);
    pixel[1] = currentSaturation;

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
