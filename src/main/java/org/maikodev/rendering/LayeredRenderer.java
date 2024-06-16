package org.maikodev.rendering;

import org.maikodev.order.Position;
import org.maikodev.order.RowMajor;
import org.maikodev.thread.task.ScheduleDrawTask;
import org.maikodev.thread.ThreadPool;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LayeredRenderer {
    public LayeredRenderer(IRenderableLayer renderable, OutputStream out) throws NullPointerException, IOException {
        if (renderable == null) throw new NullPointerException();

        this.MAX_ROWS = renderable.getBufferHeight();
        this.MAX_COLUMNS = renderable.getBufferWidth();

        CONSOLE_OUT = new PrintWriter(out);

        Position consoleDimensions = initConsole();

        HALF_POS_X = consoleDimensions.getColumn() / 2 - MAX_COLUMNS / 2;
        HALF_POS_Y = consoleDimensions.getRow() / 2 - MAX_ROWS / 2;

        PIXELS_PER_LAYER = MAX_ROWS * MAX_COLUMNS;
        THREAD_POOL = ThreadPool.getPool();

        RENDERABLE_LAYERS = new IRenderableLayer[MAX_LAYER_COUNT];
        addLayer(renderable, 0);

        DISPLAY_BUFFER = new char[PIXELS_PER_LAYER];
        DRAW_BUFFER = new char[PIXELS_PER_LAYER];

        Arrays.fill(DISPLAY_BUFFER, ' ');
        Arrays.fill(DRAW_BUFFER, ' ');

        DRAW_TASK_QUEUE = new ConcurrentLinkedQueue<>();
        SCHEDULE_DRAWS = new ArrayList<>();

        for (int row = 0; row < MAX_ROWS; row++) {
            SCHEDULE_DRAWS.add(Executors.callable(new ScheduleDrawTask(RENDERABLE_LAYERS, DRAW_TASK_QUEUE, DRAW_BUFFER, DISPLAY_BUFFER, row, MAX_COLUMNS)));
        }

        CONSOLE_OUT.flush();
    }

    public void scheduleDraws() throws InterruptedException {
        /* Go through every pixel on DRAW_BUFFER and DISPLAY_BUFFER to check if
         * they are different and need to be redrawn. */
        THREAD_POOL.invokeAll(SCHEDULE_DRAWS);
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

            moveCursor(drawPosition.y + HALF_POS_Y, drawPosition.x + HALF_POS_X);
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
        CONSOLE_OUT.printf(MOVE_CURSOR, row, column);
        CONSOLE_OUT.flush();
    }

    private void moveCursor(int row, int column) {
        CONSOLE_OUT.printf(MOVE_CURSOR, row, column);
    }

    private Position initConsole() {
        int rows = 0, cols = 0;
        boolean isResolutionValid = false;

        CONSOLE_OUT.print(HIDE_CURSOR);
        do {
            for (String escapeCode : INIT_ESCAPE_CODES) {
                CONSOLE_OUT.print(escapeCode);
            }

            CONSOLE_OUT.print(CONSOLE_CLEAR);
            CONSOLE_OUT.print(MOVE_CURSOR_ORIGIN);

            CONSOLE_OUT.println("Welcome to CoffeeJava!");
            CONSOLE_OUT.println("[Enter to Start] or [Ctrl-C to Exit]");
            CONSOLE_OUT.flush();

            try {
                int iterations = 0;
                StringBuilder sb = new StringBuilder();
                byte[] buff = new byte[1];
                while (System.in.read(buff, 0, 1) != -1 && iterations < 10) {
                    sb.append((char) buff[0]);

                    if ('R' == buff[0]) {
                        break;
                    }

                    iterations++;
                }

                String size = sb.toString();
                rows = Integer.parseInt(size.substring(size.indexOf("\u001b[") + 2, size.indexOf(';')));
                cols = Integer.parseInt(size.substring(size.indexOf(';') + 1, size.indexOf('R')));
            } catch (Exception ex) {
                continue;
            }

            isResolutionValid = true;
        } while (!isResolutionValid);

        CONSOLE_OUT.print(CONSOLE_CLEAR);
        CONSOLE_OUT.print(MOVE_CURSOR_ORIGIN);

        return new Position(cols, rows);
    }

    private final int MAX_ROWS;
    private final int MAX_COLUMNS;

    private final int PIXELS_PER_LAYER;

    private final PrintWriter CONSOLE_OUT;

    private final int HALF_POS_X;
    private final int HALF_POS_Y;

    private final ExecutorService THREAD_POOL;
    private final List<Callable<Object>> SCHEDULE_DRAWS;
    private final ConcurrentLinkedQueue<Position> DRAW_TASK_QUEUE;

    private final IRenderableLayer[] RENDERABLE_LAYERS;
    private final char[] DISPLAY_BUFFER;
    private final char[] DRAW_BUFFER;

    private static final byte MAX_LAYER_COUNT = (byte)5;

    private static final String CONSOLE_CLEAR = "\u001b[2J";
    private static final String HIDE_CURSOR = "\u001b[?25l";
    private static final String MOVE_CURSOR = "\u001b[%d;%dH";
    private static final String REQUEST_CURSOR_POS = "\u001b[6n";
    private static final String RESTORE_CURSOR_POS = "\u001b[u";
    private static final String MAX_CURSOR_POS = "\u001b[5000;5000H";
    private static final String SAVE_CURSOR_POS = "\u001b[s";
    private static final String MOVE_CURSOR_ORIGIN = "\u001b[H";

    private static final String[] INIT_ESCAPE_CODES = new String[]{ SAVE_CURSOR_POS, MAX_CURSOR_POS, REQUEST_CURSOR_POS, RESTORE_CURSOR_POS };
}
