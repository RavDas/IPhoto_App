package org.raveen.thesis.iphoto.camera;

import android.Manifest;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.annotation.RequiresPermission;

import java.io.IOException;

import static org.raveen.thesis.iphoto.camera.GraphicOverlay.TOP_RECT_W_TO_H_RATIO;

public class CameraSourcePreview extends ViewGroup {

    private static final String TAG = CameraSourcePreview.class.getSimpleName();
    private final SurfaceView    mSurfaceView;
    private       boolean        mStartRequested;
    private       boolean        mSurfaceAvailable;
    private       CameraSource   mCameraSource;
    private       GraphicOverlay mOverlay;

    public CameraSourcePreview(
            final Context context,
            final AttributeSet attrs) {
        super(context, attrs);
        mStartRequested = false;
        mSurfaceAvailable = false;

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(final CameraSource cameraSource)
            throws IOException, SecurityException {
        mCameraSource = cameraSource;
        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(
            final CameraSource cameraSource,
            final GraphicOverlay overlay)
            throws IOException, SecurityException {
        mOverlay = overlay;
        start(cameraSource);
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void startIfReady() throws IOException {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(mSurfaceView.getHolder());
            if (mOverlay != null) {
                Size size = mCameraSource.getPreviewSize();
                mOverlay.setCameraInfo(size.getHeight(), size.getWidth());
            }
            mStartRequested = false;
        }
    }

    @Override
    protected void onLayout(
            final boolean changed,
            final int left,
            final int top,
            final int right,
            final int bottom) {

        final int layoutWidth = right - left;
        final int layoutHeight = layoutWidth * 45 / 35;
        final int cameraPreviewTopOffset =
                (int) (layoutWidth / TOP_RECT_W_TO_H_RATIO);

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(
                    0, 0, layoutWidth, layoutHeight + cameraPreviewTopOffset);
        }
        if (mSurfaceView != null) {
            mSurfaceView.layout(
                    0, cameraPreviewTopOffset,
                    layoutWidth, layoutHeight + cameraPreviewTopOffset);
        }

        try {
            startIfReady();
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        } catch (SecurityException se) {
            Log.e(TAG, "Do not have permission to start the camera", se);
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(final SurfaceHolder surface) {
            mSurfaceAvailable = true;
            try {
                startIfReady();
            } catch (SecurityException se) {
                Log.e(TAG, "Do not have permission to start the camera", se);
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceChanged(
                final SurfaceHolder holder,
                final int format, final int width,
                final int height) {
        }

        @Override
        public void surfaceDestroyed(final SurfaceHolder surface) {
            mSurfaceAvailable = false;
        }

    }
}
