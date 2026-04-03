package org.example;

import java.util.Random;

public class Main {

    private static final int THREAD_COUNT = 4;

    public static void main(String[] args) {
        Random rand = new Random();

        Thread[] threads = new Thread[THREAD_COUNT];
        Imitation[] workers = new Imitation[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            int a = rand.nextInt(100000) + 1;
            int b = rand.nextInt(40) + 11;
            System.out.println(String.format("Thread %d: id - %d; iterations - %d", i, a, b));
            workers[i] = new Imitation(a, b);
            threads[i] = new Thread(workers[i]);
            threads[i].start();
        }

        while (true) {
            boolean allCompleted = true;

            for (int i = 0; i < THREAD_COUNT; i++) {
                if (workers[i].getPercent() < 100) {
                    allCompleted = false;
                    break;
                }
            }

            if (allCompleted) {
                break;
            }

            clearWindow();
            printBar(workers);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < THREAD_COUNT; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n\nRESULT");
        for (int i = 0; i < THREAD_COUNT; i++) {
            long time = workers[i].getTime();
            int id = workers[i].getId();
            int repeat = workers[i].getTotalRepeats();
            System.out.println(String.format("Thread %d (id: %d; repeat: %d): %d мс", i, id, repeat, time));
        }
    }

    public static void clearWindow() {
        System.out.println("\n".repeat(20));
    }

    public static void printBar(Imitation[] workers) {
        for (int num = 0; num < THREAD_COUNT; num++) {
            float per = workers[num].getPercent();
            int countBlack = (int) (per / 5);
            int countClear = 20 - countBlack;
            int id = workers[num].getId();
            int percentInt = (int) per;

            System.out.println(String.format("%d) ID %-5d [" + "█".repeat(countBlack) + " ".repeat(countClear) + "] %d%%", num, id, percentInt));
        }
    }
}