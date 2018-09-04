#include "FishEyeFilter.h"
#include "utils.h"
#include "logging.h"

FishEyeFilter::FishEyeFilter() {
	type_ = TYPE_CIRCLE;
	scale = 1;
	curvature = 0.5;//range: 0..1
}

void FishEyeFilter::setParam(const std::string& name, const std::string& value) {
  if (name == "type") {
    if (value == "circle") {
      type_ = TYPE_CIRCLE;
    } else if (value == "barrel") {
      type_ = TYPE_BARREL;
    } else if (value == "barrel_convex") {
      type_ = TYPE_BARREL_CONVEX;
    } else if (value == "barrel_new") {
        type_ = TYPE_BARREL_NEW;
    }
  }
  if (name == "scale") {
      scale = atoi(value.c_str());
  }
  if (name == "curvature") {//range: 0..100
	  curvature = atoi(value.c_str()) / 100.0;
  }
}

void FishEyeFilter::process(cv::Mat& image) {
	cv::Mat scaled;
	if (scale != 1) {
		resize(image, scaled, cv::Size(image.cols * scale, image.rows * scale), 0, 0, cv::INTER_LINEAR);

		if (type_ == TYPE_CIRCLE) {
			circleFilter(scaled, curvature);
		} else if (type_ == TYPE_BARREL) {
			barrelFilter(scaled);
		} else if (type_ == TYPE_BARREL_CONVEX) {
			barrelFilterConvex(scaled);
		} else if (type_ == TYPE_BARREL_NEW) {
			barrelFilterNew(scaled);
		}
		resize(scaled, image, cv::Size(image.cols, image.rows), 0, 0, cv::INTER_LINEAR);
	} else {
		if (type_ == TYPE_CIRCLE) {
			circleFilter(image, curvature);
		} else if (type_ == TYPE_BARREL) {
			barrelFilter(image);
		} else if (type_ == TYPE_BARREL_CONVEX) {
			barrelFilterConvex(image);
		} else if (type_ == TYPE_BARREL_NEW) {
			barrelFilterNew(image);
		}
	}
}

void FishEyeFilter::circleFilter(cv::Mat & mat) {
	circleFilter(mat, 0.5);
}

void FishEyeFilter::circleFilter(cv::Mat & mat, double k) {
	int bound = 0;
	int h = mat.rows;
	int w = mat.cols;
	cv::Mat tmp = mat.clone();
	cv::Vec3b black(0, 0, 0);
	std::fill(mat.begin<cv::Vec3b>(), mat.end<cv::Vec3b>(), black);

	// calculate fisheyte for 1/8
	int widthRow = w / 2;
	for (int y = 0; y <= h / 2; y++) {
		// normalize y
		double ny = 2 * (double) y / (double) h - 1;
		double ny2 = ny * ny;
		// length row
		for (int x = w / 2 - widthRow; x <= w / 2; x++) {
			double nx = 2 * (double) x / (double) w - 1;
			double nx2 = nx * nx;
			// distance from centre
			double r = sqrt(nx2 + ny2);
			if (0.0 <= r && r <= 1.0) {
			double nr = sqrt(1.0 - r * r);
			// new distance is between 0 ... 1
			//nr = (k1 * r + k2 * (1.0 - nr)) / (k1 + k2);
			nr = ((1 - k) * r + k * (1.0 - nr));
			// discard radius greater than 1.0
			// calculate the angle for polar coordinates
			double theta = atan2(ny, nx);
			// calculate new x position with new distance in same
			// angle
			double nxn = nr * cos(theta);
			// calculate new y position with new distance in same
			// angle
			double nyn = nr * sin(theta);
			// map from -1 ... 1 to image coordinates
			int x2 = (int) (((nxn + 1) * w) / 2.0);
			// map from -1 ... 1 to image coordinates
			int y2 = (int) (((nyn + 1) * h) / 2.0);
			//LOGI("x = %d y = %d x2 = %d y2 = %d", x, y, x2, y2);

			if (x2 == 0 && y2 == 0 && x > 0 && y > 0) {
				//	Log.d(LOG_TAG, "x=" + x + "y=" + y);
				//	Log.d(LOG_TAG, "r=" + r + "theta = " + theta);
				bound = x;
			}
			if (x2 < w && y2 < h && x2 > 0 && y2 > 0) {
				mat.at < cv::Vec3b > (y, x) = tmp.at < cv::Vec3b > (y2, x2);

				mat.at < cv::Vec3b > (y, w - x) = tmp.at < cv::Vec3b
						> (y2, w - x2);

				mat.at < cv::Vec3b > (x, w - y) = tmp.at < cv::Vec3b
						> (x2, w - y2);

				//LOGI("x2: %d y2: %d x: %d y: %d ", x2, y2, x, y);
				mat.at < cv::Vec3b > (h - x, w - y) = tmp.at < cv::Vec3b
						> (h - x2, w - y2);

				mat.at < cv::Vec3b > (h - y, w - x) = tmp.at < cv::Vec3b
						> (h - y2, w - x2);

				mat.at < cv::Vec3b > (h - y, x) = tmp.at < cv::Vec3b
						> (h - y2, x2);
				mat.at < cv::Vec3b > (w - x, y) = tmp.at < cv::Vec3b
						> (w - x2, y2);

				mat.at < cv::Vec3b > (x, y) = tmp.at < cv::Vec3b > (x2, y2);

			}
			}
		}
		widthRow--;

	}
	cv::Rect cropRect(bound, bound, w - 2 * bound, h - 2 * bound);
	cv::Mat cropped;
	cv::Mat(mat, cropRect).copyTo(cropped);

	tmp.release();
	mat = cropped.clone();
	cropped.release();
}

void FishEyeFilter::barrelFilter(cv::Mat & mat) {
	int bound = 0;
	int h = mat.rows;
	int w = mat.cols;
	cv::Mat tmp = mat.clone();
	cv::Vec3b black(0, 0, 0);
	std::fill(mat.begin<cv::Vec3b>(), mat.end<cv::Vec3b>(), black);

	// calculate fisheyte for 1/8
	int widthRow = w / 2;
	for (int y = 0; y <= h / 2; y++) {
		// normalize y
		double ny = 2 * (double) y / (double) h - 1;
		double ny2 = ny * ny;
		// length row
		for (int x = w / 2 - widthRow; x <= w / 2; x++) {
			double nx = 2 * (double) x / (double) w - 1;
			double nx2 = nx * nx;
			// distance from centre
			double r = sqrt(nx2 + ny2);
			double nr = sqrt(2 - (nx2 + ny2));
			// new distance is between 0 ... 1
			nr = (r + (sqrt(2) - nr)) / sqrt(2);

			// discard radius greater than 1.0
			// calculate the angle for polar coordinates
			double theta = atan2(ny, nx);
			// calculate new x position with new distance in same
			// angle
			double nxn = nr * cos(theta);
			// calculate new y position with new distance in same
			// angle
			double nyn = nr * sin(theta);
			// map from -1 ... 1 to image coordinates
			int x2 = (int) (((nxn + 1) * w) / 2.0);
			// map from -1 ... 1 to image coordinates
			int y2 = (int) (((nyn + 1) * h) / 2.0);
			//	Log.d(LOG_TAG, "x=" + x + "y=" + y + "x2=" + x2 + "y2=" + y2);
			if (x2 == 0 && y2 == 0 && x > 0 && y > 0) {
				//	Log.d(LOG_TAG, "x=" + x + "y=" + y);
				//	Log.d(LOG_TAG, "r=" + r + "theta = " + theta);
				bound = x;
			}

			if (x2 < w && y2 < h && x2 > 0 && y2 > 0) {

				mat.at < cv::Vec3b > (y, x) = tmp.at < cv::Vec3b > (y2, x2);

				mat.at < cv::Vec3b > (y, w - x) = tmp.at < cv::Vec3b
						> (y2, w - x2);

				mat.at < cv::Vec3b > (x, w - y) = tmp.at < cv::Vec3b
						> (x2, w - y2);

				//LOGI("x2: %d y2: %d x: %d y: %d ", x2, y2, x, y);
				mat.at < cv::Vec3b > (h - x, w - y) = tmp.at < cv::Vec3b
						> (h - x2, w - y2);

				mat.at < cv::Vec3b > (h - y, w - x) = tmp.at < cv::Vec3b
						> (h - y2, w - x2);

				mat.at < cv::Vec3b > (h - y, x) = tmp.at < cv::Vec3b
						> (h - y2, x2);
				mat.at < cv::Vec3b > (w - x, y) = tmp.at < cv::Vec3b
						> (w - x2, y2);

				mat.at < cv::Vec3b > (x, y) = tmp.at < cv::Vec3b > (x2, y2);

			}

		}
		widthRow--;

	}

	cv::Rect cropRect(bound, bound, w - 2 * bound, h - 2 * bound);
	cv::Mat cropped;
	cv::Mat(mat, cropRect).copyTo(cropped);

	tmp.release();
	mat = cropped.clone();
	cropped.release();
}

void FishEyeFilter::barrelFilterNew(cv::Mat & mat) {
	cv::Mat tmp = mat.clone();
	float barrel_scale = 0.2f;
	cv::Vec3b black(0, 0, 0);
	std::fill(mat.begin<cv::Vec3b>(), mat.end<cv::Vec3b>(), black);
	int h = mat.rows;
	int w = mat.cols;

	for (int y = 0; y < h; y++) {
		for (int x = 0; x < w; x++) {
			double nx = 2 * (double) x / (double) w - 1;
			double ny = 2 * (double) y / (double) h - 1;
			double nx2 = nx * nx;
			double ny2 = ny * ny;
			double dist = sqrt(nx2 + ny2);
			float percent = 1.0f + ((0.5f - dist) / 0.5f) * barrel_scale;
			int x_use = (x - w / 2) * percent + w / 2;
			int y_use = (y - h / 2) * percent + h / 2;

			if ((x_use > 0) && (x_use < w) && (y_use > 0) && (y_use < h)) {
				mat.at < cv::Vec3b > (y, x) = tmp.at < cv::Vec3b > (y_use, x_use);
			}
		}
	}
}

void FishEyeFilter::barrelFilterConvex(cv::Mat & mat) {
	int bound = 0;
	int h = mat.rows;
	int w = mat.cols;
	cv::Mat tmp = mat.clone();
	// calculate fisheyte for 1/8
	int widthRow = w / 2;
	for (int y = 0; y < h / 2; y++) {
		// normalize y
		double ny = 2 * (double) y / (double) h - 1;
		double ny2 = ny * ny;
		// length row
		for (int x = w / 2 - widthRow; x < w / 2; x++) {
			double nx = 2 * (double) x / (double) w - 1;
			double nx2 = nx * nx;
			// distance from centre
			double r = sqrt(nx2 + ny2);
			double nr = sqrt(2 - (nx2 + ny2));
			// new distance is between 0 ... 1
			nr = (r + (sqrt(2) - nr)) / sqrt(2);

			double nr2 = r - nr;
			nr = r + nr2;

			//LOGI("nr: %f ", nr);
			// discard radius greater than 1.0
			// calculate the angle for polar coordinates
			double theta = atan2(ny, nx);
			// calculate new x position with new distance in same
			// angle
			double nxn = nr * cos(theta);
			// calculate new y position with new distance in same
			// angle
			double nyn = nr * sin(theta);
			// map from -1 ... 1 to image coordinates
			int x2 = (int) (((nxn + 1) * w) / 2.0);
			// map from -1 ... 1 to image coordinates
			int y2 = (int) (((nyn + 1) * h) / 2.0);
			if (nr >= 1.4) {
				bound = x2;
			}
			if (x2 < w / 2 && y2 < h / 2 && x2 > 0 && y2 > 0) {
				//	LOGI("x2: %d y2: %d x: %d y: %d ", x2, y2, x, y);
				mat.at < cv::Vec3b > (y, x) = tmp.at < cv::Vec3b > (y2, x2);

				mat.at < cv::Vec3b > (y, w - x - 1) = tmp.at < cv::Vec3b
						> (y2, w - x2);

				mat.at < cv::Vec3b > (x, w - y - 1) = tmp.at < cv::Vec3b
						> (x2, w - y2);

				mat.at < cv::Vec3b > (h - x - 1, w - y - 1) = tmp.at < cv::Vec3b
						> (h - x2, w - y2);

				mat.at < cv::Vec3b > (h - y - 1, w - x - 1) = tmp.at < cv::Vec3b
						> (h - y2, w - x2);

				mat.at < cv::Vec3b > (h - y - 1, x) = tmp.at < cv::Vec3b
						> (h - y2, x2);
				mat.at < cv::Vec3b > (w - x - 1, y) = tmp.at < cv::Vec3b
						> (w - x2, y2);

				mat.at < cv::Vec3b > (x, y) = tmp.at < cv::Vec3b > (x2, y2);
			}
		}
		widthRow--;
	}
	cv::Rect cropRect(bound, bound, w - 2 * bound, h - 2 * bound);
	cv::Mat cropped;
	cv::Mat(mat, cropRect).copyTo(cropped);

	tmp.release();
	mat = cropped.clone();
	cropped.release();
}

