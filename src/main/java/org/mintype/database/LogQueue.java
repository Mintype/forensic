package org.mintype.database;

import org.mintype.database.model.LogEntry;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogQueue {

    private final BlockingQueue<LogEntry> queue = new LinkedBlockingQueue<>();

    public void enqueue(LogEntry entry) {
        queue.offer(entry);
    }

    public LogEntry take() throws InterruptedException {
        return queue.take();
    }

    public void drainTo(Collection<LogEntry> collection, int maxElements) {
        queue.drainTo(collection, maxElements);
    }
}