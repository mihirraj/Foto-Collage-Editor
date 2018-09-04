#include "TiltShiftFilter.h"
#include "utils.h"
#include <string>

void TiltShiftFilter::process(cv::Mat& image) {
	int width = image.cols;
	int height = image.rows;
	cv::Mat source = image.clone();
	int numChannels = image.channels();

	//some consts
	int MAX_MATRIX_SIZE = 20;
	int BLUR_GRAD_FACTOR = 10;
	int MIN_Y = height * 0.4;
	int MAX_Y = 2 * MIN_Y;

	for (int y = 0; y < height; ++y ) {
	    // choosing size of blur matrix.
	    // 3 means 3x3 matrix. all pixels around should be divided by 3^2 = 9.
		int size = 1;
		if (y > MAX_Y) {
			size = 30 * (y - MAX_Y) / MIN_Y;
		} else if (y < MIN_Y) {
			size = 30 * (MIN_Y - y) / MIN_Y;
	    }
	    size = constrain(size, 1, MAX_MATRIX_SIZE);
	    // # quick optimization - just skip if size is 1.
	    if (size <= 1) {
	      continue;
	    }
	    for (int x = 0; x < width; ++x) {
	    	float rtotal = 0.0f;
	    	float gtotal = 0.0f;
	    	float btotal = 0.0f;

	    	int offset = size / 2;
	    	float k = 1.0f / (size * size);

	      // Loop through convolution matrix
	    	for (int i = 0; i < size; ++i) {
	    		for (int j = 0; j < size; ++j) {

	    			int column = constrain(x + i - offset, 0, width - 1);
	    			int row = constrain(y + j - offset, 0, height - 1);

	    			// Calculate the convolution
	    			// We sum all the neighboring pixels multiplied by the values in the convolution matrix.
	    			cv::Vec3b pixel = source.at<cv::Vec3b>(row, column);
	    			rtotal += pixel[0] * k;
	    			gtotal += pixel[1] * k;
	    			btotal += pixel[2] * k;
	    		}
	    	}
	    	// Make sure RGB is within range
	    	image.at<cv::Vec3b>(y, x)[0] = constrain((int)rtotal, 0, 255);
	    	image.at<cv::Vec3b>(y, x)[1] = constrain((int)gtotal, 0, 255);
	    	image.at<cv::Vec3b>(y, x)[2] = constrain((int)btotal, 0, 255);
	    }
	}
	source.release();
}

void TiltShiftFilter::setParam(const std::string& name, const std::string& value) {
}
