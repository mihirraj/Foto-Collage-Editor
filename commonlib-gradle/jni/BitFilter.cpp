#include "BitFilter.h"
#include "utils.h"

void BitFilter::process(cv::Mat &image) {
  BitFilter::bitFilterOpenCV(image, image, pixelSize_, colorsPerChannel_, borderColor_, borderSize_);
}

void BitFilter::setParam(const std::string &name, const std::string &value) {
  if (name == "pixel_size") {
    pixelSize_ = atoi(value.c_str());
  } else if (name == "colors_per_channel") {
    colorsPerChannel_ = atoi(value.c_str());
  } else if (name == "border_size") {
    borderSize_ = atoi(value.c_str());
  } else if (name == "border_color") {
    borderColor_ = atoi(value.c_str());
  }
}

void BitFilter::bitFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage, uchar pixelSize, uchar colorsPerChannel, int borderColor, uchar borderSize) {
  
  int sqSize = pixelSize * pixelSize;
  int pixelAndBorder = pixelSize + borderSize;
  int colorDiv = 256 / colorsPerChannel;
  int maxX = sourceImage.cols - 1;
  int maxY = sourceImage.rows - 1;
  int borderRed = (borderColor >> 16) & 0xFF;
  int borderGreen = (borderColor >> 8) & 0xFF;
  int borderBlue = borderColor & 0xFF;
  cv::Vec3b borderColorVec(borderBlue, borderGreen, borderRed);
  for (int y = 0; y < sourceImage.rows; y += pixelAndBorder) {
    for (int x = 0; x < sourceImage.cols; x += pixelAndBorder) {
      int avgR = 0;
      int avgG = 0;
      int avgB = 0;
      for (int i = 0; i < pixelSize; ++i) {
        int cY = constrain(y + i, 0, maxY);
        for (int j = 0; j < pixelSize; j++) {
          int cX = constrain(x + j, 0, maxX);
          cv::Vec3b sourcePixel = sourceImage.at<cv::Vec3b>(cY, cX);
          avgR += sourcePixel[2];
          avgG += sourcePixel[1];
          avgB += sourcePixel[0];
        }
      }
      
      avgR /= sqSize;
      avgG /= sqSize;
      avgB /= sqSize;
      
      // palette adjustments
      avgR = (avgR / colorDiv) * colorDiv;
      avgG = (avgG / colorDiv) * colorDiv;
      avgB = (avgB / colorDiv) * colorDiv;
      cv::Vec3b avgColor(avgB, avgG, avgR);
      for (int i = 0; i < pixelAndBorder && y + i <= maxY; ++i) {
        int cY = y + i;
        for (int j = 0; j < pixelAndBorder && x + j <= maxX; j++) {
          int cX = x + j;
          if (i < pixelSize && j < pixelSize)
            resultImage.at<cv::Vec3b>(cY, cX) = avgColor;
          else
            resultImage.at<cv::Vec3b>(cY, cX) = borderColorVec;
        }
      }
    }
  }
}
