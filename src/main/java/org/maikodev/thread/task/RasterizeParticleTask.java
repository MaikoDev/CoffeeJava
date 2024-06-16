package org.maikodev.thread.task;


import org.maikodev.order.RowMajor;
import org.w3c.dom.xpath.XPathResult;

public class RasterizeParticleTask implements Runnable {
    public RasterizeParticleTask(char[] pixelBuffer, boolean[] transparencyBuffer, byte[] densityMap, int assignedRow, int maxColumns) {
        PIXEL_BUFFER = pixelBuffer;
        TRANSPARENCY_BUFFER = transparencyBuffer;
        DENSITY_MAP = densityMap;
        ASSIGNED_ROW = assignedRow;
        MAX_COLUMN = maxColumns;
    }

    @Override
    public void run() {
        for (int column = 0; column < MAX_COLUMN; column++) {
            int index = RowMajor.getIndex(ASSIGNED_ROW, column, MAX_COLUMN);

            byte density = DENSITY_MAP[index];
            char rasterCharacter;
            boolean isVisible = true;

            if (density < 2) {
                rasterCharacter = ' ';
                isVisible = false;
            } else if (density < 7) {
                rasterCharacter = PARTICLE_CHARACTERS[0];
            } else if (density < 12) {
                rasterCharacter = PARTICLE_CHARACTERS[1];
            } else {
                rasterCharacter = PARTICLE_CHARACTERS[2];
            }

            PIXEL_BUFFER[index] = rasterCharacter;
            TRANSPARENCY_BUFFER[index] = isVisible;
        }
    }

    private final char[] PIXEL_BUFFER;
    private final boolean[] TRANSPARENCY_BUFFER;
    private final byte[] DENSITY_MAP;
    private final int ASSIGNED_ROW;
    private final int MAX_COLUMN;

    public static final char[] PARTICLE_CHARACTERS = new char[]{ '░', '▒', '▓' };
}
