//
//  utils.cpp
//  native_filters
//
//  Created by Roman Inozemtsev on 7/15/13.
//  Copyright (c) 2013 Roman Inozemtsev. All rights reserved.
//

#include "utils.h"
#include "config.h"
#include "logging.h"


std::vector<std::string> &split(const std::string &s, char delim, std::vector<std::string> &elems) {
  std::stringstream ss(s);
  std::string item;
  while (std::getline(ss, item, delim)) {
    elems.push_back(item);
  }
  return elems;
}

bool getBool(const std::string& boolStr) {
  return boolStr == "true" || boolStr == "True";
}


std::string getFullAssetPath(const std::string& relativePath, const cv::Mat & image, bool square) {
  std::string basePath = "sd/";
	//  if (image.cols + image.rows > 800 + 600) {
	//	  basePath = "hd/";
	//  } else {
	//	  basePath = "sd/";
	//  }
  if (square) {
	  basePath += "square/";
  }

  return  Config::resourcePath + basePath + relativePath;
}

void readConfig(const std::string& fileName, jsonxx::Object& config) {
  std::ifstream in((Config::resourcePath + fileName).c_str());
  std::string contents((std::istreambuf_iterator<char>(in)),
                       std::istreambuf_iterator<char>());
  
  config.parse(contents);
}


uchar blendColorDodgeComp(uchar sourceComp, uchar blendComp) {
  if (blendComp == 255) {
    return blendComp;
  }
  return std::min(255, (sourceComp << 8) / (255 - blendComp));
}

int constrain(int value, int min, int max) {
  if (value < min) {
    return min;
  }
  if (value > max) {
    return max;
  }
  return value;
}

void mantainAspectRation(cv::Mat & image, int maxWidth, int maxHeight) {
	int maxSide = std::max(maxWidth, maxHeight);
	int minSide = std::min(maxWidth, maxHeight);
	double imageAspect = std::max(image.cols, image.rows) / (double)(std::min(image.cols, image.rows));
	double resizeAspect = maxSide / (double)(minSide);
	static const double ASPECT_TOLERANCE = 0.05;
	if (std::fabs(imageAspect - resizeAspect) <= ASPECT_TOLERANCE) {
		resize(image, image, cv::Size(maxWidth, maxHeight), 0, 0, cv::INTER_LINEAR);
	} else {
		if (image.cols > image.rows) {
			resize(image, image, cv::Size((int)minSide * imageAspect, minSide), 0, 0, cv::INTER_LINEAR);
		} else {
			resize(image, image, cv::Size(minSide, (int)minSide * imageAspect), 0, 0, cv::INTER_LINEAR);
		}
	}
}

void resizeImage(cv::Mat & image, int maxWidth, int maxHeight) {
	if (image.cols >= image.rows) { //landscape photo
		if (image.cols > maxWidth && image.rows > maxHeight) {
			if (image.cols == image.rows) {
				int side = std::min(maxWidth, maxHeight);
				resize(image, image, cv::Size(side, side), 0, 0, cv::INTER_LINEAR);
			} else {
				mantainAspectRation(image, maxWidth, maxHeight);
				//resize(image, image, cv::Size(maxWidth, maxHeight), 0, 0, cv::INTER_LINEAR);
			}
		}
	} else if (image.cols > maxHeight && image.rows > maxWidth) { //portrait photo
		LOGI("resize portrait photo");
		mantainAspectRation(image, maxHeight, maxWidth);
		//resize(image, image, cv::Size(maxHeight, maxWidth), 0, 0, cv::INTER_LINEAR);
	}
}
