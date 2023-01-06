package org.raveen.thesis.iphoto.processing.light.verification;

import android.app.Activity;
import android.graphics.Rect;
import android.util.Log;

import com.google.mlkit.vision.face.Face;

import org.raveen.thesis.iphoto.camera.Graphic;
import org.raveen.thesis.iphoto.camera.GraphicOverlay;
import org.raveen.thesis.iphoto.processing.Action;
import org.raveen.thesis.iphoto.processing.Verifier;
import org.raveen.thesis.iphoto.utils.ImageUtils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.raveen.thesis.iphoto.PhotoMakerActivity.PREVIEW_HEIGHT;
import static org.raveen.thesis.iphoto.PhotoMakerActivity.PREVIEW_WIDTH;
import static org.raveen.thesis.iphoto.processing.light.ShadowUtils.isEvenlyLightened;

/**
 * Verifies if face does not contain side shadows.
 */
public class ShadowVerification extends Verifier {

    private static final String TAG =
            ShadowVerification.class.getSimpleName();

    private final Graphic mShadowGraphic;
    private       ShadowVerificator mShadowVerificator;

    public ShadowVerification(
            final Activity activity,
            final GraphicOverlay<Graphic> overlay) {
        super(activity, overlay);
        mShadowGraphic = new ShadowGraphic(overlay);
        try {
            mShadowVerificator =
                    new ShadowVerificatorFloatMobileNetV2(activity);
            Log.i(TAG, "Successfully initialized shadow verifier.");
        } catch (IOException e) {
            Log.e(
                    TAG,
                    "Could not initialize shadow verifier tensorflow model. " +
                            "Program will run without it.");
            e.printStackTrace();
        }
    }

    @Override
    public Boolean verify(final byte[] data, final Face face) {

        mOverlay.add(mShadowGraphic);
        List<Action> positions = new ArrayList<>();

        Mat image = ImageUtils.getMatFromYuvBytes(
                data,
                PREVIEW_HEIGHT,
                PREVIEW_WIDTH);
        Rect bbox = face.getBoundingBox();
        int left = bbox.left + bbox.width() / 8;
        int right = bbox.right - bbox.width() / 8;
        int top = bbox.top + bbox.height() / 4;
        image = ImageUtils.cropMatToBoundingBox(
                image,
                new Rect(left, top, right, bbox.bottom));

        if (null == image) {
            return null;
        }

        ShadowVerificator.EvenlyLightened isEvenlyLightened = null;
        if (mShadowVerificator != null) {
            mShadowVerificator.classify(image);
            isEvenlyLightened = mShadowVerificator.isEvenlyLightened();
        }

        if (isEvenlyLightened == ShadowVerificator.EvenlyLightened.SHADOW) {
            positions.add(ShadowActions.NOT_UNIFORM);
            Log.i(TAG, "Face is not evenly lightened.");
        } else if (null == isEvenlyLightened || isEvenlyLightened ==
                ShadowVerificator.EvenlyLightened.NOT_SURE) {
            Log.i(TAG, "Verifying with OpenCV if face's evenly lightened.");
            if (!isEvenlyLightened(image)) {
                positions.add(ShadowActions.NOT_UNIFORM);
            }
        }

        mShadowGraphic.setBarActions(positions, mContext,
                ShadowGraphic.class);

        image.release();
        return positions.size() == 0;
    }

}
