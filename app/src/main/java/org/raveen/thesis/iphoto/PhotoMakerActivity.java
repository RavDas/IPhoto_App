package org.raveen.thesis.iphoto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import org.raveen.thesis.iphoto.processing.Enhancer;
import org.raveen.thesis.iphoto.processing.background.enhancement.BackgroundEnhancement;
import org.raveen.thesis.iphoto.processing.light.enhancement.ShadowRemoverPix2Pix;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.raveen.thesis.iphoto.MainActivity.FILE_NAME;

public class PhotoMakerActivity extends AppCompatActivity
        implements org.raveen.thesis.iphoto.CameraFragment.PhotoSender,
                   org.raveen.thesis.iphoto.PhotoPreviewFragment.PhotoReceiver {

    public static final int PREVIEW_WIDTH  = 480;
    public static final int PREVIEW_HEIGHT = 640;

    private static final String TAG = PhotoMakerActivity.class.getSimpleName();

    static {
        OpenCVLoader.initDebug();
    }

    public  androidx.fragment.app.Fragment cameraFragment       = null;
    public org.raveen.thesis.iphoto.PhotoPreviewFragment photoPreviewFragment = null;
    private Mat                            picture;
    private List<Enhancer>                 mEnhancers;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.i(TAG, "Creating photo maker activity.");

        setContentView(R.layout.photo_capture);
        mEnhancers = new ArrayList<>();
        try {
            mEnhancers.add(new BackgroundEnhancement(this));
        } catch (IOException e) {
            Toast.makeText(this, R.string.no_background_verification_error,
                    Toast.LENGTH_LONG).show();
        }

        try {
            mEnhancers.add(new ShadowRemoverPix2Pix(this));
        } catch (IOException e) {
            Toast.makeText(
                    this,
                    R.string.no_face_shadow_removal_error,
                    Toast.LENGTH_SHORT).show();
        }
        cameraFragment =
                getSupportFragmentManager().findFragmentById(R.id.camera_place);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroying photo maker activity.");
        for (Enhancer enhancer : mEnhancers) {
            enhancer.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraFragment.isVisible()) {
            cameraFragment.onResume();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (photoPreviewFragment != null &&
                    photoPreviewFragment.isVisible()) {
                displayCameraFragment();
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception happened");
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void displayPreviewFragment() {
        try {
            if (photoPreviewFragment == null) {
                photoPreviewFragment = new PhotoPreviewFragment();
            }
            FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.camera_place, photoPreviewFragment);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(null);
            transaction.commit();
            cameraFragment.onPause();
        } catch (NullPointerException e){

        }
    }

    @Override
    public void displayCameraFragment() {
        cameraFragment.onResume();
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public Mat getPhoto() {
        return picture;
    }

    @Override
    public void setPhoto(Mat thePicture) {
        picture = thePicture;

        for (Enhancer enhancer : mEnhancers) {
            if (!enhancer.verify(picture)) {
                picture = enhancer.enhance(picture);
            }
        }
    }

    @Override
    public void finishWithResult(String theFileName) {
        Intent intent = new Intent();
        intent.putExtra(FILE_NAME, theFileName);
        setResult(RESULT_OK, intent);
        finish();
    }
}