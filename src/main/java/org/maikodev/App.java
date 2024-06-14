package org.maikodev;

import org.maikodev.rendering.LayeredRenderer;
import org.maikodev.rendering.layers.CoffeeForeground;

import java.util.Scanner;

public class App {
    public App() {
        isRunning = true;
        coffeeDisplay = new CoffeeForeground();
        renderer = new LayeredRenderer(coffeeDisplay.getDisplay(), System.out);

        input = new Scanner(System.in);
    }

    public static void main(String[] args) throws InterruptedException {
        App coffeeApp = new App();
        coffeeApp.run();
    }

    public void run() throws InterruptedException {
        while (true) {
            renderer.render();
            renderer.draw();
        }
    }

    private Scanner input;

    private CoffeeForeground coffeeDisplay;
    private LayeredRenderer renderer;
    private boolean isRunning;
}
