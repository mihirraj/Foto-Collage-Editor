#include "CropRectangleFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

#include "utils.h"
#include "logging.h"

void CropRectangleFilter::setParam(const std::string &name, const std::string &value)
{}

double angle(cv::Point pt1, cv::Point pt2, cv::Point pt0) {
  double dx1 = pt1.x - pt0.x;
  double dy1 = pt1.y - pt0.y;
  double dx2 = pt2.x - pt0.x;
  double dy2 = pt2.y - pt0.y;
  return (dx1*dx2 + dy1*dy2)/sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
}

void find_squares(cv::Mat& image, std::vector<std::vector<cv::Point> >& squares)
{
  // blur will enhance edge detection
  cv::Mat blurred = image.clone();
  cv::medianBlur(blurred, blurred, 9);
  
  cv::Mat gray0(blurred.size(), CV_8U), gray;
  std::vector<std::vector<cv::Point> > contours;
  
  // find squares in every color plane of the image
  for (int c = 0; c < 3; c++)
  {
    int ch[] = {c, 0};
    cv::mixChannels(&blurred, 1, &gray0, 1, ch, 1);
    
    // try several threshold levels
    const int threshold_level = 2;
    for (int l = 0; l < threshold_level; l++)
    {
      // Use Canny instead of zero threshold level!
      // Canny helps to catch squares with gradient shading
      if (l == 0)
      {
        cv::Canny(gray0, gray, 10, 20, 3); //
        
        // Dilate helps to remove potential holes between edge segments
        cv::dilate(gray, gray, cv::Mat(), cv::Point(-1,-1));
      }
      else
      {
        gray = gray0 >= (l+1) * 255 / threshold_level;
      }
      
      // Find contours and store them in a list
      cv::findContours(gray, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
      
      // Test contours
      std::vector<cv::Point> approx;
      for (size_t i = 0; i < contours.size(); i++)
      {
        // approximate contour with accuracy proportional
        // to the contour perimeter
        cv::approxPolyDP(cv::Mat(contours[i]), approx, cv::arcLength(cv::Mat(contours[i]), true)*0.02, true);
        
        // Note: absolute value of an area is used because
        // area may be positive or negative - in accordance with the
        // contour orientation
        if (approx.size() == 4 &&
            fabs(cv::contourArea(cv::Mat(approx))) > 1000 &&
            cv::isContourConvex(cv::Mat(approx)))
        {
          double maxCosine = 0;
          
          for (int j = 2; j < 5; j++)
          {
            double cosine = fabs(angle(approx[j%4], approx[j-2], approx[j-1]));
            maxCosine = MAX(maxCosine, cosine);
          }
          
          if (maxCosine < 0.3)
            squares.push_back(approx);
        }
      }
    }
  }
}

void debugSquares(std::vector<std::vector<cv::Point> >& squares, cv::Mat& image )
{
  for ( int i = 0; i< squares.size(); i++ ) {
    // draw contour
    cv::drawContours(image, squares, i, cv::Scalar(255,0,0), 1, 8, std::vector<cv::Vec4i>(), 0, cv::Point());
    
    // draw bounding rect
    cv::Rect rect = boundingRect(cv::Mat(squares[i]));
    cv::rectangle(image, rect.tl(), rect.br(), cv::Scalar(0,255,0), 2, 8, 0);
    
    // draw rotated rect
    cv::RotatedRect minRect = minAreaRect(cv::Mat(squares[i]));
    cv::Point2f rect_points[4];
    minRect.points( rect_points );
    for ( int j = 0; j < 4; j++ ) {
      cv::line( image, rect_points[j], rect_points[(j+1)%4], cv::Scalar(0,0,255), 1, 8 ); // blue
    }
  }
}

void CropRectangleFilter::process(cv::Mat& image) {
  std::vector<std::vector<cv::Point> > squares;
  find_squares(image, squares);
  // find biggest square
  // TODO
  // for now we have just one
  LOGI("DONE! %d", squares.size());
  for (int i = 0;i < squares.size(); i++) {
    const std::vector<cv::Point>& sq = squares[i];;
    for (int j = 0; j < sq.size(); j++) {
      LOGI("%d %d", sq[j].x, sq[j].y);
    }
  }
  // debugSquares(squares, image);
  cv::RotatedRect rect = cv::minAreaRect(cv::Mat(squares[0]));
  float angle = rect.angle;
  cv::Size rect_size = rect.size;
  if (angle < -45.) {
    angle += 90.0;
    std::swap(rect_size.width, rect_size.height);
  }
  cv::Mat rot_mat = cv::getRotationMatrix2D(rect.center, angle, 1);
  cv::warpAffine(image, image, rot_mat, image.size(), cv::INTER_CUBIC);
  cv::getRectSubPix(image, rect_size, rect.center, image);
}
