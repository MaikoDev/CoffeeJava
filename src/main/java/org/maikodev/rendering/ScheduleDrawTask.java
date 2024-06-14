package org.maikodev.rendering;

import org.maikodev.order.Position;
import org.maikodev.order.RowMajor;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ScheduleDrawTask implements Runnable {
    public ScheduleDrawTask(IRenderableLayer[] renderables, ConcurrentLinkedQueue<Position> taskQueue, char[] drawBuffer, char[] displayBuffer, int assignedRow, int maxColumns) {
        RENDERABLE_LAYERS = renderables;
        DRAW_TASK_QUEUE = taskQueue;
        DRAW_BUFFER = drawBuffer;
        DISPLAY_BUFFER = displayBuffer;
        ASSIGNED_ROW = assignedRow;
        PIXELS_PER_LAYER = DRAW_BUFFER.length;
        MAX_LAYER_COUNT = renderables.length;
        MAX_COLUMNS = maxColumns;
    }
    
    @Override
    public void run() {
        for (int column = 0; column < MAX_COLUMNS; column++) {
            int pixelIndex = RowMajor.getIndex(ASSIGNED_ROW, column, MAX_COLUMNS);

            DRAW_BUFFER[pixelIndex] = findVisibleCharacter(pixelIndex, MAX_LAYER_COUNT - 1);

            if (DRAW_BUFFER[pixelIndex] != DISPLAY_BUFFER[pixelIndex]) {
                DRAW_TASK_QUEUE.offer(new Position(column, ASSIGNED_ROW));
            }
        }
    }

    private char findVisibleCharacter(int rowMajorIndex, int depth) throws IndexOutOfBoundsException {
        if (rowMajorIndex >= PIXELS_PER_LAYER) throw new IndexOutOfBoundsException();
        if (depth < 0 || depth >= MAX_LAYER_COUNT) throw new IndexOutOfBoundsException();

        IRenderableLayer renderLayer = RENDERABLE_LAYERS[depth];
        if (renderLayer == null) {
            if (depth == 0) return ' ';

            return findVisibleCharacter(rowMajorIndex, depth - 1);
        } else if (depth == 0) return renderLayer.getPixel(rowMajorIndex);

        if (!renderLayer.isPixelVisible(rowMajorIndex)) {
            return findVisibleCharacter(rowMajorIndex, depth - 1);
        }

        return renderLayer.getPixel(rowMajorIndex);
    }

    private final IRenderableLayer[] RENDERABLE_LAYERS;
    private final ConcurrentLinkedQueue<Position> DRAW_TASK_QUEUE;
    private final char[] DRAW_BUFFER;
    private final char[] DISPLAY_BUFFER;

    private final int ASSIGNED_ROW;
    private final int PIXELS_PER_LAYER;
    private final int MAX_LAYER_COUNT;
    private final int MAX_COLUMNS;
}
