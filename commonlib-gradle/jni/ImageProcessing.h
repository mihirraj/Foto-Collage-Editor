#ifndef _IMAGE_PROCESSING_H_
#define _IMAGE_PROCESSING_H_

#include "jsonxx.h"
#include <opencv2/core/core.hpp>
#include "BaseOpenCvFilter.h"

class ImageProcessing {
public:
  ImageProcessing(const jsonxx::Object& config);
  void process(cv::Mat& image, std::string presetName);
  void process(cv::Mat& image);
  void process(std::vector<cv::Mat*> images, std::string presetName);
  void process(const std::vector<cv::Mat*> & images, const std::vector<std::string> & presetNames, cv::Mat& image);
  bool presetsSortFunc(std::string preset1, std::string preset2);
  void cancelProcessing();
  bool isCancelled() const;

private:
  const jsonxx::Object* config_;
  const jsonxx::Object* getConfig(std::string presetName);
  BaseOpenCvFilter* createFilter(std::string filterName);
  void setFilterParams(BaseOpenCvFilter* filter, const jsonxx::Object & filterConfig);
  bool cancelled_;
};
#endif
