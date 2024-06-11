package org.maikodev;

import org.maikodev.rendering.LayeredRenderer;

import java.util.Scanner;

public class App {
    public App() {
        isRunning = true;
        coffeeDisplay = new JavaCoffee();
        renderer = new LayeredRenderer(coffeeDisplay.getDisplay());

        input = new Scanner(System.in);
    }

    public static void main(String[] args) {
        App coffeeApp = new App();
        coffeeApp.run();
    }

    public void run() {
        System.out.print("\u001b[2J");
        System.out.print("\u001b[?25l");
        System.out.print("\u001b[H");

        String t = input.nextLine();
    }

    private Scanner input;

    private JavaCoffee coffeeDisplay;
    private LayeredRenderer renderer;
    private boolean isRunning;
}
