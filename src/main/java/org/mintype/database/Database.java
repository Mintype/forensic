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
     * Closes the database connection.
     */
    void close();
}