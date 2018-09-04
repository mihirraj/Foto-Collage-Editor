#ifndef _UTILS_H_
#define _UTILS_H_

#include <iostream>
#include <vector>
#include <fstream>
#include <sstream>
#include <string>
#include <opencv2/opencv.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv/cvaux.h>
#include "jsonxx.h"

std::vector<std::string> &split(const std::string &s, char delim, std::vector<std::string> &elems);

bool getBool(const std::string& boolStr);

std::string getFullAssetPath(const std::string& relativePath, const cv::Mat & image, bool square);

uchar blendColorDodgeComp(uchar sourceComp, uchar blendComp);

int constrain(int value, int min, int max);

void readConfig(const std::string& fileName, jsonxx::Object& config);

void resizeImage(cv::Mat & image, int maxWidth, int maxHeight);

#endif
