package org.raveen.thesis.iphoto;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static com.google.mlkit.vision.face.FaceDetectorOptions.CLASSIFICATION_MODE_ALL;
import static com.google.mlkit.vision.face.FaceDetectorOptions.LANDMARK_MODE_NONE;
import static com.google.mlkit.vision.face.FaceDetectorOptions.PERFORMANCE_MODE_FAST;
import static org.raveen.thesis.iphoto.PhotoMakerActivity.PREVIEW_HEIGHT;
import static org.raveen.thesis.iphoto.PhotoMakerActivity.PREVIEW_WIDTH;
import static org.raveen.thesis.iphoto.utils.ImageUtils.getFaceMatFromPictureTaken;
import static org.raveen.thesis.iphoto.utils.PPCUtils.makeCenteredToast;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.raveen.thesis.iphoto.camera.CameraSource;
import org.raveen.thesis.iphoto.camera.CameraSourcePreview;
import org.raveen.thesis.iphoto.camera.Graphic;
import org.raveen.thesis.iphoto.camera.GraphicOverlay;
import org.raveen.thesis.iphoto.processing.Verifier;
import org.raveen.thesis.iphoto.processing.background.verification.BackgroundVerifier;
import org.raveen.thesis.iphoto.processing.face.FaceTracker;
import org.raveen.thesis.iphoto.processing.light.verification.ShadowVerification;
import org.raveen.thesis.iphoto.processing.visibility.FaceUncoveredVerification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = PhotoMakerActivity.class.getSimpleName();

    private static final int      REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE      =
            {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    static {
        OpenCVLoader.initDebug();
    }

    private CameraSource                                    mCameraSource;
    private CameraSourcePreview                             mPreview;
    private GraphicOverlay<Graphic>                         mGraphicOverlay;
    private FaceTracker                                     mFaceTracker;
    private List<Verifier>                                  mVerifiers;
    private PhotoSender                                     photoSender;
    private Button                                          buttonTakePicture;
    private com.google.android.gms.vision.face.FaceDetector mDetectorPhoto;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        photoSender = (PhotoSender) context;
    }

    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    public void onViewCreated(
            @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPreview = view.findViewById(R.id.preview);

        buttonTakePicture = view.findViewById(
                R.id.take_photo_button);
        buttonTakePicture.setOnClickListener(this);

        mGraphicOverlay = view.findViewById(R.id.graphicOverlay);
        mGraphicOverlay.setOnTouchListener((view1, motionEvent) -> {
            mCameraSource.onTouch(motionEvent);
            return true;
        });

        mVerifiers = new ArrayList<>();
        mVerifiers.add(new ShadowVerification(
                getActivity(), mGraphicOverlay));
        mVerifiers.add(new FaceUncoveredVerification(
                getActivity(), mGraphicOverlay));
        try {
            mVerifiers.add(new BackgroundVerifier(
                    getActivity(), mGraphicOverlay));
        } catch (IOException e) {
            Toast.makeText(
                    getActivity(),
                    R.string.no_background_verification_error,
                    Toast.LENGTH_LONG).show();
        }

        mFaceTracker = new FaceTracker(mGraphicOverlay, requireActivity());

        mDetectorPhoto = new com.google.android.gms.vision
                .face.FaceDetector.Builder(requireActivity())
                .setProminentFaceOnly(true)
                .setMode(com.google.android.gms.vision
                        .face.FaceDetector.ACCURATE_MODE)
                .build();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Verifier verifier : mVerifiers) {
            verifier.close();
        }
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        createCameraSource();
        startCameraSource();
        if (null != mCameraSource) {
            buttonTakePicture.setEnabled(true);
        }
    }

    @Override
    public void onPause() {
        mPreview.release();
        mFaceTracker.clear();
        buttonTakePicture.setEnabled(false);
        super.onPause();
    }

    private void startCameraSource() throws SecurityException {

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_photo_button:
                try {
                    takePhoto();
                } catch (Exception e) {
//                        return TabLayout.MODE_FIXED;

                    makeCenteredToast(
                            getActivity(),
                            R.string.picture_making_failed,
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void createCameraSource() {
        Context context = requireActivity().getApplicationContext();

        requireActivity().setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(PERFORMANCE_MODE_FAST)
                        .setContourMode(LANDMARK_MODE_NONE)
                        .setClassificationMode(CLASSIFICATION_MODE_ALL)
                        .build();

        final FaceDetector mDetectorVideo = FaceDetection.getClient(options);

        CameraSource.Builder builder = new CameraSource
                .Builder(context, mDetectorVideo)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(PREVIEW_HEIGHT, PREVIEW_WIDTH)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                .setVerifiers(mVerifiers)
                .setFaceDetector(mFaceTracker)
                .setRequestedFps(15.0f);

        mCameraSource = builder.build();

    }

    private void requestStoragePermissions() {
        int permission = ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    private void takePhoto() {
        if (null == mCameraSource || null == mFaceTracker ||
                cannotMakePicture(null == mFaceTracker.getFaces() ||
                        mFaceTracker.getFaces().size() != 1)) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestStoragePermissions();
        }
        Toast toast = makeCenteredToast(getActivity(),
                R.string.wait_for_a_picture, Toast.LENGTH_LONG);
        toast.show();
        mCameraSource.takePicture(null, bytes -> {
            toast.cancel();
            Mat picture = getFaceMatFromPictureTaken(bytes, mDetectorPhoto);
            if (picture == null) {
                Toast.makeText(
                        getActivity(),
                        R.string.cannot_make_a_picture,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            photoSender.setPhoto(picture);
            try {
                photoSender.displayPreviewFragment();
            } catch (Exception e) {
                Log.e(TAG, "Exception happened " + e.getMessage());
            }
        });
    }


    private boolean cannotMakePicture(final boolean condition) {
        if (condition) {
            makeCenteredToast(
                    getActivity(),
                    R.string.cannot_make_a_picture,
                    Toast.LENGTH_LONG).show();
        }
        return condition;
    }

    public interface PhotoSender {

        void setPhoto(Mat pict);

        void displayPreviewFragment();
    }
}