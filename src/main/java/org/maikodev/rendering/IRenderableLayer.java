package org.maikodev.rendering;

public interface IRenderableLayer {
    char[] getPixelBuffer();
    boolean[] getTransparencyBuffer();

    char getPixel(int rowMajorIndex);
    boolean isPixelVisible(int rowMajorIndex);

    int getPixelCount();
    int getBufferWidth();
    int getBufferHeight();
}
