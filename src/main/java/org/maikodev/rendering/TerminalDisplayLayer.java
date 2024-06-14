package org.maikodev.rendering;

import org.maikodev.exceptions.BufferSizeMismatchException;
import org.maikodev.exceptions.DisplayBufferException;
import org.maikodev.order.RowMajor;

import java.nio.Buffer;
import java.util.Arrays;

public class TerminalDisplayBuffer implements IRenderable {
    public TerminalDisplayBuffer(int rowSize, int columnSize)  {
        this.MAX_ROWS = rowSize;
        this.MAX_COLUMNS = columnSize;

        PIXEL_COUNT = rowSize * columnSize;

        PIXEL_BUFFER = new char[PIXEL_COUNT];
        TRANSPARENCY_BUFFER = new boolean[PIXEL_COUNT];
    }

    /* Should throw an exception */
    public TerminalDisplayBuffer(char[] pixelBuffer, boolean[] transparencyBuffer, int rowSize, int columnSize) throws DisplayBufferException {
        this.MAX_ROWS = rowSize;
        this.MAX_COLUMNS = columnSize;

        PIXEL_COUNT = rowSize * columnSize;
        if (pixelBuffer.length != PIXEL_COUNT || transparencyBuffer.length != PIXEL_COUNT) throw new BufferSizeMismatchException();

        PIXEL_BUFFER = pixelBuffer;
        TRANSPARENCY_BUFFER = transparencyBuffer;
    }

    /* Should throw an exception */
    public TerminalDisplayBuffer(char[] pixelBuffer, int rowSize, int columnSize) throws DisplayBufferException {
        this.MAX_ROWS = rowSize;
        this.MAX_COLUMNS = columnSize;

        PIXEL_COUNT = rowSize * columnSize;
        if (pixelBuffer.length != PIXEL_COUNT) throw new BufferSizeMismatchException();

        PIXEL_BUFFER = pixelBuffer;
        TRANSPARENCY_BUFFER = new boolean[PIXEL_BUFFER.length];
        Arrays.fill(TRANSPARENCY_BUFFER, true);
    }

    @Override
    public char[] getPixelBuffer() { return PIXEL_BUFFER; }

    @Override
    public boolean[] getTransparencyBuffer() { return TRANSPARENCY_BUFFER; }

    @Override
    public char getPixel(int rowMajorIndex) throws IndexOutOfBoundsException {
        if (rowMajorIndex < 0 || rowMajorIndex >= PIXEL_COUNT) throw new IndexOutOfBoundsException();

        return (TRANSPARENCY_BUFFER[rowMajorIndex]) ? PIXEL_BUFFER[rowMajorIndex] : ' ';
    }

    @Override
    public boolean isPixelVisible(int rowMajorIndex) throws IndexOutOfBoundsException {
        if (rowMajorIndex < 0 || rowMajorIndex >= PIXEL_COUNT) throw new IndexOutOfBoundsException();

        return TRANSPARENCY_BUFFER[rowMajorIndex];
    }

    @Override
    public int getBufferWidth() { return MAX_COLUMNS; }

    @Override
    public int getBufferHeight() { return MAX_ROWS; }

    public void write(char displayPixel, boolean isVisible, int row, int column) {
        int pixelIndex = RowMajor.getIndex(row, column, MAX_COLUMNS);

        if (pixelIndex >= PIXEL_COUNT) return;

        PIXEL_BUFFER[pixelIndex] = displayPixel;
        TRANSPARENCY_BUFFER[pixelIndex] = isVisible;
    }

    public void write(char displayPixel, int row, int column) {
        write(displayPixel, true, row, column);
    }

    public void writeln(char[] displayPixels, boolean[] isVisible, int row, int column) throws DisplayBufferException {
        /* This should probably throw an exception if displayPixel and isVisible are not
         * the same size */
        if (displayPixels.length != isVisible.length) throw new DisplayBufferException("Provided visibility buffer incompatible with displayPixels!");

        for (int columnIndex = column, readPointer = 0; columnIndex < MAX_COLUMNS && readPointer < displayPixels.length; columnIndex++) {
            int pixelIndex = RowMajor.getIndex(row, columnIndex, MAX_COLUMNS);

            PIXEL_BUFFER[pixelIndex] = displayPixels[readPointer];
            TRANSPARENCY_BUFFER[pixelIndex] = isVisible[readPointer];
        }
    }

    public void writeln(char[] displayPixels, int row, int column) {
        writeln(displayPixels, getDefaultTransparency(displayPixels.length), row, column);
    }

    public void writeln(String displayPixels, boolean[] isVisible, int row, int column) {
        writeln(displayPixels.toCharArray(), getDefaultTransparency(displayPixels.length()), row, column);
    }

    public void writeln(String displayPixels, int row, int column) {
        writeln(displayPixels.toCharArray(), getDefaultTransparency(displayPixels.length()), row, column);
    }

    private static boolean[] getDefaultTransparency(int size) {
        boolean[] buffer = new boolean[size];
        Arrays.fill(buffer, true);

        return buffer;
    }

    private final int MAX_ROWS;
    private final int MAX_COLUMNS;
    private final int PIXEL_COUNT;

    private final char[] PIXEL_BUFFER;
    private final boolean[] TRANSPARENCY_BUFFER;
}
