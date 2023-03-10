package org.raveen.thesis.iphoto.processing.background;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import org.raveen.thesis.iphoto.DetectorTensorflowLite;
import org.raveen.thesis.iphoto.utils.ImageUtils;
import org.opencv.core.Mat;

import java.io.IOException;

/**
 * Segments images with Tensorflow Lite.
 */
public abstract class ImageSegmentor extends DetectorTensorflowLite {

    public static final int PROCESS_IMG_SIZE = 448;

    private static final String TAG = ImageSegmentor.class.getSimpleName();

    /** Mat holding the image for processing. */
    protected Mat mImage;

    ImageSegmentor(final Activity activity) throws IOException {
        super(activity);
        Log.d(TAG, "Created a Tensorflow Lite Image Segmentor.");
    }

    /**
     * Segments received image.
     */
    public void segmentImg(Mat image) {
        if (mTflite == null) {
            Log.e(TAG, "Image segmentor has not been initialized; Skipped.");
        }
        mImage = image;

        Mat resizedMat128 = ImageUtils.resizeMat(image, getImageSizeX(),
                getImageSizeY());
        Bitmap tmp = ImageUtils.getBitmapFromMat(resizedMat128);
        resizedMat128.release();
        Bitmap bmp = tmp.copy(tmp.getConfig(), true);
        ImageUtils.safelyRemoveBitmap(tmp);
        convertBitmapToByteBuffer(bmp);
        ImageUtils.safelyRemoveBitmap(bmp);

        runInference();
    }

    /**
     * Returns mask hiding background, in the size of input image (but padded
     * to square!) and in CV_32F color scheme.
     *
     * @return mask
     */
    public abstract Mat getMaskedBackground();

    /**
     * Returns mask hiding person, in the size of input image (but padded
     * to square!) and in CV_32F color scheme.
     *
     * @return mask
     */
    public abstract Mat getMaskedPerson();

    /**
     * Returns original image with detected person painted black.
     *
     * @return image with person painted black
     */
    public abstract Mat getBackground();

}
