package org.raveen.thesis.iphoto.processing.background.verification;

import android.graphics.Canvas;


import org.raveen.thesis.iphoto.camera.Graphic;
import org.raveen.thesis.iphoto.camera.GraphicOverlay;
import org.raveen.thesis.iphoto.processing.PhotoValidity;
import org.raveen.thesis.iphoto.R;

public class BackgroundGraphic extends Graphic {

    {
        getActionsMap().put(
                BackgroundActions.NOT_UNIFORM,
                new BitmapMetaData(
                        BackgroundGraphic.class, R.drawable.non_uniform,
                        PhotoValidity.WARNING));
        getActionsMap().put(
                BackgroundActions.TOO_DARK,
                new BitmapMetaData(
                        BackgroundGraphic.class, R.drawable.too_dark,
                        PhotoValidity.INVALID));
    }

    public BackgroundGraphic(final GraphicOverlay overlay) {
        super(overlay);
    }

    @Override
    public void draw(final Canvas canvas) {
        drawActionsToBePerformed(canvas);
    }
}
