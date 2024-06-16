package org.maikodev.thread.task;

import org.maikodev.order.RowMajor;

public class ClearDensityTask implements Runnable {
    public ClearDensityTask(byte[] densityMatrix, int assignedRow, int maxColumns) {
        PARTICLE_DENSITY_MAP = densityMatrix;
        ASSIGNED_ROW = assignedRow;
        MAX_COLUMN = maxColumns;
    }

    @Override
    public void run() {
        for (int column = 0; column < MAX_COLUMN; column++) {
            PARTICLE_DENSITY_MAP[RowMajor.getIndex(ASSIGNED_ROW, column, MAX_COLUMN)] = 0;
        }
    }

    private final byte[] PARTICLE_DENSITY_MAP;
    private final int ASSIGNED_ROW;
    private final int MAX_COLUMN;
}
