package org.raveen.thesis.iphoto.processing.face;

import org.raveen.thesis.iphoto.processing.Action;

public enum FaceActions implements Action {
    ROTATE_LEFT,
    ROTATE_RIGHT,
    FACE_UP,
    FACE_DOWN,
    STRAIGHTEN_FROM_LEFT,
    STRAIGHTEN_FROM_RIGHT,
    LEFT_EYE_OPEN,
    RIGHT_EYE_OPEN,
    NEUTRAL_MOUTH,
    TOO_MANY_FACES
}
