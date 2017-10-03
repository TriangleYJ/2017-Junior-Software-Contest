#include <jni.h>
#include "com_yjprojects_jsctest2_activity_ImageActivity.h"

#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
using namespace cv;

jboolean ifred, ifgreen, ifblue, ifyellow;

extern "C" {

    JNIEXPORT void JNICALL Java_com_yjprojects_jsctest2_activity_ImageActivity_Convert(
            JNIEnv *env,
            jobject instance,
            jlong input,
            jlong matAddrResult,
            jlong a,
            jlong b,
            jlong c,
            jlong d, jbooleanArray bools, jint quality){

        jboolean *pBool = env->GetBooleanArrayElements(bools, NULL);
        ifred = (bool)pBool[0];
        ifgreen = (bool)pBool[1];
        ifblue = (bool)pBool[2];
        ifyellow = (bool)pBool[3];



        Mat &image = *(Mat *)input;
        Mat &matResult = *(Mat *)matAddrResult;
        Mat &strip = *(Mat *)a;
        Mat &cstrip = *(Mat *)b;
        Mat &dot = *(Mat *)c;
        Mat &xs = *(Mat *)d;


        int h = image.size().height;
        int w = image.size().width;
        double scale = quality / (double)min(h, w);
        resize(image, image, Size(), scale, scale);
        h = image.size().height;
        w = image.size().width;
        int i, j;

        cvtColor(image, image, CV_RGB2HSV);
        if (ifred) {
            for (i = 0; i < h; i++) {
                for (j = 0; j < w; j++) {
                    if (strip.at<Vec3b>(i, j)[0] > 120) continue;
                    int ta = image.at<Vec3b>(i, j)[0], tb = image.at<Vec3b>(i, j)[1], tc = image.at<Vec3b>(i, j)[2];
                    if ((ta > 10 && ta < 160) || tb < 70 || tc < 80) continue;
                    image.at<Vec3b>(i, j)[1] = tb / 2;
                    ta = ta > 90 ? 180 - ta : ta;
                    image.at<Vec3b>(i, j)[2] = 200 - ta * 2;
                }
            }
        }
        if (ifgreen) {
            for (i = 0; i < h; i++) {
                for (j = 0; j < w; j++) {
                    if (cstrip.at<Vec3b>(i, j)[0] > 120) continue;
                    int ta = image.at<Vec3b>(i, j)[0], tb = image.at<Vec3b>(i, j)[1], tc = image.at<Vec3b>(i, j)[2];
                    if (ta < 33 || ta>85 || tb < 70 || tc < 80) continue;
                    image.at<Vec3b>(i, j)[1] = tb / 3;
                    image.at<Vec3b>(i, j)[2] = 220 - abs(60 - ta) * 2;
                }
            }
        }
        if (ifblue) {
            for (i = 0; i < h; i++) {
                for (j = 0; j < w; j++) {
                    if (xs.at<Vec3b>(i, j)[0] > 120) continue;
                    int ta = image.at<Vec3b>(i, j)[0], tb = image.at<Vec3b>(i, j)[1], tc = image.at<Vec3b>(i, j)[2];
                    if (ta < 95 || ta>135 || tb < 70 || tc < 80) continue;
                    image.at<Vec3b>(i, j)[1] = tb / 3;
                    image.at<Vec3b>(i, j)[2] = 255 - abs(120 - ta) * 2;
                }
            }
        }
        if(ifyellow){
            for (i = 0; i < h; i++) {
                for (j = 0; j < w; j++) {
                    if (dot.at<Vec3b>(i, j)[0] > 120) continue;
                    int ta = image.at<Vec3b>(i, j)[0], tb = image.at<Vec3b>(i, j)[1], tc = image.at<Vec3b>(i, j)[2];
                    if (ta<16 || ta>32 || tb < 70 || tc < 80) continue;
                    image.at<Vec3b>(i, j)[1] = tb / 3;
                    image.at<Vec3b>(i, j)[2] = 255 - abs(24-ta) * 2;
                }
            }
        }

        cvtColor(image, matResult, CV_HSV2RGB);
    }



}