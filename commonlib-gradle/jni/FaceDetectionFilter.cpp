#include "FaceDetectionFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include "utils.h"
#include "logging.h"
#include "config.h"
#include "BlendFilter.h"

cv::CascadeClassifier face_cascade;
cv::CascadeClassifier eyes_cascade;
cv::CascadeClassifier mouth_cascade;

cv::String face_cascade_name = "lbpcascade_frontalface.xml";
cv::String eyes_cascade_name = "haarcascade_eye_tree_eyeglasses.xml";
cv::String mouth_cascade_name = "haarcascade_mcs_mouth.xml";

cv::String cache_name = "eyes_position.txt";
cv::String cache_name_preview = "eyes_position_preview.txt";

FaceDetectionFilter::FaceDetectionFilter() {
	whiten_eye_max_coef = 0;
	whiten_teeth_max_coef = 0;
	smooth_skin_alpha = 0;
	unsharp_eye_alpha = 0;
	brightness = 0;
	temperature = 0;
	isPreview = false;
}

void FaceDetectionFilter::setParam(const std::string &name,
		const std::string &value) {
	if (name == "whiten_eye_max_coef") {
		whiten_eye_max_coef = atof(value.c_str());
	}
	if (name == "whiten_teeth_max_coef") {
		whiten_teeth_max_coef = atof(value.c_str());
	}
	if (name == "smooth_skin_alpha") {
		smooth_skin_alpha = atoi(value.c_str());
	}
	if (name == "balanse_face_color_alpha") {
		balanse_face_color_alpha = atoi(value.c_str());
	}
	if (name == "unsharp_eye_alpha") {
		unsharp_eye_alpha = atoi(value.c_str());
	}
	if (name == "brightness") {
		brightness = atoi(value.c_str());
	}
	if (name == "temperature") {
		temperature = atoi(value.c_str());
	}
	if (name == "preview") {
		if (value == "true") {
			isPreview = true;
		}
	}
}

int min(int a, int b, int c) {
	int res = a;
	if (res > b) {
		res = b;
	}
	if (res > c) {
		res = c;
	}
	return res;
}

void FaceDetectionFilter::addWhitenTeeth(cv::Mat& image, cv::Rect& face,
		cv::Rect& mouth) {

	float mouth_scale = 1; //0.8;
	int origin_x = mouth.x + mouth.width / 2;
	int origin_y = mouth.y + mouth.height / 2;
	cv::Rect scaledMouth;
	scaledMouth.width = mouth.width * mouth_scale;
	scaledMouth.height = mouth.height * mouth_scale;
	scaledMouth.x = origin_x - scaledMouth.width / 2;
	scaledMouth.y = origin_y - scaledMouth.height / 2;

	int cols = face.x + scaledMouth.x + scaledMouth.width;
	int rows = face.y + scaledMouth.y + scaledMouth.height
			+ ((face.height * 2) / 3);

//	cv::Mat MagentaChannel;
//	image.copyTo(MagentaChannel);

	cv::Vec3b resultPixel;

	float Black;
	float Cyan;
	float Magenta;
	float Yellow;

//	for (int i = face.x + mouth.x; i < cols; i++) {
//		for (int j = face.y + mouth.y + ((face.height * 2) / 3); j < rows;
//				j++) {
//			cv::Vec3b source = image.at < cv::Vec3b > (j, i);
//			float Blue = source[0] / 255.0;
//			float Green = source[1] / 255.0;
//			float Red = source[2] / 255.0;
//			float Black = min(1 - Red, 1 - Green, 1 - Blue);
//
//			if (Black != 1) {
//				Cyan = (1 - Red - Black) / ((1 - Black) * 1.0);
//				Magenta = (1 - Green - Black) / ((1 - Black) * 1.0);
//				Yellow = (1 - Blue - Black) / ((1 - Black) * 1.0);
//			} else {
//				Cyan = 1 - Red;
//				Magenta = 1 - Green;
//				Yellow = 1 - Blue;
//			}
//
//			resultPixel[0] = Magenta * 255;
//			resultPixel[1] = Magenta * 255;
//			resultPixel[2] = Magenta * 255;
//			MagentaChannel.at < cv::Vec3b > (j, i) = resultPixel;
//		}
//	}

//	int dilation_type = cv::MORPH_RECT;
//	int dilation_size = 1;
//	cv::Mat element = cv::getStructuringElement(dilation_type,
//			cv::Size(2 * dilation_size + 1, 2 * dilation_size + 1),
//			cv::Point(dilation_size, dilation_size));
//	cv::erode(MagentaChannel, MagentaChannel, element);
//	cv::dilate(MagentaChannel, MagentaChannel, element);
//
//	cv::medianBlur(MagentaChannel, MagentaChannel, 3/*cv::Size(3,3)*/);

//	MagentaChannel.convertTo(MagentaChannel, -1, 1.0, 0);

	int a = scaledMouth.width / 2;
	int b = scaledMouth.height / 2;
	int a2 = a * a;
	int b2 = b * b;

//	float center_x = 0;
//	float center_y = 0;
//	float sum_mass = 0;
//	for (int i = face.x; i < face.x + face.width; i++) {
//			for (int j = face.y + ((face.height * 2) / 3); j < face.y + face.height;
//					j++) {
//				cv::Vec3b source = image.at < cv::Vec3b > (j, i);
//				float Blue = source[0] / 255.0;
//				float Green = source[1] / 255.0;
//				float Red = source[2] / 255.0;
//				float Black = min(1 - Red, 1 - Green, 1 - Blue);
//
//				if (Black != 1) {
//					Cyan = (1 - Red - Black) / ((1 - Black) * 1.0);
//					Magenta = (1 - Green - Black) / ((1 - Black) * 1.0);
//					Yellow = (1 - Blue - Black) / ((1 - Black) * 1.0);
//				} else {
//					Cyan = 1 - Red;
//					Magenta = 1 - Green;
//					Yellow = 1 - Blue;
//				}
//				center_x += Magenta * i;
//				center_y += Magenta * j;
//				sum_mass += Magenta;
//			}
//	}
//	center_x = center_x / sum_mass;
//	center_y = center_y / sum_mass;
//	cv::circle(image, cv::Point(center_x, center_y), 20, 8);

	float R = (scaledMouth.width / 2) * (scaledMouth.width / 2)
			+ (scaledMouth.height / 2) * (scaledMouth.height / 2);
	for (int i = face.x + scaledMouth.x; i < cols; i++) {
		for (int j = face.y + scaledMouth.y + ((face.height * 2) / 3); j < rows;
				j++) {
			cv::Vec3b source = image.at < cv::Vec3b > (j, i);
			float Blue = source[0] / 255.0;
			float Green = source[1] / 255.0;
			float Red = source[2] / 255.0;
			float Black = min(1 - Red, 1 - Green, 1 - Blue);

			if (Black != 1) {
				Cyan = (1 - Red - Black) / ((1 - Black) * 1.0);
				Magenta = (1 - Green - Black) / ((1 - Black) * 1.0);
				Yellow = (1 - Blue - Black) / ((1 - Black) * 1.0);
			} else {
				Cyan = 1 - Red;
				Magenta = 1 - Green;
				Yellow = 1 - Blue;
			}

			int x = i - face.x - scaledMouth.x - scaledMouth.width / 2;
			int y = j - face.y - scaledMouth.y - scaledMouth.height / 2
					- ((face.height * 2) / 3);
			float radius = x * x + y * y;

			float coord = (x * x) / (a2 * 1.0) + (y * y) / (b2 * 1.0);

			if (coord > 1) {
				continue;
			}

			int d = x * x + y * y;
			float innerRadius = sqrt(d);

			float cost = 0;
			float sint = 1;
			if (d != 0) {
				cost = x / innerRadius;
				sint = y / innerRadius;
			}
			float newx = a * cost;
			float newy = b * sint;
			float outerRadius = sqrt(newx * newx + newy * newy);

//			if (radius > R) {
//				continue;
//			}
			//float k = radius / R;
			float k = (innerRadius / outerRadius) * (innerRadius / outerRadius)
					* (innerRadius / outerRadius) * (innerRadius / outerRadius);
			if (k < 0) {
				k = -k;
			}
			if (k > 1) {
				k = 1;
			}

			//LOGI("x = %d, y = %d, radius = %f, R = %f, k = %f", x, y, radius, R, k);
			//cv::Vec3b Magenta = MagentaChannel.at < cv::Vec3b > (j, i);

			int alpha = Magenta * 255;
			if (alpha == 0) {
				alpha = 1;
			}
			for (int ch = 0; ch < 3; ch++) {
				int result = source[ch] + ((1 - k) * whiten_teeth_max_coef)
						- alpha * (1.3);
				//LOGI("source[%d] = %d, 1 - k = %f, brightness = %d, alpha = %d, result = %d", ch, source[ch], 1 - k, brightness, alpha, result);
				if (result > 255) {
					result = 255;
				}
				if (result < source[ch]) {
					result = source[ch];
				}
				resultPixel[ch] = result;
			}
			image.at < cv::Vec3b > (j, i) = resultPixel;

//			if (alpha > brightness) {
//				resultPixel[0] = 255;
//				resultPixel[1] = 0;
//				resultPixel[2] = 0;
//
//				for (int ch = 0; ch < 3; ch++) {
//					resultPixel[ch] = BlendFilter::blendTransparencyComp(
//							alpha, source[ch], resultPixel[ch]);
//				}
//				image.at < cv::Vec3b > (j, i) = resultPixel;
//			}

		}
	}
	//image = MagentaChannel;
}

void FaceDetectionFilter::addTemperatureFace(cv::Mat& image, cv::Rect& face) {
	int cols = image.cols;
	int rows = image.rows;

	cv::Mat orig;
	cv::Rect scaledFace;

	float face_scale = 1.4;
	int origin_x = face.x + face.width / 2;
	int origin_y = face.y + face.height / 2;
	scaledFace.width = face.width * face_scale;
	scaledFace.height = face.height * face_scale;
	scaledFace.x = origin_x - scaledFace.width / 2;
	scaledFace.y = origin_y - scaledFace.height / 2;

	int face_a = scaledFace.width / 2;
	int face_b = scaledFace.height / 2;
	int face_a2 = face_a * face_a;
	int face_b2 = face_b * face_b;

	cv::Vec3b resultPixel;

	if (temperature == 0) {
		return;
	}

	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			if ((i < scaledFace.x) || (i > scaledFace.x + scaledFace.width)
					|| (j < scaledFace.y)
					|| (j > scaledFace.y + scaledFace.height)) {
				continue;
			}

			int face_x = i - face_a - scaledFace.x;
			int face_y = j - face_b - scaledFace.y;
			float face_coord = (face_x * face_x) / (face_a2 * 1.0)
					+ (face_y * face_y) / (face_b2 * 1.0);

			int face_d = face_x * face_x + face_y * face_y;
			float face_innerRadius = sqrt(face_d);

			float cost = 0;
			float sint = 1;
			if (face_d != 0) {
				cost = face_x / face_innerRadius;
				sint = face_y / face_innerRadius;
			}
			float face_newx = face_a * cost;
			float face_newy = face_b * sint;
			float face_outerRadius = sqrt(
					face_newx * face_newx + face_newy * face_newy);

			cv::Vec3b source = image.at < cv::Vec3b > (j, i);
			resultPixel = source;
			if (face_coord < 1) {
				int k = (int) (((face_outerRadius - face_innerRadius)
						/ face_outerRadius) * 255);
				int temp;
				temp = source[0] - (2.55 * temperature);
				if (temp < 0) {
					resultPixel[0] = 0;
				} else if (temp > 255) {
					resultPixel[0] = 255;
				} else {
					resultPixel[0] = temp;
				}
				resultPixel[1] = source[1];
				temp = source[2] + (2.55 * temperature);
				if (temp < 0) {
					resultPixel[2] = 0;
				} else if (temp > 255) {
					resultPixel[2] = 255;
				} else {
					resultPixel[2] = temp;
				}
				for (int ch = 0; ch < 3; ch++) {
					resultPixel[ch] = BlendFilter::blendTransparencyComp(k,
							source[ch], resultPixel[ch]);
				}
			}
			image.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}
}

void FaceDetectionFilter::addBrightnessFace(cv::Mat& image, cv::Rect& face) {
	int cols = image.cols;
	int rows = image.rows;

	cv::Mat orig;
	cv::Rect scaledFace;

	float face_scale = 1.0;
	int origin_x = face.x + face.width / 2;
	int origin_y = face.y + face.height / 2;
	scaledFace.width = face.width * face_scale;
	scaledFace.height = face.height * face_scale * 1.25;
	scaledFace.x = origin_x - scaledFace.width / 2;
	scaledFace.y = origin_y - scaledFace.height / 2;

	int face_a = scaledFace.width / 2;
	int face_b = scaledFace.height / 2;
	int face_a2 = face_a * face_a;
	int face_b2 = face_b * face_b;

	cv::Vec3b resultPixel;

	if (brightness == 0) {
		return;
	}

	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			if ((i < scaledFace.x) || (i > scaledFace.x + scaledFace.width)
					|| (j < scaledFace.y)
					|| (j > scaledFace.y + scaledFace.height)) {
				continue;
			}

			int face_x = i - face_a - scaledFace.x;
			int face_y = j - face_b - scaledFace.y;
			float face_coord = (face_x * face_x) / (face_a2 * 1.0)
					+ (face_y * face_y) / (face_b2 * 1.0);

			int face_d = face_x * face_x + face_y * face_y;
			float face_innerRadius = sqrt(face_d);

			float cost = 0;
			float sint = 1;
			if (face_d != 0) {
				cost = face_x / face_innerRadius;
				sint = face_y / face_innerRadius;
			}
			float face_newx = face_a * cost;
			float face_newy = face_b * sint;
			float face_outerRadius = sqrt(
					face_newx * face_newx + face_newy * face_newy);

			cv::Vec3b source = image.at < cv::Vec3b > (j, i);
			resultPixel = source;
			if (face_coord < 1) {
				int k = (int) (((face_outerRadius - face_innerRadius)
						/ face_outerRadius) * 255);

//				k *= 255;
//				k = sqrt(k);
//				k = 2 * k;
				if (k > 255) {
					k = 255;
				}
				for (int ch = 0; ch < 3; ch++) {
					int result = source[ch] + source[ch] * (brightness / 100.0);
					if (result > 255) {
						result = 255;
					}
					resultPixel[ch] = result;
					resultPixel[ch] = BlendFilter::blendTransparencyComp(k,
							source[ch], resultPixel[ch]);
				}
			}

			image.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}
}

void FaceDetectionFilter::whitenFace(cv::Mat& image, cv::Rect& face,
		cv::Rect& left_eye, cv::Rect& right_eye, cv::Rect& mouth) {
//thresholdBlur
	int cols = image.cols;
	int rows = image.rows;
	int kernel_size = 9;
	int kh = kernel_size;
	int kw = kernel_size;
	int threshold = 40;
	int kh2 = kh / 2;
	int kw2 = kw / 2;

	cv::Mat orig;

	LOGI(
			"left_eye = (%d, %d, %d, %d)", left_eye.x, left_eye.y, left_eye.width, left_eye.height);
	LOGI(
			"right_eye = (%d, %d, %d, %d)", right_eye.x, right_eye.y, right_eye.width, right_eye.height);

//thresholdblur
	if (smooth_skin_alpha == 0) {
		return;
	}

	int face_a = face.width / 2;
	int face_b = face.height / 2;
	int face_a2 = face_a * face_a;
	int face_b2 = face_b * face_b;

	int left_eye_a = left_eye.width / 2;
	int left_eye_b = left_eye.height / 2;
	int left_eye_a2 = left_eye_a * left_eye_a;
	int left_eye_b2 = left_eye_b * left_eye_b;

	int right_eye_a = right_eye.width / 2;
	int right_eye_b = right_eye.height / 2;
	int right_eye_a2 = right_eye_a * right_eye_a;
	int right_eye_b2 = right_eye_b * right_eye_b;
	cv::Vec3b resultPixel;

	for (int i = kh2; i < cols - kh2; i++) {
		for (int j = kw2; j < rows - kw2; j++) {
			if ((i < face.x) || (i > face.x + face.width) || (j < face.y)
					|| (j > face.y + face.height)) {
				continue;
			}

			int left_eye_x = i - left_eye_a - left_eye.x;
			int left_eye_y = j - left_eye_b - left_eye.y;

			float left_eye_coord = (left_eye_x * left_eye_x)
					/ (left_eye_a2 * 1.0)
					+ (left_eye_y * left_eye_y) / (left_eye_b2 * 1.0);

			int right_eye_x = i - right_eye_a - right_eye.x;
			int right_eye_y = j - right_eye_b - right_eye.y;

			float right_eye_coord = (right_eye_x * right_eye_x)
					/ (right_eye_a2 * 1.0)
					+ (right_eye_y * right_eye_y) / (right_eye_b2 * 1.0);

			if (left_eye_coord < 1) {
				//LOGI("left_eye");
				continue;
			}

			if (right_eye_coord < 1) {
				//LOGI("right_eye");
				continue;
			}

			if ((mouth.width != 0)
					&& ((i > face.x + mouth.x)
							&& (i < face.x + mouth.x + mouth.width)
							&& (j > face.y + ((face.height * 2) / 3) + mouth.y)
							&& (j
									< face.y + mouth.y + ((face.height * 2) / 3)
											+ mouth.height))) {
				//LOGI("mouth");
				continue;
			}

			int face_x = i - face_a - face.x;
			int face_y = j - face_b - face.y;
			float face_coord = (face_x * face_x) / (face_a2 * 1.0)
					+ (face_y * face_y) / (face_b2 * 1.0);

			int face_d = face_x * face_x + face_y * face_y;
			float face_innerRadius = sqrt(face_d);

			float cost = 0;
			float sint = 1;
			if (face_d != 0) {
				cost = face_x / face_innerRadius;
				sint = face_y / face_innerRadius;
			}
			float face_newx = face_a * cost;
			float face_newy = face_b * sint;
			float face_outerRadius = sqrt(
					face_newx * face_newx + face_newy * face_newy);

			//thresholdblur
			cv::Vec3b source = image.at < cv::Vec3b > (j, i);
			int sum0 = 0;
			int sum1 = 0;
			int sum2 = 0;
			int size = 0;
			for (int x = i - kh2; x <= i + kh2; x++) {
				for (int y = j - kw2; y <= j + kw2; y++) {
					cv::Vec3b data = image.at < cv::Vec3b > (y, x);
					if ((abs(data[0] - source[0]) < threshold)
							&& (abs(data[1] - source[1]) < threshold)
							&& (abs(data[1] - source[1]) < threshold)) {
						sum0 += data[0];
						sum1 += data[1];
						sum2 += data[2];
						size++;
					}
				}
			}

			int avg0 = sum0 / size;
			int avg1 = sum1 / size;
			int avg2 = sum2 / size;

			resultPixel[0] = avg0;
			resultPixel[1] = avg1;
			resultPixel[2] = avg2;

			int k = (int) (((face_outerRadius - face_innerRadius)
					/ face_outerRadius) * 255);
			int transformk = (int) (k * (smooth_skin_alpha / 100.0));
			if (transformk > 255) {
				transformk = 255;
			}

			resultPixel[0] = BlendFilter::blendTransparencyComp(transformk,
					source[0], resultPixel[0]);
			resultPixel[1] = BlendFilter::blendTransparencyComp(transformk,
					source[1], resultPixel[1]);
			resultPixel[2] = BlendFilter::blendTransparencyComp(transformk,
					source[2], resultPixel[2]);

			image.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}
}

std::string itoa(long n){
	std::stringstream stream;
	stream <<n;
	return stream.str();
}

void FaceDetectionFilter::correctCurveFace(cv::Mat& image, cv::Rect& face) {
	int cols = image.cols;
	int rows = image.rows;
	cv::Mat orig;

	if (balanse_face_color_alpha == 0) {
		return;
	}

	int center_x = face.x + face.width / 2;
	int center_y = face.y + face.height / 4;
	cv::Vec3b data = image.at < cv::Vec3b > (center_y, center_x);
	cv::Vec3b dif;

	dif[0] = data[0];
	if (dif[0] < 95) {
		dif[0] = 95;
	}
	dif[1] = data[1];
	if (dif[1] < 95) {
		dif[1] = 95;
	}
	dif[2] = data[2];
	if (dif[2] < 95) {
		dif[2] = 95;
	}

	cv::Mat tmp;
	tmp.create(image.size(), image.type());
	image.copyTo(tmp);

	cv::Vec3b resultPixel;

	CurveFilter curveFilter;

	curveFilter.setParam("blue_curve", "0,0;" + itoa(dif[0]) + ",255;255,255");
	curveFilter.setParam("green_curve", "0,0;" + itoa(dif[1]) + ",255;255,255");
	curveFilter.setParam("red_curve", "0,0;" + itoa(dif[2]) + ",255;255,255");
	curveFilter.process(image);

	for (int i = 0; i < cols; i++) {
		for (int j = 0; j < rows; j++) {
			cv::Vec3b sourceA = tmp.at < cv::Vec3b > (j, i);
			cv::Vec3b sourceB = image.at < cv::Vec3b > (j, i);

			for (int ch = 0; ch < 3; ch++) {
				resultPixel[ch] = BlendFilter::blendTransparencyComp(balanse_face_color_alpha, sourceA[ch], sourceB[ch]);
			}

			image.at < cv::Vec3b > (j, i) = resultPixel;
		}
	}
}

void FaceDetectionFilter::unsharpEye(cv::Mat& image, cv::Rect& eye) {
	if (unsharp_eye_alpha == 0) {
		return;
	}

	int radius = cvRound((eye.width + eye.height) * 0.25);

	cv::Mat eyeROI = image(eye);

	int result = 0;
	int r2 = radius * radius;
	int a = eye.width / 2;
	int b = eye.height / 2;
	int a2 = a * a;
	int b2 = b * b;
	float scale = a / b;

	cv::Mat tmp;
	tmp.create(eyeROI.size(), eyeROI.type());
	eyeROI.copyTo(tmp);

	cv::Vec3b resultPixel;

	SharpnessFilter sharpnessFilter;
	sharpnessFilter.setParam("size", "91");//41
	sharpnessFilter.process(tmp);

	for (int k = 0; k < eyeROI.cols; k++) {
		for (int r = 0; r < eyeROI.rows; r++) {
			int x = k - a;
			int y = r - b;

			float coord = (x * x) / (a2 * 1.0) + (y * y) / (b2 * 1.0);
			int d = x * x + y * y;
			float innerRadius = sqrt(d);

			float cost = 0;
			float sint = 1;
			if (d != 0) {
				cost = x / innerRadius;
				sint = y / innerRadius;
			}
			float newx = a * cost;
			float newy = b * sint;
			float outerRadius = sqrt(newx * newx + newy * newy);

			int coef = 0;
			int transform = 0;
			if (coord < 1) {
				cv::Vec3b sourceA = eyeROI.at <cv::Vec3b> (r, k);
				cv::Vec3b sourceB = tmp.at <cv::Vec3b> (r, k);

				coef = (int) (((outerRadius - innerRadius) / outerRadius) * 255);
				transform = (int) (k * (unsharp_eye_alpha / 100.0));
				if (transform > 255) {
					transform = 255;
				}

				for (int ch = 0; ch < 3; ch++) {
					resultPixel[ch] = BlendFilter::blendTransparencyComp(transform, sourceA[ch], sourceB[ch]);
				}

				eyeROI.at < cv::Vec3b > (r, k) = resultPixel;
			}
		}
	}
}

void FaceDetectionFilter::whitenEye(cv::Mat& image, cv::Rect& eye) {
	int radius = cvRound((eye.width + eye.height) * 0.25);
//circle(image, center, radius, cv::Scalar(255, 0, 0), 4, 8, 0);

	cv::Mat eyeROI = image(eye);

	int result = 0;
	int r2 = radius * radius;
	int a = eye.width / 2;
	int b = eye.height / 2;
	int a2 = a * a;
	int b2 = b * b;
	float scale = a / b;

	for (int k = 0; k < eyeROI.cols; k++) {
		for (int r = 0; r < eyeROI.rows; r++) {
			int x = k - a;
			int y = r - b;

			float coord = (x * x) / (a2 * 1.0) + (y * y) / (b2 * 1.0);
			int d = x * x + y * y;
			float innerRadius = sqrt(d);

			float cost = 0;
			float sint = 1;
			if (d != 0) {
				cost = x / innerRadius;
				sint = y / innerRadius;
			}
			float newx = a * cost;
			float newy = b * sint;
			float outerRadius = sqrt(newx * newx + newy * newy);

//			LOGI("x = %d, y = %d", x, y);
//			LOGI("newx = %f, newy = %f", newx, newy);
//			LOGI("cost = %f, sint = %f", cost, sint);
//			LOGI("innerRadius = %f", innerRadius);
//			LOGI("outerRadius = %f", outerRadius);
			if (coord < 1) {
				cv::Vec3b pixel = eyeROI.at < cv::Vec3b > (r, k);
				for (int t = 0; t < 3; t++) {
					result = pixel[t]
							+ whiten_eye_max_coef * pixel[t]
									* (outerRadius - innerRadius) / outerRadius;
					if (result > 255)
						result = 255;
					pixel[t] = result;
				}
				eyeROI.at < cv::Vec3b > (r, k) = pixel;
			}
		}
	}
}

void FaceDetectionFilter::process(cv::Mat& image) {
	std::string pathFaceCascade = Config::resourcePath + face_cascade_name;
	std::string pathEyesCascade = Config::resourcePath + eyes_cascade_name;
	std::string pathMouthCascade = Config::resourcePath + mouth_cascade_name;
	std::string pathToCache;
	if (isPreview) {
		pathToCache = Config::resourcePath + cache_name_preview;
	} else {
		pathToCache = Config::resourcePath + cache_name;
	}

	FILE* fileSource = fopen(pathToCache.c_str(), "r");
	if (fileSource != NULL) {
		int faces_count;
		int eyes_count;
		int mouth_count;
		cv::Rect left_eye;
		cv::Rect right_eye;

		fscanf(fileSource, "%d\n", &faces_count);
		for (size_t i = 0; i < faces_count; i++) {
			cv::Rect face;
			fscanf(fileSource, "n%d\n%d\n%d\n%d\n", &face.x, &face.y,
					&face.height, &face.width);

			fscanf(fileSource, "%d\n", &eyes_count);
			for (int j = 0; j < eyes_count; j++) {
				cv::Rect eye;
				fscanf(fileSource, "n%d\n%d\n%d\n%d\n", &eye.x, &eye.y,
						&eye.height, &eye.width);
				if (j == 0) {
					left_eye.x = eye.x;
					left_eye.y = eye.y;
					left_eye.width = eye.width;
					left_eye.height = eye.height;
				}
				if (j == 1) {
					right_eye.x = eye.x;
					right_eye.y = eye.y;
					right_eye.width = eye.width;
					right_eye.height = eye.height;
				}
				unsharpEye(image, eye);
				whitenEye(image, eye);
			}

			cv::Rect mouth;
			fscanf(fileSource, "%d\n", &mouth_count);
			for (size_t j = 0; j < mouth_count; j++) {
				if (j > 0)
					break;
				//			eye.x += faces[i].x;
				//			eye.y += faces[i].y;
				//			eye.y += eye.height / 4;
				//			eye.height -= eye.height / 2;
				fscanf(fileSource, "n%d\n%d\n%d\n%d\n", &mouth.x, &mouth.y,
						&mouth.height, &mouth.width);
			}

			if (eyes_count >= 2) {
				if (mouth_count > 0) {
					whitenFace(image, face, left_eye, right_eye, mouth);
				} else {
					cv::Rect mouth;
					mouth.x = 0;
					mouth.y = 0;
					mouth.width = 0;
					mouth.height = 0;
					whitenFace(image, face, left_eye, right_eye, mouth);
				}
			}
			if (i == 0) {
				correctCurveFace(image, face);
			}
			addBrightnessFace(image, face);
			addTemperatureFace(image, face);
			if (mouth_count > 0) {
				addWhitenTeeth(image, face, mouth);
			}
		}

		fclose(fileSource);
		return;
	}

	if (!face_cascade.load(pathFaceCascade)) {
		printf("--(!)Error loading\n");
		LOGI("error loading");
		return;
	};
	if (!eyes_cascade.load(pathEyesCascade)) {
		printf("--(!)Error loading\n");
		return;
	};
	if (!mouth_cascade.load(pathMouthCascade)) {
		printf("--(!)Error loading\n");
		return;
	};

	std::vector < cv::Rect > faces;
	cv::Mat frame_gray;

	cvtColor(image, frame_gray, CV_BGR2GRAY);
	equalizeHist(frame_gray, frame_gray);

//-- Detect faces
	face_cascade.detectMultiScale(frame_gray, faces, 1.1, 2,
			2, cv::Size(30, 30));

	LOGI("faces.size() = %d", faces.size());
	int faces_count = 0;
	for (size_t j = 0; j < faces.size(); j++) {
		faces_count++;
	}

	FILE* file = fopen(pathToCache.c_str(), "w");
	if (file != NULL) {
		fprintf(file, "%d\n", faces_count);
	}
	for (size_t i = 0; i < faces.size(); i++) {
		cv::Mat faceROI = frame_gray(faces[i]);
		std::vector < cv::Rect > eyes;

		if (file != NULL) {
			cv::Rect face = faces[i];
			fprintf(file, "n%d\n%d\n%d\n%d\n", face.x, face.y, face.height,
					face.width);
		}

		//-- In each face, detect eyes
		eyes_cascade.detectMultiScale(faceROI, eyes, 1.1, 2,
				0 | CV_HAAR_SCALE_IMAGE, cv::Size(30, 30));

		LOGI("eyes.size() = %d", eyes.size());
		int eyes_count = 0;
		for (size_t j = 0; j < eyes.size(); j++) {
			eyes_count++;
		}

		if (eyes_count == 1) {
			cv::Rect eye;
			eye.y = eyes[0].y;
			eye.height = eyes[0].height;
			eye.width = eyes[0].width;
			if (eyes[0].x < (faces[i].width / 2)) {
				eye.x = eyes[0].x + eyes[0].width * 2;
				LOGI("add eye from right");
			} else {
				eye.x = eyes[0].x - eyes[0].width * 2;
				LOGI("add eye from left");
			}
			eyes.push_back(eye);
			eyes_count++;
		}

		if (file != NULL) {
			fprintf(file, "%d\n", eyes_count);
			for (size_t j = 0; j < eyes.size(); j++) {
				cv::Rect eye = eyes[j];
				eye.x += faces[i].x;
				eye.y += faces[i].y;
				eye.y += eye.height / 4;
				eye.height -= eye.height / 2;
				fprintf(file, "n%d\n%d\n%d\n%d\n", eye.x, eye.y, eye.height,
						eye.width);
			}
		}

		cv::Rect left_eye, right_eye;
		for (size_t j = 0; j < eyes.size(); j++) {
			cv::Rect eye = eyes[j];
			eye.x += faces[i].x;
			eye.y += faces[i].y;
			eye.y += eye.height / 4;
			eye.height -= eye.height / 2;
			if (j == 0) {
				left_eye.x = eye.x;
				left_eye.y = eye.y;
				left_eye.width = eye.width;
				left_eye.height = eye.height;
			}
			if (j == 1) {
				right_eye.x = eye.x;
				right_eye.y = eye.y;
				right_eye.width = eye.width;
				right_eye.height = eye.height;
			}
			unsharpEye(image, eye);
			whitenEye(image, eye);
		}

		std::vector < cv::Rect > mouthes;
		cv::Rect face = faces[i];
		face.y = face.y + (face.height * 2) / 3;
		face.height = face.height / 3;
		cv::Mat faceMouthROI = frame_gray(face);

		mouth_cascade.detectMultiScale(faceMouthROI, mouthes, 1.15, 4, //1.1,2
				0 | CV_HAAR_SCALE_IMAGE, cv::Size(30, 30));
		LOGI("mouthes.size() = %d", mouthes.size());
		int mouth_count = 0;
		for (size_t j = 0; j < mouthes.size(); j++) {
			mouth_count++;
		}

		if (file != NULL) {
			fprintf(file, "%d\n", mouth_count);
			for (size_t j = 0; j < mouthes.size(); j++) {
				cv::Rect mouth = mouthes[j];
				fprintf(file, "n%d\n%d\n%d\n%d\n", mouth.x, mouth.y,
						mouth.height, mouth.width);
			}
		}

		if (eyes_count >= 2) {
			LOGI(
					"whiten face faces[i].x = (%d, %d, %d, %d)", faces[i].x, faces[i].y, faces[i].width, faces[i].height);
			if (mouth_count > 0) {
				whitenFace(image, faces[i], left_eye, right_eye, mouthes[0]);
			} else {
				cv::Rect mouth;
				mouth.x = 0;
				mouth.y = 0;
				mouth.width = 0;
				mouth.height = 0;
				whitenFace(image, faces[i], left_eye, right_eye, mouth);
			}
		}
		if (i == 0) {
			correctCurveFace(image, faces[i]);
		}
		addBrightnessFace(image, faces[i]);
		addTemperatureFace(image, faces[i]);
		if (mouth_count > 0) {
			addWhitenTeeth(image, faces[i], mouthes[0]);
		}
	}
	if (file != NULL) {
		fclose(file);
	}
}
