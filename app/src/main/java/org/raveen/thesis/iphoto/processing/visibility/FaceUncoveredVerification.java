package org.raveen.thesis.iphoto.processing.visibility;

import android.app.Activity;
import android.graphics.Rect;

import com.google.mlkit.vision.face.Face;

import org.raveen.thesis.iphoto.camera.Graphic;
import org.raveen.thesis.iphoto.camera.GraphicOverlay;
import org.raveen.thesis.iphoto.processing.Action;
import org.raveen.thesis.iphoto.processing.Verifier;
import org.raveen.thesis.iphoto.processing.face.FaceUtils;
import org.raveen.thesis.iphoto.utils.ImageUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.raveen.thesis.iphoto.PhotoMakerActivity.PREVIEW_HEIGHT;
import static org.raveen.thesis.iphoto.PhotoMakerActivity.PREVIEW_WIDTH;
import static org.opencv.imgproc.Imgproc.TM_CCORR_NORMED;

/**
 * Verifies if there are no objects covering face.
 */
public class FaceUncoveredVerification extends Verifier {

    private static final String TAG =
            FaceUncoveredVerification.class.getSimpleName();

    private final Graphic mVisibilityGraphic;

    public FaceUncoveredVerification(
            final Activity activity,
            final GraphicOverlay<Graphic> overlay) {
        super(activity, overlay);
        mVisibilityGraphic = new VisibilityGraphic(overlay);
    }

    @Override
    public Boolean verify(final byte[] data, final Face face) {

        List<Action> positions = new ArrayList<>();
        if (!FaceUtils.isFacePositionCorrect(face)) {
            mVisibilityGraphic.setBarActions(positions, mContext,
                    VisibilityGraphic.class);
            return null;
        }
        mOverlay.add(mVisibilityGraphic);
        Mat image = ImageUtils.getMatFromYuvBytes(
                data,
                PREVIEW_HEIGHT,
                PREVIEW_WIDTH);

        final Rect bBox = face.getBoundingBox();
        int left = bBox.left + bBox.width() / 16;
        int top = bBox.top + bBox.height() / 4;
        int right = bBox.right - bBox.width() / 16;
        int bottom = bBox.bottom + bBox.height() / 8;
        image = ImageUtils.cropMatToBoundingBox(
                image, new Rect(left, top, right, bottom));
        if (null == image) {
            return null;
        }
        if (!isSimilar(image)) {
            positions.add(VisibilityActions.HIDDEN);
        }
        mVisibilityGraphic.setBarActions(positions, mContext,
                VisibilityGraphic.class);
        image.release();
        return positions.size() == 0;
    }

    private boolean isSimilar(final Mat src) {

        int oneThird = src.width() / 3;
        Mat left = src.submat(
                0, src.height(),
                0, oneThird);

        Mat right = src.submat(
                0, src.height(),
                src.width() - oneThird, src.width());
        Core.flip(right, right, 1);

        Mat comparisionResult = new Mat();

        Imgproc.matchTemplate(left, right, comparisionResult, TM_CCORR_NORMED);

        final double epsilon = 0.96;
        final double similarity = comparisionResult.get(0, 0)[0];

        left.release();
        right.release();
        comparisionResult.release();

        return similarity > epsilon;
    }
}
