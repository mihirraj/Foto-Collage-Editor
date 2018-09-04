#include "ComicFilter.h"
#include "utils.h"
#include <opencv2/opencv.hpp>

#define IMAGES_PARAM "images"
#define PATTERN_SIZE_PARAM "pattern_size"
#define COLOR_DODGE_PARAM "color_dodge"
#define SMALL_PATTERN_PARAM "small_pattern"

void fillImages(const std::string& imagesString, std::vector<std::string>& images, int* brightnessImageMap) {
  std::vector<std::string> pairs;
  std::vector<int> brightnesses;
  split(imagesString, ';', pairs);
  for (int i = 0; i < pairs.size(); i++) {
    std::vector<std::string> vals;
    split(pairs[i], ':', vals);
    images.push_back(vals[1]);
    brightnesses.push_back(atoi(vals[0].c_str()));
  }
  
  int imageIndex = 0;
  int curBrighness = brightnesses[imageIndex];
  for (int i = 0; i < 256; i++) {
    if (i < curBrighness) {
      brightnessImageMap[i] = imageIndex;
    } else {
      imageIndex++;
      curBrighness = brightnesses[imageIndex];
      brightnessImageMap[i] = imageIndex;
    }
  }
}

void ComicFilter::setParam(const std::string &name, const std::string &value) {
  if (name == IMAGES_PARAM) {
    imagesConfig_ = value;
  } else if (name == PATTERN_SIZE_PARAM) {
    patternSize_ = atoi(value.c_str());
  } else if (name == COLOR_DODGE_PARAM) {
    colorDodge_ = getBool(value);
  } else if (name == SMALL_PATTERN_PARAM) {
    smallPattern_ = getBool(value);
  }
}

void ComicFilter::process(cv::Mat& image) {
  // parse params
  std::vector<std::string> images;
  int brightnessImageMap[256];
  
  fillImages(imagesConfig_, images, brightnessImageMap);

  // two cases possible:
  if (smallPattern_) {
    processSmallPattern(image, colorDodge_, patternSize_, images, brightnessImageMap);
  } else {
    processBigPattern(image, colorDodge_, patternSize_, images, brightnessImageMap);
  }
}

void ComicFilter::processBigPattern(cv::Mat& image, bool colorDodge, int patternSize, const std::vector<std::string>& images, const int brightnessImageMap[]) {
  cv::Mat brightnessImg;
  cv::cvtColor(image, brightnessImg, CV_BGR2GRAY);
  for (int i = 0 ; i < images.size(); i++) {
    cv::Mat pattern = cv::imread(getFullAssetPath(images[i], image, true));
    if (pattern.empty()) {
        pattern = cv::imread(getFullAssetPath(images[i], image, false));
        if (pattern.empty()) {
    	  return;
	}
    }
    if (pattern.rows < image.rows || pattern.cols < image.cols) {
    	resize(pattern, pattern, cv::Size(image.cols, image.rows), 0, 0, cv::INTER_LINEAR);
    }
    for (int y = 0; y < image.rows; y ++) {
      for (int x = 0; x < image.cols; x ++) {
        uchar brightness = brightnessImg.at<uchar>(y, x);
        int imageIndex = brightnessImageMap[brightness];
        if (imageIndex != i) {  // only one brightness at a time
          continue;
        }
        cv::Vec3b patternPixel = pattern.at<cv::Vec3b>(y % pattern.rows, x % pattern.cols);
        if (colorDodge) {
          const cv::Vec3b& sourcePixel = image.at<cv::Vec3b>(y, x);
          patternPixel[2] = blendColorDodgeComp(sourcePixel[2], patternPixel[2]);
          patternPixel[1] = blendColorDodgeComp(sourcePixel[1], patternPixel[1]);
          patternPixel[0] = blendColorDodgeComp(sourcePixel[0], patternPixel[0]);
        }
        image.at<cv::Vec3b>(y, x) = patternPixel;
      }
    }
    pattern.release();
  }
}

void ComicFilter::processSmallPattern(cv::Mat& image, bool colorDodge, int patternSize, const std::vector<std::string>& images, const int  brightnessImageMap[]) {
  // load all images initially
  std::vector<cv::Mat> imageMats;
  for (int i = 0; i < images.size(); i++)
  {
    std::string fullPath = getFullAssetPath(images[i], image, true);
    cv::Mat m = cv::imread(fullPath);
    if (m.empty()) {
      m = cv::imread(getFullAssetPath(images[i], image, false));
    }
    imageMats.push_back(m);
  }
  
  int maxX = image.cols - 1;
  int maxY = image.rows - 1;
  for (int y = 0; y < image.rows; y += patternSize) {
    for (int x = 0; x < image.cols; x += patternSize) {
      for (int i = 0; i < patternSize; ++i) {
        int cY = constrain(y + i, 0, maxY);
        for (int j = 0; j < patternSize; j++) {
          int cX = constrain(x + j, 0, maxX);
          const cv::Vec3b& sourcePixel = image.at<cv::Vec3b>(cY, cX);
          int brightness = (sourcePixel[0] + sourcePixel[1] + sourcePixel[2]) / 4;
          int imageIndex = brightnessImageMap[brightness];
          const cv::Mat& patternMat = imageMats[imageIndex];
          cv::Vec3b patternPixel = patternMat.at<cv::Vec3b>(i, j);
          if (colorDodge) {
            patternPixel[2] = blendColorDodgeComp(sourcePixel[2], patternPixel[2]);
            patternPixel[1] = blendColorDodgeComp(sourcePixel[1], patternPixel[1]);
            patternPixel[0] = blendColorDodgeComp(sourcePixel[0], patternPixel[0]);
          }
          image.at<cv::Vec3b>(cY, cX) = patternPixel;
        }
      }
    }
  }
  
  for (int i = 0; i < imageMats.size(); i++) {
    imageMats[i].release();
  }
}

