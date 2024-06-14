package org.maikodev.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.maikodev.rendering.layers.CoffeeForeground;
import org.maikodev.rendering.IRenderableLayer;
import org.maikodev.order.Position;
import org.maikodev.rendering.ScheduleDrawTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ScheduleDrawTaskTest {

    @Before
    public void Init() throws NoSuchMethodException{
        coffee = new CoffeeForeground();
        IRenderableLayer coffeeRender = coffee.getDisplay();

        MAX_LAYER_COUNT = 5;

        RENDERABLE_LAYERS = new IRenderableLayer[MAX_LAYER_COUNT];
        RENDERABLE_LAYERS[0] = coffeeRender;

        MAX_COLUMNS = coffeeRender.getBufferWidth();

        DRAW_BUFFER = new char[MAX_COLUMNS * coffeeRender.getBufferHeight()];
        TASK_QUEUE = new ConcurrentLinkedQueue<>();

        Arrays.fill(DRAW_BUFFER, ' ');

        testMethod = ScheduleDrawTask.class.getDeclaredMethod("findVisibleCharacter", int.class, int.class);
        testMethod.setAccessible(true);
    }

    @Test
    public void findVisibleCharacter_returns_top_most_visible_character() throws IllegalAccessException, InvocationTargetException {
        ScheduleDrawTask task = new ScheduleDrawTask(RENDERABLE_LAYERS, TASK_QUEUE, DRAW_BUFFER, null, 0, MAX_COLUMNS);

        char actual = (char)testMethod.invoke(task, 0, MAX_LAYER_COUNT - 1);
        Assert.assertEquals(RENDERABLE_LAYERS[0].getPixel(0), actual);

        RENDERABLE_LAYERS[0] = null;
        task = new ScheduleDrawTask(RENDERABLE_LAYERS, TASK_QUEUE, DRAW_BUFFER, null, 0, MAX_COLUMNS);
        actual = (char)testMethod.invoke(task, 0, MAX_LAYER_COUNT - 1);
        Assert.assertEquals(' ', actual);
    }

    private Method testMethod;
    private IRenderableLayer[] RENDERABLE_LAYERS;
    private char[] DRAW_BUFFER;

    private CoffeeForeground coffee;

    private ConcurrentLinkedQueue<Position> TASK_QUEUE;

    private int MAX_LAYER_COUNT;
    private int MAX_COLUMNS;
}
