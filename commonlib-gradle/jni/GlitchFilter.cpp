#include "GlitchFilter.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <cstdlib>
#include "utils.h"
#include <android/log.h>
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,"GlitchFilter",__VA_ARGS__)

void GlitchFilter::process(cv::Mat& image) {
  GlitchFilter::glitchFilterOpenCV(image, image, hue_, saturation_, value_);
}

void GlitchFilter::setParam(const std::string& name, const std::string& value) {
  if (name == "h") {
    hue_ = atoi(value.c_str());
  } else if (name == "v" ) {
    value_ = atoi(value.c_str());
  } else if (name == "s") {
    saturation_ = atoi(value.c_str());
  }
}

void GlitchFilter::moveSlice(cv::Mat & sourceImage, int x, int y, int w, int h, int toX, int toY,
                                                            int incH, int incS, int incV) {
   LOGD("rows: %d, cols: %d", sourceImage.rows, sourceImage.cols);
   /*if(w == -1) {
      w = sourceImage.rows;
   }
   if(h == -1) {
      h = sourceImage.cols;
   }*/

    const cv::Mat & _copyImage = sourceImage.clone();

    x+=sourceImage.cols;
    toX+=sourceImage.cols;
    y+=sourceImage.rows;
    toY+=sourceImage.rows;

   for(int i=0; i<h; i++) {
       for(int j=0; j<w; j++) {
           sourceImage.at<cv::Vec3b>((toY + i) % sourceImage.rows, (toX + j) % sourceImage.cols)
           = _copyImage.at<cv::Vec3b>((y + i) % _copyImage.rows, (x + j) % _copyImage.cols);
           sourceImage.at<cv::Vec3b>((toY + i) % sourceImage.rows, (toX + j) % sourceImage.cols)[0]+=incH;
           sourceImage.at<cv::Vec3b>((toY + i) % sourceImage.rows, (toX + j) % sourceImage.cols)[1]+=incS;
           sourceImage.at<cv::Vec3b>((toY + i) % sourceImage.rows, (toX + j) % sourceImage.cols)[2]+=incV;
       }
   }

   //for (int i = 0; i < h; i++) {
       //for (int j = 0; j < 100; j++) {
           //sourceImage.at<cv::Vec3b>(j, j) = pixel;
      /*     if(x + j < sourceImage.rows && x + j >= 0 && y + i >= 0 && y + i < sourceImage.cols) {
               cv::Vec3b pixel = sourceImage.at<cv::Vec3b>(x + j, y + i);
               sourceImage.at<cv::Vec3b>((toX + j) % sourceImage.rows, (toY + i) % sourceImage.cols ) = pixel;
           }*/
       //}
   //}
}

void GlitchFilter::glitchFilterOpenCV(const cv::Mat & sourceImage, cv::Mat & resultImage,
                                int hue, int saturation, int value) {
    /*float m_h = (hue / 360.0f) * 180.0f;
    float m_s = saturation / 100.0f;
    float m_v = value / 100.0f;

    cv::cvtColor(sourceImage, sourceImage, CV_BGR2HSV);
    */
    /*cv::Vec3b pixel;
    for (int i = 0; i < sourceImage.cols; i++) {
        for (int j = 0; j < sourceImage.rows; j++) {
            pixel = sourceImage.at<cv::Vec3b>(j, i);
            resultImage.at<cv::Vec3b>(j, i) = pixel;//modifyPixel(pixel, m_h, m_s, m_v);
        }
    }*/



    switch ( value ) {
    case 0:
        for(int i=0; i<10; i++) {
          // moveSlice(resultImage, 0, rand()%sourceImage.rows, sourceImage.cols - rand()%100, rand()%50+10, rand()%100-50, rand()%sourceImage.rows);
        }
        break;
    case 1:
        moveSlice(resultImage, 0, 180, sourceImage.cols, 30, 300, 300,0,0,0);
        moveSlice(resultImage, 0, 180, sourceImage.cols, 30, -40, 140,0,0,0);
        moveSlice(resultImage, 0, 180, sourceImage.cols, 30, 10, 110,0,0,0);
        moveSlice(resultImage, 0, 300, sourceImage.cols, 50, -20, 280,0,0,0);

        moveSlice(resultImage, 0, 440, sourceImage.cols, 60, -10, 380,0,0,0);
        moveSlice(resultImage, 0, 440, sourceImage.cols, 30, 30, 410,0,0,0);
          break;
    case 2:
//    1. Отступаем сверху 400рх, выделяем полоску высотой 130рх, затем эту полоску сдвигаем на 20рх вправо. Hue +330, Saturation +60, Lightness +30, Colorize
        moveSlice(resultImage, 0, 200, sourceImage.cols, 100, 15, 200,330,60,30);
//    2. Отступаем сверху 530рх, выделяем полоску высотой 170рх, затем эту полоску сдвигаем на 40рх вправо. Hue +330, Saturation +30, Lightness +30, Colorize
        moveSlice(resultImage, 0, 350, sourceImage.cols, 130, 30, 350,330,30,30);
//    3. Отступаем сверху 700рх, выделяем полоску высотой 100рх, затем эту полоску сдвигаем на 60рх вправо. Hue +330, Saturation +60, Lightness -30, Colorize
        moveSlice(resultImage, 0, 400, sourceImage.cols, 130, 40, 400,330,60,-30);
//    4. Сверху через Hue накладываем теrстуру texture_2
          break;
    case 3:
//                1. Начиная с самого верха выделяем полоску высотой 80рх, затем эту полоску сдвигаем на 50рх вправо.
        moveSlice(resultImage, 0, 0, sourceImage.cols, 80, 50, 0,0,0,0);
//                2. Отступаем сверху 80рх, выделяем полоску высотой 10рх, затем эту полоску сдвигаем на 90рх вправо.
        moveSlice(resultImage, 0, 80, sourceImage.cols, 10, 90, 80,0,0,0);
//                3. Отступаем сверху 90рх, выделяем полоску высотой 10рх, затем эту полоску сдвигаем на 110рх вправо.
        moveSlice(resultImage, 0, 90, sourceImage.cols, 10, 110, 90,0,0,0);
//                4. Отступаем сверху 100рх, выделяем полоску высотой 120рх, затем эту полоску сдвигаем на 130рх вправо.
        moveSlice(resultImage, 0, 100, sourceImage.cols, 120, 130, 100,0,0,0);
//                5. Отступаем сверху 220рх, выделяем полоску высотой 10рх, затем эту полоску сдвигаем на 140рх вправо.
        moveSlice(resultImage, 0, 220, sourceImage.cols, 10, 140, 220,0,0,0);
//                6. Отступаем сверху 230рх, выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 150рх вправо.
        moveSlice(resultImage, 0, 230, sourceImage.cols, 40, 150, 230,0,0,0);
//                7. Отступаем сверху 270рх, выделяем полоску высотой 200рх, затем эту полоску сдвигаем на 200рх вправо.
        moveSlice(resultImage, 0, 270, sourceImage.cols, 200, 200, 270,0,0,0);
//                8. Отступаем сверху 470рх, выделяем полоску высотой 100рх, затем эту полоску сдвигаем на 260рх вправо.
        moveSlice(resultImage, 0, 470, sourceImage.cols, 100, 260, 470,0,0,0);
//                9. Отступаем сверху 570рх, выделяем полоску высотой 120рх, затем эту полоску сдвигаем на 300рх вправо.
        moveSlice(resultImage, 0, 570, sourceImage.cols, 120, 300, 570,0,0,0);
          break;
    case 4:
//        1. Начиная с самого верха выделяем полоску высотой 100рх, затем эту полоску сдвигаем на 20рх вправо. Hue +60, Saturation +60, Lightness +30, Colorize
        moveSlice(resultImage, 0, 0, sourceImage.cols, 80, 20, 0,60,60,30);
//        2. Отступаем сверху 10рх, выделяем полоску высотой 60рх, затем эту полоску сдвигаем на 40рх вправо. Hue +240, Saturation +30, Colorize
        moveSlice(resultImage, 0, 10, sourceImage.cols, 60, 40, 10,240,30,0);
//        3. Отступаем сверху 160рх, выделяем полоску высотой 10рх, затем эту полоску сдвигаем на 10рх вправо. Hue +240, Saturation +60, Colorize
        moveSlice(resultImage, 0, 160, sourceImage.cols, 80, 20, 160,240,60,0);
//        4. Отступаем сверху 170рх, выделяем полоску высотой 10рх, затем эту полоску сдвигаем на 140рх вправо. Hue +240, Saturation +60, Colorize
        moveSlice(resultImage, 0, 170, sourceImage.cols, 10, 140, 170,240,60,0);
//        5. Отступаем сверху 190рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 160рх вправо. Hue +60, Saturation +30, Colorize
        moveSlice(resultImage, 0, 190, sourceImage.cols, 20, 160, 190,60,30,0);
//        6. Отступаем сверху 210рх, выделяем полоску высотой 140рх, затем эту полоску сдвигаем на 180рх вправо.
        moveSlice(resultImage, 0, 210, sourceImage.cols, 140, 180, 210,0,0,0);
//        7. Отступаем сверху 350рх, выделяем полоску высотой 270рх, затем эту полоску сдвигаем на 200рх вправо. Hue +60, Saturation +60, Lightness +30, Colorize
        moveSlice(resultImage, 0, 350, sourceImage.cols, 270, 200, 350,60,60,30);
//        8. Отступаем сверху 620рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 140рх вправо.
        moveSlice(resultImage, 0, 450, sourceImage.cols, 20, 140, 450,0,0,0);


          break;
    case 5:
//    1. Отступаем сверху 80рх, выделяем полоску высотой 80рх, затем эту полоску сдвигаем на 200рх вправо.
        moveSlice(resultImage, 0, 80, sourceImage.cols, 80, 200, 80,0,0,0);
//    2. Отступаем сверху 160рх, выделяем полоску высотой 100рх, затем эту полоску сдвигаем на 50рх вправо.
        moveSlice(resultImage, 0, 160, sourceImage.cols, 120, 50, 160,0,0,0);
//    3. Отступаем сверху 400рх, выделяем полоску высотой 220рх, затем эту полоску сдвигаем на 160рх вправо.
        moveSlice(resultImage, 0, 400, sourceImage.cols, 220, 160, 400,0,0,0);
//    4. Отступаем сверху 740рх, выделяем полоску высотой 60рх, затем эту полоску сдвигаем на 160рх вправо.
        moveSlice(resultImage, 0, 500, sourceImage.cols, 60, 160, 500,0,0,0);
          break;
    case 6:
//    1. Отступаем сверху 180рх выделяем полоску высотой 70рх, затем эту полоску сдвигаем на 20рх вправо. Hue +180, Saturation +60, Colorize
        moveSlice(resultImage, 0, 130, sourceImage.cols, 70, 20, 130,180,60,0);
//    2. Отступаем сверху 240рх, выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 10рх вправо. Hue +180, Saturation +60, Lightness +30, Colorize
        moveSlice(resultImage, 0, 200, sourceImage.cols, 40, 10, 200,180,60,30);
//    3. Отступаем сверху 380рх, выделяем полоску высотой 30рх, затем эту полоску сдвигаем на 20рх вправо. Hue +180, Saturation +60, Lightness +60, Colorize
        moveSlice(resultImage, 0, 340, sourceImage.cols, 30, 20, 340,180,60,60);
//    4. Отступаем сверху 410рх, выделяем полоску высотой 10рх, затем эту полоску сдвигаем на 10рх вправо. Hue +180, Saturation +60, Lightness +70, Colorize
        moveSlice(resultImage, 0, 390, sourceImage.cols, 10, 10, 390,180,60,70);
//    5. Отступаем сверху 420рх, выделяем полоску высотой 50рх, затем эту полоску сдвигаем на 40рх вправо.
        moveSlice(resultImage, 0, 400, sourceImage.cols, 50, 40, 400,0,0,0);
//    6. Отступаем сверху 520рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 50рх вправо.
        moveSlice(resultImage, 0, 470, sourceImage.cols, 20, 50, 470,0,0,0);
//    7. Отступаем сверху 540рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 60рх вправо. Hue +180, Saturation +60, Lightness +30, Colorize
        moveSlice(resultImage, 0, 500, sourceImage.cols, 20, 60, 500,180,60,30);
//    8. Отступаем сверху 560рх, выделяем полоску высотой 140рх, затем эту полоску сдвигаем на 80рх вправо. Hue +180, Saturation +60, Lightness +60, Colorize
        moveSlice(resultImage, 0, 530, sourceImage.cols, 40, 80, 530,180,60,60);
          break;
    case 7:
//    1. Отступаем сверху 50рх выделяем полоску высотой 100рх, затем эту полоску сдвигаем на 20рх вправо. Hue -40
        moveSlice(resultImage, 0, 50, sourceImage.cols, 100, 20, 50,-40,0,0);
//    2. Отступаем сверху 300рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 10рх вправо.
        moveSlice(resultImage, 0, 300, sourceImage.cols, 20, 10, 300,0,0,0);
//    3. Отступаем сверху 320рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 20рх вправо. Hue -40
        moveSlice(resultImage, 0, 320, sourceImage.cols, 20, 20, 320,-40,0,0);
//    4. Отступаем сверху 340рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 30рх вправо. Hue -40, Saturation -50
        moveSlice(resultImage, 0, 340, sourceImage.cols, 20, 30, 340,-40,-50,0);
//    5. Отступаем сверху 360рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 40рх вправо. Hue +120, Saturation +50, Colorize
        moveSlice(resultImage, 0, 360, sourceImage.cols, 20, 40, 360,120,50,0);
//    6. Отступаем сверху 380рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 60рх вправо.
        moveSlice(resultImage, 0, 380, sourceImage.cols, 20, 60, 380,0,0,0);
//    7. Отступаем сверху 500рх, выделяем полоску высотой 100рх, затем эту полоску сдвигаем на 20рх вправо.
        moveSlice(resultImage, 0, 500, sourceImage.cols, 100, 20, 500,0,0,0);
          break;
    case 8:
//    1. Отступаем сверху 140рх выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 40рх вверх.
        moveSlice(resultImage, 0, 140, sourceImage.cols, 40, 0, 100,0,0,0);
//    2. Отступаем сверху 140рх, выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 80рх вверх.
        moveSlice(resultImage, 0, 140, sourceImage.cols, 40, 0, 60,0,0,0);
//    3. Отступаем сверху 300рх, выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 40рх вверх и на 40рх влево.
        moveSlice(resultImage, 0, 300, sourceImage.cols, 40, -40, 260,0,0,0);
//    4. Отступаем сверху 300рх, выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 40рх вверх и 20рх влево.
        moveSlice(resultImage, 0, 300, sourceImage.cols, 40, -20, 260,0,0,0);
//    5. Отступаем сверху 500рх, выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 40рх влево. Hue -140
        moveSlice(resultImage, 0, 500, sourceImage.cols, 40, -40, 500,-140,0,0);
          break;
    case 9:
//    1. Отступаем сверху 140рх выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 20рх вниз.
        moveSlice(resultImage, 0, 100, sourceImage.cols, 20, 0, 120,0,0,0);
//    2. Отступаем сверху 140рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 40рх вниз.
        moveSlice(resultImage, 0, 100, sourceImage.cols, 20, 0, 140,0,0,0);
//    3. Отступаем сверху 300рх, выделяем полоску высотой 60рх, затем эту полоску сдвигаем на 10рх вниз..
        moveSlice(resultImage, 0, 200, sourceImage.cols, 60, 0, 210,0,0,0);
//    4. Отступаем сверху 300рх, выделяем полоску высотой 60рх, затем эту полоску сдвигаем на 20рх вниз.
        moveSlice(resultImage, 0, 200, sourceImage.cols, 60, 0, 220,0,0,0);
//    5. Отступаем сверху 500рх, выделяем полоску высотой 100рх, затем эту полоску сдвигаем на 20рх вниз и на 20рх вправо.
        moveSlice(resultImage, 0, 400, sourceImage.cols, 100, 20, 420,0,0,0);
//    6. Отступаем сверху 640рх, выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 20рх вниз.
        moveSlice(resultImage, 0, 500, sourceImage.cols, 40, 0, 520,0,0,0);
//    7. Отступаем сверху 640рх, выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 40рх вниз и на 20рх влево.
        moveSlice(resultImage, 0, 500, sourceImage.cols, 40, -20, 540,0,0,0);

          break;
    case 10:
//    1. Отступаем сверху 160рх, выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 40рх вверх.
        moveSlice(resultImage, 0, 100, sourceImage.cols, 40, 0, 60,0,0,0);
//    2. Отступаем сверху 160рх, выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 80рх вверх.
        moveSlice(resultImage, 0, 100, sourceImage.cols, 40, 0, 20,0,0,0);
//    3. Отступаем сверху 160рх, выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 120рх вверх.
        moveSlice(resultImage, 0, 100, sourceImage.cols, 40, 0, 10,0,0,0);
//    4. Отступаем сверху 160рх, выделяем полоску высотой 40рх, затем эту полоску сдвигаем на 160рх вверх.
        moveSlice(resultImage, 0, 100, sourceImage.cols, 40, 0, 0,0,0,0);
//    5. Отступаем сверху 360рх, выделяем полоску высотой 100рх, затем эту полоску сдвигаем на 40рх вправо.
        moveSlice(resultImage, 0, 300, sourceImage.cols, 100, 40, 0,0,0,0);
//    6. Отступаем сверху 360рх, выделяем полоску высотой 100рх, затем эту полоску сдвигаем на 40рх вправо и на 40рх вниз.
        moveSlice(resultImage, 0, 300, sourceImage.cols, 100, 40, 340,0,0,0);
//    7. Отступаем сверху 700рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 20рх вниз.
        moveSlice(resultImage, 0, 500, sourceImage.cols, 20, 0, 520,0,0,0);
//    8. Отступаем сверху 700рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 40рх вниз.
        moveSlice(resultImage, 0, 500, sourceImage.cols, 20, 0, 540,0,0,0);
//    9. Отступаем сверху 700рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 60рх вниз.
        moveSlice(resultImage, 0, 500, sourceImage.cols, 20, 0, 560,0,0,0);
//    10. Отступаем сверху 700рх, выделяем полоску высотой 20рх, затем эту полоску сдвигаем на 80рх вниз.
        moveSlice(resultImage, 0, 500, sourceImage.cols, 20, 0, 580,0,0,0);

          break;
    }






    //cv::cvtColor(resultImage, resultImage, CV_HSV2BGR);
}

cv::Vec3b GlitchFilter::modifyPixel(cv::Vec3b pixel, float hue, float saturation_diff, float vdiff_percent) {
    pixel[0] = (pixel[0] + (int)hue + 180) % 180;
    
    uchar currentSaturation = pixel[1];
    // here vdiff_percent is in range -1..+1
    currentSaturation = constrain(currentSaturation + saturation_diff * currentSaturation, 0, 255);
    pixel[1] = currentSaturation;

    uchar currentValue = pixel[2];
    // here vdiff_percent is in range -1..+1
    if (vdiff_percent < 0) {
        currentValue += currentValue * vdiff_percent; // so if vdiff_percent is -1 - it will go to 0.
    } else {
        currentValue += (255 - currentValue) * vdiff_percent;  // the value will be between v and
    }
    pixel[2] = currentValue;
    pixel[0] = 255;
    pixel[1] = 255;
    pixel[2] = 255;

    return pixel;
}
