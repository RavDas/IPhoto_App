package org.raveen.thesis.iphoto.processing.light.enhancement;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import org.raveen.thesis.iphoto.DetectorTensorflowLite;
import org.raveen.thesis.iphoto.processing.Enhancer;
import org.raveen.thesis.iphoto.processing.light.ShadowUtils;
import org.raveen.thesis.iphoto.utils.ImageUtils;
import org.opencv.core.Mat;

import java.io.IOException;

import static org.raveen.thesis.iphoto.processing.light.ShadowUtils.isEvenlyLightened;

public abstract class ShadowRemover extends DetectorTensorflowLite
        implements Enhancer {

    private static final String TAG = ShadowRemover.class.getSimpleName();
    /** Mat holding the image for processing. */
    protected            Mat    mImage;

    ShadowRemover(final Activity activity) throws IOException {
        super(activity);
        Log.d(TAG, "Created a Tensorflow Lite Shadow Remover.");
    }

    @Override
    public Mat enhance(final Mat src) {
        Mat input = prepareInput(src);
        Mat deshadowed = getDeshadowedOverlay(input);
        input.release();
        deshadowed = ImageUtils.resizeMatToFinalSize(deshadowed);
        deshadowed = ShadowUtils.overlayDeshadowed(src, deshadowed);
        return deshadowed;
    }

    private Mat prepareInput(final Mat src) {

        Mat image = ImageUtils.resizeMat(src, getOutputImageWidth(),
                getImageSizeY());
        image = ImageUtils.padMatToSquareBlack(image, getImageSizeY());
        return image;
    }

    private Mat getDeshadowedOverlay(final Mat src) {
        if (mTflite == null) {
            Log.e(TAG, "Shadow remover has not been initialized; Skipped.");
        }
        mImage = src;

        Mat resizedMat = ImageUtils.resizeMat(src, getImageSizeX(),
                getImageSizeY());
        Bitmap tmp = ImageUtils.getBitmapFromMat(resizedMat);
        resizedMat.release();
        Bitmap bmp = tmp.copy(tmp.getConfig(), true);
        ImageUtils.safelyRemoveBitmap(tmp);
        convertBitmapToByteBuffer(bmp);
        ImageUtils.safelyRemoveBitmap(bmp);

        runInference();
        Mat deshadowed = getDeshadowedOverlay();
        deshadowed = ImageUtils.unpadMatFromSquare(
                deshadowed,
                getOutputImageWidth());
        return deshadowed;
    }

    protected abstract Mat getDeshadowedOverlay();

    protected abstract int getOutputImageWidth();

    @Override
    public boolean verify(final Mat src) {
        return isEvenlyLightened(src);
    }
}
