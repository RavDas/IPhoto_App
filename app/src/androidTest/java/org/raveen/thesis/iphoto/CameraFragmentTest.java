package org.raveen.thesis.iphoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.raveen.thesis.iphoto.camera.Graphic;
import org.raveen.thesis.iphoto.camera.GraphicOverlay;
import org.raveen.thesis.iphoto.processing.background.verification.BackgroundVerifier;
import org.raveen.thesis.iphoto.processing.light.verification.ShadowVerification;
import org.raveen.thesis.iphoto.processing.visibility.FaceUncoveredVerification;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.mlkit.vision.face.FaceDetectorOptions.CLASSIFICATION_MODE_ALL;
import static com.google.mlkit.vision.face.FaceDetectorOptions.LANDMARK_MODE_NONE;
import static com.google.mlkit.vision.face.FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE;
import static org.raveen.thesis.iphoto.utils.TestUtils.getBitmapFromFile;
import static org.raveen.thesis.iphoto.utils.TestUtils.getYuvBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class CameraFragmentTest {
    @Rule
    public ActivityTestRule<PhotoMakerActivity> mActivityTestRule =
            new ActivityTestRule<PhotoMakerActivity>(PhotoMakerActivity.class);
    ShadowVerification        shadowVerification        = null;
    FaceUncoveredVerification faceUncoveredVerification = null;
    BackgroundVerifier        backgroundVerifier        = null;
    Context                   context                   = null;
    FaceDetectorOptions       options                   = null;

    @Before
    public void setUp() throws Exception {
        PhotoMakerActivity mActivity = mActivityTestRule.getActivity();
        Fragment cameraFragment =
                mActivity.getSupportFragmentManager()
                         .findFragmentById(R.id.camera_place);
        View viewT = cameraFragment.getView();

        GraphicOverlay<Graphic> mGraphicOverlayT =
                viewT.findViewById(R.id.graphicOverlay);

        shadowVerification = new ShadowVerification(
                mActivity, mGraphicOverlayT);

        faceUncoveredVerification = new FaceUncoveredVerification(
                mActivity, mGraphicOverlayT);

        backgroundVerifier = new BackgroundVerifier(
                mActivity, mGraphicOverlayT);

        context = InstrumentationRegistry.getInstrumentation().getContext();
        options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(PERFORMANCE_MODE_ACCURATE)
                        .setContourMode(LANDMARK_MODE_NONE)
                        .setClassificationMode(CLASSIFICATION_MODE_ALL)
                        .build();
    }

    @Test
    public void testBackgroundVerificationPositive()
            throws IOException, InterruptedException {

        final Bitmap in = getBitmapFromFile("face.jpg", context);
        final byte[] yuv = getYuvBytes(in);
        final InputImage image = InputImage.fromBitmap(in, 0);
        final List<Face> result = getFaces(image);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(backgroundVerifier.verify(yuv, result.get(0)));
    }


    @Test
    public void testBackgroundVerificationNegative()
            throws IOException, InterruptedException {

        final Bitmap in = getBitmapFromFile("background1.jpg", context);
        final byte[] yuv = getYuvBytes(in);
        final InputImage image = InputImage.fromBitmap(in, 0);
        final List<Face> result = getFaces(image);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(backgroundVerifier.verify(yuv, result.get(0)));
    }

    @Test
    public void testShadowVerificationPositive()
            throws IOException, InterruptedException {

        final Bitmap in = getBitmapFromFile("face.jpg", context);
        final byte[] yuv = getYuvBytes(in);
        final InputImage image = InputImage.fromBitmap(in, 0);
        final List<Face> result = getFaces(image);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(shadowVerification.verify(yuv, result.get(0)));
    }

    @Test
    public void testShadowVerificationNegative()
            throws IOException, InterruptedException {

        final Bitmap in = getBitmapFromFile("shadow3.jpg", context);
        final byte[] yuv = getYuvBytes(in);
        final InputImage image = InputImage.fromBitmap(in, 0);
        final List<Face> result = getFaces(image);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(shadowVerification.verify(yuv, result.get(0)));
    }

    @Test
    public void testFaceCoveredVerificationPositive()
            throws IOException, InterruptedException {

        final Bitmap in = getBitmapFromFile("light2.png", context);
        final byte[] yuv = getYuvBytes(in);
        final InputImage image = InputImage.fromBitmap(in, 0);
        final List<Face> result = getFaces(image);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(faceUncoveredVerification.verify(yuv, result.get(0)));
    }

    @Test
    public void testFaceCoveredVerificationNegative()
            throws IOException, InterruptedException {

        final Bitmap in = getBitmapFromFile("face_covered.jpg", context);
        final byte[] yuv = getYuvBytes(in);
        final InputImage image = InputImage.fromBitmap(in, 0);
        final List<Face> result = getFaces(image);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(faceUncoveredVerification.verify(yuv, result.get(0)));
    }

    private List<Face> getFaces(final InputImage image)
            throws InterruptedException {
        final Task<List<Face>> p = FaceDetection
                .getClient(options)
                .process(image);
        while (!p.isComplete()) {
            TimeUnit.SECONDS.sleep(1);
        }
        return p.getResult();
    }
}