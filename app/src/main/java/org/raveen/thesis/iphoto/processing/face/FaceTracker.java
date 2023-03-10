package org.raveen.thesis.iphoto.processing.face;

import android.content.Context;

import com.google.mlkit.vision.face.Face;

import org.raveen.thesis.iphoto.camera.Graphic;
import org.raveen.thesis.iphoto.camera.GraphicOverlay;
import org.raveen.thesis.iphoto.processing.Action;

import java.util.ArrayList;
import java.util.List;

public class FaceTracker {

    static final double NEUTRAL_FACE_THRESHOLD = 0.5;
    static final double ROTATION_X_THRESHOLD   = 12; // up down
    static final double ROTATION_Y_THRESHOLD   = 8; // right left
    static final double ROTATION_Z_THRESHOLD   = 4; // with or against clock
    static final double EYES_OPEN_THRESHOLD    = 0.7;

    private static final String                  TAG =
            FaceTracker.class.getSimpleName();
    private final        GraphicOverlay<Graphic> mOverlay;
    private final        FaceGraphic             mFaceGraphic;
    private final        Context                 mContext;
    private              List<Face>              mFaces;

    public FaceTracker(
            final GraphicOverlay<Graphic> overlay,
            final Context context) {
        super();
        this.mContext = context;
        this.mOverlay = overlay;
        mFaceGraphic = new FaceGraphic(overlay, context);
        mOverlay.add(mFaceGraphic);
    }

    public void processFaces(final List<Face> faces) {
        mFaces = faces;
        List<Action> positions = new ArrayList<>();
        mFaceGraphic.updateFaces(faces);

        if (null == faces || faces.size() == 0) {
            mFaceGraphic.clearActions();
            return;
        }
        if (faces.size() > 1) {
            mFaceGraphic.clearActions();
            positions.add(FaceActions.TOO_MANY_FACES);
            mFaceGraphic.setBarActions(positions, mContext, FaceGraphic.class);
            return;
        }

        Face face = faces.get(0);

        if (face.getHeadEulerAngleY() < -ROTATION_Y_THRESHOLD) {
            positions.add(FaceActions.ROTATE_RIGHT);
        } else if (face.getHeadEulerAngleY() > ROTATION_Y_THRESHOLD) {
            positions.add(FaceActions.ROTATE_LEFT);
        }
        if (face.getHeadEulerAngleX() < -ROTATION_X_THRESHOLD) {
            positions.add(FaceActions.FACE_UP);
        } else if (face.getHeadEulerAngleX() > ROTATION_X_THRESHOLD) {
            positions.add(FaceActions.FACE_DOWN);
        }
        if (face.getHeadEulerAngleZ() < -ROTATION_Z_THRESHOLD) {
            positions.add(FaceActions.STRAIGHTEN_FROM_RIGHT);
        } else if (face.getHeadEulerAngleZ() > ROTATION_Z_THRESHOLD) {
            positions.add(FaceActions.STRAIGHTEN_FROM_LEFT);
        }
        if (null != face.getLeftEyeOpenProbability() &&
                face.getLeftEyeOpenProbability() < EYES_OPEN_THRESHOLD) {
            positions.add(FaceActions.LEFT_EYE_OPEN);
        }
        if (null != face.getRightEyeOpenProbability() &&
                face.getRightEyeOpenProbability() < EYES_OPEN_THRESHOLD) {
            positions.add(FaceActions.RIGHT_EYE_OPEN);
        }
        if (null != face.getSmilingProbability() &&
                face.getSmilingProbability() > NEUTRAL_FACE_THRESHOLD) {
            positions.add(FaceActions.NEUTRAL_MOUTH);
        }
        mFaceGraphic.setBarActions(positions, mContext, FaceGraphic.class);
    }

    public List<Face> getFaces() {
        return mFaces;
    }

    public void clear() {
        mFaceGraphic.updateFaces(new ArrayList<>());
        mFaceGraphic.clearActions();
        mFaceGraphic.clearBoundingBoxes();
    }
}
