package org.raveen.thesis.iphoto.processing.visibility;

import android.graphics.Canvas;

import org.raveen.thesis.iphoto.camera.Graphic;
import org.raveen.thesis.iphoto.camera.GraphicOverlay;
import org.raveen.thesis.iphoto.processing.PhotoValidity;
import org.raveen.thesis.iphoto.R;

public class VisibilityGraphic extends Graphic {

    {
        getActionsMap().put(
                VisibilityActions.HIDDEN,
                new BitmapMetaData(
                        VisibilityGraphic.class, R.drawable.face_covered,
                        PhotoValidity.WARNING));
    }

    public VisibilityGraphic(final GraphicOverlay overlay) {
        super(overlay);
    }

    @Override
    public void draw(final Canvas canvas) {
        drawActionsToBePerformed(canvas);
    }
}
