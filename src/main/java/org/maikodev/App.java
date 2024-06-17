package org.maikodev;

import org.maikodev.rendering.LayeredRenderer;
import org.maikodev.rendering.TerminalDisplayLayer;
import org.maikodev.rendering.layers.CoffeeForeground;
import org.maikodev.rendering.layers.ParticleLayer;
import org.maikodev.thread.task.CleanupTask;

import java.io.IOException;
import java.util.Scanner;

public class App {
    public App() throws IOException {
        CoffeeForeground foregroundLayer = new CoffeeForeground();
        TerminalDisplayLayer coffeeDisplay = foregroundLayer.getDisplay();

        particleLayer = new ParticleLayer(coffeeDisplay.getBufferWidth(), coffeeDisplay.getBufferHeight(), (byte)127, 2000);

        renderer = new LayeredRenderer(coffeeDisplay, System.out);
        renderer.addLayer(particleLayer.getDisplay(), 1);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(new CleanupTask()));

        App coffeeApp = new App();
        coffeeApp.run();
    }

    public void run() throws InterruptedException {
        while (true) {
            particleLayer.update();

            renderer.scheduleDraws();
            renderer.draw();

            Thread.sleep(8);
        }
    }

    private Scanner input;

    private final ParticleLayer particleLayer;
    private final LayeredRenderer renderer;
}
