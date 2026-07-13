package org.mintype.database;


import org.mintype.database.model.LogEntry;

import java.util.ArrayList;
import java.util.List;

public class BatchWriter implements Runnable {

    private final LogQueue queue;
    private final Database database;

    private volatile boolean running = true;

    private static final int MAX_BATCH_SIZE = 500;
    private static final long BATCH_DELAY_MS = 50;

    public BatchWriter(LogQueue queue, Database database) {
        this.queue = queue;
        this.database = database;
    }

    @Override
    public void run() {

        while (running) {

            try {
                List<LogEntry> batch = new ArrayList<>();

                // Wait for at least one entry
                batch.add(queue.take());

                // Wait 50ms for potentially more logs to enter the queue
                Thread.sleep(BATCH_DELAY_MS);

                // Grab up to 499 more
                queue.drainTo(batch, MAX_BATCH_SIZE - 1);

                database.insertBatch(batch);

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