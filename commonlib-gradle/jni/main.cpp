// Example showing how to read and write images
#include <opencv2/opencv.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv/cvaux.h>
#include "ColorizeHsvFilter.h"
#include "HsvFilter.h"
#include "BitFilter.h"
#include "GrayFilter.h"
#include "CropRectangleFilter.h"
#include "BlackWhiteScanFilter.h"
#include "BlendFilter.h"
#include "jsonxx.h"
#include "ImageProcessing.h"
#include <fstream>
#include "utils.h"
#include "config.h"

std::vector<int> getJpegParams(int quality) {
    std::vector<int> param = std::vector<int>(2);
    param[0] = CV_IMWRITE_JPEG_QUALITY;
    param[1] = quality; //default(95) 0-100
    return param;
}



void runTurboScan() {
  std::string file_name = "vcard1.jpg";
  cv::Mat srcMat = cv::imread("/Users/romaninozemtsev/Pictures/" + file_name );
  CropRectangleFilter cropFilter;
  cropFilter.process(srcMat);
  GrayFilter grayFilter;
  grayFilter.process(srcMat);
  BlackWhiteScanFilter bwFilter;
  bwFilter.process(srcMat);
  bool res = cv::imwrite("/Users/romaninozemtsev/Pictures/processed_" + file_name, srcMat, getJpegParams(100));
  srcMat.release();
}

void runLomoCameraFilters()
{
  
  Config::resourcePath = "/Users/romaninozemtsev/src/camera/LomoCamera/assets/";
  
  jsonxx::Object config;
  readConfig("preset.json", config);

  std::vector<std::string> cameras;
  cameras.push_back("Lomo Camera");
  cameras.push_back("Old Camera");
  cameras.push_back("UnderWater Camera");
  cameras.push_back("Holga");
  
  std::vector<std::string> processings;
  processings.push_back("Lomo");
  processings.push_back("Push");
  processings.push_back("Polaroid");
  processings.push_back("Paper");
  processings.push_back("Cross");
  processings.push_back("Film");
  processings.push_back("Dreams");
  processings.push_back("Rusty");
  processings.push_back("Black & White");
  processings.push_back("Old Photo");
  processings.push_back("Vintage");
  processings.push_back("Sepia");
  processings.push_back("Novosibirsk");
  processings.push_back("WW-II");
  processings.push_back("Cinema");
  processings.push_back("Childhood");
  
  ImageProcessing processing(config);
  
//  std::vector<cv::Mat *> images;
//  std::vector<std::string> presetNames;
//  presetNames.push_back("Holga");
//  presetNames.push_back("Lomo");
//  
//  
//  processing.process(images, presetNames);

  for (int i = 0; i < cameras.size(); i++) {
    for (int j = 0; j < processings.size(); j++) {
      cv::Mat srcMat = cv::imread("/Users/romaninozemtsev/Pictures/peng.jpg");
      
      std::string camera_name = cameras[i];
      processing.process(srcMat, camera_name);
      std::string processing_name = processings[j];
      processing.process(srcMat, processing_name);
      
      bool res = cv::imwrite("/Users/romaninozemtsev/Pictures/out/" + camera_name + "___" + processing_name + ".jpg", srcMat, getJpegParams(100));
      
      srcMat.release();      
    }
  }
}

int main(int argc, char** argv)
{
  runLomoCameraFilters();
  // runTurboScan();
  return 0;
}