package org.raveen.thesis.iphoto.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public final class PPCUtils {

    private static final String TAG = PPCUtils.class.getSimpleName();

    public static Rect multiplyRect(
            final int bigToSmallImgScale, final Rect faceBoundingBoxSmall) {
        return new Rect(
                faceBoundingBoxSmall.left * bigToSmallImgScale,
                faceBoundingBoxSmall.top * bigToSmallImgScale,
                faceBoundingBoxSmall.right * bigToSmallImgScale,
                faceBoundingBoxSmall.bottom * bigToSmallImgScale);
    }

    public static org.opencv.core.Rect AndroidRectToOpenCVRect(
            final Rect faceBoundingBoxBig) {

        return new org.opencv.core.Rect(
                faceBoundingBoxBig.left, faceBoundingBoxBig.top,
                faceBoundingBoxBig.width(), faceBoundingBoxBig.height());
    }

    public static Rect translateY(final Rect faceBoundingBox, final float v) {
        return new Rect(
                faceBoundingBox.left,
                (int) (faceBoundingBox.top + v),
                faceBoundingBox.right,
                (int) (faceBoundingBox.bottom + v)
        );
    }

    public static Toast makeCenteredToast(
            Activity activity, int textRef, int duration) {
        Toast toast = Toast.makeText(activity, textRef, duration);
        return makeCenteredToast(toast);
    }

    private static Toast makeCenteredToast(final Toast toast) {
        try {
            LinearLayout layout = (LinearLayout) toast.getView();
            if (layout.getChildCount() > 0) {
                TextView tv = (TextView) layout.getChildAt(0);
                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            }
            toast.setGravity(Gravity.CENTER, 0, 0);
        } catch (NullPointerException e) {

        }
        return toast;
    }

    private PPCUtils() {
    }
}
