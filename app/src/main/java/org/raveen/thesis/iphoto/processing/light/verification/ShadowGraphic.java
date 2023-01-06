package org.raveen.thesis.iphoto.processing.light.verification;

import android.graphics.Canvas;

import org.raveen.thesis.iphoto.camera.Graphic;
import org.raveen.thesis.iphoto.camera.GraphicOverlay;
import org.raveen.thesis.iphoto.processing.PhotoValidity;
import org.raveen.thesis.iphoto.R;

public class ShadowGraphic extends Graphic {

    {
        getActionsMap().put(
                ShadowActions.NOT_UNIFORM,
                new BitmapMetaData(
                        ShadowGraphic.class, R.drawable.face_shadow,
                        PhotoValidity.WARNING));
    }

    public ShadowGraphic(final GraphicOverlay overlay) {
        super(overlay);
    }

    @Override
    public void draw(final Canvas canvas) {
        drawActionsToBePerformed(canvas);
    }
}
