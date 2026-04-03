package org.example;

public class Imitation implements Runnable {
    private int id;
    private volatile float percent;
    private int totalRepeats;
    private volatile int currentRepeats;
    private volatile long time;

    public Imitation(int id, int count) {
        this.id = id;
        this.totalRepeats = count;
        this.currentRepeats = 0;
        this.percent = 0;
        this.time = 0;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();

        while (currentRepeats < totalRepeats) {
            currentRepeats++;
            percent = ((float) currentRepeats * 100) / totalRepeats;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        percent = 100;

        long endTime = System.currentTimeMillis();
        time = endTime - startTime;
    }

    public float getPercent() {
        return percent;
    }

    public int getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public int getTotalRepeats() {
        return totalRepeats;
    }
}