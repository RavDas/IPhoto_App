package org.raveen.thesis.iphoto.processing;

import org.opencv.core.Mat;

public interface Enhancer {
    Mat enhance(final Mat src);

    boolean verify(final Mat src);

    void close();
}
