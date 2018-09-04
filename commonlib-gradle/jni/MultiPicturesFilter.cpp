#include "MultiPicturesFilter.h"
#include <string>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "logging.h"

#include "utils.h"

void MultiPicturesFilter::process(cv::Mat& image) {
  // do nothing
}


void MultiPicturesFilter::process(std::vector<cv::Mat*>& images) {
  // do nothing for now
//  if (composeType_ == "vertical") {
//    cv::Mat resImage;
//    cv::Rect cropRect(image.cols / 2 - side / 2, image.rows / 2 - side / 2, side,
//                      side);
//  }
}

void MultiPicturesFilter::setParam(const std::string& name, const std::string& value) {
  if (name == "pictures_count") {
    picturesCount_ = atoi(value.c_str());
  } else if (name == "compose_type") {
    if (value == "square") {
    	composeType_ = COMPOSE_SQUARE;
    } else if (value == "vertical") {
    	composeType_ = COMPOSE_VERTICAL;
    } else if (value == "blend") {
    	composeType_ = COMPOSE_BLEND;
    }
  }
}

int MultiPicturesFilter::preProcess(const std::vector<cv::Mat*>& images, cv::Mat & image) {
	if (composeType_ == COMPOSE_SQUARE) {
		for(std::vector<cv::Mat*>::const_iterator it = images.begin(); it != images.end(); ++it) {
			resize(*(*it), *(*it), cv::Size(image.cols / 2, image.rows / 2), 0, 0, cv::INTER_LINEAR);
		}
		return images.size();
	} else if (composeType_ == COMPOSE_VERTICAL) {
		//LOGI("COMPOSE_VERTICAL cols:%d rows:%d", image.cols, image.rows);
		if (images[0]->cols < images[0]->rows) {
			int cols = 0;
			int rows = images[0]->rows;
			for(std::vector<cv::Mat*>::const_iterator it = images.begin(); it != images.end(); ++it) {
				cv::Mat * im = *it;
				cols += im->cols;
				rows = std::max(rows, im->rows);
			}
			resize(image, image, cv::Size(cols, rows), 0, 0, cv::INTER_LINEAR);
			//LOGI("resize cols:%d rows:%d", image.cols, image.rows);
			int x = 0;
			for (int i = 0; i < images.size(); ++i) {
				images[i]->copyTo(image(cv::Rect(x, 0, images[i]->cols, images[i]->rows)));
				//LOGI("copy %d %d %d %d", x, 0, images[i]->cols, images[i]->rows);
				x += images[i]->cols;
			}
			return 1;
		}
		int side = image.cols / images.size();
		int i = 0;
		for(std::vector<cv::Mat*>::const_iterator it = images.begin(); it != images.end(); ++it) {
			cv::Mat * im = *it;
			cv::Rect cropRect((im->cols - side) / 2, 0, side, im->rows);
			cv::Mat cropped;
			cv::Mat(*im, cropRect).copyTo(cropped);
			cropped.copyTo(image(cv::Rect(side * i, 0, cropped.cols, cropped.rows)));
			++i;
		}
		return 1;
	} else if (composeType_ == COMPOSE_BLEND) {
		int channels = image.channels();
	    for (int i = 0; i < image.rows; i++) {
	        for (int j = 0; j < image.cols; j++) {
	            cv::Vec3b pixel0 = images[0]->at<cv::Vec3b>(i, j);
	            cv::Vec3b pixel1 = images[1]->at<cv::Vec3b>(i, j);
	            for (int k = 0; k < channels; ++k) {
	            	image.at<cv::Vec3b>(i, j)[k] = (pixel0[k] * pixel1[k]) / 255;
	            }
	        }
	    }
		return 1;
	}
}

void MultiPicturesFilter::postProcess(const std::vector<cv::Mat*>& images, cv::Mat & image) {
	if (composeType_ == COMPOSE_SQUARE) {
		if (images[0]->channels() == 1) {
			 cv::cvtColor(image, image, CV_BGR2GRAY);
		}

		int cols = std::max(images[0]->cols + images[1]->cols, images[2]->cols + images[3]->cols);
		int maxRows = std::max(images[0]->rows, images[1]->rows);
		int rows = maxRows + std::max(images[2]->rows, images[3]->rows);

		if (image.cols != cols || image.rows != rows) {
			resize(image, image, cv::Size(cols, rows), 0, 0, cv::INTER_LINEAR);
		}
		images[0]->copyTo(image(cv::Rect(0, 0, images[0]->cols, images[0]->rows)));
		images[1]->copyTo(image(cv::Rect(images[0]->cols, 0, images[1]->cols, images[1]->rows)));
		images[2]->copyTo(image(cv::Rect(0, maxRows, images[2]->cols, images[2]->rows)));
		images[3]->copyTo(image(cv::Rect(images[2]->cols, maxRows, images[3]->cols, images[3]->rows)));
		//src.copyTo(dst(Rect(left, top, src.cols, src.rows));
	}
}
