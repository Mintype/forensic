package org.mintype.logging;

import com.google.gson.JsonObject;
import org.mintype.database.model.ActionType;
import org.mintype.database.model.LogEntry;

import java.util.UUID;

public class LoggerService {

    private final LogQueue queue;

    public LoggerService(LogQueue queue) {
        this.queue = queue;
    }

    public void log(
            UUID player,
            ActionType action,
            String world,
            int x,
            int y,
            int z,
            JsonObject data
    ) {
        LogEntry entry = new LogEntry(
                0,
                player,
                action,
                world,
                x,
                y,
                z,
                System.currentTimeMillis(),
                data
        );

        queue.enqueue(entry);
    }
}