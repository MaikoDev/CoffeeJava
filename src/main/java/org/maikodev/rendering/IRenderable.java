package org.maikodev.rendering;

public interface IRenderable {
    char[] getPixelBuffer();
    boolean[] getTransparencyBuffer();

    int getBufferWidth();
    int getBufferHeight();
}
