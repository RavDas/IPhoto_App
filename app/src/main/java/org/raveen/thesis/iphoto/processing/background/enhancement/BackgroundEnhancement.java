package org.raveen.thesis.iphoto.processing.background.enhancement;

import android.app.Activity;

import org.jetbrains.annotations.NotNull;
import org.raveen.thesis.iphoto.processing.Enhancer;
import org.raveen.thesis.iphoto.processing.background.BackgroundUtils;
import org.raveen.thesis.iphoto.processing.background.ImageSegmentor;
import org.raveen.thesis.iphoto.processing.background.ImageSegmentorFloatMobileUNet;
import org.raveen.thesis.iphoto.processing.background.verification.BackgroundProperties;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

import static org.opencv.core.CvType.CV_32FC3;
import static org.opencv.core.CvType.CV_8UC3;

public class BackgroundEnhancement implements Enhancer {

    private static final String TAG =
            BackgroundEnhancement.class.getSimpleName();

    public  ImageSegmentor mSegmentor;
    private BackgroundProperties mBackgroundProperties;


    public BackgroundEnhancement(final Activity activity) throws IOException {
        mSegmentor = new ImageSegmentorFloatMobileUNet(activity);
        mBackgroundProperties = new BackgroundProperties();
    }

    @Override
    public boolean verify(final Mat src) {
        Mat background = BackgroundUtils.getBackground(src, mSegmentor);
        return BackgroundUtils.isUniform(
                background, mBackgroundProperties, mSegmentor) < 3;
    }
//store the values of an image
    @Override
    public Mat enhance(final Mat src) {

        if (mSegmentor == null) {
            return null;
        }
        Mat input = src.clone();
        if (src.channels() == 4) {
            Imgproc.cvtColor(input, input, Imgproc.COLOR_RGBA2RGB);
        }
        Mat personMask = BackgroundUtils.getPersonMask(input, mSegmentor);

        Mat person = input.clone();
        person.convertTo(person, CV_32FC3);
        Core.multiply(personMask, person, person);

        Mat background = getBlurredMaskedBackground(input, personMask);

        Core.add(person, background, background);

        input.release();
        personMask.release();
        person.release();
        background.convertTo(background, CV_8UC3);
        return background;
    }

    @NotNull
    private Mat getBlurredMaskedBackground(
            final Mat input, final Mat personMask) {

        Mat background = input.clone();

        Imgproc.medianBlur(background, background, 51);
        background.convertTo(background, -1, 1, 30);
        background.convertTo(background, CV_32FC3);

        Mat invertedMask =
                new Mat(personMask.rows(), personMask.cols(), personMask.type(),
                        new Scalar(-1, -1, -1));
        Core.multiply(invertedMask, personMask, invertedMask);
        Core.add(invertedMask, new Scalar(1, 1, 1), invertedMask);
        Core.multiply(background, invertedMask, background);

        invertedMask.release();
        return background;
    }


    @Override
    public void close() {
        if (mSegmentor != null) {
            mSegmentor.close();
        }
    }
}
