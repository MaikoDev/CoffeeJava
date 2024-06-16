package org.maikodev.thread.task;

public class CleanupTask implements Runnable {
    @Override
    public void run() {
        System.out.println("\u001b[?25h");
        System.out.println("\u001b[2J");
    }
}
