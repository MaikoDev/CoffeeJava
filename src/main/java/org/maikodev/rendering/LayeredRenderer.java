package org.maikodev.rendering;

import org.maikodev.order.Position;
import org.maikodev.order.RowMajor;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LayeredRenderer {
    /*public LayeredRenderer(OutputStream out, int maxRows, int maxColumns) {
        this.MAX_ROWS = maxRows;
        this.MAX_COLUMNS = maxColumns;

        PIXELS_PER_LAYER = maxRows * maxColumns;

        RENDERABLE_LAYERS = new IRenderable[MAX_LAYER_COUNT];

        DISPLAY_BUFFER = new char[PIXELS_PER_LAYER];
        DRAW_BUFFER = new char[PIXELS_PER_LAYER];

        DRAW_TASK_QUEUE = new ConcurrentLinkedQueue<>();
    }*/

    public LayeredRenderer(IRenderableLayer renderable, OutputStream out) throws NullPointerException {
        if (renderable == null) throw new NullPointerException();

        this.MAX_ROWS = renderable.getBufferHeight();
        this.MAX_COLUMNS = renderable.getBufferWidth();

        CONSOLE_OUT = new PrintWriter(out);

        CONSOLE_OUT.print(CONSOLE_CLEAR_CODE);
        //CONSOLE_OUT.print(HIDE_CURSOR_CODE);
        CONSOLE_OUT.print(MOVE_CURSOR_ORIGIN);

        PIXELS_PER_LAYER = MAX_ROWS * MAX_COLUMNS;
        SCHEDULE_POOL = Executors.newFixedThreadPool(5);

        RENDERABLE_LAYERS = new IRenderableLayer[MAX_LAYER_COUNT];
        addLayer(renderable, 0);

        DISPLAY_BUFFER = new char[PIXELS_PER_LAYER];
        DRAW_BUFFER = new char[PIXELS_PER_LAYER];

        Arrays.fill(DISPLAY_BUFFER, ' ');
        Arrays.fill(DRAW_BUFFER, ' ');

        DRAW_TASK_QUEUE = new ConcurrentLinkedQueue<>();
        CONSOLE_OUT.flush();
    }

    public void render() throws InterruptedException {
        List<Callable<Object>> renderTask = new ArrayList<>();

        for (int row = 0; row < MAX_ROWS; row++) {
            renderTask.add(Executors.callable(new ScheduleDrawTask(RENDERABLE_LAYERS, DRAW_TASK_QUEUE, DRAW_BUFFER, DISPLAY_BUFFER, row, MAX_COLUMNS)));
        }

        SCHEDULE_POOL.invokeAll(renderTask);
    }

    public void draw() {
        if (DRAW_TASK_QUEUE.isEmpty()) return;

        Position drawPosition;
        int bufferPosition;
        char displayCharacter;

        while(!DRAW_TASK_QUEUE.isEmpty()) {
            drawPosition = DRAW_TASK_QUEUE.poll();

            bufferPosition = RowMajor.getIndex(drawPosition.y, drawPosition.x, MAX_COLUMNS);
            displayCharacter = DRAW_BUFFER[bufferPosition];

            moveCursor(drawPosition.y, drawPosition.x);
            CONSOLE_OUT.print(displayCharacter);

            DISPLAY_BUFFER[bufferPosition] = displayCharacter;
        }

        CONSOLE_OUT.flush();
    }

    public void addLayer(IRenderableLayer renderable, int layer, boolean shouldOverwrite) throws IndexOutOfBoundsException{
        if (layer < 0 || layer >= MAX_LAYER_COUNT) throw new IndexOutOfBoundsException();
        if (RENDERABLE_LAYERS[layer] != null && !shouldOverwrite) return;

        RENDERABLE_LAYERS[layer] = renderable;
    }

    public void addLayer(IRenderableLayer renderable, int layer) throws IndexOutOfBoundsException {
        addLayer(renderable, layer, false);
    }

    public void removeLayer(int layer) throws IndexOutOfBoundsException {
        if (layer < 0 || layer >= MAX_LAYER_COUNT) throw new IndexOutOfBoundsException();
        if (RENDERABLE_LAYERS[layer] == null) return;

        RENDERABLE_LAYERS[layer] = null;
    }

    private void moveCursorf(int row, int column) {
        CONSOLE_OUT.printf(MOVE_CURSOR_CODE, row, column);
        CONSOLE_OUT.flush();
    }

    private void moveCursor(int row, int column) {
        CONSOLE_OUT.printf(MOVE_CURSOR_CODE, row, column);
    }

    private final int MAX_ROWS;
    private final int MAX_COLUMNS;

    private final int PIXELS_PER_LAYER;

    private final PrintWriter CONSOLE_OUT;

    private final ExecutorService SCHEDULE_POOL;
    private final ConcurrentLinkedQueue<Position> DRAW_TASK_QUEUE;

    private final IRenderableLayer[] RENDERABLE_LAYERS;
    private final char[] DISPLAY_BUFFER;
    private final char[] DRAW_BUFFER;

    private static final byte MAX_LAYER_COUNT = (byte)5;

    private static final String CONSOLE_CLEAR_CODE = "\u001b[2J";
    private static final String HIDE_CURSOR_CODE   = "\u001b[?25l";
    private static final String MOVE_CURSOR_CODE   = "\u001b[%d;%dH";
    private static final String MOVE_CURSOR_ORIGIN = "\u001b[H";
}
