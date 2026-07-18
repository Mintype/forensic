package org.mintype.database;

import org.mintype.database.model.LogEntry;

import java.util.List;

public interface Database {

    /**
     * Opens the database connection and initializes the schema.
     */
    void init();

    /**
     * Inserts a single log entry.
     */
    void insert(LogEntry entry);

    /**
     * Inserts a batch of log entries.
     */
    void insertBatch(List<LogEntry> entries);

    /**
     * Gets logs at a specific block position.
     */
    List<LogEntry> getLogs(
            String world,
            int x,
            int y,
            int z,
            int limit
    );

    /**
     * Closes the database connection.
     */
    void close();
}