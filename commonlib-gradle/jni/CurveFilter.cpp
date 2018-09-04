#include "CurveFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

#include "utils.h"
#include "logging.h"

CurveFilter::CurveFilter(): useBlueCurve_(false), useGreenCurve_(false), useRedCurve_(false) {
}

void CurveFilter::setParam(const std::string &name, const std::string &value)
{
  if (name == "green_curve") {
    greenCurveStr_ = value;
    useGreenCurve_ = true;
  } else if (name == "red_curve") {
    redCurveStr_ = value;
    useRedCurve_ = true;
  } else if (name == "blue_curve") {
    blueCurveStr_ = value;
    useBlueCurve_ = true;
  }
}

void CurveFilter::process(cv::Mat& image) {
	//LOGI("CurveFilter::process 1");
  int redCurve[256];
  int greenCurve[256];
  int blueCurve[256];

  fillCurve(redCurve, redCurveStr_);
  fillCurve(greenCurve, greenCurveStr_);
  fillCurve(blueCurve, blueCurveStr_);
  
  CurveFilter::curveFilterOpenCV(image, image, redCurve, greenCurve, blueCurve);
	//LOGI("CurveFilter::process 5");
}

void CurveFilter::curveFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage, int* redCurve, int* greenCurve, int* blueCurve) {
	//LOGI("CurveFilter::curveFilterOpenCV ch1:%d %dx%d ch2:%d %dx%d", sourceImage.channels(), sourceImage.cols, sourceImage.rows,
			//resultImage.channels(), resultImage.cols, resultImage.rows);
  for (int i = 0; i < sourceImage.cols; i++) {
      for (int j = 0; j < sourceImage.rows; j++) {
          cv::Vec3b sourcePixel = sourceImage.at<cv::Vec3b>(j, i);
          cv::Vec3b resultPixel;
          resultPixel[0] = useBlueCurve_ ? CurveFilter::applyCurve(sourcePixel[0], blueCurve) : sourcePixel[0];
          resultPixel[1] = useGreenCurve_ ? CurveFilter::applyCurve(sourcePixel[1], greenCurve) : sourcePixel[1];
          resultPixel[2] = useRedCurve_ ? CurveFilter::applyCurve(sourcePixel[2], redCurve) : sourcePixel[2];
          resultImage.at<cv::Vec3b>(j, i) = resultPixel;
       }
  }
}

uchar CurveFilter::applyCurve(uchar comp, int* curve) {
  if (curve) {
	 // if (comp < 0 || comp > 255) {
	//	  LOGI("CurveFilter::applyCurve %d", comp);
	 // }
    return curve[comp];
  }
  return comp;
}

std::vector<double> CurveFilter::secondDerivative( std::vector< std::vector<int> > points)
{
	//LOGI("secondDerivative 1");
  int n = points.size();
  
  // build the tridiagonal system
  // (assume 0 boundary conditions: y2[0]=y2[-1]=0)
  double matrix[n][3];
  double result[n];

  // init array with 0s.
  for (int i =0 ; i < n; ++i) {
    result[i] = 0;
  }

  // init matrix with zeros as well
  for (int i =0 ; i < n; ++i) {
    for (int j = 0; j < 3; ++j) {
      matrix[i][j] = 0;
    }
  }

  matrix[ 0 ][ 1 ] = 1;
  for( int i = 1; i < n - 1; ++i )
  {
    matrix[ i ][ 0 ] = ( double )( points[ i ][ 0 ] - points[ i - 1 ][ 0 ] ) / 6;
    matrix[ i ][ 1 ] = ( double )( points[ i + 1 ][ 0 ] - points[ i - 1 ][ 0 ] ) / 3;
    int div = points[ i + 1 ][ 0 ] - points[ i ][ 0 ];
    //if (div == 0) {
    	//LOGE("division by zero 1");
   /// }
    int div2 = points[ i ][ 0 ] - points[ i - 1 ][ 0 ] ;
    //if (div2 == 0) {
    	//LOGE("division by zero 2");
    //}
    result[ i ] = ( double )( points[ i + 1 ][ 1 ] - points[ i ][ 1 ] ) /
    ( div ) -
    ( double )( points[ i ][ 1 ] - points[ i - 1 ][ 1 ] ) /
    ( div2 );
  }
  matrix[ n - 1 ][ 1 ] = 1;
	//LOGI("secondDerivative 2");
  
  // solving pass1 (up->down)
  for( int i = 1; i < n; ++i )
  {
	    int div = matrix[ i - 1 ][ 1 ];
	   // if (div == 0) {
	    	//LOGE("division by zero 3");
	   // }
    double k = matrix[ i ][ 0 ] / div;
    matrix[ i ][ 1 ] -= k * matrix[ i - 1 ][ 2 ];
    matrix[ i ][ 0 ] = 0;
    result[ i ] -= k * result[ i - 1 ];
  }
	//LOGI("secondDerivative 3");
  // solving pass2 (down->up)
  for( int i = n - 2; i >= 0; --i )
  {
	    int div = matrix[ i + 1 ][ 1 ];
	   // if (div == 0) {
	    	//LOGE("division by zero 4");
	  //  }
    double k = matrix[ i ][ 2 ] / div;
    matrix[ i ][ 1 ] -= k * matrix[ i + 1 ][ 0 ];
    matrix[ i ][ 2 ] = 0;
    result[ i ] -= k * result[ i + 1 ];
  }
	//LOGI("secondDerivative 4");

  // return second derivative value for each point P
  std::vector<double> y2;
  for( int i = 0; i < n; ++i )
  {
    if (matrix[ i ][ 1 ] == 0) {
      y2.push_back(0);
    }
    else {
      y2.push_back(result[ i ] / matrix[ i ][ 1 ]);
    }
  }
	//LOGI("secondDerivative 5");

  return y2;
}

void CurveFilter::getSpline(const std::vector< std::vector<int> >& key_points, int* result)
{
	//LOGI("getSpline 1");

  std::vector<double> sd = secondDerivative( key_points );
  for( int i = 0; i < key_points.size() - 1; ++i )
  {
    std::vector<int> cur = key_points[ i ];
    std::vector<int> next = key_points[ i + 1 ];
    for( int x = cur[ 0 ]; x < next[ 0 ]; ++x )
    {
	    int div = next[ 0 ] - cur[ 0 ];
	    //if (div == 0) {
	    	//LOGE("division by zero 5");
	    //}
      double t = ( double )( x - cur[ 0 ] ) /
      ( div );
      double a = 1 - t;
      double b = t;
      double h = next[ 0 ] - cur[ 0 ];
      double y = a * cur[ 1 ] +
      b * next[ 1 ] +
      ( h * h / 6 ) *
      (
       ( a * a * a - a ) * sd[ i ] +
       ( b * b * b - b ) * sd[ i + 1 ]
       );
      
      result[ x ] = constrain(y, 0, 255);
    }
  }
  result[ 255 ] = key_points[ key_points.size() - 1 ][ 1 ];
	//LOGI("getSpline 2");
}

void CurveFilter::fillCurve(int* array, std::string curveString) {
	//LOGI("CurveFilter::fillCurve 1");
	if (curveString.empty()) {
		return;
	}
  std::vector<std::string> pairs;
  split(curveString, ';', pairs);
  
  // vector of pairs
  std::vector< std::vector<int> > curveValues;
  for( int i = 0; i < pairs.size(); ++i)
  {
    std::vector<std::string> vals;
    split(pairs[i], ',', vals);
    std::vector<int> item;
    item.push_back(atoi(vals[ 0 ].c_str()));
    item.push_back(atoi(vals[ 1 ].c_str()));
    curveValues.push_back(item);
  }
	//LOGI("CurveFilter::fillCurve 2");
  getSpline(curveValues, array);
	//LOGI("CurveFilter::fillCurve 3");

}
