#include "FlipRotateFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include "utils.h"
#include "logging.h"

FlipRotateFilter::FlipRotateFilter() {
	angle = 0;
	flip_horizontal = false;
	flip_vertical = false;
}

void FlipRotateFilter::setParam(const std::string &name,
		const std::string &value) {
	if (name == "angle") {
		angle = atoi(value.c_str());
	}
	if (name == "flip_horizontal") {
		if (value == "true") flip_horizontal = true;
	}
	if (name == "flip_vertical") {
		if (value == "true") flip_vertical = true;
	}
}

void FlipRotateFilter::process(cv::Mat& image) {
	//cv::flip(image, image, flip_mode);

	   angle = ((angle / 90) % 4) * 90;

	   //0 : flip vertical; 1 flip horizontal
	   bool const flip_horizontal_or_vertical = angle > 0 ? 1 : 0;
	   int const number = std::abs(angle / 90);
	   //rotate
	   for(int i = 0; i != number; ++i){
	       cv::transpose(image, image);
	       cv::flip(image, image, flip_horizontal_or_vertical);
	   }
	   //flip
	   if ((flip_horizontal == true) && (flip_vertical == true)) {
		    cv::flip(image, image, -1);
	   } else if (flip_horizontal == true) {
		    cv::flip(image, image, 1);
	   } else if (flip_vertical == true) {
		    cv::flip(image, image, 0);
	   }
}
