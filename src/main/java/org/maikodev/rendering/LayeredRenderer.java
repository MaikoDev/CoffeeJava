package org.maikodev.rendering;

import java.io.OutputStream;

public class LayeredRenderer {
    public LayeredRenderer(OutputStream out, int maxRows, int maxColumns) {
        this.MAX_ROWS = maxRows;
        this.MAX_COLUMNS = maxColumns;

        RENDERABLE_LAYERS = new IRenderable[MAX_LAYER_COUNT];
    }

    public LayeredRenderer(IRenderable renderable) throws NullPointerException {
        if (renderable == null) throw new NullPointerException();

        this.MAX_ROWS = renderable.getBufferHeight();
        this.MAX_COLUMNS = renderable.getBufferWidth();

        RENDERABLE_LAYERS = new IRenderable[MAX_LAYER_COUNT];
        addLayer(renderable, 0);
    }

    public void addLayer(IRenderable renderable, int layer, boolean shouldOverwrite) throws IndexOutOfBoundsException{
        if (layer < 0 || layer >= MAX_LAYER_COUNT) throw new IndexOutOfBoundsException();
        if (RENDERABLE_LAYERS[layer] != null && !shouldOverwrite) return;

        RENDERABLE_LAYERS[layer] = renderable;
    }

    public void addLayer(IRenderable renderable, int layer) throws IndexOutOfBoundsException {
        addLayer(renderable, layer, false);
    }

    private final int MAX_ROWS;
    private final int MAX_COLUMNS;

    private final IRenderable[] RENDERABLE_LAYERS;

    private static final byte MAX_LAYER_COUNT = (byte)5;
}
