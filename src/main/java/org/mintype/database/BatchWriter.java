package org.mintype.database;

import org.mintype.database.model.LogEntry;

public class BatchWriter implements Runnable {

    private final LogQueue queue;
    private final Database database;

    private volatile boolean running = true;

    public BatchWriter(LogQueue queue, Database database) {
        this.queue = queue;
        this.database = database;
    }

    @Override
    public void run() {
        while (running) {
            try {
                LogEntry entry = queue.take();
                database.insert(entry);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        running = false;
    }
}