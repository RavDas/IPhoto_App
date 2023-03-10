package org.raveen.thesis.iphoto.processing.face;

import static org.raveen.thesis.iphoto.camera.GraphicOverlay.TOP_RECT_W_TO_H_RATIO;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.mlkit.vision.face.Face;

import org.raveen.thesis.iphoto.R;

import org.raveen.thesis.iphoto.camera.Graphic;
import org.raveen.thesis.iphoto.camera.GraphicOverlay;
import org.raveen.thesis.iphoto.processing.PhotoValidity;
import org.raveen.thesis.iphoto.utils.PPCUtils;

import java.util.ArrayList;
import java.util.List;

public class FaceGraphic extends Graphic {

    private static final int        ARROW_MIN_SIZE = 12;
    private static final int        ARROW_MAX_SIZE = 24;
    private              double     bbProportionWidth;
    private              List<Rect> mFaceBoundingBoxes;
    private volatile     List<Face> mFaces;
    private              Context    mContext;
    private              int        mArrowsScale   = ARROW_MIN_SIZE;

    {
        getActionsMap().put(
                FaceActions.ROTATE_LEFT,
                new Graphic.BitmapMetaData(FaceGraphic.class, R.drawable.arrow_left,
                        PhotoValidity.INVALID));
        getActionsMap().put(
                FaceActions.ROTATE_RIGHT,
                new BitmapMetaData(FaceGraphic.class, R.drawable.arrow_right,
                        PhotoValidity.INVALID));
        getActionsMap().put(
                FaceActions.STRAIGHTEN_FROM_LEFT,
                new BitmapMetaData(
                        FaceGraphic.class, R.drawable.arrow_straighten_right,
                        PhotoValidity.INVALID));
        getActionsMap().put(
                FaceActions.STRAIGHTEN_FROM_RIGHT,
                new BitmapMetaData(
                        FaceGraphic.class, R.drawable.arrow_straighten_left,
                        PhotoValidity.INVALID));
        getActionsMap().put(
                FaceActions.FACE_DOWN,
                new BitmapMetaData(FaceGraphic.class, R.drawable.arrow_down,
                        PhotoValidity.INVALID));
        getActionsMap().put(
                FaceActions.FACE_UP,
                new BitmapMetaData(FaceGraphic.class, R.drawable.arrow_up,
                        PhotoValidity.INVALID));
        getActionsMap().put(
                FaceActions.LEFT_EYE_OPEN,
                new BitmapMetaData(
                        FaceGraphic.class, R.drawable.eye,
                        PhotoValidity.INVALID));
        getActionsMap().put(
                FaceActions.RIGHT_EYE_OPEN,
                new BitmapMetaData(
                        FaceGraphic.class, R.drawable.eye,
                        PhotoValidity.INVALID));
        getActionsMap().put(
                FaceActions.NEUTRAL_MOUTH,
                new BitmapMetaData(
                        FaceGraphic.class, R.drawable.mouth,
                        PhotoValidity.INVALID));
        getActionsMap().put(
                FaceActions.TOO_MANY_FACES,
                new BitmapMetaData(
                        FaceGraphic.class, R.drawable.too_many_faces,
                        PhotoValidity.INVALID));
    }

    FaceGraphic(final GraphicOverlay overlay, final Context context) {
        super(overlay);
        mContext = context;
        mFaceBoundingBoxes = new ArrayList<>();
    }

    public void updateFaces(final List<Face> faces) {
        mFaces = faces;
        postInvalidate();
    }

    public void clearBoundingBoxes(){
        mFaceBoundingBoxes.clear();
    }

    @Override
    public void draw(final Canvas canvas) {
        clearBoundingBoxes();
        if (null == mFaces || mFaces.size() == 0) {
            return;
        }
        int i = 0;
        for (Face face : mFaces) {
            mFaceBoundingBoxes.add(i, FaceUtils.getFaceBoundingBox(face, this));
            Rect displayBoundingBox = PPCUtils.translateY(
                    mFaceBoundingBoxes.get(i),
                    getGraphicOverlay().getWidth() / TOP_RECT_W_TO_H_RATIO);
            canvas.drawRect(displayBoundingBox, getPaint());
        }
        drawActionsToBePerformed(canvas);
        if (mFaces.size() == 1) {
            setFirstBoundingBoxProportions();
            if (isFaceTooSmall()) {
                drawEnlargingInfo(canvas);
            }
        }
    }

    private boolean isFaceTooSmall() {
        return bbProportionWidth < 0.5;
    }

    private void drawEnlargingInfo(final Canvas canvas) {
        Rect bBox = mFaceBoundingBoxes.get(0);
        Bitmap enlarge = BitmapFactory.decodeResource(
                mContext.getResources(),
                R.drawable.enlarge);
        final Rect rectSrc = new Rect(0, 0, enlarge.getWidth(),
                enlarge.getHeight());
        int dstHalfWidth = bBox.width() * mArrowsScale++ /
                ARROW_MAX_SIZE;
        int dstHalfHeight =
                dstHalfWidth * enlarge.getHeight() / enlarge.getWidth();
        final int centerX = bBox.centerX();
        final int centerY = (int) (bBox.centerY() +
                getGraphicOverlay().getWidth() / TOP_RECT_W_TO_H_RATIO);
        final Rect rectDst = new Rect(
                centerX - dstHalfWidth,
                centerY - dstHalfHeight,
                centerX + dstHalfWidth,
                centerY + dstHalfHeight);
        canvas.drawBitmap(enlarge, rectSrc, rectDst, new Paint());
        if (mArrowsScale == ARROW_MAX_SIZE) {
            mArrowsScale = ARROW_MIN_SIZE;
        }
    }

    private void setFirstBoundingBoxProportions() {
        double canvasWidth = getGraphicOverlay().getWidth();
        bbProportionWidth = mFaceBoundingBoxes.get(0).width() / canvasWidth;
    }
}