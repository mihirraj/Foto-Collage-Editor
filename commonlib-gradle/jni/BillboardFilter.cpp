#include "BillboardFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "utils.h"
#include "logging.h"
#include "SaveImageFilter.h"
#include <algorithm>
#include <android/log.h>
#include <vector>
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,"BillboardFilter",__VA_ARGS__)

using namespace cv;

BillboardFilter::BillboardFilter() {
	blend_with_image_memory = false;
	alpha = 0;
}

int square(int aAx, int aAy, int aBx, int aBy, int aCx, int aCy) {
    return abs(aBx*aCy - aCx*aBy - aAx*aCy + aCx*aAy + aAx*aBy - aBx*aAy);
}

bool inside_triangle(int aAx, int aAy, int aBx, int aBy, int aCx, int aCy, int aPx, int aPy){
int s;

  s = square(aPx, aPy, aAx, aAy, aBx, aBy) + square(aPx, aPy, aBx, aBy, aCx, aCy) +
    square(aPx, aPy, aCx, aCy, aAx, aAy);
  return abs(square(aAx, aAy, aBx, aBy, aCx, aCy) - s) <= 0.01;



/*
  int Bx;
  int By;
  int Cx;
  int Cy;
  int Px;
  int Py;

  int m;
  int l;

  bool result = false;
  // переносим треугольник точкой А в (0;0).
  Bx = aBx - aAx;
  By = aBy - aAy;
  Cx = aCx - aAx;
  Cy = aCy - aAy;
  Px = aPx  - aAx;
  Py = aPy  - aAy;
  //
  m = (Px*By - Bx*Py) / (Cx*By - Bx*Cy);
  if (m >= 0 && m <= 1) {
    l = (Px - m*Cx) / Bx;
    if (l >= 0 && (m + l) <= 1)
      result = true;
  }
	return result;*/
}

void BillboardFilter::setParam(const std::string& name, const std::string& value) {
	if (name == "algorithm") {
		if (value == "colorDodge") {
			algorithm_ = ALGORITHM_COLOR_DODGE;
		} else if (value == "screen") {
			algorithm_ = ALGORITHM_SCREEN;
		} else if (value == "multiply") {
			algorithm_ = ALGORITHM_MULTIPLY;
		} else if (value == "transparency") {
			algorithm_ = ALGORITHM_TRANSPARENCY;
		} else if (value == "transparency_alpha") {
			algorithm_ = ALGORITHM_TRANSPARENCY_ALPHA;
		} else if (value == "overlay") {
			algorithm_ = ALGORITHM_OVERLAY;
		} else if (value == "hue") {
			algorithm_ = ALGORITHM_HUE;
		}
	} else if (name == "image") {
		imagePath_ = value;
	} else if (name == "blend_with_image_memory") {
		if (value == "true") {
			blend_with_image_memory = true;
		}
	} else if (name == "position") {
		// nobody cares for now
	} else if (name == "alpha") {
    	alpha = atoi(value.c_str());
	} else if (name == "x0") {
    	x0 = atoi(value.c_str());
	} else if (name == "x1") {
    	x1 = atoi(value.c_str());
	} else if (name == "x2") {
    	x2 = atoi(value.c_str());
	} else if (name == "x3") {
    	x3 = atoi(value.c_str());
	} else if (name == "y0") {
    	y0 = atoi(value.c_str());
	} else if (name == "y1") {
    	y1 = atoi(value.c_str());
	} else if (name == "y2") {
    	y2 = atoi(value.c_str());
	} else if (name == "y3") {
    	y3 = atoi(value.c_str());
    }
}

bool BillboardFilter::inBounds(int i, int j) {
	int _x0 = std::min(std::min(x0,x1),std::min(x2,x3));
    int _x1 = std::max(std::max(x0,x1),std::max(x2,x3));
    int _y0 = std::min(std::min(y0,y1),std::min(y2,y3));
    int _y1 = std::max(std::max(y0,y1),std::max(y2,y3));
    if(i>_y0 && i<_y1 && j>_x0 && j<_x1) {
		return true;
	}
	return false;
}

void BillboardFilter::process(cv::Mat& image) {
	LOGD("1. rows: %d; cols: %d", image.rows, image.cols);
	cv::Mat _image(image);
	bool square = image.cols == image.rows;
	cv::Mat blendImage;
	if (blend_with_image_memory) {
		SaveImageFilter::memory.copyTo(blendImage);
	} else {
		blendImage = cv::imread(getFullAssetPath(imagePath_, image, square),
				CV_LOAD_IMAGE_UNCHANGED);
	}
	if (blendImage.empty()) {
		blendImage = cv::imread(getFullAssetPath(imagePath_, image, !square),
				CV_LOAD_IMAGE_UNCHANGED);
	}
	if (blendImage.empty()) {
			blendImage = cv::imread(imagePath_, CV_LOAD_IMAGE_UNCHANGED);
			if (blendImage.empty()) {
				return;
			}
		}
	int cols = blendImage.cols;
	int rows = blendImage.rows;
    LOGD("2. rows: %d; cols: %d", blendImage.rows, blendImage.cols);
	resize(image, image, cv::Size(blendImage.cols, blendImage.rows), 0, 0,
    				cv::INTER_LINEAR);

    LOGD("3. rows: %d; cols: %d", blendImage.rows, blendImage.cols);
    LOGD("4. rows: %d; cols: %d", image.rows, image.cols);
    LOGD("5. rows: %d; cols: %d", _image.rows, _image.cols);
	//image.resize(700);



	blendImage.copyTo(image);
	Mat copyBlend;
	blendImage.copyTo(copyBlend);


	int _x0 = std::min(std::min(x0,x1),std::min(x2,x3));
    int _y0 = std::min(std::min(y0,y1),std::min(y2,y3));

    int _x1 = std::max(std::max(x0,x1),std::max(x2,x3));
    int _y1 = std::max(std::max(y0,y1),std::max(y2,y3));

    int w = _x1-_x0;
    int h = _y1-_y0;

	cv::Rect rect;

	cv::Size size(w,h);
    LOGD("6. w: %d; h: %d", w, h);

/*	if(w/h < _image.cols/_image.rows) {

   	 LOGD("7");
		rect = cv::Rect(cv::Point(0.5*(_image.cols-_image.cols * w/h),0), cv::Size(w,h*w/_image.cols));
	} else {

   	 LOGD("8");
		rect = cv::Rect(cv::Point(0,0.5*(_image.rows - _image.rows * h/w)), cv::Size(w,h*w/_image.cols));
	}*/

	if(1.0*_image.cols/_image.rows > 1.0*w/h) {
    	//LOGD("7. %f %f %f %f", 0.5*(_image.cols-1.0*w*_image.rows/float(h)),0,1.0*w*_image.rows/float(h),_image.rows);
		rect = cv::Rect(cv::Point(0.5*(_image.cols-1.0*w*_image.rows/float(h)),0), cv::Size(1.0*w*_image.rows/float(h),_image.rows));
		//rect = cv::Rect(cv::Point(0,0), cv::Size(25,25));
	} else {
    	//LOGD("8. %f %f %f %f", float(0),0.5*(_image.rows-(float)h*(float)_image.cols/(float)w),(float)1/2,(float)(1/2));
		rect = cv::Rect(cv::Point((float)0,0.5*((float)_image.rows-(float)h*(float)_image.cols/(float)w)), cv::Size((float)_image.cols,(float)h*(float)_image.cols/(float)w));
		//rect = cv::Rect(cv::Point(0,0), cv::Size(25,25));
	}

    cv::Mat _imageResized;
    cv::resize(_image(rect), _imageResized, size);

	cv::Mat dst_roi = image(cv::Rect(cv::Point(_x0, _y0), _imageResized.size()));
	_imageResized.copyTo(dst_roi);
	_imageResized.release();
	_image.release();
	blendImage.release();



	for(int i=0; i<image.rows; i++) {
		for(int j=0; j<image.cols; j++) {
			if(!inside_triangle(x0,y0, x1, y1, x2, y2 ,j,i) &&
						!inside_triangle(x2,y2,x3,y3,x0,y0 ,j,i))
			{
				image.at<cv::Vec3b>(i,j)=copyBlend.at<cv::Vec3b>(i,j);
			}
		}
	}


//	cv::Mat dst_roi = image(cv::Rect(_x0, _y0, _x1-_x0, _y1-_y0));
//    _image.copyTo(dst_roi);

	/*if (cols != image.cols || rows != image.rows) {
		if (image.cols < image.rows && cols > rows) { //potrait photo - texture should be rotated
			int len = cv::min(rows, cols);
			cv::Point2f center(len / 2., len / 2.);
			warpAffine(blendImage, blendImage,
					cv::getRotationMatrix2D(center, -90, 1.0),
					cv::Size(rows, cols));
		}
		resize(image, image, cv::Size(blendImage.rows, blendImage.cols), 0, 0,
				cv::INTER_LINEAR);
	}*/

	blendImage.release();
}

/*
void blendFilterOpenCVAlpha(const cv::Mat & sourceImage,
		const cv::Mat & blendImage, cv::Mat & resultImage, int alpha) {
	int cols = std::min(sourceImage.cols, blendImage.cols);
	int rows = std::min(sourceImage.rows, blendImage.rows);
	int blendNumChannels = blendImage.channels();
	int srcNumChannels = sourceImage.channels();

	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			cv::Vec3b sourcePixel = sourceImage.at < cv::Vec3b > (j, i);
			cv::Vec3b blendPixel;
			uchar blendA = 255;
			if (blendNumChannels == 4) {
				cv::Vec4b blendPixel4 = blendImage.at < cv::Vec4b > (j, i);
				blendA = blendPixel4[3];
				blendPixel = cv::Vec3b(blendPixel4[0], blendPixel4[1],
						blendPixel4[2]);
			} else if (blendNumChannels == 1) {
				uchar gray = blendImage.at < uchar > (j, i);
				blendPixel = cv::Vec3b(gray, gray, gray);
			} else {
				blendPixel = blendImage.at < cv::Vec3b > (j, i);
			}
			cv::Vec3b resultPixel;
			for (int k = 0; k < srcNumChannels; k++) {
				resultPixel[k] = BillboardFilter::blendTransparencyComp(alpha,
						sourcePixel[k], blendPixel[k]);
			}
			resultImage.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}
}

void BillboardFilter::blendFilterOpenCV(const cv::Mat & sourceImage,
		const cv::Mat & blendImage, cv::Mat & resultImage, int algorithm) {
	int cols = std::min(sourceImage.cols, blendImage.cols);
	int rows = std::min(sourceImage.rows, blendImage.rows);
	int blendNumChannels = blendImage.channels();
	int srcNumChannels = sourceImage.channels();

	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			if (algorithm == ALGORITHM_HUE) {
            	cv::Vec3b sourcePixel = sourceImage.at < cv::Vec3b > (j, i);
                cv::Vec3b blendPixel = blendImage.at < cv::Vec3b > (j, i);
                cv::Vec3b resultPixel = sourceImage.at < cv::Vec3b > (j, i);
                resultPixel[0] = blendPixel[0];
				resultImage.at < cv::Vec3b > (j, i) = resultPixel;
            } else {
				cv::Vec3b sourcePixel = sourceImage.at < cv::Vec3b > (j, i);
				cv::Vec3b blendPixel;
				uchar blendA = 255;
				if (blendNumChannels == 4) {
					cv::Vec4b blendPixel4 = blendImage.at < cv::Vec4b > (j, i);
					blendA = blendPixel4[3];
					blendPixel = cv::Vec3b(blendPixel4[0], blendPixel4[1],
							blendPixel4[2]);
				} else if (blendNumChannels == 1) {
					uchar gray = blendImage.at < uchar > (j, i);
					blendPixel = cv::Vec3b(gray, gray, gray);
				} else {
					blendPixel = blendImage.at < cv::Vec3b > (j, i);
				}
				cv::Vec3b resultPixel;
				for (int k = 0; k < srcNumChannels; k++) {
					if (algorithm == ALGORITHM_SCREEN) {
						resultPixel[k] = BillboardFilter::blendScreenComp(sourcePixel[k], blendPixel[k]);
						if (blendNumChannels == 4) {
							resultPixel[k] = BillboardFilter::blendTransparencyComp(blendA,	sourcePixel[k], resultPixel[k]);
						}
					} else if (algorithm == ALGORITHM_MULTIPLY) {
						resultPixel[k] = BillboardFilter::blendMultiplyComp(sourcePixel[k], blendPixel[k]);
						if (blendNumChannels == 4) {
							resultPixel[k] = BillboardFilter::blendTransparencyComp(blendA,	sourcePixel[k], resultPixel[k]);
						}
					} else if (algorithm == ALGORITHM_OVERLAY) {
						resultPixel[k] = BillboardFilter::blendOverlay(sourcePixel[k], blendPixel[k]);
						if (blendNumChannels == 4) {
							resultPixel[k] = BillboardFilter::blendTransparencyComp(blendA,	sourcePixel[k], resultPixel[k]);
						}
					} else if (algorithm == ALGORITHM_TRANSPARENCY) {
						resultPixel[k] = BillboardFilter::blendTransparencyComp(blendA, sourcePixel[k], blendPixel[k]);
					} else if (algorithm == ALGORITHM_COLOR_DODGE) {
						resultPixel[k] = blendColorDodgeComp(sourcePixel[k], blendPixel[k]);
						if (blendNumChannels == 4) {
							resultPixel[k] = BillboardFilter::blendTransparencyComp(blendA,	sourcePixel[k], resultPixel[k]);
						}
					} else if (algorithm == ALGORITHM_GETFIRSRT) {
						resultPixel[k] = sourcePixel[k];
					}
				}
				resultImage.at < cv::Vec3b > (j, i) = resultPixel;
			}
		}
	}
}

void BillboardFilter::blendFilterTransparencyOpenCV(const cv::Mat & sourceImage,
		const cv::Mat & blendImage, cv::Mat & resultImage, int alpha) {
	int cols = std::min(sourceImage.cols, blendImage.cols);
	int rows = std::min(sourceImage.rows, blendImage.rows);
	int srcNumChannels = sourceImage.channels();
	int blendNumChannels = blendImage.channels();

	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			cv::Vec3b sourcePixel = sourceImage.at < cv::Vec3b > (j, i);
			cv::Vec3b blendPixel;
			if (blendNumChannels == 4) {
				cv::Vec4b blendPixel4 = blendImage.at < cv::Vec4b > (j, i);
				blendPixel = cv::Vec3b(blendPixel4[0], blendPixel4[1],
						blendPixel4[2]);
			} else if (blendNumChannels == 1) {
				uchar gray = blendImage.at < uchar > (j, i);
				blendPixel = cv::Vec3b(gray, gray, gray);
			} else {
				blendPixel = blendImage.at < cv::Vec3b > (j, i);
			}
			cv::Vec3b resultPixel;
			for (int k = 0; k < srcNumChannels; k++) {
				resultPixel[k] = BillboardFilter::blendTransparencyComp(alpha,
						sourcePixel[k], blendPixel[k]);
			}
			resultImage.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}
}

void BillboardFilter::blendSticker(const cv::Mat & source, const cv::Mat & sticker,
		cv::Mat & result, double centerX, double centerY, int alpha,
		int color) {
	int x = centerX * source.cols;
	int y = centerY * source.rows;
	int offsetX = x - sticker.cols / 2;
	int offsetY = y - sticker.rows / 2;
	int x1 = std::max(offsetX, 0);
	int y1 = std::max(offsetY, 0);
//	offsetX = offesetX < 0 ? abs(offsetX) : 0;
//	offsetY = offesetY < 0 ? abs(offsetY) : 0;
	int x2 = std::min(x + sticker.cols / 2, source.cols);
	int y2 = std::min(y + sticker.rows / 2, source.rows);
	int stickerChannels = sticker.channels();
	int srcNumChannels = source.channels();
	for (int i = x1; i < x2; i++) {
		for (int j = y1; j < y2; j++) {
			cv::Vec3b sourcePixel = source.at < cv::Vec3b > (j, i);

			cv::Vec3b blendPixel;

			double blendA = 0.0f;
			if (stickerChannels == 4) {
				cv::Vec4b blendPixel4 = sticker.at < cv::Vec4b
						> (j - offsetY, i - offsetX);
				if (alpha == 255) {
				blendA = blendPixel4[3] * (alpha / 255.0f);

				if (color != 0 && blendPixel4[3] != 0) {

					blendPixel = cv::Vec3b((color & 0x000000FF),
							(color & 0x0000FF00) >> 8,
							(color & 0x00FF0000) >> 16);
				} else {
					blendPixel = cv::Vec3b(blendPixel4[0], blendPixel4[1],
							blendPixel4[2]);
				}
				}
			}

			if (stickerChannels == 3) {
				cv::Vec3b blendPixel3 = sticker.at < cv::Vec3b
									> (j - offsetY, i - offsetX);
				blendPixel = cv::Vec3b(blendPixel3[0], blendPixel3[1],
										blendPixel3[2]);
			}

			cv::Vec3b resultPixel;
			for (int k = 0; k < srcNumChannels; k++) {
				if (stickerChannels == 4) {
				resultPixel[k] = BillboardFilter::blendTransparencyComp(blendA,
						sourcePixel[k], blendPixel[k]);
				}
				if (stickerChannels == 3) {
					resultPixel[k] = BillboardFilter::blendMultiplyComp(sourcePixel[k], blendPixel[k]);
				}
			}
			result.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}
}

uchar BillboardFilter::blendScreenComp(uchar sourceComp, uchar blendComp) {
	return sourceComp + blendComp - (sourceComp * blendComp) / 255;
}

uchar BillboardFilter::blendMultiplyComp(uchar sourceComp, uchar blendComp) {
	return (sourceComp * blendComp) / 255;
}

uchar BillboardFilter::blendTransparencyComp(uchar blendA, uchar sourceComp,
		uchar blendComp) {
	if (blendA == 0) {
		return sourceComp;
	}
	if (blendA == 255) {
		return blendComp;
	}
	double k = blendA / 255.0f;
	return sourceComp - (sourceComp - blendComp) * k;
}

uchar BillboardFilter::blendOverlay(uchar B, uchar A) {
	return ((uchar)(
			(B < 128) ?
					(2 * A * B / 255) : (255 - 2 * (255 - A) * (255 - B) / 255)));
}*/

