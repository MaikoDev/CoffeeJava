package org.maikodev.rendering;

public interface IRenderable {
    char[] getPixelBuffer();
    boolean[] getTransparencyBuffer();

    char getPixel(int rowMajorIndex);
    boolean isPixelVisible(int rowMajorIndex);

    int getBufferWidth();
    int getBufferHeight();
}
